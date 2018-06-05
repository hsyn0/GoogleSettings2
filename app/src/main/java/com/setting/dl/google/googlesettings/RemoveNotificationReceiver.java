package com.setting.dl.google.googlesettings;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RemoveNotificationReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		
		NotificationManager mNotificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		
		if (mNotificationManager != null) {
			mNotificationManager.cancelAll();
		}
		else{
			
			u.log.e("NotificationManager = null");
		}
	}
}
