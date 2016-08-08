package com.wosplayer.cmdBroadcast.Command.Schedule;

import android.content.Intent;
import android.os.Bundle;

import com.wos.Toals;
import com.wosplayer.Ui.performer.UiExcuter;
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

    private static String PreUUKS = null;//上一个uuks

    private static  Timer timer = null;
    private static  TimerTask timerTask = null;

    private static Timer notToTimer = null;// 未到时间的定时任务
    private static TimerTask notToTimeTask = null;


    public static void clear(){
        PreUUKS = null;
    }


    private static void startTimer(long millisecond,boolean isNotTimer){
        log.i(TAG," 定时器时间:"+millisecond+" , 是否延时任务:"+ isNotTimer);

        try {
            stopTimer(isNotTimer);//停止所有定时任务
        } catch (Exception e) {
            log.e(TAG,"停止 定时器 err:"+ e.getCause()+";"+e.getMessage());
            return;
        }
        log.i(TAG,"清空定时器 gogogo");
        if (isNotTimer){
            //未到时间的任务
            notToTimeTask = new TimerTask() {
                @Override
                public void run() {
                    //重新读取排期
                    Toals.Say("一个延时任务到时间了,开始执行");
                    log.e(TAG,"一个延时任务到时间了,开始执行");
                    PreUUKS = null;
                    Start(false);
                }
            };
            //未到时间的定时任务
            notToTimer = new Timer();
            notToTimer.schedule(notToTimeTask,millisecond);
            log.i(TAG,"开始 未到时间 的 定时任务 ,延时毫秒数:" +millisecond);
        }else{
            //在当前时间段的任务
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    //重新读取排期
                    Toals.Say("一个定时任务到时间了,开始执行");
                    log.e(TAG,"一个定时任务到时间了,开始执行");
                    PreUUKS = null;
                    Start(false);
                }
            };
            timer = new Timer();
            timer.schedule(timerTask,millisecond);
            log.i(TAG,"开始已经在当前段时间 的 定时任务 ,延时毫秒数:" +millisecond);
        }

        Toals.Say("时间器任务,延时秒数:" +millisecond/1000);

    }

    private static void stopTimer(boolean isNotToTimer){
        if (isNotToTimer){
            if(notToTimeTask!=null){
                notToTimeTask.cancel();
                notToTimeTask=null;
            }
            if (notToTimer!=null){
                notToTimer.cancel();
                notToTimer = null;
            }
            log.i(TAG,"停止 延时 任务");
        }

        if (timerTask!=null){
            timerTask.cancel();
            timerTask = null;
        }
           if (timer!=null){
               timer.cancel();
               timer = null;
           }
        Toals.Say("停止 所有 定时任务");
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
            log.i(TAG,"清理:" + entry.getKey()+ ">" + entry.getValue().size());
            entry.getValue().clear();
        }
    }
    /**
     * 删除指定排期
     */
    private static void deleteOnlySchedule(XmlNodeEntity schedule){
        String type = schedule.getXmldata().get("type");
        log.e(TAG,"删除:"+schedule.getXmldata().get("summary")+",类型:"+type);
        scheduleMap.get(type).remove(schedule);
        log.i(TAG,scheduleMap.toString());
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
            String summary = entity.getXmldata().get("summary");
            boolean flag = addScheduleToMap(type,entity);
           if(flag){
               log.i(TAG,"summary:"+summary+",type" +"=="+type+"[1=轮播,2=点播,3=重复,4=插播,5=重复],添加success");
           }else{
               log.e(TAG,"summary:"+summary+",type" +"=="+type+"[1=轮播,2=点播,3=重复,4=插播,5=重复],添加failure");
           }
        }
    }

    private static ReentrantLock lock = new ReentrantLock();
    private static XmlNodeEntity currentEntity = null;
    public static void Start(boolean iserr){
        try {
            lock.lock();
            if (!iserr){
            //true 上次读取排期时 读取到一个错误的排期 再次尝试
            ArrayList<XmlNodeEntity> list =  getAllSchedule();
            //判断当前uuks 是否和上一个相同
            if(list==null || list.size()==0){
                log.e(TAG," 无排期 ");
                return;
            }else{
                String uuks = list.get(0).getXmldata().get("uuks");
                if (uuks==null || uuks.equals("") || uuks.length()==0){
                    log.e(TAG,"reader ,uuks is null");
                    return;
                }
                if (PreUUKS!=null){
                    if (PreUUKS.equals(uuks)){
                        log.e(TAG," 读取排期 uuks is same :"+ uuks+",UUKS:"+ PreUUKS);
                        return;
                    }
                }
                //执行到这里 代表 上一个uuks 不存在或者 上一个uuks 和当前uuks 不相同
                PreUUKS = uuks;
            }
            log.i(TAG," UUKS IS NOT SAME, start reader");
            Stop();
            log.i(TAG,"全部排期数量:"+list.size());
            Toals.Say("全部排期数量:"+list.size());

            filterScheduleList(list);
            log.i(TAG,"分组全部排期完成"+scheduleMap.toString());
            }


            XmlNodeEntity entity = querySchedule();
            currentEntity = entity;

            if (entity==null){
                log.e(TAG," 空排期 ");
                UiExcuter.getInstancs().StopExcuter();
                return;
            }

           /* clearScheduleMap();
            log.i(TAG,"清理完全部排期");*/

            log.i(TAG,"今天要播放的排期 type:"+entity.getXmldata().get("type")+"name:"+entity.getXmldata().get("summary"));
            Toals.Say("当前排期的类型:"+entity.getXmldata().get("type")+"最后修改时间:"+entity.getXmldata().get("modifydt"));


            boolean isExistes = false;//是否存在排期
            boolean isUploadUI = true;
            if (entity != null){
                isExistes = true;
                isUploadUI = makeTimerTask(entity);
            }
            //false 不存在任何排期
            //播放默认的图片



        if (isUploadUI){
            //更新ui
            //发送广播 通知视图更新
            notificationUIexcuter(isExistes,entity);
        }else{
            log.e(TAG,"不更新ui 获取低优先级的任务");
            deleteOnlySchedule(currentEntity);
            Start(true);
        }

        }
        catch (Exception e)
        {
            log.e(TAG," 执行读取排期错误:"+e.getMessage());

            //如果错误是dalay is err ,删除这个排期,重新来过
            if (e.getMessage().equals("dalay is err")){
                if (currentEntity==null){
                    log.e(TAG,"当前无排期");
                    return;
                }
                deleteOnlySchedule(currentEntity);
                Start(true);
            }
            log.e(TAG,"执行读取排期 异常 :"+ e.getCause());
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * 制作定时器
     * false 代表不立刻更新视图
     * */
    private static boolean makeTimerTask(XmlNodeEntity entity) throws Exception{
        boolean flag = true;
        boolean isNotTo = false;

        String type = entity.getXmldata().get("type");
        long dalay = -1;
        //如果是插播
       if (type.equals("4")){
        String timelength = entity.getXmldata().get("timelength");
           try{
               dalay = Integer.parseInt(timelength);
               dalay = dalay * 1000;
           }catch (Exception e){
               log.e(TAG,"插播解析播放时长异常:"+timelength);
               return false;
           }
       }
        else
        if(type.equals("3")){
        //如果是重复
            String start = entity.getXmldata().get("start");
            String end = entity.getXmldata().get("end");
            log.i(TAG," 重复 . 开始时间:"+start+"- 结束时间:"+end);

            String current = dataFormatUtils.format(new Date());//当前时间
            Date startTime = null;
            Date currentData = null;
            Date endTime = null;
            //转换成时间对象
            try {
                startTime = dataFormatUtils.parse(start);
                currentData = dataFormatUtils.parse(current);
                endTime = dataFormatUtils.parse(end);
            } catch (ParseException e) {
                log.e(TAG,"读取排期失败"+e.getMessage());
                return false;
            }
            //开始时间 > 现在的时间 ?
            if (startTime.after(currentData)){
                Toals.Say("重复类型,还 未到 时间段");

                dalay =  getTimeMillsecondes(start) - getTimeMillsecondes(current);
                Toals.Say("持续秒数:"+ dalay/1000);

                //现在 - 开始 的时间, 这期间 播放低优先级的? 还没想好怎么处理 - - 再说吧;
                isNotTo = true;//未到时间的任务
                flag = false;
            }else{
                //开始时间 < 现在时间  < 结束时间
                if (startTime.getTime()-currentData.getTime()==0 || currentData.after(startTime) && currentData.before(endTime)){

                    //计算 结束时间- 当前时间的时间差 ,立即更新视图
                    dalay =  getTimeMillsecondes(end) - getTimeMillsecondes(current);
                    Toals.Say("持续秒数:"+ dalay/1000);
                }
                if (endTime.before(currentData)){
                    log.e(TAG,"排期 结束时间 在 当前时间 之前");
                }

            }
        }
        else
        //如果是点播
        if (type.equals("2") && !entity.getXmldata().get("allday").equals("1")){
            //如果不是全天
            String current = dataFormatUtils.format(new Date());
            String startcron = entity.getXmldata().get("startcron");//entity.getXmldata().get("startcron");
            String endcron = entity.getXmldata().get("endcron");
            Toals.Say("点播开始时间:"+startcron+"点播结束时间:"+endcron +"; 当前时间:"+current);

            Date startTime = null;
            Date currentData = null;
            //转换成时间对象
            try {
                startTime = dataFormatUtils.parse(startcron);
                currentData = dataFormatUtils.parse(current);
            } catch (ParseException e) {
              log.e(TAG,"读取排期失败"+e.getMessage());
                return false;
            }

            //判断 当前时间 < 开始时间?
           if (startTime.after(currentData)){
               Toals.Say("还未到点播时间段");
               dalay =  getTimeMillsecondes(startcron) - getTimeMillsecondes(current);
               Toals.Say("持续秒数:"+ dalay/1000);
               isNotTo = true;//未到时间的任务
               flag = false;
           }else{
               //"endcron" -> "2016-08-23 23:30:00"
               Toals.Say("当前 时间段 点播");
               dalay = getTimeMillsecondes(endcron) - getTimeMillsecondes(current);
               Toals.Say("持续秒数:"+ dalay/1000);
           }

        }
        else{
            //其他 在今天结束时 重新读取排期
            String currentTimeText = dataFormatUtils.format(new Date());
            String todayEndTimeText =  currentTimeText.substring(0, 11) + "23:59:59";
            dalay = getTimeMillsecondes(todayEndTimeText) - getTimeMillsecondes(currentTimeText);
        }

        if (dalay<0){
            log.e(TAG," 定时任务 执行时间 错误:"+dalay);
            throw new Exception("dalay is err");
        }

        log.i(TAG,"延时秒数:"+dalay/1000);
        try {
            startTimer(dalay,isNotTo);
        } catch (Exception e) {
           log.e(TAG,"开始定时器 err:"+ e.getMessage()+" cause:"+e.getCause());
            throw new Exception("start timer is err");
        }
        return flag;
    }

    //通知视图更新
    private static void notificationUIexcuter(boolean isExistes, XmlNodeEntity entity) {
        //true 有排期
        // false 默认版面播放


        if (isExistes){//有排期
            Intent intent = new Intent();
            intent.setAction(UibrocdCastReceive.action);
            Bundle b = new Bundle();
            b.putParcelable(UibrocdCastReceive.key,entity);
            intent.putExtras(b);
            wosPlayerApp.appContext.sendBroadcast(intent);
        }else{
            log.e(TAG,"播放默认排期");
        }
    }

    /**
     * 查询 合适排期
     */
    private static XmlNodeEntity querySchedule() throws ParseException {

        XmlNodeEntity e = null;

        //根据排期优先级排序
        ArrayList<XmlNodeEntity> currentArr = DetermineScheduling();

        if (currentArr==null){
            log.e(TAG,"当前无排期信息"+ currentArr);
            return e;
        }

        //根据最后修改时间得到唯一一个排期
        XmlNodeEntity entity = determineModificationDate(currentArr);
        if (entity==null){
            log.e(TAG,"根据最后修改时间 未获取到 任何排期");
            return e;
        }

        //根据时间范围判断
        boolean flag = determineTime(entity);

        if (!flag){//false 不符合要求 删除这个排期 再次查询
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
        if (executeExpression ==null || executeExpression.length()==0){
            log.e(TAG," 重复类型判断: executeExpression ->" + executeExpression);
            return false;
        }
        String []executeExpressionArray = executeExpression.split("\\s"); //空格分割

        if (executeExpressionArray.length == 0 ){
            log.e(TAG," 重复类型判断: executeExpressionArray 大小->" + executeExpressionArray.length);
            return false;
        }


                log.i(TAG,"判断 重复类型 每天?");
//                //如果是每天 true  "executeexpression" -> "0 0 0 * * ? 2016-2099" 每天  //第四个是* 第6个是?
                if (executeExpressionArray[3].equals("*") && executeExpressionArray[5].equals("?")){
                    log.i(TAG,"重复类型 每天播放");
                    return true;
                }

                log.i(TAG,"判断 重复类型 每月的第几天?");
// 如果是每个月 判断是哪一天 再判断是不是今天 || 每月 在第 28天  "executeexpression" -> "0 0 0 28 * ? 2016-2099"
                if (!executeExpressionArray[3].equals("*")  && executeExpressionArray[4].equals("*") && executeExpressionArray[5].equals("?")){

                 //获取今天是这个月的第几天
                    int day_of_month = todayCalendar.get(Calendar.DAY_OF_MONTH);
                    int schedule_dayNum = -1;
                    try{
                        schedule_dayNum = Integer.parseInt(executeExpressionArray[3]);
                    }catch (Exception e){
                        log.e(TAG,"重复类型排期 每月的某天播放 解析天数参数 异常:"+executeExpressionArray[3]);
                        return false;
                    }

                    if (day_of_month == schedule_dayNum){
                        log.i(TAG,"重复类型 每月的某天播放");
                        return true;
                    }else{
                        log.e("^^^^^^^^^");
                      //  return false;
                    }
                }

                log.i(TAG,"判断 重复类型 每年?");
                //如果是每个年  "cronexpression" -> "* * * 19 9 ? 2016-2017"
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
                        log.i(TAG,"重复类型 每年播放");
                        return true;
                    }else{
                        log.e("^^^^^^^^^");
                       // return false;
                    }
                }

            log.i(TAG,"判断 重复类型 每月的第几星期的星期几?");
//          //如果是每个月的 的某个星期 判断 >是不是这个星期 >再判断是不是今天|  每月 在第 4周 周三 "executeexpression" -> "0 0 0 ? * 4#4 2016-2099" //4#4 星期几#这个月的第几个星期

            if (executeExpressionArray[3].equals("?") &&  executeExpressionArray[5].contains("#")){
                //获取 当前是这个月的第几个星期 星期几
                int week = 0;
                int day_of_week = 0;
                try {
                    week = todayCalendar.get(Calendar.WEEK_OF_MONTH);
                    day_of_week = todayCalendar.get(Calendar.DAY_OF_WEEK);
                } catch (Exception e) {
                    log.e(TAG," "+e.getMessage());
                  return false;
                }
                //获取排期规定的 第几个星期星期几
                String text = executeExpressionArray[5];
                log.e("executeExpressionArray[5] :"+executeExpressionArray[5]);
                if (text==null || text.equals("") || text.length()==0){
                    log.e(TAG," 重复类型判断星期几 text:"+text);
                    return false;
                }
                String []textArr = text.split("#");
                if (textArr.length == 0){
                    log.e(TAG," 解析星期数异常 :"+ executeExpressionArray[5]);
                    return false;
                }

                int schedule_week = -1;//星期
                int schedule_week_day = -1;//星期下的天数
                try{
                    schedule_week = Integer.parseInt(textArr[1]);
                    schedule_week_day = Integer.parseInt(textArr[0]);
                }catch (Exception e){
                    log.e(TAG,"重复类型排期 每个月的第几个星期的星期几 解析天数参数 异常:"+textArr[0]+"-"+textArr[1]);
                    return false;
                }

                log.i(TAG," schedule_week:"+schedule_week+";schedule_week_day:"+schedule_week_day);
                log.i(TAG," week:"+week+";day_of_week:"+day_of_week);

                if (week==schedule_week && day_of_week==schedule_week_day){
                    log.i(TAG,"重复类型 每个星期播放");
                    return true;
                }else{
                    log.e("^^^^^^^^^");
                    //return false;
                }
            }

        log.i(TAG,"判断 重复类型 每个星期的星期几?");
        if (executeExpressionArray[3].equals("?") &&  !executeExpressionArray[5].contains("#")){

            int day_of_week = todayCalendar.get(Calendar.DAY_OF_WEEK);//获取当前是星期几
            String text = executeExpressionArray[5];
            log.e("executeExpressionArray[5] :"+executeExpressionArray[5]);
            if (text==null && text.length()==0){
                log.e(TAG," 重复类型 每星期的星期几 text:"+text);
                return false;
            }

            int schedule_week_day = -1;//星期下的天数
            try{
                schedule_week_day = Integer.parseInt(text);
            }catch (Exception e){
                log.e(TAG,""+e.getMessage());
                return false;
            }

            log.i(TAG,"schedule_week_day:"+schedule_week_day);
            if (day_of_week==schedule_week_day){
                log.i(TAG,"重复类型 每个星期播放");
                return true;
            }else{
                log.e("^^^^^^^^^");
                //return false;
            }
        }

        log.i(TAG,"重复类型解析完毕");
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

                return a-b>0 ? -1: a-b==0 ? 0:1;  //-1代表前者小，0代表两者相等，1代表前者大。
            }
        });

        log.i(TAG,"-----------------------start-------------------------------");
        for (int i = 0;i<current.size();i++){
            log.i(TAG,"name: {"+current.get(i).getXmldata().get("summary")+"}最后修改时间:["+current.get(i).getXmldata().get("modifydt")+"]");
        }

        log.i(TAG,"-----------------------end-------------------------------");
        entity = current.get(0);
        return entity;
    }

    /**
     * 停止
     */
    public static void Stop() {


        //清楚所有排期
        clearScheduleMap();
        //停止所有定时任务
        stopTimer(false);
        //停止ui执行者
//        UiExcuter.getInstancs().StopExcuter();
    }

    /**
     * 判断排期类型
     */
    private static ArrayList<XmlNodeEntity> DetermineScheduling() {
        //优先级 : 插播 4> 重复 3> 点播 2> 轮播 1> 默认5
        if (scheduleMap.get("4").size()>0){
            log.i(TAG,"返回全部的 插播 排期");
            return scheduleMap.get("4");
        }
        if(scheduleMap.get("3").size()>0){
            log.i(TAG,"返回全部的 重复 排期");
            return scheduleMap.get("3");
        }
        if (scheduleMap.get("2").size()>0){
            log.i(TAG,"返回全部的 点播 排期");
            return scheduleMap.get("2");
        }
        if(scheduleMap.get("1").size()>0){
            log.i(TAG,"返回全部的 轮播 排期");
            return scheduleMap.get("1");
        }
        if(scheduleMap.get("5").size()>0){
            log.i(TAG,"返回全部的 插默 排期");
            return scheduleMap.get("5");
        }
        return null;
    }


    /**
     * 判断时间范围
     * //不符合要求 删除这个具体的排期所在数组 ,重新查询下一个排期
     * @param current
     * @throws ParseException
     */
    private static boolean determineTime(XmlNodeEntity current) throws ParseException {

        //如果是默认排期 直接返回
        String type = current.getXmldata().get("type");
        if (type.equals("5")){
            log.i(TAG,"默认排期");
            return true;
        }


        String schedule_startTimeText = current.getXmldata().get("startcron");
        String schedule_endTimeText = current.getXmldata().get("endcron");


        if ( !Command_SYTI.RegexMatches(schedule_startTimeText) ||  !Command_SYTI.RegexMatches(schedule_endTimeText)){
           log.e(TAG,"排期开始时间 或者 结束时间 错误");
            return false;
        }

        //换成 日历类型
        Date startTime = dataFormatUtils.parse(schedule_startTimeText);
        Date endTime = dataFormatUtils.parse(schedule_endTimeText);


        //获取当前的时间
        String currentTimeText =  dataFormatUtils.format(new Date());
        Date currentData = dataFormatUtils.parse(currentTimeText);

        log.i(TAG,"1 --- >当前时间:"+currentTimeText+";排期 开始:"+schedule_startTimeText+"---结束:"+schedule_endTimeText);


        String start = current.getXmldata().get("start");
        String end = current.getXmldata().get("end");
        log.i(TAG,"2---> 当前时间:"+currentTimeText+";排期 开始:"+start+"---结束:"+end);
        if (type.equals("3")){
            log.i(TAG,"--重复类型--");

            //换成 日历类型
            Date mstart = dataFormatUtils.parse(start);
            Date mend = dataFormatUtils.parse(end);

            // 如果结束时间 < 当前时间  false
            if (mend.before(currentData)){
                log.e(TAG," 重复类型 结束时间 小于 当前时间 ");
                return false;
            }
            //判断开始时间 > 当前时间 ?
            if(mstart.after(currentData)){
                return true;
            }
            //如果 开始时间 < 当前时间 < 结束时间  或者 开始时间==当前时间
            if ( currentData.getTime()-mstart.getTime()==0 || currentData.after(mstart) && currentData.before(mend)){
                return determineTypeOnRepeat(current,currentData);//重复类型 深度判断
            }
        }


        // 判断 当前的时间 是不是 符合: 在开始 - 结束 的范围内 ,如果是点播类型 2 ,判断 他的开始时间 是不是在当前时间之后,是的话,也返回true
        if (type.equals("2")){
            Toals.Say("点播类型,开始:"+schedule_startTimeText+"  当前:"+currentTimeText);
            boolean f = startTime.after(currentData);
            Toals.Say("开始时间 > 当前时间 :"+ f);
            log.e(TAG,"点播 开始时间 > 当前时间 ? ->"+ f);
            if (f){
                return true;
            }else{
                log.e(TAG,"...");

            }
        }
        log.i(TAG," - - - -determineTime hh");

        if ( currentData.getTime()-startTime.getTime()==0 || currentData.after(startTime) && currentData.before(endTime)){
            log.i(TAG,"在当前时间存在有效排期");
            return true;
        }

        log.i(TAG," - - - -determineTime over");
        return false;
    }






}
