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
    private String currentFilter = "Tampilkan Semua";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutasi);
        constant = new Constant(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(toolbar);

        if (getIntent().getBooleanExtra("isPinjam", true)) {
            initializePinjamData();
        } else {
            toolbar.setTitle("Data Pengembalian");
        }
    }

    private void initializePinjamData() {
        toolbar.setTitle("Data Peminjaman");
        pinjamModels = new ArrayList<>();
        pinjamAdapter = new PinjamAdapter(this, pinjamModels);
        recyclerView.setAdapter(pinjamAdapter);

        pinjamAdapter.setCallBack(new PinjamAdapter.CallBack() {
            @Override
            public void onClick(int position, boolean isEdit) {
                navigateToDetailActivity(position, isEdit);
            }
        });

        loadPinjamData(currentFilter);
    }

    private void navigateToDetailActivity(int position, boolean isEdit) {
        Intent intent;
        if (isEdit) {
            intent = new Intent(MutasiActivity.this, AddActivity.class);
        } else {
            intent = new Intent(MutasiActivity.this, ConfirmationActivity.class);
        }
        intent.putExtra("isEdit", true);
        intent.putExtra("pinjamKey", pinjamAdapter.getFilteredItem(position).getKey());
        intent.putParcelableArrayListExtra("listUser", getIntent().getParcelableArrayListExtra("listUser"));
        intent.putParcelableArrayListExtra("listBuku", getIntent().getParcelableArrayListExtra("listBuku"));
        startActivity(intent);
        Animatoo.animateSlideDown(MutasiActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        if (getIntent().getBooleanExtra("isPinjam", true)) {
            loadPinjamData(currentFilter);
        }
    }

    private void loadPinjamData(String filterType) {
        currentFilter = filterType;
        mDatabase.child("listPinjam").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pinjamModels.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    processPinjamData(dataSnapshot, filterType);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MutasiActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processPinjamData(DataSnapshot dataSnapshot, String filterType) {
        PinjamModel pinjamModel = dataSnapshot.getValue(PinjamModel.class);
        if (pinjamModel == null) return;

        pinjamModel.setKey(dataSnapshot.getKey());
        boolean isAdmin = constant.getLevel(MutasiActivity.this) == 1;
        boolean isCurrentUser = pinjamModel.getNis().equals(constant.getUserId(MutasiActivity.this));

        if (isAdmin || isCurrentUser) {
            if (filterType.equals("Tampilkan Semua")) {
                pinjamModels.add(pinjamModel);
                pinjamAdapter.notifyDataSetChanged();
            } else {
                checkReturnStatus(pinjamModel, filterType);
            }
        }
    }

    private void checkReturnStatus(PinjamModel pinjamModel, String filterType) {
        mDatabase.child("listKembali").child(pinjamModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot kembaliSnapshot) {
                        boolean shouldAdd = false;

                        if (filterType.equals("Sudah di Kembalikan")) {
                            shouldAdd = kembaliSnapshot.exists() &&
                                    "sudah_dikembalikan".equals(kembaliSnapshot.child("status").getValue(String.class));
                        }
                        else if (filterType.equals("Menunggu Konfirmasi")) {
                            shouldAdd = kembaliSnapshot.exists() &&
                                    "menunggu_konfirmasi".equals(kembaliSnapshot.child("status").getValue(String.class));
                        }
                        else if (filterType.equals("Belum di Kembalikan")) {
                            shouldAdd = !kembaliSnapshot.exists();
                        }

                        if (shouldAdd) {
                            pinjamModels.add(pinjamModel);
                            pinjamAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MutasiActivity.this, "Gagal memeriksa status pengembalian", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        configureMenuItems(menu);
        setupSearchView(menu);
        return true;
    }

    private void configureMenuItems(Menu menu) {
        menu.findItem(R.id.print).setVisible(false);
        menu.findItem(R.id.add).setVisible(constant.getLevel(this) == 1);
        menu.findItem(R.id.filter).setVisible(true);
    }

    private void setupSearchView(Menu menu) {
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.acion_search));
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
                if (getIntent().getBooleanExtra("isPinjam", true)) {
                    pinjamAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.filter) {
            showFilterPopup();
            return true;
        } else if (item.getItemId() == R.id.add) {
            navigateToAddActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterPopup() {
        View menuItemView = findViewById(R.id.filter);
        PopupMenu popupMenu = new PopupMenu(this, menuItemView);

        popupMenu.getMenu().add("Tampilkan Semua");
        popupMenu.getMenu().add("Sudah di Kembalikan");
        popupMenu.getMenu().add("Belum di Kembalikan");
        popupMenu.getMenu().add("Menunggu Konfirmasi");

        popupMenu.setOnMenuItemClickListener(item -> {
            currentFilter = item.getTitle().toString();
            loadPinjamData(currentFilter);
            Toast.makeText(MutasiActivity.this, "Filter Data: " + currentFilter, Toast.LENGTH_SHORT).show();
            return true;
        });

        popupMenu.show();
    }

    private void navigateToAddActivity() {
        Intent intent;
        if (getIntent().getBooleanExtra("isPinjam", true)) {
            intent = new Intent(this, AddActivity.class);
        } else {
            intent = new Intent(this, KembaliActivity.class);
        }

        intent.putParcelableArrayListExtra("listUser", getIntent().getParcelableArrayListExtra("listUser"));
        intent.putParcelableArrayListExtra("listBuku", getIntent().getParcelableArrayListExtra("listBuku"));
        startActivity(intent);
        Animatoo.animateSlideDown(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideUp(this);
    }
}