package com.perpussapp.perpusapp.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.joooonho.SelectableRoundedImageView;
import com.perpussapp.perpusapp.Adapter.KategoriAdapter;
import com.perpussapp.perpusapp.Model.BukuModel;
import com.perpussapp.perpusapp.Model.KembaliModel;
import com.perpussapp.perpusapp.Model.PinjamModel;
import com.perpussapp.perpusapp.Model.UserModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;
import com.perpussapp.perpusapp.Util.PrintPinjaman2;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity {
    LinearLayout linearLayout;
    private DatabaseReference mDatabase;
    private ArrayList<BukuModel> bukuModels;
    private ArrayList<UserModel> userModels;
    private Constant constant;
    private CircleImageView circleImageView;
    private TextView txtNis;
    private TextView txtNama;
    private UserModel userModel;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        circleImageView = findViewById(R.id.circleImageView);
        txtNama = findViewById(R.id.txtNama);
        txtNis = findViewById(R.id.txtNis);
        constant = new Constant(this);
        userModels = new ArrayList<>();
        bukuModels = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        linearLayout = findViewById(R.id.linearLayout);
        if (constant.getLevel(this)==1){

            View viewAdmin= LayoutInflater.from(this).inflate(R.layout.menu_admin, null, false);
            linearLayout.addView(viewAdmin);
        }else {
            View viewAdmin= LayoutInflater.from(this).inflate(R.layout.menu_siswa, null, false);
            linearLayout.addView(viewAdmin);
            setUpMenuSiswa(viewAdmin);
        }
        findViewById(R.id.linearLayout).setVisibility(View.GONE);
        getUser();
        getInfoUser();
    }

    private void setUpMenuSiswa(View viewAdmin) {
        RecyclerView recyclerView = viewAdmin.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        ArrayList <String> kategory =  new ArrayList<>();
        ArrayList <String> gambar =  new ArrayList<>();

        for (int i=0; i<getResources().getStringArray(R.array.kategory).length; i++){
            try {
                kategory.add(getResources().getStringArray(R.array.kategory)[i]);
                gambar.add(getResources().getStringArray(R.array.gambar)[i]);
            }catch (NullPointerException n){
             n.getMessage();
            }

        }
        recyclerView.setAdapter(new KategoriAdapter(this, kategory,gambar));

    }


    public void toListChat(View view) {
        startActivity(new Intent(MainActivity.this, ChatActivity.class));
    }

    public void toChatAdmin(View view) {
        startActivity(new Intent(MainActivity.this, ConversationActivity.class));
    }

    private void getInfoUser() {
        mDatabase.child("user").child(constant.getUserId(MainActivity.this))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userModel = snapshot.getValue(UserModel.class);
                        userModel.setNis(constant.getUserId(MainActivity.this));
                        String image =  snapshot.child("image").getValue(String.class);
                        String nama =  snapshot.child("nama").getValue(String.class);
                        Glide.with(MainActivity.this).load(image).into(circleImageView);
                        txtNama.setText(nama);
                        txtNis.setText(constant.getUserId(MainActivity.this));
                        circleImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.detail_user,null  );
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Detail User");
                                builder.setView(view1);

                                final AlertDialog mAlertDialog = builder.create();
                                CardView card = view1.findViewById(R.id.card);
                                card.setVisibility(View.VISIBLE);
                                TextView txtEdit =  view1.findViewById(R.id.txtEdit);
                                txtEdit.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mAlertDialog.dismiss();
                                        editForm();
                                    }
                                });
                                CircleImageView circleImageView = view1.findViewById(R.id.circleImageView);
                                circleImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                                        intent.putExtra("link", userModel.getImage());
                                        startActivity(intent);
                                    }
                                });
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
                                Glide.with(MainActivity.this).load(userModel.getImage()).into(circleImageView);
                                txtNis.setText(userModel.getNis());
                                txtNama.setText(userModel.getNama());
                                txtAlamat.setText("Alamat : "+userModel.getAlamat());
                                txtTempatLahir.setText("Tempat  lahir : "+userModel.getTempatLahir() +" \nTanggal Lahir : "+new Constant(MainActivity.this).changeFromLong2(userModel.getTanggalLahir()));

                                mAlertDialog.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private TextInputEditText edtNis,edtNama,edtTempatLahir,
            edtTanggalLahir,edtAlamat,edtPassword;
    private RadioGroup radio,radioStatus;
    private int jenisKelamin = 1;
    private static final int PERMISSION_REQUEST_STORAGE = 2;
   
    private String imgPath =null;
    private SelectableRoundedImageView circleImg;
    private long tanggalLahir = 0;
    private int isAdmin = 0;
    private void editForm() {
        View view1 = LayoutInflater.from(this).inflate(R.layout.form_user,null  );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit User");
        builder.setView(view1);
        circleImg =  view1.findViewById(R.id.circleImg);
        Glide.with(this).load(userModel.getImage()).into(circleImg);
        edtPassword = view1.findViewById(R.id.edtPassword);
        edtPassword.setText(userModel.getPassword());
        edtAlamat = view1.findViewById(R.id.edtAlamat);
        edtAlamat.setText(userModel.getAlamat());
        edtTanggalLahir = view1.findViewById(R.id.edtTanggalLahir);
        edtTanggalLahir.setText(constant.changeFromLong(userModel.getTanggalLahir()));
        edtTempatLahir = view1.findViewById(R.id.edtTempatLahir);
        edtTempatLahir.setText(userModel.getTempatLahir());
        edtNama = view1.findViewById(R.id.edtNama);
        edtNama.setText(userModel.getNama());
        edtNis = view1.findViewById(R.id.edtNis);
        edtNis.setEnabled(false);
        edtNis.setText(userModel.getNis());
        radio = view1.findViewById(R.id.radio);
        radioStatus = view1.findViewById(R.id.radioStatus);
        if (constant.getLevel(this)==1){
            view1.findViewById(R.id.linear).setVisibility(View.VISIBLE);
        }else {
            view1.findViewById(R.id.linear).setVisibility(View.GONE);
        }
        CircleImageView imgBtn =  view1.findViewById(R.id.imgBtn);
        radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();
                if(id== R.id.rbL)
                    jenisKelamin =1;
                else
                    jenisKelamin =2;
            }
        });
        radioStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();
                if (id== R.id.rbAdmin)
                    isAdmin=1;
                else isAdmin=2;
            }
        });
        RadioButton rbL = view1.findViewById(R.id.rbL);
        RadioButton rbP = view1.findViewById(R.id.rbP);

        if (userModel.getJenisKelamin()==1){
            rbL.setChecked(true);
        }else {

            rbP.setChecked(true);
        }

        RadioButton rbAdmin = view1.findViewById(R.id.rbAdmin);
        RadioButton rbSiswa = view1.findViewById(R.id.rbSiswa);
        if (userModel.getIsAdmin()==1){
            rbAdmin.setChecked(true);
        }else {
            rbSiswa.setChecked(true);}
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });
        edtTanggalLahir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        edtTanggalLahir.setText(constant.changeFromDate(newDate.getTime()));
                        String yyyymmd = constant.changeFromDate(newDate.getTime()) +" 00:00:00";
                         tanggalLahir = constant.changeYyyyMMDDtoMili(yyyymmd);
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

                        if (edtNis.getText().length()==0){
                            edtNis.setError("Masih Kosong");
                            return;
                        } if (edtNama.getText().length()==0){
                            edtNama.setError("Masih Kosong");
                            return;
                        } if (edtPassword.getText().length()==0){
                            edtPassword.setError("Masih Kosong");
                            return;
                        }
                        simpanEdit(mAlertDialog, userModel);

                    }
                });
            }
        });
        mAlertDialog.show();
    }

    private void simpanEdit(AlertDialog mAlertDialog, UserModel u) {
        if (imgPath==null){
            UserModel userModel = new UserModel();
            userModel.setJenisKelamin(jenisKelamin);
            userModel.setIsAdmin(isAdmin);
            userModel.setNama(edtNama.getText().toString().trim());
            userModel.setTempatLahir(edtTempatLahir.getText().toString().trim());
            userModel.setTanggalLahir( tanggalLahir );
            userModel.setImage(u.getImage());
            userModel.setAlamat(edtAlamat.getText().toString().trim());
            userModel.setPassword(edtPassword.getText().toString().trim());
            mDatabase.child("user").child(u.getNis())
                    .setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mAlertDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Berhasil Menyimpan", Toast.LENGTH_SHORT).show();

                }
            });
        }else {
            SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.purple_200));
            pDialog.setTitleText("Loading");
            pDialog.setCancelable(false);
            pDialog.show();

            final StorageReference ref = storageRef.child("userImage/"+edtNis.getText().toString().trim());
            UploadTask uploadTask = ref.putFile(Uri.fromFile(new File(getRealPathFromURIPath(Uri.parse(imgPath)))));

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        UserModel userModel = new UserModel();
                        userModel.setJenisKelamin(jenisKelamin);
                        userModel.setIsAdmin(isAdmin);
                        userModel.setNama(edtNama.getText().toString().trim());
                        userModel.setTempatLahir(edtTempatLahir.getText().toString().trim());
                        userModel.setTanggalLahir( tanggalLahir );
                        userModel.setImage(String.valueOf(downloadUri));
                        userModel.setAlamat(edtAlamat.getText().toString().trim());
                        userModel.setPassword(edtPassword.getText().toString().trim());
                        mDatabase.child("user").child(edtNis.getText().toString().trim())
                                .setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mAlertDialog.dismiss();
                                imgPath=null;
                                Toast.makeText(MainActivity.this, "Berhasil Menyimpan", Toast.LENGTH_SHORT).show();

                            }
                        });
                    } else {
                        Log.d( "onComplete: ",task.getResult().toString());
                    }
                    pDialog.dismissWithAnimation();
                }
            });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    openGallery();
                }

                return;
            }
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getPICK_IMAGE() && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri returnUri = data.getData();
                imgPath = String.valueOf(returnUri);
                Log.d("Filedadadada", returnUri.toString());
                Glide.with(this).load(returnUri).into(circleImg);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getUser() {
        mDatabase.child("user")
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModels.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    userModel.setNis(dataSnapshot.getKey());
                    if (userModel.getIsAdmin()==2){
                        userModels.add(userModel);
                    }

                }
                getData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void logout(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Apakah Kamu Yakin Akan Logout");
        builder.setPositiveButton("Iya",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        constant.setLevel(MainActivity.this,  0);
                        constant.setUserId(MainActivity.this,null);

                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
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

    private void getData() {
        mDatabase.child("bookData")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bukuModels.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            BukuModel bukuModel = dataSnapshot.getValue(BukuModel.class);
                            bukuModel.setKey(dataSnapshot.getKey());
                            bukuModels.add(bukuModel);
                        }
                        findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public void toList(View view) {
        startActivity(new Intent(this, ListActivity.class));
        Animatoo.animateSlideDown(this);

    }

    private long dariTgl, hinggaTgl;
    public void toRekap(View view) {
        View view1 = LayoutInflater.from(this).inflate(R.layout.form_kembalikan,null  );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Berdasarkan Tanggal");
        builder.setView(view1);
        TextInputLayout txtInputLayoutDari = view1.findViewById(R.id.txtInputLayoutDari);
        TextInputLayout txtInputLayoutHingga = view1.findViewById(R.id.txtInputLayoutHingga);
        txtInputLayoutHingga.setVisibility(View.VISIBLE);
        TextInputEditText edtDariTanggal = view1.findViewById(R.id.edtTanggalKembali);
        edtDariTanggal.setHint("");
        txtInputLayoutDari.setHint("Dari Tangggal");
        edtDariTanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        edtDariTanggal.setText(constant.changeFromDate(newDate.getTime()));
                        String yyyymmd = constant.changeFromDate(newDate.getTime()) +" 00:00:00";
                        dariTgl = constant.changeYyyyMMDDtoMili(yyyymmd);
                        // kembaliModel.setTanggalKembali(constant.changeYyyyMMDDtoMili(yyyymmd));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        TextInputEditText edtHinggaTanggal = view1.findViewById(R.id.edtTanggalHingga);
        edtHinggaTanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        edtHinggaTanggal.setText(constant.changeFromDate(newDate.getTime()));
                        String yyyymmd = constant.changeFromDate(newDate.getTime()) +" 23:59:59";
                        hinggaTgl = constant.changeYyyyMMDDtoMili(yyyymmd);
                        // kembaliModel.setTanggalKembali(constant.changeYyyyMMDDtoMili(yyyymmd));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        builder.setPositiveButton("Cari", null);
        builder.setNegativeButton("cancel", null);
        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (edtDariTanggal.getText().length()==0){
                            edtDariTanggal.setError("Pilih Tanggal");
                            return;
                        }
                        if (edtHinggaTanggal.getText().length()==0){
                            edtHinggaTanggal.setError("Pilih Tanggal");
                            return;
                        }
                        Query query =  mDatabase.child("listPinjam")
                                .orderByChild("tanggal")
                                .startAt(dariTgl)
                                .endAt(hinggaTgl);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ArrayList<PinjamModel> pinjamModels = new ArrayList<>();
                                    for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                                        PinjamModel pinjamModel = dataSnapshot.getValue(PinjamModel.class);
                                        pinjamModel.setKey(dataSnapshot.getKey());
                                        pinjamModels.add(pinjamModel);
                                    }
                                Query query1 =  mDatabase.child("listKembali")
                                        .orderByChild("tanggalKembali")
                                        .startAt(dariTgl)
                                        .endAt(hinggaTgl);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ArrayList<KembaliModel> kembaliModels = new ArrayList<>();
                                        for (DataSnapshot ds :snapshot.getChildren()){
                                            KembaliModel k = new KembaliModel();
                                            k.setPinjamKey(ds.getKey());
                                            long tgl = ds.child("tanggalKembali").getValue(Long.class);
                                            k.setTanggalKembali(tgl);
                                            kembaliModels.add(k);
                                        }
                                        new PrintPinjaman2(MainActivity.this, dariTgl, hinggaTgl,
                                                mDatabase,bukuModels,userModels,pinjamModels,kembaliModels);
                                        mAlertDialog.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    public void toPinjam(View view) {
        Intent intent = new Intent(this, MutasiActivity.class);
        intent.putExtra("isPinjam", true);
        intent.putParcelableArrayListExtra("listBuku", bukuModels);
        intent.putParcelableArrayListExtra("listUser", userModels);
        startActivity(intent);
        Animatoo.animateSlideDown(this);
    }

    public void toBalian(View view){
        Intent intent = new Intent(this, MutasiActivity.class);
        intent.putExtra("isPinjam", false);
        intent.putParcelableArrayListExtra("listBuku", bukuModels);
        intent.putParcelableArrayListExtra("listUser", userModels);
        startActivity(intent);
        Animatoo.animateSlideDown(this);
    }


    public void toUser(View view) {
        startActivity(new Intent(this, UserActivity.class));
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tap Sekali Lagi Untuk Close Aplikasi", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


}