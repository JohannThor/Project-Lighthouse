package ucl.LightHouse;

import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.Test;

import junit.framework.TestCase;

public class ObstacleDeserializerTest extends TestCase {
	
	@Test
    public void testLighthouseJSONObstacleDeserializerMultiple() {
    	String json = "{\"Rows\":[\n{\"ID\":\"a217e852-596a-11e6-8b77-86f30ca893d3\",\n\"Latitude\":\"51.5246\",\n\"Longitude\":\"0.1340\",\n\"Confidence\":\"0.0001\"\n}]}";
    	JSONObject jsonObject = new JSONObject(json);
    	ArrayList<Obstacle> obstacles = ObstacleDeserializer.deserializeResponse(jsonObject);
    	assertTrue(obstacles.size() != 0);
    	assertTrue(obstacles.get(0).getId().equals(UUID.fromString("a217e852-596a-11e6-8b77-86f30ca893d3")));
    	assertTrue(obstacles.get(0).getConfidence() == 0.0001);
    	assertTrue(obstacles.get(0).getLatitude() == 51.5246);
    	assertTrue(obstacles.get(0).getLongitude() == 0.1340);
    }
    
    @Test
    public void testLighthouseJSONObstaclesDeserializerEmpty() {
    	String json = "{\"Rows\":[]}";
    	JSONObject jsonObject = new JSONObject(json);
    	ArrayList<Obstacle> obstacles = ObstacleDeserializer.deserializeResponse(jsonObject);
    	assertTrue(obstacles.size() == 0);
    }
    
    @Test
    public void testLighthouseJSONObstaclesDeserializerNone() {
    	String json = "{}";
    	JSONObject jsonObject = new JSONObject(json);
    	ArrayList<Obstacle> obstacles = ObstacleDeserializer.deserializeResponse(jsonObject);
    	assertTrue(obstacles.size() == 0);
    }
    
    @Test
    public void testLighthouseJSONObstacleDeserializerSingle() {
    	String json = "{\"Rows\":[\n	{\"ID\":\"a217e852-596a-11e6-8b77-86f30ca893d3\",\n	\"Latitude\":\"51.5246\",\n	\"Longitude\":\"0.1340\",\n	\"Confidence\":\"0.0001\"},\n	{\"ID\":\"a317e852-596a-11e6-8b77-86f30ca893d3\",\n	\"Latitude\":\"52.5246\",\n	\"Longitude\":\"0.2340\",\n	\"Confidence\":\"0.0002\"}\n]}";
    	JSONObject jsonObject = new JSONObject(json);
    	ArrayList<Obstacle> obstacles = ObstacleDeserializer.deserializeResponse(jsonObject);
    	assertTrue(obstacles.size() == 2);
    	assertTrue(obstacles.get(0).getId().equals(UUID.fromString("a217e852-596a-11e6-8b77-86f30ca893d3")));
    	assertTrue(obstacles.get(0).getConfidence() == 0.0001);
    	assertTrue(obstacles.get(0).getLatitude() == 51.5246);
    	assertTrue(obstacles.get(0).getLongitude() == 0.1340);
    	assertTrue(obstacles.get(1).getId().equals(UUID.fromString("a317e852-596a-11e6-8b77-86f30ca893d3")));
    	assertTrue(obstacles.get(1).getConfidence() == 0.0002);
    	assertTrue(obstacles.get(1).getLatitude() == 52.5246);
    	assertTrue(obstacles.get(1).getLongitude() == 0.2340);
    }
}
