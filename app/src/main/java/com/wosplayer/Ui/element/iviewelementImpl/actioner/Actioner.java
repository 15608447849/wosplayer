package com.wosplayer.Ui.element.iviewelementImpl.actioner;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.actioner.ContainerItem.LayoutContainer;
import com.wosplayer.Ui.performer.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.cmdBroadcast.Command.Schedule.correlation.XmlNodeEntity;

/**
 * Created by user on 2016/8/2.
 */
public class Actioner extends AbsoluteLayout implements IPlayer{

    private static final java.lang.String TAG = "_Actioner";
    //上下文对象
    private Context mContext;

    //依附的视图
    private ViewGroup vp;

    //布局属性
    private int x=0;
    private int y=0;
    private int h=0;
    private int w=0;

    //是否已经布局在父容器上面
    private boolean isExistOnLayout = false;

    //当前的容器
    private Container container;

    public Actioner(Context mContext, ViewGroup vp) {
        super(mContext);
        this.mContext = mContext;
        this.vp = vp;
    }



    private DataStore stores;//数据存储
    @Override
    public void loadData(DataList mp, Object ob) {
        try {
            this.x = mp.GetIntDefualt("x", 0);
            this.y = mp.GetIntDefualt("y", 0);
            this.w = mp.GetIntDefualt("width", 0);
            this.h = mp.GetIntDefualt("height", 0);

            // 分离 数据
            DataSeparator dst = new DataSeparator();
            dst.Split((XmlNodeEntity) ob,null);

            if (dst==null){
                Logs.e(TAG,"互动模块 数据 分离错误 DataSeparator is null");
                return;
            }
            stores = dst.getDataStore();

            if (stores == null){
                Logs.e(TAG,"互动模块 数据 分离错误 stores is null");
                return;
            }
            //转换 数据变成视图 创建 所有子容器
            ContainerFactory.SettingParam(mp);
           container = ContainerFactory.TanslateDataToContainer(stores,null);
            if (container==null){
                Logs.e(TAG," 互动模块 容器 获取错误,container="+container);
            }
            Logs.i(TAG," 互动模块 初始化 完成");


        }catch (Exception e){
            Logs.e(TAG, "loaddata() " + e.getMessage());
        }
    }

    @Override
    public void start() {
        try{
            setlayout();//设置布局
            loadContainer();
        }catch (Exception e){
            Logs.e(TAG,"开始:"+e.getMessage());
        }
    }



    @Override
    public void stop() {
        try {
            //移除父视图
            vp.removeView(this);
            isExistOnLayout = false;
            removeContainer();
        }catch (Exception e){
            Logs.e(TAG,"停止:"+e.getMessage());
        }
    }
    //加载容器
    private void loadContainer() {
        if (container!=null){
            if (container instanceof LayoutContainer){
                container.onBind(this);
            }
        }
    }
    //移除容器
    private void removeContainer() {
        if (container!=null){
            if (container instanceof LayoutContainer){
                container.onUnbind();
            }
        }
    }

    @Override
    public void setlayout() {
        try {
            if (!isExistOnLayout){
                this.setBackgroundColor(Color.YELLOW);
                vp.addView(this);
                isExistOnLayout = true;
            }

            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) this
                    .getLayoutParams();
            lp.x = x;
            lp.y = y;
            lp.width = w;
            lp.height = h;
            this.setLayoutParams(lp);

        } catch (Exception e) {
            Logs.e(TAG,"设置布局:" + e.getMessage());
        }
    }

    @Override
    public DataList getDatalist() {
        return null;
    }

    @Override
    public void setTimerCall(TimeCalls timer) {

    }

    @Override
    public void unTimerCall() {

    }


}
