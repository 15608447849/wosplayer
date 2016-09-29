package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.IImagePlayer;
import com.wosplayer.Ui.element.iviewelementImpl.ImageViewPicassocLoader;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.excuteBolock.Loader;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import java.io.File;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/6/30.
 */
public class ActiveImagePlayer extends ImageView implements Loader.LoaderCaller, IviewPlayer {


    private static final java.lang.String TAG = "ActiveImagePlayer.class.getName()";
    private Loader load;
    private boolean isloading = false;//是否正在下载中
    private String uriPath;  //网络 uri 地址
    private String localPath; // 本地文件路径
    private Context mcontext ;
    public ActiveImagePlayer(Context context, String uriPath, String localPath) {
        super(context);
        this.mcontext = context;
        this.uriPath = uriPath;
        this.localPath = localPath;

        //设置布局 属性　
        settingLayout(AbsoluteLayout.LayoutParams.MATCH_PARENT, AbsoluteLayout.LayoutParams.MATCH_PARENT, 0, 0);

        //设置 图片显示 方式
//        this.setScaleType(ImageView.ScaleType.FIT_XY);

        //初始化 资源加载者
        load = new Loader();
        load.settingCaller(this);

    }

    /**
     * 设置 layout
     */
    private int x;
    private int y;
    private int h;
    private int w;

    public void settingLayout(int width, int height, int x, int y) {
        this.w = width;
        this.h = height;
        this.x = x;
        this.y = y;

        //设置宽高坐标
        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(w, h, x, y);
        this.setLayoutParams(params);
    }

    /**
     * 添加自己到父控件
     */
    private View mFather;

    @Override
    public void addMeToFather(View Father) {
        if (this.mFather != null) {
            ((AbsoluteLayout) mFather).removeView(this);
            this.mFather = null;
        }
        if (Father != null) {
            this.mFather = Father;
        }

        if (mFather != null) {
            if (mFather instanceof AbsoluteLayout) {
                //容器是个绝对布局的话

                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        ((AbsoluteLayout) mFather).removeView(ActiveImagePlayer.this);
                        ((AbsoluteLayout) mFather).addView(ActiveImagePlayer.this);
                    }
                });


