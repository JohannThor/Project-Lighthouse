package ucl.LightHouse;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UdpPacketSender.java 
 * Purpose: Send sensor data to Lighthouse platform. The server's IP and ports are stored in 
 * config.properties file.
 *
 * @author Vesko
 * @version 1.0 27.08.2016
 */

class UdpPacketSender {

	/**
	 * Send array of bytes to the Lighthouse platform asynchronously.
	 *  Since the data is sent asynchronously the
	 * result is return as a callback function which should be overwritten by
	 * extending the Response class.
	 *
	 * @param data
	 *            json string represented as array of bytes
	 * @param response
	 *            class extending Response and implementing callback function
	 * 
	 * @return void
	 */
	public void sendPacketAsync(final byte[] data, final Response<Boolean> response) {

		Thread t = new Thread(new Runnable() {
		    public void run() {
		    	try {
		        	
		        	DatagramSocket testSocket = new DatagramSocket();
		        	String strIP = PropertiesRetriever.getInstance().getServerIP("config.properties");
		            InetAddress IPAddress = InetAddress.getByName(strIP);
		            int port = PropertiesRetriever.getInstance().getServerPort("config.properties");
		            DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, port);
		            testSocket.send(packet);
		            testSocket.close();
		            response.callback(true);
		            
		        } catch (SocketException | UnknownHostException e) {
		            e.printStackTrace();
		            response.callback(false);
		        } catch (IOException e) {
		        	e.printStackTrace();
		        	response.callback(false);
		        } catch (NumberFormatException e) {
		        	e.printStackTrace();
		        	response.callback(false);
		        } 
		    }
		});
		
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
	
	/**
	 * Send array of bytes to the Lighthouse platform synchronously.
	 *
	 * @param data
	 *            json string represented as array of bytes
	 * 
	 * @return result of the sending as a boolean
	 */
    public boolean sendPacketSync(final byte[] data) {
    	final AtomicBoolean result = new AtomicBoolean();
    	
        Thread t = new Thread(new Runnable() {
		    public void run() {
		    	try {
		        	
		        	DatagramSocket testSocket = new DatagramSocket();
		        	String strIP = PropertiesRetriever.getInstance().getServerIP("config.properties");
		            InetAddress IPAddress = InetAddress.getByName(strIP);
		            int port = PropertiesRetriever.getInstance().getServerPort("config.properties");
		            DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, port);
		            testSocket.send(packet);
		            testSocket.close();
		            result.set(true);
		            
		        } catch (SocketException | UnknownHostException e) {
		            e.printStackTrace();
		            result.set(false);
		        } catch (IOException e) {
		        	e.printStackTrace();
		        	result.set(false);
		        } catch (NumberFormatException e) {
		        	e.printStackTrace();
		        	result.set(false);
		        } 
		    }
		});
		
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		try {
			t.join();
			result.set(true);
		} catch (InterruptedException e) {
			e.printStackTrace();
			result.set(false);
		}
        
        return result.get();
    }
}