package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.mSurfaceview;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;

/**
 * Created by user on 2016/7/27.
 */
public class IrunTextPlayer implements IPlayer{
    private static final java.lang.String TAG = IrunTextPlayer.class.getName();

    private Context mCcontext;
    private ViewGroup mfatherView = null;

    private int x=0;
    private int y=0;
    private int h=0;
    private int w=0;
    private boolean isExistOnLayout = false;

    private mSurfaceview text = null;

    public IrunTextPlayer(Context context, ViewGroup vp){
        mCcontext =context;
        this.mfatherView = vp;
    }

    private DataList mp;

    @Override
    public void loadData(DataList mp, Object ob) {
        try{
            this.mp = mp;
            //创建 surfaceview
            text = new mSurfaceview(mCcontext);

            //背景色
           String bgcolor = mp.GetStringDefualt("bgcolor","#FFFFFF");
            //背景透明图
            int bgalpha = mp.GetIntDefualt("bgalpha",0);
            //文本颜色
            String fontcolor = mp.GetStringDefualt("fontcolor","#000000");
            //文本透明度
            int fontalpha = mp.GetIntDefualt("fontalpha",255);
            //文本大小
            float fontsize = mp.GetIntDefualt("fontsize",20);
            fontsize = fontsize * 2.5f;
            //是否移动
            String ismove = mp.GetStringDefualt("ismove","true");
            boolean isMove = false;
            if (ismove.equals("true")){
                isMove = true;
            }

            //
            boolean loop = isMove;//循环 + 控制线程

            //移动速度
            int speed = mp.GetIntDefualt("speed",15);
            speed = speed * 10;

            //文本内容
            String textcontent = mp.GetStringDefualt("textcontent","抬头仰望天空吧,少年!");
            //文字类型
            String texttype = mp.GetStringDefualt("texttype","pop字体");
            //文字风格
            int textstyle = mp.GetIntDefualt("textstyle", Typeface.BOLD);
            //方向
            int orientation = mp.GetIntDefualt("orientation",mSurfaceview.MOVE_LEFT);

            //设置
            text.setBgColor(bgcolor);
            text.setBgalpha(bgalpha);
            text.setMove(isMove);
            text.setLoop(loop);
            text.setFontColor(fontcolor);
            text.setFontAlpha(fontalpha);
            text.setFontSize(fontsize);
            text.setContent(textcontent);
            text.setOrientation(orientation);
            text.setSpeed(speed);
            text.setFontStyle(textstyle);
            text.setTypeFace(texttype);


            //属性参数
            this.x = mp.GetIntDefualt("x", 0);
            this.y = mp.GetIntDefualt("y", 0);
            this.w = mp.GetIntDefualt("width", 0);
            this.h = mp.GetIntDefualt("height", 0);


        }catch (Exception e){
            log.e(TAG, "loadData :"+ e.getMessage());
        }
    }

    @Override
    public void setlayout() {
        //设置布局
        try{
        if (!isExistOnLayout){
            //添加到上面去
            mfatherView.addView(text);
            isExistOnLayout = true;
        }
            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) text.getLayoutParams();
            lp.x = x;
            lp.y = y;
            lp.width = w;
            lp.height = h;
            text.setLayoutParams(lp);

        }catch (Exception e){
            log.e(TAG, "setlayout :"+ e.getMessage());
        }
    }


    @Override
    public DataList getDatalist() {
        return mp;
    }

    @Override
    public void Call(String filePath) {
        //no
    }


    private Thread textHelper = null;

    @Override
    public void  start(){
        try{
            setlayout();
            if (textHelper == null){
                textHelper = new Thread(text);
            }
            text.setLoop(true);
            textHelper.start();

        }catch (Exception e){
            log.e(TAG, "start :"+ e.getMessage());
        }
    }

    @Override
    public void stop(){
        try{
            if (textHelper!=null){
                text.setLoop(false);
                textHelper=null;
            }
            mfatherView.removeView(text);
            isExistOnLayout=false;
        }catch (Exception e){
            log.e(TAG, "stop :"+ e.getMessage());
        }
    }


}
