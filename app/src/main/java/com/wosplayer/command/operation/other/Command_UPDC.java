package com.wosplayer.command.operation.other;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.AdbCommand;
import com.wosplayer.app.DisplayerApplication;
import com.wosplayer.app.Logs;
import com.wosplayer.command.kernal.iCommand;
import com.wosplayer.download.kernal.DownloadManager;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

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
        Logs.i(TAG,"更新app,远程版本号:"+param);
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
                        Logs.i(TAG,"upload version info:"+responseInfo.result);
                        parseRemoteInfo(responseInfo.result);
                    }

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Logs.i(TAG,"upload version info:"+msg);
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

        Logs.i(TAG,"upload  LocalVersion :"+ local+" remoteVersion:"+remote);


        DisplayerApplication.sendMsgToServer("terminalNo:"+ DisplayerApplication.config.GetStringDefualt("terminalNo","0000")+",localVersionNumber:"+local+",serverVersionNumber:"+remote);

        if (local<remote){

            isUploading =false;
            if (broad!=null){
                try {
                    DisplayerApplication.appContext.unregisterReceiver(broad);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                broad=null;
            }
            broad = new UPDCbroad(this);
            IntentFilter filter=new IntentFilter();
            filter.addAction(UPDCbroad.ACTION);
            DisplayerApplication.appContext.registerReceiver(broad, filter); //只需要注册一次
            //发送 远程升级
            Intent intent = new Intent(DisplayerApplication.appContext, DownloadManager.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = new Bundle();
            bundle.putInt(DownloadManager.KEY_TYPE, DownloadManager.KEY_TYPE_UPDATE_APK);
            bundle.putString(DownloadManager.KEY_TERMINAL_NUM, DisplayerApplication.config.GetStringDefualt("terminalNo",""));
            bundle.putString(DownloadManager.KEY_SAVE_PATH, DisplayerApplication.config.GetStringDefualt("basepath", ""));
            bundle.putString(DownloadManager.KEY_TASK_SINGLE,uri);
            intent.putExtras(bundle);
            DisplayerApplication.appContext.startService(intent);
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
            versionCode = DisplayerApplication.appContext.getPackageManager().getPackageInfo(DisplayerApplication.appContext.getPackageName(), 0).versionCode;
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
        Logs.e(TAG,"install apk is not exists >>> "+apkLocalPath +" <<<");
        return;
    }
    if (isUploading){
        return;
    }
    isUploading = true;
    Logs.e(TAG," 安装升级包并且结束程序,upadte.apk 路径 :"+apkLocalPath);
    int code =  PackageUtils.install(DisplayActivity.activityContext,apkLocalPath);
    Logs.e(TAG," 安装结果 返回值 : "+code);
    if (code == PackageUtils.INSTALL_SUCCEEDED){
        ShellUtils.execCommand(AdbCommand.commands_startApp,false,false);
    }
    /*else{
        String filane = apkLocalPath.substring(apkLocalPath.lastIndexOf("/")+1);
        String param[] = AdbShellCommd.getInstallAdb(apkLocalPath,filane);
        Log.e(TAG,"shell 命令安装 >>> \n"+param[0]+param[1]+param[2]+param[3]+param[4]);
        ShellUtils.execCommand(param,true,true);
        ShellUtils.execCommand(AdbShellCommd.commands_startApp,false,true);
        //String p = "adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.activity.DisplayActivity\n";
    }*/

}























}