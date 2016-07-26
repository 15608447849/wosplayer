package com.wosplayer.Ui.performer;

import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.broadcast.Command.Schedule.correlation.XmlNodeEntity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/7/25.
 * 执行布局
 */
public class layoutExcuter {
    private static final java.lang.String TAG = layoutExcuter.class.getName();
    private XmlNodeEntity layout ;
    private IPlayer currentIplayer=null ;
    private ArrayList<XmlNodeEntity> contentArr = null;
    public layoutExcuter(XmlNodeEntity layout) {
        this.layout = layout;
        log.i(TAG,"创建成功");
    }

    public void start(){
        contentArr = layout.getChildren();
        if (contentArr==null || contentArr.size()==0){
            log.e(TAG," 布局下 无内容列表" + layout.getChildren());
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
        log.i(TAG,"执行具体内容的播放: ["+contentArr.get(_index).getXmldata().get("contentsnewname") + "]当前时间毫秒数:"+System.currentTimeMillis());
        log.i(TAG,"在"+(second*1000)+"后执行下一个内容");
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
        final DataList datalist = ReorganizationData(content);
        createContent(datalist);//自动执行
    }



    //组装数据
    private DataList ReorganizationData(XmlNodeEntity content) {
        DataList datalist =new DataList();

        //1 x,y,w,h  2.本地文件路径,<contentsnewname><![CDATA[1469177181932.mp4]]></contentsnewname> 3.<fileproterty>video</fileproterty>类型 4.资源uri    <getcontents><![CDATA[ftp://ftp:FTPmedia@172.16.0.19/uploads/1469177181932.mp4]]></getcontents>
        String x = layout.getXmldata().get("x");
        String y = layout.getXmldata().get("y");
        String width = layout.getXmldata().get("width");
        String height = layout.getXmldata().get("height");
        String fileproterty = content.getXmldata().get("fileproterty");//类型
        String getcontents = content.getXmldata().get("getcontents");//uri
        String localpath = wosPlayerApp.config.GetStringDefualt("basepath","")+content.getXmldata().get("contentsnewname");//本地路径
        String timelength = content.getXmldata().get("timelength");
        datalist.put("x",x);
        datalist.put("y",y);
        datalist.put("width",width);
        datalist.put("height",height);
        datalist.put("fileproterty",fileproterty);
        datalist.put("getcontents",getcontents);
        datalist.put("localpath",localpath);
        datalist.put("timelength",timelength);

        String key = layout.getXmldata().get("id")+content.getXmldata().get("id")+content.getXmldata().get("materialid")+fileproterty+content.getXmldata().get("contentsnewname");//生成一个唯一标识
        datalist.setKey(key);//唯一标识
        return  datalist;
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
        log.i(TAG,"----"+layout.getChildren());
        layout = null;
        contentArr=null;
        //清理内容 放在 主线程
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
           clearContent();
            }
        });
    }

    /**
     * 创建内容视图 放入主线程
     * @param datalist
     */
    private void createContent(final DataList datalist){

        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                clearContent();
                //生成iplayer
                currentIplayer = contentTanslater.tanslationAndStart(datalist);//创建必须放入主线程执行
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
    }


}