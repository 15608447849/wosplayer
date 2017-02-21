package com.wosplayer.Ui.performer;

import com.wosplayer.app.Logs;
import com.wosplayer.cmdBroadcast.Command.Schedule.correlation.XmlNodeEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2016/7/24.
 * 单例
 */

public class UiExcuter {
    private static final java.lang.String TAG = "Ui Excute";
    private static UiExcuter uiExcuter = null;

    private UiExcuter() {
        Logs.i(TAG, "ui excuter create");
    }

    public static UiExcuter getInstancs() {
        if (uiExcuter == null) {
            uiExcuter = new UiExcuter();
        }
        return uiExcuter;
    }

    private static ReentrantLock lock = new ReentrantLock();
    public static boolean isStoping = false;

    public void StartExcuter(XmlNodeEntity schedule) {
        Logs.i(TAG, "ui执行者 所在线程:" + Thread.currentThread().getName());
        try {
            if (schedule == null) {
                Logs.e(TAG, " ui执行者不执行 ,schedule is null");
                return;
            }

            lock.lock();
            try {
                StopExcuter();
            } catch (Exception e) {
                Logs.e(TAG, "UI Executer stop err:" + e.getMessage());
            }
            Logs.i(TAG, "uiExcuter setting schedule");
            uiExcuter.setPlaySchedule(schedule);

        } catch (Exception e) {
            Logs.e(TAG, "ui 执行者 开始异常 " + e.getMessage());
        } finally {
            lock.unlock();
        }

    }

    public void StopExcuter() {
        Logs.i(TAG, "ui执行者 清理...................");
//        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
//            @Override
//            public void call() {
                isStoping = true;
                //清理 : 1 存在的定时器 2.初始化_index 3.清理节目执行者
                clearTimer();
                _index = 0;
                contentTanslater.clearCache();
                clearProgramExcuter();
//                if (DisplayActivity.activityContext != null) {
//                    //隐藏层布局
//                    DisplayActivity.activityContext.goneLayoutdialog();
//                }
                isStoping = false;
                Logs.i(TAG, "ui执行者 清理完毕");
//            }
//        });
    }


    /**
     * @param schedule
     */
    private void setPlaySchedule(XmlNodeEntity schedule) {

        //得到排期的类型创建定时器 时长计算: 布局下的内容的总时长 得到布局的 时长   布局时长最长的就是 节目的时长
        String type = schedule.getXmldata().get("type");
        Logs.i(TAG, "准备执行的排期类型:" + type + " ,  [1=轮播,2=点播,3=重复,4=插播,5=重复]");


        ArrayList<XmlNodeEntity> ProgramTimerList = new ArrayList<XmlNodeEntity>();
        //得到节目数组
        ArrayList<XmlNodeEntity> programArr = schedule.getChildren();
        if (programArr == null || programArr.size() == 0) {
            Logs.e(TAG, "当前排期无节目列表");
            return;
        }

        for (XmlNodeEntity program : programArr) {
            Logs.i(TAG, "计算当前节目 << " + program.getXmldata().get("title") + " >> 的时长中");
            long programTime = getProgramTimeLength(program);
            program.getXmldata().put("programTime", String.valueOf(programTime));
            ProgramTimerList.add(program);
        }


        if (ProgramTimerList.size() == 1) {
            //只有一个节目
            //直接执行 节目执行者
            createProgramExcuter(ProgramTimerList.get(0));
        } else {
            //创建定时器 去执行节目执行者
            startProgramTimerExcuter(ProgramTimerList);
        }
    }

    private int _index = 0; //在每次开始执行ui时 请初始化一次
    Timer timer = null;//定时器
    TimerTask timerTask = null;

    private void startProgramTimerExcuter(final ArrayList<XmlNodeEntity> programTimerlist) {
        //取消存在的定时器
        clearTimer();

        //执行 节目执行者
        Logs.i(TAG, "执行节目执行者: " + programTimerlist.get(_index).getXmldata().get("title") + "当前时间毫秒数:" + System.currentTimeMillis());
        long second = Long.parseLong(programTimerlist.get(_index).getXmldata().get("programTime"));
        Logs.i(TAG, "在" + (second * 1000) + "后执行下一个节目");
        createProgramExcuter(programTimerlist.get(_index));

        //创建定时器
        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                startProgramTimerExcuter(programTimerlist);
            }
        };
        timer.schedule(timerTask, second * 1000);//延时多久 毫秒数
        //设置下标
        _index++;
        if (_index == programTimerlist.size()) {
            _index = 0;
        }
    }

    //取消定时器
    private void clearTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * 得到时长
     *
     * @param program
     */
    private long getProgramTimeLength(XmlNodeEntity program) {
        long programTime = -1;
        ArrayList<Long> layoutTimeArr = new ArrayList<Long>();
        //得到布局的数组
        ArrayList<XmlNodeEntity> layoutArr = program.getChildren();
        for (XmlNodeEntity layout : layoutArr) {
            Logs.i(TAG, "当前节目下一个布局:" + layout.getXmldata().get("id"));
            long layoutTime = -1;
            //得到布局下面的内容
            ArrayList<XmlNodeEntity> contentArr = layout.getChildren();
            for (XmlNodeEntity content : contentArr) {
                String contentTimeText = content.getXmldata().get("timelength");
                try {
                    long contentTime = Long.parseLong(contentTimeText);
                    layoutTime += contentTime;
                } catch (Exception e) {
                    Logs.e(TAG, "获取节目-布局-内容 下的时长 解析错误:" + contentTimeText);
                    continue;
                }
            }
            Logs.i(TAG, "得到一个布局的时长:" + layoutTime);
            layoutTimeArr.add(layoutTime);
        }
        //排序
        Collections.sort(layoutTimeArr, new Comparator<Long>() {

            @Override
            public int compare(Long lhs, Long rhs) {
                return lhs - rhs > 0 ? -1 : lhs - rhs == 0 ? 0 : 1;  //-1代表前者小，0代表两者相等，1代表前者大。
            }
        });

        programTime = layoutTimeArr.get(0);
        return programTime;
    }


    /**
     * 创建 节目
     */
    private programExcuter currentPlayProgram = null;

    private void createProgramExcuter(XmlNodeEntity program) {
        clearProgramExcuter();
        currentPlayProgram = new programExcuter(program);
        currentPlayProgram.start();
    }

    //清理节目执行者
    private void clearProgramExcuter() {
        if (currentPlayProgram != null) {
            Logs.i(TAG, "开始清理节目中...");
            currentPlayProgram.stop();
            currentPlayProgram = null;
        }
    }

}
