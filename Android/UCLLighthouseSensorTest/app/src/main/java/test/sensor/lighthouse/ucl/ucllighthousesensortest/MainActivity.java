package test.sensor.lighthouse.ucl.ucllighthousesensortest;

import android.Manifest;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;


public class MainActivity extends AppCompatActivity implements SensorEventListener,IALocationListener, IARegion.Listener {
    private final int CODE_PERMISSIONS = 0;
    private IALocationManager mIALocationManager;
    TextView feedback;
    TextView gyro;
    TextView magnetic;
    TextView accell;
    TextView grav;
    TextView stat;
    Sensor magneticSensor; Sensor gyroSensor; Sensor accellSensor; Sensor gravSensor;
    private SensorManager mSensorManager;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getName().contains("Gyro")) {
            gyro.setText(event.values[0] + ":" + event.values[1] + ":" + event.values[2]);
        }
        else if(event.sensor.getName().contains("Accel")) {
            accell.setText(event.values[0] + ":" + event.values[1] + ":" + event.values[2]);
        }
        else if(event.sensor.getName().contains("Grav")) {
            grav.setText(event.values[0] + ":" + event.values[1] + ":" + event.values[2]);
        }
        else if(event.sensor.getName().contains("Magnet")) {
            magnetic.setText(event.values[0] + ":" + event.values[1] + ":" + event.values[2]);
        }
        else{
            feedback.setText(event.sensor.getName());
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accellSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        feedback = (TextView) findViewById(R.id.feedback);
        gyro = (TextView) findViewById(R.id.gyro);
        magnetic = (TextView) findViewById(R.id.magnetic);
        accell = (TextView) findViewById(R.id.accell);
        grav = (TextView) findViewById(R.id.grav);
        stat = (TextView) findViewById(R.id.status);

        String[] neededPermissions = {
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(this, neededPermissions, CODE_PERMISSIONS);
        mIALocationManager = IALocationManager.create(this);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        int sensorDelay = 500000;
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), this);
        mSensorManager.registerListener(this, accellSensor, sensorDelay,sensorDelay);
        mSensorManager.registerListener(this, gravSensor, sensorDelay, sensorDelay);
        mSensorManager.registerListener(this, gyroSensor, sensorDelay, sensorDelay);
        mSensorManager.registerListener(this, magneticSensor, sensorDelay, sensorDelay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIALocationManager.removeLocationUpdates(this);
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        feedback.setText(activeNetworkInfo != null && activeNetworkInfo.isConnected() ? "INTERNET!!!" :" :*( no internet");
    }

    @Override
    protected void onDestroy() {
        mIALocationManager.destroy();
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(IALocation iaLocation) {
        feedback.setText("Latitude: " + iaLocation.getLatitude() + "  Longitude: " + iaLocation.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case IALocationManager.STATUS_CALIBRATION_CHANGED:
                String quality = "unknown";
                switch (extras.getInt("quality")) {
                    case IALocationManager.CALIBRATION_POOR:
                        quality = "Poor";
                        break;
                    case IALocationManager.CALIBRATION_GOOD:
                        quality = "Good";
                        break;
                    case IALocationManager.CALIBRATION_EXCELLENT:
                        quality = "Excellent";
                        break;
                }
                stat.setText("Calibration change. Quality: " + quality);
                break;
            case IALocationManager.STATUS_AVAILABLE:
                stat.setText("onStatusChanged: Available");
                break;
            case IALocationManager.STATUS_LIMITED:
                stat.setText("onStatusChanged: Limited");
                break;
            case IALocationManager.STATUS_OUT_OF_SERVICE:
                stat.setText("onStatusChanged: Out of service");
                break;
            case IALocationManager.STATUS_TEMPORARILY_UNAVAILABLE:
                stat.setText("onStatusChanged: Temporarily unavailable");
        }
    }
    @Override
    public void onEnterRegion(IARegion iaRegion) {
        stat.setText("Entered region: " + iaRegion.getId());
    }
    @Override
    public void onExitRegion(IARegion iaRegion) {
        stat.setText("Exited region: " + iaRegion.getId());
    }
}
