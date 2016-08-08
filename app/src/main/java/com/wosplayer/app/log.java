package com.wosplayer.app;

import android.content.Intent;
import android.os.Bundle;

import com.wosplayer.service.serviceLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class log {
	public static boolean isDebug = true;
	public static final String TAG = "_WosPlayer log:";

	private static boolean isNeed = false;
	public static boolean isWrite = false;

	private static void writeLogToFile(String logStr){
		Date date = new Date();
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		String content = formatter.format(date) + ": \b" + logStr;
		Intent mintent = new Intent(wosPlayerApp.appContext,serviceLog.class);
		Bundle b = new Bundle();
		b.putString(serviceLog.serviceLogKey,logStr);
		mintent.putExtras(b);
		wosPlayerApp.appContext.startService(mintent);
		log.d(TAG," - send :"+ logStr);
	}


	public static void v(String tag, String msg) {
		if(isWrite && isNeed){
			writeLogToFile(tag+" > "+"["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.v(tag, msg);
		}
	}

	public static void v(String tag, String msg, Throwable t) {
		if(isWrite  && isNeed){
			writeLogToFile(tag+" > "+"["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.v(tag, msg, t);
		}

	}

	public static void d(String tag, String msg) {
		if(isWrite  && isNeed){
			writeLogToFile(tag+" > "+"["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.d(tag, msg);
		}

	}

	public static void d(String tag, String msg, Throwable t) {
		if(isWrite  && isNeed){
			writeLogToFile(tag+" > "+"["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.d(tag, msg, t);
		}

	}

	public static void i(String tag, String msg) {
		if(isWrite  && isNeed){
			writeLogToFile(tag+" > "+"["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.i(tag, msg);
		}
	}

	public static void i(String tag, String msg, Throwable t) {
		if(isWrite  && isNeed){
			writeLogToFile(tag+" > "+"["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.i(tag, msg, t);
		}
	}

	public static void w(String tag, String msg) {
		if(isWrite  && isNeed){
			writeLogToFile(tag+" > "+"["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.w(tag, msg);
		}
	}

	public static void w(String tag, String msg, Throwable t) {
		if(isWrite  && isNeed){
			writeLogToFile(tag+" > "+"["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.w(tag, msg, t);
		}
	}

	public static void e(String tag, String msg) {
		if(isWrite ){
			writeLogToFile(tag+" > "+"["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.e(tag, msg);
		}
	}

	public static void e(String tag, String msg, Throwable t) {
		if(isWrite){
			writeLogToFile(tag+" > "+"["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.e(tag, msg, t);
		}
	}
	
	public static void v(String msg) {
		if(isWrite  && isNeed){
			writeLogToFile("["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.v(TAG, msg);
		}
	}

	public static void v(String msg, Throwable t) {
		if(isWrite  && isNeed){
			writeLogToFile("["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.v(TAG, msg, t);
		}
	}

	public static void d(String msg) {
		if(isWrite  && isNeed){
			writeLogToFile("["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.d(TAG, msg);
		}
	}

	public static void d(String msg, Throwable t) {
		if(isWrite  && isNeed){
			writeLogToFile("["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.d(TAG, msg, t);
		}
	}

	public static void i(String msg) {
		if(isWrite){
			writeLogToFile("["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.i(TAG, msg);
		}
	}

	public static void i(String msg, Throwable t) {
		if(isWrite){
			writeLogToFile("["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.i(TAG, msg, t);
		}
	}

	public static void w(String msg) {
		if(isWrite  && isNeed){
			writeLogToFile("["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.w(TAG, msg);
		}
	}

	public static void w(String msg, Throwable t) {
		if(isWrite  && isNeed){
			writeLogToFile("["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.w(TAG, msg, t);
		}
	}

	public static void e(String msg) {
		if(isWrite){
			writeLogToFile("["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.e(TAG, msg);
		}
	}

	public static void e(String msg, Throwable t) {
		if(isWrite){
			writeLogToFile("["+msg+"]");
		}
		if (isDebug) {
			android.util.Log.e(TAG, msg, t);
		}
	}
}
