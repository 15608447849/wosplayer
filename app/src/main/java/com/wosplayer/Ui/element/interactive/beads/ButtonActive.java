package com.wosplayer.Ui.element.interactive.beads;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wosplayer.Ui.element.uiViewimp.IinteractionPlayer;
import com.wosplayer.Ui.element.uitools.ImageStore;
import com.wosplayer.Ui.element.uitools.ImageViewPicassocLoader;
import com.wosplayer.Ui.element.interfaces.IviewPlayer;
import com.wosplayer.Ui.element.interactive.iCache.InteractionCache;
import com.wosplayer.Ui.element.interactive.viewbeans.InteractionContentShowExer;
import com.wosplayer.Ui.element.interactive.xml.XmlParse;
import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.app.Logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.trinea.android.common.util.FileUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import com.wosTools.ToolsUtils;
import com.wosplayer.app.SystemConfig;

/**
 * Created by Administrator on 2016/6/28.
 * button
 */

public class ButtonActive extends ImageButton implements View.OnClickListener, IviewPlayer {

    private static final java.lang.String TAG = "_ActionButotn";//ButtonActive.class.getName();
    public int x, y, w, h; //大小坐标
    public int bindtype; //绑定类型
    public String bindid; //绑定id
    public String layoutUri;
    public String folderurl;
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
        this.setOnClickListener(this);
        this.setEnabled(false);//不可点击
    }

    private IinteractionPlayer CanvasView; //按钮绑定的视图 显示的 容器
    private List<IviewPlayer> myBindFileViews;//绑定的文件视图
    private LayoutActive myBindLayoutView;
    //互动显示层
    private InteractionContentShowExer mvp = null;
    private ImageButton retenbtn;//返回按钮
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
        if (UiExcuter.isStoping) {
            Logs.e("清理布局中.....");
            return;
        }
        Logs.i(TAG, "按钮  ,绑定id:" + bindid);//click:[" + this.toString() +"]
        if (CanvasView == null) {
            Logs.e(TAG, " 互动模块容器 不存在 ");
            return;
        }
        if(fLayout==null){
            fLayout = new FrameLayout(mcontext);
            fLayout.setLayoutParams(new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                    AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                    CanvasView.getLayoutParams().width - 175,
                    CanvasView.getLayoutParams().height - 175));
        }
        if (retenbtn == null) {
            //添加一个返回按钮　
            retenbtn = ToolsUtils.mImageButton(mcontext);
            retenbtn.setScaleType(ImageView.ScaleType.FIT_XY);
            retenbtn.setImageBitmap(ImageStore.getInstants().getButton_back(mcontext));
            retenbtn.setLayoutParams(new AbsoluteLayout.LayoutParams(60, 60, 0, 0));
            retenbtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Logs.e(" ---------------------- 点击了 返回按钮 -------------------------");
                        //删除自己的存在
                        CanvasView.removeView(fLayout);
                        //返回上一个视图
                        CanvasView.returnPrevionsView();
                        releasedSubViewResource(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            fLayout.addView(retenbtn);
        }
        Logs.i(TAG, "按钮  ,绑定id:" + bindid +" - 返回按钮 ok - bindtype -" +bindtype);
        // 如果有人点击我了. 1.移除 画布当前视图(不删除视图栈), 我就把我绑定的的视图放上去,
        switch (bindtype) {
            case 0: //排版
            case 2:
                if (myBindLayoutView == null) {
                    Logs.e(TAG, "当前按钮 所 绑定的 下一级 布局层 不存在");
                    return;
                }
                myBindLayoutView.addReturnButton(fLayout);
                myBindLayoutView.addMeToFather(CanvasView);
                CanvasView.setmCurrentView((View) myBindLayoutView);

                break;
            case 3://网页
            case 1://文件 (视频, 图片,网站)
                if (myBindFileViews == null || myBindFileViews.size() < 1) {
                    return;
                }
                if (mvp == null) {
                    mvp = new InteractionContentShowExer(mcontext, myBindFileViews);
                }
                mvp.addMeToFather(CanvasView, fLayout); //互动滑动控件　加到画布上
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
        Logs.i(TAG, " 互动执行者 绑定的视图>子项-按钮>加载自己的xml文件");
        //根据类型拼接 URI 0 2排版 1 文件夹 3web
        String srcUri = bindtype == 0 || bindtype == 2 ? layoutUri + bindid : folderurl + bindid;
//        log.i(TAG, "----bindtype--- " + bindtype + "\n----bindid--" + bindid + "\n---->" + srcUri);
        getXmlData(srcUri);
    }

    /**
     * 移除资源
     * 请放入 新线程中
     */
    private void removeSrc() {
        releasedSubViewResource(false);
    }

    /**
     * 释放子项所占资源
     */
    private void releasedSubViewResource(boolean isLoacl) {

        //如果滑动控件存在 移除
        if (mvp != null) {
            mvp.removeMeToFather();
            mvp = null;
        }

        //如果返回按钮存在. 移除
        if (retenbtn != null) {
            if (fLayout != null) {
                fLayout.removeView(retenbtn);
                fLayout = null;
            }
            retenbtn = null;
        }

        if (isLoacl){
            return;
        }
        //如果视图 文件子视图列表 有视图的话 -> 移除
        if (myBindFileViews != null && myBindFileViews.size() > 0) {
            for (IviewPlayer mv : myBindFileViews) {
                mv.removeMeToFather();
            }
            myBindFileViews.clear();
        }
        //如果 排版布局 视图有视图的话 清理
        if (myBindLayoutView != null) {
            myBindLayoutView.removeMeToFather();
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
     * 把自己加载到我的父控件上面
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
                    mFather.removeView(ButtonActive.this);
                    mFather.addView(ButtonActive.this);
                    Logs.i(TAG, " 互动执行者 绑定的视图>>子项button>>按钮>>加载自己到布局层");
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


    private int nw, nh, nx, ny;
    /**
     * 设置布局属性
     *
     * @param wScale
     * @param hScale
     */
    public void setMyLayoutparam(float wScale, float hScale) {

        nw = (int) ((float) this.w * wScale);
        nh = (int) ((float) this.h * hScale);
        nx = (int) ((float) this.x * wScale);
        ny = (int) ((float) this.y * hScale);
        this.setLayoutParams(
                new AbsoluteLayout.LayoutParams(
                        nw, nh, nx, ny
                )
        );

//        log.i(TAG, wScale + "," + hScale + "按钮" + this.bindid + "---" + this.toString() + "设置layout属性,原大小[" + this.w + "," + this.h + "," + this.x + "," + this.y + "]现在大小:[" + nw + "," + nh + "," + nx + "," + ny + "]");
    }

    /**
     * 把自己从父控件上面 移除
     */
    @Override
    public void removeMeToFather() {
            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    if (mFather != null) {
                    mFather.removeView(ButtonActive.this);
                    mFather = null;
                    removeSrc();
                    }
                }
            });

    }


    @Override
    public int getPlayDration(IviewPlayer iviewPlayer) {
        return 0;
    }

    @Override
    public void otherMother(Object object) {

    }

    /**
     * 获取xml资源文件
     *
     * @param uri
     */
    private void getXmlData(String uri) {
        //看缓存
        final String result = InteractionCache.pull(uri);
        if (result != null) {
            IinteractionPlayer.worker.schedule(new Action0() {
                @Override
                public void call() {
                    //进行XML解析
                    parseTanslation(result);
                }
            });
            return;
        }

//        log.e(TAG, "xml 返回结果 null");
        final String key = uri;
        http.send(
                HttpRequest.HttpMethod.GET,
                uri,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String xml = responseInfo.result;
                        InteractionCache.push(key, xml);//存
                        parseTanslation(xml);
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Logs.e(TAG, s);
                    }
                }
        );
    }

    private void parseTanslation(String result) {
        loadMeBindView(result); //得到 绑定的子视图->加载 自己 绑定 的视图
        ParseResultXmlGetBgUri(result); //得到 背景图片->进行XML解析
    }

    /**
     * 解析
     *
     * @param xml
     * @return
     */
    private void ParseResultXmlGetBgUri(final String xml) {
//        log.i(TAG, " 互动执行者 绑定的视图>子项>按钮>>加载自己的背景资源 \n " + xml);
        try {
            switch (bindtype) {
                case 0: //排版
                case 2:
                    myBgUri = XmlParse.interactionParse_ButtonSrcUri_LayoutType(xml);
                    Logs.i(TAG, bindid + "是布局类型,  : \n" + myBgUri);
                    break;
                case 1://文件夹
                case 3://网页
                    myBgUri = XmlParse.interactionParse_ButtonSrcUri_FildType(xml);
                    Logs.i(TAG, bindid + "是文件夹网页类型  : \n" + myBgUri);
                    break;
            }
        } catch (Exception e) {
            Logs.e(TAG, "" + e.getMessage());
        }
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                loadMeBackImage(); //自己的背景图
            }
        });

    }

    /**
     * 加载自己的背景图片
     */
    private void loadMeBackImage() {

//        Logs.i(TAG, "互动 - 按钮 -背景图片 bg image uri " + myBgUri);
        if (myBgUri != null) {
            String filename = myBgUri.substring(myBgUri.lastIndexOf("/") + 1);
            filename = UiExcuter.getInstancs().basepath + filename;
            Logs.i(TAG, "bg image local path - " + filename);
            if (FileUtils.isFileExist(filename)) {
                //存在直接设置
                picassoLoaderImager(filename);
            } else {
                Logs.e(TAG, "按钮 找不到背景图片 url - " + myBgUri + "本地 路径: " + filename);
            }

        }
    }

    /**
     * 加载自己的绑定的视图 或者 文件夹
     * 请放入其他线程
     */
    private void loadMeBindView(final String xml) {
        Logs.i(TAG, " 互动执行者 绑定的视图...的子项...按钮..加载自己的子项");
        //解析 得到 子视图信息
        try {
            switch (bindtype) {
                case 0: //排版 传进来的 排版的xml数据
                case 2:
                    AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                        @Override
                        public void call() {
                            try {
                                myBindLayoutView = XmlParse.interactionParse_one(mcontext,xml); //1 先解析
                                //在 需要显示的时候 去加载他的 子资源
                                ButtonActive.this.setEnabled(true);
                                Logs.i(TAG, bindid + "可以点击了");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
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


                            IviewPlayer view = null;
                            for (int i = 0; i < arr.size(); i++) {
                                view = arr.get(i).TanslateInfoToView(mcontext,UiExcuter.getInstancs().basepath);//转化视图 (创建...)
                                if (view != null) {
                                    myBindFileViews.add(view);//把视图 保存
                                }
                            }
                            ButtonActive.this.setEnabled(true);
                            Logs.i(TAG, bindid + "可以点击了");
                        }
                    });
                    break;
            } //switch 结束
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void picassoLoaderImager(String filePath) {
//        log.i(TAG, "button _width:" + nw);
//        log.i(TAG, "button _height:" + nh);
        ImageViewPicassocLoader.getBitmap(mcontext,filePath,this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            Logs.e(TAG, "试图引用　一个　回收的图片 [" + e.getMessage() + "-----" + e.getCause() + "]");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
            // setImageDrawable(null);
        } catch (Exception e) {
            Logs.e(TAG, "onDetachedFromWindow:" + e.getMessage());
        }
    }
}
