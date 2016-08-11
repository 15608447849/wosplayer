package com.wosplayer.Ui.element.iviewelementImpl.actioner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.wosplayer.app.log;

import java.util.List;

import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Created by user on 2016/8/2.
 * “容器”的作用是帮助我们展示一项内容并处理后退操作
 */
public abstract  class Container {
    public static Scheduler.Worker threadHeaper = Schedulers.io().createWorker();
    public static final String TAG = "_actionContainer";
    protected View view;
    protected List<Container> childs ;//要显示在上面的子view
    protected Container previous;//上一个视图
    protected Container next;//下一个视图
    protected boolean isBind = false;
    protected boolean isLayout =false;
    protected Context context;
    protected ViewGroup vp;


   protected abstract float[]  onSettingScale (int fwidth,int fheight);
   protected abstract void  onSettingScale (float widthScale,float heightScale);
   protected abstract void  onBg (ViewGroup vp);
   protected abstract void  onUnbg (ViewGroup vp);

   protected abstract void  onLayout (ViewGroup vp);
   protected abstract void  onUnlayout ();
   public abstract void  onBind(ViewGroup vp);
   public abstract void  onUnbind();
   protected abstract void onClick(View v);
   protected abstract void onBack(View v);
   protected abstract void addChilds(Container child);
   public ViewGroup getVp(){
        return vp;
   }

    /**
     *   释放资源背景
     * //被移除时调用
     */
    protected void releasSourceBg(View v) {
        try{
            Drawable drawable = view.getBackground();
            if (drawable != null && drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    drawable.setCallback(null);
                    bitmap.recycle();
                    view.setBackgroundResource(0);
                    log.i(TAG, "释放背景资源 over");
                }
            }
        }catch (Exception e){
            log.e(TAG,e.getMessage());
        }
    }


}
