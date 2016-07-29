package ucl.LightHouse;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

class UdpPacketSender {
	
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