package com.wosplayer.Ui.element.iviewelementImpl.actioner;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by user on 2016/8/2.
 * “容器”的作用是帮助我们展示一项内容并处理后退操作
 */
public abstract  class Container {
    public static final String TAG = "_actionContainer";
    protected View view;
    protected List<Container> childs ;//要显示在上面的子view
    protected Container previous;//上一个视图
    protected Container next;//下一个视图
    protected boolean isBind = false;
    protected boolean isLayout =false;


   protected abstract void  onSettingScale (long fwidth,long fheight);
   protected abstract void  onBg (ViewGroup vp);
   protected abstract void  onUnbg (ViewGroup vp);

   protected abstract void  onLayout (ViewGroup vp);
   protected abstract void  onUnlayout ();
   protected abstract void  onBind(ViewGroup vp);
   protected abstract void  onUnbind();
   protected abstract void onClick(View v);
   protected abstract void onBack(View v);
   protected abstract void addChilds(Container child);

}
