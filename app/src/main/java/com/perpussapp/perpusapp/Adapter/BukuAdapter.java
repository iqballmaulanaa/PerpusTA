package com.perpussapp.perpusapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.joooonho.SelectableRoundedImageView;
import com.perpussapp.perpusapp.Activity.AddActivity;
import com.perpussapp.perpusapp.Model.BukuModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.Constant;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BukuAdapter extends RecyclerView.Adapter<BukuAdapter.ViewHolder> implements Filterable {

    private List<BukuModel> bukuModels;
    private List<BukuModel> dataListfull = new ArrayList<>();
    private Context context;
    private DatabaseReference mDatabase;
    private Constant constant;
    private CallBack mCallBack;

    public interface CallBack {
        void onClick(int position);
    }

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }

    public BukuAdapter(Context context, ArrayList<BukuModel> bukuModels) {
        this.bukuModels = bukuModels;
        this.context = context;
        this.dataListfull = bukuModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_buku, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        constant = new Constant(context);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        BukuModel bukuModel = bukuModels.get(position);

        Glide.with(context).load(bukuModel.getGambar())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.d("FirmanTAG", "onLoadFailed: " + e.fillInStackTrace());
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.selectableRoundedImageView);

        holder.txtNama.setText(bukuModel.getNama());
        holder.txtCategory.setText("Kategory : " + bukuModel.getCategory());
        holder.txtPenulis.setText("Penulis : " + bukuModel.getPenulis());
        holder.txtTahun.setMaxLines(1);
        holder.txtTahun.setText("Tahun : " + bukuModel.getTahun());

        if (constant.getLevel(context) == 1) {
            holder.imgMore.setVisibility(View.VISIBLE);
            holder.btnPinjam.setVisibility(View.GONE);
        } else {
            holder.imgMore.setVisibility(View.GONE);
            holder.btnPinjam.setVisibility(View.VISIBLE);
        }

        holder.imgMore.setOnClickListener(v -> {
            PopupMenu pop = new PopupMenu(context, holder.imgMore);
            pop.inflate(R.menu.menu_item);
            pop.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.edit) {
                    if (mCallBack != null) {
                        mCallBack.onClick(position);
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

                        final StorageReference ref = storageRef.child("bookImage/" + bukuModel.getKey());
                        ref.delete().addOnSuccessListener(aVoid -> {
                            mDatabase.child("bookData").child(bukuModel.getKey())
                                    .removeValue().addOnSuccessListener(aVoid1 -> {
                                        pDialog.dismissWithAnimation();
                                        bukuModels.remove(position);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "berhasil menghapus", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.create().show();
                }
                return true;
            });
            pop.show();
        });

        holder.btnPinjam.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddActivity.class);
            intent.putExtra("listBuku", new ArrayList<>(List.of(bukuModel))); // kirim buku yang dipilih
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bukuModels.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    bukuModels = dataListfull;
                } else {
                    List<BukuModel> filteredList = new ArrayList<>();
                    for (BukuModel row : dataListfull) {
                        if (row.getNama().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    bukuModels = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = bukuModels;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                bukuModels = (ArrayList<BukuModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private SelectableRoundedImageView selectableRoundedImageView;
        private TextView txtNama, txtPenulis, txtCategory, txtTahun;
        private ImageButton imgMore;
        private Button btnPinjam;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMore = itemView.findViewById(R.id.imgMore);
            txtNama = itemView.findViewById(R.id.txtNama);
            txtPenulis = itemView.findViewById(R.id.txtPenulis);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            selectableRoundedImageView = itemView.findViewById(R.id.selectableRoundedImageView);
            txtTahun = itemView.findViewById(R.id.txtTahun);
            btnPinjam = itemView.findViewById(R.id.btnPinjam); // Tambahkan ini
        }
    }
}
