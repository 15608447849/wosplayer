package com.wosplayer.download.kernal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.app.Logs;
import com.wosplayer.command.operation.schedules.ScheduleReader;
import com.wosplayer.command.operation.schedules.ScheduleSaver;

/**
 * 数据下载完成
 * Created by user on 2016/7/21.
 */
public class DownloadCompleterBroadcast extends BroadcastReceiver {
    private static String TAG = "数据下载完成通知";
    public static final String action = "com.complete.taskList";
    @Override
    public void onReceive(Context context, Intent intent) {

        //收到通知
        Logs.i(TAG," =============执行排期数据保存,排期数据读取======== ");
        //序列化数据
        ScheduleSaver.Serialize();
        Logs.i("------------------------------------------ 序列化保存数据完成,准备获取本地排期 请稍后-------------------------------------------------");
        //执行 数据读取者
        try {
            ScheduleReader.clear();
            ScheduleReader.Start(false);
        } catch (Exception e) {
          Logs.e(TAG," 下载数据完成 ->序列化数据完成 -> 开始读取数据时异常:" + e.getMessage());
        }
        //通知 时间控制台
    }
}
