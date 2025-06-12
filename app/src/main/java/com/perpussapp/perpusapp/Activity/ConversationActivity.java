package com.perpussapp.perpusapp.Activity;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.perpussapp.perpusapp.Adapter.ConversationAdapter;
import com.perpussapp.perpusapp.Model.CoversationModel;
import com.perpussapp.perpusapp.Model.UserModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

import java.io.File;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends BaseActivity {
    private String TAG ="ConversationActivityTAG";
    private RecyclerView recyclerView;
    private static final int PERMISSION_REQUEST_STORAGE = 2;
   
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private DatabaseReference mDatabase;
    private Constant constant;
    private ArrayList<CoversationModel> conversation;
    private TextInputEditText textInputEditText;
    private ConversationAdapter conversationAdapter;
    private TextView action_bar_title_1;
    private CircleImageView conversation_contact_photo;
    private Toolbar toolbar;
    private String keyUser = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        constant = new Constant(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        toolbar = (Toolbar) findViewById(R.id.toolbar_chats);
        setSupportActionBar(toolbar);
        conversation = new ArrayList<>();
        conversation_contact_photo = findViewById(R.id.conversation_contact_photo);
        recyclerView = findViewById(R.id.recyclerView);
        textInputEditText = findViewById(R.id.textInputEditText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationAdapter = new ConversationAdapter(ConversationActivity.this, conversation);
        recyclerView.setAdapter(conversationAdapter);
        action_bar_title_1 = findViewById(R.id.action_bar_title_1);
        if (constant.getLevel(ConversationActivity.this)==2){
            action_bar_title_1.setText("Admin");
            getDataAsSiswa();
            keyUser = constant.getUserId(ConversationActivity.this);
        }else {
            keyUser =getIntent().getStringExtra("nis");
            mDatabase.child("user")
                    .child(getIntent().getStringExtra("nis"))
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            userModel.setNis(getIntent().getStringExtra("nis"));
                            action_bar_title_1.setText(userModel.getNama());
                            Glide.with(getApplicationContext()).load(userModel.getImage())
                                    .into(conversation_contact_photo);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            getDataAsAdmin();
        }

    }

    private void getDataAsAdmin() {
        mDatabase.child("listChat")
                .child(getIntent().getStringExtra("nis"))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        conversation.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            CoversationModel coversationModel = dataSnapshot.getValue(CoversationModel.class);
                            coversationModel.setKey(dataSnapshot.getKey());
                            conversation.add(coversationModel);
                            Log.d(TAG, "onDataChange: "+dataSnapshot.getKey());
                        }
                        conversationAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(conversation.size()-1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getDataAsSiswa() {
        mDatabase.child("listChat")
                .child(constant.getUserId(ConversationActivity.this))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        conversation.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                CoversationModel coversationModel = dataSnapshot.getValue(CoversationModel.class);
                                coversationModel.setKey(dataSnapshot.getKey());
                                conversation.add(coversationModel);
                                Log.d(TAG, "onDataChange: "+dataSnapshot.getKey());
                        }
                        recyclerView.scrollToPosition(conversation.size()-1);
                        conversationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void back(View view){
        finish();
    }

    public void sendChat(View view) {
        if (textInputEditText.getText().length()==0){
            Toast.makeText(this, "Tidak Bisa Mengirim Pesan Kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        CoversationModel coversationModel = new CoversationModel();
        coversationModel.setIsAdmin(constant.getLevel(ConversationActivity.this));
        coversationModel.setMessage(textInputEditText.getText().toString().trim());
        coversationModel.setMessageType(1);
        coversationModel.setTime(System.currentTimeMillis());

        String key =  mDatabase.child("listChat")
                .child (keyUser)
                .push().getKey();

        mDatabase.child("listChat")
                .child( keyUser)
                .child(key)
                .setValue(coversationModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        textInputEditText.setText("");
                        recyclerView.scrollToPosition(conversation.size()-1);
                      //  Toast.makeText(ConversationActivity.this, "kirim", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        menu.findItem(R.id.print).setVisible(false);
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.filter).setVisible(true).setTitle("Hapus").setIcon(R.drawable.ic_baseline_delete_24);
        if(Constant.getLevel(this)!=1){
            menu.findItem(R.id.filter).setVisible(false);
        }
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.acion_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Cari Percakapan");
        searchView.setSubmitButtonEnabled(true);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                conversationAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.filter){

            AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
            builder.setCancelable(true);
            builder.setTitle("Konfirmasi");
            builder.setMessage("Apakah Kamu Yakin Akan Menghapus");
            builder.setPositiveButton("Iya",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SweetAlertDialog pDialog = new SweetAlertDialog(ConversationActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                            pDialog.getProgressHelper().setBarColor( getResources().getColor(R.color.purple_200));
                            pDialog.setTitleText("Loading");
                            pDialog.setCancelable(false);
                            pDialog.show();
                            mDatabase.child("listChat")
                                    .child(getIntent().getStringExtra("nis"))
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pDialog.dismissWithAnimation();
                                    Toast.makeText(ConversationActivity.this, "berhasil menghapus", Toast.LENGTH_SHORT).show();
                                    finish();
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
        return super.onOptionsItemSelected(item);
    }

    public void openGallery(View view) {
        choosePhoto();
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
                Log.d("Filedadadada", returnUri.toString());
                uploadImage(returnUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImage(Uri returnUri) {

        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.purple_200));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        final StorageReference ref = storageRef.child("chatImage/"+ System.currentTimeMillis());
        UploadTask uploadTask = ref.putFile(Uri.fromFile(new File(getRealPathFromURIPath(returnUri))));

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

                    CoversationModel coversationModel = new CoversationModel();
                    coversationModel.setIsAdmin(constant.getLevel(ConversationActivity.this));
                    coversationModel.setMessage(String.valueOf(downloadUri));
                    coversationModel.setMessageType(2);
                    coversationModel.setTime(System.currentTimeMillis());

                    String key =  mDatabase.child("listChat")
                            .child(keyUser )
                            .push().getKey();

                    mDatabase.child("listChat")
                            .child( keyUser)
                            .child(key)
                            .setValue(coversationModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    textInputEditText.setText("");
                                    recyclerView.scrollToPosition(conversation.size()-1);
                                    //  Toast.makeText(ConversationActivity.this, "kirim", Toast.LENGTH_SHORT).show();
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