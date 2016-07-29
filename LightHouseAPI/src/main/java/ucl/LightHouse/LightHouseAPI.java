package ucl.LightHouse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Hello world!
 *
 */
public class LightHouseAPI 
{
	public void sendSensorDataAsync(HashMap<String, String> map, Response<Boolean> response)
	{
		LighthouseDTO dto = new LighthouseDTO(map);
		UdpPacketSender transfer = new UdpPacketSender();
		byte[] bytesToSend = dto.toBytes();
		
		transfer.sendPacketAsync(bytesToSend, response);
	}
	
	public boolean sendSensorDataSync(HashMap<String, String> map)
	{
		LighthouseDTO dto = new LighthouseDTO(map);
		UdpPacketSender transfer = new UdpPacketSender();
		byte[] bytesToSend = dto.toBytes();
		
		//boolean result = transfer.sendPacket(bytesToSend);
		transfer.sendPacketSync(bytesToSend);
		
		return true;
	}
	
	public ArrayList<Obstacle> queryDatabaseSync(double longitude, double latitude, double radius)
	{
		ObstacleQuery query = new ObstacleQuery();
		
		ArrayList<Obstacle> obstacles = query.querySync(longitude, latitude, radius);
		
		return obstacles;
	}
	
	public void queryDatabaseAsync(double longitude, double latitude, double radius, 
			Response<ArrayList<Obstacle>> response)
	{
		ObstacleQuery query = new ObstacleQuery();
		
		query.queryAsync(longitude, latitude, radius, response);
	}
}
