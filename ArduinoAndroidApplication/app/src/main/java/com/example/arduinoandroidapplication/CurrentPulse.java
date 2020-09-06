package com.example.arduinoandroidapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

public class CurrentPulse extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference ref;
    private TextView currentPulse;
    private FirebaseFirestore dbFirestore;
    private String braceletId;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_pulse);

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            FirebaseUser userUId = auth.getCurrentUser();
            getUserBraceletID(userUId);
        }
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
                }
                else {
                    Toast.makeText(CurrentPulse.this, "Must Insert User Name!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
