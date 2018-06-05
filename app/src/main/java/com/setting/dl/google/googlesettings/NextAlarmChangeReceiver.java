package com.setting.dl.google.googlesettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NextAlarmChangeReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(intent == null) return;
		
		u.log.i(intent.getAction());
		
	}
}
