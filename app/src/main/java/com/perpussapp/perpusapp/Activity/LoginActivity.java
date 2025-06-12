package com.perpussapp.perpusapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;
import com.perpussapp.perpusapp.Util.Constant;

public class LoginActivity extends BaseActivity {
    private TextInputEditText edtNis, edtPassword;
    private DatabaseReference mDatabase;
    Constant constant = new Constant(LoginActivity.this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (constant.getUserId(this)!=null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();
        edtNis = findViewById(R.id.edtNis);
        edtPassword = findViewById(R.id.edtPassword);
    }

    public void toRegister(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
        Animatoo.animateSlideDown(this);
    }

    public void login(View view) {
        if (edtNis.getText().length()==0){
            edtNis.setError("NIS Masih Boleh Kosong");
            return;
        }
        if (edtPassword.getText().length()==0){
            edtPassword.setError("Password Masih Boleh Kosong");
            return;
        }

        mDatabase.child("user")
                .child(edtNis.getText().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String pw = snapshot.child("password").getValue(String.class);
                            if (pw.equals(edtPassword.getText().toString().trim())){
                                int isAdmin = snapshot.child("isAdmin").getValue(Integer.class);
                                constant.setLevel(LoginActivity.this,  isAdmin);
                                constant.setUserId(LoginActivity.this,edtNis.getText().toString());
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                               } else {
                                 Toast.makeText(LoginActivity.this, "Tidak Ada dengan User Tersebut", Toast.LENGTH_SHORT).show();

                            }
                        }else {
                            Toast.makeText(LoginActivity.this, "Tidak Ada dengan User Tersebut", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirmanTAG", "onCancelled: ", error.toException() );
                    }
                });

    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tap Sekali Lagi Untuk Close Aplikasi", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}