package ucl.LightHouse.Mock;

import java.util.ArrayList;

import ucl.LightHouse.Response;
import ucl.LightHouse.Interfaces.IPacketSender;

public class MockPacketSender implements IPacketSender {

	ArrayList<byte[]> receivedPackets = new ArrayList<byte[]>();
	
	public ArrayList<byte[]> receivedPackets() {
		return receivedPackets;
	}
	
	@Override
	public void sendPacketAsync(byte[] data, Response<Boolean> response) {
		receivedPackets.add(data);
	}

	@Override
	public boolean sendPacketSync(byte[] data) {
		receivedPackets.add(data);
		return true;
	}

}
