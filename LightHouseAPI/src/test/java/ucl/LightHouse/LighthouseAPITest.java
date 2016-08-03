package ucl.LightHouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import ucl.LightHouse.LightHouseAPI;
import ucl.LightHouse.Obstacle;
import ucl.LightHouse.ObstacleDeserializer;
import ucl.LightHouse.Response;
import ucl.LightHouse.Interfaces.IPacketSender;
import ucl.LightHouse.Mock.MockObstacleQuery;
import ucl.LightHouse.Mock.MockPacketSender;

public class LighthouseAPITest 
{    
    private class MockResponseSingleObstacle extends Response<ArrayList<Obstacle>> {
		@Override
		public void callback(ArrayList<Obstacle> parameter) {
			assertTrue("Single Async Obstacle",parameter.size() == 1 && parameter.get(0).getId().equals(new UUID(1,1)));
		}
    }
    
    private class MockResponseMultipleObstacle extends Response<ArrayList<Obstacle>> {
		@Override
		public void callback(ArrayList<Obstacle> parameter) {
			assertTrue("Multiple Async Obstacles",parameter.size() == 2 && parameter.get(0).getId().equals(new UUID(1,1)) && parameter.get(1).getId().equals(new UUID(1,2)));
		}
    }
    
    @Test
    public void testLighthouseAPIQueryAsync() {
        LightHouseAPI api = new LightHouseAPI(new MockPacketSender(),new MockObstacleQuery());
        api.queryDatabaseAsync(0.0, 0.0, 1, new MockResponseSingleObstacle());
        api.queryDatabaseAsync(0.0, 0.0, 2, new MockResponseMultipleObstacle());
    }
    
    @Test
    public void testLighthouseAPIQuerySync()
    {
        LightHouseAPI api = new LightHouseAPI(new MockPacketSender(),new MockObstacleQuery());
        ArrayList<Obstacle> out1 = api.queryDatabaseSync(0.0, 0.0, 1);
        assertTrue(out1.size() == 1);
        assertTrue(out1.size() == 1 && out1.get(0).getId().equals(new UUID(1,1)));
        
        api = new LightHouseAPI(new MockPacketSender(),new MockObstacleQuery());
        ArrayList<Obstacle> out2 = api.queryDatabaseSync(0.0, 0.0, 2);
        assertTrue("Multiple Sync Obstacle Size",out2.size() == 2);
        assertTrue("Multiple Sync Obstacle Content",out2.size() == 2 && out2.get(0).getId().equals(new UUID(1,1)) && out2.get(1).getId().equals(new UUID(1,2)));      
        
    }
    
    @Test
    public void testLighthouseAPISendDataSync() {
    	MockPacketSender sender = new MockPacketSender();
    	LightHouseAPI api = new LightHouseAPI(sender,new MockObstacleQuery());
    	HashMap<String,String> map = new HashMap<String,String>();
    	map.put("TestKey1", "TestValue1");
    	Boolean out1 = api.sendSensorDataSync(map);
    	byte[] bytes1 = {123, 34, 84, 101, 115, 116, 75, 101, 121, 49, 34, 58, 34, 84, 101, 115, 116, 86, 97, 108, 117, 101, 49, 34, 125};
    	assertTrue("Single Send Data Sync",Arrays.equals(sender.receivedPackets().get(0),bytes1));
    	
    	sender = new MockPacketSender();
    	map = new HashMap<String,String>();
    	map.put("TestKey1","TestValue1");
    	map.put("TestKey2","TestValue2");
    	map.put("TestKey3","TestValue3");
    	byte[] bytes2 = {123, 34, 84, 101, 115, 116, 75, 101, 121, 51, 34, 58, 34, 84, 101, 115, 116, 86, 97, 108, 117, 101, 51, 34, 44, 34, 84, 101, 115, 116, 75, 101, 121, 50, 34, 58, 34, 84, 101, 115, 116, 86, 97, 108, 117, 101, 50, 34, 44, 34, 84, 101, 115, 116, 75, 101, 121, 49, 34, 58, 34, 84, 101, 115, 116, 86, 97, 108, 117, 101, 49, 34, 125};
    	api = new LightHouseAPI(sender,new MockObstacleQuery());
    	Boolean out2 = api.sendSensorDataSync(map);
    	assertTrue("Multiple Send Data Sync",Arrays.equals(sender.receivedPackets().get(0),bytes2));
    }
    
    private class MockResponseSend extends Response<Boolean> {
		@Override
		public void callback(Boolean parameter) {
		}
    }    
    
    @Test
    public void testLighthouseAPISendDataAsync() {
    	
    	MockPacketSender sender = new MockPacketSender();
    	LightHouseAPI api = new LightHouseAPI(sender,new MockObstacleQuery());
    	HashMap<String,String> map = new HashMap<String,String>();
    	map.put("TestKey1", "TestValue1");
    	api.sendSensorDataAsync(map,new MockResponseSend());
    	byte[] bytes1 = {123, 34, 84, 101, 115, 116, 75, 101, 121, 49, 34, 58, 34, 84, 101, 115, 116, 86, 97, 108, 117, 101, 49, 34, 125};
    	assertTrue("Single Send Data Async",Arrays.equals(sender.receivedPackets().get(0),bytes1));
    	
    	sender = new MockPacketSender();
    	api = new LightHouseAPI(sender,new MockObstacleQuery());
    	map = new HashMap<String,String>();
    	map.put("TestKey1", "TestValue1");
    	map.put("TestKey2", "TestValue2");
    	map.put("TestKey3", "TestValue3");
    	api.sendSensorDataAsync(map,new MockResponseSend());
    	byte[] bytes2 = {123, 34, 84, 101, 115, 116, 75, 101, 121, 51, 34, 58, 34, 84, 101, 115, 116, 86, 97, 108, 117, 101, 51, 34, 44, 34, 84, 101, 115, 116, 75, 101, 121, 50, 34, 58, 34, 84, 101, 115, 116, 86, 97, 108, 117, 101, 50, 34, 44, 34, 84, 101, 115, 116, 75, 101, 121, 49, 34, 58, 34, 84, 101, 115, 116, 86, 97, 108, 117, 101, 49, 34, 125};
    	assertTrue("Multiple Send Data Async",Arrays.equals(sender.receivedPackets().get(0),bytes2));
    }
}
