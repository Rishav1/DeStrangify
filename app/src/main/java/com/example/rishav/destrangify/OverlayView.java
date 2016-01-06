package com.example.rishav.destrangify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;

public class OverlayView extends View implements SensorEventListener {

    public static final String DEBUG_TAG = "OverlayView Log";

    private final Context context;
    private Handler handler;

    // Mount Washington, NH: 44.27179, -71.3039, 6288 ft (highest peak
    private  Location inputLocation = new Location("manual");
    {
        inputLocation.setLatitude(23.806005);
        inputLocation.setLongitude(86.433726);
        inputLocation.setAltitude(0d);
    }

    String accelData = "Accelerometer Data";
    String compassData = "Compass Data";
    String gyroData = "Gyro Data";

    private LocationManager locationManager = null;
    private SensorManager sensors = null;

    private Location lastLocation;
    private float[] lastAccelerometer;
    private float[] lastCompass;

    private float verticalFOV;
    private float horizontalFOV;

    private boolean isAccelAvailable;
    private boolean isCompassAvailable;
    private boolean isGyroAvailable;
    private Sensor accelSensor;
    private Sensor compassSensor;
    private Sensor gyroSensor;

    private TextPaint contentPaint;

    private Paint targetPaint;

    private People currentuser;
    private People inputuser;

    private float[] orientation = new float[3];

    public OverlayView(Context context, People input,float vFOV, float hFOV) {
        super(context);

        {
            inputLocation.setLatitude(input.getCurLoc().getLatitude());
            inputLocation.setLongitude(input.getCurLoc().getLongitude());
            inputLocation.setAltitude(0d);        //TODO add altitude in the online database
        }
        currentuser=(People)People.getCurrentUser();
        inputuser = input;
        this.context = context;
        this.handler = new Handler();
        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        sensors = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        startSensors();



        verticalFOV = vFOV;
        horizontalFOV = hFOV;

        // paint for text
        contentPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setTextAlign(Align.LEFT);
        contentPaint.setTextSize(35);
        contentPaint.setColor(Color.RED);

        // paint for target

        targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetPaint.setColor(Color.GREEN);
        targetPaint.setTextSize(35);



    }

    private void startSensors() {
        isAccelAvailable = sensors.registerListener(this, accelSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        isCompassAvailable = sensors.registerListener(this, compassSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        isGyroAvailable = sensors.registerListener(this, gyroSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //Log.d(DEBUG_TAG, "onDraw");
        super.onDraw(canvas);

        // Draw something fixed (for now) over the camera view


        float curBearingToMW = 0.0f;

        StringBuilder text = new StringBuilder(accelData).append("\n");
        text.append(compassData).append("\n");
        text.append(gyroData).append("\n");
        lastLocation = new Location("last location");
        if (currentuser!= null){
            lastLocation.setLatitude(currentuser.getCurLoc().getLatitude());
            lastLocation.setLongitude(currentuser.getCurLoc().getLongitude());
            lastLocation.setAltitude(0d);
            //Log.d("currentuser location", currentuser.getCurLoc().toString());
        }

        if (lastLocation != null) {
            text.append(
                    String.format("GPS = (%.3f, %.3f) @ (%.2f meters up)",
                            lastLocation.getLatitude(),
                            lastLocation.getLongitude(),
                            lastLocation.getAltitude())).append("\n");

            curBearingToMW = lastLocation.bearingTo(inputLocation);

            //Log.d("lastlocation",lastLocation.getLatitude()+","+lastLocation.getLongitude());
            text.append(String.format("Bearing to " + inputuser.getUsername() + ": %f", curBearingToMW))
                    .append("\n");
        }

        // compute rotation matrix
        float rotation[] = new float[9];
        float identity[] = new float[9];
        if (lastAccelerometer != null && lastCompass != null) {
            boolean gotRotation = SensorManager.getRotationMatrix(rotation,
                    identity, lastAccelerometer, lastCompass);
            if (gotRotation) {
                float cameraRotation[] = new float[9];
                // remap such that the camera is pointing straight down the Y
                // axis
                SensorManager.remapCoordinateSystem(rotation,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z,
                        cameraRotation);

                // orientation vector
                float inp_orientation[] = new float[3];
                SensorManager.getOrientation(cameraRotation, inp_orientation);

                orientation = lowPass(inp_orientation,orientation);

                text.append(
                        String.format("Orientation (%.3f, %.3f, %.3f)",
                                Math.toDegrees(orientation[0]), Math.toDegrees(orientation[1]), Math.toDegrees(orientation[2])))
                        .append("\n");

                // draw horizon line (a nice sanity check piece) and the target (if it's on the screen)
                canvas.save();
                // use roll for screen rotation
                canvas.rotate((float)(0.0f- Math.toDegrees(orientation[2])));

                // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
                float dx = (float) ( (canvas.getWidth()/ horizontalFOV) * (Math.toDegrees(orientation[0])-curBearingToMW));
                float dy = (float) ( (canvas.getHeight()/ verticalFOV) * Math.toDegrees(orientation[1])) ;

                text.append(String.format("Co ordinates (%f, %f)",dx,dy)).append("\n");

                // wait to translate the dx so the horizon doesn't get pushed off
                canvas.translate(0.0f, 0.0f - dy);


                // make our line big enough to draw regardless of rotation and translation
                //canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight()/2, canvas.getWidth()+canvas.getHeight(), canvas.getHeight()/2, targetPaint);

                // now translate the dx
                canvas.translate(0.0f - dx, 0.0f);

                // draw our point -- we've rotated and translated this to the right spot already
                canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 8.0f, contentPaint);
                canvas.drawText(inputuser.getUsername()+" ("+currentuser.getCurLoc().distanceInKilometersTo(inputuser.getCurLoc())+" m)",canvas.getWidth()/2,canvas.getHeight()/2,targetPaint);

                canvas.restore();

            }
        }

        canvas.save();
        canvas.translate(15.0f, 15.0f);
        StaticLayout textBox = new StaticLayout(text.toString(), contentPaint,
                480, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
        textBox.draw(canvas);
        canvas.restore();
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        Log.d(DEBUG_TAG, "onAccuracyChanged");

    }

    public void onSensorChanged(SensorEvent event) {
        // Log.d(DEBUG_TAG, "onSensorChanged");

        StringBuilder msg = new StringBuilder(event.sensor.getName())
                .append(" ");
        for (float value : event.values) {
            msg.append("[").append(String.format("%.3f", value)).append("]");
        }

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                lastAccelerometer = event.values.clone();
                accelData = msg.toString();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroData = msg.toString();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lastCompass = event.values.clone();
                compassData = msg.toString();
                break;
        }

        this.invalidate();
    }


    // this is not an override
    public void onPause() {
        sensors.unregisterListener(this);
    }

    // this is not an override
    public void onResume() {
        startSensors();
    }

    static final float ALPHA = 0.15f;
    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
