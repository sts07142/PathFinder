package com.example.getapi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
public class MapActicity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    String[] MainDataArray;
    double startX,startY,endX,endY,CenterX,CenterY;
    String StartAddress,EndAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_acticity);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        String FromMainData = intent.getStringExtra("SendToMapData");
        MainDataArray = FromMainData.split(",");
        StartAddress = MainDataArray[0];
        startX = Double.parseDouble(MainDataArray[1]);
        startY = Double.parseDouble(MainDataArray[2]);
        EndAddress = MainDataArray[3];
        endX = Double.parseDouble(MainDataArray[4]);
        endY = Double.parseDouble(MainDataArray[5]);
        CenterX = (endX + startX)/2;
        CenterY = (endY + startY)/2;

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        LatLng Center = new LatLng(CenterX,CenterY);
        LatLng Start = new LatLng(startX, startY);
        LatLng end = new LatLng(endX, endY);

        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(Start);
        startMarker.title(StartAddress);
        startMarker.snippet("출발지");
        mMap.addMarker(startMarker);

        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(end);
        endMarker.title(EndAddress);
        endMarker.snippet("도착지");
        mMap.addMarker(endMarker);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Center, 13));

    }

}