package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.iCommand;
import com.wosplayer.loadArea.excuteBolock.Loader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Calendar;

import cn.trinea.android.common.util.PackageUtils;

/**
 * Created by user on 2016/8/1.
 */
public class Command_UPDC implements iCommand {
    @Override
    public void Execute(String param) {
        log.i("更新app,远程版本号:"+param);

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
                        log.i("upload version info:"+responseInfo.result);
                        parseRemoteInfo(responseInfo.result);
                    }

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        log.i("upload version info:"+msg);
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
        compareVersion(Integer.parseInt(code),path);//比较版本
    }

    /**
     * 比较版本号
     */
    private void compareVersion(int remoteVersion, String uri) {
        int local = getLocalVersionCode();
        int remote = remoteVersion;

        log.i("upload  LocalVersion :"+ local+" remoteVersion:"+remote);
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
        log.e("install apk is not exists...");
        return;
    }
    // 通过Intent安装APK文件
	/*	Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		config.activity.startActivity(i);*/

//		System.exit(0);

   Intent intent = new Intent(DisplayActivity.activityContext,  DisplayActivity.class);
    // 创建PendingIntent对象
    final PendingIntent pi = PendingIntent.getActivity(DisplayActivity.activityContext, 0, intent, 0);

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.add(Calendar.SECOND, 20);
    ((AlarmManager) DisplayActivity.activityContext.getSystemService(Context.ALARM_SERVICE))
            .set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
    /*Intent intent = new Intent(DisplayActivity.activityContext,  MonitorService.class);
    // 创建PendingIntent对象
    final PendingIntent pi = PendingIntent.getService(DisplayActivity.activityContext, 0, intent, 0);
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.add(Calendar.SECOND, 10);
    ((AlarmManager) DisplayActivity.activityContext.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);*/








    log.e("execute install APK.. end progress");
    PackageUtils.install(DisplayActivity.activityContext,apkLocalPath);
}

}