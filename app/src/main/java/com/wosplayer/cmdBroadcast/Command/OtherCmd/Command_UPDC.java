package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.AdbShellCommd;
import com.wosplayer.app.WosApplication;
import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.Command.iCommand;
import com.wosplayer.loadArea.kernal.loaderManager;
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
            e.printStackTrace();
        }
        compareVersion(Integer.parseInt(code),path);//比较版本
    }
    private UPDCbroad broad;
    /**
     * 比较版本号
     */
    private void compareVersion(int remoteVersion, String uri) {
        int local = getLocalVersionCode();
        int remote = remoteVersion;

        log.i(TAG,"upload  LocalVersion :"+ local+" remoteVersion:"+remote);


        WosApplication.sendMsgToServer("terminalNo:"+ WosApplication.config.GetStringDefualt("terminalNo","0000")+",localVersionNumber:"+local+",serverVersionNumber:"+remote);

        if (local<remote){

            isUploading =false;
            if (broad!=null){
                try {
                    WosApplication.appContext.unregisterReceiver(broad);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                broad=null;
            }
            broad = new UPDCbroad(this);
            IntentFilter filter=new IntentFilter();
            filter.addAction(UPDCbroad.ACTION);
            WosApplication.appContext.registerReceiver(broad, filter); //只需要注册一次

            //发送 远程升级
            Intent intent = new Intent(WosApplication.appContext, loaderManager.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = new Bundle();
            bundle.putString("terminalNo", WosApplication.config.GetStringDefualt("terminalNo","0000"));
            bundle.putString("savepath", WosApplication.config.GetStringDefualt("basepath", "/sdcard/mnt/playlist"));
            bundle.putString("UPDC",uri);
            intent.putExtras(bundle);
            WosApplication.appContext.startService(intent);
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
            versionCode = WosApplication.appContext.getPackageManager().getPackageInfo(WosApplication.appContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

private boolean isUploading = false;
/**
 * 安装APK文件
 */
public void installApk(String apkLocalPath) {
    File apkfile = new File(apkLocalPath);
    if (!apkfile.exists()) {
        log.e(TAG,"install apk is not exists >>> "+apkLocalPath +" <<<");
        return;
    }
    if (isUploading){
        return;
    }
    isUploading = true;

    Intent intent = new Intent();
    intent.setAction(RestartApplicationBroad.action);
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.add(Calendar.SECOND, 10);
    PendingIntent pi = PendingIntent.getBroadcast(DisplayActivity.activityContext,0,intent,0);
    ((AlarmManager) DisplayActivity.activityContext.getSystemService(Context.ALARM_SERVICE))
            .set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);

    log.e(TAG," 执行 install APK.. 并且结束程序 ,\n apk path :"+apkLocalPath);

    int code =  PackageUtils.install(DisplayActivity.activityContext,apkLocalPath);

    log.e(TAG," 安装结果 返回值 : "+code);

    if (code == PackageUtils.INSTALL_SUCCEEDED){

        //String commands = "adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.activity.DisplayActivity";
        ShellUtils.execCommand(AdbShellCommd.commands_startApp,false,true);
//        //打开apk
//			String param =
//			"adb shell am start -n com.wosplayer/com.wosplayer.activity.DisplayActivity";
//			AppToSystem.execRootCmdSilent(param);
//        PackageUtils.startInstalledAppDetails(DisplayActivity.activityContext,packagename);
    }else{

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

        Log.e(TAG,"shell 命令安装 >>> \n"+param[0]+param[1]+param[2]+param[3]+param[4]);
        ShellUtils.execCommand(param,true,true);
        ShellUtils.execCommand(AdbShellCommd.commands_startApp,false,true);
        //String p = "adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.activity.DisplayActivity\n";
    }

}























}