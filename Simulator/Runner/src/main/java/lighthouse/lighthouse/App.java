package lighthouse.lighthouse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import ucl.LightHouse.LightHouseAPI;
import ucl.LightHouse.Response;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main( String[] args )
    {
		
    	if(args.length < 1) {
    		System.out.println("Please specify simulation directory.");
    		System.exit(1);
    	}
    	ArrayList<SensorFeed> feeds = new ArrayList<SensorFeed>();
    	
    	try {
			Files.walk(Paths.get(args[0])).forEach(filePath -> {
				SensorFeed feed = new SensorFeed();
				feed.feed = new ArrayList<SensorReading>();
				feed.name = filePath.getFileName().toString();
			    if (Files.isRegularFile(filePath)) {
			        BufferedReader reader = null;
					try {
						reader = new BufferedReader(new FileReader(filePath.toFile()));
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			        String line = null;
					try {
						line = reader.readLine();
					} catch (IOException e) {
						System.out.println("Could not read first line");
						System.exit(1);
					}
			    	while(line != null) {
						String[] parts = line.split(",");
						Double tick = Double.parseDouble(parts[0]);
						Double value = null;
						if(parts.length < 2) {
							value = null;
						} else {
							value = Double.parseDouble(parts[1]);
						}
						SensorReading reading = new SensorReading(tick,value);
						feed.feed.add(reading);
			    		try {
							line = reader.readLine();
						} catch (IOException e) {
							System.out.println("Could not read line");
							System.exit(1);						
						}
			    	}
			    	feeds.add(feed);
			    }
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
    	KalmanFilter(feeds);
    	//SendToKafka(feeds);
    }
	
	private void SendToKafka(ArrayList<SensorFeed> feeds) {
    	long startTime = System.nanoTime();

    	LightHouseAPI api = new LightHouseAPI();
    	for(int i = 0; i < feeds.get(0).feed.size(); i++) {
	    	HashMap<String, String> map = new HashMap<String,String>();
	    	map.put("sensor_type", "TestLightHouse");
	    	map.put("ID", "4ee831f4-3da1-4e81-895d-f219ab1c4c35");
    		for(SensorFeed feed : feeds) {
    	    	SensorReading reading = feed.feed.get(i);
    	    	if(reading == null) {
    	    		map.put(feed.name, "0.0");
    	    	}
    	    	else {
        	    	map.put(feed.name, reading.sensorValue == null ? "0.0" : reading.sensorValue.toString());
    	    	}
    		}
			api.sendSensorDataAsync(map,new AsyncResponse());
    	}
    	long stopTime = System.nanoTime();
    	System.out.println("Sending time: ");
    	System.out.println(stopTime - startTime);
    	System.out.println("DONE!!!!");
	}
	
	private static void KalmanFilter(ArrayList<SensorFeed> feeds) {
		final double dt = 0.001;
        
		 final RealMatrix A = MatrixUtils.createRealMatrix(new double[][] {
			 {1,0,0,0},
			 {0,1,0,0 },
			 {0,0, 1, 0 },
            { 0,0,dt,  1 } 
		 });
		
		 // The control vector, which adds acceleration to the kinematic equations.
		 // 0          =>  x(n+1) =  x(n+1)
		 // 0          => vx(n+1) = vx(n+1)
		 // -9.81*dt^2 =>  y(n+1) =  y(n+1) - 1/2 * 9.81 * dt^2
		 // -9.81*dt   => vy(n+1) = vy(n+1) - 9.81 * dt
		 final RealVector controlVector =
		         MatrixUtils.createRealVector(new double[] { 0.0 , 0.0 , 0.0 , 0.0 } );
		
		 // The control matrix B only update y and vy, see control vector
		 final RealMatrix B = MatrixUtils.createRealMatrix(new double[][] {
			 	{ 1,0,0,0},
			 	{0,1,0,0},
			 	{0,0,1,0},
			 	{0,0,0,1}
		 });
		
		 // After state transition and control, here are the equations:
		 //
		 //  x(n+1) = x(n) + vx(n)
		 // vx(n+1) = vx(n)
		 //  y(n+1) = y(n) + vy(n) - 0.5 * 9.81 * dt^2
		 // vy(n+1) = vy(n) + -9.81 * dt
		 //
		 // Which, if you recall, are the equations of motion for a parabola.
		
		 // We only observe the x/y position of the cannonball
		 final RealMatrix H = MatrixUtils.createRealMatrix(new double[][] {
		         { 1,0,0, 0 },
		         { 0, 1, 0, 0 },
		         { 0, 0, 1, 0 },
		         { 0, 0, 0, 1 }

		 });
		 
		 // This is our guess of the initial state.  I intentionally set the Y value
		 // wrong to illustrate how fast the Kalman filter will pick up on that.
		
		 final RealVector initialState = MatrixUtils.createRealVector(new double[] { 0, 0, 0, 0 } );
		
		 // The initial error covariance matrix, the variance = noise^2
		 //final double var = measurementNoise * measurementNoise;
		 final RealMatrix initialErrorCovariance = MatrixUtils.createRealMatrix(new double[][] {
		         {   0,    0,0,0},
		         {   0,    0,0,0},
		         {0,0,0,0},
		         {0,0,0,0}
		 });
		
		 // we assume no process noise -> zero matrix
		 final RealMatrix Q = MatrixUtils.createRealMatrix(4, 4);
		 
		 // the measurement covariance matrix
		 final RealMatrix R = MatrixUtils.createRealMatrix(new double[][] {

		         { 1,  0,  0,  0 },
		         { 0,  1,  0,  0 },
		         { 0,  0,  1,  0 },
		         { 0,  0,  0,  1 },
		 });
		
		 final ProcessModel pm = new DefaultProcessModel(A, B, Q, initialState, initialErrorCovariance);
		 final MeasurementModel mm = new DefaultMeasurementModel(H, R);
		 final KalmanFilter filter = new KalmanFilter(pm, mm);	
		 
        final List<Number> realX = new ArrayList<Number>();
        final List<Number> realY = new ArrayList<Number>();
        final List<Number> measuredX1 = new ArrayList<Number>();
        final List<Number> measuredY1 = new ArrayList<Number>();
        final List<Number> measuredX2 = new ArrayList<Number>();
        final List<Number> measuredY2 = new ArrayList<Number>();
        final List<Number> measuredX3 = new ArrayList<Number>();
        final List<Number> measuredY3 = new ArrayList<Number>();
        final List<Number> kalmanX = new ArrayList<Number>();
        final List<Number> kalmanY = new ArrayList<Number>();
		 
		 
		 for(int i = 0; i<feeds.get(0).feed.size();i++ ) {
			 SensorFeed m1 = feeds.get(0),m2 = feeds.get(1),m3 = feeds.get(2);
	         final double[] state = filter.getStateEstimation();
	         final double time = m1.feed.get(i).tickTime;
	         
	         Double v1 = m1.feed.get(i).sensorValue == null ? 0.0 : m1.feed.get(i).sensorValue;
	         Double v2 = m2.feed.get(i).sensorValue == null ? 0.0 : m2.feed.get(i).sensorValue;
	         Double v3 = m3.feed.get(i).sensorValue == null ? 0.0 : m3.feed.get(i).sensorValue;
	         
	         kalmanX.add(time);
	         kalmanY.add(state[0]);
	         
	         measuredX1.add(time);
	         measuredY1.add(v1);
	         measuredX2.add(time);
	         measuredY2.add(v2);
	         measuredX3.add(time);
	         measuredY3.add(v3);
	         
			 filter.predict(new double[] {v1,v2,v3,0.0});
			 filter.correct(new double[] {(v1 + v2 + v3)/3,0.0,0,0});
		 }
		
	  
	    // Create Chart
	    XYChart chart = new XYChart(4000, 500);
	    chart.setTitle("Sample Chart");
	    chart.setXAxisTitle("X");
	    chart.setYAxisTitle("Y");
	    //XYSeries realSeries = chart.addSeries("Real", realX, realY);
	    XYSeries measuredSeries1 = chart.addSeries("Measured1", measuredX1, measuredY1);
	    XYSeries measuredSeries2 = chart.addSeries("Measured2", measuredX2, measuredY2);
	    XYSeries measuredSeries3 = chart.addSeries("Measured3", measuredX3, measuredY3);
	    XYSeries kalmanSeries = chart.addSeries("Kalman", kalmanX, kalmanY);
	    measuredSeries1.setMarker(SeriesMarkers.CIRCLE);
	    measuredSeries2.setMarker(SeriesMarkers.DIAMOND);
	    measuredSeries3.setMarker(SeriesMarkers.SQUARE);
	    kalmanSeries.setMarker(SeriesMarkers.TRIANGLE_DOWN);

	    try {
			BitmapEncoder.saveBitmap(chart, "/Development/chartBitmap", BitmapFormat.PNG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    System.out.println("DONE!!");
	}
}
