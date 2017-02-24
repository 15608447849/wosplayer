package com.wosplayer.Ui.element.iviewelementImpl.actioner.ContainerItem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.actioner.Container;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.download.util.DownloadFileUtil;

import java.util.ArrayList;

import it.sephiroth.android.library.picasso.MemoryPolicy;
import it.sephiroth.android.library.picasso.Picasso;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/8/8.
 *
 *
 */

public class LayoutContainer extends Container{


   private int bgmode = -1;
   private String bgName = null;

   private int width ;
   private int height;



    private int bgWidth ;
    private int bgHeight ;

    private AbsoluteLayout absulute ;

    public LayoutContainer(Context context, DataList ls){
       this.context = context;
        view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.action_layout_layout,null);

       absulute = (AbsoluteLayout) view.findViewById(R.id.layot_absulute);

       bgmode = ls.GetIntDefualt("bgmodel",1);// 1 #ffffcc   , 2 imageName
       bgName = ls.GetStringDefualt("bg","");
       width = ls.GetIntDefualt("w",-1);
       height = ls.GetIntDefualt("h",-1);
   }



    public void addReturnButton(boolean isAddReturnButton){

        if (isAddReturnButton){
            Logs.e(TAG,"-----");
           FrameLayout f = (FrameLayout) view.findViewById(R.id.layot_frame);
           f.setVisibility(View.VISIBLE);
           ImageButton back = (ImageButton) view.findViewById(R.id.layout_back_back);
           onBack(back);
           Logs.e(TAG,"layout add back is success");
        }

    }


    //设置 w h x y 大小 比例
    @Override
    public float[] onSettingScale(int fwidth,int fheight){
      float wScale = (float)fwidth/(float)width ;
      float hScale =(float)fheight/ (float)height ;
      Logs.i(TAG,"宽度比例-"+wScale+",高度比例-"+ hScale);
        if (wScale == 0.0 || hScale== 0.0){
          Logs.e(TAG,"scale is err");
            return new float[]{1.0f,1.0f};
        }

        bgWidth = (int) ((float)this.width * wScale);
        bgHeight =(int) ((float)this.height * hScale);
        //布局容器 匹配父容器就好了
        width = AbsoluteLayout.LayoutParams.MATCH_PARENT;
        height = AbsoluteLayout.LayoutParams.MATCH_PARENT;
        return new float[]{wScale,hScale};
    }

    @Override
    public void onSettingScale(float widthScale, float heightScale) {
        bgWidth = (int) ((float)this.width * widthScale);
        bgHeight =(int) ((float)this.height * heightScale);
    }

    @Override
    protected void onBg(ViewGroup vp) {
        try{
            if (bgmode==1){ //color

                if (bgName.equals("")){
                    absulute.setBackgroundColor(Color.WHITE);
                }else{
                    absulute.setBackgroundColor(Color.parseColor(bgName));
                }

            }else if (bgmode==2){ //image
                if (DownloadFileUtil.checkFileExists(bgName)) { //资源是否存在
                   loadBg();
                } else {
                    Logs.e(TAG," 互动背景 资源不存在 ");
                }
            }else{
                Logs.e(TAG,"bg model is err : bgmodel=" + bgmode);
            }

        }catch (Exception e){
                Logs.e(TAG,"布局容器:"+e.getMessage());
        }
    }

    private void loadBg() {
        Container.threadHeaper.schedule(new Action0() {
            @Override
            public void call() {
                Bitmap bitmap = null;
                try {
                    bitmap = Picasso.with(context)
                            .load(bgName)
                            .resize(bgWidth,bgHeight) //w h
                            .centerCrop()
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .config(Bitmap.Config.RGB_565)
                            .get();
                } catch (Exception e) {
                    Logs.e(TAG," call(): "+e.getMessage());
                    bitmap = BitmapFactory.decodeResource(view.getResources(), R.drawable.no_found);
                }

                final Drawable dw = new BitmapDrawable(bitmap);
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        Logs.i(TAG, " 互动布局设置背景图片");
                        absulute.setBackgroundDrawable(dw);
                    }
                });
            }
        });
    }

    @Override
    protected void onUnbg(ViewGroup vp) {
        releasSourceBg(absulute);
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
            lp.x = 0;
            lp.y = 0;
            lp.width = width;
            lp.height = height;
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

    //布局绑定时
    @Override
    public void onBind(ViewGroup vp) { // 容器

        try {
            // 1. 设置 view 宽高属性 添加到外层容器上
            onLayout(vp);
            //加载资源
            onBg(null);
            // 2. 设置 子项 ，调用子项 bind 方法
            if (childs!=null && childs.size()>0){
                Logs.i(TAG," 布局子类:"+ childs.size());
                for (Container button:childs){
                    if (button instanceof ButtonContainer){
                        ((ButtonContainer)button).onBind((ViewGroup) absulute);
                    }
                }
            }
        }catch (Exception e){
            Logs.e(TAG,"onBind() err:" + e.getMessage());
        }
    }

    @Override
    public void onUnbind() {
        try {
            // 1. 删除视图
            onUnlayout();
            //解除资源
            onUnbg(null);
            // 2. 设置 子项 ，调用子项 unbind 方法
            if (childs!=null && childs.size()>0){
                for (Container button:childs){
                    if (button instanceof ButtonContainer){
                        ((ButtonContainer)button).onUnbind();
                    }
                }
            }
        }catch (Exception e){
            Logs.e(TAG,"onUnbind() err:" + e.getMessage());
        }

    }

    @Override
    protected void onClick(View v) {

    }

    @Override
    protected void onBack(View v) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logs.e(TAG,"- 布局 返回 按钮 - ");
                    if (previous!=null && previous instanceof LayoutContainer){
                        //调用上一个视图的 bind
                        ((LayoutContainer)previous).onBind(vp);
                        //调用自己的 onbind
                        onUnbind();
                    }
                }
            });
    }

    @Override
    protected void addChilds(Container child) {
        if (super.childs == null){
            childs = new ArrayList<Container>();
        }
        if (childs.contains(child)){
            Logs.e(TAG,"布局容器 添加 子视图 失败， 已存在");
        }else{
            childs.add(child);
            Logs.i(TAG,"布局容器 添加 子视图 success");
        }
    }


}
