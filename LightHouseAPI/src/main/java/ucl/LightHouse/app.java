package ucl.LightHouse;

import java.util.ArrayList;
import java.util.HashMap;

public class app {
	public static void main( String[] args ){
		LightHouseAPI api = new LightHouseAPI();
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put("ID", "Broke your program");
		boolean result = api.sendSensorDataSync(map);
		
		//ArrayList<Obstacle> obs = api.queryDatabaseSync(-0.131, 51.522, 10);
		
		api.queryDatabaseAsync(-0.131, 51.522, 10, new QueryResponse());
		
//		for(int i = 0; i < obs.size(); i++)
//		{
//			System.out.println(obs.get(i).toString());
//		}		
	}
	
	
}
