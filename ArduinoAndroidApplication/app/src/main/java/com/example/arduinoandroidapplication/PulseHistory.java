package com.example.arduinoandroidapplication;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class PulseHistory extends AppCompatActivity {

    private TableLayout layout;
    private TableRow tableRow;
    private TextView firstText, secondText;
    private Map<String, String> map = new HashMap<>();
    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_history);

        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("100/pulse_history");

        /*dbref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                map.put(snapshot.getKey(), snapshot.child("heart_rate").getValue().toString());
                addDataToTable(map);
                //notification("Anomaly pulse");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    String date = ds.getKey();
                    String heartBeat = ds.child("heart_rate").getValue().toString();
                    map.put(date, heartBeat);
                }

                addDataToTable(map);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/

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

    /*private void notification(String msg){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("n", "n", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "n")
                .setContentText(String.format("New %s detected!", msg))
                .setAutoCancel(true)
                .setPriority( NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(999, builder.build());
    }*/
}
