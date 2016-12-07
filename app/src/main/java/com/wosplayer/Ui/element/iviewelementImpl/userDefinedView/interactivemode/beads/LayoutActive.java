package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.beads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.wosplayer.Ui.element.iviewelementImpl.IinteractionPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.uitools.ImageStore;
import com.wosplayer.Ui.element.iviewelementImpl.uitools.ImageViewPicassocLoader;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.app.WosApplication;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by Administrator on 2016/6/28.
 * 互动模块 层布局
 */

public class LayoutActive extends AbsoluteLayout implements IviewPlayer {

    private static final java.lang.String TAG = "_LayoutActive";//LayoutActive.class.getName();
    public String bgImagelurl;
    public String id;
    public int w, h;
    private float wScale, hScale;
    private int scale_w, scale_h;
    public int bgType;
    public String bgImagename;
    public List<ButtonActive> myItems;
    private Context mcontext;

    private boolean isLayout = false;

    /**
     * @param context
     * @param bgImagelurl 　背景图片　ｕｒｉ
     * @param id
     * @param w
     * @param h
     * @param bgType      　背景类型
     * @param bgImagename
     * @param myItems     　我的子控件
     */
    public LayoutActive(Context context, String bgImagelurl, String id, int w, int h, int bgType, String bgImagename, List<ButtonActive> myItems) {
        super(context);
        this.mcontext = context;
        this.bgImagelurl = bgImagelurl;
        this.id = id;
        this.w = w;
        this.h = h;
        this.bgType = bgType;
        this.bgImagename = bgImagename;
        this.myItems = myItems;
    }


    private IinteractionPlayer mFather;//
    public FrameLayout returnBtn;

    /**
     * 设置我的宽高属性
     */
    private void setMylayoutParma() {
        log.i(TAG, " 互动执行者-> 绑定的视图->设置自己的布局参数");
        //获取 与父容器的 宽高比值
        log.i(TAG, "1f宽度" + mFather.getWidth() + "--f高度" + mFather.getHeight());
        log.i(TAG, "2f宽度" + mFather.getMeasuredWidth() + "--f高度" + mFather.getMeasuredHeight());
        log.i(TAG, "3f宽度" + mFather.getLayoutParams().width + "--f高度" + mFather.getLayoutParams().height);
        wScale = (float) mFather.getLayoutParams().width / (float) this.w;
        hScale = (float) mFather.getLayoutParams().height / (float) this.h;
        log.i(TAG, "宽度比例" + wScale + "--高度比例" + hScale);
        if (wScale == 0.0 || hScale == 0.0) {
            log.e(TAG, " 比例异常 ");
            return;
        }
        scale_w = (int) ((float) this.w * wScale);
        scale_h = (int) ((float) this.h * hScale);
        //设置宽高
        this.setLayoutParams(new LayoutParams(
                scale_w,
                scale_h,
                0,
                0));
    }

    //添加返回按钮
    public void addReturnButton(FrameLayout returnbtn) {
        this.returnBtn = returnbtn;
    }

