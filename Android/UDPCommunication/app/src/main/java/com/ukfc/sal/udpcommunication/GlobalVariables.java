package com.ukfc.sal.udpcommunication;

import android.app.Application;

/**
 * Created by Sal on 19/11/2015.
 */
public class GlobalVariables extends Application {

    private boolean hasSetHubIP=false;
    private String hubIP="255.255.255.255";

    private boolean hasReceivedMessage=false;
    private String message="";

    public void setHasReceivedMessage(boolean state){hasReceivedMessage=state;}
    public boolean getHasReceivedMessage(){return hasReceivedMessage;}

    public void setMessage(String m){message = m;}
    public String getMessage(){return message;}

    public void setHasSetHubIP(boolean state){
        hasSetHubIP=state;
    }
    public boolean getHasSetHubIP(){return hasSetHubIP;}

    public void setHubIP(String hub_ip){hubIP = hub_ip;}
    public String getHubIP(){return hubIP;}


}
