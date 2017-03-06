package com.wosplayer.command.operation.other;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.app.SystemConfig;
import com.wosplayer.command.operation.interfaces.iCommand;
import com.wosplayer.service.logUploadService;

/**
 * Created by user on 2016/8/1.
 *
 */
public class Command_UPLG implements iCommand {
    @Override
    public void execute(Activity activity, String param) {

        //获取 uri
        String uri = param;
        Logs.i("上传错误日志","服务器地址: "+ uri);
        if (uri==null || uri.equals("")) return;
        if (!uri.contains("http://")){
            return;
        }

        //开启 日志上传服务
        Intent logIntent = new Intent(PlayApplication.appContext,logUploadService.class);
        Bundle b = new Bundle();
        b.putString(logUploadService.uriKey,uri);
        b.putString("terminalNo", SystemConfig.get().GetStringDefualt("terminalNo","0000"));
        logIntent.putExtras(b);
        activity.startService(logIntent);

    }
}
