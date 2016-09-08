package com.wosplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.wos.Toals;
import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.MyVideoView;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_Close_App;
import com.wosplayer.cmdBroadcast.Command.Schedule.ScheduleReader;
import com.wosplayer.cmdBroadcast.Command.Schedule.ScheduleSaver;
import com.wosplayer.service.MonitorService;
import com.wosplayer.service.RestartApplicationBroad;

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

public class DisplayActivity extends FragmentActivity {

    private static final java.lang.String TAG = DisplayActivity.class.getName();
    public static boolean isSendRestartBroad = true;
    public static AbsoluteLayout baselayout = null;

    public  static AbsoluteLayout main = null;    //存放所有 排期视图 的主容器
    public static FrameLayout frame = null;  //隐藏图层
    public static AbsoluteLayout frame_main = null; //隐藏图层上面的 容器图层
    public static DisplayActivity activityContext = null;

    public static boolean isShowDialog = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);//设置布局文件
        baselayout = (AbsoluteLayout) LayoutInflater.from(this).inflate(R.layout.activity_main,null);

        main = (AbsoluteLayout) this.findViewById(R.id.main);
        frame = (FrameLayout)this.findViewById(R.id.frame_layout);
        frame_main = (AbsoluteLayout)this.findViewById(R.id.frame_layout_main);
        activityContext = this;
        log.i(TAG,"onCreate() 正在执行的所有线程数:"+ Thread.getAllStackTraces().size());

        //开启监听服务
       Intent intent = new Intent(this, MonitorService.class);
        log.d(TAG," 开启<监听>服务");
        this.startService(intent);

        ImageButton closebtn = (ImageButton)findViewById(R.id.closeappbtn);
        closebtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toals.Say("close app");

                new Command_Close_App().Execute("true");

                return false;
            }
        });
        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowDialog = !isShowDialog;
            }
        });
        log.d("--------create over-------------");

    }

    @Override
    public void onStart() {
        log.d(TAG,"onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
       super.onResume();
        log.d(TAG,"onResume");
        //开启通讯服务
        wosPlayerApp.startCommunicationService(this);
        if (activityContext!=null){
            try {
                ScheduleReader.Start(false);
            } catch (Exception e) {
               log.e(TAG,"activity 开始执行读取排期 时 err:"+ e.getMessage());

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        log.d(TAG,"onPause");
        wosPlayerApp.stopCommunicationService(this); //关闭服务
        ScheduleSaver.clear();
        ScheduleReader.clear();
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
        log.d(TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.d(TAG,"onDestroy");
        if(isSendRestartBroad){
            Intent intent  = new Intent();
            intent.setAction(RestartApplicationBroad.action);
            intent.putExtra(RestartApplicationBroad.IS_START,false);
            intent.putExtra(RestartApplicationBroad.KEYS,wosPlayerApp.config.GetStringDefualt("sleepTime","10"));
            sendBroadcast(intent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
                        log.e(TAG,"click back key");
                      DisplayActivity.this.finish();
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
            log.e("帧布局 已显示");
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

        log.e("main childs :"+main.getChildCount());
     //   closeOtherVideo(main);

    }



    /**
     * 打开
     */
    private void openOtherVideo(ViewGroup vg){
        for (int i = 0;i<vg.getChildCount();i++){

            log.e("");
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











}
