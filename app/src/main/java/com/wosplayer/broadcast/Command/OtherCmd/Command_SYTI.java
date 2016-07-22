package com.wosplayer.broadcast.Command.OtherCmd;

import android.text.format.Time;

import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.broadcast.Command.iCommand;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 设置终端时间
 * @author Administrator
 *
 */
public class Command_SYTI implements iCommand {
	private static final String TAG = Command_SYTI.class.getName();

	//显示系统时间
	public  void disableSystemSyncTime()
	{
		android.provider.Settings.System.putInt(wosPlayerApp.appContext.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0);//1 获取网络时间
		android.provider.Settings.System.putInt(wosPlayerApp.appContext.getContentResolver(), android.provider.Settings.System.AUTO_TIME_ZONE, 0);
	}
	
	private void syncSystemTime(String timeExp)
	{
		log.i(TAG,"当前终端时间:"+getSystemTime());
		log.i(TAG, "准备同步服务器时间 : " + timeExp);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		DateTime dateTime  = fmt.parseDateTime(timeExp);
		long millseconds = dateTime.getMillis();
		log.i(TAG, "sync system time : "+ dateTime);
		log.i(TAG, "sync system time to milliseconds: "+ String.valueOf(millseconds));

	}


	private String getSystemTime(){
		long time= System.currentTimeMillis();
		final Calendar mCalendar= Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		int mHour=mCalendar.get(Calendar.HOUR);//取得小时：
		int mMinuts=mCalendar.get(Calendar.MINUTE);//取得分钟：
		Time t=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料
		t.setToNow(); // 取得系统时间。
		int year = t.year;
		int month = t.month;
		int date = t.monthDay;
		int hour = t.hour;    // 0-23
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String times = df.format(new Date());
		return times;
	}
	@Override
	public void Execute(String param) {
		log.i(TAG,"终端时间同步 parama:"+param +";当前线程:"+Thread.currentThread().getName());

		if (!RegexMatches(param)){
			log.e(TAG,"Sync server time err-> param not atches" + param);
			return;
		}
		disableSystemSyncTime();
		String settingTime = param.replaceAll("-", "").replace(":","").replaceAll(" ", ".");
		log.i(TAG,settingTime);
		liunx_SU_syncTimeCmd(settingTime);

	}


	private void  liunx_SU_syncTimeCmd(String param){
				log.i(TAG,"yyyyMMdd.HHmmss ==>"+param);
				Process process = null;
				DataOutputStream os = null;
				try {
					process = Runtime.getRuntime().exec("su");
					String datetime=param;//"20131023.112800" _测试的设置的时间【时间格式 yyyyMMdd.HHmmss】
					os = new DataOutputStream(process.getOutputStream());
					os.writeBytes("setprop persist.sys.timezone GMT\n");
					os.writeBytes("/system/bin/date -s "+datetime+"\n");
					os.writeBytes("clock -w\n");
					os.writeBytes("exit\n");
					os.flush();
					process.waitFor();
					log.i(TAG,"时间同步成功:"+getSystemTime());
				}catch (Exception e) {
					log.e(TAG,"time sync \"su\" cmd err");
					return;
				}finally {
					try {
						if (os != null) {
							os.close();
						}
						if (process != null) {
							process.destroy();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
	}


	private boolean RegexMatches (String matcherStr){

		String date = "((((1[6-9]|[2-9]\\d)\\d{2})-(1[02]|0?[13578])-([12]\\d|3[01]|0?[1-9]))|(((1[6-9]|[2-9]\\d)\\d{2})-(1[012]|0?[13456789])-([12]\\d|30|0?[1-9]))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(1\\d|2[0-8]|0?[1-9]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29-))";
		String space = "\\s";
		String time = "([01]?\\d|2[0-3]):[0-5]?\\d:[0-5]?\\d";

		String pattern = date+space+time;

		Pattern pattenrn = Pattern.compile(pattern);
		Matcher matcher = pattenrn.matcher(matcherStr);
		boolean flog = matcher.matches();
		return flog;
	}


}

