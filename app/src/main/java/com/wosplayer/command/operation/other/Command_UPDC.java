package com.wosplayer.command.operation.other;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wosplayer.app.AppTools;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.app.Logs;
import com.wosplayer.app.SystemConfig;
import com.wosplayer.command.operation.interfaces.iCommand;
import com.wosplayer.download.kernal.DownloadManager;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/8/1.
 */
public class Command_UPDC implements iCommand {

    private  static final String TAG = "终端升级";
    @Override
    public void execute(Activity activity, String param) {
        Logs.i(TAG,"更新app: "+param);
        getRemoteVersionCode(activity,param);
    }

    /**
     * 获取远程版本号
     */
    private void getRemoteVersionCode(final Activity activity,String uri) {
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
                        Logs.i(TAG,"版本升级文本信息:"+responseInfo.result);
                        parseRemoteInfo(activity,responseInfo.result);
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Logs.i(TAG,"升级包下载失败:" + msg);
                    }
                });
    }

    /**
     * 解析远程版本信息
     */
    private void parseRemoteInfo(Activity activity,String info){

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

        try {
            int code =Integer.parseInt(antivirus.element("code").getText());
            String url = antivirus.element("path").getText();
            compareVersion(activity,code,url);//比较版本
        } catch (NumberFormatException e) {
            PlayApplication.sendMsgToServer("UPDC:升级终端标识码错误,请输入数字(1-99)");
        }
    }

    /**
     * 比较版本号
     */
    private void compareVersion(Activity activity,int remoteVersion, String uri) {
        int local = getLocalVersionCode(activity);
        int remote = remoteVersion;
        Logs.i(TAG,"本地版本号:"+ local+" ,升级包版本号:"+remote);
        if (local>=remote){
           return;
        }
        SystemConfig config = SystemConfig.get().read();
        String tepPath = config.GetStringDefualt("updatepath", "");
        String terminalNo = config.GetStringDefualt("terminalNo", "");
        ShellUtils.CommandResult result = ShellUtils.execCommand("chmod 777 "+tepPath,true);
        if (result.result == 0) {
            //下载升级包
            Intent intent = new Intent(PlayApplication.appContext, DownloadManager.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = new Bundle();
            bundle.putInt(DownloadManager.KEY_TYPE, DownloadManager.KEY_TYPE_UPDATE_APK);
            bundle.putString(DownloadManager.KEY_TERMINAL_NUM,terminalNo );
            bundle.putString(DownloadManager.KEY_SAVE_PATH, tepPath);
            bundle.putString(DownloadManager.KEY_TASK_SINGLE, uri);
            intent.putExtras(bundle);
            activity.startService(intent);
        }
        else{
            Logs.e(TAG,"无法发送文件到下载服务,路径:"+tepPath+"无法执行权限设置.");
        }
    }
    /**
     * 获取软件版本号
     *
     * @return
     */
    public static int getLocalVersionCode(Activity activity) {
         // 获取软件版本号
        return AppTools.getAppVersion(activity);
    }
}










//    Intent intent = new Intent();
//                    intent.setAction(UPDCbroad.ACTION);
//                    Bundle bundle = new Bundle();
//                    bundle.putString(UPDCbroad.key,lpath);
//                    intent.putExtras(bundle);
//                    getApplicationContext().sendBroadcast(intent);



//    private UPDCbroad broad;
//try {
//        String spackagename = antivirus.element("packagename").getText();
//
//        if (spackagename != null && !spackagename.equals("")) {
//        packagename = spackagename;
//        }
//        }catch (Exception e){
//        e.printStackTrace();
//        }
//    private String packagename = "com.wosplayer";

//        DisplayerApplication.sendMsgToServer("terminalNo:"+ DisplayerApplication.config.GetStringDefualt("terminalNo","0000")+","+"localVersionNumber:"+local+",serverVersionNumber:"+remote);
//            isUploading =false;
//            if (broad!=null){
//                try {
//                    DisplayerApplication.appContext.unregisterReceiver(broad);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                broad=null;
//            }
//            broad = new UPDCbroad(this);
//            IntentFilter filter=new IntentFilter();
//            filter.addAction(UPDCbroad.ACTION);
//            DisplayerApplication.appContext.registerReceiver(broad, filter); //只需要注册一次