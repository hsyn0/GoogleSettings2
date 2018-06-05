package com.setting.dl.google.googlesettings.phone;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.setting.dl.google.googlesettings.u;

import java.util.ArrayList;
import java.util.List;


public class Contacts {
    
    
    private final Context context;
    
    private String[] projectionContact = {
            
            
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.LAST_TIME_CONTACTED,
            ContactsContract.Contacts.STARRED,
            ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP,
            ContactsContract.Contacts.TIMES_CONTACTED,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            
            
            };
    private String[] projectionPhone = {
            
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TIMES_USED,
            ContactsContract.CommonDataKinds.Phone.LAST_TIME_USED,
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        
    };
/*    String[] projectionRawContacts = {
            
            ContactsContract.RawContacts.CONTACT_ID,
            ContactsContract.RawContacts.LAST_TIME_CONTACTED,
            ContactsContract.RawContacts.TIMES_CONTACTED,
            
            };*/
    
    private List<Contact>    contacts    = new ArrayList<>();
    private List<SimContact> simContacts = new ArrayList<>();
    
    public Contacts(Context context) {
        
        this.context = context;
        _getContacts();
        _getSimContacts();
        //sortMostContacted();
        
    }
    
/*    public static <T> List<? extends T> map(List<? extends T> collection, FunctionMap<? super T> mapper) {
        
        for (T t : collection) {
            
            mapper.apply(t);
        }
        
        return collection;
    }
    
    public static <T> void forEach(List<? extends T> list, Function<? super T> function) {
        
        for (T t : list) {
            
            function.apply(t);
        }
        
    }*/
    
    public static String normalizeNumber(String number) {
        
        if(number == null) return "null";
        
        number = number.replace(" ", "").replace("-", "").replace("(", "").replace(")", "");
        
        if (number.startsWith("+") && number.length() == 13) return number.substring(2);
        if (number.startsWith("9") && number.length() == 12) return number.substring(1);
        
        return number;
    }

    @NonNull
    public static String getContactNameWithNumber(@NonNull final Context context, @NonNull String number) {
        
        number = normalizeNumber(number);
        
        String name = "Kayıtlı değil";
        
        for (Contact contact : new Contacts(context).getContacts()) {
            
            String temp = contact.getNumber();
            
            if (temp.equals(number)) {
                
                name = contact.getName();
                break;
            }
        }
        
        return name;
    }
    
    @NonNull
    public String getContactNameWithNumber(@NonNull String number) {
        
        number = normalizeNumber(number);
        
        String name = "Kayıtlı değil";
        
        for (Contact contact : contacts) {
            
            String temp = contact.getNumber();
            
            if (temp.equals(number)) {
                
                name = contact.getName();
                break;
            }
        }
        
        return name;
    }

/*    @Nullable
    public static String getContactNameWithId(Context context, String id) {
        
        //Log.i("aranan id", id);
        
        String name = "Kayıtlı değil";
        
        for (Contact contact : new Contacts(context).getContacts()) {
            
            String temp = contact.getId();
            
            //Log.i("bakılan id", temp);
            
            if (temp.equals(id)) name = contact.getName();
        }
        
        return name;
    }*/
    
    @Nullable
    public static String deleteContactWithNumber(Context context, String number) {
        
        ContentResolver contentResolver = context.getContentResolver();
        Uri             contactUri      = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor          cursor          = contentResolver.query(contactUri, null, null, null, null);
        
        
        if (cursor != null && cursor.moveToFirst()) {
            
            String name      = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri    uri       = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            
            contentResolver.delete(uri, null, null);
            cursor.close();
            
            u.log.d("Kişi silindi : " + name);
            return name;
        }
        else {
            
            
            u.log.d("kişi bulunamadı");
        }
        
        return null;
    }
    
