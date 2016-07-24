package com.wosplayer.Ui.performer;

import android.nfc.Tag;

import com.wosplayer.app.log;
import com.wosplayer.broadcast.Command.Schedule.correlation.XmlNodeEntity;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2016/7/24.
 * 单例
 */

public class UiExcuter {
    private static final java.lang.String TAG = UiExcuter.class.getName();
    private static UiExcuter uiExcuter = null;
    private UiExcuter() {
        log.i(TAG,"ui excuter create");
    }

    public static UiExcuter getInstancs(XmlNodeEntity schedule){
        if (uiExcuter == null){
            uiExcuter = new UiExcuter();
        }
        uiExcuter.setPlaySchedule(schedule);
        return uiExcuter;
    }

    private static ReentrantLock lock = new ReentrantLock();
    private XmlNodeEntity schedule = null;
    //设置排期
    private void setPlaySchedule(XmlNodeEntity schedule){

        clearPlaySchedule();
       this.schedule = schedule;
    }
    //清理排期
    private void clearPlaySchedule() {
        if (schedule!=null){
            schedule = null;
        }
    }


}
