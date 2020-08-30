package com.example.arduinoandroidapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ScreenMainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore dbFirestore;

    FirebaseDatabase database;
    DatabaseReference reference;
    private String braceletId;
    DatabaseReference falldbref;
    DatabaseReference pulsedbref;

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

        dbFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = dbFirestore.collection("Users");
        DocumentReference documentReference = collectionReference.document(currentUser.getUid());

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    braceletId = document.get("braceletId").toString();
                    onResume();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(braceletId != null) {
            pulsedbref = FirebaseDatabase.getInstance().getReference().
                    child(String.format("%s/pulse_history", braceletId));

            pulsedbref.addChildEventListener(new ChildEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String dateTime = String.format("%s", snapshot.getKey());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime givenDateTime = LocalDateTime.parse(dateTime, formatter);
                    LocalDateTime now = LocalDateTime.now().plusHours(3);

                    if(now.minusMinutes(5).isBefore(givenDateTime.plusHours(2)) &&
                            (!snapshot.hasChild("seen"))){
                        notification("Pulse Anomaly");
                        pulsedbref.child(snapshot.getKey()).child("seen").setValue(true);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });

            falldbref = FirebaseDatabase.getInstance().getReference().
                    child(String.format("%s/falls", braceletId));

            falldbref.addChildEventListener(new ChildEventListener() {

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String dateTime = String.format("%s", snapshot.getKey());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime givenDateTime = LocalDateTime.parse(dateTime, formatter);

                    LocalDateTime now = LocalDateTime.now().plusHours(3);

                    if(now.minusMinutes(5).isBefore(givenDateTime.plusHours(2)) &&
                            (!snapshot.hasChild("seen"))){
                        notification("Fall");
                        falldbref.child(snapshot.getKey()).child("seen").setValue(true);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    private void notification(String msg) {
        new AlertDialog.Builder(ScreenMainActivity.this)
                .setTitle("Warning")
                .setMessage(String.format("%s Detected!", msg))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Call Emergency", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String number = "+972526586120".trim();
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
                        startActivity(intent);
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("Dismiss", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    private void getUserDisplayName(final FirebaseUser currentUser) {
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
