package com.perpussapp.perpusapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.joooonho.SelectableRoundedImageView;
import com.perpussapp.perpusapp.Activity.ImageActivity;
import com.perpussapp.perpusapp.Model.BukuModel;
import com.perpussapp.perpusapp.Model.ListBukuModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

import java.util.ArrayList;
import java.util.List;

import com.perpussapp.perpusapp.Spinner.JRSpinner;

public class ListBukuAdapter extends RecyclerView.Adapter<ListBukuAdapter.ViewHolder> {

    private ArrayList<ListBukuModel> listBuku;
    private List<ListBukuModel> dataListfull = new ArrayList<>();
    private ArrayList<BukuModel> bukuModels;
    private Context context;
    private DatabaseReference mDatabase;
    private boolean isShow;
    private CallBack mCallBack;


    public interface CallBack {
        void onClick(int position);
    }

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }

    public ListBukuAdapter(Context context, ArrayList<ListBukuModel> listBuku, ArrayList<BukuModel> bukuModels, boolean isShow) {
        this.listBuku = listBuku;
        this.context = context;
        this.dataListfull = listBuku;
        this.bukuModels = bukuModels;
        this.isShow = isShow;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_buku, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Constant constant = new Constant(context);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ListBukuModel list = listBuku.get(position);
        getInfoBuku(list.getBukuKey(), holder, list.getJumlah());
        if (isShow) {
            holder.imgMore.setVisibility(View.VISIBLE);
        } else {

            holder.imgMore.setVisibility(View.GONE);
        }
        holder.imgMore.setOnClickListener(new View.OnClickListener() {
            int p = constant.getPos(list.getBukuKey());

            @Override
            public void onClick(View v) {
                PopupMenu pop = new PopupMenu(context, holder.imgMore);
                pop.inflate(R.menu.menu_item);
                pop.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.edit) {

                        View view1 = LayoutInflater.from(context).inflate(R.layout.form_list_buku, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Pilih Buku");
                        builder.setView(view1);
                        TextInputEditText txtJumlah = view1.findViewById(R.id.txtJumlah);
                        txtJumlah.setText(String.valueOf(list.getJumlah()));
                        JRSpinner mySpinner = view1.findViewById(R.id.spinner);
                        mySpinner.setText(holder.txtNama.getText().toString());
                        constant.setListBuku(bukuModels);
                        mySpinner.setItems(constant.getBukuNama());
                        //   mySpinner.setSelection(p);
                        mySpinner.setOnItemClickListener(new JRSpinner.OnItemClickListener() {
                            @Override
                            public void onItemClick(int pos) {
                                p = pos;
                            }
                        });
                        builder.setPositiveButton("ok", null);
                        builder.setNegativeButton("cancel", null);
                        final AlertDialog mAlertDialog = builder.create();
                        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                b.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        list.setJumlah(Integer.valueOf(txtJumlah.getText().toString()));
                                        list.setBukuKey(constant.getBukuKey(p));
                                        listBuku.set(position, list);
                                        notifyDataSetChanged();

                                        mAlertDialog.dismiss();
                                    }
                                });
                            }
                        });
                        mAlertDialog.show();
                    } else if (itemId == R.id.delete) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                        builder1.setCancelable(true);
                        builder1.setTitle("Konfirmasi");
                        builder1.setMessage("Apakah Kamu Yakin Akan Menghapus");
                        builder1.setPositiveButton("Iya",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        listBuku.remove(position);
                                        notifyDataSetChanged();
                                    }
                                });
                        builder1.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });


                        AlertDialog dialog = builder1.create();
                        dialog.show();

                    }
                    return false;
                });
                pop.show();
            }
        });
    }

    public ArrayList<ListBukuModel> getListBuku() {
        return listBuku;
    }

    private void getInfoBuku(String bukuKey, ViewHolder holder, int jumlah) {
        mDatabase.child("bookData").child(bukuKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        holder.txtTahun.setMaxLines(2);
                        holder.txtNama.setText(snapshot.child("nama").getValue(String.class));
                        holder.txtCategory.setText("Kategory : " + snapshot.child("category").getValue(String.class));
                        holder.txtPenulis.setText("Penulis : " + snapshot.child("penulis").getValue(String.class));
                        holder.txtTahun.setText("Tahun : " + snapshot.child("tahun").getValue(String.class)
                                + "\nJumlah Pinjam : " + String.valueOf(jumlah));
                        Glide.with(context).load(snapshot.child("gambar").getValue(String.class)).into(holder.selectableRoundedImageView);
                        holder.selectableRoundedImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, ImageActivity.class);
                                intent.putExtra("link", snapshot.child("gambar").getValue(String.class));
                                context.startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return listBuku.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private SelectableRoundedImageView selectableRoundedImageView;
        private TextView txtNama, txtPenulis, txtCategory,
                txtTahun;
        private ImageButton imgMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMore = itemView.findViewById(R.id.imgMore);
            txtNama = itemView.findViewById(R.id.txtNama);
            txtPenulis = itemView.findViewById(R.id.txtPenulis);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            selectableRoundedImageView = itemView.findViewById(R.id.selectableRoundedImageView);
            txtTahun = itemView.findViewById(R.id.txtTahun);
        }
    }
}
