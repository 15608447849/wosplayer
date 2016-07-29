package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import com.wosplayer.R;
import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.excuteBolock.Loader;

import java.io.File;

import it.sephiroth.android.library.picasso.MemoryPolicy;
import it.sephiroth.android.library.picasso.Picasso;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by Administrator on 2016/7/24.
 */

public class IImagePlayer extends ImageView implements IPlayer{

    private static final java.lang.String TAG = IImagePlayer.class.getName();
    private Loader loader;
    private Context mCcontext;
    private ViewGroup mfatherView = null;
    private int x=0;
    private int y=0;
    private int h=0;
    private int w=0;
    private boolean isExistOnLayout = false;


    public IImagePlayer(Context context, ViewGroup mfatherView) {
        super(context);
        mCcontext =context;
        //资源加载者
        loader = new Loader();
        loader.settingCaller(this);
        this.mfatherView = mfatherView;
       //设置 图片显示 方式
//        this.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    private String localpath = null;
    private String uri = null;
    private DataList mp = null;
    @Override
    public void loadData(DataList mp, Object ob) {
        try {
            this.mp = mp;
            this.x = mp.GetIntDefualt("x", 0);
            this.y = mp.GetIntDefualt("y", 0);
            this.w = mp.GetIntDefualt("width", 0);
            this.h = mp.GetIntDefualt("height", 0);
            this.localpath = mp.GetStringDefualt("localpath", "");
            this.uri = mp.GetStringDefualt("getcontents", "");
        }catch (Exception e){
            log.e(TAG, "loaddata() " + e.getMessage());
        }
    }



    @Override
    public DataList getDatalist() {
        return mp;
    }
    @Override
    public void setlayout() {

        try {
            if (!isExistOnLayout){
                mfatherView.addView(this);
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


    //主线程中执行
    @Override
    public void start() {
        try{
            setlayout();//设置布局
            loadMyImage();
        }catch (Exception e){
            log.e(TAG,"开始:"+e.getMessage());
        }
    }

    //加载图片
    private void loadMyImage() {
        //先判断文件是不是存在
        if (loader.fileIsExist(localpath)){
//            Call(localpath);
            picassoLoaderImager(localpath);
            return;
        }
        //通过 资源加载者
        loader.LoadingUriResource(uri,null);
    }


    //主线程中执行
    @Override
    public void stop() {
        try {
            //移除父视图
            mfatherView.removeView(this);
            isExistOnLayout = false;
            //移除存在的图片资源
            removeMyImage();
        }catch (Exception e){
            log.e(TAG,"停止:"+e.getMessage());
        }
    }

    //放入主线程
    private void removeMyImage() {
        //资源回调的地方
        Drawable drawable = this.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
//                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
//                    @Override
//                    public void call() {
                        IImagePlayer.this.setBackgroundResource(0);
//                    }
//                });
                drawable.setCallback(null);
                bitmap.recycle();
                log.i(TAG, IImagePlayer.this.toString()+" 释放资源..." );
            }
        }
    }

    @Override
    public void Call(final String filePath) {
        log.i(TAG," 图片资源回传:" + filePath +" 当前所在线程:"+Thread.currentThread().getName()+"正在执行的所有线程数:"+ Thread.getAllStackTraces().size());

      /*  Bitmap bitmap = null;
        if (filePath.equals("404")) {//如果找不到资源
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.no_found);
        }else{
            try {
                bitmap =  Picasso.with(mCcontext).load(filePath).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).config(Bitmap.Config.RGB_565).get();//memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)

            } catch (Exception e) {
                log.e(TAG,"Call() "+e.getMessage() );
            }
            if (bitmap==null){
                return;
            }
            log.i(TAG, "call() bitmap:" + bitmap.toString());

            final BitmapDrawable drawable = new BitmapDrawable(bitmap);
            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    IImagePlayer.this.setImageDrawable(drawable);
                }
            });
        }*/


try {
    AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
        @Override
        public void call() {
            picassoLoaderImager(filePath);
        }
    });
}catch (Exception e){
    log.e(TAG,""+e.getMessage());
}




    }

    private void picassoLoaderImager(String filePath) {
        log.i(TAG,"width:"+w);
        log.i(TAG,"layoutparam w:"+this.getLayoutParams().width);
        log.i(TAG,"getMeasuredWidth:"+this.getMeasuredWidth());
        //纯用picasso 加载本地图片
        Picasso.with(mCcontext)
                .load(new File(filePath))
//                .resize(w-1,h-1)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .centerCrop()
                // .resize(this.getMeasuredWidth(), this.getMeasuredHeight())
                .resize(w,h)
                .placeholder(R.drawable.no_found)
                .error(R.drawable.error)
                .into(this);
        /**.memoryPolicy(NO_CACHE, NO_STORE)
         * 其中memoryPolicy的NO_CACHE是指图片加载时放弃在内存缓存中查找，NO_STORE是指图片加载完不缓存在内存中。
         *        .transform(new Transformation(){

        @Override
        public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
        if (result != source) {
        source.recycle();
        }
        return result;
        }

        @Override
        public String key() {
        return "square()";
        }
        })
         */
    }


    //重写系统方法

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            log.e(TAG,"试图引用　一个　回收的图片 ["+e.getMessage()+"-----"+e.getCause()+"]");
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
            setImageDrawable(null);
        }catch (Exception e){
            log.e(TAG,"onDetachedFromWindow:"+e.getMessage());
        }

    }

}
