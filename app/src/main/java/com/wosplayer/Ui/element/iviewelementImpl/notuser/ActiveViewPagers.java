package com.wosplayer.Ui.element.iviewelementImpl.notuser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;

import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.IinteractionPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/6/24.
 *
 */
public class ActiveViewPagers extends ViewPager implements IviewPlayer {

    private static final java.lang.String TAG = ActiveViewPagers.class.getName();
    private ArrayList<View> myViewList = new ArrayList<View>();
    private View mCurrentView; //当前视图
    private ViewpagerAttrAnimotion vpAnimo = null;
    /**
     * 适配器
     */
    private PagerAdapter pa = new PagerAdapter() {
        /**
         * 这个方法，是获取当前窗体界面数
         * 返回页卡的数量
         * @return
         */
        @Override
        public int getCount() {
            return myViewList.size();
        }

        /**
         * 用于判断是否由对象生成界面
         * @param view
         * @param object
         * @return
         *
         */
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;//官方提示这样写
        }

        /**
         * return一个对象，这个对象表明了PagerAdapter适配器选择哪个对象*放在当前的ViewPager中
         * 用来实例化页
         * @param container
         * @param position
         * @return
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            container.addView((View) myViewList.get(position), 0);//添加页卡
            return  myViewList.get(position);//super.instantiateItem(container, position);

            /*container.removeView((View) myViewList.get(position));
            container.addView((View) myViewList.get(position));
            return myViewList.get(position);*/


        }

        @Override
        public int getItemPosition(Object object) {
             return POSITION_NONE;//super.getItemPosition(object);
        }

        /**
         * 从ViewGroup中移出当前View
         * @param container
         * @param position
         * @param object
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((View) myViewList.get(position));//删除页卡
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mCurrentView = (View)object; //当前视图
        }
    };



    /**
     * 构造
     * @param context
     */
    public ActiveViewPagers(Context context) {
        super(context);

        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,0,0);
        this.setLayoutParams(lp);
        this.setBackgroundColor(Color.WHITE);
        this.setAdapter(pa); //适配器
        this.addOnPageChangeListener(new mVPageChangger());//滑动监听


        //页面切换效果
        this.setPageTransformer(true, new PageTransformer() {
            /**
             * 页面滑动时回调的方法,
             * @param page 当前滑动的view
             * @param position 当从右向左滑的时候,左边page的position是[0一-1]变化的
             * 右边page的position是[1一0]变化的,再次滑动的时候,刚才变化到-1(即已经画出视野的page)將从-1变化到-2,
             * 而当前可见的page和右边滑过来的page的position将再次从[0一-1]变化 和 [1一0]变化   但是我们关心是position是[-1一1]变化的
             * page,所以处理动画的时候需要我们过滤一下
             */
            @Override
            public void transformPage(View page, float position) {
                if (vpAnimo!=null){
                    vpAnimo.TranslationPager(page,position);
                }
            }
        });
    }

    private Button left;
    private Button right;
    private int mCurrentPos=0;

    /**
     * 加载资源
     */
    @Override
    public void AotuLoadingResource() {

        if (mFather==null){
            return;
        }

        //切换动画
        this.vpAnimo = ViewpagerAttrAnimotion.getInstands();

        //创建左右滑动按钮
        left = new Button(DisplayActivity.activityContext);
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.left);
        BitmapDrawable bd = new BitmapDrawable(this.getResources(), bitmap);
        left.setBackgroundDrawable(bd);
        left.setLayoutParams(new AbsoluteLayout.LayoutParams(60, 60, 0, (mFather.getLayoutParams().height/2)-60));
        left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ScollToLeft();

            }
        });

        right = new Button(DisplayActivity.activityContext);
        Bitmap bitmap2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.right);
        BitmapDrawable bd2 = new BitmapDrawable(this.getResources(), bitmap2);
        right.setBackgroundDrawable(bd2);
        right.setLayoutParams(new AbsoluteLayout.LayoutParams(60, 60, mFather.getLayoutParams().width-60, (mFather.getLayoutParams().height/2)-60));
        right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ScollToRight();
            }
        });
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                ((AbsoluteLayout)mFather).removeView(left);
                ((AbsoluteLayout)mFather).addView(left);

                ((AbsoluteLayout)mFather).removeView(right);
                ((AbsoluteLayout)mFather).addView(right);
            }
        });
    }



    private void ScollToRight() {
        log.e(TAG,"ScollToRight");
        int index = ++mCurrentPos;
        if ( index<= ActiveViewPagers.this.myViewList.size() - 1) {
            //现在的下标如果小于最后一个 向右边滑动
            ActiveViewPagers.this.setCurrentItem(index);
        } else {
            //现在的下标如果大于最后一个  设置为第一个
            mCurrentPos = 0;
            ActiveViewPagers.this.setCurrentItem(mCurrentPos);
        }
        log.e(TAG,"ScollToRight over");
    }

    private void ScollToLeft() {
        int index = --mCurrentPos;
        if (index >= 0) { //如果比最左边的大 左滑动
            ActiveViewPagers.this.setCurrentItem(index);

        } else {

            mCurrentPos = (ActiveViewPagers.this.myViewList.size()-1);
            ActiveViewPagers.this.setCurrentItem(mCurrentPos);
        }
    }
    //移除左右視圖對象
    private void removeLeftAndRightButton() {
        if (left!=null){
            releativeBtnBgImage(left);
            left = null;
        }
        if (right!=null){
            releativeBtnBgImage(right);
            left=null;
        }

    }
    //移除左右背景資源
    private void releativeBtnBgImage(final Button ibtn){
        Drawable drawable = ibtn.getBackground();
        if (drawable!=null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable != null) {
                if (!bitmapDrawable.getBitmap().isRecycled()) {
                    bitmapDrawable.getBitmap().recycle();
                    bitmapDrawable.setCallback(null);
                    ibtn.setBackgroundResource(0);
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        }catch (Exception e){
            log.e(TAG,"" + e.getMessage());
        }
    }

    //移除资源  解除与我绑定的对象 需要在主線程執行
    private void releasedResource(){
        stopTimer();//停止計時器
                //清空 左右按鈕 資源
                removeLeftAndRightButton();
                ActiveViewPagers.this.removeAllViews();//移除全部的視圖
    }
    private View mFather=null;
    private FrameLayout returnbtn;//返回按钮
    //重载　添加带按钮　的　
    public void setMyReturnBtn(FrameLayout buttonLayout){
        returnbtn = buttonLayout;
    }

    /**
     * 设置Viewpage 的 大小
     * 把自己 添加到 父布局上
     */
    public void addMeToFather(View f){

        if(f!=null){
            this.mFather = f;
        }
        if (mFather!=null) {

            if(mFather instanceof AbsoluteLayout) {

                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        ((AbsoluteLayout)mFather).removeView(ActiveViewPagers.this);
                        ((AbsoluteLayout)mFather).addView(ActiveViewPagers.this);
                        if(returnbtn!=null){
                            ((AbsoluteLayout) mFather).removeView(returnbtn);
                            ((AbsoluteLayout) mFather).addView(returnbtn);
                        }
                       IinteractionPlayer.worker.schedule(new Action0() {
                            @Override
                            public void call() {
                                AotuLoadingResource();
                                startTimer();
                            }
                        });

                    }
                });


            }
        }
    }

    public void addMeToFather(View view, boolean f) {
        //null
        addMeToFather(view);
    }

    @Override
    public void removeMeToFather(){
        if (mFather!=null) {
            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {

                     if(left!=null){
                          ((AbsoluteLayout)mFather).removeView(left);
                    }
                    if(right!=null){
                            ((AbsoluteLayout)mFather).removeView(right);
                    }

                    ((AbsoluteLayout)mFather).removeView(ActiveViewPagers.this);
                    mFather = null;
                    releasedResource();
                }
            });



        }
    }

    public void removeMeToFather(boolean f) {
        removeMeToFather();
    }

    @Override
    public int getPlayDration(IviewPlayer iviewPlayer) {
        return 0;
    }

    @Override
    public void otherMother(Object object) {

    }


    /**
         * 添加子视图对象
         *
         */
        public void addMeSubView(IviewPlayer iview){

            AbsoluteLayout loadLayout = new AbsoluteLayout(DisplayActivity.activityContext);
            loadLayout.setLayoutParams(
                    new AbsoluteLayout.LayoutParams(
                            AbsoluteLayout.LayoutParams.MATCH_PARENT,
                            AbsoluteLayout.LayoutParams.MATCH_PARENT,
                            0,
                            0));
            loadLayout.setBackgroundColor(Color.TRANSPARENT);//透明
//            iview.addMeToFather(loadLayout,false);
        if(!myViewList.contains(loadLayout)){ //不存在
            myViewList.add(loadLayout);
        }
        this.pa.notifyDataSetChanged();
    }


    public void downloadResult(String filePath) {
        //
    }


    /**
     * 滑动监听
     */
    class  mVPageChangger implements OnPageChangeListener {
    /**
     * 滑动完成
     * @param position
     * @param positionOffset
     * @param positionOffsetPixels
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * 选择完成
     * @param position
     */
    @Override
    public void onPageSelected(int position) {
//        mCurrentPos = position;
    }

    /**
     * 滑动状态监听
     * @param state
     */
    @Override
    public void onPageScrollStateChanged(int state) {
       // Log.i("view","onPageScrollStateChanged:"+ state);
        if (state==1){ // 正在滑动1 滑动完毕2 不动3空闲

        }else if (state==2){

            //释放别的view的bitmap
            for (int i = 0;i<ActiveViewPagers.this.getChildCount();i++){
                ViewGroup layout = (ViewGroup) ActiveViewPagers.this.getChildAt(i);

                View v = ((ViewGroup)mCurrentView).getChildAt(0);
                if (v instanceof IviewPlayer){
//                    ((IviewPlayer)v).removeMeToFather(false);
                    ((IviewPlayer)v).AotuLoadingResource();
                }
            }

//            View view = ((ViewGroup)mCurrentView).getChildAt(0);
//            log.d(TAG,"当前页 :"+view.getTag());
//
//            if (view instanceof IviewPlayer){
//                ((IviewPlayer)view).AotuLoadingResource();
//            }
//            log.d(" \n\r \n\r");


        }else if(state == ViewPager.SCROLL_STATE_IDLE){

        }

    }
}

    @Override
    public void scrollTo(int x, int y) {
//        Log.i("views", myViewList.size()+"");
        if(myViewList.size()<=1){
            return;
        }
        super.scrollTo(x, y);
    }


    /**
     *
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){

        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN){

        }
        return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        return super.onTouchEvent(event);
    }


    private Timer timer = null;
    private TimerTask timerTask  = null;
    //開始
    private void startTimer(){
        stopTimer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                log.e(TAG,"Thread name:"+Thread.currentThread().getName());
                AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        ScollToRight();
                    }
                });
            }
        };
        timer = new Timer();
        timer.schedule(timerTask,3*1000,30*1000);
    }
    //停止
    private void stopTimer(){
        if (timerTask != null){
            timerTask.cancel();
            timerTask=null;

        }
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
    }


}