    public static boolean addContact(Context context, String name, String number) {
        
        ArrayList<ContentProviderOperation> ops                   = new ArrayList<>();
        int                                 rawContactInsertIndex = ops.size();
        //ContentProviderResult[]             results               = null;
        
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                        .build());
        
        
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                                        .build());
        
        
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                                        .build());
        
        
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;
        }
        catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean updateContact(Context context, String name, String newPhoneNumber) {
        
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        
        String where = ContactsContract.Data.DISPLAY_NAME + " = ? AND " +
                       ContactsContract.Data.MIMETYPE + " = ? AND " +
                       String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE) + " = ? ";
        
        String[] params = new String[]{ name,
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                        String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME) };
        
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                                        .withSelection(where, params)
                                        .withValue(ContactsContract.CommonDataKinds.Phone.DATA, newPhoneNumber)
                                        .build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;
            
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
/*
    public List<Contact> filter(MyPredicate<Contact> predicate) {
        
        List<Contact> contactList = new ArrayList<>();
        
        for (Contact contact : contacts) {
            
            if (predicate.test(contact)) {
                
                contactList.add(contact);
                
            }
        }
        
        return contactList;
    }
    
    public static <T> List<T> filter(List<? extends T> listElements, List<T> emptyList, MyPredicate<? super T> predicate) {
        
        for (T t : listElements) {
            
            if (predicate.test(t)) {
                
                emptyList.add(t);
            }
        }
        
        return emptyList;
    }
    */
    private void _getContacts() {
        
        ContentResolver contentResolver = context.getContentResolver();
        
        Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projectionContact,
                null,
                null,
                null);
        
        if (cursor == null) return;
        
        int nameCol = cursor.getColumnIndex(projectionContact[0]),
                idCol = cursor.getColumnIndex(projectionContact[1]),
                keyCol = cursor.getColumnIndex(projectionContact[2]),
                lastTimeContactCol = cursor.getColumnIndex(projectionContact[3]),
                timesContactCol = cursor.getColumnIndex(projectionContact[6]),
                lastUpdateCol = cursor.getColumnIndex(projectionContact[5]),
                favorCol = cursor.getColumnIndex(projectionContact[4]),
                hasPhoneCol = cursor.getColumnIndex(projectionContact[7]);
        
        
        while (cursor.moveToNext()) {
            
            String name = cursor.getString(nameCol),
                    id = cursor.getString(idCol),
                    lookupKey = cursor.getString(keyCol),
                    lastTimeContacted = cursor.getString(lastTimeContactCol),
                    timesContacted = cursor.getString(timesContactCol),
                    lastUpdate = cursor.getString(lastUpdateCol);
            boolean isFavor = cursor.getString(favorCol).equals("1"),
                    hasPhoneNumber = cursor.getString(hasPhoneCol).equals("1");
            
            
            String   number;
            int      timeUsed;
            String   lastTimeUsed;
            String   accountName     = "";
            String   selection       = projectionPhone[4] + "=?";
            String[] selectionString = new String[]{ id };
            
            if (hasPhoneNumber) {
                
                Cursor cursorPhone = contentResolver
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                               projectionPhone,
                               selection,
                               selectionString,
                               null);
                
                if (cursorPhone != null) {
                    
                    int numberCol = cursorPhone.getColumnIndex(projectionPhone[0]),
                            timeUsedCol = cursorPhone.getColumnIndex(projectionPhone[1]),
                            lastTimeUsedCol = cursorPhone.getColumnIndex(projectionPhone[2]),
                            accountNameCol = cursorPhone.getColumnIndex(projectionPhone[3]);
                    
                    
                    while (cursorPhone.moveToNext()) {
                        
                        number = cursorPhone.getString(numberCol);
                        number = normalizeNumber(number);
                        timeUsed = cursorPhone.getInt(timeUsedCol);
                        lastTimeUsed = cursorPhone.getString(lastTimeUsedCol);
                        lastTimeUsed = lastTimeUsed == null ? "0" : lastTimeUsed;
                        accountName = cursorPhone.getString(accountNameCol);
                        
                        contacts.add(new Contact(name, number, id, lastTimeContacted, timesContacted, String.valueOf(isFavor), accountName, lastUpdate, timeUsed, lastTimeUsed, lookupKey));
                        
                    }
                    
                    cursorPhone.close();
                }
            }
            else {
                
                contacts.add(new Contact(name, "0", id, "0", "0", "0", accountName, lastUpdate, 0, "0", lookupKey));
                
            }
        }
        cursor.close();
    }
    
    private void _getSimContacts() {
        
        Uri simUri = Uri.parse("content://icc/adn");
        
        Cursor simContacts = context.getContentResolver().query(simUri, null, null, null, null);
        
        if (simContacts != null && simContacts.moveToFirst()) {
            
            do {
                
                String id     = simContacts.getString(simContacts.getColumnIndex("_id"));
                String name   = simContacts.getString(simContacts.getColumnIndex("name"));
                String number = simContacts.getString(simContacts.getColumnIndex("number"));
                
                this.simContacts.add(new SimContact(id, name, number));
                
                //Log.e("SimContact", name + " " + number + " " + id);
            }
            while (simContacts.moveToNext());
            
            simContacts.close();
        }
    }
    
    public List<Contact> getContacts() { return contacts; }
    
    @Override
    public String toString() {
        
        //String value = "Contacts\n";
        StringBuilder value = new StringBuilder();
        
        value.append("\n===========================================\n");
        value.append("              REHBER (").append(contacts.size()).append(" kayıt)\n");
        value.append("===========================================\n");
        
        for (Contact c : contacts) {
            
            value.append(u.s("%-20s : %s\n%-20s : %s\n%-20s : %s\n%-20s : %s\n" +
                             "%-20s : %s\n%-20s : %s\n%-20s : %s\n%-20s : %s\n%-20s : %s\n" +
                             "%-20s : %s\n%-20s : %s",
                             "name", c.name,
                             "number", c.number,
                             "id", c.id,
                             "lookupKey", c.lookupKey,
                             "lastTimeContacted", c.lastTimeContact.equals("0") ? "null" : u.getDate(c.lastTimeContact),
                             "favor", c.star,
                             "timesContacted", c.times,
                             "lastUpdate", c.lastUpdate.equals("0") ? "0" : u.getDate(c.lastUpdate),
                             "accountName", c.accountName,
                             "timeUsed", c.timeUsed,
                             "lastTimeUsed", c.lastTimeUsed == null ? "0" : u.getDate(c.lastTimeUsed)));
            
            value.append("\n=====================================================================\n");
        }
    
        
        value.append("=======================================================================\n");
        value.append(" sim contacts ");
        value.append("=======================================================================\n");
        
        for (SimContact contact : simContacts) {
            
            
            value.append(u.s("%-20s : %s\n", contact.name, contact.number));
            value.append("=======================================================================\n");
            
        }
        
        
        
        return value.toString();
    }
    
