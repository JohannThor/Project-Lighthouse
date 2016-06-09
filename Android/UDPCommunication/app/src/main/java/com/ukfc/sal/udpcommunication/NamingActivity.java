package com.ukfc.sal.udpcommunication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class NamingActivity extends AppCompatActivity {
    private EditText name;
    private String object;
    private String dataToBePassed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name);
        //Sets the view corresponding to this activity

        Bundle extras = getIntent().getExtras();
        if(extras==null){
            Log.e("IntentError", "false intent");
            System.exit(0);
        }
        else{
            String[]transitionData = extras.getStringArray("object");
            if(transitionData.length!=2) {
                Log.e("NamingActivityError", "TransitionDataError: Data length must be 2");
                System.exit(0);
            }
            else {
                object = transitionData[0];
                dataToBePassed = transitionData[1];
            }
        }
        //Gets the extras list

        TextView description = (TextView)findViewById(R.id.description);
        switch(object){
            case "room": description.setText("Provide a unique name to this room");
                break;
            case "band": description.setText("Provide a unique name to this band");
                break;
            default:
                Log.e("NamingActivityError","Unknown object passed to namingactivity via putExtra method: "+object);
                System.exit(0);
                break;
        }
        //Sets the appropriate description as the main title for this activity

        name = (EditText)findViewById(R.id.name);
        Button completedNameButton = (Button)findViewById(R.id.completedNameButton);
        completedNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = name.getText().toString();
                Log.i("NamingActivity","Name of object entered is: "+data);
                //Log the name entered by the user
                String filename="";
                switch(object){
                    case "room":filename = "roomnames.txt";
                        break;
                    case "band":filename = "bandnames.txt";
                        break;
                    default:
                        Log.e("NamingActivityError","Unknown object passed to namingactivity via putExtra method: "+object);
                        System.exit(0);
                        break;
                }
                //Sets the appropriate description as the main title for this activity
                FileOperations fileOperations = new FileOperations(getApplicationContext());
                if(fileOperations.checkFileExists(filename)){
                    Log.i("File","File does exist");
                    if(!fileOperations.checkIfDuplicateData(filename, data)){
                        performWaiting(data);
                    }
                    else{
                        Context context = getApplicationContext();
                        CharSequence text = "Name not unique! Enter a unique name";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context,text,duration);
                        toast.show();
                    }
                }
                else{
                    Log.i("File","File does not exist");
                    performWaiting(data);
                }
            }
        });
        //When button pressed, get the message from the edittext and pass it to the
        //performWaiting method
    }
    /*
     * This method enables the transition to the waiting screen with the appropriate command
     * passed to the waiting activity
     */
    public void performWaiting(String data){
        Intent intent = new Intent(this,WaitingActivity.class);
        Bundle bundles = new Bundle();
        switch(object) {
            case "room":
                bundles.putString("command", "send_room_name");
                break;
            case "band":
                bundles.putString("command", "send_band_name");
                break;
            default:
                Log.e("NamingActivityError", "Unknown object passed to namingactivity via putExtra method: " + object);
                System.exit(0);
                break;
        }
        if(!dataToBePassed.isEmpty())
            bundles.putString("additional",dataToBePassed);
        //Creates a package of commands that is to be passed to the waiting activity
        //Appends data that should be passed to the next interface from previous interface
        //Only applies to Add Band as the IP is required for matching name to band

        bundles.putString("data",data);
        //Creates a package of commands that is to be passed to the waiting activity
        intent.putExtras(bundles);
        //Puts that package of commands into the intent
        startActivity(intent);
        finish();
    }
    /*
     * This method causes a transition to the main activity if back button is pressed
     */
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }


}
