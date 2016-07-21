package com.wosplayer.loadArea;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.app.log;
import com.wosplayer.broadcast.Command.Schedule.ScheduleSaver;

/**
 * Created by user on 2016/7/21.
 */
public class completeTaskListBroadcast extends BroadcastReceiver {
    public static final String action = "com.complete.taskList";
    @Override
    public void onReceive(Context context, Intent intent) {

        //收到通知
        log.i(" 下载数据成功 ");
        //序列化数据
        ScheduleSaver.Serialize();

        //执行 数据读取者


    }
}
