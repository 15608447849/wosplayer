package com.wosplayer.Ui.element.uiViewimp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.definedView.mSurfaceview;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;

/**
 * Created by user on 2016/7/27.
 */
public class IrunTextPlayer implements IPlayer{
    private static final java.lang.String TAG = IrunTextPlayer.class.getName();

    private Context context;
    private ViewGroup superView = null;

    private boolean isLayout = false;

    private mSurfaceview text = null;

    public IrunTextPlayer(Context context, ViewGroup vp){
        this.context =context;
        this.superView = vp;
    }


    @Override
    public void loadData(DataList mp, Object ob) {
        try{

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

            //创建 surfaceview
            text = new mSurfaceview(context);
            //属性参数
            int x,y,w,h;
            x = mp.GetIntDefualt("x", 0);
            y = mp.GetIntDefualt("y", 0);
            w = mp.GetIntDefualt("width", 0);
            h = mp.GetIntDefualt("height", 0);
            AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(w,h,x,y);
            text.setLayoutParams(layoutParams);
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
        }catch (Exception e){
            Logs.e(TAG, "loadData :"+ e.getMessage());
        }
    }







    private Thread textHelper = null;

    @Override
    public void  start(){
        try{
            if (!isLayout){
                //添加到上面去
                superView.addView(text);
                isLayout = true;
            }
            if (textHelper == null){
                textHelper = new Thread(text);
            }
            text.setLoop(true);
            textHelper.start();
        }catch (Exception e){
            Logs.e(TAG, "start :"+ e.getMessage());
        }
    }

    @Override
    public void stop(){
        try{
            if (textHelper!=null){
                text.setLoop(false);
                textHelper=null;
            }
            if (isLayout){
                superView.removeView(text);
                isLayout=false;
            }
        }catch (Exception e){
            Logs.e(TAG, "stop :"+ e.getMessage());
        }
    }
    //时间回调
    private TimeCalls timeCalls = null;

    @Override
    public void setTimerCall(TimeCalls timer) {
        timeCalls = timer;
    }

    @Override
    public void unTimerCall() {
        timeCalls = null;
    }

}
