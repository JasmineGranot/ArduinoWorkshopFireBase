package com.example.arduinoandroidapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FallHistory extends AppCompatActivity {

    private TableLayout layout;
    private TableRow tableRow;
    private TextView firstText;
    private Map<String, String> map = new HashMap<>();
    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_history);


        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("100/falls");

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


    private void addDataToTable(List<String> dateList) {
        layout = (TableLayout) findViewById(R.id.fallHistoryTableLayout);
        View a = layout.getChildAt(0);
        View b = layout.getChildAt(1);
        layout.removeAllViews();
        layout.addView(a);
        layout.addView(b);

        for(String date : dateList){
            tableRow = new TableRow(this);
            firstText = new TextView(this);
            firstText.setLayoutParams(lp);
            firstText.setText(date);
            tableRow.addView(firstText);
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

