package com.wosplayer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.wosplayer.R;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.broadcast.Command.Schedule.ScheduleReader;

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

public class activity extends Activity {

    private static final java.lang.String TAG = activity.class.getName();
    public  static AbsoluteLayout main = null;    //存放所有 视图 的主容器
    public static FrameLayout frame = null;  //隐藏图层
    public static AbsoluteLayout frame_main = null; //隐藏图层上面的 容器图层
    public static activity activityContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//去标题
        setContentView(R.layout.activity_main);//设置布局文件
        main = (AbsoluteLayout) this.findViewById(R.id.main);
        frame = (FrameLayout)this.findViewById(R.id.frame_layout);
        frame_main = (AbsoluteLayout)this.findViewById(R.id.frame_layout_main);
        activityContext = this;
        log.i(TAG,"正在执行的所有线程数:"+ Thread.getAllStackTraces().size());
        ScheduleReader.Start();
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
    }

    @Override
    public void onStop() {
        super.onStop();
        wosPlayerApp.stopCommunicationService(); //关闭服务
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

/////////////////////////////////////////////////////////////////////////////////////
    public void FrameBtnEvent(View view){

    }

}
