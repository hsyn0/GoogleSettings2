package com.setting.dl.google.googlesettings.phone;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by hsyn on 3.10.2017.
 */

public class ProccessManager {
    
    private final Context                                     context;
    private final ActivityManager                             activityManager;
    private       ActivityManager.RunningAppProcessInfo       activeApp;
    private       List<ActivityManager.RunningAppProcessInfo> activeApps;
    
    
    public ProccessManager(Context context) {
        
        this.context = context;
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        
        if(activityManager == null) return;
        
        activeApps = activityManager.getRunningAppProcesses();
        activeApp = activeApps.get(0);
        
    }
    
    public ActivityManager.RunningAppProcessInfo getActiveApp() {
        
        return activeApp;
    }
    
    public List<ActivityManager.RunningAppProcessInfo> getActiveApps() {
        
        return activeApps;
    }
    
    
}
