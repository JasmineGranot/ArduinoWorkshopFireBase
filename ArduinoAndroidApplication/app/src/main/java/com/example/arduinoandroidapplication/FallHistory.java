package com.example.arduinoandroidapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class FallHistory extends AppCompatActivity {

    private TableLayout layout;
    private TableRow tableRow;
    private TextView firstText;
    private Map<String, String> map = new HashMap<>();
    private FirebaseFirestore dbFirestore;
    private String braceletId;
    private FirebaseAuth auth;
    DatabaseReference falldbref;
    DatabaseReference pulsedbref;
    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_history);
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        getBraceletData(currentUser);
        Button showOnMapButton = (Button) findViewById(R.id.showOnMapButton);

        showOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FallHistory.this, Location.class));
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
        new AlertDialog.Builder(FallHistory.this)
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


    private void getBraceletData(final FirebaseUser currentUser){
        dbFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = dbFirestore.collection("Users");
        DocumentReference documentReference = collectionReference.document(currentUser.getUid());

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    braceletId = document.get("braceletId").toString();
                    DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().
                            child(String.format("%s/falls", braceletId));

                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<String> dateList = new LinkedList<>();
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String date = ds.getKey();
                                dateList.add(date);
                            }

                            addDataToTable(dateList);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    dbref.addValueEventListener(valueEventListener);
                    onResume();
                }
            }
        });
    }

    private void addDataToTable(List<String> dateList) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#DCDCDC"));
        gd.setCornerRadius(5);
        gd.setStroke(1, 0xFF000000);

        layout = (TableLayout) findViewById(R.id.fallHistoryTableLayout);
        View a = layout.getChildAt(0);
        View b = layout.getChildAt(1);
        b.setBackground(gd);
        layout.removeAllViews();
        layout.addView(a);
        layout.addView(b);
        for (String element : dateList){
            GradientDrawable gd_in = new GradientDrawable();
            gd_in.setColor(Color.parseColor("#F8F8FF"));
            gd_in.setCornerRadius(5);
            gd_in.setStroke(1, 0xFF000000);
            tableRow = new TableRow(this);
            firstText = new TextView(this);
            firstText.setBackground(gd_in);
            firstText.setLayoutParams(lp);
            firstText.setText(element.replace("\"","") +"\n");
            firstText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            firstText.setX(0);
            firstText.setTextColor(Color.parseColor("#000000"));
            firstText.setTextSize(15);

            tableRow.addView(firstText,1058,50);
            layout.addView(tableRow);
        }
    }
}

