package com.perpussapp.perpusapp.Activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.perpussapp.perpusapp.Adapter.ChatAdapter;
import com.perpussapp.perpusapp.Model.CoversationModel;
import com.perpussapp.perpusapp.Model.ListChat;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatActivity extends BaseActivity {
    private String TAG = "ChatActivityTAG";
    private DatabaseReference mDatabase;
    private Toolbar toolbar;
    private Constant constant;
    private RecyclerView recyclerView;
    private List<ListChat> listChats;
    private ChatAdapter chatAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        constant = new Constant(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listChats = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
       // linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        chatAdapter = new ChatAdapter(this, listChats);
        recyclerView.setAdapter(chatAdapter);
        getData();
    }



    private void getData() {
        mDatabase.child("listChat").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listChats.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Log.d(TAG, "onDataChange: "+dataSnapshot.getKey());
                            mDatabase.child("listChat").
                                    child(dataSnapshot.getKey())
                                    .limitToLast(1)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                CoversationModel coversationModel = ds.getValue(CoversationModel.class);
                                                coversationModel.setKey(ds.getKey());
                                                Log.d(TAG, "onDataChange: "+coversationModel.getMessage());
                                                listChats.add(new ListChat(dataSnapshot.getKey(), coversationModel.getMessage(),
                                                        coversationModel.getTime(), coversationModel.getMessageType()));
                                            }
                                            sortList();
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

    private void sortList() {
        Collections.sort(listChats);
        Collections.reverse(listChats);
        chatAdapter.notifyDataSetChanged(); 

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
//        menu.findItem(R.id.print).setVisible(false);
//        menu.findItem(R.id.add).setVisible(false);
//        menu.findItem(R.id.filter).setVisible(false);
//        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.acion_search));
//        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setQueryHint("Cari Chat");
//        searchView.setSubmitButtonEnabled(true);
//        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
//        return true;
//    }

}