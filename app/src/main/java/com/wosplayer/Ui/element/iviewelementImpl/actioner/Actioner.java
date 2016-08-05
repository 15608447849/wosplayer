package com.wosplayer.Ui.element.iviewelementImpl.actioner;

import android.content.Context;
import android.view.ViewGroup;

import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.Command.Schedule.correlation.XmlNodeEntity;

/**
 * Created by user on 2016/8/2.
 */
public class Actioner implements IPlayer{

    //上下文对象
    private Context mContext;
    //依附的视图
    private ViewGroup vp;

    //当前的容器
    private Container container;

    public Actioner(Context mContext, ViewGroup vp) {
        this.mContext = mContext;
        this.vp = vp;
    }


    private String muuks = null;
    private DataStore stores;
    @Override
    public void loadData(DataList mp, Object ob) {



        String uuks = mp.GetStringDefualt("uuks","000000000");


       if (muuks==null){
           muuks = uuks;
           DataSeparator.clear();
           //分离数据
           DataSeparator.Split((XmlNodeEntity) ob,null);

       }else{
           if (!muuks.equals(uuks)){
               DataSeparator.clear();
               //分离数据
               DataSeparator.Split((XmlNodeEntity) ob,null);
           }
       }


        //执行视图绑定
        stores = DataSeparator.getDataStore();

        if (stores==null){
            log.e("互动模块 数据获取错误");
            return;
        }



        //创建所有的容器

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void setlayout() {

    }

    @Override
    public DataList getDatalist() {
        return null;
    }

    @Override
    public void Call(String filePath) {

    }

}
