package com.perpussapp.perpusapp.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.perpussapp.perpusapp.Activity.ConversationActivity;
import com.perpussapp.perpusapp.Model.ListChat;
import com.perpussapp.perpusapp.Model.UserModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>    {

    private List<ListChat> listChats;
    private List<ListChat> dataListfull = new ArrayList<>();
    private Context context;
    private DatabaseReference mDatabase;

    private CallBack mCallBack;

    public interface CallBack {
        void onClick(int position);
    }

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }
    public ChatAdapter(Context context, List<ListChat> listChats) {
        this.listChats = listChats;
        this.context = context;
        this.dataListfull = listChats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.list_chat, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ListChat listChat = listChats.get(position);
        holder.txtLastChat.setText(listChat.getLastMessage());
        holder.txtDate.setText(TimeAgo(listChat.getTime()));
        getInfoUser(listChat.getNis(),holder);
        if (listChat.getMessageType()==1){
            holder.img.setVisibility(View.GONE);
            holder.txtLastChat.setVisibility(View.VISIBLE);

        }else {
            holder.img.setVisibility(View.VISIBLE);
            holder.txtLastChat.setVisibility(View.GONE);

        }
        holder.linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ConversationActivity.class);
                i.putExtra("nis", listChat.getNis());
                context.startActivity(i);
            }
        });


        holder.imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pop = new PopupMenu(context, holder.imgMore);
                pop.inflate(R.menu.menu_item);
                pop.getMenu().findItem(R.id.edit).setVisible(false);
                pop.setOnMenuItemClickListener(item -> {
                    if(item.getItemId()== R.id.edit) {

                        if (mCallBack!=null){
                            mCallBack.onClick(position);
                        }
                    }else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setCancelable(true);
                        builder.setTitle("Konfirmasi");
                        builder.setMessage("Apakah Kamu Yakin Akan Menghapus");
                        builder.setPositiveButton("Iya",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SweetAlertDialog pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
                                        pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.purple_200));
                                        pDialog.setTitleText("Loading");
                                        pDialog.setCancelable(false);
                                        pDialog.show();
                                        mDatabase.child("listChat")
                                                .child(listChats.get(position).getNis())
                                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        pDialog.dismissWithAnimation();
                                                        //   listChats.remove(position);
                                                        notifyDataSetChanged();
                                                        Toast.makeText(context, "berhasil menghapus", Toast.LENGTH_SHORT).show();
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
                    return false;
                });
                pop.show();
            }
        });

    }


    private void getInfoUser(String nis, ViewHolder holder) {
        mDatabase.child("user").child(nis)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        try {
                            Glide.with(context).load(userModel.getImage())
                                    .into(holder.circleImageView);
                            holder.textTitle.setText(userModel.getNama());

                        }catch (NullPointerException n){

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    @Override
    public int getItemCount() {
        return listChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView circleImageView;
        private TextView textTitle, txtDate,txtLastChat ;
        private ImageButton imgMore;
        private ImageView img;
        private RelativeLayout linear;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMore = itemView.findViewById(R.id.imgMore);
            linear = itemView.findViewById(R.id.linear);
            img = itemView.findViewById(R.id.img);
            imgMore = itemView.findViewById(R.id.imgMore);
            txtLastChat = itemView.findViewById(R.id.txtLastChat); ;
            textTitle = itemView.findViewById(R.id.textTitle); ;
            circleImageView = itemView.findViewById(R.id.circleImageView);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    public static String TimeAgo(long time){
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
}
