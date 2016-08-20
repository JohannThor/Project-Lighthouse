
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import ucl.LightHouse.LightHouseAPI;
import ucl.LightHouse.Obstacle;

import java.io.*;

import java.net.*;

public class DataTransfer {
	public static void main(String[] args) {
		final Timer tm = new Timer();
		java.util.TimerTask task = null;
		try {
			task = new TimerTask() {
				DatagramSocket testSocket = new DatagramSocket();
				InetAddress IPAddress = InetAddress.getByName("51.254.222.223");
				LocGenerator loc = new LocGenerator();
				int count = 0;
				@Override
				public void run() {

					if(count <= 10)
					{
						JSONObject obj = loc.generateLocJSON();
						LighthouseDTO sc = new LighthouseDTO();
						byte[] data = sc.toBytes(obj);

						DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, 9998);
						try {
							testSocket.send(packet);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
					{
						tm.cancel();
						tm.purge();
						testSocket.close();   
					}
					count++;
				}
			};
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
		tm.schedule(task, 1l, 100l);
	}    
}