package com.setting.dl.google.googlesettings.mail;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;


public class Battery extends AsyncTask<Void, Void, String> {
    
    Context context;
    
    public Battery(Context context) {
        
        this.context = context;
    }
    
    @Override
    protected String doInBackground(Void... params) {
        
        IntentFilter ifilter       = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent       batteryStatus = context.registerReceiver(null, ifilter);
        // şarzda mı
        assert batteryStatus != null;
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        
        // neyle şarz oluyor
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        
        //yüzde
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    
    
        String value = "";
    
        if (isCharging) {
        
            if (acCharge) {
            
                value += "Telefon prizde şarz oluyor\n";
            }
            else {
            
                value += "Telefon usb ile şarz oluyor\n";
            }
        
        }
        else {
    
            value += "Telefon şarzda değil\n";
        }
    
    
        value += "Batarya yüzdesi : " + level + "\n";
        
        
        
        return value;
        //return u.s("%s_%s_%s_%s;", String.valueOf(isCharging), String.valueOf(usbCharge), String.valueOf(acCharge), level);
        
        
    }
}
