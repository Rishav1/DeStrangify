package com.example.rishav.destrangify;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{
    private boolean map_update_running = false;
    private boolean querystarted = false;

    private GoogleMap mMap;
    private People currentuser;
    private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
    private final HashSet<String> toKeep = new HashSet<String>();

    private class MapUpdate extends Thread {
        public void run() {
            while(map_update_running) {
                if(Thread.interrupted()) {
                    Log.d("Info", "map update interupted");
                    break;
                }
                if(!querystarted) {
                    querystarted=true;
                    ParseQuery<People> nearbyPeople= ParseQuery.getQuery(People.class);
                    nearbyPeople.whereWithinKilometers("curLoc", currentuser.getCurLoc(), DestrangifyApplication.getMaxRadius());
                    nearbyPeople.findInBackground(new FindCallback<People>() {
                        @Override
                        public void done(List<People> objects, ParseException e) {
                            if (e != null) {
                                createAndShowDialog(e, "Error");
                            }
                            mMap.clear();
                            mapMarkers.clear();
                            for (People nearby_person : objects) {
                                if (nearby_person != currentuser) {
                                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(nearby_person.getCurLoc().getLatitude(), nearby_person.getCurLoc().getLongitude())).title(nearby_person.getUsername()));
                                    marker.setVisible(nearby_person.isLocVis());
                                    mapMarkers.put(nearby_person.getObjectId(), marker);
                                }
                            }
                            querystarted = false;
                        }
                    });
                    Log.d("Info", "Periodic Update done");
                    //                  createAndShowDialog("PeriodicUpdatedone","Info");
                }
                else {
                    Log.d("Info","Periodic update skipped");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    //createAndShowDialog(ex,"Error");
                }
            }
        }
    }

    private MapUpdate updaterMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        currentuser = (People)People.getCurrentUser();
        if(currentuser == null) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        Log.d("Info","Periodic update stopped");
        stopPeriodicUpdate();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Info", "Periodic update started");
        startPeoridicUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentuser.getCurLoc().getLatitude(), currentuser.getCurLoc().getLongitude())));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
    }

    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }


    void startPeoridicUpdates() {
        map_update_running = true;
        updaterMap = new MapUpdate();
        updaterMap.start();
    }

    void stopPeriodicUpdate() {
        map_update_running = false;
        updaterMap.interrupt();
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
