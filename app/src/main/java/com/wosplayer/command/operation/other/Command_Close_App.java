package com.wosplayer.command.operation.other;

import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.app.SystemConfig;
import com.wosplayer.command.kernal.iCommand;
/**
 * Created by user on 2016/7/30.
 */
public class Command_Close_App implements iCommand {
    @Override
    public void Execute(String param) {
        try {
            if (param.equals("false")){
                return;
            }
            //设置系统配置-是否监听的值
            SystemConfig.get().putOr("watchValue","1").save();
            Logs.i("===== 强制停止播放器运行 =====");
            PlayApplication.stopCommunicationService(null);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
