package com.ukfc.sal.udpcommunication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class CompletedTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.completed_task);
        //Set the layout corresponding to this screen

        TextView titleView = (TextView)findViewById(R.id.titleView);
        TextView outputView = (TextView)findViewById(R.id.outputView);

        String[]transitionData = new String[2];
        Bundle extras = getIntent().getExtras();
        if(extras==null){
            Log.e("IntentError", "false intent");
            System.exit(0);
        }
        else{
            transitionData=extras.getStringArray("success");
        }
        //Gets the data that was passed by the previous activity

        switch (transitionData[0]) {
            case "group":
                titleView.setText("Completed Grouping Protocol!");
                break;
            case "configure":
                titleView.setText("Successfully obtained HUB IP Address!");
                break;
            case "send_room_name":
                titleView.setText("Successfully named group!");
                break;
            case "remove_room":
                titleView.setText("Successfully deleted room!");
                break;
            case "send_band_name":
                titleView.setText("Successfully named band!");
                break;
            case "remove_band":
                titleView.setText("Successfully deleted band!");
                break;
            case "track_band":
                titleView.setText("Successfully tracked band!");
                break;
            case "synchronise_band_list":
                titleView.setText("Successfully synchronised data!");
                break;
            default:
                Log.e("CompletedTaskError", "Unknown extra passed from Waiting_Activity to CompletedTaskActivity activity: " + transitionData[0]);
                System.exit(0);
                break;
        }
        outputView.setText(transitionData[1]);
        //Sets the correct title and output according to which button was pressed
        //to transition to the previous activity

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
