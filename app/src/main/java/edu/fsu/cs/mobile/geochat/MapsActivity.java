package edu.fsu.cs.mobile.geochat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
*   Online guides were used to help create this code, listed as follows:
*   -Google Maps and initial setup: https://developers.google.com/maps/documentation/android-api/start
*   -Fetching current location: Video tutorial - https://www.youtube.com/watch?v=fPFr0So1LmI&t=375s
*   -Placing markers: Video tutorials - https://www.youtube.com/watch?v=MWowf5SkiOE, https://www.youtube.com/watch?v=s_6xxTjoLGY
*/

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    Marker palace, town, bull, pots, horse, brass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();

        getLocationPermission();

        //TODO: Check this
        //Use this once a sign out option has been created
        //mAuth.signOut();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(MapsActivity.this, MainActivity.class));
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            loadAddresses();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    15f);

                        }else{
                            Toast.makeText(MapsActivity.this, "Unable to fetch current location!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
        }
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        String name = marker.getTitle();

        if(name.equals("Palace Saloon")){
            Toast.makeText(getApplicationContext(), "You clicked on palace!", Toast.LENGTH_SHORT).show();
            return true;
        }else if(name.equals("Township")){
            Toast.makeText(getApplicationContext(), "You clicked on township!", Toast.LENGTH_SHORT).show();
            return true;
        }else if(name.equals("Bullwinkle's")){
            Toast.makeText(getApplicationContext(), "You clicked on bullwinkle's!", Toast.LENGTH_SHORT).show();
            return true;
        }else if(name.equals("Warhorse")){
            Toast.makeText(getApplicationContext(), "You clicked on warhorse!", Toast.LENGTH_SHORT).show();
            return true;
        }else if(name.equals("Potbelly's")){
            Toast.makeText(getApplicationContext(), "You clicked on pots!", Toast.LENGTH_SHORT).show();
            return true;
        }else if(name.equals("Brass Tap")){
            Toast.makeText(getApplicationContext(), "You clicked on brass tap!", Toast.LENGTH_SHORT).show();
            return true;
        }else {
            Toast.makeText(getApplicationContext(), "What happened here?", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void loadAddresses(){
        mMap.setOnMarkerClickListener(this);

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> addressList = new ArrayList<>();

        String [] search = new String[6];
        search[0] = "1303 Jackson Bluff Rd, Tallahassee, FL 32303";         //Palace Saloon
        search[1] = "459 W College Ave, Tallahassee, FL 32301";             //Pots
        search[2] = "699 W Gaines St #110, Tallahassee, FL 32304";          //Brass Tap
        search[3] = "603 W Gaines St, Tallahassee, FL 32304";               //Warhorse
        search[4] = "619 S Woodward Ave, Tallahassee, FL 32304";            //Township
        search[5] = "620 W Tennessee St, Tallahassee, FL 32304";            //Bullwinkle's

        for(int i = 0; i < 6; i++) {

            if(i == 0) {
                try {
                    addressList = geocoder.getFromLocationName(search[0], 1);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    palace = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Palace Saloon")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                }
            }else if(i == 1){
                try {
                    addressList = geocoder.getFromLocationName(search[1], 1);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    pots = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Potbelly's")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                }
            }else if(i == 2){
                try {
                    addressList = geocoder.getFromLocationName(search[2], 1);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    brass = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Brass Tap")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                }
            }else if(i == 3){
                try {
                    addressList = geocoder.getFromLocationName(search[3], 1);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    horse = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Warhorse")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                }
            }else if(i == 4){
                try {
                    addressList = geocoder.getFromLocationName(search[4], 1);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    town = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Township")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                }
            }else{
                try {
                    addressList = geocoder.getFromLocationName(search[5], 1);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    bull = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Bullwinkle's")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                }
            }
        }
    }
}
