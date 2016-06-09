package com.ukfc.sal.udpcommunication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TimeoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeout);
        //Outputs the timeout screen
    }
    /*
     * This method causes a transition to the main activity if back button is pressed
     */
    @Override
    public void onBackPressed(){
        finish();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }


}
