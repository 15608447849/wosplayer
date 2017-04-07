package com.wosplayer.Ui.performer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.ViewGroup;

import com.wosplayer.Ui.element.uitools.ImageStore;
import com.wosplayer.Ui.element.uitools.ImageViewPicassocLoader;
import com.wosplayer.Ui.element.uitools.IplayerStore;
import com.wosplayer.app.AppUtils;
import com.wosplayer.app.BackRunner;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.app.SystemConfig;
import com.wosplayer.command.operation.schedules.ScheduleReader;
import com.wosplayer.command.operation.schedules.correlation.StringUtils;
import com.wosplayer.command.operation.schedules.correlation.XmlNodeEntity;
import com.wosplayer.download.util.MD5Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by Administrator on 2016/7/24.
 * 单例
 */

public class UiExcuter {
    private static final java.lang.String TAG = "Ui执行";
    private static UiExcuter uiExcuter = null;

    private static ReentrantLock lock = new ReentrantLock();

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
    private ArrayList<XmlNodeEntity> programList = new ArrayList<>();//节目列表
    private int _index = 0; //在每次开始执行ui时 请初始化一次
    /**
     * 初始化参数
     */
    public void onInite(DisplayActivity activity){
        this.mActivity = activity;
        SystemConfig config = SystemConfig.get();
//        config.printData();
        defVideoPath = config.GetStringDefualt("defaultVideo","");
        defImagePath =  config.GetStringDefualt("defaultImage","");
        basepath = config.GetStringDefualt("basepath","");
        ffbkPath = config.GetStringDefualt("fudianpath","");
        defaultPath = config.GetStringDefualt("default","");
        isInit = true;
        BackRunner.runBackground(new Runnable() {
            @Override
            public void run() {
                if (defaultPath.isEmpty() || ffbkPath.isEmpty() || mActivity==null ) return;
                //将默认排期放入指定文件夹下
                AppUtils.unzipFiles(mActivity,defaultPath,"default.zip");
                Logs.i("后台任务","默认排期解压缩完成");
                //将默认图片或者视频放入指定 文件夹下
                // Logs.i("后台任务","默认资源放入指定目录下 - "+resourcePath+"default.mp4 成功");
                AppUtils.unzipFiles(mActivity,ffbkPath,"bank.zip");
                Logs.i("后台任务","富颠金融网页模板解压缩完成");
                ScheduleReader.notifySchedule();//通知排期读取
            }
        });
    }

    /**
     *
     */
    public void onUnInit(){
        onStop();//清理界面
        ScheduleReader.clear();//清理排期读取
        ImageStore.getInstants().clearCache();//清理图片缓存
        this.mActivity = null;//清理 上下文
        isInit = false;
    }


