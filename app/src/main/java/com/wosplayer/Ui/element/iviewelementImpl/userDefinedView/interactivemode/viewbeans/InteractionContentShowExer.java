package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Looper;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;

import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.ImageAttabuteAnimation;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.app.log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/10/10.
 * 交互展示层
 * 绝对布局
 *      添加左右点击按钮
 *         动态添加返回按钮
 * 宽高大小自适应父布局
 * 内容集合LinkList
 * 定时器,定时任务
 *
 *
 */
public class InteractionContentShowExer extends FrameLayout implements IviewPlayer {

    private static final String TAG = "_InteractionContentShowExer";
    private static final int leftTag = 0;
    private static final int rightTag = 1;



    private Context mContext = null;
    private List<IviewPlayer> contentList = null;
    private int currentIndex = 0;
    private View mFather = null;
    private AbsoluteLayout mBackLayout = null;
    private AbsoluteLayout mFontLayout = null;

    private Button left=null;
    private Button right=null;
    private FrameLayout returnbtn;//返回按钮


    private Timer timer = null;
    private TimerTask timerTask  = null;



    /**
     * 构造
     * @param context 环境
     */
    public InteractionContentShowExer(Context context,List<IviewPlayer> list) {
        super(context);

        this.mContext = context;
        //设置自己的布局
        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT,0,0);
        this.setLayoutParams(lp);
        this.setBackgroundColor(Color.TRANSPARENT);
        setContentList(list);
        log.d(TAG,"创建完成 -"+this.toString());
    }

    @Override
    public void AotuLoadingResource() {
        try {

            genereteLayout();//前后图层


            //首次 显示 第一个控件
            if (contentList==null){
                log.e(TAG,"无内容集合");
                //设置一张默认图片 显示2秒 退出
                removeMeToFather();
                return;
            }
            addButton(mFontLayout);
            playShow();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //播放
    private void playShow() {
        contentList.get(currentIndex).addMeToFather(mBackLayout);
        ImageAttabuteAnimation.SttingAnimation(mContext, (View) contentList.get(currentIndex),null);
        int time = contentList.get(currentIndex).getPlayDration(this);
        startTimer(time);
    }


    private void cancalTimer() {
        log.d(TAG,"停止定时任务");
        if (timerTask != null){
            timerTask.cancel();
            timerTask=null;

        }
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
    }
    //開始
    private void startTimer(int times){
        if (times<=0){
            log.e(TAG,"永久播放");
            return;
        }
        log.d(TAG,"播放时间: "+ times);

        cancalTimer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                log.e(TAG,"Thread name:"+Thread.currentThread().getName());
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        ScollTo(rightTag);
                    }
                });
            }
        };
        timer = new Timer();
        timer.schedule(timerTask,times);
    }

    //tianjia anniu
    private void addButton(ViewGroup layout) {
        addReturnButton(layout);
        addLeftOrRightButton(layout);

    }

    private void removeResoure(){
        try {
            cancalTimer();
            if (contentList!=null || contentList.size()>0){
                contentList.get(currentIndex).removeMeToFather();
            }

            this.removeAllViews();//删除全部的视图
            left = null;
            right = null;
            mBackLayout =null;
            mFontLayout =null;
            contentList=null;
            mContext = null;
  /*
            if (!isRemoveBtn){
                return;
            }
         if (returnbtn!=null){
               // ((ViewGroup)mFather).removeView(returnbtn);
                this.removeView(returnbtn);
                returnbtn=null;
            }
            if (left!=null){
                ((ViewGroup)mFather).removeView(left);
                left=null;
            }
            if (right!=null){
                ((ViewGroup)mFather).removeView(right);
                right=null;
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void addMeToFather(View view) {

        try {
            mFather = view;
//            ((ViewGroup)mFather).removeView(this);
            ((ViewGroup)mFather).addView(this);
            AotuLoadingResource();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addMeToFather(View view, boolean f) {

    }

    @Override
    public void removeMeToFather() {
        try {
            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    log.d(TAG,"准备清理中...");
                    //清空内容
                    removeResoure();
                   log.d(TAG,"清理资源完毕");
                    //移除自己
                    ((ViewGroup)mFather).removeView(InteractionContentShowExer.this);
                    log.d(TAG,"移除自己成功");
                    mFather=null;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void removeMeToFather(boolean f) {

    }

    @Override
    public int getPlayDration(IviewPlayer iviewPlayer) {
        return 0;
    }

    //因为获取视频时长,在加载视频之前无法获取,  所以开一个接口 在视频播放完之后可供调用
    @Override
    public void otherMother(Object object) {

        try {
            Integer var = (Integer) object;
            log.e(TAG," otherMother() call : "+ var);
            startTimer(var);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /*

    我的方法
     */

    private void genereteLayout(){

        if (Thread.currentThread() != Looper.getMainLooper().getThread()){
            log.e(TAG,"当前线程不在主线程 创建图层");
            return;
        }

        try {
            //第一层
            if (mBackLayout==null){
                //全宽全高
                mBackLayout = new AbsoluteLayout(mContext);
                LayoutParams lp = new LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT);
                mBackLayout.setLayoutParams(lp);
                mBackLayout.setBackgroundColor(Color.RED);
                this.addView(mBackLayout);
                log.d(TAG,"背景图层 创建完成 "+mBackLayout);
            }


            //第二层
            if (mFontLayout==null){
                mFontLayout = new AbsoluteLayout(mContext);
                LayoutParams lp = new LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT);
                mFontLayout.setLayoutParams(lp);
                mFontLayout.setBackgroundColor(Color.TRANSPARENT);
                this.addView(mFontLayout);
                log.d(TAG,"前景图层 创建完成 "+ mFontLayout);
            }

        } catch (Exception e) {
            log.e(TAG,"genereteLayout() err:"+e.getMessage());
        }

    }







    //添加显示的视图内容
    private void setContentList(List<IviewPlayer> list){
        try {
            contentList = list;
        } catch (Exception e) {
            log.e(TAG,"setContentList() err:"+e.getMessage());
        }
    }
    //添加返回按钮
    public void setMyReturnBtn(FrameLayout buttonLayout){
        returnbtn = buttonLayout;
    }
    private void addReturnButton(ViewGroup v){

        try {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()){
                log.e(TAG,"当前线程不在主线程 不能添加按钮");
                return;
            }
            if (returnbtn==null){
                log.e(TAG,"返回按钮不存在");
                return;
            }
            v.removeView(returnbtn);
            v.addView(returnbtn);
            log.e(TAG,"已经添加返回按钮");
        } catch (Exception e) {
           log.e(TAG,"addReturnButton() err:"+e.getMessage());
        }

    }

    //添加左右按钮
    private void addLeftOrRightButton(ViewGroup v){

      try{

          if (Thread.currentThread() != Looper.getMainLooper().getThread()){
              log.e(TAG,"当前线程不在主线程 不能添加按钮");
              return;
          }

            if (mFather == null){
                log.e(TAG,"添加左右按钮 失败 ,无父容器");
                return;
            }
        if (left==null) {
            left = new Button(mContext);
            left.setTag(leftTag);
            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.left);
            BitmapDrawable bd = new BitmapDrawable(this.getResources(), bitmap);
            left.setBackgroundDrawable(bd);
            left.setLayoutParams(new AbsoluteLayout.LayoutParams(60, 60, 0, (mFather.getLayoutParams().height / 2) - 60));
            left.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ScollTo((Integer) v.getTag());
                }
            });
        }

          if(right==null) {
              right = new Button(mContext);
              right.setTag(rightTag);
              Bitmap bitmap2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.right);
              BitmapDrawable bd2 = new BitmapDrawable(this.getResources(), bitmap2);
              right.setBackgroundDrawable(bd2);
              right.setLayoutParams(new AbsoluteLayout.LayoutParams(60, 60, mFather.getLayoutParams().width - 60, (mFather.getLayoutParams().height / 2) - 60));
              right.setOnClickListener(new OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      ScollTo((Integer) v.getTag());
                  }
              });
          }


          v.removeView(left);
          v.addView(left);

          v.removeView(right);
          v.addView(right);

      }catch(Exception e){
            log.e(TAG,"addLeftOrRightButton() err:"+e.getMessage());
      }
    }


    //按钮滑动事件
    private void ScollTo(int tag) {
        if (contentList==null){
            log.e(TAG,"   contentList is null - ");
            return;
        }
        switch (tag){
            case leftTag:
            log.d("左边 \n <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            contentList.get(currentIndex).removeMeToFather();
                currentIndex--;
                if (currentIndex<0){
                    currentIndex=contentList.size()-1;
                }
                playShow();


            break;
            case rightTag:
            log.d("右边\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>");

                contentList.get(currentIndex).removeMeToFather();
                currentIndex++;
                if (currentIndex>=contentList.size()){
                    currentIndex=0;
                }

                playShow();

            break;
        }
    }


}
