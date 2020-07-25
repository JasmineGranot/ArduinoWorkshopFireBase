package com.example.arduinoandroidapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PulseHistory extends AppCompatActivity {

    private TableLayout layout;
    private TableRow tableRow;
    private TextView firstText, secondText;
    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_history);

        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("100/pulse_history");
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
            secondText.setLayoutParams(lp);
            secondText.setText(entry.getValue());
            tableRow.addView(firstText);
            tableRow.addView(secondText);
            layout.addView(tableRow);
        }
    }
}
