package com.example.rishav.destrangify;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class Dashboard extends AppCompatActivity  implements LocationListener{

    private People currentuser;
    private Button _loadMap;
    private Button _loadCameraView;
    private Button _loadProfiles;
    private Button _editProfile;
    private Button _logout;

    private LocationManager locationManager;
    private Location currentlocation;

    public static final int REQUEST_MAP=0;
    public static final int REQUEST_EDIT=1;
    public static final int REQUEST_PROFILES=2;
    public static final int REQUEST_LOGIN=3;
    public static final int REQUEST_CAMERA=4;

    public static final int MIN_TIME= 1000;
    public static final float MIN_DISTANCE = 1;


    public static final int LOC_UPDATE_TIME=15000;

    private Criteria criteria;

    private class locationUpdate extends Thread {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(LOC_UPDATE_TIME);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Log.d("Error", "update thread interrupted");
                }
                currentuser=(People)People.getCurrentUser();
                if (currentuser==null) {
                    Log.d("Info", "ThreadInterrupted_UserLoggedOut");
                    continue;
                }
                if(currentlocation!=null) {
                    currentuser.setCurLoc(geoPointFromLocation(currentlocation));
                    Log.d("info", "attempting to save");
                    currentuser.saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null)
                                Log.d("info", "Loc save done");
                            else
                                Log.d("Error", e.toString());
                        }
                    });
                } else {
                    Log.d("Info","No location got");
                }
            }
        }
    }

    private locationUpdate updater= new locationUpdate();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        currentuser = (People)People.getCurrentUser();
        if(currentuser==null)
            logout(findViewById(R.id.logout_button));

        _loadMap=(Button)findViewById(R.id.map_button);
        _loadCameraView=(Button)findViewById(R.id.ar_camera_button);
        _loadProfiles=(Button)findViewById(R.id.view_button);
        _editProfile=(Button)findViewById(R.id.myprofile_button);
        _logout=(Button)findViewById(R.id.logout_button);

        _loadMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMap(v);
            }
        });
        _loadCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCameraView(v);
            }
        });
        _loadProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProfiles(v);
            }
        });
        _editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfile(v);
            }
        });
        _logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(v);
            }
        });

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, true), MIN_TIME, MIN_DISTANCE, this);
        Log.d("Info","Best Provider "+locationManager.getBestProvider(criteria,true));
        updater.start();        
    }

    public void loadMap(View view) {
        if(currentuser.isLocVis()) {
            Intent intent_map = new Intent(this, MapsActivity.class);
            startActivityForResult(intent_map, REQUEST_MAP);
        }
        else {
            Toast.makeText(getApplicationContext(),"Your location should be shared",Toast.LENGTH_SHORT).show();
        }
    }

    public void loadCameraView(View view) {
        Intent intent_camera_view = new Intent(this,ArCameraActivity.class);
        startActivityForResult(intent_camera_view,REQUEST_CAMERA);
    }

    public void loadProfiles(View view) {

    }

    public void editProfile(View view) {
        Intent intent_my_profile = new Intent(this,my_profile.class);
        startActivityForResult(intent_my_profile,REQUEST_EDIT);
    }

    public void logout(View view)
    {
        if(currentuser!=null) {
            currentuser.setLogged(false);
            try {
                currentuser.save();
                Log.d("Info", "logged_false_set");
            } catch (Exception e) {
                Log.d("Error",e.toString());
            }
            currentuser.logOut();
        }
        Intent i = new Intent(this,LoginActivity.class);
        startActivityForResult(i,REQUEST_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                Log.d("Info","UserLoggedin");
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Info","Location Changed");
        currentlocation = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    private void createAndShowDialog(Exception exception, String title) {
        createAndShowDialog(exception.toString(), title);
    }


    private void createAndShowDialog(String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }
}