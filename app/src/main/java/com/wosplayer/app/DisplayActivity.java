package com.wosplayer.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.wosTools.AppToolsFragment;
import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.mycons_view.MyVideoView;
import com.wosplayer.Ui.element.iviewelementImpl.uitools.ImageStore;
import com.wosplayer.Ui.element.iviewelementImpl.uitools.ImageViewPicassocLoader;
import com.wosplayer.Ui.element.interfaces.IviewPlayer;
import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.command.operation.schedules.ScheduleReader;
import com.wosplayer.command.operation.schedules.ScheduleSaver;


import static com.wosplayer.app.PlayApplication.appContext;

/**
 *  Timer timer = new Timer();
 timer.schedule(new TimerTask() {
@Override
public void run() {
String terminalNo = wosPlayerApp.config.GetStringDefualt("terminalNo","00000");
String msg = "123:" + terminalNo;
wosPlayerApp.sendMsgToServer(msg);
}
},5000);
 */

public class DisplayActivity extends Activity {
    /**
     *
     public static final int sucess = 0x05;
     public static final int outtext = 0x06;
     */

    public enum HandleEvent{
        success,outtext,close,
    }

    public final  Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
           if (msg.what == HandleEvent.outtext.ordinal()){
              AppTools.Toals(appContext,msg.obj.toString());
           }

