package com.perpussapp.perpusapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.perpussapp.perpusapp.Activity.ImageActivity;
import com.perpussapp.perpusapp.Model.UserModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> implements Filterable {

    private List<UserModel> userModels;
    private List<UserModel> dataListfull = new ArrayList<>();
    private Context context;
    private DatabaseReference mDatabase;

    private CallBack mCallBack;


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    userModels = dataListfull;
                } else {
                    List<UserModel> filteredList = new ArrayList<>();
                    for (UserModel row : dataListfull) {
                        if ( row.getNama().startsWith(charString)) {
                            filteredList.add(row);
                        }
                    }

                    userModels = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = userModels;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                userModels = (ArrayList<UserModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
    public interface CallBack {
        void onClick(int position);
    }

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }
    public UserAdapter(Context context, ArrayList<UserModel> userModels) {
        this.userModels = userModels;
        this.context = context;
        this.dataListfull = userModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.list_user, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        UserModel userModel = userModels.get(position);
        holder.txtNama.setText(userModel.getNama());
        Glide.with(context).load(userModel.getImage()).into(holder.circleImageView);
        holder.circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImageActivity.class);
                intent.putExtra("link", userModel.getImage());
                context.startActivity(intent);
            }
        });
        if (userModel.getIsAdmin()==1){
            holder.txtStatus.setText("Admin");
        }else {
            holder.txtStatus.setText("Siswa");}
        holder.imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pop = new PopupMenu(context, holder.imgMore);
                pop.inflate(R.menu.menu_item);
                pop.setOnMenuItemClickListener(item -> {
                    if (item.getItemId()== R.id.edit) {

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
                                        final StorageReference ref = storageRef.child("userImage/"+userModels.get(position).getNis());
                                        ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mDatabase.child("user")
                                                        .child(userModels.get(position).getNis())
                                                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                pDialog.dismissWithAnimation();
                                                                userModels.remove(position);
                                                                notifyDataSetChanged();
                                                                Toast.makeText(context, "berhasil menghapus", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
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
                    return true;
                });
                pop.show();
            }
        });
        holder.pathRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetail(userModel);
            }
        });
    }

    private void showDetail(UserModel userModel) {
        View view1 = LayoutInflater.from(context).inflate(R.layout.detail_user,null  );
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Detail User");
        builder.setView(view1);
        CircleImageView circleImageView = view1.findViewById(R.id.circleImageView);
        TextView txtNis = view1.findViewById(R.id.txtNis);
        TextView txtNama = view1.findViewById(R.id.txtNama);
        TextView txtJenisKelamin = view1.findViewById(R.id.txtJenisKelamin);
        TextView txtTempatLahir = view1.findViewById(R.id.txtTempatLahir);
        TextView txtAlamat = view1.findViewById(R.id.txtAlamat);
        TextView txtStatus = view1.findViewById(R.id.txtStatus);
        if (userModel.getJenisKelamin()==1){
            txtJenisKelamin.setText("Jenis Kelamin : Laki-Laki");
        }else {
            txtJenisKelamin.setText("Jenis Kelamin : Perempuan");
        }

        if (userModel.getIsAdmin()==1){
            txtStatus.setText("Status : Admin \nPasswrod: "+userModel.getPassword());
        }else {
            txtStatus.setText("Status : Siswa \nPasswrod: "+userModel.getPassword());
        }
        Glide.with(context).load(userModel.getImage()).into(circleImageView);
        txtNis.setText(userModel.getNis());
        txtNama.setText(userModel.getNama());
        txtAlamat.setText("Alamat : "+userModel.getAlamat());
        txtTempatLahir.setText("Tempat  lahir : "+userModel.getTempatLahir() +" \nTanggal Lahir : "+new Constant(context).changeFromLong2(userModel.getTanggalLahir()));

        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView circleImageView;
        private TextView txtNama, txtStatus ;
        private ImageButton imgMore;
        private RelativeLayout pathRelative;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pathRelative = itemView.findViewById(R.id.pathRelative);
            imgMore = itemView.findViewById(R.id.imgMore);
            txtNama = itemView.findViewById(R.id.txtNama); ;
            circleImageView = itemView.findViewById(R.id.circleImageView);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}
