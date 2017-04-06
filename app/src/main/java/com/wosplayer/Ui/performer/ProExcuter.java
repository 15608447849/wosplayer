package com.wosplayer.Ui.performer;

import com.wosplayer.command.operation.schedules.correlation.XmlNodeEntity;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by user on 2017/4/5.
 */

public class ProExcuter {
    private static final java.lang.String TAG = "节目执行";

    private static ProExcuter instants;


    /**
     * 获取单例
     */
    public static ProExcuter getInstants(){
        if (instants==null)
            instants = new ProExcuter();
        return instants;
    }

    //布局列表
    private ArrayList<LayoutsExcuter> layoutList = null;
    private ProExcuter(){
        layoutList = new ArrayList<>();

    }


    /**
     * 解析排期数据对象
     */
    public void onParse(XmlNodeEntity program){
        for (XmlNodeEntity layout:program.getChildren()){
            //Logs.i(TAG,"准备创建一个布局执行者:"+layout.getXmldata().get("id"));
            layoutList.add(new LayoutsExcuter(layout));// 可以根据ID写一个布局缓存
        }
    }


    public void onStart(){

        if (layoutList!=null && layoutList.size()>0){
            //执行布局
            Iterator<LayoutsExcuter> itr = layoutList.iterator();
            while (itr.hasNext()){
                itr.next().start();
            }
        }

    }

    public void onStop(){
        //停止布局的执行
        if (layoutList!=null && layoutList.size()>0){
            //执行布局
            Iterator<LayoutsExcuter> itr = layoutList.iterator();
            while (itr.hasNext()){
                itr.next().stop();
                itr.remove();
            }
        }

    }
}
