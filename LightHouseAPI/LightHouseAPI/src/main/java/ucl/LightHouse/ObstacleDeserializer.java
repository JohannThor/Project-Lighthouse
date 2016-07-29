package ucl.LightHouse;

import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

class ObstacleDeserializer {
	
	public static ArrayList<Obstacle> deserializeResponse(JSONObject jsonObj)
	{
		ArrayList<Obstacle> deserializedObjects = new ArrayList<Obstacle>();
		
		JSONArray jsonArray = jsonObj.getJSONArray("Rows");
		
		for (int i = 0, size = jsonArray.length(); i < size; i++)
	    {
	      JSONObject objectInArray = jsonArray.getJSONObject(i);

	      Obstacle newObs = new Obstacle();
	      newObs.setId(UUID.fromString((String)objectInArray.get("ID")));
	      newObs.setLatitude(Double.parseDouble((String)objectInArray.get("Latitude")));
	      newObs.setLongitude(Double.parseDouble((String)objectInArray.get("Longitude")));
	      newObs.setConfidence(Double.parseDouble((String)objectInArray.get("Confidence")));
	      
	      deserializedObjects.add(newObs);
	    }

		return deserializedObjects;
	}
}
