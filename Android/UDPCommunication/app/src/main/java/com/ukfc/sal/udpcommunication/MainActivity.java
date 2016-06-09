package com.ukfc.sal.udpcommunication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //Display main interface and if there was a saved instance, display it

        Button group = (Button)findViewById(R.id.group);
        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performWaiting("group");
            }
        });

        Button triangulate = (Button)findViewById(R.id.triangulate);
        triangulate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToListActivity("track_band");
            }
        });

        Button configure = (Button)findViewById(R.id.configure);
        configure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performWaiting("configure");
            }
        });

        Button room = (Button)findViewById(R.id.room);
        room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToSettingActivity("room");
            }
        });

        Button band = (Button)findViewById(R.id.band);
        band.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToSettingActivity("band");
            }
        });

        Button synchronise = (Button)findViewById(R.id.synchronise);
        synchronise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performWaiting("synchronise_room_list");
                //Initially synchronise room list first
            }
        });
        //Assign the link between the variable and the different interface buttons
        //Perform the correct transition based on which button was pressed
    }
    public void performWaiting(String command){
        Intent intent = new Intent(this,WaitingActivity.class);
        intent.putExtra("command",command);
        //Include the variable "command" that will be used in the WaitingActivity
        //screen to output the appropriate interface.
        startActivity(intent);
        finish();
        //Start the activity and close this interface
    }
    public void transitionToSettingActivity(String command){
        Intent intent = new Intent(this,SettingMenuActivity.class);
        intent.putExtra("command",command);
        //Include the variable "command" that will be used in the WaitingActivity
        //screen to output the appropriate interface.
        startActivity(intent);
        finish();
        //Start the activity and close this interface
    }
    public void transitionToListActivity(String command){
        Intent intent = new Intent(this,ListActivity.class);
        intent.putExtra("command",command);
        //Include the variable "command" that will be used in the WaitingActivity
        //screen to output the appropriate interface.
        startActivity(intent);
        finish();
        //Start the activity and close this interface
    }
}
