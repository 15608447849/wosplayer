package com.wosplayer.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.wosTools.AppToolsFragment;
import com.wosplayer.R;
import com.wosplayer.Ui.element.uitools.ImageStore;
import com.wosplayer.Ui.element.uitools.ImageViewPicassocLoader;
import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.command.kernal.CommandCenter;
import com.wosplayer.command.operation.schedules.ScheduleReader;
import com.wosplayer.service.CommunicationService;


import static com.wosplayer.app.PlayApplication.appContext;


public class DisplayActivity extends Activity {
    private static final java.lang.String TAG = "播放器UI界面控制";
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
                start();
            }
        }
    };

    public  static AbsoluteLayout main = null; //存放所有排期视图的主容器
    public static boolean isPlay = false;//是否可以执行显示ui
    public static DisplayActivity activityContext = null;
    private FrameLayout closebtn ;//左上角 隐藏的 关闭视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logs.i(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
        setContentView(R.layout.activity_main);//设置布局文件
        activityContext = this;
        main = (AbsoluteLayout) this.findViewById(R.id.uilayer);
        closebtn =  (FrameLayout) findViewById(R.id.close_layer);
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
                    stop(false);
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
    }
    @Override
    public void onStart() {
        Logs.i(TAG,"onStart");
        super.onStart();
        //注册指令广播
        registCommand();
        //是否已经设置了服务器信息?
        if (AppTools.isSettingServerInfo(activityContext)){
            //开始工作
            start();
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
            stop(true);
        }
        //注销指令广播
         unregistCommand();
      }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logs.i(TAG,"onDestroy");
    }
    //键盘监听返回事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
                        return true;
                    }
        return super.onKeyDown(keyCode, event);
    }

    //开始工作
    private void start(){
        if (activityContext!=null){
            //初始化数据
            PlayApplication.initConfig();
            playTypeStart();
            //执行监听通讯服务是否存活
            mHandler.post(keepAlive);
        }
    }
    //结束工作 - true,关闭 activity
    private void stop(boolean isclose){
            //关闭通讯服务检测
            mHandler.removeCallbacks(tryCommu);
            mHandler.removeCallbacks(keepAlive);
            playTypeStop();
            if (isclose) finish();
    }
    private void playTypeStart() {
        try {
            isPlay = true;
           ScheduleReader.clear();
           ScheduleReader.Start(false);
        } catch (Exception e) {
            Logs.e(TAG,"activity 开始执行读取排期失败");
        }
    }
    private void playTypeStop() {
        try {
            isPlay = false;
            ScheduleReader.clear();
            UiExcuter.getInstancs().StopExcuter();
            ImageStore.getInstants().clearCache();
        } catch (Exception e) {
            Logs.e(TAG,"activity 停止执行播放排期 时 err:"+ e.getMessage());
        }
    }


    /**
     * 接受命令的广播
     */
    private BroadcastReceiver commandCenter = null;
    private void registCommand() {
        if (commandCenter!=null) return;
        commandCenter = new CommandCenter(this);
        IntentFilter ifl = new IntentFilter();
        ifl.addAction(CommandCenter.action);
        this.registerReceiver(commandCenter,ifl);
    }
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
    private final Runnable tryCommu = new Runnable() {
        @Override
        public void run() {
            Logs.e(TAG,"尝试打开通讯服务进程.");
            PlayApplication.startCommunicationService();
        }
    };

    //尝试连接通讯服务
    private final Runnable keepAlive = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(tryCommu,10*1000);
            PlayApplication.sendMsgToServer(CommunicationService.ALIVE);
            mHandler.postDelayed(keepAlive,45*1000);
        }
    };
    public void communicationLives(){
        //移出 启动通讯服务广播
        mHandler.removeCallbacks(tryCommu);
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
