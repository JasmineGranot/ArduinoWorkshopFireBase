package com.example.arduinoandroidapplication;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
                        public void onCancelled(@NonNull DatabaseError error) { }
                    };
                    dbref.addValueEventListener(valueEventListener);
                }
            }
        });
    }

    private void addDataToTable(Map<String, String> map) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#DCDCDC"));
        gd.setCornerRadius(5);
        gd.setStroke(1, 0xFF000000);

        layout = (TableLayout) findViewById(R.id.tableLayout);
        View a = layout.getChildAt(0);
        View b = layout.getChildAt(1);
        b.setBackground(gd);
        layout.removeAllViews();
        layout.addView(a);
        layout.addView(b);

        List<Map.Entry<String, String> > list =
                new LinkedList<Map.Entry<String, String> >(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, String> >() {
            public int compare(Map.Entry<String, String> o1,
                               Map.Entry<String, String> o2)
            {
                return (o2.getKey()).compareTo(o1.getKey());
            }
        });

        for(Map.Entry<String, String> entry : list){
            GradientDrawable gd_in = new GradientDrawable();
            gd_in.setColor(Color.parseColor("#F8F8FF"));
            gd_in.setCornerRadius(5);
            gd_in.setStroke(1, 0xFF000000);

            tableRow = new TableRow(this);
            firstText = new TextView(this);
            secondText = new TextView(this);
            firstText.setBackground(gd_in);
            firstText.setLayoutParams(lp);
            firstText.setText(entry.getKey().replace("\"",""));
            firstText.setX(0);
            firstText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            firstText.setTextColor(Color.parseColor("#000000"));
            firstText.setTextSize(15);

            secondText.setBackground(gd_in);
            secondText.setLayoutParams(lp);
            secondText.setText(entry.getValue());
            secondText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            secondText.setX(0);
            secondText.setTextColor(Color.parseColor("#000000"));
            secondText.setTextSize(15);

            tableRow.addView(firstText,520,60);
            tableRow.addView(secondText,530,60);

            layout.addView(tableRow);
        }
    }
}
