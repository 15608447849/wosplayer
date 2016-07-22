package com.wosplayer.broadcast.Command.Schedule;

import com.wosplayer.app.log;
import com.wosplayer.broadcast.Command.Schedule.correlation.XmlNodeEntity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/7/22.
 * 读取排期
 */
public class ScheduleReader {

    private static final java.lang.String TAG = ScheduleReader.class.getName();

    private static Map<String,ArrayList<XmlNodeEntity>> scheduleMap = Collections.synchronizedMap(new HashMap<String, ArrayList<XmlNodeEntity>>());
    static {
        //轮播排期
        scheduleMap.put("1",new ArrayList<XmlNodeEntity>());
        //点播
        scheduleMap.put("2",new ArrayList<XmlNodeEntity>());
        //重复
        scheduleMap.put("3",new ArrayList<XmlNodeEntity>());
        //插播
        scheduleMap.put("4",new ArrayList<XmlNodeEntity>());
        //默认
        scheduleMap.put("5",new ArrayList<XmlNodeEntity>());
    }

    /**
     * 清理排期
     */
    private static void  clearScheduleMap (){
        //循环
        for (Map.Entry<String, ArrayList<XmlNodeEntity>> entry : scheduleMap.entrySet()) {
            entry.getValue().clear();
        }
    }

    /**
     * 添加排期
     */
     private static boolean addScheduleToMap(String type,XmlNodeEntity entity){
         //判断是否包含类型
        if (scheduleMap.containsKey(type)){
             scheduleMap.get(type).add(entity);
            return true;
         }
         return false;

     }

    //获取所有的排期
    private static ArrayList<XmlNodeEntity> getAllSchedule(){

        ArrayList<XmlNodeEntity> allScheduleList = XmlNodeEntity.GetAllNodeInfo();

        if (allScheduleList==null){
            log.e(TAG," not fount schedule.");
            return null;
        }
        log.i(TAG,"all schedule :" + allScheduleList);
        return allScheduleList;
    }

    /**
     *
     *    获取 播放的排期
     * @param allScheduleList
     */
    private static void filterScheduleList(ArrayList<XmlNodeEntity> allScheduleList){

        //分组
        for (XmlNodeEntity entity : allScheduleList){
            String type = entity.getXml().get("type");

           if(addScheduleToMap(type,entity)){
               log.i(TAG,"type" +"=="+type+"[1=轮播,2=点播,3=重复,4=插播,5=重复],添加success");
           }else{
               log.i(TAG,"type" +"=="+type+"[1=轮播,2=点播,3=重复,4=插播,5=重复],添加failure");
           }
        }
    }









    private static ReentrantLock lock = new ReentrantLock();

    public static void Start(){

        try {
            lock.lock();
            Stop();
           ArrayList<XmlNodeEntity> list =  getAllSchedule();
           filterScheduleList(list);

           //根据排期优先级
            ArrayList<XmlNodeEntity> current = DetermineScheduling();
            //根据时间范围判断
            determineTime(current);









        }
        catch (Exception e)
        {
            log.e(TAG," 执行读取排期错误:"+e.getMessage());
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * 停止
     */
    private static void Stop() {
        //清楚所有排期
        clearScheduleMap();
    }

    /**
     * 判断排期
     */
    private static ArrayList<XmlNodeEntity> DetermineScheduling() {
        //优先级 : 点播 2> 重复 3> 插播 4> 轮播 1> 默认5

        if (scheduleMap.get("2").size()>0){
            return scheduleMap.get("2");
        }
        if(scheduleMap.get("3").size()>0){
            return scheduleMap.get("3");
        }
        if (scheduleMap.get("4").size()>0){
            return scheduleMap.get("4");
        }
        if(scheduleMap.get("1").size()>0){
            return scheduleMap.get("1");
        }
        if(scheduleMap.get("5").size()>0){
            return scheduleMap.get("5");
        }
        return null;
    }


    private static void determineTime(ArrayList<XmlNodeEntity> current) throws ParseException {

        //获取当天的开始时间-结束时间
        DateFormat dataFormatUtils = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        String dateText =  dataFormatUtils.format(new Date());

        String beginText = dateText.substring(0, 11) + "00:00:00";//start time text
        String endText =  dateText.substring(0, 11) + "23:59:59";//end time text

        Calendar today_begin =  Calendar.getInstance();
                today_begin.setTime(dataFormatUtils.parse(beginText));
        Calendar today_end =  Calendar.getInstance();
                today_end.setTime(dataFormatUtils.parse(endText));

        // 判断 当前排期中的时间 是不是 符合:   1.在当天时间范围内  2.排期开始时间


    }






}
