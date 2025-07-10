package com.perpussapp.perpusapp.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.perpussapp.perpusapp.Model.ListBukuModel;
import com.perpussapp.perpusapp.Model.PinjamModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.Constant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class PinjamAdapter extends RecyclerView.Adapter<PinjamAdapter.ViewHolder> implements Filterable {

    private static final int BATAS_PEMINJAMAN_HARI = 7;
    private static final int DENDA_PER_HARI = 1000;

    private List<PinjamModel> pinjamModels;
    private List<PinjamModel> dataListfull = new ArrayList<>();
    private Context context;
    private DatabaseReference mDatabase;
    private CallBack mCallBack;
    Constant constant;

    public interface CallBack {
        void onClick(int position, boolean isEdit);
    }

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }

    public PinjamAdapter(Context context, ArrayList<PinjamModel> pinjamModels) {
        this.pinjamModels = pinjamModels;
        this.context = context;
        this.dataListfull = pinjamModels;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.constant = new Constant(context);
    }

    // Make this method public to access from activities
    public PinjamModel getFilteredItem(int position) {
        if (position >= 0 && position < pinjamModels.size()) {
            return pinjamModels.get(position);
        }
        return null;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase().trim();
                if (charString.isEmpty()) {
                    pinjamModels = dataListfull;
                } else {
                    List<PinjamModel> filteredList = new ArrayList<>();
                    for (PinjamModel row : dataListfull) {
                        if ((row.getNama() != null && row.getNama().toLowerCase().contains(charString)) ||
                                (row.getNis() != null && row.getNis().toLowerCase().contains(charString)) ||
                                constant.changeFromLong(row.getTanggal()).contains(charString)) {
                            filteredList.add(row);
                        }
                    }
                    pinjamModels = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = pinjamModels;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                pinjamModels = (ArrayList<PinjamModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_pinjam, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PinjamModel pinjamModel = pinjamModels.get(position);
        holder.txtStatus.setVisibility(View.VISIBLE);
        PopupMenu pop = new PopupMenu(context, holder.imgMore);
        pop.inflate(R.menu.menu_item);

        getUserInfo(pinjamModel, holder);
        holder.txtTanggal.setText("Tanggal Pinjam: " + constant.changeFromLong(pinjamModel.getTanggal()));

        checkReturnStatus(pinjamModel, holder, pop, position);

        holder.imgMore.setOnClickListener(v -> pop.show());
        pop.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.kembalikan) {
                handleReturnAction(pinjamModel);
                return true;
            } else if (item.getItemId() == R.id.edit) {
                if (mCallBack != null) mCallBack.onClick(position, true);
                return true;
            } else {
                showDeleteConfirmation(pinjamModel, position);
                return true;
            }
        });

        holder.parentRelative.setOnClickListener(v -> {
            if (mCallBack != null) mCallBack.onClick(position, false);
        });

        setupUserLevelUI(holder);
    }

    private long hitungDenda(PinjamModel pinjamModel, DataSnapshot returnSnapshot) {
        if (returnSnapshot == null || !returnSnapshot.hasChild("tanggalKembali")) {
            return 0; // Belum dikembalikan
        }

        Long tanggalKembali = returnSnapshot.child("tanggalKembali").getValue(Long.class);
        if (tanggalKembali == null) {
            return 0;
        }

        long batasTanggal = pinjamModel.getTglBatas(); // gunakan batas waktu pinjam
        long selisihHari = (tanggalKembali - batasTanggal) / (1000 * 60 * 60 * 24);
        if (selisihHari <= 0) {
            return 0; // Tidak terlambat
        }

        return selisihHari * DENDA_PER_HARI; // Denda dihitung berdasarkan hari keterlambatan

    }

    private void refreshData() {
        mDatabase.child("listPinjam").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PinjamModel> updatedList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PinjamModel model = dataSnapshot.getValue(PinjamModel.class);
                    if (model != null) {
                        model.setKey(dataSnapshot.getKey());
                        updatedList.add(model);
                    }
                }
                pinjamModels = updatedList;
                dataListfull = updatedList;
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Gagal memuat data terbaru", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkReturnStatus(PinjamModel pinjamModel, ViewHolder holder, PopupMenu pop, int position) {
        mDatabase.child("listKembali").child(pinjamModel.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long denda = hitungDenda(pinjamModel, snapshot);
                pinjamModel.setDenda(denda);

                if (denda > 0) {
                    holder.txtDenda.setText("Denda: Rp" + denda);
                    holder.txtDenda.setVisibility(View.VISIBLE);
                } else {
                    holder.txtDenda.setVisibility(View.GONE);
                }

                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if (status != null) {
                        switch (status) {
                            case "sudah_dikembalikan":
                                holder.txtStatus.setText("Sudah dikembalikan");
                                holder.txtStatus.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                                pop.getMenu().findItem(R.id.kembalikan).setVisible(true).setTitle("Belum dikembalikan");
                                break;
                            case "menunggu_konfirmasi":
                                holder.txtStatus.setText("Menunggu konfirmasi");
                                holder.txtStatus.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
                                pop.getMenu().findItem(R.id.kembalikan).setVisible(true).setTitle("Sudah dikembalikan");
                                if (constant.getLevel(context) == 1) {
                                    holder.txtStatus.setOnClickListener(v -> showConfirmationDialog(pinjamModel));
                                }
                                break;
                            case "ditolak":
                                holder.txtStatus.setText("Pengembalian ditolak");
                                holder.txtStatus.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));
                                pop.getMenu().findItem(R.id.kembalikan).setVisible(true).setTitle("Sudah dikembalikan");
                                break;
                        }
                    }
                } else {
                    holder.txtStatus.setText("Belum dikembalikan");
                    holder.txtStatus.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    pop.getMenu().findItem(R.id.kembalikan).setVisible(true).setTitle("Sudah dikembalikan");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Gagal memuat status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUserLevelUI(ViewHolder holder) {
        if (constant.getLevel(context) == 1) {
            holder.circleImageView.setVisibility(View.VISIBLE);
            holder.imgMore.setVisibility(View.VISIBLE);
        } else {
            holder.circleImageView.setVisibility(View.GONE);
            holder.imgMore.setVisibility(View.GONE);
        }
    }

    private void showConfirmationDialog(PinjamModel pinjamModel) {
        new AlertDialog.Builder(context)
                .setTitle("Konfirmasi Pengembalian")
                .setMessage("Terima atau tolak pengembalian buku ini?")
                .setPositiveButton("Terima", (dialog, which) -> {
                    updateReturnStatus(pinjamModel.getKey(), "sudah_dikembalikan");
                })
                .setNegativeButton("Tolak", (dialog, which) -> {
                    updateReturnStatus(pinjamModel.getKey(), "ditolak");
                })
                .show();
    }

    private void updateReturnStatus(String key, String newStatus) {
        mDatabase.child("listKembali").child(key).child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Status diperbarui", Toast.LENGTH_SHORT).show();
                    refreshData();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Gagal memperbarui", Toast.LENGTH_SHORT).show());
    }

    private void handleReturnAction(PinjamModel pinjamModel) {
        mDatabase.child("listKembali").child(pinjamModel.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if (status != null) {
                        switch (status) {
                            case "menunggu_konfirmasi":
                                if (constant.getLevel(context) == 1) {
                                    showConfirmationDialog(pinjamModel);
                                } else {
                                    new AlertDialog.Builder(context)
                                            .setTitle("Konfirmasi")
                                            .setMessage("Pengembalian Anda sedang menunggu konfirmasi")
                                            .setPositiveButton("OK", null)
                                            .show();
                                }
                                break;

                            case "ditolak":
                                showReturnForm(pinjamModel, snapshot);
                                break;

                            case "sudah_dikembalikan":
                                if (constant.getLevel(context) == 1) {
                                    new AlertDialog.Builder(context)
                                            .setTitle("Konfirmasi")
                                            .setMessage("Apakah Anda ingin mengubah status menjadi belum dikembalikan?")
                                            .setPositiveButton("Ya", (dialog, which) -> {
                                                mDatabase.child("listKembali").child(pinjamModel.getKey())
                                                        .removeValue()
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(context, "Status diperbarui", Toast.LENGTH_SHORT).show();
                                                            refreshData();
                                                        });
                                            })
                                            .setNegativeButton("Tidak", null)
                                            .show();
                                } else {
                                    new AlertDialog.Builder(context)
                                            .setTitle("Info")
                                            .setMessage("Buku ini sudah dikembalikan.")
                                            .setPositiveButton("OK", null)
                                            .show();
                                }
                                break;

                            default:
                                showDeleteReturnConfirmation(pinjamModel);
                                break;
                        }
                    }
                } else {
                    showReturnForm(pinjamModel, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showReturnForm(PinjamModel pinjamModel, DataSnapshot existingReturn) {
        View view = LayoutInflater.from(context).inflate(R.layout.form_kembalikan, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Pengembalian Buku");
        builder.setView(view);

        TextView txtInfoDenda = view.findViewById(R.id.txtInfoDenda);
        TextInputEditText edtTanggalKembali = view.findViewById(R.id.edtTanggalKembali);

        // Hitung denda saat ini
        long denda = hitungDenda(pinjamModel, existingReturn);
        if (denda > 0) {
            txtInfoDenda.setText("Denda yang harus dibayar: Rp" + denda);
        } else {
            txtInfoDenda.setText("Tidak ada denda");
        }

        if (existingReturn != null && existingReturn.hasChild("tanggalKembali")) {
            Long dateMillis = existingReturn.child("tanggalKembali").getValue(Long.class);
            if (dateMillis != null) {
                edtTanggalKembali.setText(constant.changeFromLong(dateMillis));
            }
        }

        edtTanggalKembali.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                    (view1, year, month, dayOfMonth) -> {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        edtTanggalKembali.setText(constant.changeFromDate(newDate.getTime()));

                        // Update denda saat tanggal berubah
                        long newReturnDate = newDate.getTimeInMillis();
                        long selisihHari = (newReturnDate - pinjamModel.getTanggal()) / (1000 * 60 * 60 * 24);
                        long newDenda = selisihHari > BATAS_PEMINJAMAN_HARI ?
                                (selisihHari - BATAS_PEMINJAMAN_HARI) * DENDA_PER_HARI : 0;
                        txtInfoDenda.setText(newDenda > 0 ?
                                "Denda yang harus dibayar: Rp" + newDenda : "Tidak ada denda");
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            if (constant.getLevel(context) != 1) {
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            }

            datePickerDialog.show();
        });

        AlertDialog dialog = builder.create();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Simpan", (d, which) -> {});
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, which) -> dialog.dismiss());

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (edtTanggalKembali.length() == 0) {
                    edtTanggalKembali.setError("Tanggal masih kosong");
                    return;
                }

                // Hitung denda final
                long tanggalKembali = constant.changeYyyyMMDDtoMili(edtTanggalKembali.getText().toString().trim() + " 00:00:00");
                long selisihHari = (tanggalKembali - pinjamModel.getTanggal()) / (1000 * 60 * 60 * 24);
                long dendaFinal = selisihHari > BATAS_PEMINJAMAN_HARI ?
                        (selisihHari - BATAS_PEMINJAMAN_HARI) * DENDA_PER_HARI : 0;

                Map<String, Object> returnData = new HashMap<>();
                returnData.put("tanggalKembali", tanggalKembali);
                returnData.put("status", constant.getLevel(context) == 1 ? "sudah_dikembalikan" : "menunggu_konfirmasi");
                returnData.put("denda", dendaFinal);

                mDatabase.child("listKembali").child(pinjamModel.getKey())
                        .updateChildren(returnData)
                        .addOnSuccessListener(aVoid -> {
                            dialog.dismiss();
                            Toast.makeText(context, "Berhasil menyimpan", Toast.LENGTH_SHORT).show();
                            refreshData();
                        });
            });
        });
        dialog.show();
    }

    private void showDeleteReturnConfirmation(PinjamModel pinjamModel) {
        new AlertDialog.Builder(context)
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin akan menghapus dari daftar pengembalian buku?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    mDatabase.child("listKembali").child(pinjamModel.getKey())
                            .removeValue().addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Berhasil menghapus", Toast.LENGTH_SHORT).show();
                                refreshData();
                            });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showDeleteConfirmation(PinjamModel pinjamModel, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin akan menghapus?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    SweetAlertDialog pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.purple_200));
                    pDialog.setTitleText("Loading");
                    pDialog.setCancelable(false);
                    pDialog.show();

                    mDatabase.child("listPinjam").child(pinjamModel.getKey()).removeValue().addOnSuccessListener(aVoid -> {
                        mDatabase.child("listBookPinjam").child(pinjamModel.getKey()).removeValue();
                        pinjamModels.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Berhasil menghapus", Toast.LENGTH_SHORT).show();
                        pDialog.dismissWithAnimation();
                        refreshData();
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void getUserInfo(PinjamModel pinjamModel, ViewHolder holder) {
        mDatabase.child("user").child(pinjamModel.getNis())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nama = snapshot.child("nama").getValue(String.class);
                        pinjamModel.setNama(nama);
                        holder.txtNama.setText("Nama: " + nama);
                        Glide.with(context).load(snapshot.child("image").getValue(String.class)).into(holder.circleImageView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        mDatabase.child("listBookPinjam").child(pinjamModel.getKey())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int tot = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ListBukuModel l = dataSnapshot.getValue(ListBukuModel.class);
                            if (l != null) tot += l.getJumlah();
                        }
                        holder.txtJumlah.setText("Jumlah Pinjam: " + tot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public int getItemCount() {
        return pinjamModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView txtNama, txtJumlah, txtTanggal, txtStatus, txtDenda;
        ImageButton imgMore;
        RelativeLayout parentRelative;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parentRelative = itemView.findViewById(R.id.parentRelative);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDenda = itemView.findViewById(R.id.txtDenda);
            imgMore = itemView.findViewById(R.id.imgMore);
            txtNama = itemView.findViewById(R.id.txtNama);
            txtJumlah = itemView.findViewById(R.id.txtJumlah);
            circleImageView = itemView.findViewById(R.id.circleImageView);
            txtTanggal = itemView.findViewById(R.id.txtTanggal);
        }
    }
}