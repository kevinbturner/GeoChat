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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

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
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

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

    private void loadAddresses(){
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

                    MarkerOptions mOptions = new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Palace Saloon");
                    mMap.addMarker(mOptions);
                }
            }else if(i == 1){
                try {
                    addressList = geocoder.getFromLocationName(search[1], 1);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    MarkerOptions mOptions = new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Potbelly's");
                    mMap.addMarker(mOptions);
                }
            }else if(i == 2){
                try {
                    addressList = geocoder.getFromLocationName(search[2], 1);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    MarkerOptions mOptions = new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Brass Tap");
                    mMap.addMarker(mOptions);
                }
            }else if(i == 3){
                try {
                    addressList = geocoder.getFromLocationName(search[3], 1);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    MarkerOptions mOptions = new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Warhorse");
                    mMap.addMarker(mOptions);
                }
            }else if(i == 4){
                try {
                    addressList = geocoder.getFromLocationName(search[4], 1);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    MarkerOptions mOptions = new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Township");
                    mMap.addMarker(mOptions);
                }
            }else{
                try {
                    addressList = geocoder.getFromLocationName(search[5], 1);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    MarkerOptions mOptions = new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Bullwinkle's");
                    mMap.addMarker(mOptions);
                }
            }
        }
    }
}
