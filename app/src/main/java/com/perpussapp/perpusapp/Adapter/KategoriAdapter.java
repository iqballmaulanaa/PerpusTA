package com.perpussapp.perpusapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.perpussapp.perpusapp.Activity.ListActivity;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class KategoriAdapter extends RecyclerView.Adapter<KategoriAdapter.ViewHolder>  {

    private List<String> kategory;
    private List<String> gambar;
    private Context context;
    private DatabaseReference mDatabase;
    public KategoriAdapter(Context context, ArrayList<String> kategory, ArrayList<String> gambar) {
        this.gambar = gambar;
        this.kategory = kategory;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.list_kategori, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txt.setText(kategory.get(position));
        Glide.with(context).load(gambar.get(position))
                .into(holder.img);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListActivity.class);
                intent.putExtra("kat", kategory.get(position));
                context.startActivity(intent);
                Animatoo.animateSlideDown(context);
            }
        });
    }



    @Override
    public int getItemCount() {
        return kategory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        private TextView txt, txtStatus ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.txt); ;
            img = itemView.findViewById(R.id.img);
        }
    }
}
