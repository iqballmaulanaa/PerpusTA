package com.perpussapp.perpusapp.Activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
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
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

import java.util.ArrayList;
import java.util.Calendar;

public class ConfirmationActivity extends BaseActivity {
    private Constant constant;
    private ArrayList<ListBukuModel> listBukuModels;
    private ListBukuAdapter listBukuAdapter;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private DatabaseReference mDatabase;
    private TextView txtTanggal,txtNis,txtNama,
            txtKembali,txtSim,txtBatas;
    private CardView c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        constant = new Constant(this);
        constant.setListUser(getIntent().getParcelableArrayListExtra("listUser"));
        mDatabase = FirebaseDatabase.getInstance().getReference();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        c = findViewById(R.id.c);
        recyclerView = findViewById(R.id.recyclerView);
        txtSim = findViewById(R.id.txtSim);
        txtBatas = findViewById(R.id.txtBatas);
        txtKembali = findViewById(R.id.txtKembali);
        txtNama = findViewById(R.id.txtNama);
        txtNis = findViewById(R.id.txtNis);

        txtTanggal = findViewById(R.id.txtTanggal);
        listBukuModels = new ArrayList<>();
        listBukuAdapter = new ListBukuAdapter(this,listBukuModels,
                getIntent().getParcelableArrayListExtra("listBuku"), false );
        recyclerView.setAdapter(listBukuAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setData();
        if (constant.getLevel(this)==1){
            c.setVisibility(View.VISIBLE);
        }else {
            c.setVisibility(View.GONE);
        }
    }

    private void setData() {
        mDatabase.child("listKembali").child(getIntent().getStringExtra("pinjamKey"))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            txtSim.setBackgroundColor(getResources().getColor(cn.pedant.SweetAlert.R.color.red_btn_bg_color ));
                            txtSim.setText("Belum di Kembalikan");
                            txtKembali.setText(constant.changeFromLong(snapshot.child("tanggalKembali").getValue(Long.class)));
                            txtSim.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmationActivity.this);
                                    builder.setCancelable(true);
                                    builder.setTitle("Konfirmasi");
                                    builder.setMessage("Apakah Kamu Yakin Akan Menghapus Dari Daftar Pengembalian Buku?");
                                    builder.setPositiveButton("Iya",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    mDatabase.child("listKembali")
                                                            .child(getIntent().getStringExtra("pinjamKey"))
                                                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(ConfirmationActivity.this, "berhasil menghapus", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    });

                                                }
                                            });
                                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });

                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            });
                        }else {
                            txtSim.setBackgroundColor(getResources().getColor(cn.pedant.SweetAlert.R.color.main_green_color));
                            findViewById(R.id.tbRow).setVisibility(View.GONE);
                            txtKembali.setVisibility(View.GONE);
                            txtSim.setText("Sudah di Kembalikan");
                            txtSim.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    formInput();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        mDatabase.child("listPinjam").child(getIntent().getStringExtra("pinjamKey"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        txtBatas.setText(constant.changeFromLong(snapshot.child("tglBatas").getValue(Long.class)));
                        txtTanggal.setText(constant.changeFromLong(snapshot.child("tanggal").getValue(Long.class)));
                        txtNama.setText(constant.getNamaByNis(snapshot.child("nis").getValue(String.class)));
                        txtNis.setText(snapshot.child("nis").getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        mDatabase.child("listBookPinjam").child(getIntent().getStringExtra("pinjamKey"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            ListBukuModel l = dataSnapshot.getValue(ListBukuModel.class);
                            listBukuModels.add(l);
                        }
                        listBukuAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void formInput() {
        View view1 = LayoutInflater.from(ConfirmationActivity.this).inflate(R.layout.form_kembalikan,null  );
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmationActivity.this);
        builder.setTitle("Pengembalian Buku");
        builder.setView(view1);
        TextInputEditText edtTanggalKembali  = view1.findViewById(R.id.edtTanggalKembali);
        edtTanggalKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(ConfirmationActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        edtTanggalKembali.setText(constant.changeFromDate(newDate.getTime()));
                        String yyyymmd = constant.changeFromDate(newDate.getTime()) +" 00:00:00";
                        // kembaliModel.setTanggalKembali(constant.changeYyyyMMDDtoMili(yyyymmd));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        builder.setPositiveButton("Simpan", null);
        builder.setNegativeButton("Cancel", null);
        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (edtTanggalKembali.length()==0){
                            edtTanggalKembali.setError("Tanggal Masih Kosong");
                            return;
                        }
                        mDatabase.child("listKembali")
                                .child(getIntent().getStringExtra("pinjamKey"))
                                .child("tanggalKembali")
                                .setValue(constant.changeYyyyMMDDtoMili(edtTanggalKembali.getText().toString().trim()+" 00:00:00"))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mAlertDialog.dismiss();
                                        Toast.makeText(ConfirmationActivity.this, "berhasil menyimpan", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });

                    }
                });
            }
        });
        mAlertDialog.show();
    }
}