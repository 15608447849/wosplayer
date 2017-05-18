package com.wosplayer.Ui.element.definedView;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.ImageView;

/**
 * Created by 79306 on 2017/3/11.
 */

public class Mimage extends ImageView{
    private static final String TAG = "图片加载";

    public Mimage(Context context) {
        super(context);
    }
    //重写系统方法
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        }catch (Exception e){
        }
    }

}
