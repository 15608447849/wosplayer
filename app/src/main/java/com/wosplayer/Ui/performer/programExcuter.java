package com.wosplayer.Ui.performer;

import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.Command.Schedule.correlation.XmlNodeEntity;

import java.util.ArrayList;

/**
 * Created by user on 2016/7/25.
 * 执行节目
 */
public class programExcuter {

    private static final java.lang.String TAG = "program Excuter";
    private XmlNodeEntity program = null;
    private ArrayList<layoutExcuter> layoutList = null;
    public programExcuter(XmlNodeEntity program) {
        this.program = program;
        layoutList = new ArrayList<layoutExcuter>();
        log.i(TAG,"节目:"+program.getXmldata().get("title")+"创建了");
    }

    public void start(){
        //获取 布局信息 ,创建布局 执行所有的节目
        ArrayList<XmlNodeEntity> layoutArr = program.getChildren();
        if ( layoutArr==null || layoutArr.size()==0){
            log.i(TAG," 当前无布局列表");
            return;
        }
        for (XmlNodeEntity layout:layoutArr){
        log.i(TAG,"准备创建一个布局执行者:"+layout.getXmldata().get("id"));
            createLayout(layout);
        }
    }

    /**
     * 创建layout
     * @param layout
     */
    private void createLayout(XmlNodeEntity layout) {
        layoutExcuter layoutexcuter = new layoutExcuter(layout);
        layoutList.add(layoutexcuter);
        layoutexcuter.start();
    }

    public void stop(){
        //停止所有 布局
        log.i(TAG,"节目:"+program.getXmldata().get("title")+"准备停止了");

        if (layoutList!=null && layoutList.size()!=0 ){
            for (layoutExcuter layoutexcuter:layoutList){
                layoutexcuter.stop();
                layoutexcuter=null;
            }
            layoutList.clear();
            layoutList = null;
        }

        program = null;
    }
}
