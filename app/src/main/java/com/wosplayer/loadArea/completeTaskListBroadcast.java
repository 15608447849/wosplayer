package com.wosplayer.loadArea;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wos.Toals;
import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.Command.Schedule.ScheduleReader;
import com.wosplayer.cmdBroadcast.Command.Schedule.ScheduleSaver;

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
        Toals.Say("序列化保存数据完成,准备本地获取排期");
        //执行 数据读取者
        ScheduleReader.Start();
        //通知 时间控制台
    }
}
