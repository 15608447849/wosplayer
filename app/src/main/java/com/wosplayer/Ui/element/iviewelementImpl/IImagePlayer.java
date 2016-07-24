package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.wosplayer.loadArea.loaderManager;

import it.sephiroth.android.library.picasso.MemoryPolicy;
import it.sephiroth.android.library.picasso.Picasso;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

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
    public IImagePlayer(Context context, ViewGroup mfatherView) {
        super(context);
        mCcontext =context;
        //资源加载者
        loader = new Loader();
        loader.settingCaller(this);
        this.mfatherView = mfatherView;
        mfatherView.addView(this);
        this.setlayout();//设置布局
        //设置 图片显示 方式
        this.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    private String uri = null;
    @Override
    public void loadData(DataList mp) {
        this.x = mp.GetIntDefualt("x", 0);
        this.y = mp.GetIntDefualt("y", 0);
        this.w = mp.GetIntDefualt("width", 0);
        this.h = mp.GetIntDefualt("height", 0);
        this.uri = mp.GetStringDefualt("sourece_uri", "");
    }

    @Override
    public void setlayout() {

        try {
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
        //通过 资源加载者
        loader.LoadingUriResource(uri,null);
    }


    @Override
    public void stop() {
        try {
            //移除父视图
            mfatherView.removeView(this);
            //移除存在的图片资源
            Schedulers.newThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    //异步释放视图
                    removeMyImage();
                }
            });
        }catch (Exception e){
            log.e(TAG,"停止:"+e.getMessage());
        }

    }

    private void removeMyImage() {
        //资源回调的地方
        Drawable drawable = this.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        IImagePlayer.this.setBackgroundResource(0);
                    }
                });
                drawable.setCallback(null);
                bitmap.recycle();
                log.i(TAG, IImagePlayer.this.toString()+" 释放资源..." );
            }
        }
    }

    @Override
    public void Call(String filePath) {
        log.i(TAG, IImagePlayer.this.toString()+"  一个图片 资源 传递了来了:" + filePath);
        Bitmap bitmap = null;
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
        }
    }
}
