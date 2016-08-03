package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.beads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.IinteractionPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.iCache.InteractionCache;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans.ActiveViewPagers;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.xml.XmlParse;
import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.loadArea.excuteBolock.Loader;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.sephiroth.android.library.picasso.MemoryPolicy;
import it.sephiroth.android.library.picasso.Picasso;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by Administrator on 2016/6/28.
 * button
 */

public class ButtonActive extends ImageButton implements View.OnClickListener, Loader.LoaderCaller, IviewPlayer {

    private static final java.lang.String TAG = ButtonActive.class.getName();
    public int x, y, w, h; //大小坐标
    public int bindtype; //绑定类型
    public String bindid; //绑定id
    public String layoutUri;
    public String folderurl;
    private Loader loader; //加载资源操作者
    private HttpUtils http = IinteractionPlayer.http;//网络连接使用
    private String myBgUri = null; //我的背景uri
    private Context mcontext;
    /**
     * @param context
     * @param x
     * @param y
     * @param w
     * @param h
     * @param bindtype  按钮绑定的文件类型
     * @param bindid    绑定的id
     * @param layoutUri 如果是 布局 对应的资源文件uri
     * @param folderurl 如果是 文件 对应的资源文件uri
     */
    public ButtonActive(Context context, int x, int y, int w, int h, int bindtype, String bindid, String layoutUri, String folderurl) {
        super(context);
        this.mcontext = context;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.bindtype = bindtype;
        this.bindid = bindid;
        this.layoutUri = layoutUri;
        this.folderurl = folderurl;
        loader = new Loader();
        loader.settingCaller(this);
        this.setOnClickListener(this);
        this.setEnabled(false);//不可点击
    }



    private IinteractionPlayer CanvasView; //按钮绑定的视图 显示的 容器
    private List<IviewPlayer> myBindFileViews;//绑定的文件视图
    private LayoutActive myBindLayoutView;
    private ActiveViewPagers mvp;//滑动控件　－　当绑定类型是文件　使用！
    private Button retenbtn;//返回按钮
    private FrameLayout fLayout;//存放返回按钮


    /**
     * 设置 我的 画布
     * 如果有人点击我了.
     * 1.移除 画布当前视图(不删除视图栈), 我就把我绑定的的视图放上去, 从数组第一个开始
     *
     * @param vanvas
     */
    public void setMeCanvasView(IinteractionPlayer vanvas) {
        this.CanvasView = vanvas;
    }

    public void removeMeCanvasView() {
        if (this.CanvasView != null) {
            this.CanvasView = null;
        }
    }

    /**
     * 自己的 点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (UiExcuter.isStoping){
            log.e("清理布局中.....");
            return;
        }

        log.i(TAG, "按钮 click:[" + this.toString() +"] ,绑定id:"+bindid);
        if (CanvasView == null) {
            log.e(TAG," 互动模块容器 不存在");
            return;
        }

        if (retenbtn == null) {
            //添加一个返回按钮　
            retenbtn = new Button(DisplayActivity.activityContext);
            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.back);
            BitmapDrawable bd = new BitmapDrawable(this.getResources(), bitmap);
            retenbtn.setBackgroundDrawable(bd);
            retenbtn.setLayoutParams(new AbsoluteLayout.LayoutParams(60, 60, 0, 0));
            retenbtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //删除自己的存在
                    CanvasView.removeView(fLayout);
                    //返回上一个视图
                    CanvasView.returnPrevionsView();
                    releasedSubViewResource();
                }
            });
            fLayout = new FrameLayout(DisplayActivity.activityContext);
            fLayout.setLayoutParams(new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                    CanvasView.getLayoutParams().width -75,
                    CanvasView.getLayoutParams().height -75));
            fLayout.addView(retenbtn);
        }


        // 如果有人点击我了. 1.移除 画布当前视图(不删除视图栈), 我就把我绑定的的视图放上去,
        switch (bindtype) {
            case 0: //排版
            case 2:
                if(myBindLayoutView==null){
                    log.e(TAG,"当前按钮 所 绑定的 下一级 布局层 不存在");
                    return;
                }
                myBindLayoutView.addReturnButton(fLayout);
                myBindLayoutView.addMeToFather(CanvasView);
                CanvasView.setmCurrentView((View) myBindLayoutView);

                break;
            case 3://网页
              /*if (myBindFileViews == null || myBindFileViews.size() < 1) {
                    return;
                }
                if (myBindFileViews.size()==1 && myBindFileViews.get(0) instanceof iWebPlayer){
                    //网页
                    IviewPlayer iv = myBindFileViews.get(0);
                    iv.addMeToFather(CanvasView);
                    CanvasView.setmCurrentView((View) iv);
                }*/

//                break;
            case 1://文件 (视频, 图片,网站)

