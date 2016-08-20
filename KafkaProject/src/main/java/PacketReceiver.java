import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class PacketReceiver {
	
	private DatagramSocket socket = null;
	private Integer port = null;
	private byte[] receivedData = new byte[4096];
	
	private PacketReceiver(){}
	public PacketReceiver(Integer port) throws Exception {
		this.port = port;
		socket = new DatagramSocket(this.port);
	}
	public byte[] returnByteBuffer() throws Exception {
		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
		this.socket.receive(receivedPacket);
		return receivedPacket.getData();
	}
	public Integer getPort(){
		return this.port;
	}
}
