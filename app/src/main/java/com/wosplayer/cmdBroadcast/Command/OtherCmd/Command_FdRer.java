package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.content.Intent;
import android.os.Bundle;

import com.wosplayer.app.WosApplication;
import com.wosplayer.cmdBroadcast.Command.iCommand;
import com.wosplayer.loadArea.kernal.loaderManager;

import java.util.ArrayList;

/**
 * Created by 79306 on 2017/2/21.
 * FFBK:url
 */
public class Command_FdRer implements iCommand {
    @Override
    public void Execute(String param) {
        if (param.isEmpty()) return;
        sendloadTask(param);
    }
    /**
     * 通知 开始 下载 资源
     */
    private void sendloadTask(String taskUrl) {
        Intent intent = new Intent(WosApplication.appContext, loaderManager.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putInt(loaderManager.KEY_TYPE,loaderManager.KEY_TYPE_FFBK);
        bundle.putString(loaderManager.KEY_TERMINAL_NUM, WosApplication.config.GetStringDefualt("terminalNo",""));
        bundle.putString(loaderManager.KEY_SAVE_PATH, WosApplication.config.GetStringDefualt("fudianpath", ""));//保存路径
        bundle.putString(loaderManager.KEY_ALIAS,"resource.xml");//别名
        bundle.putString(loaderManager.KEY_TASK_SINGLE, taskUrl);//url
        intent.putExtras(bundle);
        WosApplication.appContext.startService(intent);
    }
}
