package com.example.arduinoandroidapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
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
                }
            }
        });
    }

    private void addDataToTable(List<String> dateList) {
        layout = (TableLayout) findViewById(R.id.fallHistoryTableLayout);
        View a = layout.getChildAt(0);
        layout.removeAllViews();
        layout.addView(a);
        for (String element : dateList){

            tableRow = new TableRow(this);
            firstText = new TextView(this);

            firstText.setLayoutParams(lp);
            firstText.setText(element.replace("\"",""));
            firstText.setX(400);
            firstText.setTextColor(Color.parseColor("#000000"));
            firstText.setTextSize(20);
            firstText.setBackgroundColor(Color.parseColor("#deeaee"));

            tableRow.addView(firstText);
            layout.addView(tableRow);
        }
    }
}

