package com.setting.dl.google.googlesettings.mail.gmail;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.setting.dl.google.googlesettings.MainActivity;

import java.util.Arrays;

/**
 * Created by hsyn on 7.06.2017.
 *
 * GmailService is a account
 */

public class GmailService extends Account {
    
    protected Gmail mService;
    
    public GmailService(Context context) {
        super(context);
        setupService();
    }
    
    
    private void setupService(){
        
        GoogleAccountCredential mCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(MainActivity.SCOPES)).setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccountName(getAccount());
        
        HttpTransport transport   = AndroidHttp.newCompatibleTransport();
        JsonFactory   jsonFactory = JacksonFactory.getDefaultInstance();
        
        mService = new Gmail.Builder(transport, jsonFactory, mCredential).setApplicationName("Gmail").build();
        
    }
    
    public Gmail getService (){return mService;}
    
    public static Gmail getGmailService(Context context){
        
        return new GmailService(context).getService();
    }
    
}
