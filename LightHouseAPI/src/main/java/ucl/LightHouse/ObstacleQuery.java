package ucl.LightHouse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

class ObstacleQuery {
	private String _serverIP;
	private int _apiPort;
	
	public ObstacleQuery()
	{
		try {
			_serverIP = PropertiesRetriever.getInstance().getServerIP("config.properties");
			_apiPort = PropertiesRetriever.getInstance().getApiPort("config.properties");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean checkRadius(double radius)
	{
		if(radius > 0 && radius < 100)
			return true;
		
		return false;
	}
	
	private ArrayList<Obstacle> deserialize(String response)
	{
		JSONObject jsonObj = new JSONObject(response);
		
		ArrayList<Obstacle> obstacles = ObstacleDeserializer.deserializeResponse(jsonObj);
		
		return obstacles;
	}
	
	public ArrayList<Obstacle> querySync(final double longitude, final double latitude, final double radius) {
		final List<Obstacle> obstacles = Collections.synchronizedList(new ArrayList<Obstacle>());
		if(!checkRadius(radius))
			return null;
		
		Thread t = new Thread(new Runnable() {
		    public void run() {
				try {
			
					URL url = new URL("http://" + _serverIP + ":" + _apiPort + "/ObstacleAPI/v0_1/Locations/Query?"
							+ "longitude=" + longitude + "&" + "latitude=" + latitude + "&" + "radius=" + radius);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Accept", "application/json");
			
					if (conn.getResponseCode() == 200) {
						String response = IOUtils.toString(conn.getInputStream()); 
						obstacles.addAll(deserialize(response));
						conn.disconnect();
					}
			
				  } catch (MalformedURLException e) {
					  e.printStackTrace();
				  } catch (IOException ex) {
					  ex.printStackTrace();	
				  }
		    }
		});
		
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		return new ArrayList<>(obstacles);
	}
	
	public void queryAsync(final double longitude, final double latitude, final double radius, 
			final Response<ArrayList<Obstacle>> response) {
		
		
		final List<Obstacle> obstacles = Collections.synchronizedList(new ArrayList<Obstacle>());
		if(!checkRadius(radius))
			response.callback(null);
		
		Thread t = new Thread(new Runnable() {
		    public void run() {
				try {
			
					URL url = new URL("http://" + _serverIP + ":" + _apiPort + "/ObstacleAPI/v0_1/Locations/Query?"
							+ "longitude=" + longitude + "&" + "latitude=" + latitude + "&" + "radius=" + radius);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Accept", "application/json");
			
					if (conn.getResponseCode() == 200) {
			
						String getResponse = IOUtils.toString(conn.getInputStream()); 
						obstacles.addAll(deserialize(getResponse));
						conn.disconnect();
						response.callback(new ArrayList<>(obstacles));
					}
			
				  } catch (MalformedURLException e) {
					    e.printStackTrace();
					    response.callback(null);
				  } catch (IOException ex) {
						ex.printStackTrace();
						response.callback(null);
				  }
		    }
		});
		
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
}
