package com.example.arduinoandroidapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.util.Pair;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Location extends FragmentActivity implements OnMapReadyCallback {

        private GoogleMap mMap;
        private Double lat, lng;
        LatLng fallLocation;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_location);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("100/falls");

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String date = ds.getKey();
                        Object data = ds.getValue();
                        if (data != null) {
                            if (data.getClass() == HashMap.class) {
                                HashMap<String, Double> coords = (HashMap<String, Double>) data;

                                if (!coords.isEmpty()) {
                                    lat = coords.get("lat");
                                    lng = coords.get("long");
                                    fallLocation = new LatLng(lat, lng);
                                    mMap.addMarker(new MarkerOptions().position(fallLocation).title(date));
                                }
                            }
                        }
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(fallLocation));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            };
            dbref.addValueEventListener(valueEventListener);



            // Add a marker in Sydney and move the camera

        }

}


