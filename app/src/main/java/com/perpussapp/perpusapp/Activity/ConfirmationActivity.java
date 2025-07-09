package com.perpussapp.perpusapp.Activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.perpussapp.perpusapp.Adapter.ListBukuAdapter;
import com.perpussapp.perpusapp.Model.ListBukuModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ConfirmationActivity extends BaseActivity {
    private static final String TAG = "ConfirmationActivity";
    private Constant constant;
    private ArrayList<ListBukuModel> listBukuModels;
    private ListBukuAdapter listBukuAdapter;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private DatabaseReference mDatabase;
    private TextView txtTanggal, txtNis, txtNama, txtKembali, txtSim, txtBatas;
    private CardView c;
    private Button btnKembalikan;
    private String pinjamKey;
    private ValueEventListener returnStatusListener;
    private ValueEventListener loanDataListener;
    private ValueEventListener bookDataListener;
    private ValueEventListener buttonStatusListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        constant = new Constant(this);
        constant.setListUser(getIntent().getParcelableArrayListExtra("listUser"));
        mDatabase = FirebaseDatabase.getInstance().getReference();
        pinjamKey = getIntent().getStringExtra("pinjamKey");

        initializeViews();
        setupRecyclerView();
        setupRealTimeListeners();

        if (constant.getLevel(this) == 1) {
            c.setVisibility(View.VISIBLE);
        } else {
            c.setVisibility(View.GONE);
            setupReturnButtonListener();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        c = findViewById(R.id.c);
        recyclerView = findViewById(R.id.recyclerView);
        txtSim = findViewById(R.id.txtSim);
        txtBatas = findViewById(R.id.txtBatas);
        txtKembali = findViewById(R.id.txtKembali);
        txtNama = findViewById(R.id.txtNama);
        txtNis = findViewById(R.id.txtNis);
        txtTanggal = findViewById(R.id.txtTanggal);
        btnKembalikan = findViewById(R.id.btnKembalikanSiswa);
        btnKembalikan.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        listBukuModels = new ArrayList<>();
        listBukuAdapter = new ListBukuAdapter(this, listBukuModels,
                getIntent().getParcelableArrayListExtra("listBuku"), false);
        recyclerView.setAdapter(listBukuAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupRealTimeListeners() {
        // Listener for return status
        returnStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateReturnStatusUI(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Return status listener cancelled: " + error.getMessage());
                Toast.makeText(ConfirmationActivity.this,
                        "Gagal memuat status pengembalian", Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.child("listKembali").child(pinjamKey).addValueEventListener(returnStatusListener);

        // Listener for loan data
        loanDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    txtBatas.setText(constant.changeFromLong(snapshot.child("tglBatas").getValue(Long.class)));
                    txtTanggal.setText(constant.changeFromLong(snapshot.child("tanggal").getValue(Long.class)));
                    txtNama.setText(constant.getNamaByNis(snapshot.child("nis").getValue(String.class)));
                    txtNis.setText(snapshot.child("nis").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Loan data listener cancelled: " + error.getMessage());
            }
        };
        mDatabase.child("listPinjam").child(pinjamKey).addValueEventListener(loanDataListener);

        // Listener for book data
        bookDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listBukuModels.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ListBukuModel l = dataSnapshot.getValue(ListBukuModel.class);
                    if (l != null) {
                        listBukuModels.add(l);
                    }
                }
                listBukuAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Book data listener cancelled: " + error.getMessage());
            }
        };
        mDatabase.child("listBookPinjam").child(pinjamKey).addValueEventListener(bookDataListener);
    }

    private void updateReturnStatusUI(DataSnapshot snapshot) {
        if (snapshot.exists()) {
            String status = snapshot.child("status").getValue(String.class);
            Long tanggalKembali = snapshot.child("tanggalKembali").getValue(Long.class);

            if (status != null) {
                switch (status) {
                    case "menunggu_konfirmasi":
                        txtSim.setBackgroundColor(ContextCompat.getColor(
                                this, R.color.status_waiting));
                        txtSim.setText("Menunggu Konfirmasi");

                        if (constant.getLevel(this) == 1) {
                            txtSim.setOnClickListener(v ->
                                    showAdminConfirmationDialog(pinjamKey));
                        } else {
                            txtSim.setOnClickListener(null);
                        }
                        break;

                    case "sudah_dikembalikan":
                        txtSim.setBackgroundColor(ContextCompat.getColor(
                                this, R.color.status_success));
                        txtSim.setText("Ubah Status Buku Belum di Kembalikan");

                        if (constant.getLevel(this) == 1) {
                            txtSim.setOnClickListener(v ->
                                    showRevertConfirmationDialog());
                        } else {
                            txtSim.setOnClickListener(null);
                        }
                        break;

                    case "ditolak":
                        txtSim.setBackgroundColor(ContextCompat.getColor(
                                this, R.color.status_error));
                        txtSim.setText("Pengembalian Ditolak");
                        txtSim.setOnClickListener(null);
                        break;
                }
            }

            if (tanggalKembali != null) {
                txtKembali.setText(constant.changeFromLong(tanggalKembali));
                findViewById(R.id.tbRow).setVisibility(View.VISIBLE);
                txtKembali.setVisibility(View.VISIBLE);
            }
        } else {
            txtSim.setBackgroundColor(ContextCompat.getColor(
                    this, R.color.status_error));
            txtSim.setText("Kembalikan Buku");
            findViewById(R.id.tbRow).setVisibility(View.GONE);
            txtKembali.setVisibility(View.GONE);

            if (constant.getLevel(this) == 1) {
                txtSim.setOnClickListener(v -> formInput());
            } else {
                txtSim.setOnClickListener(null);
            }
        }
    }

    private void setupReturnButtonListener() {
        buttonStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    btnKembalikan.setVisibility(View.VISIBLE);
                    btnKembalikan.setText("KEMBALIKAN BUKU");
                    btnKembalikan.setOnClickListener(v -> formInput());
                } else {
                    String status = snapshot.child("status").getValue(String.class);
                    if (status != null && status.equals("ditolak")) {
                        btnKembalikan.setVisibility(View.VISIBLE);
                        btnKembalikan.setText("KEMBALIKAN ULANG");
                        btnKembalikan.setOnClickListener(v -> formInput());
                    } else {
                        btnKembalikan.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Button status listener cancelled: " + error.getMessage());
            }
        };
        mDatabase.child("listKembali").child(pinjamKey).addValueEventListener(buttonStatusListener);
    }

    private void showAdminConfirmationDialog(String pinjamKey) {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Pengembalian")
                .setMessage("Terima atau tolak pengembalian buku ini?")
                .setPositiveButton("Terima", (dialog, which) ->
                        updateReturnStatus(pinjamKey, "sudah_dikembalikan"))
                .setNegativeButton("Tolak", (dialog, which) ->
                        updateReturnStatus(pinjamKey, "ditolak"))
                .show();
    }

    private void showRevertConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda ingin mengubah status menjadi Belum di Kembalikan?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    mDatabase.child("listKembali").child(pinjamKey)
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this,
                                        "Status berhasil diubah", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void updateReturnStatus(String key, String newStatus) {
        mDatabase.child("listKembali").child(key).child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status diperbarui", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memperbarui", Toast.LENGTH_SHORT).show();
                });
    }

    private void formInput() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.form_kembalikan, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pengembalian Buku");
        builder.setView(dialogView);

        TextInputEditText edtTanggalKembali = dialogView.findViewById(R.id.edtTanggalKembali);
        edtTanggalKembali.setOnClickListener(v -> showDatePickerDialog(edtTanggalKembali));

        builder.setPositiveButton("Simpan", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(buttonView -> {
                if (edtTanggalKembali.length() == 0) {
                    edtTanggalKembali.setError("Tanggal Masih Kosong");
                    return;
                }

                submitReturnData(edtTanggalKembali.getText().toString().trim(), dialog);
            });
        });
        dialog.show();
    }

    private void showDatePickerDialog(TextInputEditText edtTanggalKembali) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (datePickerView, year, month, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, month, dayOfMonth);
                    edtTanggalKembali.setText(constant.changeFromDate(newDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        if (constant.getLevel(this) != 1) {
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    private void submitReturnData(String tanggalStr, AlertDialog dialog) {
        String fullDateStr = tanggalStr + " 00:00:00";
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("tanggalKembali", constant.changeYyyyMMDDtoMili(fullDateStr));
        updateData.put("status", constant.getLevel(this) == 1 ?
                "sudah_dikembalikan" : "menunggu_konfirmasi");

        mDatabase.child("listKembali").child(pinjamKey)
                .updateChildren(updateData)
                .addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    Toast.makeText(this,
                            constant.getLevel(this) == 1 ?
                                    "Pengembalian berhasil dicatat" :
                                    "Pengembalian berhasil diajukan. Menunggu konfirmasi.",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Gagal menyimpan: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove all listeners when activity is destroyed
        if (returnStatusListener != null) {
            mDatabase.child("listKembali").child(pinjamKey).removeEventListener(returnStatusListener);
        }
        if (loanDataListener != null) {
            mDatabase.child("listPinjam").child(pinjamKey).removeEventListener(loanDataListener);
        }
        if (bookDataListener != null) {
            mDatabase.child("listBookPinjam").child(pinjamKey).removeEventListener(bookDataListener);
        }
        if (buttonStatusListener != null) {
            mDatabase.child("listKembali").child(pinjamKey).removeEventListener(buttonStatusListener);
        }
    }
}