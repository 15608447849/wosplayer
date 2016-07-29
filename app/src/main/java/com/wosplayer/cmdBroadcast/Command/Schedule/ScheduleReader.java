package com.wosplayer.cmdBroadcast.Command.Schedule;

import android.content.Intent;
import android.os.Bundle;

import com.wosplayer.Ui.uiBroadcast.UibrocdCastReceive;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_SYTI;
import com.wosplayer.cmdBroadcast.Command.Schedule.correlation.XmlNodeEntity;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/7/22.
 * 读取排期
 */
public class ScheduleReader {

    private static  Timer timer = null;
    private static  TimerTask timerTask = null;
    private static void startTimer(long millisecond){
        stopTimer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
            //重新读取排期
               Start();

            }
        };

        timer = new Timer();
        timer.schedule(timerTask,millisecond);
        log.i(TAG,"开始 定时任务 ,延时毫秒数:" +millisecond);
    }

    private static void stopTimer(){
        if (timerTask!=null){
            timerTask.cancel();
            timerTask = null;
        }
       if (timer!=null){
           timer.cancel();
           timer = null;
       }

        log.i(TAG,"停止 定时任务");
    }

    private static final java.lang.String TAG = ScheduleReader.class.getName();

    private static DateFormat dataFormatUtils = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()); // 格式化 时间 工具

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
     * 清理全部排期
     */
    private static void  clearScheduleMap (){
        //循环
        for (Map.Entry<String, ArrayList<XmlNodeEntity>> entry : scheduleMap.entrySet()) {
            entry.getValue().clear();
        }
    }
    /**
     * 删除指定排期
     */
    private static void deleteOnlySchedule(XmlNodeEntity schedule){
        String type = schedule.getXmldata().get("type");
        scheduleMap.get(type).remove(schedule);
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
            String type = entity.getXmldata().get("type");

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
            XmlNodeEntity entity = querySchedule();
            clearScheduleMap();
            log.i(TAG," 今天要播放的排期 :"+entity);
            boolean isExistes = false;
            if (entity != null){
                isExistes = true;
                makeTimerTask(entity);
            }
            //false 不存在任何排期
            //播放默认的图片
            //发送广播 通知视图更新
            notificationUIexcuter(isExistes,entity);
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
     * 制作定时器
     * */
    private static void makeTimerTask(XmlNodeEntity entity) {
        String type = entity.getXmldata().get("type");
        long dalay = -1;
        //如果是插播
       if (type.equals("4")){
        String timelength = entity.getXmldata().get("timelength");
           try{
               dalay = Integer.parseInt(timelength);
               dalay = dalay * 1000;
           }catch (Exception e){
               log.e(TAG,"点播解析播放时长异常:"+timelength);
               return;
           }
       }
        else
        //如果是点播
        if (type.equals("2") && !entity.getXmldata().get("allday").equals("1")){
            //如果不是全天
            //"endcron" -> "2016-08-23 23:30:00"
            String startcron = dataFormatUtils.format(new Date());//entity.getXmldata().get("startcron");
            String endcron = entity.getXmldata().get("endcron");

            dalay = getTimeMillsecondes(endcron) - getTimeMillsecondes(startcron);


        }
        else{
            //其他 在今天结束时 重新读取排期
            String currentTimeText = dataFormatUtils.format(new Date());
            String todayEndTimeText =  currentTimeText.substring(0, 11) + "23:59:59";

            dalay = getTimeMillsecondes(todayEndTimeText) - getTimeMillsecondes(currentTimeText);

        }

        if (dalay<0){
            log.e(TAG,"定时任务 执行时间 错误:"+dalay);
            return;
        }

        log.i(TAG,"延时秒数:"+dalay/1000);
        startTimer(dalay);
    }

    //通知视图更新
    private static void notificationUIexcuter(boolean isExistes, XmlNodeEntity entity) {
        //true 有排期 false 默认版面播放
        Intent intent = new Intent();
        intent.setAction(UibrocdCastReceive.action);

        if (isExistes){
            Bundle b = new Bundle();
            b.putParcelable(UibrocdCastReceive.key,entity);
            intent.putExtras(b);
        }
        wosPlayerApp.appContext.sendBroadcast(intent);
    }

    /**
     * 查询 合适排期
     */
    private static XmlNodeEntity querySchedule() throws ParseException {

        XmlNodeEntity e = null;

        //根据排期优先级
        ArrayList<XmlNodeEntity> currentArr = DetermineScheduling();
        if (currentArr==null){
            return e;
        }
        //根据最后修改时间得到唯一一个排期
        XmlNodeEntity entity = determineModificationDate(currentArr);
        if (entity==null){
            return e;
        }
        //根据时间范围判断
        boolean flag = determineTime(entity);
        if (!flag){
            deleteOnlySchedule(entity);
            //再次寻找合适目标
           e = querySchedule();
        }else{
            e = entity;
        }
        return e;
    }

    /**
     * 判断重复类型
     *      是不是 可播放的
     * @param entity
     * @return
     */
    private static boolean determineTypeOnRepeat(XmlNodeEntity entity,Date today) {
        Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.setTime(today);



        String executeExpression = entity.getXmldata().get("executeexpression");
        String []executeExpressionArray = executeExpression.split("\\s"); //空格分割


//                //如果是每天 true  "executeexpression" -> "0 0 0 * * ? 2016-2099" 每天  //第四个是* 第6个是?
                if (executeExpressionArray[3].equals("*") && executeExpressionArray[5].equals("?")){
                    return true;
                }

// 如果是每个月 判断是哪一天 再判断是不是今天 || 每月 在第 28天  "executeexpression" -> "0 0 0 28 * ? 2016-2099"
                if (!executeExpressionArray[3].equals("*")  && executeExpressionArray[4].equals("*") && executeExpressionArray[5].equals("?")){
                 //获取今天是这个月的第几天
                  int day_of_month =  todayCalendar.get(Calendar.DAY_OF_MONTH);
                    int schedule_dayNum = -1;
                    try{
                        schedule_dayNum = Integer.parseInt(executeExpressionArray[3]);
                    }catch (Exception e){
                        log.e(TAG,"重复类型排期 每月的某天播放 解析天数参数 异常:"+executeExpressionArray[3]);
                        return false;
                    }

                    if (day_of_month == schedule_dayNum){
                        return true;
                    }else{
                        return false;
                    }


                }

        //                //如果是每个年  "cronexpression" -> "* * * 19 9 ? 2016-2017"

                if (!executeExpressionArray[3].equals("*")  && !executeExpressionArray[4].equals("*") && executeExpressionArray[5].equals("?")){

                    //得到当前 月份 和当前 天数
                    int month = todayCalendar.get(Calendar.MONTH)+1;
                    int day_of_month =  todayCalendar.get(Calendar.DAY_OF_MONTH);

                    //得到排期的月份和排期的天数
                    int schedule_month = -1;
                    int schedule_dayNum = -1;
                    try{
                        schedule_month = Integer.parseInt(executeExpressionArray[4]) ;
                        schedule_dayNum = Integer.parseInt(executeExpressionArray[3]);
                    }catch (Exception e){
                        log.e(TAG,"重复类型排期 每年某月某天播放 解析天数参数 异常:"+executeExpressionArray[3]+"-"+executeExpressionArray[4]);
                        return false;
                    }
                    if (month == schedule_month && day_of_month==schedule_dayNum){
                        return true;
                    }else{
                        return false;
                    }
                }



//                //如果是每个月的 的某个星期 判断 >是不是这个星期 >再判断是不是今天|  每月 在第 4周 周三 "executeexpression" -> "0 0 0 ? * 4#4 2016-2099" //4#4 星期几#这个月的第几个星期
            if (executeExpressionArray[3].equals("?")){
                //获取 当前是这个月的第几个星期 星期几
                int week = todayCalendar.get(Calendar.WEEK_OF_MONTH);
                int day_of_week =  todayCalendar.get(Calendar.DAY_OF_WEEK);

                //获取排期规定的 第几个星期星期几
                String text = executeExpressionArray[5];
                String []textArr = text.split("#");
                if (textArr.length == 0){
                    log.e(TAG," 解析星期数异常 :"+ executeExpressionArray[5]);
                    return false;
                }
                int schedule_week = -1;
                int schedule_week_day = -1;
                try{
                    schedule_week = Integer.parseInt(textArr[1]) + 1;
                    schedule_week_day = Integer.parseInt(textArr[0]);
                }catch (Exception e){
                    log.e(TAG,"重复类型排期 每个月的第几个星期的星期几 解析天数参数 异常:"+textArr[0]+"-"+textArr[1]);
                    return false;
                }

                if (week==schedule_week && day_of_week==schedule_week_day){
                    return true;
                }else{
                    return false;
                }
            }
        return false;
    }


    /*
    *获取时间毫秒数
     */
    private static long getTimeMillsecondes(String time)
    {
        log.i(TAG,"准备转换时间:"+time);
       if(! Command_SYTI.RegexMatches(time)){
           log.e(TAG,"不正确的时间参数:"+time);
           return 0;
       };
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime dateTime  = fmt.parseDateTime(time);
        long millseconds = dateTime.getMillis();
        log.i(TAG, "转换时间成功: "+ millseconds);
        return millseconds;
    }

    /**
     * 判断最后修改时间
     * @param current
     */
    private static XmlNodeEntity determineModificationDate(ArrayList<XmlNodeEntity> current) {

        XmlNodeEntity entity = null;
        Collections.sort(current, new Comparator<XmlNodeEntity>() {
            @Override
            public int compare(XmlNodeEntity lhs, XmlNodeEntity rhs) {
                long a = getTimeMillsecondes(lhs.getXmldata().get("modifydt"));
                long b = getTimeMillsecondes(rhs.getXmldata().get("modifydt"));
                return a-b>0 ? -1:1;  //-1代表前者小，0代表两者相等，1代表前者大。
            }
        });

        for (int i = 0;i<current.size();i++){
            log.i(TAG,"排序后数组情况:"+current.get(i).getXmldata().get("modifydt"));
        }

        log.i(TAG,"------------------------------------------------------");
        entity = current.get(0);
        return entity;
    }

    /**
     * 停止
     */
    private static void Stop() {
        //清楚所有排期
        clearScheduleMap();
    }

    /**
     * 判断排期类型
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


    /**
     * 判断时间范围
     * @param current
     * @throws ParseException
     */
    private static boolean determineTime(XmlNodeEntity current) throws ParseException {

        //如果是默认排期 直接返回
        String type = current.getXmldata().get("type");
        if (type.equals("5")){
            return true;
        }

        String schedule_startTimeText = current.getXmldata().get("startcron");
        String schedule_endTimeText = current.getXmldata().get("endcron");


        if ( !Command_SYTI.RegexMatches(schedule_startTimeText) ||  !Command_SYTI.RegexMatches(schedule_endTimeText)){

           log.e(TAG,"排期开始时间 或者 结束时间 错误");
            //不符合要求 删除这个类型的排期所在数组 ,重新查询下一个排期
            return false;
        }

        //换成 日历类型
        Date startTime = dataFormatUtils.parse(schedule_startTimeText);
        Date endTime = dataFormatUtils.parse(schedule_endTimeText);


        //获取当前的时间
        String currentTimeText =  dataFormatUtils.format(new Date());
        Date currentData = dataFormatUtils.parse(currentTimeText);


        log.i(TAG,"当前时间:"+currentTimeText+";排期 :"+schedule_startTimeText+"---"+schedule_endTimeText);
        // 判断 当前的时间 是不是 符合: 在开始 - 结束 的范围内
        currentData.before(startTime);


       if (currentData.after(startTime) && currentData.before(endTime)){
           log.i(TAG,"在当前时间存在有效排期");

           if (type.equals("3")){

              return determineTypeOnRepeat(current,currentData);
           }

           return true;
       }
        return false;
    }






}
