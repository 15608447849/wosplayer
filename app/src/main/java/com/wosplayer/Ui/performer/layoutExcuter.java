package com.wosplayer.Ui.performer;

import android.graphics.Typeface;

import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.Ui.element.uiViewimp.IVideoPlayer;
import com.wosplayer.app.DataList;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.app.Logs;
import com.wosplayer.command.operation.schedules.correlation.XmlNodeEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.trinea.android.common.util.FileUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/7/25.
 * 执行布局
 */
public class layoutExcuter implements TimeCalls {
    private static final java.lang.String TAG = "layout Excuter";
    private XmlNodeEntity layout ;
    private IPlayer currentIplayer=null ;
    private ArrayList<XmlNodeEntity> contentArr = null;
    private List<String> videoNameList = null;
    public layoutExcuter(XmlNodeEntity layout) {
        this.layout = layout;
       // Logs.i(TAG,"创建成功");
        //收集 有效 视频 数量
        takeVideoVaildCount();
    }
    //收集有效 视频 数量
    private void takeVideoVaildCount() {
        contentArr = layout.getChildren();
        for (XmlNodeEntity content : contentArr){
            if (content.getXmldata().get("fileproterty").equals("video")){
                if (videoNameList==null){
                    videoNameList = new ArrayList<>();
                }
                videoNameList.add(content.getXmldata().get("getcontents"));
            }
        }

    }

    public void start(){

        if (contentArr==null || contentArr.size()==0){
            Logs.e(TAG," 布局下 无内容列表" + layout.getChildren());
            return;
        }

        //判断当前节目 是不是互动类型并且只存在一个 如果是  不开启定时任务
        if (contentArr.size()==1 && contentArr.get(0).getXmldata().get("fileproterty").equals("interactive")){
            //互动
            //直接创建
            startContent(contentArr.get(0));
        }else{
            //设置定时任务
            startContentTimer(contentArr);
        }
    }

