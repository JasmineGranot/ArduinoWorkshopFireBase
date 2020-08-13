package com.example.arduinoandroidapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.HashMap;
import java.util.Map;


public class PulseHistory extends AppCompatActivity {

    private TableLayout layout;
    private TableRow tableRow;
    private TextView firstText, secondText;
    private Map<String, String> map = new HashMap<>();
    private FirebaseFirestore dbFirestore;
    private String braceletId;
    private FirebaseAuth auth;

    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_history);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        getUserBraceletID(currentUser);
    }

    private void getUserBraceletID(final FirebaseUser currentUser){
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
                            child(String.format("%s/pulse_history", braceletId));
                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, String> map = new HashMap<>();
                            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                String date = ds.getKey();
                                String heartBeat = ds.child("heart_rate").getValue().toString();
                                map.put(date, heartBeat);
                            }

                            addDataToTable(map);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    dbref.addValueEventListener(valueEventListener);

                }
            }
        });
    }

    private void addDataToTable(Map<String, String> map) {
        layout = (TableLayout) findViewById(R.id.tableLayout);
        View a = layout.getChildAt(0);
        View b = layout.getChildAt(1);
        layout.removeAllViews();
        layout.addView(a);
        layout.addView(b);

        for(Map.Entry<String, String> entry : map.entrySet()){
            tableRow = new TableRow(this);
            firstText = new TextView(this);
            secondText = new TextView(this);

            firstText.setLayoutParams(lp);
            firstText.setText(entry.getKey());
            firstText.setX(250);
            firstText.setTextColor(Color.parseColor("#000000"));
            firstText.setTextSize(20);
            firstText.setBackgroundColor(Color.parseColor("#deeaee"));



            secondText.setLayoutParams(lp);
            secondText.setText(entry.getValue());
            secondText.setX(650);
            secondText.setTextColor(Color.parseColor("#000000"));
            secondText.setTextSize(20);
            secondText.setBackgroundColor(Color.parseColor("#deeaee"));

            tableRow.addView(firstText);
            tableRow.addView(secondText,200,200);

            layout.addView(tableRow);
        }
    }
}
