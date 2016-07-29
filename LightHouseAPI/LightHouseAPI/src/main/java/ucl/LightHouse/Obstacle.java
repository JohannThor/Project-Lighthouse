package ucl.LightHouse;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Obstacle implements Serializable
{
	private static final long serialVersionUID = 1L;
	private UUID _id;
	private double _latitude;
	private double _longitude;
	private Date _createdDate;
	private Date _updatedDate;
	private double _confidence;
	
	public Obstacle(){ }
	
	public UUID getId() { return _id; }
    public void setId(UUID id) { _id = id; }
    
    public double getLatitude() { return _latitude; }
    public void setLatitude(double latitude) { _latitude = latitude; }
    
    public double getLongitude() { return _longitude; }
    public void setLongitude(double longitude) { _longitude = longitude; }
    
    public Date getCreatedDate() { return _createdDate; }
    public void setCreatedDate(Date createdDate) { _createdDate = createdDate; }
    
    public Date getUpdatedDate() { return _updatedDate; }
    public void setUpdatedDate(Date updatedDate) { _updatedDate = updatedDate; }
    
    public double getConfidence() { return _confidence; }
    public void setConfidence(double confidence) { _confidence = confidence; }
    
    @Override
    public String toString() {
        return MessageFormat.format("Obstacle'{'id={0}, latitute={1}, longitude={2}, confidence={3}'}'", 
        		_id, _latitude, _longitude, _confidence);
    }
    
    public static List<String> columns() {
        List<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("latitude");
        columns.add("longitude");
        columns.add("confidence");
        columns.add("created_date");
        columns.add("updated_date");
        return columns;
}
}