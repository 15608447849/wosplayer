package com.wosplayer.Ui.element.definedView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wosplayer.app.Logs;

/**
 * Created by Administrator on 2016/7/26.
 * SurfaceView的使用
 * 首先继承SurfaceView，并实现SurfaceHolder.Callback接口，实现它的三个方法：surfaceCreated，surfaceChanged，surfaceDestroyed。
 * <p/>
 * <p/>
 * surfaceCreated(SurfaceHolder holder)：surface创建的时候调用，一般在该方法中启动绘图的线程。
 * surfaceChanged(SurfaceHolder holder, int format, int width,int height)：surface尺寸发生改变的时候调用，如横竖屏切换。
 * surfaceDestroyed(SurfaceHolder holder) ：surface被销毁的时候调用，如退出游戏画面，一般在该方法中停止绘图线程。
 * 还需要获得SurfaceHolder，并添加回调函数，这样这三个方法才会执行。
 */

public class Mtextscroll extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "滚动字幕";
    /**
     *上下文
     */
    private Context context;

    /**
     */
    public Mtextscroll(Context context) {
        super(context);
        this.context = context;
        setZOrderOnTop(true);
//        setZOrderMediaOverlay(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);//透明 前景
        getHolder().addCallback(this);
        setTypeFace(null);
    }

    /**
     * 线程控制 循环 绘制
     */
    private boolean loop = false;

    /**
     * 执行线程
     */
    private Thread thread;
    private void start(){
        //设置画笔属性
        settingPaint();
        //设置背景参数
        setBg();
        loop = true;
        thread = new Thread(this);
        thread.start();
    }



    private void stop(){
        loop = false;
        thread.interrupt();
        thread = null;
        //移除画笔
        paint = null;
        //移除背景
        this.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        start();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    @Override
    public void run() {
        while (loop) {
            try {

                drawText();
                Thread.sleep(speed);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 画笔
     */
    private Paint paint;

    private void settingPaint(){
        if (paint==null){
//            Log.e(TAG,"设置画笔 - 字体大小:"+fontSize+",字体颜色:"+fontColor+"字体透明度:"+fontAlpha);
            paint = new Paint();
            paint.setAntiAlias(true);//锯齿
            paint.setTypeface(fontType);//字体Typeface.SANS_SERIF
            paint.setTextSize(fontSize);//字体大小
            paint.setColor(fontColor);//字体颜色
            paint.setAlpha(fontAlpha);//字体透明度
        }
    }
    private void setBg() {
//        Log.e(TAG,"设置背景:"+bgalpha+" 颜色值:"+bgColor);
        this.setAlpha(bgalpha);
        this.setBackgroundColor(bgColor);
    }

    /**
     * 绘制
     */
    private synchronized void drawText() {

        //锁定画布
        Canvas canvas = getHolder().lockCanvas();
        if(canvas == null) return;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);   //清屏
        float conlen = paint.measureText(content);
        float distance = fontSize * 0.5f;
        scroll(conlen,distance);
        canvas.drawText(content, x, (getHeight() - MTools.getFontHeight(paint))/2 + MTools.getFontLeading(paint), paint);//画文字*/
        getHolder().unlockCanvasAndPost(canvas);//解锁显示
    }

    //  内容所占像素
    private synchronized void scroll(float conlen,float distance){

        //滚动
        if (isMove) {
            //组件宽度
            int w = getWidth();
            //方向
            if (orientation == MOVE_LEFT) {//向左
                if (x < -conlen) {
                    x = w;
                } else {
                    x -= distance;
                }
            } else if (orientation == MOVE_RIGHT) {//右边
                if (x >= w) {
                    x = -conlen;
                } else {
                    x += distance;
                }
            }
        }
        //Logs.e(TAG,"当前x:"+x+" 移动距离:"+ distance);
    }

    /**
     * 是否滚动
     */
    private boolean isMove = true;
    /**
     * 移动方向
     */
    private int orientation = MOVE_RIGHT;
    /**
     * 向左
     */
    public static final int MOVE_LEFT = 0;
    /**
     * 向右
     */
    public static final int MOVE_RIGHT = 1;
    /**
     * 移动速度 1.5s 一次
     */
    private long speed = 1500;
    /**
     * 字幕内容
     */
    private String content = "暂无内容";
    /**
     * 字幕背景色
     */
    private int bgColor = 0;
    /**
     * 背景透明度
     */
    private float bgalpha = 0;
    /**
     * 字体颜色
     */
    private int fontColor = 0;//"#FFFFFF";
    /**
     * 字体透明度
     * 0-255 透明->不透明
     */
    private int fontAlpha = 255;
    /**
     * 字体大小20
     */
    private float fontSize = 20f;
    /**
     * 字体类型
     * serif 是有衬线字体，意思是在字的笔画开始、结束的地方有额外的装饰，而且笔画的粗细会有所不同。
     * sans serif 就没有这些额外的装饰，而且笔画的粗细差不多
     * monospace
     */
    private Typeface fontType = null;
    /**
     * 字体风格
     * BOLD 加粗
     * BOLD_ITALIC 倾斜加粗
     * ITALIC  倾斜
     * NORMAL 正常
     */
    private int fontStyle = Typeface.BOLD;

    /**
     * 内容滚动位置
     * 起始坐标
     */
    private float x = 0;
    //get - set
    public void setMove(boolean isMove) {
        this.isMove = isMove;
    }

    public void setContent(String content) {
        //Logs.e(TAG,"播放消息:\n"+content);
        //Logs.e(TAG,"长度:"+content.length());
        this.content = content;
    }

    /**
     *背景颜色设置
     */
    public void setBgColor(String bgColors) {
        try {
            this.bgColor = Color.parseColor(bgColors);
        } catch (Exception e) {
            this.bgColor = Color.parseColor("#000000");
        }
    }
    //数值
    public void setBgalpha(double bgalpha) {
//        Logs.e(TAG,"背景参数:"+bgalpha);
        this.bgalpha = (float)(bgalpha<0?0:bgalpha>1?1:bgalpha);
    }
    public void setFontColor(String fontColor) {
        try {
            this.fontColor = Color.parseColor(fontColor);
        }
        catch (Exception e) {
            this.fontColor = Color.parseColor("#FFFFFF");
        }
    }
    public void setFontAlpha(double fontAlpha) {  //  0- 1 => 0-255
        this.fontAlpha =  (int)( (float)(fontAlpha<0?0:fontAlpha>1?1:fontAlpha) ) * 255;
    }
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }
    public void setOrientation(int orientation) {
//        Logs.e(TAG,"移动方向:"+orientation);
        this.orientation = orientation;
    }
    public void setSpeed(long speed) {
        this.speed = speed;
    }
    public void setFontStyle(int fontStyle) {
        this.fontStyle = fontStyle;
    }
    public void setTypeFace(String fonttype) {
        try {
            fontType = Typeface.create(Typeface.createFromAsset(context.getAssets(), "fonts/" + (fonttype == null ? "幼圆.ttf" : fonttype+".ttf") ), fontStyle);
        } catch (Exception e) {
            fontType = Typeface.create(Typeface.createFromAsset(context.getAssets(), "fonts/黑体.ttf" ), fontStyle);
            Logs.e(TAG,"["+fonttype+"] type face err, setting defult typeface :\n"+e.getMessage());
        }
    }

    public Bitmap getBitmap(){
        return null;
    }


}
