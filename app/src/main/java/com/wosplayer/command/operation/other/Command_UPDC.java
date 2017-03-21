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
import com.wosplayer.tool.SdCardTools;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/8/1.
 */
public class Command_UPDC implements iCommand {

    private static final String TAG = "终端升级";
    private HttpUtils http = null;

    @Override
    public void execute(Activity activity, String param) {

        SystemConfig config = SystemConfig.get().read();
        String terminalNo = config.GetStringDefualt("terminalNo", "");
        String tepPath = config.GetStringDefualt("updatepath", "");
        getRemoteVersionCode(activity, param, terminalNo, tepPath);
    }

    /**
     * 获取远程版本号
     */
    private void getRemoteVersionCode(final Activity activity, final String apkVersionInfoUri, final String terminalNo, final String tepPath) {
        if (http == null) {
            http = new HttpUtils();
        }
        http.send(HttpRequest.HttpMethod.GET,
                apkVersionInfoUri,
                new RequestCallBack<String>() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        parseRemoteInfo(activity, responseInfo.result, terminalNo, tepPath);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        PlayApplication.sendMsgToServer("UPDC:" + terminalNo + "#[获取版本信息失败:" + msg + "]#[URL:" + apkVersionInfoUri + "]");
                    }
                });
    }

    /**
     * 解析远程版本信息
     */
    private void parseRemoteInfo(Activity activity, String info, String terminalNo, String tepPath) {
        try {
            Logs.i(TAG, "版本升级文本信息:\n" + info + "\n");
            InputStream inStream = new ByteArrayInputStream(info.getBytes());
            // 创建saxReader对象
            SAXReader reader = new SAXReader();
            Document document = reader.read(inStream);
            //获取根节点元素对象
            Element antivirus = document.getRootElement();
            String url = antivirus.element("path").getText();

            ShellUtils.CommandResult result = ShellUtils.execCommand("chmod 777 " + tepPath, true);
            if (result.result == 0) {
                //去下载
                settingLoadingApkParam(activity, url, terminalNo, tepPath);
            } else {
                PlayApplication.sendMsgToServer("UPDC:" + terminalNo + "#[本地路径:" + tepPath + ",权限不足]");
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void settingLoadingApkParam(Activity activity, String url, String terminalNo, String tepPath) {
        //下载升级包
        Intent intent = new Intent(PlayApplication.appContext, DownloadManager.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putInt(DownloadManager.KEY_TYPE, DownloadManager.KEY_TYPE_UPDATE_APK);
        bundle.putString(DownloadManager.KEY_TERMINAL_NUM, terminalNo);//终端id
        bundle.putString(DownloadManager.KEY_SAVE_PATH, tepPath);//临时路径
        bundle.putString(DownloadManager.KEY_TASK_SINGLE, url);//url
        bundle.putString(DownloadManager.KEY_ALIAS, geneLocalApkFialeNme());//文件名
        intent.putExtras(bundle);
        activity.startService(intent);
    }

    private String geneLocalApkFialeNme() {
        String str = "Aiplay_u.apk";
        try {
            String date = new SimpleDateFormat("yyyyMMdd_HHmmss_").format(new Date());
            str = date + str;
        } catch (Exception e) {
        }
        return str;
    }
}
