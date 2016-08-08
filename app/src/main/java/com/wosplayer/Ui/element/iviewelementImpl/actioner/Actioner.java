package com.wosplayer.Ui.element.iviewelementImpl.actioner;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;
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
            DataSeparator ds = new DataSeparator();
            ds.Split((XmlNodeEntity) ob,null);
            stores = ds.getDataStore();

            if (stores==null){
                log.e("互动模块 数据 分离错误");
                return;
            }
            //转换 数据变成视图 创建 所有子容器
            ContainerFactory.SettingParam(mp);
            ContainerFactory.TanslateDataToContainer(stores,null);



        }catch (Exception e){
            log.e(TAG, "loaddata() " + e.getMessage());
        }
    }

    @Override
    public void start() {
        try{
            setlayout();//设置布局
           // loadMyImage();
        }catch (Exception e){
            log.e(TAG,"开始:"+e.getMessage());
        }
    }

    @Override
    public void stop() {
        try {
            //移除父视图
            vp.removeView(this);
            isExistOnLayout = false;
          //  removeMyImage();
        }catch (Exception e){
            log.e(TAG,"停止:"+e.getMessage());
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
            log.e(TAG,"设置布局:" + e.getMessage());
        }
    }

    @Override
    public DataList getDatalist() {
        return null;
    }

    @Override
    public void Call(String filePath) {
        //null
    }

}
