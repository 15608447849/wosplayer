package com.wosplayer.command.operation.other;

import android.content.Intent;
import android.os.Bundle;

import com.wosplayer.app.PlayApplication;
import com.wosplayer.command.kernal.iCommand;
import com.wosplayer.download.kernal.DownloadManager;

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
        Intent intent = new Intent(PlayApplication.appContext, DownloadManager.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putInt(DownloadManager.KEY_TYPE, DownloadManager.KEY_TYPE_FFBK);
        bundle.putString(DownloadManager.KEY_TERMINAL_NUM, PlayApplication.config.GetStringDefualt("terminalNo",""));
        bundle.putString(DownloadManager.KEY_SAVE_PATH, PlayApplication.config.GetStringDefualt("fudianpath", ""));//保存路径
        bundle.putString(DownloadManager.KEY_ALIAS,"resource.xml");//别名
        bundle.putString(DownloadManager.KEY_TASK_SINGLE, taskUrl);//url
        intent.putExtras(bundle);
        PlayApplication.appContext.startService(intent);
    }
}
