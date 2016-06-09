package com.ukfc.sal.udpcommunication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ErrorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error);
        //Sets the layout corresponding to this activity

        String errorString ="";
        Bundle extras = getIntent().getExtras();
        if(extras==null){
            Log.e("IntentError", "false intent");
            System.exit(0);
        }
        else{
            errorString = extras.getString("error");
        }
        //Obtains the error string

        TextView errorTextView = (TextView)findViewById(R.id.errorTextView);
        errorTextView.setText(errorString);
        //Output the error
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
