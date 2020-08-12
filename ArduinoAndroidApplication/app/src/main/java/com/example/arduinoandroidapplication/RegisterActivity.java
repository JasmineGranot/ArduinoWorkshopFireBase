package com.example.arduinoandroidapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private TextView nameField;
    private TextView emailField;
    private TextView phoneField;
    private TextView ardIdField;
    private TextView passwordField;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private FirebaseFirestore dbFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameField = (TextView) findViewById(R.id.registeredName);
        emailField = (TextView) findViewById(R.id.registeredEmail);
        phoneField = (TextView) findViewById(R.id.registeredPhoneNumber);
        ardIdField = (TextView) findViewById(R.id.registeredBraceletId);
        passwordField = (TextView) findViewById(R.id.registeredPassword);
        Button registerButton = (Button) findViewById(R.id.registerButton);
        dialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegister();
            }
        });

    }

    private void startRegister() {
        final String userName = nameField.getText().toString().trim();
        final String userEmail = emailField.getText().toString().trim();
        final String userPhone = phoneField.getText().toString().trim();
        final String braceletID = ardIdField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if(userName.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Must Insert User Name!", Toast.LENGTH_SHORT).show();
        }
        else if(userEmail.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Must Insert User Email!", Toast.LENGTH_SHORT).show();
        }
        else if(userPhone.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Must Insert User Phone!", Toast.LENGTH_SHORT).show();
        }
        else if(braceletID.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Must Insert Bracelet ID!", Toast.LENGTH_SHORT).show();
        }
        else if(password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Must Insert Password!", Toast.LENGTH_SHORT).show();
        }
        else {
            dialog.setMessage("Signing Up...");
            dialog.show();
            auth.createUserWithEmailAndPassword(userEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        dbFirestore = FirebaseFirestore.getInstance();
                        CollectionReference collectionReference = dbFirestore.collection("Users");
                        DocumentReference documentReference = collectionReference.document(userId);
                        Map<String, Object> user = new HashMap<>();

                        user.put("name", userName);
                        user.put("email", userEmail);
                        user.put("phoneNumber", userPhone);
                        user.put("braceletId", braceletID);
                        documentReference.set(user);
                        dialog.dismiss();
                        Intent mainIntent = new Intent(RegisterActivity.this, ScreenMainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                }
            });
        }
    }
}
