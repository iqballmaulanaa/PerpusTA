package com.perpussapp.perpusapp.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.joooonho.SelectableRoundedImageView;
import com.perpussapp.perpusapp.Model.UserModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;
import android.os.Build;

import java.io.File;
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends BaseActivity {
    private String TAG ="RegisterActivityTAG";
    private TextInputEditText edtNis,edtNama,edtTempatLahir,
            edtTanggalLahir,edtAlamat,edtPassword;
    private RadioGroup radio;
    private int jenisKelamin = 1;
    private String imgPath =null;
    private SelectableRoundedImageView circleImg;
    private DatabaseReference mDatabase;
    private Constant constant ;
    private long tanggalLahir = 0;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        constant = new Constant(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        circleImg =  findViewById(R.id.circleImg);
        edtPassword = findViewById(R.id.edtPassword);
        edtAlamat = findViewById(R.id.edtAlamat);
        edtTanggalLahir = findViewById(R.id.edtTanggalLahir);
        edtTempatLahir = findViewById(R.id.edtTempatLahir);
        edtNama = findViewById(R.id.edtNama);
        edtNis = findViewById(R.id.edtNis);
        radio = findViewById(R.id.radio);
        CircleImageView imgBtn =  findViewById(R.id.imgBtn);
        radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();
                if (id == R.id.rbL )
                    jenisKelamin =1;
                else jenisKelamin =2;
            }
        });
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        edtTanggalLahir.setText(constant.changeFromDate(newDate.getTime()));
                        String yyyymmd = constant.changeFromDate(newDate.getTime()) +" 00:00:00";
                        Log.d(TAG, "onDateSetStart: "+constant.changeYyyyMMDDtoMili(yyyymmd) );
                        tanggalLahir = constant.changeYyyyMMDDtoMili(yyyymmd);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
    }

    public void saveRegis(View view) {
        if (imgPath==null) {
            Toast.makeText(this, "Foto Wajib di Isi", Toast.LENGTH_SHORT).show();
            return;
        }

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
        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.purple_200));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        mDatabase.child("user")
                .child(edtNis.getText().toString().trim())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Toast.makeText(RegisterActivity.this, "Username atau NIS Sudah Terdaftar", Toast.LENGTH_SHORT).show();
                    pDialog.dismissWithAnimation();
                }else {
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
                                userModel.setIsAdmin(2);
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
                                        Toast.makeText(RegisterActivity.this, "Berhasil Registrasi", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            } else {
                                Log.d(TAG, "onComplete: "+task.getResult().toString());
                            }
                            pDialog.dismissWithAnimation();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideUp(this);
    }


}