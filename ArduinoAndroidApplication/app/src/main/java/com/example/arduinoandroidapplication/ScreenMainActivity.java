package com.example.arduinoandroidapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static java.lang.Thread.sleep;

public class ScreenMainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore dbFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            getUserDisplayName(currentUser);
        }

        Button logOutButton = (Button) findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth = FirebaseAuth.getInstance();
                auth.signOut();
                startActivity(new Intent(ScreenMainActivity.this, MainActivity.class));
            }
        });

        Button currentHearBeatButton = (Button) findViewById(R.id.getCurrentPulseButton);
        currentHearBeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScreenMainActivity.this, CurrentPulse.class));
            }
        });

        Button hearRateHistory = (Button) findViewById(R.id.getPulseHistoryButton);
        hearRateHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScreenMainActivity.this, PulseHistory.class));
            }
        });

        Button fallHistory = (Button) findViewById(R.id.getFallHistoryButton);
        fallHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScreenMainActivity.this, FallHistory.class));
            }
        });

        Button emergencyCallButton = (Button) findViewById(R.id.emergencyCallButton);
        emergencyCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScreenMainActivity.this, EmergencyCall.class));
            }
        });
    }

    private void getUserDisplayName(final FirebaseUser currentUser){
        dbFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = dbFirestore.collection("Users");
        DocumentReference documentReference = collectionReference.document(currentUser.getUid());

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String name = document.get("name").toString();
                    TextView greeting = (TextView) findViewById(R.id.GreetingUserTxt);
                    greeting.setText(String.format("Hello\n%s", name));
                }
            }
        });
    }
}
