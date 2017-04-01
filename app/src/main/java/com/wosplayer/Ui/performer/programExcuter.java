package com.wosplayer.Ui.performer;

import com.wosplayer.app.Logs;
import com.wosplayer.command.operation.schedules.correlation.XmlNodeEntity;

import java.util.ArrayList;

/**
 * Created by user on 2016/7/25.
 * 执行节目
 */
public class programExcuter {

    private static final java.lang.String TAG = "节目执行";
    private XmlNodeEntity program = null;
    private ArrayList<layoutExcuter> layoutList = null;
    private String bgInfo;
    //传递一个节目 数据进来
    public programExcuter(XmlNodeEntity program) {
        this.program = program;
        layoutList = new ArrayList<>();
       // Logs.i(TAG,"节目:"+program.getXmldata().get("title")+"创建了");
        //获取背景信息
        getBgInfos();
    }
    public void getBgInfos() {
        if (program==null || "".equals(program)){
            return;
        }
        //查看背景是图片还是颜色
        bgInfo = program.getXmldata().get("bgimage");
        //如果是图片
        if (bgInfo==null || bgInfo.equals("")){
            //如果是颜色
            bgInfo = program.getXmldata().get("bgcolor");
        }else{
            bgInfo = UiExcuter.getInstancs().basepath+bgInfo;
        }
        //Logs.e(TAG,"节目背景 - "+bgInfo);
    }

    public void onStart(){

        //设置背景给 main acticvity
        UiExcuter.getInstancs().setMainBg(bgInfo);

        //获取 布局信息 ,创建布局 执行所有的节目
        ArrayList<XmlNodeEntity> layoutArr = program.getChildren();
        if ( layoutArr==null || layoutArr.size()==0){
            Logs.i(TAG," 当前无布局列表");
            return;
        }
        for (XmlNodeEntity layout:layoutArr){
        //Logs.i(TAG,"准备创建一个布局执行者:"+layout.getXmldata().get("id"));
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

    public void onStop(){
        //停止所有 布局
       // Logs.i(TAG,"节目:"+program.getXmldata().get("title")+"准备停止了");

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
