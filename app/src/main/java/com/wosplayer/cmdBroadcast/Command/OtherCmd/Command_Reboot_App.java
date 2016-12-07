package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.Command.iCommand;

/**
 * Created by user on 2016/7/30.
 */
public class Command_Reboot_App implements iCommand {
    @Override
    public void Execute(String param) {
        try {
            log.e("重启 application");

       /* Intent intent1 = new Intent();
        intent1.setClass(DisplayActivity.activityContext, DisplayActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        DisplayActivity.activityContext.startActivity(intent1);

        System.exit(0);*/
            if ( DisplayActivity.activityContext!=null){
                DisplayActivity.activityContext.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