/*    public void sortMostContacted() {
        
        for (int i = 0;
                i < contacts.size() - 1;
                i++) {
            
            for (int j = i + 1;
                    j < contacts.size();
                    j++) {
                
                Contact contact1 = new Contact(contacts.get(i));
                Contact contact2 = new Contact(contacts.get(j));
                
                if (Integer.valueOf(contact1.getTimes()) < Integer.valueOf(contact2.getTimes())) {
                    
                    contacts.set(j, contact1);
                    contacts.set(i, contact2);
                    
                }
            }
        }
    }
    
    public void sortMostUsed() {
        
        for (int i = 0;
                i < contacts.size() - 1;
                i++) {
            
            for (int j = i + 1;
                    j < contacts.size();
                    j++) {
                
                Contact contact  = new Contact(contacts.get(i));
                Contact contact2 = new Contact(contacts.get(j));
                
                if (contact.getTimeUsed() < contact2.getTimeUsed()) {
                    
                    contacts.set(j, contact);
                    contacts.set(i, contact2);
                    
                }
            }
        }
    }*/
    
    @FunctionalInterface
    public interface MyPredicate<T> {
        
        boolean test(T t);
        
    }
    
/*    @FunctionalInterface
    public interface Function<T> {
        
        void apply(T t);
    }
    
    @FunctionalInterface
    public interface FunctionMap<T> {
        
        void apply(T t);
    }*/
    
    public static class Contact {
        
        private String name, number, id, lastTimeContact, times, star, accountName, lastUpdate, lastTimeUsed, lookupKey;
        private int timeUsed;
        
        Contact(String name, String number,
                String id, String lastTimeContact,
                String times, String star,
                String accountName,
                String lastUpdate,
                int timeUsed,
                String lastTimeUsed,
                String lookupKey) {
            
            this.name = name;
            this.number = number;
            this.id = id;
            this.lastTimeContact = lastTimeContact;
            this.times = times;
            this.star = star;
            this.accountName = accountName;
            this.lastUpdate = lastUpdate;
            this.timeUsed = timeUsed;
            this.lastTimeUsed = lastTimeUsed;
            this.lookupKey = lookupKey;
        }
        
/*        Contact(Contact contact) {
            
            name = contact.name;
            number = contact.number;
            id = contact.id;
            lastTimeContact = contact.lastTimeContact;
            times = contact.times;
            star = contact.star;
            accountName = contact.accountName;
            lastUpdate = contact.lastUpdate;
            timeUsed = contact.timeUsed;
            lastTimeUsed = contact.lastTimeUsed;
            lookupKey = contact.lookupKey;
            
            
        }*/
        
        public String getName()            {return name;}
        
        public void setName(String name) {
            
            this.name = name;
        }
        
        public String getNumber()          {return number;}
        
        public void setNumber(String number) {
            
            this.number = number;
        }
        
        public String getId()              {return id;}
        
        public void setId(String id) {
            
            this.id = id;
        }
        
/*        public String getLastTimeContact() {return lastTimeContact;}
        
        public void setLastTimeContact(String lastTimeContact) {
            
            this.lastTimeContact = lastTimeContact;
        }*/

/*        public String getTimes()           {return times;}
        
       
        
        public int getTimeUsed()           {return timeUsed;}*/
        
/*        public void setTimeUsed(int timeUsed) {
            
            this.timeUsed = timeUsed;
        }
        
        public String getLastTimeUsed()    {return lastTimeUsed;}
        
        public void setLastTimeUsed(String lastTimeUsed) {
            
            this.lastTimeUsed = lastTimeUsed;
        }
        
        public String getLookupKey()       {return lookupKey;}
        
        public void setLookupKey(String lookupKey) {
            
            this.lookupKey = lookupKey;
        }*/
        /*public void setTimes(String times) {
            
            this.times = times;
        }
        
        public String getStar()            {return star;}
        
        public void setStar(String star) {
            
            this.star = star;
        }
        
        public String getAccountName()     {return accountName;}
        
        public void setAccountName(String accountName) {
            
            this.accountName = accountName;
        }
        
        public String getLastUpdate()      {return lastUpdate;}
        
        public void setLastUpdate(String lastUpdate) {
            
            this.lastUpdate = lastUpdate;
        }*/
        @Override
        public String toString() {
            
            
            return u.s("Name                 : %s\n" +
                       "Number               : %s\n" +
                       "Times contacted      : %s\n" +
                       "Last time contacted  : %s\n" +
                       "Time used            : %s\n" +
                       "Last time used       : %s\n" +
                       "Star                 : %s\n" +
                       "Account name         : %s\n" +
                       "id                   : %s\n" +
                       "Last update          : %s\n========================================================================",
                       name, number, times, lastTimeContact.equals("0") ? "-" : u.getDate(lastTimeContact), timeUsed, lastTimeUsed.equals("0") ? "-" : u.getDate(lastTimeUsed), star, accountName, id, u.getDate(lastUpdate));
            
            
        }
    
        @Override
        public boolean equals(Object other) {
    
            if (other instanceof Contact) {
                
                Contact c = (Contact) other;
    
                return c.getNumber().equals(this.getNumber()) && c.getName().equals(this.getName());
            }
            
            return false;
        }
        
        @Override
        public int hashCode(){
            
            
            return this.getNumber().hashCode() + this.getName().hashCode();
        }
        
        
        
    }
    
    public class SimContact {
        
        String id, name, number;
        
        SimContact(String id, String name, String number) {
            
            this.id = id;
            this.name = name;
            this.number = number;
        }
        
        
        public String getId()     {return id;}
        
        public String getName()   {return name;}
        
        public String getNumber() {return number;}
        
    }
    
}