            if (msg.what == HandleEvent.success.ordinal()){
                if (frgAct!=null){
                    frgAct.sendMessage(msg.obj);
                }
        }
            if (msg.what == HandleEvent.close.ordinal()){
                //关闭 配置服务信息的fragment
                closeWosTools();
                //开始工作
                StartWork();
            }
        }
    };

    private static final java.lang.String TAG = "播放器UI界面控制";
    public static AbsoluteLayout baselayout = null;

    public  static AbsoluteLayout main = null;    //存放所有 排期视图 的主容器
    public static FrameLayout frame = null;  //隐藏图层
    public static AbsoluteLayout frame_main = null; //隐藏图层上面的 容器图层
    public static DisplayActivity activityContext = null;

    private ImageButton closebtn ;//左上角 隐藏的 按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityContext = this;
        ((PlayApplication)getApplication()).startAppInit(mHandler);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
        setContentView(R.layout.activity_main);//设置布局文件
        baselayout = (AbsoluteLayout) LayoutInflater.from(this).inflate(R.layout.activity_main,null);
        main = (AbsoluteLayout) this.findViewById(R.id.main);
        frame = (FrameLayout)this.findViewById(R.id.frame_layout);
        frame_main = (AbsoluteLayout)this.findViewById(R.id.frame_layout_main);
        closebtn =  (ImageButton)findViewById(R.id.closeappbtn);
        Logs.i(TAG,"onCreate() 正在执行的所有线程数:"+ Thread.getAllStackTraces().size());

        //弹出密码输入框
        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               OverAppDialog.ShowDialog(DisplayActivity.this);
            }
        });
        //长按 显示/隐藏 信息输出
        /*setOnLongClickListener中return的值决定是否在长按后再加一个短按动作
                true为不加短按,false为加入短按*/
        closebtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (frgAct!=null){
                    AppTools.Toals(appContext,"请设置播放器参数");
                }else{
                    AppTools.settingServerInfo(activityContext,false);
                    //停止工作
                    StopWork(false);
                    AppTools.Toals(appContext,"2秒后进入配置界面");
                    //打开配置界面
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            openWosTools();
                        }
                    },2*1000);
                }

                return true;
            }
        });

        Logs.i("activity --------create over-------------");
    }
    @Override
    public void onStart() {
        Logs.i(TAG,"onStart");
        super.onStart();
        //是否已经设置了服务器信息?
        if (AppTools.isSettingServerInfo(activityContext)){
            //开始工作
            StartWork();
        }else{
            //设置服务器信息
            openWosTools();
        }
    }
    @Override
    public void onResume() {
       super.onResume();
        Logs.i(TAG,"onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Logs.i(TAG,"onPause");

    }

    @Override
    public void onStop() {
        super.onStop();
        Logs.i(TAG,"onStop");
        //结束工作
        if (AppTools.isSettingServerInfo(appContext)){
            StopWork(true);
        }
      }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logs.i(TAG,"onDestroy");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
//                        log.e(TAG,"click back key");
//                      DisplayActivity.this.finish();
                        return true;
                    }
        return super.onKeyDown(keyCode, event);
    }


    /////////////////////////////////////////////////////////////////////////////////////
    public void FrameBtnEvent(View view){
        goneLayoutdialog();
    }//返回按钮

    //隐藏帧布局
    public void goneLayoutdialog(){
        //退出 framelayout
        if (frame != null){

            //如果是显示的  销毁上面的绝对布局上面的所有视图
            if (frame.getVisibility() == View.GONE){
                return;
            }

            for (int i = 0; i < frame_main.getChildCount(); i++){
                View v =  frame_main.getChildAt(i);

                if (v instanceof IviewPlayer){
                    ((IviewPlayer)v).removeMeToFather();
                }
            }
            //隐藏
            frame.setVisibility(View.GONE);
           // openOtherVideo(main);
        }
    }


    /**
     * 显示帧布局
     */
    public void visibleLayoutDialog(boolean isShowLoadWaitImage,AbsoluteLayout.LayoutParams p){
        //如果是显示的
        if (frame.getVisibility() == View.VISIBLE){
            Logs.e("帧布局 已显示");
            return;
        }

        //显示
        frame.setVisibility(View.VISIBLE);

        frame.setLayoutParams(p);

        if (isShowLoadWaitImage){
            //显示 图片
            findViewById(R.id.frame_load_wait_image).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.frame_load_wait_image).setVisibility(View.GONE);
        }

        Logs.e("main childs :"+main.getChildCount());
     //   closeOtherVideo(main);
    }

    /**
     * 打开
     */
    private void openOtherVideo(ViewGroup vg){
        for (int i = 0;i<vg.getChildCount();i++){

            Logs.e("");
            View view = vg.getChildAt(i);
            if (view instanceof AbsoluteLayout ||  view instanceof ViewGroup){
                closeOtherVideo((ViewGroup) view);
            }
//            if (view instanceof IVideoPlayer){
//                IVideoPlayer v = (IVideoPlayer)view;
//                v.start();
//            }
            if (view instanceof MyVideoView){
                MyVideoView v = (MyVideoView)view;
                v.start();
            }
        }
    }
    /**
     * 关闭 其他视频资源
     */
    private void closeOtherVideo(ViewGroup vg){

        for (int i = 0;i<vg.getChildCount();i++){

            View view = vg.getChildAt(i);
            if (view instanceof AbsoluteLayout || view instanceof ViewGroup ){
                closeOtherVideo((ViewGroup) view);
            }
//            if (view instanceof IVideoPlayer){
//                IVideoPlayer v = (IVideoPlayer)view;
//                v.stop();
//            }
            if (view instanceof MyVideoView){
                MyVideoView v = (MyVideoView)view;
                v.pause();
            }
        }
    }
    //-----------------------------------------------------------------------
    //开始工作
    private void StartWork(){

        if (activityContext!=null){
            //初始化数据
           ((PlayApplication)this.getApplication()).initConfig();
            //开启通讯服务
            PlayApplication.startCommunicationService(this);
            PlayTypeStart();
        }
    }



    //结束工作 - true,关闭 activity
    private void StopWork(boolean isclose){
            PlayApplication.stopCommunicationService(this); //关闭服务
            PlayTypeStop();
            if (isclose) finish();
    }

    private void PlayTypeStart() {
        //如果是富滇银行-全屏数据显示
        try {
            ScheduleSaver.clear();
            ScheduleReader.clear();
            ScheduleReader.Start(false);
        } catch (Exception e) {
            Logs.e(TAG,"activity 开始执行读取排期失败");
        }

    }
    private void PlayTypeStop() {
        //如果是富癫银行- 清理fragment
        try {
        } catch (Exception e) {
            Logs.e(TAG,"activity 停止执行播放排期 时 err:"+ e.getMessage());
        }
        ScheduleSaver.clear();
        ScheduleReader.clear();
        UiExcuter.getInstancs().StopExcuter();
        ImageStore.getInstants().clearCache();
    }


    //设置 main 视图 背景
    public void setMainBg(final String var){
        if (var==null || var.equals("null")){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        main.setBackgroundDrawable(null);
                        main.setBackgroundColor(Color.WHITE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }else
        if (var.startsWith("#")){
            //颜色
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        main.setBackgroundColor(Color.parseColor(var));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }else{
            //图片
            Bitmap bitmap = ImageViewPicassocLoader.getBitmap(var,null);
            if (bitmap==null){
                //获取错误图片bitmap
                final String errorTag = "errorimage";
                bitmap = ImageStore.getInstants().getBitmapCache(errorTag);
                if (bitmap==null || bitmap.isRecycled()){
                    bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.error);
                    ImageStore.getInstants().addBitmapCache(errorTag,bitmap);
                }
            }
            final BitmapDrawable drawable = new BitmapDrawable(bitmap);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        main.setBackgroundDrawable(drawable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    //-------------------------------------------------wosTools -----------------------------------//

    //通讯接口
    public interface onFragAction{
        void sendMessage(Object obj);
    }
    private onFragAction frgAct;
    //打开配置界面
    private void openWosTools() {
            findViewById(R.id.frgment_layout).setVisibility(View.VISIBLE);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment wostools= fm.findFragmentByTag(AppToolsFragment.FLAG);
            if (wostools==null){
                wostools = new AppToolsFragment();
                ft.add(R.id.frgment_layout,wostools,AppToolsFragment.FLAG);
            }
            ft.show(wostools);
            ft.commitAllowingStateLoss();
            frgAct = (onFragAction)wostools;
    }
    //关闭配置界面
    private void closeWosTools() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment wostools= fm.findFragmentByTag(AppToolsFragment.FLAG);
        if (wostools!=null){
            ft.hide(wostools);
            ft.commitAllowingStateLoss();
        }
        frgAct = null;
        findViewById(R.id.frgment_layout).setVisibility(View.GONE);
    }
}
