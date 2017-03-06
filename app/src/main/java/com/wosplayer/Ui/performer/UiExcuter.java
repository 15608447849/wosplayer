package com.wosplayer.Ui.performer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.R;
import com.wosplayer.Ui.element.uitools.ImageStore;
import com.wosplayer.Ui.element.uitools.ImageViewPicassocLoader;
import com.wosplayer.app.AppTools;
import com.wosplayer.app.BackRunner;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.app.SystemConfig;
import com.wosplayer.command.operation.schedules.ScheduleReader;
import com.wosplayer.command.operation.schedules.correlation.XmlNodeEntity;

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
    private static final java.lang.String TAG = "Ui执行";
    private static UiExcuter uiExcuter = null;

    private UiExcuter() {
    }

    public static UiExcuter getInstancs() {
        if (uiExcuter == null) {
            uiExcuter = new UiExcuter();
        }
        return uiExcuter;
    }

    private DisplayActivity mActivity;
    private boolean isInit = false;
    public String defImagePath="";
    public String defVideoPath="";
    public String basepath ="";
    public String defaultPath = "";
    public String ffbkPath="";
    public void init(DisplayActivity activity){
        this.mActivity = activity;
        SystemConfig config = SystemConfig.get();
        config.printData();
        defVideoPath = config.GetStringDefualt("defaultVideo","");
        basepath = config.GetStringDefualt("basepath","");
        ffbkPath = config.GetStringDefualt("fudianpath","");
        defaultPath = config.GetStringDefualt("default","");
        isInit = true;
        BackRunner.runBackground(new Runnable() {
            @Override
            public void run() {
                if (defaultPath.isEmpty() || ffbkPath.isEmpty()) return;
                //将默认排期放入指定文件夹下
                AppTools.defaultProgram(mActivity,defaultPath);
                Logs.i("后台任务","默认排期解压缩完成");
                //将默认图片或者视频放入指定 文件夹下
                // Logs.i("后台任务","默认资源放入指定目录下 - "+resourcePath+"default.mp4 成功");
                AppTools.fudianBankSource(mActivity,ffbkPath);
                Logs.i("后台任务","富颠金融网页模板解压缩完成");
                ScheduleReader.clear();
                ScheduleReader.Start(false);
            }
        });
    }
    public void unInit(){
        isInit = false;
        this.mActivity = null;
        ScheduleReader.clear();
        UiExcuter.getInstancs().StopExcuter();
        ImageStore.getInstants().clearCache();
    }



    private static ReentrantLock lock = new ReentrantLock();
    public static boolean isStoping = false;
    public void StartExcuter(XmlNodeEntity schedule) {

        Logs.i(TAG, "线程名:" + Thread.currentThread().getName());
        if (schedule == null) {
            Logs.e(TAG, "不执行空排期");
            return;
        }
        if (!isInit) {
            Logs.e(TAG, "不执行绘制UI界面");
            return;
        }
        try {
            lock.lock();
            StopExcuter();
            Logs.i(TAG, "开始关联数据");
            uiExcuter.settingSchedule(schedule);
        } catch (Exception e) {
            Logs.e(TAG, "ui 执行者 开始异常 " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void StopExcuter() {
                Logs.i(TAG, "清理界面中");
                isStoping = true;
                //清理 : 1 存在的定时器 2.初始化_index 3.清理节目执行者
                clearTimer();
                _index = 0;
                contentTanslater.clearCache();
                clearProgramExcuter();
                isStoping = false;
                Logs.i(TAG, "清理完毕");
    }


    /**
     * @param schedule
     */
    private void settingSchedule(XmlNodeEntity schedule) {
        //得到排期的类型创建定时器 时长计算: 1.布局下的内容的总时长得到布局的时长 2.布局时长最长的就是节目的时长
        ArrayList<XmlNodeEntity> ProgramTimerList = new ArrayList<XmlNodeEntity>();
        //得到节目数组
        ArrayList<XmlNodeEntity> programArr = schedule.getChildren();
        if (programArr == null || programArr.size() == 0) {
            Logs.e(TAG, "当前排期无节目列表");
            return;
        }
        for (XmlNodeEntity program : programArr) {
           // Logs.i(TAG, "计算当前节目 << " + program.getXmldata().get("title") + " >> 的时长中");
            long programTime = getProgramTimeLength(program);
            program.getXmldata().put("programTime", String.valueOf(programTime));
            ProgramTimerList.add(program);
        }

        if (ProgramTimerList.size() == 1) {
            //只有一个节目 直接执行 节目执行者
            createProgramExcuter(ProgramTimerList.get(0));
        } else {

            //创建定时器去执行节目执行者
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
        if (layoutArr==null || layoutArr.size() == 0) return 9999;
        for (XmlNodeEntity layout : layoutArr) {
            //Logs.i(TAG, "当前节目下一个布局:" + layout.getXmldata().get("id"));
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
           // Logs.i(TAG, "得到一个布局的时长:" + layoutTime);
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



    //设置 main 视图 背景
    public void setMainBg(final String var){
        if (!isInit) return;
        if (var==null || var.equals("null")){
            mActivity.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mActivity.main.setBackgroundDrawable(null);
                        mActivity.main.setBackgroundColor(Color.WHITE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }else
        if (var.startsWith("#")){
            //颜色
            mActivity.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mActivity.main.setBackgroundColor(Color.parseColor(var));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }else{
            //图片
            Bitmap bitmap = ImageViewPicassocLoader.getBitmap(var,null);
            if (bitmap==null){
                //获取错误图片bitmap
                final String errorTag = "errorimage";
                bitmap = ImageStore.getInstants().getBitmapCache(errorTag);
                if (bitmap==null || bitmap.isRecycled()){
                    bitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.error);
                    ImageStore.getInstants().addBitmapCache(errorTag,bitmap);
                }
            }
            final BitmapDrawable drawable = new BitmapDrawable(bitmap);
            mActivity.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mActivity.main.setBackgroundDrawable(drawable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public ViewGroup getMainLayout(){
        if (isInit) {
            return mActivity.main;
        }
        return null;
    }

    public Context getContext(){
        if (isInit) {
            return mActivity;
        }
        return null;
    }

}
