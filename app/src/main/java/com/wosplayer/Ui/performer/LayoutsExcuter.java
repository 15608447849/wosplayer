package com.wosplayer.Ui.performer;

import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.command.operation.schedules.correlation.XmlNodeEntity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 2016/7/25.
 * 执行布局
 */
public class LayoutsExcuter implements TimeCalls {
    private static final java.lang.String TAG = "布局执行器";
    //当前内容对象
    private IPlayer currentIplayer=null ;
    private ArrayList<XmlNodeEntity> contentArr = null;
    private int _index = 0;//当前下标的选中内容
    public LayoutsExcuter(XmlNodeEntity layoutData) {
        contentArr = layoutData.getChildren();
    }
    //开始执行 - 请在主进程中
    public void start(){
        if (contentArr==null || contentArr.size()==0){
            return;
        }
        //根据下标 获取 content
        XmlNodeEntity content = contentArr.get(_index);
        //根据content 获取map
        HashMap<String,String> map = content.getXmldata();
        //判断map是否是正确的
        if (map.get("error")==null || !map.get("error").equals("true")){

            //转成datalist
            DataList datalist = new DataList(map);
            datalist.setKey(map.get("key"));
            //转成iplayer
            currentIplayer = ContentFactory.tanslationAndStart(
                    UiExcuter.getInstancs().getContext(),
                    UiExcuter.getInstancs().getMainLayout(),
                    datalist,
                    this);//创建必须放入主线程执行
//            createContent(datalist);
        }else{
            Logs.e(TAG,"错误的内容,不可播放");
        }
        //下标 ++
        _index++;
        if (_index==contentArr.size()) _index = 0;
    }
    /**
     * 停止
     */
    public void stop(){
        //还原下标
        _index = 0;
        //清除数据
//        Logs.i(TAG,"----"+layout.getChildren());
        contentArr=null;
        clearContent();
        //清理 内容 放在 主线程
//        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
//            @Override
//            public void call() {
//
//            }
//        });
    }



    /**
     * 清理内容 必须放在主线程
     */
    private void clearContent() {
        if (currentIplayer!=null){
            currentIplayer.stop();
            currentIplayer = null;
        }
      //Logs.i(TAG,"清理 布局 下 的 内容");
    }

    @Override
    public void playOvers(IPlayer play) {
        clearContent();
        start();
    }



}