    /**
     * 把自己添加到父控件
     */
    @Override
    public void addMeToFather(View Father) {
        if (Father != null) {
            this.mFather = (IinteractionPlayer) Father;
        }
        if (mFather != null) {
            if (mFather instanceof IinteractionPlayer) {
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        log.i(TAG, " 互动执行者 ->绑定的视图->添加自己到互动执行者");
                        setMylayoutParma();
                        //容器是个绝对布局的话
                        ((AbsoluteLayout) mFather).removeView(LayoutActive.this);
                        ((AbsoluteLayout) mFather).addView(LayoutActive.this);
                        if (returnBtn != null) {
                            ((AbsoluteLayout) mFather).removeView(returnBtn);
                            ((AbsoluteLayout) mFather).addView(returnBtn);
                        }
                        isLayout = true;
                        AotuLoadingResource();//自动加载资源
                    }
                });

            }

        }
    }


    /**
     * 加载资源
     */
    @Override
    public void AotuLoadingResource() {
        log.i(TAG, " 互动执行者-> 绑定的视图 ->加载自己的资源");
        //加载视图
        LoaderSource();
        addMeSubView(); //加载我的子控件
    }

    //释放资源
    private void releasedResource() {
        removeMeSubView();//先移除我的子控件
    }

    /**
     * 把自己 从父控件移除
     */
    public void removeMeToFather() {
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                if (mFather != null) {
                    if (returnBtn != null) {
                        returnBtn = null;
                    }
                    //容器是个绝对布局的话
                    mFather.removeView(LayoutActive.this);
                    mFather = null;
                    releasedResource();
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
     * 转换背景颜色代码
     *
     * @param colorValue
     * @return
     */
    public static String TanslateColor(String colorValue) {
        String color = null;
        try {
            String tem = Integer.toHexString(Integer.parseInt(colorValue));
            if (tem.length() == 6) {
                color = tem;
            } else {
                StringBuffer addZeor = new StringBuffer();
                for (int i = 0; i < 6 - tem.length(); i++) {
                    addZeor.append("0");
                }
                color = addZeor + tem;
            }
            return "#" + color;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 加载背景图片资源资源
     * 在添加到父组件中被调用
     */
    public void LoaderSource() {
        if (bgType == 1) { //纯色
            final String colorValue = TanslateColor(bgImagename);
            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    if (colorValue != null) {
                        LayoutActive.this.setBackgroundColor(Color.parseColor(colorValue));
                    } else {
                        LayoutActive.this.setBackgroundColor(Color.WHITE);
                    }
                }
            });

        } else if (bgType == 2) {
            String uriLoad = bgImagelurl + bgImagename; //下载地址
            String localpath = WosApplication.config.GetStringDefualt("basepath", "") + bgImagename; //本地路径

            if (fileUtils.checkFileExists(localpath)) { //资源是否存在
                setBgImagers(localpath);
            } else {
                log.e(TAG, "互动布局 图片资源 不存在 - " + localpath + "\n uri:" + uriLoad);
            }
        }

    }




    public void setBgImagers(String filePath) {
        Bitmap bitmap = null;
        try {
//            log.d(TAG, "互动 layout . scale after w,h = " + scale_w + "," + scale_h);
            bitmap = ImageViewPicassocLoader.getBitmap(filePath, null);
            if (bitmap == null) {
                throw new NullPointerException("filepath err :" + filePath);
            }
        } catch (Exception e) {
            log.e(TAG, "layout call() err : " + e.getMessage());
            bitmap = ImageStore.getInstants().getButton_err(mcontext);
        }
        log.i(TAG, "互动布局设置背景图片");
        LayoutActive.this.setBackgroundDrawable(new BitmapDrawable(bitmap));
    }

    /**
     * 添加我的子控件　对象
     */
    private void addMeSubView() {
        try {
            for (ButtonActive button : myItems) {
                button.setMyLayoutparam(wScale, hScale);//设置按钮大小位置
                button.addMeToFather(LayoutActive.this); //把自己添加到父控件
                //设置它的画布　　
                button.setMeCanvasView(LayoutActive.this.mFather);
            }
            log.i(TAG, " 互动执行者 -> 绑定的视图->加载自己的子项 success");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 移除我的子控件对象  在我被父控件移除时候调用
     */
    private void removeMeSubView() {
        try {
            for (ButtonActive button : myItems) {
                button.removeMeCanvasView();//移除 自己的画板 视图
                button.removeMeToFather(); // 从 父控件中 移除 自己
            }
        } catch (Exception e) {
            log.e(TAG, "捕获一个 Exception...  " + e.getMessage());
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            log.e(TAG, "试图引用　一个　回收的图片 [" + e.getMessage() + "-----" + e.getCause() + "]");
        }
    }
}
