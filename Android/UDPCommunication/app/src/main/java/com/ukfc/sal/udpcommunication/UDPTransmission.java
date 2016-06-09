package com.ukfc.sal.udpcommunication;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import android.net.wifi.WifiManager.MulticastLock;

/**
 *
 * Created by Sal on 05/11/2015.
 */
public class UDPTransmission extends Thread {

    private WifiManager mWifi;
    //Used to obtain the multicast lock in order to receive from all IP address
    private GlobalVariables globalVariables;
    //Used to enable access to the global variables avaliable to the entire application
    private DatagramSocket socket;
    //Stores the socket object that enables udp communication
    private String message;
    //Stores the message that is received by the phone (if any)
    private String[]expectedMessage;
    //Used to store the expected messages passed to this class
    private int portNum=8050;
    //Stores the global port number
    private int bufferSize=10000;
    //Describes the size of the window for messages to be received/sent
    private boolean terminateThread = false;
    //Used to terminate the while loop externally

    public UDPTransmission(){
        mWifi = null;
        globalVariables = null;
        socket=null;
        message=null;
        expectedMessage=null;

    }

    public UDPTransmission(WifiManager nWifi,GlobalVariables globalVariables,String data,String[]expectedMessage){
        mWifi = nWifi;
        this.globalVariables = globalVariables;
        message=data;
        this.expectedMessage = expectedMessage;
        socket=null;
    }

    public void run(){
        Log.i("SUCCESS", "Running new thread!");

        MulticastLock lock=null;
        boolean hasSetHubIP = globalVariables.getHasSetHubIP();
        if(!hasSetHubIP){
            lock = mWifi.createMulticastLock("lock");
            lock.acquire();
        }
        //NOTE: If configuring hub ip address then the hasSetHubIP variable in GlobalVariables class should be set before running this clas

        //Enable UDP multicasting (Equivalently, allowing broadcasting to the local network via WIFI)

        socket=createSocket(hasSetHubIP);
        //Initialises and creates the socket

        if(!message.isEmpty())
            sendUDPMessage(message,hasSetHubIP);
        //If there was a message to be sent, send the message via that socket

        String dataReceived="";
        boolean isAnExpectedMessage = false;
        boolean sentOnce = false;
        while(isAnExpectedMessage==false) {
            if (!hasSetHubIP && !message.isEmpty() && sentOnce==false) {
                for (int i = 0; i < 2; i++) {
                    dataReceived = receiveUDPMessage();
                }
                sentOnce=true;
                //When you send a messsage, all receiving message are not your own broadcast
                //Therefore, do not skip the first message you receive on subsequent messages
            } else {
                dataReceived = receiveUDPMessage();
            }
            //Receive data via that socket.
            //If HUB IP not set, address is broadcast therefore, phone would receive its own data first
            //=> negate first message received
            dataReceived = dataReceived.trim();
            //Removes any hidden spaces
            for(int i=0;i<expectedMessage.length;i++) {
                if (dataReceived.contains(expectedMessage[i])) {
                    isAnExpectedMessage = true;
                    break;
                }
            }
            if(terminateThread==true){
                return;
            }//This error occurs if the user presses the back button during receiving udp message without timeout
        }//Ignore any message isnt expected

        if(!hasSetHubIP)
            lock.release();
        //Release the multicasting lock

        setGlobalVariables(dataReceived);
        //Set the message received and set the variable that states it was obtained

        closeSocket();
        //Close the socket
    }

    /*
     * This method creates a socket that can receive from all addresses on port specified
     * in the global variable.
     */
    public DatagramSocket createSocket(boolean hasSetHubIP){
        try{
            if(!hasSetHubIP) {
                socket = new DatagramSocket(portNum, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);
            }
            else
                socket = new DatagramSocket(portNum,InetAddress.getByName(globalVariables.getHubIP()));
        }
        catch(SocketException se){
            Log.e("ERROR", "Socket cannot be created on port selected" + se.getMessage());
            System.exit(0);
        }
        catch(UnknownHostException uhe){
            Log.e("ERROR", "Unable to get local host address" + uhe.getMessage());
            System.exit(0);
        }
        return socket;
    }
    /*
     * This method sends an UDP message that is passed as a parameter to the socket that is also
     * passed as a parameter
     */
    public void sendUDPMessage(String messageStr,boolean hasSetHubIP) {
        byte[] sendData = messageStr.getBytes();
        //Obtains the byte representation of the data that is to be sent

        DatagramPacket sendPacket = null;
        try {
            if(hasSetHubIP)
                sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(globalVariables.getHubIP()), portNum);
            else
                sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), portNum);
        }
        catch (Exception csp) {
            Log.e("ERROR", "Creation of datagram SEND packet error due to getBroadCastAddress Method: " + csp.getMessage());
            System.exit(0);
        }
        try {
            socket.send(sendPacket);
        }
        catch (IOException e) {
            Log.e("Tag", "Unable to send: " + e.getMessage());
            System.exit(0);
        }
    }
    /*
     * This method receives an UDP message of maximum 1500 bytes that is sent from the socket that was
     * passed as a parameter
     */
    public String receiveUDPMessage(){

        DatagramPacket receivePacket = null;
        String receivedData=null;
        byte[]rec_buff=new byte[bufferSize];

        try{
            receivePacket = new DatagramPacket(rec_buff, rec_buff.length);
        }
        catch(Exception csp){
            Log.e("ERROR", "Creation of datagram RECEIVE packet error due to getBroadCastAddress Method: " +csp.getMessage());
            System.exit(0);
        }
        try {
            socket.receive(receivePacket);
            receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
        } catch (IOException er) {
            Log.e("Tag", "Unable to receive: " + er.getMessage());
            terminateThread=true;
            return "";
        }
        Log.i("RECEIVED: ", receivedData);
        return receivedData;
    }
    /*
     * This method closes the socket that was passed as a parameter
     */
    public void closeSocket(){
        try {
            socket.close();
        } catch (Exception e) {
            Log.e("Tag", "Closing port error: " + e.getMessage());
            System.exit(0);
        }
    }
    /*
     * This method sets the message related global variables
    */
    private void setGlobalVariables(String message){
        globalVariables.setHasReceivedMessage(true);
        globalVariables.setMessage(message);
    }
    /*
     * This method enables jumping out of the while loop in the run()
     */
    public void setTerminateThread(){
        terminateThread=true;
    }
}
