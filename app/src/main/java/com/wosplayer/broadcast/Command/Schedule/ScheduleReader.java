package com.wosplayer.broadcast.Command.Schedule;

import com.wosplayer.app.log;
import com.wosplayer.broadcast.Command.Schedule.correlation.XmlNodeEntity;

import java.util.ArrayList;

/**
 * Created by user on 2016/7/22.
 * 读取排期
 */
public class ScheduleReader {

    private static final java.lang.String TAG = ScheduleReader.class.getName();

    private static String getPlayType(String t) {
        if (t.equals("4"))
            return "插播";
        if (t.equals("3"))
            return "重复播放";
        if (t.equals("2"))
            return "点播";
        if (t.equals("1"))
            return "轮播";
        if (t.equals("5")){

        }
        return "默认播放";

    }
    //获取所有的排期
    private void getAllSchedule(){

        ArrayList<XmlNodeEntity> allScheduleList = XmlNodeEntity.GetAllNodeInfo();

        if (allScheduleList==null){
            log.e(TAG," not fount schedule.");
            return;
        }
        log.i(TAG,"all schedule :" + allScheduleList);
    }

    private void filterScheduleList(ArrayList<XmlNodeEntity> allScheduleList){

        //获取当前的 时间 的毫秒数

        //循环排期

        //获取排期的 结束时间 ; 如果 结束时间<当前时间 过滤掉




    }



}
