package com.wosplayer.Ui.element.iviewelementImpl.actioner.ContainerItem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.actioner.Container;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.otherBlock.fileUtils;

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

   private Context context;
   private ViewGroup vp;

    private int bgWidth ;
    private int bgHeight ;

   public LayoutContainer(Context context, DataList ls){
       this.context = context;
       view = new AbsoluteLayout(context);//创建绝对布局
       bgmode = ls.GetIntDefualt("bgmodel",1);// 1 #ffffcc   , 2 imageName
       bgName = ls.GetStringDefualt("bg","");
       width = ls.GetIntDefualt("w",-1);
       height = ls.GetIntDefualt("h",-1);
   }

    //设置 w h x y 大小 比例
    @Override
    public float[] onSettingScale(int fwidth,int fheight){
      float wScale = (float)fwidth/(float)width ;
      float hScale =(float)fheight/ (float)height ;
      log.i(TAG,"宽度比例-"+wScale+",高度比例-"+ hScale);
        if (wScale == 0.0 || hScale== 0.0){
          log.e(TAG,"scale is err");
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
    protected void onSettingScale(float widthScale, float heightScale) {
        //
    }

    @Override
    protected void onBg(ViewGroup vp) {
        try{
            if (bgmode==1){ //color

                if (bgName.equals("")){
                    view.setBackgroundColor(Color.WHITE);
                }else{
                    view.setBackgroundColor(Color.parseColor(bgName));
                }

            }else if (bgmode==2){ //image
                if (fileUtils.checkFileExists(bgName)) { //资源是否存在
                   loadBg();
                } else {
                    log.e(TAG," 互动背景 资源不存在 ");
                }
            }else{
                log.e(TAG,"bg model is err : bgmodel=" + bgmode);
            }

        }catch (Exception e){
                log.e(TAG,"布局容器:"+e.getMessage());
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
                    log.e(TAG," call(): "+e.getMessage());
                    bitmap = BitmapFactory.decodeResource(view.getResources(), R.drawable.no_found);
                }

                final Drawable dw = new BitmapDrawable(bitmap);
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        log.i(TAG, " 互动布局设置背景图片");
                        view.setBackgroundDrawable(dw);
                    }
                });
            }
        });
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
            lp.x = 0;
            lp.y = 0;
            lp.width = width;
            lp.height = height;
            view.setLayoutParams(lp);
        }catch (Exception e){
            log.e(TAG,"onLayout() err:" + e.getMessage());
        }
    }

    @Override
    protected void onUnlayout() {
        try{
            if (isLayout){
                vp.removeView(view);
                isLayout = false;
            }
        }catch (Exception e){
            log.e(TAG,"onUnlayout() err:" + e.getMessage());
        }
    }

    //布局绑定时
    @Override
    protected void onBind(ViewGroup vp) { // 容器

        try {
            // 1. 设置 view 宽高属性 添加到外层容器上
            onLayout(vp);
            //加载资源
            onBg(null);
            // 2. 设置 子项 ，调用子项 bind 方法
            if (childs!=null && childs.size()>0){
                log.i(TAG," 布局子类:"+ childs.size());
                for (Container button:childs){
                    if (button instanceof ButtonContainer){
                        ((ButtonContainer)button).onBind((ViewGroup) view);
                    }
                }
            }
        }catch (Exception e){
            log.e(TAG,"onBind() err:" + e.getMessage());
        }
    }

    @Override
    protected void onUnbind() {
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
            log.e(TAG,"onBind() err:" + e.getMessage());
        }

    }

    @Override
    protected void onClick(View v) {

    }

    @Override
    protected void onBack(View v) {

    }

    @Override
    protected void addChilds(Container child) {
        if (super.childs == null){
            childs = new ArrayList<Container>();
        }
        if (childs.contains(child)){
            log.e(TAG,"布局容器 添加 子视图 失败， 已存在");
        }else{
            childs.add(child);
            log.i(TAG,"布局容器 添加 子视图 success");
        }
    }


}