    /**
     * 开始执行
     */
    public void onStart(XmlNodeEntity schedule) { //在非UI线程

        if (schedule == null) {
            Logs.e(TAG, "不执行空排期");
            return;
        }
        if (!isInit) {
            Logs.e(TAG, "不执行绘制UI界面,尚未初始化Ui");
            return;
        }
        try {
            lock.lock();
            Logs.e(TAG,"\n=================================================\n");
            Logs.i(TAG, "执行任务所在线程: " + Thread.currentThread().getName());
            onStop();
            onSchedule(schedule);
            runingMain(PLAYS);//跳转到主线程
            Logs.e(TAG,"\n=================================================\n");
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    //activity结束时调用;读取到新排期时调用
    public void onStop() {
                Logs.i(TAG, "UI清理界面开始");
                _index = -1;
                removeMain(PLAYS);//移出一个任务
                Iterator<XmlNodeEntity> itr = programList.iterator();
                while (itr.hasNext()){
                    itr.next();
                    itr.remove();
                }
                Logs.i(TAG, "UI清理节目列表 - 节目数量:"+programList.size()); //清理现在的节目列表
                Logs.i(TAG, "UI清理界面结束");
                IplayerStore.getInstants().clearCache();
                Logs.i(TAG,"UI清理清理组件缓存完成");
                runingMain(PLAYS);
    }
    /**
     *
     * @param schedule
     */
    private void onSchedule(XmlNodeEntity schedule) {
        if (schedule==null) throw new IllegalStateException("UI无法关联空排期数据:"+schedule);
        Long sumTime = System.nanoTime();
        Logs.i(TAG, "开始关联UI数据 ");

        //得到节目数组ProgramList
        ArrayList<XmlNodeEntity> programArr = schedule.getChildren();
        if (programArr == null || programArr.size() == 0) {
            throw  new IllegalStateException("当前排期无节目,排期信息: id = "+schedule.getXmldata().get("id")+" ; summary = "+schedule.getXmldata().get("summary"));
        }
        //第一层循环 关于节目
        for (XmlNodeEntity program : programArr) {
           settingProgParam(program);
            //获取 布局信息 ,创建布局 执行所有的节目
            ArrayList<XmlNodeEntity> layoutArr = program.getChildren();
            if ( layoutArr==null || layoutArr.size()==0){
                throw  new IllegalStateException("当前节目无布局列表,节目信息: id = "+program.getXmldata().get("id")+" ; title = "+program.getXmldata().get("title"));
            }
            //第二层循环 - 关于布局
            for (XmlNodeEntity layout : layoutArr){
                ArrayList<XmlNodeEntity> contentArr = layout.getChildren();
                if ( contentArr==null || contentArr.size()==0){
                    throw  new IllegalStateException("当前布局无内容列表,内容信息: id = "+layout.getXmldata().get("id")+" ; title = "+layout.getXmldata().get("layoutname"));
                }
                settingLayoutContents(layout,contentArr);
            }//第二层循环

            programList.add(program);
        } //第一层循环

        sumTime = System.nanoTime() - sumTime;
        Logs.i(TAG, "关联UI数据结束 - 总用时:" +  sumTime +" 纳秒");
    }

    /**
     *获取背景
     */
    private String getBackgroud(XmlNodeEntity program) {
        //解析背景信息-查看背景是图片还是颜色
        String backgroud = program.getXmldata().get("bgimage");
        //如果是图片
        if (backgroud==null || backgroud.equals("")){
            //如果是颜色
            backgroud = program.getXmldata().get("bgcolor");
        }else{
            backgroud = UiExcuter.getInstancs().basepath + backgroud;
        }
        return backgroud==null?"":backgroud;
    }

    //执行节目执行
    private final Runnable PLAYS = new Runnable() {
        @Override
        public void run() {
            ProExcuter.getInstants().onStop();// 停止当前节目
            Logs.i(TAG, "节目数量:"+ programList.size() +" - 当前下标:"+_index);
            if (_index==-1){

                Logs.i(TAG,"结束节目播放");
                _index=0;
                //设置黑色背景
                setMainBg("#000000");
            }
            if (_index>=0 && programList.size()>0 ){//如果节目数量>0 并且 下标不为-1
                XmlNodeEntity currentProgram = programList.get(_index);
                //获取一个时长 - 在多久之后再次执行
                long second = Long.parseLong(currentProgram.getXmldata().get("programTime"));
                String name = currentProgram.getXmldata().get("title");
                setMainBg(currentProgram.getXmldata().get("backgroud"));
                ProExcuter.getInstants().onParse(currentProgram);// 解析
                ProExcuter.getInstants().onStart();//放在主线程
                Logs.i(TAG,"开始一个节目 ["+ name +"] 时长:"+ ++second +" 秒");
                runingMainDelayed(this,second * 1000);
                //设置下标
                _index++;
                if (_index == programList.size()) {
                    _index = 0;
                }
            }
        }
    };



    /**
     * 得到时长
     * 得到排期的类型创建定时器 时长计算: 1.布局下的内容的总时长>>得到布局的时长 2.布局时长最长的就是节目的时长
     * @param program
     */
    private long getProgramTimeLength(XmlNodeEntity program) {
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
        return layoutTimeArr.get(0);
    }

    //设置 main 视图 背景
    public void setMainBg(final String var){
        if (!isInit) return;
        if (StringUtils.isEmpty(var))return;
        if (var.startsWith("#")){
            //颜色
            runingMain(new Runnable() {
                @Override
                public void run() {
                    try {
                        mActivity.main.setBackgroundColor(Color.parseColor(var));
                    } catch (Exception e) {
                        mActivity.main.setBackgroundColor(Color.WHITE);
                    }
                }
            });
        }else{
            //图片
            if (!FileUtils.isFileExist(var)){
                return;
            }
            final BitmapDrawable drawable = new BitmapDrawable(ImageViewPicassocLoader.getBitmap(var));
            runingMain(new Runnable() {
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
    public Handler getHandle(){
        if (isInit) {
            return mActivity.mHandler;
        }
        return null;
    }

    public void runingMain(Runnable run){
        if (isInit){
            mActivity.mHandler.post(run);
        }
    }
    public void removeMain(Runnable run){
        if (isInit){
            mActivity.mHandler.removeCallbacks(run);
        }
    }
    public void runingMainDelayed(Runnable run,long delayMillis){
        if (isInit){
            mActivity.mHandler.postDelayed(run,delayMillis);
        }
    }


    public void settingLayoutContents(XmlNodeEntity layout,ArrayList<XmlNodeEntity> contentArr) {
        //布局大小
        String x = layout.getXmldata().get("x");
        String y = layout.getXmldata().get("y");
        String width = layout.getXmldata().get("width");
        String height = layout.getXmldata().get("height");
        String lid = layout.getXmldata().get("id");//布局id

        //第三层循环 - 关于内容
        for (XmlNodeEntity content : contentArr){
            HashMap<String,String> map = new HashMap<>();
            String fileproterty = content.getXmldata().get("fileproterty");//内容类型
            ContentFactory.ContentTypeEnum contentType = null;
            try {
                contentType = ContentFactory.ContentTypeEnum.valueOf(fileproterty);
            } catch (IllegalArgumentException e) {
                Logs.e(TAG,"布局数据 内容类型错误,未知类型:" + e.getMessage());
                map.put("error","true");
                content.setXmldata(map);
                continue;
            }
            String cid = content.getXmldata().get("id");//内容id
            String getcontents = content.getXmldata().get("getcontents");//uri 资源
            String contentsnewname = content.getXmldata().get("contentsnewname");//内容资源名字
            String timelength = content.getXmldata().get("timelength");//时长
            String contentsname = content.getXmldata().get("contentsname");//内容资源数据库名字
            String key =
                    lid// 布局id
                            +cid // 内容id
                            +fileproterty
                            +x+y+width+height
                            +getcontents
                            +contentsnewname
                            +timelength
                            +contentsname;
            //生成一个唯一标识
            map.put("key", MD5Util.toHexString(key.getBytes()));//唯一标识
            map.put("fileproterty",fileproterty);//内容类型
            map.put("timelength",timelength);//播放时长
            map.put("x",x);
            map.put("y",y);
            map.put("width",width);
            map.put("height",height);
            if (contentType.equals(ContentFactory.ContentTypeEnum.webpage)){
                map.put("type","1"); //  0 - 本地网页  1 -远程网页   2 -富滇项目
                map.put("url",getcontents.startsWith("http")?getcontents:"http://" +getcontents);//网页链接
            }
            if (contentType.equals(ContentFactory.ContentTypeEnum.fudianbank)){
                map.put("type","2");
                map.put("resource",getcontents);
                map.put("fudianpath", "file://"+UiExcuter.getInstancs().ffbkPath+"index.html");
            }
            if (contentType.equals(ContentFactory.ContentTypeEnum.image) || contentType.equals(ContentFactory.ContentTypeEnum.video)){
                map.put("localpath", UiExcuter.getInstancs().basepath+ AppUtils.subLastString(getcontents,"/"));//本地路径
            }
            if (contentType.equals(ContentFactory.ContentTypeEnum.text)){
                //滚动字幕
                ArrayList<XmlNodeEntity> data = content.getChildren();
                if (data==null || data.size()== 0 ){
                    Logs.e(TAG,"文本类型(text) 数据 不存在");
                    map.put("error","true");
                    content.setXmldata(map);
                    continue;
                }
                XmlNodeEntity textcontent = data.get(0);
                String boldstr = textcontent.getXmldata().get("fontweight");//字体类型
                String speed = textcontent.getXmldata().get("txtspeed");//速度
                String fontcolor = textcontent.getXmldata().get("fontcolor");//字体颜色
                String bgcolor = textcontent.getXmldata().get("backgroundcolor");//背景颜色
                String texttype = textcontent.getXmldata().get("txtfont");
                String fontsize = textcontent.getXmldata().get("fontsize");//字体大小
                String textContent = textcontent.getXmldata().get("txtcontents");//内容
                String orientation = textcontent.getXmldata().get("txtDir");
                String bgalpha =  textcontent.getXmldata().get("opacity");//背景透明度
                String fontalpha =  textcontent.getXmldata().get("txtAlpha");//字体透明度

                map.put("textstyle",(boldstr==null || !boldstr.equals("bold"))?String.valueOf(Typeface.NORMAL):String.valueOf(Typeface.BOLD));
                map.put("bgcolor",bgcolor);
                map.put("fontcolor",fontcolor);
                map.put("fontsize",fontsize);
                map.put("textcontent",textContent);
                map.put("texttype",texttype);
                map.put("speed",speed);
                map.put("orientation",orientation!=null?((orientation.equals("自左向右"))?"1":"0"):"0");//方向
                map.put("fontalpha",fontalpha==null?bgalpha:fontalpha);//文本透明度 如果没有文本透明度 - 背景透明度设置文本透明度
                map.put("bgalpha",fontalpha==null?"0":bgalpha);//背景透明度 如果文本透明度不存在-背景透明度设置为透明
            }
            if (contentType.equals(ContentFactory.ContentTypeEnum.time)){
                //滚动字幕
                ArrayList<XmlNodeEntity> data = content.getChildren();
                if (data==null || data.size()== 0 ){
                    Logs.e(TAG,"时间类型(time) 数据 不存在");
                    map.put("error","true");
                    content.setXmldata(map);
                    continue;
                }
                XmlNodeEntity textcontent = data.get(0);
                String fontcolor = textcontent.getXmldata().get("fontcolor");//字体颜色
                String bgcolor = textcontent.getXmldata().get("backgroundcolor");//背景颜色
                String fontsize = textcontent.getXmldata().get("fontsize");//字体大小
                String bgalpha =  textcontent.getXmldata().get("opacity");//背景透明度
                String fontalpha =  textcontent.getXmldata().get("txtAlpha");//字体透明度
                map.put("bgcolor",bgcolor);
                map.put("fontcolor",fontcolor);
                map.put("fontsize",fontsize);
                map.put("fontalpha",fontalpha==null?bgalpha:fontalpha);//文本透明度 如果没有文本透明度 - 背景透明度设置文本透明度
                map.put("bgalpha",fontalpha==null?"0":bgalpha);//背景透明度 如果文本透明度不存在-背景透明度设置为透明
            }
            if (contentType.equals(ContentFactory.ContentTypeEnum.interactive)){
                map.put("xmlurl",getcontents);
                map.put("tag",contentsnewname+contentsname);
            }
            //-***-//
            content.setXmldata(map); // 在布局执行中 转成 datalist类型数据
        }
    }

    //设置节目 时长 和 背景参数
    public void settingProgParam(XmlNodeEntity program) {
        long programTime = getProgramTimeLength(program);//获取时长
        program.getXmldata().put("programTime", String.valueOf(programTime));//
        String backgroud = getBackgroud(program);//获取背景颜色
        program.getXmldata().put("backgroud", backgroud);
    }
}