                if (myBindFileViews == null || myBindFileViews.size() < 1) {
                    return;
                }

              /*  if (myBindFileViews.size()==1 && myBindFileViews.get(0) instanceof iWebPlayer){
                        //网页
                    IviewPlayer iv = myBindFileViews.get(0);
                    iv.addMeToFather(CanvasView);
                    CanvasView.setmCurrentView((View) iv);
                    return;
                }*/
                if (mvp == null) {
                    //滑动控件是空的
                    mvp = new ActiveViewPagers(DisplayActivity.activityContext);//第一次创建滑动控件
                    //加载需要显示的资源
                        for(IviewPlayer iview:myBindFileViews){
                            log.i(TAG,"滑动控件 添加: "+iview);
                            mvp.addMeSubView(iview);
                        }
                }

                mvp.setMyReturnBtn(fLayout);//添加返回键
                mvp.addMeToFather(CanvasView); //滑动控件　加到画布上
                CanvasView.setmCurrentView(mvp); //设置当前视图
                break;
        }


    }

    /**
     * 1
     * 加载 自己的 资源文件
     * 在我显示出来的时候调用
     * 请放入 新线程中
     */
    private void LoadingSrc() {
        log.i(TAG," 互动执行者 绑定的视图>子项-按钮>加载自己的xml文件");
        //根据类型拼接 URI 0 2排版 1 文件夹 3web
        String srcUri = bindtype == 0 || bindtype == 2 ? layoutUri + bindid : folderurl + bindid;
        log.i(TAG, "----bindtype--- " + bindtype + "----bindid--" + bindid + "---->" + srcUri);
        getXmlData(srcUri);
    }

    /**
     *
     * 移除资源
     * 请放入 新线程中
     */
    private void removeSrc() {
        releasedSubViewResource();
        if (myBindFileViews!=null){
            myBindFileViews.clear();
            myBindFileViews = null;
        }
        releaseBgImage();
    }

    /**
     * 释放子项所占资源
     */
    private void releasedSubViewResource() {

        //如果滑动控件存在 移除
        if (mvp != null) {
            //mvp.removeMeToFather();
            mvp = null;
        }

        //如果视图 文件子视图列表 有视图的话 -> 移除
        if (myBindFileViews != null && myBindFileViews.size() > 0) {

            for (IviewPlayer mv : myBindFileViews) {
                    mv.removeMeToFather();
            }

            //如果 排版布局 视图有视图的话 清理
            if(myBindLayoutView!=null){
                myBindLayoutView.removeMeToFather();
            }

            //如果返回按钮存在. 移除
            if (retenbtn != null) {

                Drawable drawable = retenbtn.getBackground();
                if (drawable!=null){
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    if (bitmapDrawable!=null){

                        if (!bitmapDrawable.getBitmap().isRecycled()) {
                            bitmapDrawable.getBitmap().recycle();
                            bitmapDrawable.setCallback(null);
                            retenbtn.setBackgroundResource(0);
                        }
                    }

                }
                if (fLayout!=null){
                    AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                        @Override
                        public void call() {
                            fLayout.removeView(retenbtn);
                            fLayout=null;
                            retenbtn = null;
                        }
                    });
                }

            }

        }
    }

    /**
     * 释放背景图片
     */
    private void releaseBgImage() {
        //释放背景图片
        Drawable drawable = this.getBackground();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {

                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        ButtonActive.this.setBackgroundResource(0);
                    }
                });
                drawable.setCallback(null);
                bitmap.recycle();
                log.i(TAG, "释放资源bg...");

            }
        }
    }

    private LayoutActive mFather;

    /**
     * 自动加载资源
     */
    @Override
    public void AotuLoadingResource() {
    //null
    }

    /**
     * 把自己 加载到我的父控件上面
     */
    @Override
    public void addMeToFather(View Father) {
        if (Father != null) {
            this.mFather = (LayoutActive) Father;
        }
        if (mFather != null) {
            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    log.i(TAG," 互动执行者 绑定的视图>>子项button>>按钮>>加载自己到布局层");
                    mFather.removeView(ButtonActive.this);
                    mFather.addView(ButtonActive.this);
                }
            });


            //加载资源
           IinteractionPlayer.worker.schedule(new Action0() {
                @Override
                public void call() {
                    LoadingSrc(); //加载
                }
            });
        }

    }


    private int nw,nh,nx,ny;
    /**
     * 设置布局属性
     * @param wScale
     * @param hScale
     */
    public void setMyLayoutparam(float wScale,float hScale) {

         nw =  (int)((float)this.w* wScale );
         nh =(int)((float)this.h* hScale );
         nx = (int)((float)this.x* wScale );
         ny = (int)((float)this.y* hScale );

        this.setLayoutParams(
                new AbsoluteLayout.LayoutParams(
                      nw ,nh,nx,ny
            )
        );

        log.i(TAG, wScale+","+hScale+"按钮" + this.bindid+"---"+this.toString() + "设置layout属性,原大小[" + this.w + "," + this.h + "," + this.x + "," + this.y+"]现在大小:["+nw+","+nh+","+nx+","+ny+"]");
    }

    /**
     * 把自己从父控件上面 移除
     */
    @Override
    public void removeMeToFather() {
        if (mFather != null) {
            IinteractionPlayer.worker.schedule(new Action0() {
                @Override
                public void call() {
                    removeSrc();
                }
            });

            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    mFather.removeView(ButtonActive.this);
                    mFather = null;
                }
            });

        }
    }

    /**
     * 获取xml资源文件
     *
     * @param uri
     */
    private void getXmlData(String uri) {
        //看缓存
        final String result = InteractionCache.pull(uri);
        if (result!=null){
           IinteractionPlayer.worker.schedule(new Action0() {
                @Override
                public void call() {
                    //进行XML解析
                    ParseResultXmlGetBgUri(result); //得到 背景图片
                    //加载 自己 绑定 的视图
                    loadMeBindView(result); //得到绑定的子视图
                }
            });
            return;
        }

        log.e(TAG,"xml 返回结果 null");
       final String key = uri;
        http.send(
                HttpRequest.HttpMethod.GET,
                uri,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String xml = responseInfo.result;
                        InteractionCache.push(key,xml);//存
                        //进行XML解析
                        ParseResultXmlGetBgUri(xml); //得到 背景图片
                        //加载 自己 绑定 的视图
                        loadMeBindView(xml); //得到 绑定的子视图

                    }
                    @Override
                    public void onFailure(HttpException e, String s) {
                        log.e(TAG, s);
                    }
                }
        );
    }

    /**
     * 解析
     *
     * @param xml
     * @return
     */
    private void ParseResultXmlGetBgUri(final String xml) {
        log.i(TAG," 互动执行者 绑定的视图>子项>按钮>>加载自己的背景资源");
        try {
        switch (bindtype) {
            case 0: //排版
            case 2:
                myBgUri = XmlParse.interactionParse_ButtonSrcUri_LayoutType(xml);
                log.i(TAG, this.bindid + "是布局类型, 获取的xml : " + xml);
                break;
            case 1://文件夹
            case 3://网页
                    myBgUri = XmlParse.interactionParse_ButtonSrcUri_FildType(xml);
                break;
        }
            loadMeBackImage(); //自己的背景图
        } catch (Exception e) {

            log.e(TAG,""+e.getMessage());
        }

    }

    /**
     * 加载自己的背景图片
     */
    private void loadMeBackImage() {
        if (myBgUri == null) {
            log.e(TAG,"背景图片uri不存在");
            return;
        }
            log.i(TAG, "bg image uri " + myBgUri);
            String filename = myBgUri.substring(myBgUri.lastIndexOf("/") + 1);
            final String loacPath = wosPlayerApp.config.GetStringDefualt("basepath", "") + filename;

            if (fileUtils.checkFileExists(loacPath)) {
                //存在 直接设置
                Call(loacPath);
            } else {
                //下载
                loader.LoadingUriResource(myBgUri,null);
            }
    }

    /**
     * 加载自己的绑定的视图 或者 文件夹
     * 请放入其他线程
     */
    private void loadMeBindView(String xml) {
        log.i(TAG," 互动执行者 绑定的视图...的子项...按钮..加载自己的子项");
        //解析 得到 子视图信息
        try {
        switch (bindtype) {
            case 0: //排版 传进来的 排版的xml数据
            case 2:

               myBindLayoutView =  XmlParse.interactionParse_one(xml); //1 先解析
                log.i(TAG,"获取到一个 绑定布局视图:"+myBindLayoutView);
                //在 需要显示的时候 去加载他的 子资源
                this.setEnabled(true);
                log.i(TAG, bindid+"可以点击了");
                break;

            case 1://文件夹
            case 3://web

                    final List<FileActive> arr = XmlParse.interactionParse_Button_Item_View_FildType(xml);//返回所有的 file
                    if (myBindFileViews == null) {
                        myBindFileViews = Collections.synchronizedList(new ArrayList<IviewPlayer>());
                    } else {
                        myBindFileViews.clear();
                    }

                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        ////////////////////////
                        for (int i = 0; i < arr.size(); i++) {

                            try {
                                IviewPlayer view = arr.get(i).TanslateInfoToView();//转化视图
                                if (view != null) {
                                    myBindFileViews.add(view);//把视图 保存
                                }
                                if (i == arr.size() - 1) {//最后一个
                                    ButtonActive.this.setEnabled(true);
                                    log.i(TAG, bindid+"可以点击了");
                                }
                            }catch (Exception e){
                                log.e(" "+e.getMessage());
                                continue;
                            }
                        }

                        if (bindtype == 1) {//文件类型  图片加视频
                            try {

                                log.i(TAG,myBindFileViews.size()+" ");
                                //开启资源下载
                                for (final IviewPlayer ip:myBindFileViews){
                                    log.i(TAG,ip.toString());
                                    log.i(TAG,"准备开启一个下载任务,当前所在线程:" + Thread.currentThread().getName());
                                    ip.AotuLoadingResource();
                                }
                            }catch (Exception e){
                                log.e(TAG,"捕获一个 Exception... 3  "+ e.getMessage() );
                            }

                        }
                        /////////////////////////
                    }
                });



                break;
        } //switch 结束
        } catch (Exception e) {
          log.e(TAG,""+e.getMessage());
        }

    }

    /**
     * 回调资源
     *
     * @param filePath
     */
    @Override
    public void Call(final String filePath) {
        log.i(TAG, "Button Active  一个图片 资源 下载结果传递了来了:" + filePath);

        if (mFather == null) {
            log.e(TAG,"互动模块 button 无父容器");
            return;
        }
//        releaseBgImage();
    /*    Bitmap bitmap = null;
            try {
                bitmap =  Picasso.with(DisplayActivity.activityContext).load(filePath).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).get();//.config(Bitmap.Config.RGB_565)
            } catch (Exception e) {
                log.e(TAG,""+e.getMessage());
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.no_found);
            }

        final BitmapDrawable drawable = new BitmapDrawable(bitmap);
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                ButtonActive.this.setBackgroundDrawable(drawable);
            }
        });*/

        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                picassoLoaderImager(filePath);
            }
        });
    }
    private void picassoLoaderImager(String filePath) {
        log.i(TAG,"width:"+w);
        log.i(TAG,"current width :"+nw);
        log.i(TAG,"layoutparam w:"+this.getLayoutParams().width);
        log.i(TAG,"getMeasuredWidth:"+this.getMeasuredWidth());
        //纯用picasso 加载本地图片
        Picasso.with(mcontext)
                .load(new File(filePath))
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .centerCrop()
                .resize(nw,nh)
                .placeholder(R.drawable.no_found)
                .error(R.drawable.error)
                .into(this);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            log.i(TAG,"试图引用　一个　回收的图片 ["+e.getMessage()+"-----"+e.getCause()+"]");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
           // setImageDrawable(null);
        }catch (Exception e){
            log.e(TAG,"onDetachedFromWindow:"+e.getMessage());
        }
    }
}
