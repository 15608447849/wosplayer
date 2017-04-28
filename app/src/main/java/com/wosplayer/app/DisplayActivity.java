package com.wosplayer.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.standalone.StandUi;
import com.wosTools.AppToolsFragment;
import com.wosplayer.R;
import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.command.kernal.CommandCenter;
import com.wosplayer.command.kernal.CommandStore;
import com.wosplayer.service.CommunicationService;

public class DisplayActivity extends Activity {
    private static final String TAG = "播放器UI界面控制";
    public  AbsoluteLayout main = null; //存放所有排期视图的主容器
    public static DisplayActivity activityContext = null;
    private FrameLayout closebtn ;//左上角 隐藏的 关闭视图
    public PlayHandler mHandler;
    private FrameLayout fragmentLayer;
    //fragment消息接口
    public DisPlayInterface.onFragAction mFragmentImp;
    /**
     * 接受命令的广播
     */
    private BroadcastReceiver commandCenter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.i(TAG,"onCreate");
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
        setContentView(R.layout.activity_main);//设置布局文件
        initActivity();

    }
    @Override
    public void onStart() {
        super.onStart();
        Logs.i(TAG,"onStart");

        //是否已经设置了服务器信息?
        if (AppUtils.isSettingServerInfo(this)){
            //开始工作
            start();
        }else{
            //设置服务器信息
            openWosTools();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        Logs.i(TAG,"onStop");
        //结束工作
        if (AppUtils.isSettingServerInfo(this)){
            stop(true);
        }
    }




    @Override
    public void onResume() {
        super.onResume();
        Logs.i(TAG,"onResume");
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logs.i(TAG,"onNewIntent");
    }
    @Override
    protected void onPause() {
        super.onPause();
        Logs.i(TAG,"onPause");
    }

    @Override
    protected void onDestroy() {
        Logs.i(TAG,"onDestroy");
        super.onDestroy();
        System.exit(0);
    }
    //键盘监听返回事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
                        return true;}
        return super.onKeyDown(keyCode, event);
    }
    //初始化 activity
    private void initActivity() {
        mHandler = new PlayHandler(this);
        main = (AbsoluteLayout) this.findViewById(R.id.uilayer);
        fragmentLayer = (FrameLayout)this.findViewById(R.id.frgment_layout);
        closebtn =  (FrameLayout) findViewById(R.id.close_layer);
        //弹出密码输入框
        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OverAppDialog.ShowDialog(DisplayActivity.this);
            }
        });
        //长按 显示/隐藏 信息输出
        /*setOnLongClickListener中return的值决定是否在长按后再加一个短按动作true为不加短按,false为加入短按*/
        closebtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mHandler.sendEmptyMessage(PlayHandler.HandleEvent.open.ordinal());
                return true;
            }
        });
        CommandStore.getInstands().init(this);//初始化命令集
    }


    //开始工作
    public void start(){
            playTypeStart();//选择模式
    }
    //结束工作 - true,关闭 activity
    public void stop(boolean isclose){
            closeWosTools();//关闭配置界面
            playTypeStop();//根据模式停止播放
            if (isclose) finish();
    }
    private void playTypeStart() {
        try {
            String mode = SystemConfig.get().GetStringDefualt("playMode","");
            Log.e(TAG,"执行播放模式:"+mode);
            if (mode.equals(SystemConfig.playMode[0])){
                //注册指令广播
                registCommand();
                UiExcuter.getInstancs().onInite(this);//初始化ui
                openCommunication();//开启通讯
            }
            if (mode.equals(SystemConfig.playMode[1])){//单机版本
                StandUi.getInstands().init(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void playTypeStop() {
        try {
            String mode = SystemConfig.get().GetStringDefualt("playMode","");
            if (mode.equals(SystemConfig.playMode[0])){
                //注销命令集
                unregistCommand();
                closeCommunication();//关闭通讯
                UiExcuter.getInstancs().onUnInit();//停止UI界面
            }
            if (mode.equals(SystemConfig.playMode[1])){//单机版本
                StandUi.getInstands().unInin();
            }
        } catch (Exception e) {
          e.printStackTrace();
        }
    }

    private void openCommunication() {
        PlayApplication.startCommunicationService();
        //执行监听通讯服务是否存活
        mHandler.post(keepAlive);
    }
    private void closeCommunication() {
        PlayApplication.stopCommunicationService();
        //关闭通讯服务检测
        mHandler.removeCallbacks(tryCommu);
        mHandler.removeCallbacks(keepAlive);
    }
    //注册广播
    private void registCommand() {
        if (commandCenter!=null) return;
        commandCenter = new CommandCenter();
        IntentFilter ifl = new IntentFilter();
        ifl.addAction(CommandCenter.action);
        this.registerReceiver(commandCenter,ifl);
    }
    //注销广播
    private void unregistCommand() {
        if (commandCenter!=null){
            try {
                this.unregisterReceiver(commandCenter);
            } catch (Exception e) {
            }finally {
                commandCenter = null;
            }
        }
    }


    //尝试连接通讯服务
    private final Runnable keepAlive = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(tryCommu,10*1000);
            PlayApplication.sendMsgToServer(CommunicationService.ALIVE);//发送存活检测信息
            mHandler.postDelayed(keepAlive,60*1000);
        }
    };
    private final Runnable tryCommu = new Runnable() {
        @Override
        public void run() {
            Logs.e(TAG,"尝试打开通讯服务进程.");
            PlayApplication.startCommunicationService();
        }
    };
    public void communicationLives(){
        //移出启动通讯服务广播
        if (mHandler!=null){
            Logs.d(TAG,"本地通讯正常.");
            mHandler.removeCallbacks(tryCommu);
        }
    }



    //-------------------------------------------------wosTools -----------------------------------//

    //打开配置界面
    public void openWosTools() {
        if (fragmentLayer.getVisibility() == View.GONE) fragmentLayer.setVisibility(View.VISIBLE);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment wostools= fm.findFragmentByTag(AppToolsFragment.FLAG);
            if (wostools==null){
                wostools = new AppToolsFragment();
                ft.add(R.id.frgment_layout,wostools,AppToolsFragment.FLAG);
            }
            ft.show(wostools);
            ft.commitAllowingStateLoss();
            mFragmentImp = (DisPlayInterface.onFragAction) wostools;
    }
    //关闭配置界面
    public void closeWosTools() {
        mFragmentImp = null;
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment wostools= fm.findFragmentByTag(AppToolsFragment.FLAG);
        if (wostools!=null){
            ft.hide(wostools);
            ft.commitAllowingStateLoss();
        }
        if (fragmentLayer.getVisibility() == View.VISIBLE) fragmentLayer.setVisibility(View.GONE);
    }

}
