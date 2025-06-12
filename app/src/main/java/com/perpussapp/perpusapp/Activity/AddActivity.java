package com.perpussapp.perpusapp.Activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.perpussapp.perpusapp.Adapter.ListBukuAdapter;
import com.perpussapp.perpusapp.Model.ListBukuModel;
import com.perpussapp.perpusapp.Model.PinjamModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

import java.util.ArrayList;
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;
import com.perpussapp.perpusapp.Spinner.JRSpinner;

public class AddActivity extends BaseActivity {
    private  JRSpinner mySpinner;
    private TextInputEditText txtTanggal,txtBatas;
    private Constant constant;
    private ArrayList<ListBukuModel> listBukuModels;
    private PinjamModel pinjamModel;
    private ListBukuAdapter listBukuAdapter;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listBukuModels = new ArrayList<>();
        pinjamModel = new PinjamModel();
        listBukuAdapter = new ListBukuAdapter(this,listBukuModels,getIntent().getParcelableArrayListExtra("listBuku"), true);
        constant = new Constant(this);
        constant.setListUser(getIntent().getParcelableArrayListExtra("listUser"));
        txtTanggal =  findViewById(R.id.txtTanggal);
        txtBatas =  findViewById(R.id.txtBatas);
        recyclerView =  findViewById(R.id.recyclerView);
        mySpinner =  findViewById(R.id.spinner);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listBukuAdapter);
        mySpinner.setItems(constant.getSiswaNisNama());
        mySpinner.setOnItemClickListener(new JRSpinner.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                pinjamModel.setNis(constant.getNis(position));
            }
        });
        txtTanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog( AddActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        txtTanggal.setText(constant.changeFromDate(newDate.getTime()));
                        String yyyymmd = constant.changeFromDate(newDate.getTime()) +" 00:00:00";
                        pinjamModel.setTanggal(constant.changeYyyyMMDDtoMili(yyyymmd));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        txtBatas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog( AddActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        txtBatas.setText(constant.changeFromDate(newDate.getTime()));
                        String yyyymmd = constant.changeFromDate(newDate.getTime()) +" 00:00:00";
                        pinjamModel.setTglBatas(constant.changeYyyyMMDDtoMili(yyyymmd));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();

            }
        });
        if (getIntent().hasExtra("isEdit")){
            setUIEdit();
        }

    }

    private void setUIEdit() {
        mDatabase.child("listPinjam").child(getIntent().getStringExtra("pinjamKey"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pinjamModel.setTanggal(snapshot.child("tanggal").getValue(Long.class));
                        pinjamModel.setNis(snapshot.child("nis").getValue(String.class));
                        pinjamModel.setTglBatas(snapshot.child("tglBatas").getValue(Long.class));
                        txtTanggal.setText(constant.changeFromLong(snapshot.child("tanggal").getValue(Long.class)));
                        txtBatas.setText(constant.changeFromLong(snapshot.child("tglBatas").getValue(Long.class)));
                        mySpinner.setText(constant.getNamaByNis(snapshot.child("nis").getValue(String.class)));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        mDatabase.child("listBookPinjam").child(getIntent().getStringExtra("pinjamKey"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            ListBukuModel l = dataSnapshot.getValue(ListBukuModel.class);
                            listBukuModels.add(l);
                        }
                        listBukuAdapter.notifyDataSetChanged();
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
        menu.findItem(R.id.acion_search).setVisible(false);
        menu.findItem(R.id.filter).setVisible(false);
        menu.findItem(R.id.add).setVisible(true);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.filter){
        } else if (item.getItemId() == R.id.add) {
            addList();
        }
        return super.onOptionsItemSelected(item);

    }

    public void addList() {
        ListBukuModel listBukuModel = new ListBukuModel();
        View view1 = LayoutInflater.from(this).inflate(R.layout.form_list_buku,null  );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Buku");
        builder.setView(view1);
        TextInputEditText txtJumlah = view1.findViewById(R.id.txtJumlah);
        JRSpinner mySpinner = view1.findViewById(R.id.spinner);
        constant.setListBuku(getIntent().getParcelableArrayListExtra("listBuku"));
        mySpinner.setItems(constant.getBukuNama());
        mySpinner.setOnItemClickListener(new JRSpinner.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                listBukuModel.setBukuKey(constant.getBukuKey(position));
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
                        if (mySpinner.getText().toString().equals("Pilih Buku")){
                            mySpinner.setError("Belum Pilih");
                            return;
                        }

                        if (txtJumlah.getText().length()==0){
                            txtJumlah.setError("Belum di Isi");
                            return;
                        }

                        listBukuModel.setJumlah(Integer.parseInt(txtJumlah.getText().toString()));
                        listBukuModels.add(listBukuModel);
                        listBukuAdapter.notifyDataSetChanged();
                        mAlertDialog.dismiss();
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    public void simpanPinjam(View view) {
        if (txtTanggal.getText().length()==0){
            txtTanggal.setError("Tanggal Masih Kosong");
            return;
        }
        if (pinjamModel.getNis()==null){
            mySpinner.setError("Belum di Pilih");
            return;
        }

        if (listBukuModels.size()==0) {
            Toast.makeText(this, "Buku Masih Kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.purple_200));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
      //  pinjamModel.setListBuku(listBukuAdapter.getListBuku());
        if (getIntent().hasExtra("isEdit")) {
            mDatabase.child("listPinjam").child(getIntent().getStringExtra("pinjamKey"))
                    .setValue(pinjamModel)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mDatabase.child("listBookPinjam")
                                    .child(getIntent().getStringExtra("pinjamKey"))
                                    .setValue(listBukuAdapter.getListBuku())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(AddActivity.this, "berhasil menyimpan", Toast.LENGTH_SHORT).show();
                                            pDialog.dismissWithAnimation();
                                            finish();

                                        }
                                    });
                        }
                    });
        }else {
            String key = mDatabase.child("listPinjam").push().getKey();
            mDatabase.child("listPinjam").child(key)
                    .setValue(pinjamModel)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mDatabase.child("listBookPinjam")
                                    .child(key)
                                    .setValue(listBukuAdapter.getListBuku())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(AddActivity.this, "berhasil menyimpan", Toast.LENGTH_SHORT).show();
                                            pDialog.dismissWithAnimation();
                                            finish();

                                        }
                                    });
                        }
                    });

        }
    }


}