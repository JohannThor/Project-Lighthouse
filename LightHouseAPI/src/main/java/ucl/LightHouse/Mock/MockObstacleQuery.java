package ucl.LightHouse.Mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import ucl.LightHouse.Obstacle;
import ucl.LightHouse.Response;
import ucl.LightHouse.Interfaces.IObstacleQuery;

public class MockObstacleQuery implements IObstacleQuery {

	private ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();

	@SuppressWarnings("deprecation")
	public MockObstacleQuery() {
		Obstacle obs = new Obstacle();
		obs.setConfidence(0.1);
		obs.setCreatedDate(new Date(2016,8,1));
		obs.setId(new UUID(1,1));
		// 51.5246° N, 0.1340° W
		obs.setLatitude(51.5246);
		obs.setLongitude(0.1340);
		obs.setUpdatedDate(new Date(2016,8,1));
		obstacleList.add(obs);
		
		
		 obs = new Obstacle();
		obs.setConfidence(0.1);
		obs.setCreatedDate(new Date(2016,8,1));
		obs.setId(new UUID(1,2));
		// 51.5246° N, 0.1340° W
		obs.setLatitude(51.5246);
		obs.setLongitude(0.1340);
		obs.setUpdatedDate(new Date(2016,8,1));
		obstacleList.add(obs);
	}
	
	@Override
	public ArrayList<Obstacle> querySync(double longitude, double latitude, double radius) {
		if(radius == 1) {
			return new ArrayList<Obstacle>(obstacleList.subList(0, 1));
		} else {
			return obstacleList;
		}
	}

	@Override
	public void queryAsync(double longitude, double latitude, double radius, Response<ArrayList<Obstacle>> response) {
		if(radius == 1) {
			response.callback( new ArrayList<Obstacle>(obstacleList.subList(0, 1)));
		} else {
			response.callback(obstacleList);
		}
	}

}
