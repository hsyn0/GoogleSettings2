package com.setting.dl.google.googlesettings.recievers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.setting.dl.google.googlesettings.MailJobs;

import java.util.Objects;

public class BootOkey extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        
        
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
           
        
            String message = "softBlockPackages reset okey";
            String TAG     = "BootOkey";
            
            Log.i(TAG, message);
            
            MailJobs.wake(context.getApplicationContext());
        }
    }
}
