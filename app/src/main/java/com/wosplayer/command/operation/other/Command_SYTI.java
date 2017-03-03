package com.wosplayer.command.operation.other;

import android.text.format.Time;

import com.wosplayer.app.PlayApplication;
import com.wosplayer.app.Logs;
import com.wosplayer.command.operation.schedules.ScheduleReader;
import com.wosplayer.command.operation.interfaces.iCommand;

import java.io.DataOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
		android.provider.Settings.System.putInt(PlayApplication.appContext.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0);//1 获取网络时间
		android.provider.Settings.System.putInt(PlayApplication.appContext.getContentResolver(), android.provider.Settings.System.AUTO_TIME_ZONE, 0);
	}

	private String getSystemTime(){
		//Logs.i(TAG,"当前时区 - "+ TimeZone.getDefault().getDisplayName());
		long time= System.currentTimeMillis();
		final Calendar mCalendar= Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		int mYear = mCalendar.get(Calendar.YEAR);//年
		int mMonth = mCalendar.get(Calendar.MONTH);//月
		int mDate = mCalendar.get(Calendar.DATE);//日
		//Logs.i(TAG,"mCalendar >>> "+mYear+"-"+mMonth+"-"+mDate);
		int mHour=mCalendar.get(Calendar.HOUR_OF_DAY);//取得小时：
		int mMinuts=mCalendar.get(Calendar.MINUTE);//取得分钟：
		int mSecond=mCalendar.get(Calendar.SECOND);//取得秒
		//Logs.i(TAG,"mCalendar >>> "+mHour+":"+mMinuts+":"+mSecond);

		Time t=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料
		t.setToNow(); // 取得系统时间。
		int year = t.year;
		int month = t.month;
		int date = t.monthDay;
		//Logs.i(TAG,"Time() >>> \n"+year+"-"+month+"-"+date);
		int hour = t.hour;    // 0-23
		int minuts = t.minute;
		int seconds = t.second;
		//Logs.i(TAG,"Time() >>> \n"+hour+":"+minuts+":"+seconds);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String times = df.format(new Date());
		return times;
	}
	@Override
	public void Execute(String param) {
		Logs.i(TAG,"终端时间同步 parama:"+param +";当前线程:"+Thread.currentThread().getName());

		if (!RegexMatches(param)){
			Logs.e(TAG,"Sync server time err-> param not atches" + param);
			return;
		}

		if(justTime(getSystemTime(),param)){
			return;
		}
		String settingTime = param.replaceAll("-", "").replace(":","").replaceAll(" ", ".");
		//Logs.i(TAG,"准备设置时间参数 >date>> "+settingTime);
		liunx_SU_syncTimeCmd(settingTime,"Asia/Shanghai");
		//Logs.i(TAG,"当前设置时区 Asia/Shanghai >>> 时间 - "+getSystemTime());

		//再次执行排期读取
		ScheduleReader.clear();
		ScheduleReader.Start(false);

	}

	private boolean justTime(String androidTime,String serviceTime) {

		try {
			DateFormat dataFormatUtils = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()); // 格式化 时间 工具
			long systemTime = dataFormatUtils.parse(serviceTime).getTime();    //服务器传来的时间
			long currentTime = dataFormatUtils.parse(androidTime).getTime();    //当前时间

			if (Math.abs(currentTime-systemTime) < (5 * 1000)){
				//Logs.i(TAG, "时间正确 - android"+currentTime +"   server - "+systemTime);
				return true;
			}else{
				Logs.i(TAG, "时间错误 android  "+currentTime +"   server - "+systemTime);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}


	private String  liunx_SU_syncTimeCmd(String param,String timeZone){
				//Logs.i(TAG,"yyyyMMdd.HHmmss ==>"+param);
				Process process = null;
				DataOutputStream os = null;
				try {
					process = Runtime.getRuntime().exec("su");
					String datetime=param;//"20131023.112800" _测试的设置的时间【时间格式 yyyyMMdd.HHmmss】
					os = new DataOutputStream(process.getOutputStream());
					os.writeBytes("setprop persist.sys.timezone "+ timeZone+"\n");
					os.writeBytes("/system/bin/date -s "+datetime+"\n");
					os.writeBytes("clock -w\n");
					os.writeBytes("exit\n");
					os.flush();
					process.waitFor();
					//Logs.i(TAG,"时间同步成功:"+getSystemTime());

					return getSystemTime();


				}catch (Exception e) {
					Logs.e(TAG,"time sync \"su\" cmd err");
					return "";
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


	public static boolean RegexMatches (String matcherStr){

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

