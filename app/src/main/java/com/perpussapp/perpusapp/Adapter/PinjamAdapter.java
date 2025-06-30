package com.perpussapp.perpusapp.Adapter;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class PinjamAdapter extends RecyclerView.Adapter<PinjamAdapter.ViewHolder> implements Filterable {

    private List<PinjamModel> pinjamModels;
    private List<PinjamModel> dataListfull = new ArrayList<>();
    private Context context;
    private DatabaseReference mDatabase;

    private CallBack mCallBack;
    Constant constant;

    @Override
    public Filter getFilter() {
        Constant constant = new Constant(context);
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
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_pinjam, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txtStatus.setVisibility(View.VISIBLE);
        PopupMenu pop = new PopupMenu(context, holder.imgMore);
        pop.inflate(R.menu.menu_item);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        constant = new Constant(context);
        PinjamModel pinjamModel = pinjamModels.get(position);
        getUserInfo(pinjamModel, holder);
        holder.txtTanggal.setText("Tanggal : " + constant.changeFromLong(pinjamModel.getTanggal()));

        mDatabase.child("listKembali")
                .child(pinjamModel.getKey())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            holder.txtStatus.setText("Sudah di Kembalikan");
                            holder.txtStatus.setBackgroundColor(context.getResources().getColor(cn.pedant.SweetAlert.R.color.main_green_color));
                            pop.getMenu().findItem(R.id.kembalikan).setVisible(true).setTitle("Belum di Kembalikan");
                            holder.btnKembalikanSiswa.setVisibility(View.GONE);
                        } else {
                            holder.txtStatus.setText("Belum di Kembalikan");
                            holder.txtStatus.setBackgroundColor(context.getResources().getColor(cn.pedant.SweetAlert.R.color.red_btn_bg_pressed_color));
                            pop.getMenu().findItem(R.id.kembalikan).setVisible(true).setTitle("Sudah di Kembalikan");
                            if (constant.getLevel(context) != 1) {
                                holder.btnKembalikanSiswa.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        holder.imgMore.setOnClickListener(v -> {
            pop.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.kembalikan) {
                    formKembalikan(pinjamModel);
                } else if (item.getItemId() == R.id.edit) {
                    if (mCallBack != null) {
                        mCallBack.onClick(position, true);
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(true);
                    builder.setTitle("Konfirmasi");
                    builder.setMessage("Apakah Kamu Yakin Akan Menghapus");
                    builder.setPositiveButton("Iya", (dialog, which) -> {
                        SweetAlertDialog pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
                        pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.purple_200));
                        pDialog.setTitleText("Loading");
                        pDialog.setCancelable(false);
                        pDialog.show();
                        mDatabase.child("listPinjam").child(pinjamModel.getKey()).removeValue().addOnSuccessListener(aVoid -> {
                            mDatabase.child("listBookPinjam").child(pinjamModel.getKey()).removeValue();
                            pinjamModels.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context, "berhasil menghapus", Toast.LENGTH_SHORT).show();
                            pDialog.dismissWithAnimation();
                        });
                    });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.create().show();
                }
                return true;
            });
            pop.show();
        });

        holder.parentRelative.setOnClickListener(v -> {
            if (mCallBack != null) {
                mCallBack.onClick(position, false);
            }
        });

        if (constant.getLevel(context) == 1) {
            holder.circleImageView.setVisibility(View.VISIBLE);
            holder.imgMore.setVisibility(View.VISIBLE);
            holder.btnKembalikanSiswa.setVisibility(View.GONE);
        } else {
            holder.circleImageView.setVisibility(View.GONE);
            holder.imgMore.setVisibility(View.GONE);
            holder.btnKembalikanSiswa.setVisibility(View.VISIBLE);
            holder.btnKembalikanSiswa.setOnClickListener(v -> formKembalikan(pinjamModel));
        }
    }


    private void formKembalikan(PinjamModel pinjamModel) {
        mDatabase.child("listKembali")
                .child(pinjamModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Konfirmasi")
                                    .setMessage("Apakah Kamu Yakin Akan Menghapus Dari Daftar Pengembalian Buku?")
                                    .setPositiveButton("Iya", (dialog, which) -> {
                                        mDatabase.child("listKembali").child(pinjamModel.getKey())
                                                .removeValue().addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(context, "berhasil menghapus", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .show();
                        } else {
                            View view1 = LayoutInflater.from(context).inflate(R.layout.form_kembalikan, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Pengembalian Buku");
                            builder.setView(view1);
                            TextInputEditText edtTanggalKembali = view1.findViewById(R.id.edtTanggalKembali);

                            edtTanggalKembali.setOnClickListener(v -> {
                                Calendar calendar = Calendar.getInstance();
                                DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                                    Calendar newDate = Calendar.getInstance();
                                    newDate.set(year, month, dayOfMonth);
                                    edtTanggalKembali.setText(constant.changeFromDate(newDate.getTime()));
                                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                                datePickerDialog.show();
                            });

                            builder.setPositiveButton("Simpan", null);
                            builder.setNegativeButton("Cancel", null);
                            AlertDialog mAlertDialog = builder.create();

                            mAlertDialog.setOnShowListener(dialog -> {
                                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                b.setOnClickListener(view -> {
                                    if (edtTanggalKembali.length() == 0) {
                                        edtTanggalKembali.setError("Tanggal Masih Kosong");
                                        return;
                                    }
                                    mDatabase.child("listKembali").child(pinjamModel.getKey())
                                            .child("tanggalKembali")
                                            .setValue(constant.changeYyyyMMDDtoMili(edtTanggalKembali.getText().toString().trim() + " 00:00:00"))
                                            .addOnSuccessListener(aVoid -> {
                                                mAlertDialog.dismiss();
                                                Toast.makeText(context, "berhasil menyimpan", Toast.LENGTH_SHORT).show();
                                            });
                                });
                            });

                            mAlertDialog.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void getUserInfo(PinjamModel pinjamModel, ViewHolder holder) {
        mDatabase.child("user").child(pinjamModel.getNis())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nama = snapshot.child("nama").getValue(String.class);
                        pinjamModel.setNama(nama);
                        holder.txtNama.setText("Nama : " + nama);
                        Glide.with(context).load(snapshot.child("image").getValue(String.class)).into(holder.circleImageView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        mDatabase.child("listBookPinjam").child(pinjamModel.getKey())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int tot = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ListBukuModel l = dataSnapshot.getValue(ListBukuModel.class);
                            tot = l.getJumlah() + tot;
                        }
                        holder.txtJumlah.setText("Jumlah Pinjam : " + tot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return pinjamModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView circleImageView;
        private TextView txtNama, txtJumlah, txtTanggal, txtStatus;
        private ImageButton imgMore;
        private RelativeLayout parentRelative;
        private Button btnKembalikanSiswa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parentRelative = itemView.findViewById(R.id.parentRelative);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            imgMore = itemView.findViewById(R.id.imgMore);
            txtNama = itemView.findViewById(R.id.txtNama);
            txtJumlah = itemView.findViewById(R.id.txtJumlah);
            circleImageView = itemView.findViewById(R.id.circleImageView);
            txtTanggal = itemView.findViewById(R.id.txtTanggal);
            btnKembalikanSiswa = itemView.findViewById(R.id.btnKembalikanSiswa);
        }
    }
}
