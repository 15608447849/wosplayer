package com.wosplayer.command.operation.other;

import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.command.operation.interfaces.iCommand;

/**
 * Created by user on 2016/7/30.
 */
public class Command_Reboot_App implements iCommand {
    @Override
    public void Execute(String param) {
        try {

            if ( DisplayActivity.activityContext!=null){
                DisplayActivity.activityContext.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
