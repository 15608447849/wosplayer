package com.wosplayer.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.MyVideoView;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.Schedule.ScheduleReader;

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

    public static FrameLayout baselayout = null;
    public  static AbsoluteLayout main = null;    //存放所有 视图 的主容器
    public static FrameLayout frame = null;  //隐藏图层
    public static AbsoluteLayout frame_main = null; //隐藏图层上面的 容器图层
    public static DisplayActivity activityContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//去标题
        setContentView(R.layout.activity_main);//设置布局文件
        baselayout = (FrameLayout)this.findViewById(R.id.baselayout);
        main = (AbsoluteLayout) this.findViewById(R.id.main);
        frame = (FrameLayout)this.findViewById(R.id.frame_layout);
        frame_main = (AbsoluteLayout)this.findViewById(R.id.frame_layout_main);
        activityContext = this;
        log.i(TAG,"正在执行的所有线程数:"+ Thread.getAllStackTraces().size());

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
       super.onResume();
        //开启通讯服务
        wosPlayerApp.startCommunicationService();
        ScheduleReader.Start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        wosPlayerApp.stopCommunicationService(); //关闭服务
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

/////////////////////////////////////////////////////////////////////////////////////
    public void FrameBtnEvent(View view){
        goneLayoutdialog();
    }

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
            openOtherVideo(main);
        }
    }


    /**
     * 显示帧布局
     */
    public void visibleLayoutDialog(boolean isShowLoadWaitImage){
        //如果是显示的
        if (frame.getVisibility() == View.VISIBLE){
            return;
        }

        //显示
        frame.setVisibility(View.VISIBLE);



        if (isShowLoadWaitImage){
            //显示 图片
            findViewById(R.id.frame_load_wait_image).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.frame_load_wait_image).setVisibility(View.GONE);
        }

        closeOtherVideo(main);

    }



    /**
     * 打开
     */
    private void openOtherVideo(ViewGroup vg){
        for (int i = 0;i<vg.getChildCount();i++){

            View view = vg.getChildAt(i);
            if (view instanceof ViewGroup){
                closeOtherVideo((ViewGroup) view);
            }
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
            if (view instanceof ViewGroup){
                closeOtherVideo((ViewGroup) view);
            }
            if (view instanceof MyVideoView){
                MyVideoView v = (MyVideoView)view;
                v.pause();
            }
        }
    }











}