    private int _index = 0;//当前下标的选中内容
    Timer timer =null;//定时器
    TimerTask timerTask = null;
    private void clearTimer() {
        if (timerTask!=null){
            timerTask.cancel();
            timerTask= null;
        }
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
    }
    private void startContentTimer(final ArrayList<XmlNodeEntity> contentArr) {
        //取消存在的定时器
        clearTimer();

        //获取内容播放时长
        long second = Long.parseLong(contentArr.get(_index).getXmldata().get("timelength"));
       // Logs.i(TAG,"执行具体内容的播放: ["+contentArr.get(_index).getXmldata().get("contentsnewname") + "]当前时间毫秒数:"+System.currentTimeMillis());
        //Logs.i(TAG,"在"+(second*1000)+"后执行下一个内容");
        startContent(contentArr.get(_index));
        //创建定时器任务
        //创建定时器
        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                startContentTimer(contentArr);
            }
        };
        timer.schedule(timerTask,second*1000);//延时多久 毫秒数
        //设置下标
        _index++;
        if(_index==contentArr.size()){
            _index = 0;
        }
    }


    /**
     * 开始 一个 内容
     * @param content
     */
    private void startContent(XmlNodeEntity content) {
        //重新组合数据 -> 生成 datalist
        if(content==null){
            Logs.e(TAG,"布局 开始 生成 内容 err："+content);
            return;
        }
        Object[] dataArr = ReorganizationData(content);
        if (dataArr!=null){
            createContent((DataList) dataArr[0],dataArr[1]);//自动执行
        }

    }

    //组装数据
    private Object[] ReorganizationData(XmlNodeEntity content) {
        if (layout==null || content ==null ) {
            return null;
        }
        DataList datalist =new DataList();
        Object ob = null;
        //1 x,y,w,h  2.本地文件路径,<contentsnewname><![CDATA[1469177181932.mp4]]></contentsnewname> 3.<fileproterty>video</fileproterty>类型 4.资源uri    <getcontents><![CDATA[ftp://ftp:FTPmedia@172.16.0.19/uploads/1469177181932.mp4]]></getcontents>
        String x = layout.getXmldata().get("x");
        String y = layout.getXmldata().get("y");
        String width = layout.getXmldata().get("width");
        String height = layout.getXmldata().get("height");
        String fileproterty = content.getXmldata().get("fileproterty");//类型
        String getcontents = content.getXmldata().get("getcontents");//uri
        String localpath = UiExcuter.getInstancs().basepath+content.getXmldata().get("contentsnewname");//本地路径
        String timelength = content.getXmldata().get("timelength");
        String UUKS = content.getXmldata().get("uuks");
        String contentsname = content.getXmldata().get("contentsname");

        datalist.put("x",x);
        datalist.put("y",y);
        datalist.put("width",width);
        datalist.put("height",height);
        datalist.put("fileproterty",fileproterty);
        datalist.put("getcontents",getcontents);
        datalist.put("localpath",localpath);
        datalist.put("timelength",timelength);
        datalist.put("uuks",UUKS);
        datalist.put("contentsname",contentsname);
        String key = layout.getXmldata().get("id")+content.getXmldata().get("id")+content.getXmldata().get("materialid")+fileproterty+contentsname+UUKS;//生成一个唯一标识
        datalist.setKey(key);//唯一标识

        if (fileproterty.equals("text")){
            //滚动字幕
            ArrayList<XmlNodeEntity> contentArr = content.getChildren();
                if (contentArr.size()==0 || contentArr==null){

                    Logs.e(TAG,"一个文本类型的内容 不存在"+ contentArr);
                    return new Object[]{datalist,ob};
                }
            Logs.i("");

            XmlNodeEntity textcontent = contentArr.get(0);

            String boldstr = textcontent.getXmldata().get("fontweight");
            int boldvalue = Typeface.NORMAL;
            if (boldstr.equals("bold")){
                boldvalue = Typeface.BOLD;
            }
            boldstr = String.valueOf(boldvalue);
            String speed = textcontent.getXmldata().get("txtspeed");
            String fontcolor = textcontent.getXmldata().get("fontcolor");
            String bgcolor = textcontent.getXmldata().get("backgroudcolor");
            String texttype = textcontent.getXmldata().get("txtfont");
            String fontsize = textcontent.getXmldata().get("fontsize");
            String textContent = textcontent.getXmldata().get("txtcontents");


            datalist.put("bgcolor",bgcolor);
            datalist.put("fontcolor",fontcolor);
            datalist.put("fontsize",fontsize);
            datalist.put("textcontent",textContent);
            datalist.put("texttype",texttype);
            datalist.put("textstyle",boldstr);
            datalist.put("speed",speed);

        }

        if (fileproterty.equals("interactive")){
            ArrayList<XmlNodeEntity> activeArr = content.getChildren();
            if (activeArr==null || activeArr.size()==0){
                Logs.e(TAG,"互动模块 无布局");
                return new Object[]{datalist,ob};
            }
            if (activeArr.size() == 1){
                ob = activeArr.get(0);
            }else{
                Logs.e(TAG,"互动模块 存在多个 布局 无法解析 ...");
                return new Object[]{datalist,ob};
            }
        }
        if (fileproterty.equals("fudianbank")){
            datalist.put("fudianpath", UiExcuter.getInstancs().ffbkPath);
        }
        return  new Object[]{datalist,ob};
    }

    /**
     * 停止
     */
    public void stop(){
        //还原下标
        _index = 0;
        //停止定时器
        clearTimer();
        //清除数据
//        Logs.i(TAG,"----"+layout.getChildren());
        layout = null;
        contentArr=null;
        //清理 内容 放在 主线程
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
           clearContent();
            }
        });
        Logs.i(TAG,"布局 停止了 "+this.toString());
    }

    /**
     * 创建内容 视图 放入主线程
     * @param datalist
     */
    private void createContent(final DataList datalist, final Object ob){

        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                clearContent();
                //生成iplayer
                currentIplayer = contentTanslater.tanslationAndStart(datalist,ob,true,null,layoutExcuter.this);//创建必须放入主线程执行

            }
        });
    }

    /**
     * 清理内容 必须放在主线程
     */
    private void clearContent() {
        if (currentIplayer!=null){
            currentIplayer.stop();
            currentIplayer = null;
        }
      //  Logs.i(TAG,"清理 布局 下 的 内容");
    }

    @Override
    public void playOvers(IPlayer play) {
        if (play instanceof IVideoPlayer){
          if(!checkValidVideo((IVideoPlayer)play)){
              Logs.e(TAG,"无有效视频 播放默认视频资源");
              return;
          }
        }
        start();
    }



    //判断有效视频数量
    private boolean checkValidVideo(IVideoPlayer video){
        if (videoNameList!=null){
            if(videoNameList.contains(video.singleFileUri)){
             videoNameList.remove(video.singleFileUri);
            }
            if (FileUtils.isFileExist(video.singleFileUri) && !videoNameList.contains(video.singleFileUri)){
                videoNameList.add(video.singleFileUri);
            }
            Logs.e(TAG," 有效 视频数:"+videoNameList.size());
            if (videoNameList.size()==0){
                //没有一个有效视频 -> 播放默认视频
                    return false;
            }
            return true;
        }

      return true;
    }
}
