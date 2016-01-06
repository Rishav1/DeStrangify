package com.example.rishav.destrangify;

import android.app.Activity;
import android.graphics.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.Parse;

import java.util.List;

public class ArCameraActivity extends Activity {
    private OverlayView arContent;
    private People currentuser;
    private boolean camera_update_running = false;
    private boolean querystarted = false;

    private FrameLayout arViewPane;
    private FrameLayout arCameraPane;

    private float vFOV;
    private float hFOV;


    private class CameraUpdate extends Thread {
        public void run() {
            while (camera_update_running) {
                if (Thread.interrupted()) {
                    Log.d("Info", "Camera Update Interrupted");
                    break;
                }
                if (!querystarted) {
                    querystarted = true;
                    ParseQuery<People> nearbyPeople = ParseQuery.getQuery(People.class);
                    nearbyPeople.whereWithinKilometers("curLoc", currentuser.getCurLoc(), DestrangifyApplication.getMaxRadius());
                    nearbyPeople.findInBackground(new FindCallback<People>() {
                        @Override
                        public void done(List<People> objects, ParseException e) {
                            if (e != null) {
                                Log.d("Error", e.getStackTrace().toString());
                            }
                            else {
                                arViewPane.removeAllViews();
                                //ArDisplayView arDisplay = new ArDisplayView(getApplicationContext(), mActivity);
                                //arViewPane.addView(arDisplay);
                                for (People nearby_person : objects) {
                                    if (nearby_person != currentuser && nearby_person.isLocVis()) {
                                        arContent = new OverlayView(getApplicationContext(), nearby_person, vFOV,hFOV);
                                        arViewPane.addView(arContent);
                                    }
                                }
                                querystarted = false;
                            }
                        }
                    });
                    Log.d("Info", "Implementing ar_camera_view");
                } else {
                    Log.d("Info", "Camera Update Skipped");
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private CameraUpdate cameraUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_camera);

        currentuser=(People)People.getCurrentUser();

        android.hardware.Camera mCamera = android.hardware.Camera.open();
        vFOV = mCamera.getParameters().getVerticalViewAngle();
        hFOV = mCamera.getParameters().getHorizontalViewAngle();
        mCamera.release();

        arCameraPane = (FrameLayout) findViewById(R.id.ar_camera_pane);
        ArDisplayView arDisplay= new ArDisplayView(getApplicationContext(),this);
        arCameraPane.addView(arDisplay);
        arViewPane = (FrameLayout) findViewById(R.id.ar_view_pane);

    }

    @Override
    protected void onPause() {
    //    arContent.onPause();
        stopCameraUpdate();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    //    arContent.onResume();
        startCameraUpdate();
    }

    void startCameraUpdate() {
        camera_update_running = true;
        cameraUpdater = new CameraUpdate();
        cameraUpdater.start();
    }

    void stopCameraUpdate() {
        camera_update_running = false;
        cameraUpdater.interrupt();
    }

}
