package com.ukfc.sal.udpcommunication;

import android.content.Context;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class WaitingActivity extends AppCompatActivity {
    UDPTransmission udptransmission = null;
    //Used to reference the thread that will be stored in this variable
    TextView countdown;
    CountDownTimer timer=null;
    //Used to display & set up a timer for which will timeout if a udp message is not received
    String buttonPressed="";
    //Stores the command that corresponds to the button pressed before going to this screen
    String data="";
    //Stores data that is passed over to this activity
    boolean errorOccurred = false;
    //Used to determine whether to transition to the error screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting_activity);
        //Display the waiting activity interface
        Bundle extras = getIntent().getExtras();
        if(extras==null){
            Log.e("IntentError", "false intent");
            System.exit(0);
        }
        else{
            buttonPressed = extras.getString("command");
        }
        //Obtains a string that corresponds to what button was pressed on the main screen
        //else exit if there wasnt one.

        TextView primaryText = (TextView)findViewById(R.id.primaryText);
        TextView secondaryText = (TextView)findViewById(R.id.secondaryText);
        countdown = (TextView)findViewById(R.id.countdown);
        //Assign the link between the variable and the different interface textViews.

        String transmittedData="";
        String[]expectedMessage=null;
        switch(buttonPressed){
            case "group": primaryText.setText("Forming Group");
                secondaryText.setText("Awaiting hub response...");
                transmittedData = "Group Nodes";
                expectedMessage = new String[1];
                expectedMessage[0] = "Group Formed";
                break;
            case "configure": primaryText.setText("Obtaining Hub IP Address");
                secondaryText.setText("Awaiting hub response...");
                getGlobalVariables().setHasSetHubIP(false);
                //Reset sethubIP variable for changing destination ip to broadcast
                transmittedData = "Discover Hub";
                expectedMessage = new String[1];
                expectedMessage[0] = "Store Hub";
                break;
            case "name_room": primaryText.setText("Press a button on any node in the room you wish to name");
                secondaryText.setText("");
                transmittedData = "Expect Node_Button";
                expectedMessage = new String[1];
                expectedMessage[1] = "Discover Room_Name";
                break;
            case "send_room_name": data=extras.getString("data");
                primaryText.setText("Sending room name '"+data+"' to hub");
                secondaryText.setText("Awaiting hub response...");
                transmittedData = "Store Room_Name "+data;
                expectedMessage = new String[2];
                expectedMessage[0] = "Group Named";
                expectedMessage[1] = "Group_Name Failed";
                break;
            case "remove_room": data=extras.getString("data");
                primaryText.setText("Deleting room '"+data+"'");
                secondaryText.setText("Awaiting hub response...");
                transmittedData = "Remove Room "+data;
                expectedMessage = new String[2];
                expectedMessage[0] = "Room Deleted";
                expectedMessage[1] = "Room_Delete Failed";
                break;
            case "add_band": primaryText.setText("Press a button on the band you wish to setup");
                secondaryText.setText("");
                transmittedData = "";
                expectedMessage = new String[1];
                expectedMessage[1] = "Store Band";
                break;
            case "send_band_name": data=extras.getString("data");
                String ip = extras.getString("additional");
                primaryText.setText("Sending band name '"+data+"' to hub");
                secondaryText.setText("Awaiting hub response...");
                transmittedData = "Store Band_Name "+data+" "+ip;
                expectedMessage = new String[2];
                expectedMessage[0] = "Band Named";
                expectedMessage[1] = "Band_Name Failed";
                break;
            case "remove_band": data=extras.getString("data");
                primaryText.setText("Deleting band '"+data+"'");
                secondaryText.setText("Awaiting hub response...");
                transmittedData = "Remove Band "+data;
                expectedMessage = new String[2];
                expectedMessage[0] = "Band Deleted";
                expectedMessage[1] = "Band_Delete Failed";
                break;
            case "track_band": data=extras.getString("data");
                primaryText.setText("Triangulating '"+data+"'");
                secondaryText.setText("Awaiting completion...");
                transmittedData = "Triangulate Band "+data;
                expectedMessage = new String[1];
                expectedMessage[0] = "Triangulate "+data;
                break;
            case "synchronise_room_list":primaryText.setText("Synchronising Room List");
                secondaryText.setText("Awaiting completion...");
                transmittedData = "Sync Room_List";
                expectedMessage = new String[1];
                expectedMessage[0] = "Sync Room_List";
                break;
            case "synchronise_band_list":primaryText.setText("Synchronising Band List");
                secondaryText.setText("Awaiting completion...");
                transmittedData = "Sync Band_List";
                expectedMessage = new String[1];
                expectedMessage[0] = "Sync Band_List";
                break;
            default:
                Log.e("WaitingActivityErrorA","Unknown command passed to waitingactivity via putExtra method: "+buttonPressed);
                System.exit(0);
                break;
        }
        //Configures the screen corresponding to this activity according to which button was pressed on the main screen and define which message should be
        //sent to the Hub

        udptransmission = new UDPTransmission(getWifiManager(),getGlobalVariables(),transmittedData,expectedMessage);
        Thread t = new Thread(udptransmission);
        t.start();
        //Creates an instance of the UDP transmission class and start it in a new thread

        main();
    }

    /*
     * This method starts a 30 second timer and perform the correct output based on
     * whether a message is received or not within this time.
     */
    public void main(){
        final GlobalVariables globalVariables = getGlobalVariables();
        timer = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                countdown.setText("Seconds remaining before timeout: " + millisUntilFinished / 1000);
                if (globalVariables.getHasReceivedMessage() == true) {
                    this.cancel();
                    performFinishing();
                    //Stop timer and perform the final part of the code (Processing the message)
                }
                //Checks if a message has been received every second
            }

            public void onFinish() {
                countdown.setText("Timed out!");
                performTimeout();
            }
            //Transition to the timeout screen
        }.start();
        //Code reference:
    }
    /*
     * This method returns the wifimanager which could not be obtained in the thread class
     */
    public WifiManager getWifiManager(){
        return ((WifiManager)this.getSystemService(Context.WIFI_SERVICE));
    }
    /*
     * This method returns the global variable class that contains the global variables
     * that is seen by the entire application.
     */
    public GlobalVariables getGlobalVariables(){
        return (GlobalVariables)getApplicationContext();
    }

    /*
     * This method process the received message by comparing it to recognised messages and extract data if required
     */
    public String processReceivedMessage(String message){
        String output="";
        String[]partitionedData = message.split(" ");
        //Splits the data by using the space delimiter
        switch(buttonPressed){
            case "group": output=checkGroupMessage(partitionedData);
                break;
            case "configure": output=checkConfigureMessage(partitionedData);
                break;
            case "name_room": output=checkNameRoomMessage(partitionedData);
                break;
            case "send_room_name": output=checkSendRoomNameMessage(partitionedData);
                break;
            case "remove_room": output=checkRemoveRoomMessage(partitionedData);
                break;
            case "add_band": output=checkAddBandMessage(partitionedData);
                break;
            case "send_band_name": output=checkSendBandNameMessage(partitionedData);
                break;
            case "remove_band": output=checkRemoveBandMessage(partitionedData);
                break;
            case "track_band": output=checkTrackBandMessage(partitionedData);
                break;
            default:
                break;
            //Process the message with rules according to which button press caused the transition to this screen
            //The default case is already handled previously but stated here for completeness
        }
        return output;
        //Returns extra data that would be included in the next activity
    }
    /*
    * This method process the message that is expected whilst in the "forming group" waiting screen
    */
    public String checkGroupMessage(String[]partitionedData){
        if(partitionedData.length!=2){
            errorOccurred = true;
            return "Data should contain exactly 2 words separated by spaces. Received: "+partitionedData.length+" word(s)";
        }
        return "";
    }
    /*
     * This method process the message that is expected whilst in the "Obtaining Hub IP Address" waiting screen
     */
    public String checkConfigureMessage(String[]partitionedData){
        if(partitionedData.length!=3){
            errorOccurred = true;
            return "Data should contain exactly 3 words separated by spaces. Received: "+partitionedData.length+" word(s)";
        }
        //Check for length of message
        if(!partitionedData[2].matches("\\d+.\\d+.\\d+.\\d+")){
            errorOccurred = true;
            return "3rd Word/IP Address Is Unknown!. Received: "+partitionedData[2];
        }
        //Checks if the address has a valid format
        getGlobalVariables().setHasSetHubIP(true);
        getGlobalVariables().setHubIP(partitionedData[2]);
        //Sets the hub address and its confirmation variable
        return partitionedData[2];
        //Return the hub IP address
    }
    /*
    * This method process the message that is expected whilst in the "Press a button on the node..." waiting screen
    */
    public String checkNameRoomMessage(String[]partitionedData){
        if(partitionedData.length!=2){
            errorOccurred = true;
            return "Data should contain exactly 2 words separated by spaces. Received: "+partitionedData.length+" word(s)";
        }
        return "";
    }
    /*
     * This method process the message that is expected whilst in the "Sending Room Name to Hub" waiting screen
     */
    public String checkSendRoomNameMessage(String[] partitionedData) {
        if (partitionedData.length != 2) {
            errorOccurred = true;
            return "Data should contain exactly 2 words separated by spaces. Received: " + partitionedData.length + " word(s)";
        }
        //Check for length of message
        if (partitionedData[1].equalsIgnoreCase("Failed")) {
            errorOccurred = true;
            return "Room name has already been assigned. Please Synchronise list & Retry";
        }
        //If user hasnt synchronised their local room list then choosing the same room name as another
        //room can happen due to the hub receiving such name from another phone
        return "";
    }
    /*
     * This method process the message that is expected whilst in the "Sending Band for deletion to Hub" waiting screen
     */
    public String checkRemoveRoomMessage(String[] partitionedData) {
        if (partitionedData.length != 2) {
            errorOccurred = true;
            return "Data should contain exactly 2 words separated by spaces. Received: " + partitionedData.length + " word(s)";
        }
        //Check for length of message
        if (partitionedData[1].equalsIgnoreCase("Failed")) {
            errorOccurred = true;
            return "Room name has already been deleted. Please Synchronise list & Retry";
        }
        //If user hasnt synchronised their local room list then deleting a room name that doesnt exist globally i.e. at hub,
        //can happen due to the hub receiving such name from another phone for deletion at an earlier time
        return "";
    }
    /*
    * This method process the message that is expected whilst in the "Press a button on the band..." waiting screen
    */
    public String checkAddBandMessage(String[]partitionedData) {
        if (partitionedData.length != 3) {
            errorOccurred = true;
            return "Data should contain exactly 3 words separated by spaces. Received: " + partitionedData.length + " word(s)";
        }
        //Check for length of message
        if (!partitionedData[2].matches("\\d+.\\d+.\\d+.\\d+")) {
            errorOccurred = true;
            return "3rd Word/IP Address Is Unknown!. Received: " + partitionedData[2];
        }
        //Checks if the address has a valid format
        return partitionedData[2];
        //Return the hub IP address

    }
    /*
     * This method process the message that is expected whilst in the "Sending Band Name to Hub" waiting screen
     */
    public String checkSendBandNameMessage(String[] partitionedData) {
        if (partitionedData.length != 2) {
            errorOccurred = true;
            return "Data should contain exactly 2 words separated by spaces. Received: " + partitionedData.length + " word(s)";
        }
        //Check for length of message
        if (partitionedData[1].equalsIgnoreCase("Failed")) {
            errorOccurred = true;
            return "Band name has already been assigned. Please Synchronise list & Restart band";
        }
        //If user hasnt synchronised their local band list then choosing the same name as another
        //band can happen due to the hub receiving such name from another phone
        return "";
    }

    /*
     * This method process the message that is expected whilst in the "Sending Band for deletion to Hub" waiting screen
     */
    public String checkRemoveBandMessage(String[] partitionedData) {
        if (partitionedData.length != 2) {
            errorOccurred = true;
            return "Data should contain exactly 2 words separated by spaces. Received: " + partitionedData.length + " word(s)";
        }
        //Check for length of message
        if (partitionedData[1].equalsIgnoreCase("Failed")) {
            errorOccurred = true;
            return "Band name does not appear in global list. Please Synchronise list as band has already been deleted.";
        }
        //If user hasnt synchronised their local band list then choosing a band name that was deleted by another phone is possible
        return "";
    }

    /*
    * This method process the message that is expected whilst in the "Triangulating Band..." waiting screen
    */
    public String checkTrackBandMessage(String[]partitionedData) {
        if (partitionedData.length != 3) {
            errorOccurred = true;
            return "Data should contain exactly 3 words separated by spaces. Received: " + partitionedData.length + " word(s)";
        }
        //Check for length of message
        if (partitionedData[2].equalsIgnoreCase("Failed")) {
            errorOccurred=true;
            return "Triangulation of"+data+" could not be done. Please synchronise list and retry again.";
        }
        return partitionedData[2];
        //Return the room name of where the band is currently
    }

    /*
     * This method gets the processed received message and causes the transition to the next screen
     * according to which button was pressed that caused this screen to appear.
     */
    public void performFinishing(){
        getGlobalVariables().setHasReceivedMessage(false);
        //Reset variable
        String processedData = processReceivedMessage(getGlobalVariables().getMessage());
        //Get any output of the processedData and trim to remove unseen spaces at the start and end of the message
        FileOperations fileOperations = new FileOperations(getApplicationContext());
        //For any file writing operations that are required
        if(!errorOccurred) {
            switch (buttonPressed) {
                case "group":
                    performSuccessfulCompletion(processedData);
                    break;
                case "configure":
                    performSuccessfulCompletion(processedData);
                    break;
                case "name_room":
                    startNamingActivity("room", "");
                    break;
                case "send_room_name":
                    fileOperations.writeDataToFile("roomnames.txt", data,false);
                    performSuccessfulCompletion(processedData);
                    break;
                case "remove_room":
                    fileOperations.deleteDataFromFile("roomnames.txt", data);
                    performSuccessfulCompletion(processedData);
                    break;
                case "add_band":
                    startNamingActivity("band", processedData);
                    break;
                case "send_band_name":
                    fileOperations.writeDataToFile("bandnames.txt", data,false);
                    performSuccessfulCompletion(processedData);
                    break;
                case "remove_band":
                    fileOperations.deleteDataFromFile("bandnames.txt", data);
                    performSuccessfulCompletion(processedData);
                    break;
                case "track_band":
                    performSuccessfulCompletion(processedData);
                    break;
                case "synchronise_room_list":
                    String room_list = getGlobalVariables().getMessage().trim();
                    writeOverFile("roomnames.txt", room_list,fileOperations);
                    //Write over previous file with incoming data
                    repeatWaitingActivity("synchronise_band_list");
                    //Repeat waiting activity with band list
                    break;
                case "synchronise_band_list":
                    String band_list = getGlobalVariables().getMessage().trim();
                    writeOverFile("bandnames.txt",band_list,fileOperations);
                    //Write over previous file with incoming data
                    performSuccessfulCompletion(processedData);
                    break;
                default:
                    Log.e("WaitingActivityError","Unknown extra passed from main to waitingactivity");
                    System.exit(0);
                    break;
                //Perform the correct method corresponding to the next transition to the next screen
            }
        }
        else
            performError(processedData);
        //Output the error screen as invalid data received.
    }
    /*
     * This method writes the supplied data to a specified file (For synchronisation commands)
     */
    public void writeOverFile(String filename,String list,FileOperations fileOperations){
        String[]partitionedData = list.split(" ");
        if(partitionedData.length!=2) {
            for (int i = 2; i < partitionedData.length; i++) {
                if (i == 2)
                    fileOperations.writeDataToFile(filename, partitionedData[i], true);
                else
                    fileOperations.writeDataToFile(filename, partitionedData[i], false);
            }
        } else{
            fileOperations.deleteFile(filename);
        }
        //Rewrite over previous file with incoming data
    }

    /*
    * This method causes a transition to the NamingActivity activity
    */
    public void repeatWaitingActivity(String nextCommand){
        Intent intent = new Intent(this,WaitingActivity.class);
        intent.putExtra("command",nextCommand);
        startActivity(intent);
        finish();
        //Repeat waiting activity
    }

    /*
     * This method causes a transition to the successful completion screen and
     * passes a command that will alter the layout of the screen according to
     * which button was pressed & the data after processing the message.
     */
    public void performSuccessfulCompletion(String successOutput){
        String[]transitionData = {buttonPressed,successOutput};
        //This is needed as the group screen would be different to the configure screen (For text output only)
        Intent intent = new Intent(this,CompletedTaskActivity.class);
        intent.putExtra("success",transitionData);
        startActivity(intent);
        finish();
        //Go to completion screen
    }
    /*
     * This method causes a transition to the error screen and
     * passes a command that will alter the layout of the screen according to
     * which button was pressed & the data after processing the message.
     */
    public void performError(String errorOutput){
        Intent intent = new Intent(this,ErrorActivity.class);
        intent.putExtra("error",errorOutput);
        startActivity(intent);
        finish();
        //Go to error screen
    }
    /*
     * This method causes a transition to the NamingActivity activity
     */
    public void startNamingActivity(String objectToBeNamed,String data){
        String[]transitionData = {objectToBeNamed,data};
        //A composite of what object to be named and any additional data
        Intent intent = new Intent(this,NamingActivity.class);
        intent.putExtra("object",transitionData);
        startActivity(intent);
        finish();
        //Go to roomname screen
    }
    /*
     * This method causes a transition to the timeout activity
     */
    public void performTimeout(){
        Intent intent = new Intent(this,TimeoutActivity.class);
        startActivity(intent);
        finish();
        //Go to timeout screen
    }

    /*
     * This method causes a transition to the main activity if back button is pressed
     */
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        timer.cancel();
        udptransmission.setTerminateThread();
        udptransmission.closeSocket();
        finish();
    }

    /*
     * This method ensures that the socket is closed and cancels the timer (If it hasnt done so already) when the activity is destroyed
     */
    @Override
    public void onDestroy(){
        timer.cancel();
        udptransmission.setTerminateThread();
        udptransmission.closeSocket();
        super.onDestroy();
    }

}
