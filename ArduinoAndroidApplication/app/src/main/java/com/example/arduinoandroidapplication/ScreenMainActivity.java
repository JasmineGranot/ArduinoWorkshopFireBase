package com.example.arduinoandroidapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ScreenMainActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        auth = FirebaseAuth.getInstance();
        TextView greeting = (TextView) findViewById(R.id.GreetingUserTxt);
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            String name = getUserDisplayName(currentUser);
            name = !name.equals("") ? name : currentUser.getEmail();
            greeting.setText("Hello " + name);
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

    private String getUserDisplayName(FirebaseUser currentUser){
        currentUser.getUid();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
        return "";
    }
}
