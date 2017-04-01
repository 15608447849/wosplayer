package com.wosplayer.Ui.element.definedView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.wosplayer.Ui.element.definedView.MTools.getFontHeight;
import static com.wosplayer.Ui.element.definedView.MTools.getFontLeading;

/**
 * Created by user on 2017/3/28.
 */

public class Mtimes extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "时间显示";

    //os执行
    private final Handler handler = new Handler();

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss"); //时间格式化工具 -> 获取当前时分秒 timeFormat.format(new Date());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日"); //时间格式化工具 -> 获取当前年月日 timeFormat.format(new Date());
    //字体文件路径
    private final String FONT_TYPE_NAME = "fonts/digital-7.ttf";
    //时间 - 时间刷新
    private final Runnable RUNNING_TIMEING = new Runnable() {
        @Override
        public void run() {
            //获取时间
            String tStr = timeFormat.format(new Date());
            //设置文本
            drawTimes(tStr);
            //再次循环
            handler.postDelayed(RUNNING_TIMEING,1);
        }
    };

    //时间 - 日期刷新
    private final Runnable RUNNING_DATEING = new Runnable() {
        @Override
        public void run() {
            //获取日期
            String tStr = dateFormat.format(new Date());
            //设置文本
            drawDates(tStr);
            //再次循环
            handler.postDelayed(RUNNING_DATEING,1000);
        }
    };
    //上下文
    private Context context;
    //字体类型
    private Typeface fontType;
    //时间大小
    private int timeSize = 55;
    //时间颜色
    private int timeColor;
    //时间透明度
    private int timeAlpha = 1;
    //背景颜色
    private int bgColor;
    //背景透明度
    private float bgAlpha = 0;
    //画笔
    Paint paint1,paint2;

    private void settingPaint(){
        if (paint1==null){
//            Log.e(TAG,"设置画笔 - 字体大小:"+fontSize+",字体颜色:"+fontColor+"字体透明度:"+fontAlpha);
            paint1 = new Paint();
            paint1.setAntiAlias(true);//锯齿
            paint1.setTypeface(fontType);//字体Typeface.SANS_SERIF
            paint1.setTextSize(timeSize);//字体大小
            paint1.setColor(timeColor);//字体颜色
            paint1.setAlpha(timeAlpha);//字体透明度
        }
        if (paint2==null){
//            Log.e(TAG,"设置画笔 - 字体大小:"+fontSize+",字体颜色:"+fontColor+"字体透明度:"+fontAlpha);
            paint2 = new Paint();
            paint2.setAntiAlias(true);//锯齿
            paint2.setTypeface(fontType);//字体Typeface.SANS_SERIF
            paint2.setTextSize(timeSize/4+6);//字体大小
            paint2.setColor(timeColor);//字体颜色
            paint2.setAlpha(timeAlpha);//字体透明度
        }
    }
    private void settingBg(){
//        Log.e(TAG,"设置背景:"+bgalpha+" 颜色值:"+bgColor);
            this.setAlpha(bgAlpha);
            this.setBackgroundColor(bgColor);
    }

    public Mtimes(Context context) {
        super(context);
        this.context = context;
        fontType = Typeface.create(Typeface.createFromAsset(context.getAssets(), FONT_TYPE_NAME), Typeface.BOLD);
        setZOrderOnTop(true);//使surfaceview放到最顶层
        getHolder().setFormat(PixelFormat.TRANSLUCENT);//透明 前景->使窗口支持透明度
        getHolder().addCallback(this);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        settingPaint();
        settingBg();
        handler.post(RUNNING_TIMEING);
        handler.post(RUNNING_DATEING);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        handler.removeCallbacks(RUNNING_TIMEING);
        handler.removeCallbacks(RUNNING_DATEING);

    }
    private synchronized void drawTimes(String currentTimes){
        //锁定画布
        Canvas canvas = getHolder().lockCanvas(new Rect(0,0,getWidth(),(getHeight()/4)*3));
        if(canvas == null) return;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);   //清屏

        float textWidth = paint1.measureText(currentTimes);
        canvas.drawText(currentTimes,
                ( getWidth() - textWidth)/2,//x
                ( ((getHeight()/4)*3) - MTools.getFontHeight(paint1))/2 + MTools.getFontLeading(paint1),//y
                paint1);
        getHolder().unlockCanvasAndPost(canvas);//解锁显示
    }

    private synchronized void drawDates(String currentdata){
        //锁定画布
        Canvas canvas = getHolder().lockCanvas(new Rect(0,getHeight()-((getHeight()/4)*3),getWidth(),getHeight()));
        if(canvas == null) return;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);   //清屏

        float textWidth = paint2.measureText(currentdata);
        canvas.drawText(currentdata,
                ( getWidth() - textWidth)/2,  //(getHeight()/4)*3)
                ((getHeight()/4)*3)+(( (getHeight()-((getHeight()/4)*3) ) - getFontHeight(paint2))/2 + getFontLeading(paint2)),
                paint2);
        getHolder().unlockCanvasAndPost(canvas);//解锁显示
    }

    public void setBgAlpha(double bgAlpha) {
        this.bgAlpha = (float)(bgAlpha<0?0:bgAlpha>1?1:bgAlpha);
    }

    public void setBgColor(String bgColors) {

        try {
            this.bgColor = Color.parseColor(bgColors);
        } catch (Exception e) {
            this.bgColor = Color.parseColor("#000000");
        }
    }

    public void setTimeAlpha(double timeAlpha) {
        this.timeAlpha =  (int)( (float)(timeAlpha<0?0:timeAlpha>1?1:timeAlpha) ) * 255;
    }

    public void setTimeColor(String timeColor) {
        try {
            this.timeColor = Color.parseColor(timeColor);
        }
        catch (Exception e) {
            this.timeColor = Color.parseColor("#FFFFFF");
        }
    }

    public void setTimeSize(int timeSize) {
        this.timeSize = timeSize;
    }
}
