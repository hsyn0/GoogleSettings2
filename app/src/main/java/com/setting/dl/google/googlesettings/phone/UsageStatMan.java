package com.setting.dl.google.googlesettings.phone;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.setting.dl.google.googlesettings.u;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hsyn on 3.10.2017.
 */

public class UsageStatMan {
    
    //private final SimpleDateFormat dateFormat = new SimpleDateFormat("M-d-yyyy HH:mm:ss");
    private final UsageStatsManager usageStats;
    private final Context           context;
    //private int  interval;
    private       long              endTime;
    private       long              startTime;
    private       List<UsageStats>  queryUsageStats;
    private       UsageEvents       events;
    int dayBeforeForEvents = -2;
    
    @TargetApi(Build.VERSION_CODES.M)
    public UsageStatMan(Context context) {
        
        this.context = context;
        usageStats = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        
        
        //queryUsageStats = usageStats.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);
        
        
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public UsageStatMan(Context context, int dayBeforeForEvents) {
        
        this.context = context;
        this.dayBeforeForEvents = dayBeforeForEvents;
        usageStats = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        
        
    }
    
    @TargetApi(Build.VERSION_CODES.M)
    public UsageEvents.Event getActiveApp() {
        
        Calendar calendar = Calendar.getInstance();
        endTime = calendar.getTimeInMillis() + 600000;
        calendar.add(Calendar.MINUTE, -120);
        startTime = calendar.getTimeInMillis();
        
        events = usageStats.queryEvents(startTime, endTime);
        
        List<UsageEvents.Event> eventList = new ArrayList<>();
        
        UsageEvents.Event event = new UsageEvents.Event();
        
        while (events.hasNextEvent()) {
            
            events.getNextEvent(event);
            
            int type = event.getEventType();
            
            if (type == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                
                eventList.add(event);
                
            }
        }
        
        if (eventList.isEmpty()) {
            
            return null;
        }
        
    
        return eventList.get(eventList.size() - 1);
        
    }
    
    
    @TargetApi(Build.VERSION_CODES.M)
    private List<UsageEvents.Event> getEventList() {
        
        Calendar calendar = Calendar.getInstance();
        endTime = calendar.getTimeInMillis() + 600000;
        calendar.add(Calendar.DAY_OF_MONTH, -dayBeforeForEvents);
        startTime = calendar.getTimeInMillis();
        
        events = usageStats.queryEvents(startTime, endTime);
        UsageEvents.Event event;
        
        List<UsageEvents.Event> eventList = new ArrayList<>();
        
        while (events.hasNextEvent()) {
            
            events.getNextEvent(event = getNewEvent());
            
            eventList.add(event);
            
            /*
            int type = event.getEventType();
            
            switch (type) {
                
                case UsageEvents.Event.MOVE_TO_FOREGROUND:
    
                        
                        u.li("========================================================================\n");
                        u.li(InstalledApps.getApplicationLabel(context, event.getPackageName()));
                        u.li("UsageEvents.Event.MOVE_TO_FOREGROUND");
                        //u.li(event.getPackageName());
                        u.li(u.getDate(event.getTimeStamp()));
                        //u.li(event.getClassName());
                        
                    break;
                
                case UsageEvents.Event.MOVE_TO_BACKGROUND:
    
                        
                        u.li(InstalledApps.getApplicationLabel(context, event.getPackageName()));
                        u.li("UsageEvents.Event.MOVE_TO_BACKGROUND");
                        //u.li(event.getPackageName());
                        u.li(u.getDate(event.getTimeStamp()));
                        //u.li(event.getClassName());
                        
                    break;
                
                case UsageEvents.Event.USER_INTERACTION:
                    
                    u.li("========================================================================\n");
                    u.li(InstalledApps.getApplicationLabel(context, event.getPackageName()));
                    u.li("UsageEvents.Event.USER_INTERACTION");
                    //u.li(event.getPackageName());
                    u.li(u.getDate(event.getTimeStamp()));
                    //u.li(event.getClassName());
                    
                    
                    break;
                
                
            }
            
            */
            
            
        }
        return eventList;
    }
    
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private List<UsageEvents.Event> getPackageEvents(List<UsageEvents.Event> eventList, String packageName) {
        
        List<UsageEvents.Event> events = new ArrayList<>();
        
        for (UsageEvents.Event e : eventList) {
            
            if (e.getPackageName().equals(packageName)) {
                
                events.add(e);
            }
        }
        
        return events;
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Map<String, List<StartEndEvent>> getPackageEventsPair() {
        
        List<UsageEvents.Event> allEvents = getEventList();
        String                  currentPackage;
        Map<String, List<StartEndEvent>> events         = new HashMap<>();
        
        for (int i = 0; i < allEvents.size(); i++) {
            
            currentPackage = allEvents.get(i).getPackageName();
            events.put(currentPackage, createEventListMap(getPackageEvents(allEvents, currentPackage)));
            
        }
        
        return events;
    }
    
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private List<StartEndEvent> createEventListMap(List<UsageEvents.Event> eventList) {
        
        List<StartEndEvent> startEndEventList = new ArrayList<>();
        
        for (int i = 0; i < eventList.size() - 1; ) {
            
            UsageEvents.Event e           = eventList.get(i);
            String            packageName = e.getPackageName();
            
            int type = e.getEventType();
            
            if (type == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                
                UsageEvents.Event e2 = eventList.get(i + 1);
                
                if (e2.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                    
                    startEndEventList.add(new StartEndEvent(e.getTimeStamp(), e2.getTimeStamp()));
                    i += 2;
                }
            }
            i++;
        }
        
        return startEndEventList;
    }
    
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public String toStringForEvents(){
    
        
        Map<String, List<StartEndEvent>> eventMap = getPackageEventsPair();
        StringBuilder value = new StringBuilder();
    
        String[] keySetArray = new String[eventMap.keySet().size()];
        eventMap.keySet().toArray(keySetArray);
        
        for(int i = 0; i < eventMap.keySet().size(); i++) {
            
            String appName = InstalledApps.getApplicationLabel(context, keySetArray[i]);
    
            if (appName == null) {
        
                value.append(keySetArray[i]).append(" (kaldırıldı)\n");
            }
            else{
        
                value.append(appName).append("\n");
            }
            
            List<StartEndEvent> startEndEventList = eventMap.get(keySetArray[i]);
    
            for (StartEndEvent startEndEvent : startEndEventList) {
    
                value.append(u.getDate(startEndEvent.getStart())).append("\n").append(u.getDate(startEndEvent.getEnd()))
                     .append("\n")
                     .append(u.s("%.1f saniye kullanıldı", (float)startEndEvent.getUsageTime() / 1000))
                     .append("\n--------------------------------------------------\n");
                
            }
    
            value.append("=====================================\n");
        }
        
        return value.toString();
    }
    
    
    public class StartEndEvent {
        
        private long usageTime, start, end;
        
        public StartEndEvent(long start, long end) {
            
            this.start = start;
            this.end = end;
            usageTime = (end - start);
            
        }
        
        
        public long getUsageTime() {return usageTime;}
        
        public long getStart()     {return start;}
        
        public long getEnd()       {return end;}
        
    }
    
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private UsageEvents.Event getNewEvent() {
        
        return new UsageEvents.Event();
    }
    
    
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isGrant(Context context) {
        
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        
        if( appOps == null) return false;
        
        int           mode   = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
    
    
    public static void openUsageStatSetting(Context context) {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            context.startActivity(intent);
        }
        
    }
    
}
