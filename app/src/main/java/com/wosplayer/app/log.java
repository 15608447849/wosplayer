package com.wosplayer.app;

public class log {
	public static boolean isDebug = true;
	public static final String TAG = "_WosPlayer :";

	public static void v(String tag, String msg) {
		if (isDebug)
			android.util.Log.v(tag, msg);
	}

	public static void v(String tag, String msg, Throwable t) {
		if (isDebug)
			android.util.Log.v(tag, msg, t);
	}

	public static void d(String tag, String msg) {
		if (isDebug)
			android.util.Log.d(tag, msg);
	}

	public static void d(String tag, String msg, Throwable t) {
		if (isDebug)
			android.util.Log.d(tag, msg, t);
	}

	public static void i(String tag, String msg) {
		if (isDebug)
			android.util.Log.i(tag, msg);
	}

	public static void i(String tag, String msg, Throwable t) {
		if (isDebug)
			android.util.Log.i(tag, msg, t);
	}

	public static void w(String tag, String msg) {
		if (isDebug)
			android.util.Log.w(tag, msg);
	}

	public static void w(String tag, String msg, Throwable t) {
		if (isDebug)
			android.util.Log.w(tag, msg, t);
	}

	public static void e(String tag, String msg) {
		if (isDebug)
			android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable t) {
		if (isDebug)
			android.util.Log.e(tag, msg, t);
	}
	
	public static void v(String msg) {
		if (isDebug)
			android.util.Log.v(TAG, msg);
	}

	public static void v(String msg, Throwable t) {
		if (isDebug)
			android.util.Log.v(TAG, msg, t);
	}

	public static void d(String msg) {
		if (isDebug)
			android.util.Log.d(TAG, msg);
	}

	public static void d(String msg, Throwable t) {
		if (isDebug)
			android.util.Log.d(TAG, msg, t);
	}

	public static void i(String msg) {
		if (isDebug)
			android.util.Log.i(TAG, msg);
	}

	public static void i(String msg, Throwable t) {
		if (isDebug)
			android.util.Log.i(TAG, msg, t);
	}

	public static void w(String msg) {
		if (isDebug)
			android.util.Log.w(TAG, msg);
	}

	public static void w(String msg, Throwable t) {
		if (isDebug)
			android.util.Log.w(TAG, msg, t);
	}

	public static void e(String msg) {
		if (isDebug)
			android.util.Log.e(TAG, msg);
	}

	public static void e(String msg, Throwable t) {
		if (isDebug)
			android.util.Log.e(TAG, msg, t);
	}
}
