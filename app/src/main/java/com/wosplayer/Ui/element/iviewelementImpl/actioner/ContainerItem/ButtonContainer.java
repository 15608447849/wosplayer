package com.wosplayer.Ui.element.iviewelementImpl.actioner.ContainerItem;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;

import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.actioner.Container;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;

import java.io.File;

import it.sephiroth.android.library.picasso.MemoryPolicy;
import it.sephiroth.android.library.picasso.Picasso;

/**
 * Created by user on 2016/8/9.
 */
public class ButtonContainer extends Container{


    private int x;
    private int y;
    private int w;
    private int h;
    private String bgimage;
    public ButtonContainer(Context context, DataList ls){
        this.context = context;
        view = new ImageButton(context){
            @Override
            protected void onDraw(Canvas canvas) {
                try {
                    super.onDraw(canvas);
                } catch (Exception e) {
                    Logs.i(TAG,"布局子按钮,试图引用一个　回收的图片 ["+e.getMessage()+"-----"+e.getCause()+"]");
                }
            }
            @Override
            protected void onDetachedFromWindow() {
                try {
                    super.onDetachedFromWindow();
                    // setImageDrawable(null);
                }catch (Exception e){
                    Logs.e(TAG,"onDetachedFromWindow:"+e.getMessage());
                }
            }
        };
        x = ls.GetIntDefualt("x",0);
        y = ls.GetIntDefualt("y",0);
        h = ls.GetIntDefualt("h",0);
        w = ls.GetIntDefualt("w",0);
        bgimage = ls.GetStringDefualt("bgimage","");
    }

    @Override
    public void onSettingScale(float widthScale, float heightScale) {
        Logs.i(TAG,"button: 宽度比例-"+widthScale+", 高度比例-"+ heightScale);

        if (widthScale == 0.0 || heightScale== 0.0){
            Logs.e(TAG,"scale is err");
            return;
        }
        w = (int) ((float)this.w *widthScale);
        h =(int) ((float)this.h *heightScale);
        x = (int)((float)this.x*widthScale );
        y = (int)((float)this.y*heightScale );
    }



    @Override
    protected float[] onSettingScale(int fwidth, int fheight) {
        return null;
    }



    @Override
    protected void onBg(ViewGroup vp) {
        try{
            loadBg(bgimage);
        }catch (Exception e){
            Logs.e(TAG,"按钮容器:"+e.getMessage());
        }
    }

    private void loadBg(String filePath) {
        Logs.i(TAG,"按钮背景路径:"+filePath);
        //纯用picasso 加载本地图片
        Picasso.with(context)
                .load(new File(filePath))
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .centerCrop()
                .resize(w,h)
                .placeholder(R.drawable.loadding)
                .error(R.drawable.error)
                .into((ImageButton)view);
        Logs.i(TAG,"按钮背景加载完成");
    }

    @Override
    protected void onUnbg(ViewGroup vp) {
            releasSourceBg(view);
    }

    @Override
    protected void onLayout(ViewGroup vp) {
        this.vp = vp ;
        try {
            if (!isLayout){
                vp.addView(view);
                isLayout = true;
            }
            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) view
                    .getLayoutParams();
            lp.x = x;
            lp.y = y;
            lp.width = w;
            lp.height = h;
            view.setLayoutParams(lp);
        }catch (Exception e){
            Logs.e(TAG,"onLayout() err:" + e.getMessage());
        }
    }

    @Override
    protected void onUnlayout() {
        try{
            if (isLayout){
                vp.removeView(view);
                isLayout = false;
                this.vp = null;
            }
        }catch (Exception e){
            Logs.e(TAG,"onUnlayout() err:" + e.getMessage());
        }
    }

    @Override
    public void onBind(ViewGroup vp) {//附着在上面 layoutContainer
        try {
            // 1. 设置 view 宽高属性 添加到外层容器上
            onLayout(vp);
            //加载资源
            onBg(null);
        }catch (Exception e){
            Logs.e(TAG,"onBind() err:" + e.getMessage());
        }
    }

    @Override
    public void onUnbind() {
        try {
            onUnlayout();
            //解除资源
            onUnbg(null);
        }catch (Exception e){
            Logs.e(TAG,"onUnbind() err:" + e.getMessage());
        }
    }

    @Override
    protected void onClick(View v) {
        if (view!=null){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logs.i(TAG," tm 老子是 一个 按钮 !");
                    if (next!=null && previous!=null){
                        Logs.e(TAG,"进去下一个布局- ->"+next.toString());
//                                = previous
//                                next.onBind();
//                                onUnbind();
//                                previous.onUnbind();

                        if (previous instanceof LayoutContainer){
                            ViewGroup vp = ((LayoutContainer)previous).getVp();
                            if (vp!=null){
                                next.onBind(vp);
                                previous.onUnbind();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onBack(View v) {

    }

    @Override
    protected void addChilds(Container child) {
            next = child;
    }


}
