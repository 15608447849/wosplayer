package com.wosplayer.Ui.element.iviewelementImpl.actioner;

import android.view.View;

import java.util.List;

/**
 * Created by user on 2016/8/2.
 * “容器”的作用是帮助我们展示一项内容并处理后退操作
 */
public class Container implements View.OnClickListener{

   private String leven;//布局 按钮
   private List<Container> childs ;//要显示在上面的
   private Container previous;//上一个视图
   private Container next;//下一个视图

   private View view;

   @Override
   public void onClick(View v) {

   }
}