                        //异步加载视图
                        loadingMyImageView();


            }
        }
    }



    /**
     * 从父控件 移除自己
     */
    @Override
    public void removeMeToFather() {

        if (mFather != null) {
            if (mFather instanceof AbsoluteLayout) {
                releativePlayBtn((AbsoluteLayout) mFather);//释放 播放 按钮

                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        //容器是个绝对布局的话
                        ((AbsoluteLayout) mFather).removeView(ActiveImagePlayer.this);
                        mFather = null;
                        //异步释放视图
                        releaseImageViewResouce();

                    }
                });




            }
        }
    }

    private boolean existLoaddingBg = false;
    Bitmap bitmap_Loading = null;
    /**
     * 加载视图
     */
    private void loadingMyImageView() {

        if (mFather == null) {
            return;
        }
        if (isloading){

            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    if(bitmap_Loading==null){
                        try {
                            bitmap_Loading = BitmapFactory.decodeResource(ActiveImagePlayer.this.getResources(), R.drawable.loadding);
                        } catch (Exception e) {
                           log.e("下载 时 图片 异常:"+e.getMessage());
//                            return;
                            bitmap_Loading = BitmapFactory.decodeResource(ActiveImagePlayer.this.getResources(), R.drawable.error);
                        }
                    }
                    BitmapDrawable bd = new BitmapDrawable(ActiveImagePlayer.this.getResources(), bitmap_Loading);
                    ActiveImagePlayer.this.setImageDrawable(bd);
                    existLoaddingBg = true;
                }
            });


            return;
        }

        //  查看本地
        if (fileUtils.checkFileExists(localPath)) {
            //存在
            Call(localPath);
        } else {
            //访问网络
            if (!isloading) {
                load.LoadingUriResource(uriPath,null);
            }

        }
        ;
    }

    /**
     * 自动下载资源
     */
    @Override
    public void AotuLoadingResource() {
        if (fileUtils.checkFileExists(localPath)){
            log.e("互动 image 存在资源");
            if (mFather == null) {
                return;
            }
            Call(localPath);
          return;
        }

        isloading = true; //正在下载中
        log.i(TAG, ActiveImagePlayer.this.toString()+" 加载资源..."+uriPath );
        load.LoadingUriResource(uriPath,null);

    }

    /**
     * 资源释放
     */
    private void releaseImageViewResouce() {
        log.i(TAG, "----------------------互动图片----------------------------------Thread: "+Thread.currentThread().getName() );
        IImagePlayer.removeMyImage(this);
        log.i(TAG, "----------------------互动图片 end----------------------------------Thread: "+Thread.currentThread().getName() );
//        Drawable drawable = this.getDrawable();
//        if (drawable != null && drawable instanceof BitmapDrawable) {
//            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//            Bitmap bitmap = bitmapDrawable.getBitmap();
//            if (bitmap != null && !bitmap.isRecycled()) {
//                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
//                    @Override
//                    public void call() {
//                        ActiveImagePlayer.this.setBackgroundResource(0);
//                    }
//                });
//                drawable.setCallback(null);
//                bitmap.recycle();
//                log.i(TAG, ActiveImagePlayer.this.toString()+" 释放资源..." );
//            }
//        }


    }

    /**
     * 资源回调
     *
     * @param filePath
     */
    @Override
    public void Call(final String filePath) {
        log.i(TAG, "  一个图片 资源 下载结果传递了来了:" + filePath);

        isloading = false; //下载完毕
        if (existLoaddingBg){
            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    //releaseImageViewResouce();
                    if (bitmap_Loading!=null){
                        ActiveImagePlayer.this.setImageDrawable(null);
                        bitmap_Loading.recycle();
                        bitmap_Loading=null;
                    }
                    existLoaddingBg = false;
                }
            });

        }

        if (mFather == null) {
        return;
        }

        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                //releaseImageViewResouce();
                picassoLoaderImager(filePath);
            }
        });


        //releaseImageViewResouce(); //释放资源
       /* Bitmap bitmap = null;

        if (filePath.equals("404")) {//如果找不到资源
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.no_found);
        }else{
            try {
                bitmap =  Picasso.with(DisplayActivity.activityContext).load(filePath).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).config(Bitmap.Config.RGB_565).get();//memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                //bitmap = compressInScaleImage(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (bitmap==null){

            return;
        }
        log.i(TAG, "call() bitmap:" + bitmap.toString());
        final BitmapDrawable drawable = new BitmapDrawable(bitmap);
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                ActiveImagePlayer.this.setImageDrawable(drawable);
            }
        });*/

    }

    /**
     * Android中有四种，分别是：
     ALPHA_8：每个像素占用1byte内存
     ARGB_4444:每个像素占用2byte内存
     ARGB_8888:每个像素占用4byte内存
     RGB_565:每个像素占用2byte内存
     * @param filePath
     */
    private void picassoLoaderImager(String filePath) {

        //ImageAttabuteAnimation.SttingAnimation(mcontext,this);

        log.e(TAG,"互动 -------  loadimageing ------- "+ filePath);
        File file =new File(filePath);
        if (file.exists()){
            //ImageViewPicassocLoader.loadImage(mcontext,this,new File(filePath),null);
            log.e("exists");
        }else{
            log.e("no exists");
        }

        ImageViewPicassocLoader.loadImage(mcontext,this,new File(filePath),null,ImageViewPicassocLoader.TYPE_ACTION_IIMAGE);

        log.e(TAG,"互动 -------  loadimage end -------");
    }

    private WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    private int defaultWidth = wm.getDefaultDisplay().getWidth();

    //重写系统方法

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            log.i(TAG,"试图引用　一个　回收的图片 ["+e.getMessage()+"-"+e.getCause()+"]");
            picassoLoaderImager(localPath);
        }
    }

    @Override
    protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();
       // setImageDrawable(null);
    }

    /**
     *加载一个  播放按钮
     */
    private Button playVideoBtn ;
    private RelativeLayout relative;
    public Button getPlayVideoBtn (){
        if (mFather!=null){
        if(playVideoBtn == null){

            playVideoBtn = new Button(DisplayActivity.activityContext);

            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.play);
            BitmapDrawable bd = new BitmapDrawable(this.getResources(), bitmap);
            playVideoBtn.setBackgroundDrawable(bd); //背景图

            if (relative == null){
                 relative = new RelativeLayout(DisplayActivity.activityContext);
                relative.setLayoutParams(new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.MATCH_PARENT, AbsoluteLayout.LayoutParams.MATCH_PARENT,0,0));
            }
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            playVideoBtn.setLayoutParams(lp);

            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {

                    relative.addView(playVideoBtn); //添加到相对布局中
                    ((ViewGroup) ActiveImagePlayer.this.mFather).addView(relative);
                }
            });
        }
        }
        return playVideoBtn;
    }


    /**
     * 释放 播放 按钮
     */
    private void releativePlayBtn(final AbsoluteLayout view){
        if (playVideoBtn == null){
            return;
        }

        //如果返回按钮存在. 移除

        Drawable drawable = playVideoBtn.getBackground();
        if (drawable!=null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable != null) {
                if (!bitmapDrawable.getBitmap().isRecycled()) {
                    bitmapDrawable.getBitmap().recycle();
                    bitmapDrawable.setCallback(null);
                    AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                        @Override
                        public void call() {
                            playVideoBtn.setBackgroundResource(0);
                        }
                    });

                }
            }
        }

        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                if (relative!=null){
                    relative.removeView(playVideoBtn);
                    view.removeView(relative);
                    relative = null;
                }
                playVideoBtn = null;
            }
        });
    }
}
