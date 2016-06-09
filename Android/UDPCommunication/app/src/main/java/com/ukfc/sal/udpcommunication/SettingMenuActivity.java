package com.ukfc.sal.udpcommunication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SettingMenuActivity extends AppCompatActivity {
    String item = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_menu);

        Bundle extras = getIntent().getExtras();
        if(extras==null){
            Log.e("IntentError", "false intent");
            System.exit(0);
        }
        else{
            item = extras.getString("command");
        }
        Button add = (Button)findViewById(R.id.add);
        Button remove = (Button)findViewById(R.id.remove);

        switch(item){
            case "room": add.setText("Add Room");
                remove.setText("Remove Room");
                break;
            case "band": add.setText("Add Band");
                remove.setText("Remove Band");
                break;
            default:
                Log.e("ExtrasError","The command passed over from main to settingmenu activity is not recognised: "+item);
                System.exit(0);
                break;
        }
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddActivity();
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRemoveActivity();
            }
        });

    }
    public void startAddActivity(){
        Intent intent = new Intent(this,WaitingActivity.class);
        if(item.equals("room"))
            intent.putExtra("command","name_room");
        else
            intent.putExtra("command","add_band");
        //Include the variable "command" that will be used in the WaitingActivity
        //screen to output the appropriate interface.
        startActivity(intent);
        finish();
        //Start the activity and close this interface
    }
    public void startRemoveActivity(){
        Intent intent = new Intent(this,ListActivity.class);
        if(item.equals("room"))
            intent.putExtra("command", "remove_room");
        else
            intent.putExtra("command", "remove_band");
        //Include the variable "command" that will be used in the WaitingActivity
        //screen to output the appropriate interface.
        startActivity(intent);
        finish();
        //Start the activity and close this interface
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
