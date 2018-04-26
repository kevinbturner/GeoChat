package edu.fsu.cs.mobile.geochat;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

/*
*   This chat activity and the ability to chat in real time is possible by following this video guide:
*   -https://www.youtube.com/watch?v=Xn0tQHpMDnM
*/

public class ChatActivity extends AppCompatActivity {

    private TextView send;
    private EditText message;
    private FirebaseListAdapter<ChatMessage> fAdapter;
    private String result;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionsGranted = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        send = (TextView) findViewById(R.id.send);
        message = (EditText) findViewById(R.id.chatbox);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String chat = "Palace Saloon";       //Temporary, for testing and such

                if(chat.equals("Palace Saloon")) {
                    FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(message.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                }
                else if(chat.equals("Township")) {
                    FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(message.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                }
                else if(chat.equals("Warhorse")) {
                    FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(message.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                }
                else if(chat.equals("Brass Tap")) {
                    FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(message.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                }
                else if(chat.equals("Potbelly's")) {
                    FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(message.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                }
                else if(chat.equals("Bullwinkle's")) {
                    FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(message.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                }
                else
                    Toast.makeText(getApplicationContext(), "You shouldn't be in this activity right now. What happened?", Toast.LENGTH_SHORT).show();

                message.setText("");
                displayChatMessage();
            }
        });

        displayChatMessage();
    }

    public void displayChatMessage() {
        ListView messageList = (ListView) findViewById(R.id.messageList);

        fAdapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.message_item, FirebaseDatabase.getInstance().getReference()) {

            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView message, user, date;
                message = (TextView) v.findViewById(R.id.message_text);
                user = (TextView) v.findViewById(R.id.message_user);
                date = (TextView) v.findViewById(R.id.message_time);

                message.setText(model.getText());
                user.setText(model.getUser());
                date.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getTime()));
            }
        };

        messageList.setAdapter(fAdapter);
    }

    /*      TODO: Make sure the device can check location

    //Checks your current location, and what chat you should be able to join
    public String checkLocation(){

        try{
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();

                            //Within Palace Saloon area
                            if(distance(currentLocation.getLatitude(), currentLocation.getLongitude(), 30.434387, -84.304215) < 0.1)
                                result = "Palace Saloon";
                            //Within Township area
                            else if(distance(currentLocation.getLatitude(), currentLocation.getLongitude(), 30.437060, -84.297135) < 0.1)
                                result = "Township";
                            //Within Warhorse area
                            else if(distance(currentLocation.getLatitude(), currentLocation.getLongitude(), 30.435628, -84.290442) < 0.1)
                                result = "Warhorse";
                            //Within Brass Tap area
                            else if(distance(currentLocation.getLatitude(), currentLocation.getLongitude(), 30.436719, -84.293392) < 0.1)
                                result = "Brass Tap";
                            //Within Potbelly's area
                            else if(distance(currentLocation.getLatitude(), currentLocation.getLongitude(), 30.440783, -84.288235) < 0.1)
                                result = "Potbelly's";
                            //Within Bullwinkle's area
                            else if(distance(currentLocation.getLatitude(), currentLocation.getLongitude(), 30.445064, -84.291762) < 0.1)
                                result = "Bullwinkle's";
                            //If not in any of these locations, you are out of range, and must be kicked out of chat
                            else{
                                Toast.makeText(getApplicationContext(), "Out of range! Leaving chat...", Toast.LENGTH_SHORT).show();
                                result = "";
                                startActivity(new Intent(ChatActivity.this, MapsActivity.class));
                            }

                        }else{
                            Toast.makeText(ChatActivity.this, "What is going on?", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                return result;
            }
        }catch (SecurityException e){
            Toast.makeText(getApplicationContext(), "An error has occured! Please try again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ChatActivity.this, MapsActivity.class));
            return "";
        }

        return "";
    }

    */

    /*
     *   This function's code was found on stackoverflow, linked here: https://stackoverflow.com/questions/19056075/how-to-know-if-an-android-device-is-near-an-address-google-maps-api
     */

    /** calculates the distance between two locations in MILES */
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometers

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist;
    }
}
