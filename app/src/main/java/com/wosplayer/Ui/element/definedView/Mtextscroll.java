package com.wosplayer.Ui.element.definedView;

import android.content.Context;
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
     * 线程控制 循环绘制
     */
    private boolean loop = false;

    private Thread thread;

    private void start(){
        loop = true;
        thread = new Thread(this);
        thread.start();
    }
    private void stop(){
        loop = false;
        thread.interrupt();
        thread = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
       // Log.i(TAG, "suface created (): [" + this.getHeight()+","+this.getWidth()+"]");
//
//        if (isMove) {//滚动效果
//            if (orientation == MOVE_LEFT) {//向左
//                x = getWidth();
//            } else if (orientation == MOVE_RIGHT){//向右
//                x = -(content.length() * 10);
//            }
//        } else {//不滚动只画一次
//            draw();
//        }
        start();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        loop = false;//停止线程
        stop();
    }

    @Override
    public void run() {
        while (loop) {
            try {
                draw();
                Thread.sleep(speed);
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * -----------------------------------------------------------overriee-----------------------------------------------------------------------------------------------------
     * <p/>
     * /* *
     * 绘制
     */
    private synchronized void draw() {
       // log.i(TAG, "draw: start");
        //锁定画布
        Canvas canvas = getHolder().lockCanvas();
        Paint paint = new Paint();
        float conlen = paint.measureText(content);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);   //清屏
        paint.setAntiAlias(true);//锯齿
        paint.setTypeface(fontType);//字体Typeface.SANS_SERIF
        paint.setTextSize(fontSize);//字体大小
        paint.setColor(Color.parseColor(fontColor));//字体颜色
        paint.setAlpha(fontAlpha);//字体透明度
        canvas.drawText(content, x, (getHeight() / 2 + 5), paint);//画文字
        getHolder().unlockCanvasAndPost(canvas);//解锁显示
       // log.i(TAG, "draw: end");
        scroll(conlen);
    }
    //  内容所占像素
    private synchronized void scroll(float conlen){
        //滚动
        if (isMove) {
            //组件宽度
            int w = getWidth();
            //方向
            if (orientation == MOVE_LEFT) {//向左
                if (x < -conlen) {
                    x = w;
                } else {
                    x -= 2*5;
                }
            } else if (orientation == MOVE_RIGHT) {//右边
                if (x >= w) {
                    x = -conlen;
                } else {
                    x += 2*5;
                }
            }
        }
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
    private String bgColor = "#E7E7E7";
    /**
     * 背景透明度
     */
    private int bgalpha = 60;
    /**
     * 字体颜色
     */
    private String fontColor = "#000000";//"#FFFFFF";
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
        this.content = content;
    }
    /**
     * #ffffff 白色
     * #000000 黑色
     * @param bgColor
     */
    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
        //背景颜色
        setBackgroundColor(Color.parseColor(bgColor));
    }
    //数值
    public void setBgalpha(int bgalpha) {
        this.bgalpha = bgalpha;
        //背景透明度
        getBackground().setAlpha(bgalpha);
    }
    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }
    public void setFontAlpha(int fontAlpha) {
        this.fontAlpha = fontAlpha;
    }
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }
    public void setOrientation(int orientation) {
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
            String fontname = fonttype == null ? "华文行楷.ttf" : fonttype+".ttf";
            fontType = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontname);//Typeface.MONOSPACE;
            fontType = Typeface.create(fontType, fontStyle);
        } catch (Exception e) {
            Logs.e(TAG,"["+fonttype+"] type face err, setting defult typeface .");
            fontType = Typeface.SERIF;
        }
    }
}
