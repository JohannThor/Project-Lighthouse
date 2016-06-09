package com.ukfc.sal.udpcommunication;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.jar.Attributes;

public class ListActivity extends AppCompatActivity {
    private String operation="";
    private String filename = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if(extras==null){
            Log.e("IntentError", "false intent");
            System.exit(0);
        }
        else{
            operation = extras.getString("command");
        }
        setContentView(R.layout.list_activity);

        TextView listTitle = (TextView)findViewById(R.id.listTitle);

        switch(operation){
            case "remove_band": filename="bandnames.txt";
                listTitle.setText("Check the button for which band you wish to remove");
                break;
            case "track_band": filename="bandnames.txt";
                listTitle.setText("Check the button for which band you wish to track");
                break;
            case "remove_room": filename="roomnames.txt";
                listTitle.setText("Check the button for which room you wish to remove");
                break;
            default:
                Log.e("UnknownExtraPassed","Unknown extra passed to list_activity");
                System.exit(0);
                break;
        }
        RadioGroup group = (RadioGroup)findViewById(R.id.list_group);
        createRadioButtons(group);

    }

    public void createRadioButtons(RadioGroup group){
        int counter = 0;
        BufferedReader reader=null;
        String filepath = getFilesDir().getAbsolutePath()+"/"+filename;
        try{
            reader = new BufferedReader(new FileReader(filepath));
            String line="";
            while((line=reader.readLine())!=null){
                counter++;
                RadioButton item = new RadioButton(getApplicationContext());
                item.setText(line);
                item.setTextColor(Color.BLACK);
                item.setGravity(Gravity.LEFT);
                item.setId(counter);
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRadioButtonClicked(v);
                    }
                });
                group.addView(item);
            }
            reader.close();
        }
        catch(FileNotFoundException notFoundError) {
            //Log.e("NotFoundError", notFoundError.toString());
            //System.exit(0);
            Context context = getApplicationContext();
            CharSequence text = "List is empty!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context,text,duration);
            toast.show();
            //Show a pop up to alert the user
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
            //Return back to the main
        }
        catch(IOException readFileError){
            Log.e("ReadFileError", readFileError.toString());
            System.exit(0);
        }
    }

    public void onRadioButtonClicked(View v){
        int index=v.getId();
        FileOperations fileOperations = new FileOperations(getApplicationContext());
        String data = fileOperations.getDataAtIndex(filename,index);
        //Get the option as a text as only the id of the chosen option can be obtained
        Intent intent = new Intent(this,WaitingActivity.class);

        Bundle bundles = new Bundle();
        bundles.putString("command", operation);
        bundles.putString("data",data);
        //Creates a package of commands that is to be passed to the waiting activity
        intent.putExtras(bundles);
        //Puts that package of commands into the intent

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}

