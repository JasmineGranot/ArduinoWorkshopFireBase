package com.example.arduinoandroidapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class CurrentPulse extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference ref;
    private TextView currentPulse;
    private FirebaseFirestore dbFirestore;
    private String braceletId;
    private ProgressDialog dialog;
    DatabaseReference falldbref;
    DatabaseReference pulsedbref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_pulse);

        PulsatorLayout pulsatorLayout = (PulsatorLayout)findViewById(R.id.pulsator);
        pulsatorLayout.start();

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            FirebaseUser userUId = auth.getCurrentUser();
            getUserBraceletID(userUId);
            onStart();
        }
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
                public void onChildAdded(@NonNull com.google.firebase.database.DataSnapshot snapshot, @Nullable String previousChildName) {
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
                public void onChildChanged(@NonNull com.google.firebase.database.DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onChildRemoved(@NonNull com.google.firebase.database.DataSnapshot snapshot) { }

                @Override
                public void onChildMoved(@NonNull com.google.firebase.database.DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });

            falldbref = FirebaseDatabase.getInstance().getReference().
                    child(String.format("%s/falls", braceletId));

            falldbref.addChildEventListener(new ChildEventListener() {

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onChildAdded(@NonNull com.google.firebase.database.DataSnapshot snapshot, @Nullable String previousChildName) {
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
                public void onChildChanged(@NonNull com.google.firebase.database.DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onChildRemoved(@NonNull com.google.firebase.database.DataSnapshot snapshot) { }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    private void notification(String msg) {

        new AlertDialog.Builder(CurrentPulse.this)
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

    private void getUserBraceletID(final FirebaseUser currentUser){
        dbFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = dbFirestore.collection("Users");
        DocumentReference documentReference = collectionReference.document(currentUser.getUid());
        dialog = new ProgressDialog(this);
        dialog.setMessage("Waiting for data...");
        dialog.show();
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    braceletId = document.get("braceletId").toString();

                    ref = FirebaseDatabase.getInstance().getReference().
                            child(String.format("%s/current_pulse", braceletId));
                    ref.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            currentPulse = (TextView) findViewById(R.id.currentPulse);
                            currentPulse.setText(snapshot.getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                  
                    dialog.dismiss();
                    onResume();
                }
                else {
                    Toast.makeText(CurrentPulse.this, "Must Insert User Name!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
