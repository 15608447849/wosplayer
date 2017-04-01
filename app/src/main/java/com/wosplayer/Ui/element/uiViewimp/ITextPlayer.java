package com.wosplayer.Ui.element.uiViewimp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.definedView.Mtextscroll;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;

/**
 * Created by user on 2016/7/27.
 */
public class ITextPlayer implements IPlayer{
    private static final java.lang.String TAG = "字幕显示";
    private ViewGroup viewGroup = null;
    private boolean isLayout = false;
    private Mtextscroll text = null;

    public ITextPlayer(Context context, ViewGroup vp){
        this.viewGroup = vp;
        //创建 surfaceview
        text = new Mtextscroll(context);
    }
    @Override
    public void loadData(DataList mp) {
        try{
            //属性参数
            AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(
                    mp.GetIntDefualt("width", 0),
                    mp.GetIntDefualt("height", 0),
                    mp.GetIntDefualt("x", 0),
                    mp.GetIntDefualt("y", 0)
            );
            //背景色
           String bgcolor = mp.GetStringDefualt("bgcolor","#FFFFFF");
            //背景透明度
            double bgalpha = mp.GetdoubleDefualt("bgalpha",0.0);
            //文本颜色
            String fontcolor = mp.GetStringDefualt("fontcolor","#000000");
            //文本透明度
            double fontalpha = mp.GetdoubleDefualt("fontalpha",1.0);
            //文本大小
            float fontsize = mp.GetIntDefualt("fontsize",20);
            fontsize = fontsize * 2.5f;
            //移动速度
            int speed = mp.GetIntDefualt("speed",15);
            speed = speed * 10;
            //文本内容
            String textcontent = mp.GetStringDefualt("textcontent","暂无内容");
            //文字类型
            String texttype = mp.GetStringDefualt("texttype","幼圆");
            //文字风格
            int textstyle = mp.GetIntDefualt("textstyle", Typeface.BOLD);
            //方向
            int orientation = mp.GetIntDefualt("orientation", Mtextscroll.MOVE_LEFT);

            text.setLayoutParams(layoutParams);
            //设置
            text.setBgColor(bgcolor);//背景颜色
            text.setBgalpha(bgalpha);//背景透明度
            text.setFontColor(fontcolor);//字体颜色
            text.setFontAlpha(fontalpha);//字体透明度
            text.setFontSize(fontsize);//字体大小
            text.setContent(textcontent);//字体内容
            text.setOrientation(orientation);//字体方向
            text.setSpeed(speed);//速度
            text.setFontStyle(textstyle);//字体类型
            text.setTypeFace(texttype);//字体样式
        }catch (Exception e){
            Logs.e(TAG, "loadData :"+ e.getMessage());
        }
    }

    @Override
    public void  start(){
        try{
            if (!isLayout){
                //添加到上面去
                viewGroup.addView(text);
                isLayout = true;
            }
        }catch (Exception e){
            Logs.e(TAG, "start :"+ e.getMessage());
        }
    }

    @Override
    public void stop(){
        try{
            if (isLayout){
                viewGroup.removeView(text);
                isLayout=false;
            }
        }catch (Exception e){
            Logs.e(TAG, "stop :"+ e.getMessage());
        }
    }

    @Override
    public void setTimerCall(TimeCalls timer) {

    }


}
