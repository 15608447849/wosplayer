package com.wosplayer.Ui.performer;

import android.graphics.Typeface;

import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.app.AppTools;
import com.wosplayer.app.DataList;
import com.wosplayer.command.operation.schedules.correlation.XmlNodeEntity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/7/25.
 * 执行布局
 */
public class layoutExcuter implements TimeCalls {
    private static final java.lang.String TAG = "布局执行器";
    private XmlNodeEntity data ;
    private IPlayer currentIplayer=null ;
    private ArrayList<XmlNodeEntity> contentArr = null;
    public layoutExcuter(XmlNodeEntity layoutData) {
        this.data = layoutData;
        contentArr = data.getChildren();
    }

    public void start(){
        if (contentArr==null || contentArr.size()==0){
            return;
        }
        //判断当前节目是不是互动类型并且只存在一个如果是不开启定时任务
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
     */
    private void startContent(XmlNodeEntity content) {
        //重新组合数据 -> 生成 datalist
        if(content!=null){

            try {
                DataList dataArr = reorganizationData(content);
                createContent(dataArr);//自动执行
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private DataList reorganizationData(XmlNodeEntity content) {
        if (data==null || content ==null ) {
            throw new IllegalStateException("布局数据异常");
        }
        String fileproterty = content.getXmldata().get("fileproterty");//内容类型
        contentTanslater.ContentTypeEnum contentType;
        try {
            contentType = contentTanslater.ContentTypeEnum.valueOf(fileproterty);
        } catch (IllegalArgumentException e) {
           throw  new IllegalStateException("布局数据 内容类型错误,未知类型:" + e.getMessage());
        }
        //
        DataList datalist =new DataList();
        String x = data.getXmldata().get("x");
        String y = data.getXmldata().get("y");
        String width = data.getXmldata().get("width");
        String height = data.getXmldata().get("height");

        String getcontents = content.getXmldata().get("getcontents");//uri 资源
        String contentsnewname = content.getXmldata().get("contentsnewname");//内容资源名字
        String timelength = content.getXmldata().get("timelength");//时长
        String contentsname = content.getXmldata().get("contentsname");//内容资源数据库名字

        datalist.put("x",x);
        datalist.put("y",y);
        datalist.put("width",width);
        datalist.put("height",height);

        String key = data.getXmldata().get("id") // 布局id
                +content.getXmldata().get("id") // 内容id
                +fileproterty
                +x+y+width+height
                +getcontents
                +contentsnewname
                +timelength
                +contentsname
                ;//生成一个唯一标识
        datalist.setKey(key);//唯一标识

        //UiExcuter.getInstancs().basepath+

        datalist.put("fileproterty",fileproterty);

        if (contentType.equals(contentTanslater.ContentTypeEnum.webpage)){
            datalist.put("type","1"); //  0 - 本地网页  1 -远程网页   2 -富滇项目
            datalist.put("url",getcontents.startsWith("http")?getcontents:"http://" +getcontents);//网页链接
        }
        if (contentType.equals(contentTanslater.ContentTypeEnum.fudianbank)){
            datalist.put("type","2");
            datalist.put("resource",getcontents);
            datalist.put("fudianpath", "file://"+UiExcuter.getInstancs().ffbkPath+"index.html");
        }
        if (contentType.equals(contentTanslater.ContentTypeEnum.image) || contentType.equals(contentTanslater.ContentTypeEnum.video)){
            datalist.put("localpath", UiExcuter.getInstancs().basepath+ AppTools.subLastString(getcontents,"/"));//本地路径
        }
        if (contentType.equals(contentTanslater.ContentTypeEnum.text)){
            //滚动字幕
            ArrayList<XmlNodeEntity> contentArr = content.getChildren();
            if (contentArr==null || contentArr.size()== 0 )
                throw new IllegalStateException("文本类型(text) 数据 不存在");

            XmlNodeEntity textcontent = contentArr.get(0);
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

            datalist.put("textstyle",boldstr.equals("bold")?String.valueOf(Typeface.BOLD):String.valueOf(Typeface.NORMAL));
            datalist.put("bgcolor",bgcolor);
            datalist.put("fontcolor",fontcolor);
            datalist.put("fontsize",fontsize);
            datalist.put("textcontent",textContent);
            datalist.put("texttype",texttype);
            datalist.put("speed",speed);
            datalist.put("orientation",orientation!=null?((orientation.equals("自左向右"))?"1":"0"):"0");//方向
            datalist.put("fontalpha",fontalpha==null?bgalpha:fontalpha);//文本透明度 如果没有文本透明度 - 背景透明度设置文本透明度
            datalist.put("bgalpha",fontalpha==null?"0":bgalpha);//背景透明度 如果文本透明度不存在-背景透明度设置为透明
        }
        if (contentType.equals(contentTanslater.ContentTypeEnum.time)){
            //滚动字幕
            ArrayList<XmlNodeEntity> contentArr = content.getChildren();
            if (contentArr==null || contentArr.size()== 0 )
                throw new IllegalStateException("时间类型(time) 数据 不存在");
            XmlNodeEntity textcontent = contentArr.get(0);

            String fontcolor = textcontent.getXmldata().get("fontcolor");//字体颜色
            String bgcolor = textcontent.getXmldata().get("backgroundcolor");//背景颜色
            String fontsize = textcontent.getXmldata().get("fontsize");//字体大小
            String bgalpha =  textcontent.getXmldata().get("opacity");//背景透明度
            String fontalpha =  textcontent.getXmldata().get("txtAlpha");//字体透明度

            datalist.put("bgcolor",bgcolor);
            datalist.put("fontcolor",fontcolor);
            datalist.put("fontsize",fontsize);
            datalist.put("fontalpha",fontalpha==null?bgalpha:fontalpha);//文本透明度 如果没有文本透明度 - 背景透明度设置文本透明度
            datalist.put("bgalpha",fontalpha==null?"0":bgalpha);//背景透明度 如果文本透明度不存在-背景透明度设置为透明

        }

        if (contentType.equals(contentTanslater.ContentTypeEnum.interactive)){
            ArrayList<XmlNodeEntity> activeArr = content.getChildren();
            if (activeArr==null || activeArr.size()==0)
                throw new IllegalStateException("互动类型(interactive) 数据 不存在");

            if (activeArr.size() > 1)
                throw new IllegalStateException("互动类型(interactive) 数据 异常, 同布局存在多个互动类型内容.");

            datalist.put("xmlurl",getcontents);
            datalist.put("tag",contentsnewname+contentsname);
        }
        return datalist;
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
        data = null;
        contentArr=null;
        //清理 内容 放在 主线程
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
           clearContent();
            }
        });
       // Logs.i(TAG,"布局 停止了 "+this.toString());
    }

    /**
     * 创建内容 视图 放入主线程
     * @param datalist
     */
    private void createContent(final DataList datalist){

        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                clearContent();
                //生成iplayer
                currentIplayer = contentTanslater.tanslationAndStart(datalist,true,null,layoutExcuter.this);//创建必须放入主线程执行
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
        start();
    }



}
