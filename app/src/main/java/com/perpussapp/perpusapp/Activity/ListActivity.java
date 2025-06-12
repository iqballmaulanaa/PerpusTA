package com.perpussapp.perpusapp.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.perpussapp.perpusapp.Adapter.BukuAdapter;
import com.perpussapp.perpusapp.Model.BukuModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

import java.io.File;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import com.perpussapp.perpusapp.Spinner.JRSpinner;

public class ListActivity extends BaseActivity implements BukuAdapter.CallBack {
    private RecyclerView recyclerView;
    private ArrayList<BukuModel> bukuModels;
    private BukuAdapter bukuAdapter;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    private String TAG ="MainActivity";
    private static final int PERMISSION_REQUEST_STORAGE = 2;
   
    private String imgPath =null;
    private SelectableRoundedImageView circleImg;
    private DatabaseReference mDatabase;
    private Toolbar toolbar;
    private Constant constant;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        constant = new Constant(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        bukuModels = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bukuAdapter = new BukuAdapter(this, bukuModels);
        recyclerView.setAdapter(bukuAdapter);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent().hasExtra("kat")){
            getData(getIntent().getStringExtra("kat"));
        }else {
            getData("Semua");
        }

        bukuAdapter.setCallBack(this);
    }

    @Override
    public void onClick(int position) {
        BukuModel bukuModel = bukuModels.get(position);
        View view1 = LayoutInflater.from(this).inflate(R.layout.form_add_buku,null  );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Buku");
        builder.setView(view1);
        circleImg = view1.findViewById(R.id.circleImg);
        Glide.with(this).load(bukuModel.getGambar()).into(circleImg);
        CircleImageView imgBtn = view1.findViewById(R.id.imgBtn);
        TextInputEditText edtNama = view1.findViewById(R.id.edtNama);
        edtNama.setText(bukuModel.getNama());
        TextInputEditText txtPenulis = view1.findViewById(R.id.txtPenulis);
        txtPenulis.setText(bukuModel.getPenulis());
        TextInputEditText txtTahun = view1.findViewById(R.id.txtTahun);
        txtTahun.setText(bukuModel.getTahun());
        JRSpinner mySpinner = view1.findViewById(R.id.spinner);
        String[] fixlist = new String[getResources().getStringArray(R.array.kategory).length-1];
        for (int i = 0; i < fixlist.length; i++) {
            fixlist[i] = getResources().getStringArray(R.array.kategory)[i+1];
        }

        mySpinner.setItems(fixlist);
        mySpinner.setText(bukuModel.getCategory());
        mySpinner.setOnItemClickListener(new JRSpinner.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }
        });
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
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
                        if (edtNama.getText().length()==0){
                            edtNama.setError("Nama Tidak Boleh Kosong");
                            return;
                        }
                        bukuModel.setCategory(mySpinner.getText().toString().trim());
                        bukuModel.setNama(edtNama.getText().toString().trim());
                        bukuModel.setTahun(txtTahun.getText().toString().trim());
                        bukuModel.setPenulis(txtPenulis.getText().toString().trim());

                        edit(bukuModel,mAlertDialog);
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    private void getData(String category) {
        mDatabase.child("bookData")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bukuModels.clear();
                if (category.equals("Semua")) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        BukuModel bukuModel = dataSnapshot.getValue(BukuModel.class);
                        bukuModel.setKey(dataSnapshot.getKey());
                        bukuModels.add(bukuModel);
                    }
                }else {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        BukuModel bukuModel = dataSnapshot.getValue(BukuModel.class);
                        bukuModel.setKey(dataSnapshot.getKey());
                        if (bukuModel.getCategory().equals(category)){
                            bukuModels.add(bukuModel);
                        }
                    }
                }

                bukuAdapter.notifyDataSetChanged();
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
        if (constant.getLevel(this)==1){

            menu.findItem(R.id.add).setVisible(true);
        }else {

            menu.findItem(R.id.add).setVisible(false);
        }
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.acion_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Cari Buku");

        searchView.setSubmitButtonEnabled(true);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                bukuAdapter.getFilter().filter(newText);
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
            for (int i=0; i<getResources().getStringArray(R.array.kategory).length; i++){
                pop.getMenu().add(getResources().getStringArray(R.array.kategory)[i]);
            }
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    getData(String.valueOf(item.getTitle()));
                    Toast.makeText(ListActivity.this, "Filter Buku dengan Kategory "+ item.getTitle(), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            pop.show();
        } else if (item.getItemId() == R.id.add) {
            addLayout();
        }
        return super.onOptionsItemSelected(item);

    }

    private void addLayout() {
        BukuModel bukuModel = new BukuModel();
        View view1 = LayoutInflater.from(this).inflate(R.layout.form_add_buku,null  );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Buku");
        builder.setView(view1);
         circleImg = view1.findViewById(R.id.circleImg);
        CircleImageView imgBtn = view1.findViewById(R.id.imgBtn);
        TextInputEditText edtNama = view1.findViewById(R.id.edtNama);
        TextInputEditText txtPenulis = view1.findViewById(R.id.txtPenulis);
        TextInputEditText txtTahun = view1.findViewById(R.id.txtTahun);
        JRSpinner mySpinner = view1.findViewById(R.id.spinner);
        String[] fixlist = new String[getResources().getStringArray(R.array.kategory).length-1];
        for (int i = 0; i < fixlist.length; i++) {
                fixlist[i] = getResources().getStringArray(R.array.kategory)[i+1];
        }

        mySpinner.setItems(fixlist);

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
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
                        if (imgPath==null) {
                            Toast.makeText(ListActivity.this, "Gambar Masih Kosong", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (edtNama.getText().length()==0){
                            edtNama.setError("Nama Tidak Boleh Kosong");
                            return;
                        }
                        bukuModel.setCategory(mySpinner.getText().toString().trim());
                        bukuModel.setNama(edtNama.getText().toString().trim());
                        bukuModel.setTahun(txtTahun.getText().toString().trim());
                        bukuModel.setPenulis(txtPenulis.getText().toString().trim());

                        save(bukuModel,mAlertDialog);
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    private void edit(BukuModel bukuModel, AlertDialog mAlertDialog) {
        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.purple_200));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        String key = bukuModel.getKey();
        if (imgPath==null){
            mDatabase.child("bookData")
                    .child(key)
                    .setValue(bukuModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    pDialog.dismiss();
                    mAlertDialog.dismiss();
                    imgPath=null;
                    Toast.makeText(ListActivity.this, "berhasil menyimpan", Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            final StorageReference ref = storageRef.child("bookImage/"+key);
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

                        bukuModel.setGambar(String.valueOf(downloadUri));
                        mDatabase.child("bookData")
                                .child(key)
                                .setValue(bukuModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mAlertDialog.dismiss();
                                imgPath=null;
                                Toast.makeText(ListActivity.this, "berhasil menyimpan", Toast.LENGTH_SHORT).show();
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

    private void save(BukuModel bukuModel, AlertDialog mAlertDialog) {
        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.purple_200));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        String key = mDatabase.child("bookData").push().getKey();
        final StorageReference ref = storageRef.child("bookImage/"+key);
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

                    bukuModel.setGambar(String.valueOf(downloadUri));
                    mDatabase.child("bookData")
                            .child(key)
                            .setValue(bukuModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mAlertDialog.dismiss();
                            imgPath=null;
                            Toast.makeText(ListActivity.this, "berhasil menyimpan", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.d(TAG, "onComplete: "+task.getResult().toString());
                }
                pDialog.dismissWithAnimation();
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideUp(this);

    }


}