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
import com.wosplayer.Ui.element.iviewelementImpl.uitools.ImageAttabuteAnimation;
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
    private static final int leftTag = 0;//向左
    private static final int rightTag = 1; //向右
    private Context mContext = null;
    private List<IviewPlayer> contentList = null;
    private int currentIndex = -100;
    private IviewPlayer currentIview = null;
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
            log.i(TAG," --- 初始化 --- ");
            currentIndex = 0;
            //首次 显示 第一个控件
            if (contentList==null){
                log.e(TAG,"互动无内容列表 - contentList - "+contentList);
                //设置一张默认图片 显示2秒 退出 (没有做)
                removeMeToFather();
            }else{
                genereteLayout();//前后图层
                addButton(mFontLayout);//添加按钮
                playShow();//开始显示//第一次开始显示
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //播放
    private void playShow() {
        if (mBackLayout==null){
            log.e(TAG,"背景图层不存在 "+mBackLayout);
            return;
        }

        if (currentIview!=null){
            log.i(TAG,"清理子视图 - "+currentIview);
            currentIview.removeMeToFather();
            currentIview=null;
        }
        currentIview = contentList.get(currentIndex);
        log.i(TAG,"playShow() 获取一个显示 视图 - currentIview"+currentIview);
        if (currentIview==null){
            return;
        }
        currentIview.addMeToFather(mBackLayout);//添加到 后景
        ImageAttabuteAnimation.SttingAnimation(mContext, (View) currentIview,null);//设置入场动画
        int time = currentIview.getPlayDration(this);//获取播放时长
        log.i(TAG,"playShow 获取播放时长 : "+time);
        startTimer(time);
    }
    //取消定时器
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
    //開始定时器
    private void startTimer(int times){
        log.i(TAG,"startTimer() 播放时间: "+ times);
        if (times<=0){
            log.e(TAG,"永久播放");
            return;
        }
        cancalTimer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        ScollTo(rightTag);  //在指定时长之后 会滑动到下一个
                    }
                });
            }
        };
        timer = new Timer();
        timer.schedule(timerTask,times);
    }
    //添加 按钮
    private void addButton(ViewGroup layout) {
        addReturnButton(layout);//添加返回按钮
        addLeftOrRightButton(layout); //添加左 右 键.
    }

    //删除资源
    private void removeResoure(){
        try {
            cancalTimer();//停止计时器
            if (contentList!=null && contentList.size()>currentIndex){ //如果内容列表大小 > 当前下标
                contentList.get(currentIndex).removeMeToFather();
            }
            unGenereteLayout();
            left = null;
            right = null;
            mBackLayout =null;
            mFontLayout =null;
            contentList=null;
            mContext = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //入口
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
                    log.i(TAG,"清理中......");
                    //清空内容
                    removeResoure();
                    //移除自己
                    ((ViewGroup)mFather).removeView(InteractionContentShowExer.this);
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
            log.i(TAG," otherMother() call  视频时长 -: "+ var);
            startTimer(var);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    我的方法
     */
    private void genereteLayout(){ //生成视图层
        if (Thread.currentThread() != Looper.getMainLooper().getThread()){
            log.e(TAG,"当前线程不在主线程 不能创建图层");
            return;
        }
        try {
            //第一层
            if (mBackLayout==null){
                //全宽全高
                mBackLayout = new AbsoluteLayout(mContext);
                LayoutParams lp = new LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT);
                mBackLayout.setLayoutParams(lp);
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

    //取消视图层
    private void unGenereteLayout(){
        try {
            //第一层
            if (mBackLayout!=null){
                this.removeView(mBackLayout);
                mBackLayout = null;
            }
            //第二层
            if (mFontLayout!=null){
                this.removeView(mFontLayout);
                mFontLayout = null;
            }
        } catch (Exception e) {
            log.e(TAG,"unGenereteLayout() err:"+e.getMessage());
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
            if (returnbtn!=null){
                v.removeView(returnbtn);
                v.addView(returnbtn);
                log.d(TAG,"添加返回按钮成功");
            }

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
        log.i(TAG,"playShow() 内容列表 - contentList -> size = "+contentList.size());
        if (contentList==null){
            log.e(TAG,"   contentList is null - ");
            this.removeMeToFather();
            return;
        }
        if (contentList.size()==1){
            log.i(TAG,"   contentList 只有一个内容 - "+ contentList.size());
            currentIndex = 0;
        }else{
            justIndex(tag);
        }

        log.i(TAG,"计算之后的下标 : "+currentIndex);
        if (currentIndex>=contentList.size() || currentIndex<0){
            currentIndex = 0;
        }

        playShow();
    }
    //计算下标
    private void justIndex(int tag) {
        switch (tag){
            case leftTag://左
                log.i(TAG,"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<左边 - 当前下标  "+currentIndex);
                currentIndex--;
                if (currentIndex<0){
                    currentIndex=contentList.size()-1;
                }
                break;

            case rightTag://右
                log.i(TAG,">>>>>>>>>>>>>>>>>>>>>>>>>>>>右边  - 当前下标  "+currentIndex);
                currentIndex++;
                if (currentIndex>=contentList.size()){
                    currentIndex=0;
                }
                break;
        }
    }
}
