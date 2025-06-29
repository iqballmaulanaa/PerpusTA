package com.perpussapp.perpusapp.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.perpussapp.perpusapp.Adapter.UserAdapter;
import com.perpussapp.perpusapp.Model.UserModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends BaseActivity {

    private Toolbar toolbar;
    private UserAdapter userAdapter;
    private ArrayList<UserModel> userModels;
    private RecyclerView recyclerView;
    private DatabaseReference mDatabase;
    private String TAG ="RegisterActivityTAG";
    private TextInputEditText edtNis,edtNama,edtTempatLahir,
            edtTanggalLahir,edtAlamat,edtPassword;
    private RadioGroup radio,radioStatus;
    private int jenisKelamin = 1;
    private static final int PERMISSION_REQUEST_STORAGE = 2;
   
    private String imgPath =null;
    private SelectableRoundedImageView circleImg;
    private Constant constant ;
    private long tanggalLahir = 0;
    private int isAdmin = 0;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        constant = new Constant(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userModels = new ArrayList<>();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView =  findViewById(R.id.recyclerView);
        setSupportActionBar(toolbar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, userModels);
        recyclerView.setAdapter(userAdapter);
        userAdapter.setCallBack(new UserAdapter.CallBack() {
            @Override
            public void onClick(int position) {
                editLayout(position);
            }
        });
        getData("Semua");
    }

    private void editLayout(int position) {
        UserModel userModel = userModels.get(position);
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
        edtNis.setEnabled(true);
        edtNis.setText(userModel.getNis());
        radio = view1.findViewById(R.id.radio);
        radioStatus = view1.findViewById(R.id.radioStatus);
        CircleImageView imgBtn =  view1.findViewById(R.id.imgBtn);
        radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();
                if (id == R.id.rbL ) {
                    jenisKelamin =1;
                }else{
                    jenisKelamin =2;
                }
            }
        });
        radioStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();
                if (id == R.id.rbAdmin) {
                    isAdmin = 1;
                }else isAdmin =2 ;
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(UserActivity.this, new DatePickerDialog.OnDateSetListener() {
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
        String newNis = edtNis.getText().toString().trim();
        String oldNis = u.getNis();

        if (imgPath == null) {
            UserModel userModel = new UserModel();
            userModel.setJenisKelamin(jenisKelamin);
            userModel.setIsAdmin(isAdmin);
            userModel.setNama(edtNama.getText().toString().trim());
            userModel.setTempatLahir(edtTempatLahir.getText().toString().trim());
            userModel.setTanggalLahir(tanggalLahir);
            userModel.setImage(u.getImage());
            userModel.setAlamat(edtAlamat.getText().toString().trim());
            userModel.setPassword(edtPassword.getText().toString().trim());

            mDatabase.child("user").child(newNis)
                    .setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (!newNis.equals(oldNis)) {
                                mDatabase.child("user").child(oldNis).removeValue();
                            }
                            mAlertDialog.dismiss();
                            Toast.makeText(UserActivity.this, "Berhasil Menyimpan", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.purple_200));
            pDialog.setTitleText("Loading");
            pDialog.setCancelable(false);
            pDialog.show();

            final StorageReference ref = storageRef.child("userImage/" + newNis);
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
                        userModel.setTanggalLahir(tanggalLahir);
                        userModel.setImage(String.valueOf(downloadUri));
                        userModel.setAlamat(edtAlamat.getText().toString().trim());
                        userModel.setPassword(edtPassword.getText().toString().trim());

                        mDatabase.child("user").child(newNis)
                                .setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (!newNis.equals(oldNis)) {
                                            mDatabase.child("user").child(oldNis).removeValue();
                                        }
                                        mAlertDialog.dismiss();
                                        imgPath = null;
                                        Toast.makeText(UserActivity.this, "Berhasil Menyimpan", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.d(TAG, "onComplete: " + task.getResult());
                    }
                    pDialog.dismissWithAnimation();
                }
            });
        }
    }


    private void addLayout() {
        View view1 = LayoutInflater.from(this).inflate(R.layout.form_user,null  );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah User");
        builder.setView(view1);
        circleImg =  view1.findViewById(R.id.circleImg);
        edtPassword = view1.findViewById(R.id.edtPassword);
        edtAlamat = view1.findViewById(R.id.edtAlamat);
        edtTanggalLahir = view1.findViewById(R.id.edtTanggalLahir);
        edtTempatLahir = view1.findViewById(R.id.edtTempatLahir);
        edtNama = view1.findViewById(R.id.edtNama);
        edtNis = view1.findViewById(R.id.edtNis);
        radio = view1.findViewById(R.id.radio);
        radioStatus = view1.findViewById(R.id.radioStatus);
        CircleImageView imgBtn =  view1.findViewById(R.id.imgBtn);
        radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();
                if (id == R.id.rbL)
                    jenisKelamin =1;
                else    jenisKelamin =2;


            }
        });
        radioStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();
                if (id == R.id.rbAdmin )
                    isAdmin =1;
                else  isAdmin =2;

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
                DatePickerDialog datePickerDialog = new DatePickerDialog(UserActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                        if (imgPath==null) {
                            Toast.makeText(UserActivity.this, "Foto Wajib di Isi", Toast.LENGTH_SHORT).show();
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


                        simpan(mAlertDialog);

                    }
                });
            }
        });
        mAlertDialog.show();
    }

    private void simpan(AlertDialog mAlertDialog) {
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
                            Toast.makeText(UserActivity.this, "Username atau NIS Sudah Terdaftar", Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(UserActivity.this, "Berhasil Menyimpan", Toast.LENGTH_SHORT).show();

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


    private void getData(String string) {
        mDatabase.child("user")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userModels.clear();
                        if (string.equals("Semua")){
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                                userModel.setNis(dataSnapshot.getKey());
                                userModels.add(userModel);
                            }
                        }else {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                                userModel.setNis(dataSnapshot.getKey());
                                if (string.equals("Admin")){
                                    if (userModel.getIsAdmin()==1){
                                        userModels.add(userModel);
                                    }
                                }else {
                                    if (userModel.getIsAdmin()==2){
                                        userModels.add(userModel);
                                    }
                                }

                            }
                        }

                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        menu.findItem(R.id.print).setVisible(false);
        menu.findItem(R.id.add).setVisible(true);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.acion_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Cari User");

        searchView.setSubmitButtonEnabled(true);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.filter){
            View menuItemView =  findViewById(R.id.filter);
            PopupMenu pop = new PopupMenu(this, menuItemView);
            pop.inflate(R.menu.menu_item);
            pop.getMenu().findItem(R.id.edit).setVisible(false);
            pop.getMenu().findItem(R.id.delete).setVisible(false);
            pop.getMenu().add("Semua");
            pop.getMenu().add("Admin");
            pop.getMenu().add("Siswa");
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    getData(String.valueOf(item.getTitle()));
                    Toast.makeText(UserActivity.this, "Filter User "+ item.getTitle(), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            pop.show();
        } else if (item.getItemId() == R.id.add) {
            addLayout();
        }
        return super.onOptionsItemSelected(item);

    }

}