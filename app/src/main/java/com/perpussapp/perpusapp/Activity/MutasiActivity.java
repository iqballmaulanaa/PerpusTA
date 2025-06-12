package com.perpussapp.perpusapp.Activity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.perpussapp.perpusapp.Adapter.PinjamAdapter;
import com.perpussapp.perpusapp.Model.PinjamModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

import java.util.ArrayList;

public class MutasiActivity extends BaseActivity {

    private DatabaseReference mDatabase;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private PinjamAdapter pinjamAdapter;
    private ArrayList<PinjamModel> pinjamModels;
    private Constant constant;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutasi);
        constant = new Constant(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView =   findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(toolbar);
        if (getIntent().getBooleanExtra("isPinjam", true)){
            toolbar.setTitle("Data Peminjaman");
            pinjamModels = new ArrayList<>();
            pinjamAdapter = new PinjamAdapter(this,pinjamModels );
            recyclerView.setAdapter(pinjamAdapter);
            pinjamAdapter.setCallBack(new PinjamAdapter.CallBack() {
                @Override
                public void onClick(int position, boolean isEdit) {
                    Intent intent;
                    if (isEdit){
                        intent = new Intent(MutasiActivity.this, AddActivity.class);
                    }else {
                        intent = new Intent(MutasiActivity.this, ConfirmationActivity.class);
                    }
                    intent.putExtra("isEdit", true);
                    intent.putExtra("pinjamKey", pinjamModels.get(position).getKey());
                    intent.putParcelableArrayListExtra("listUser", getIntent().getParcelableArrayListExtra("listUser"));
                    intent.putParcelableArrayListExtra("listBuku", getIntent().getParcelableArrayListExtra("listBuku"));
                    startActivity(intent);
                    Animatoo.animateSlideDown(MutasiActivity.this);

                }
            });
            getDataPinjam("Tampilkan Semua");
        }else {
            toolbar.setTitle("Data Pengembalian");
        }
    }

    private void getDataPinjam(String string) {
        if (string.startsWith("Tampilkan")) {
            mDatabase.child("listPinjam")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            pinjamModels.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                PinjamModel pinjamModel = dataSnapshot.getValue(PinjamModel.class);
                                pinjamModel.setKey(dataSnapshot.getKey());
                                if (constant.getLevel(MutasiActivity.this)==1){
                                    pinjamModels.add(pinjamModel);
                                }else {
                                    if (pinjamModel.getNis().equals(constant.getUserId(MutasiActivity.this))){
                                        pinjamModels.add(pinjamModel);
                                    }
                                }

                            }
                            pinjamAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }else if (string.startsWith("Sudah")){
            mDatabase.child("listPinjam")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            pinjamModels.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                PinjamModel pinjamModel = dataSnapshot.getValue(PinjamModel.class);
                                pinjamModel.setKey(dataSnapshot.getKey());
                                mDatabase.child("listKembali")
                                        .child(pinjamModel.getKey())
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()){
                                                    if (constant.getLevel(MutasiActivity.this)==1){
                                                        pinjamModels.add(pinjamModel);
                                                    }else {
                                                        if (pinjamModel.getNis().equals(constant.getUserId(MutasiActivity.this))){
                                                            pinjamModels.add(pinjamModel);
                                                        }
                                                    }
                                                }
                                                pinjamAdapter.notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        }else {
            mDatabase.child("listPinjam")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            pinjamModels.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                PinjamModel pinjamModel = dataSnapshot.getValue(PinjamModel.class);
                                pinjamModel.setKey(dataSnapshot.getKey());
                                mDatabase.child("listKembali")
                                        .child(pinjamModel.getKey())
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (!snapshot.exists()){
                                                    if (constant.getLevel(MutasiActivity.this)==1){
                                                        pinjamModels.add(pinjamModel);
                                                    }else {
                                                        if (pinjamModel.getNis().equals(constant.getUserId(MutasiActivity.this))){
                                                            pinjamModels.add(pinjamModel);
                                                        }
                                                    }
                                                }
                                                pinjamAdapter.notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
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
        menu.findItem(R.id.filter).setVisible(true);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.acion_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Cari Berdasarkan Tanggal");

        searchView.setSubmitButtonEnabled(true);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (getIntent().getBooleanExtra("isPinjam", true)){
                    pinjamAdapter.getFilter().filter(newText);
                }else{

                }
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
            pop.getMenu().add("Tampilkan Semua");
            pop.getMenu().add("Sudah di Kembalikan");
            pop.getMenu().add("Belum di Kembalikan");
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    getDataPinjam(String.valueOf(item.getTitle()));
                    Toast.makeText(MutasiActivity.this, "Filter Data "+ item.getTitle(), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            pop.show();
        } else if (item.getItemId() == R.id.add) {
            if (getIntent().getBooleanExtra("isPinjam", true)){
                Intent intent = new Intent(this, AddActivity.class);
                intent.putParcelableArrayListExtra("listUser", getIntent().getParcelableArrayListExtra("listUser"));
                intent.putParcelableArrayListExtra("listBuku", getIntent().getParcelableArrayListExtra("listBuku"));
                startActivity(intent);
                Animatoo.animateSlideDown(this);
            }else {
                Intent intent = new Intent(this, KembaliActivity.class);
                intent.putParcelableArrayListExtra("listUser", getIntent().getParcelableArrayListExtra("listUser"));
                intent.putParcelableArrayListExtra("listBuku", getIntent().getParcelableArrayListExtra("listBuku"));
                startActivity(intent);
                Animatoo.animateSlideDown(this);
            }


        }
        return super.onOptionsItemSelected(item);

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideUp(this);

    }
}