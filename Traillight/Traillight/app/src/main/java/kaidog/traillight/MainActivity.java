package kaidog.traillight;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class MainActivity extends ActionBarActivity implements SensorEventListener, LocationListener {

    private static final String TAG = "MainActivity";
    private static final boolean VERBOSE = true;

    android.hardware.Camera cam;
    android.hardware.Camera.Parameters p;

    private ImageView image;

    private float currentDegree = 0f;

    private SensorManager mSensorManager;
    private SensorManager pSensorManager;

    private LocationManager locationManager;

    TextView tvHeading;

    TextView tvLat;
    TextView tvLon;
    TextView tvAlt;
    TextView tvPre;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //cam = android.hardware.Camera.open();
        //p = cam.getParameters();

        image = (ImageView) findViewById(R.id.compass);

        tvHeading = (TextView) findViewById(R.id.tvHeading);
        tvLat = (TextView) findViewById(R.id.tvLat);
        tvLon = (TextView) findViewById(R.id.tvLon);
        tvAlt = (TextView) findViewById(R.id.tvAlt);
        tvPre = (TextView) findViewById(R.id.tvPre);


        // initialize android device sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        pSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //locationManager.requestLocationUpdates();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (VERBOSE) Log.v(TAG, "++ ON START ++");

        //tvGPS.setText("aaaaa");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (VERBOSE) Log.v(TAG, "+ ON RESUME +");

        cam = android.hardware.Camera.open();
        p = cam.getParameters();

        mSensorManager.registerListener(
                this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME
        );

        pSensorManager.registerListener(
                this,
                pSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_GAME
        );

        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, this);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (VERBOSE) Log.v(TAG, "- ON PAUSE -");

        //stop listener to save battery
        mSensorManager.unregisterListener(this);
        cam.release();
        //cam.unlock();

        locationManager.removeUpdates(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (VERBOSE) Log.v(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (VERBOSE) Log.v(TAG, "- ON DESTROY -");
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


    public void powerToggle(View view)
    {
        if( ((ToggleButton) view).isChecked() )
        {
            //cam = android.hardware.Camera.open();
            //p = cam.getParameters();
            p.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);
            cam.startPreview();
        }
        else
        {

            //cam.release();
            p.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
            cam.setParameters(p);
            cam.stopPreview();
            // cam.startPreview();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {

        //if (VERBOSE) Log.v(TAG, "- SENSOR CHANGED -");
        Sensor sensor = event.sensor;

        if(sensor.getType() == Sensor.TYPE_ORIENTATION) {

            // get the angle around the z-axis rotated
            float degree = Math.round(event.values[0]);

            tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

            // create rotation animation
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );

            // how long the animation will take place in ms
            ra.setDuration(210);

            // set the animation after the end of the reservation status
            ra.setFillAfter(true);

            //start the animation
            image.startAnimation(ra);
            currentDegree = -degree;
        }
        else if (sensor.getType() == Sensor.TYPE_PRESSURE)
        {
            float pressure = event.values[0];

            //in meters
            //TODO get rid of 1024 hardcode
            float altitudeM = pSensorManager.getAltitude( (float) 1024, pressure );

            //convert to feet
            float altitudeFt = altitudeM * (float) 3.28;

            tvAlt.setText("Altitude: " + Float.toString(altitudeFt) + " ft" );
            //tvAlt.setText("Altitude: " + Float.toString(altitudeM) + " M" );
            //tvAlt.setText("Pressure: " + Float.toString(pressure));

            tvPre.setText("Pressure: " + Float.toString(pressure) + " hPA");
        }
    }

    /*
    @override
    public void onAccuracyChanged(Sensor sensor)
    {
        //not used
    }
    */

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        //not used
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (VERBOSE) Log.v(TAG, "-- ON LOCATION CHANGED --");

        //tvGPS.setText("Lat: " + Double.toString(location.getLatitude()) + " Lon: " + Double.toString(location.getLongitude()) );
        tvLat.setText("Latitude: " + Double.toString( location.getLatitude() ) + "º");
        tvLon.setText("Longitude: " + Double.toString( location.getLongitude() ) + "º");


    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }


}
