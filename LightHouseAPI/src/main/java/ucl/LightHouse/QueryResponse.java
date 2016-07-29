package ucl.LightHouse;

import java.util.ArrayList;

public class QueryResponse extends Response<ArrayList<Obstacle>> {

	@Override
	public void callback(ArrayList<Obstacle> parameter) {
		for(int i = 0; i < parameter.size(); i++)
		{
			System.out.println(parameter.get(i).toString());
		}		
	}
}