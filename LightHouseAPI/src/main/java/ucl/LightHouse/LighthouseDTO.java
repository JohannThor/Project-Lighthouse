package ucl.LightHouse;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

class LighthouseDTO{
    
    private Map<String, String> data = new HashMap<String, String>();
    
    public LighthouseDTO(){
        
    }
    
    public LighthouseDTO(Map<String, String> data) {
    	Iterator it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            this.data.put(pair.getKey().toString(), pair.getValue().toString());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
    
    public JSONObject toJSONObject(){
        JSONObject obj = new JSONObject();
        Iterator<?> it = this.data.entrySet().iterator();
        
        while(it.hasNext()){
            Map.Entry<String, String> pair = (Map.Entry<String, String>)it.next();
            obj.put(pair.getKey(), pair.getValue());
        }    
        return obj;
    }
    
    public byte[] toBytes(){
        JSONObject obj = this.toJSONObject();
        
        return obj.toString().getBytes();
    }
    
    public byte[] toBytes(JSONObject obj){
        
        return obj.toString().getBytes();
    }
    
    public Map<String, String> getData(){        
        return this.data;
    }
}