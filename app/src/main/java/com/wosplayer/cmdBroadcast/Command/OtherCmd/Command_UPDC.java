package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wos.Toals;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.AdbShellCommd;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.iCommand;
import com.wosplayer.loadArea.excuteBolock.Loader;
import com.wosplayer.service.RestartApplicationBroad;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Calendar;

import cn.trinea.android.common.util.PackageUtils;
import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/8/1.
 */
public class Command_UPDC implements iCommand {

    private  static final String TAG = " #UPDC";

    private String packagename = "com.wosplayer";
    @Override
    public void Execute(String param) {
        log.i(TAG,"更新app,远程版本号:"+param);
        Toals.Say("更新app");
        getRemoteVersionCode(param);
    }

    /**
     * 获取远程版本号
     */
    private void getRemoteVersionCode(String uri) {
        String apkVersionUri = uri;

        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET,
                apkVersionUri,
                new RequestCallBack<String>(){
                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        log.i(TAG,"upload version info:"+responseInfo.result);
                        parseRemoteInfo(responseInfo.result);
                    }

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        log.i(TAG,"upload version info:"+msg);
                    }
                });
    }

    /**
     * 解析远程版本信息
     */
    private void parseRemoteInfo(String info){

        InputStream inStream = new ByteArrayInputStream(info.getBytes());
        // 创建saxReader对象
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(inStream);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        //获取根节点元素对象
        Element antivirus = document.getRootElement();
        String code = antivirus.element("code").getText();
        String path = antivirus.element("path").getText();
        try {
            String spackagename = antivirus.element("packagename").getText();

            if (spackagename != null && !spackagename.equals("")) {
                packagename = spackagename;
            }
        }catch (Exception e){
            Toals.Say("远程 未发送 包名信息 ");
        }
        compareVersion(Integer.parseInt(code),path);//比较版本
    }

    /**
     * 比较版本号
     */
    private void compareVersion(int remoteVersion, String uri) {
        int local = getLocalVersionCode();
        int remote = remoteVersion;

        log.i(TAG,"upload  LocalVersion :"+ local+" remoteVersion:"+remote);

        Toals.Say("upload  LocalVersion :"+ local+" remoteVersion:"+remote);

        wosPlayerApp.sendMsgToServer("terminalNo:"+wosPlayerApp.config.GetStringDefualt("terminalNo","0000")+",localVersionNumber:"+local+",serverVersionNumber:"+remote);

        if (local<remote){
            final Loader loader = new Loader();
            loader.settingCaller(new Loader.LoaderCaller() {
                @Override
                public void Call(String filePath) {
                    installApk(filePath);
                }
            });

        loader.LoadingUriResource(uri,null);
        }
    }

    /**
     * 获取软件版本号
     *
     * @return
     */
    public static int getLocalVersionCode() {
        int versionCode = 0;
        try {
            // 获取软件版本号
            versionCode = wosPlayerApp.appContext.getPackageManager().getPackageInfo(wosPlayerApp.appContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


/**
 * 安装APK文件
 */
private void installApk(String apkLocalPath) {
    File apkfile = new File(apkLocalPath);
    if (!apkfile.exists()) {
        log.e(TAG,"install apk is not exists...");
        return;
    }
    // 通过Intent安装APK文件
	/*	Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		config.activity.startActivity(i);*/

//		System.exit(0);

    Toals.Say("install start ...");
   //Intent intent = new Intent(DisplayActivity.activityContext,  DisplayActivity.class);
    // 创建PendingIntent对象
  /*  final PendingIntent pi = PendingIntent.getActivity(DisplayActivity.activityContext, 0, intent, 0);

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.add(Calendar.SECOND, 20);
    ((AlarmManager) DisplayActivity.activityContext.getSystemService(Context.ALARM_SERVICE))
            .set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
    /*Intent intent = new Intent(DisplayActivity.activityContext,  MonitorService.class);*/
    // 创建PendingIntent对象
   /* final PendingIntent pi = PendingIntent.getService(DisplayActivity.activityContext, 0, intent, 0);
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.add(Calendar.SECOND, 10);
    ((AlarmManager) DisplayActivity.activityContext.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);*/


    Intent intent = new Intent();
    intent.setAction(RestartApplicationBroad.action);
//    intent.putExtra(RestartApplicationBroad.IS_START,false);
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.add(Calendar.SECOND, 10);

    PendingIntent pi = PendingIntent.getBroadcast(DisplayActivity.activityContext,0,intent,0);
    ((AlarmManager) DisplayActivity.activityContext.getSystemService(Context.ALARM_SERVICE))
            .set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);

    Toals.Say(calendar.getTime().toString()+",10 秒后 发送启动广播");
    log.e(TAG," execute install APK.. end progress");

    int code =  PackageUtils.install(DisplayActivity.activityContext,apkLocalPath);

    log.e(TAG," - - install requst code :"+code);

    if (code == PackageUtils.INSTALL_SUCCEEDED){

        //String commands = "adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.activity.DisplayActivity";
        ShellUtils.CommandResult cr = ShellUtils.execCommand(AdbShellCommd.commands_startApp,false,true);

        Log.e(TAG,cr.result+"");


//        //打开apk
//			String param =
//			"adb shell am start -n com.wosplayer/com.wosplayer.activity.DisplayActivity";
//			AppToSystem.execRootCmdSilent(param);
//
//        PackageUtils.startInstalledAppDetails(DisplayActivity.activityContext,packagename);
    }else{
        Log.e(TAG,apkLocalPath);

        String filane = apkLocalPath.substring(apkLocalPath.lastIndexOf("/")+1);
//        String [] param = {
//                "shell remount\n",
//                "adb shell\n",
//                "cp "+apkLocalPath+" /data/local/tmp/"+filane+"\n",
//                "chmod 777 /data/local/tmp/"+filane+"\n",
//                "pm install -r /data/local/tmp/"+filane+"\n",
//               // "rm /data/local/tmp.apk\n"
//        };
        String param[] = AdbShellCommd.getInstallAdb(apkLocalPath,filane);
        try{
            Log.e(TAG,"\n"+param[0]+param[1]+param[2]+param[3]+param[4]);
        }catch (Exception e){
            log.e(e.getMessage());
        }

        ShellUtils.CommandResult cr = ShellUtils.execCommand(param,true,true);
        Log.e(TAG,"result -------------------  "+  cr.result+"  --------------------------------------------------");
        if (cr.result == 0){
            //String p = "adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.activity.DisplayActivity\n";
            ShellUtils.CommandResult cr1 = ShellUtils.execCommand(AdbShellCommd.commands_startApp,false,true);
            Log.e(TAG," run result  "+  cr.result+" ^#");
        }
    }

}

}