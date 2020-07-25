package com.example.arduinoandroidapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText signInEmail;
    private EditText signInPassword;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        signInEmail = (EditText) findViewById(R.id.signInEmail);
        signInPassword = (EditText) findViewById(R.id.signInPassword);
        auth = FirebaseAuth.getInstance();

        Button regButton = (Button) findViewById(R.id.registerButton);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null) {
                    startActivity(new Intent(MainActivity.this, ScreenMainActivity.class));
                }
            }
        };

        Button signInButton = (Button) findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    private void startSignIn() {
        String userEmail = signInEmail.getText().toString();
        String userPassword = signInPassword.getText().toString();

        if(userEmail.isEmpty()) {
            Toast.makeText(MainActivity.this, "Must Insert Email!", Toast.LENGTH_SHORT).show();
        }
        else if(userPassword.isEmpty()) {
            Toast.makeText(MainActivity.this, "Must Insert Password!", Toast.LENGTH_SHORT).show();
        }
        else {
            auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Error Signing In!", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }
}
