import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class LighthouseDTO{
	
	private Map<String, String> data = new HashMap<String, String>();
	
	public LighthouseDTO(){}
	public LighthouseDTO(Map<String, String> data) {
		this.data = new HashMap<String, String>(data);
	}
	public JSONObject toJSONObject(){
		final JSONObject obj = new JSONObject();
		this.data.forEach((k, v) -> obj.put(k, v));
		return obj;
	}
	public byte[] toBytes(){
		return this.toJSONObject().toString().getBytes();
	}
	public byte[] toBytes(JSONObject obj){	
		return obj.toString().getBytes();
	}
	public Map<String, String> getData(){		
		return this.data;
	}
}
