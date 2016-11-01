package com.wosplayer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wos.SdCardTools;
import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.MyVideoView;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.app.inputPassWordDialog;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.Schedule.ScheduleReader;
import com.wosplayer.cmdBroadcast.Command.Schedule.ScheduleSaver;
import com.wosplayer.service.MonitorService;
import com.wosplayer.service.RestartApplicationBroad;

import java.util.Timer;
import java.util.TimerTask;

import wosTools.DataListEntiy;
import wosTools.RequstTerminal;
import wosTools.ToolsHandler;

import static com.wosplayer.app.wosPlayerApp.config;

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

    private static final java.lang.String TAG = DisplayActivity.class.getName();
    public static boolean isSendRestartBroad = true;
    public static AbsoluteLayout baselayout = null;

    public  static AbsoluteLayout main = null;    //存放所有 排期视图 的主容器
    public static FrameLayout frame = null;  //隐藏图层
    public static AbsoluteLayout frame_main = null; //隐藏图层上面的 容器图层
    public static DisplayActivity activityContext = null;


    private ImageButton closebtn ;//左上角 隐藏的 按钮

    public static boolean isShowDialog = false;
    private boolean isShowPsdInput = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮

        setContentView(R.layout.activity_main);//设置布局文件

        baselayout = (AbsoluteLayout) LayoutInflater.from(this).inflate(R.layout.activity_main,null);
        main = (AbsoluteLayout) this.findViewById(R.id.main);
        frame = (FrameLayout)this.findViewById(R.id.frame_layout);
        frame_main = (AbsoluteLayout)this.findViewById(R.id.frame_layout_main);

        closebtn =  (ImageButton)findViewById(R.id.closeappbtn);

        activityContext = this;

        log.i(TAG,"onCreate() 正在执行的所有线程数:"+ Thread.getAllStackTraces().size());

        //开启监听服务
       Intent intent = new Intent(this, MonitorService.class);
        log.d(TAG,"-------------------------------------------------------------- 开启<监听>服务 -----------------------------------------------------------");
        this.startService(intent);


        //弹出密码输入框
        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               inputPassWordDialog.ShowDialog(DisplayActivity.this);
            }
        });


        //长按 显示/隐藏 信息输出
        /*setOnLongClickListener中return的值决定是否在长按后再加一个短按动作
                true为不加短按,false为加入短按*/
        closebtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // isShowDialog = !isShowDialog;
                //  Toast.makeText(DisplayActivity.this,"长按 isShowDialog 显示值:"+isShowPsdInput,Toast.LENGTH_LONG).show();
                //  Intent toTools = new Intent(DisplayActivity.this, ToolsActivity.class);
                //  startActivity(toTools);
                settingServerInfo(false);
                Toast.makeText(DisplayActivity.this,"--- 下次将进入设置服务器信息 ---",Toast.LENGTH_LONG).show();
                return true;
            }
        });

        log.d("--------create over-------------");
    }

    @Override
    public void onStart() {
        log.d(TAG,"onStart");
        super.onStart();
        if(isSettingServerInfo()){
            ((wosPlayerApp)this.getApplication()).init(true);
        }
    }

    @Override
    public void onResume() {
       super.onResume();
        log.d(TAG,"onResume");
        //是否已经设置了服务器信息?
        if (isSettingServerInfo()){
            //开始工作
            startWork();
        }else{
            //设置服务器信息
            settingServerInfoDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        log.d(TAG,"onPause");
        //结束工作
        if (isSettingServerInfo()){
            endWork();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        log.d(TAG,"onStop");

      }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.d(TAG,"onDestroy");
        try {
            if(isSendRestartBroad){
                Intent intent  = new Intent();
                intent.setAction(RestartApplicationBroad.action);
                intent.putExtra(RestartApplicationBroad.IS_START,false);
                intent.putExtra(RestartApplicationBroad.KEYS, config.GetStringDefualt("sleepTime","10"));
                sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    //-----------------------------------------------------------------------


    //开始工作
    private void startWork(){
        if (activityContext!=null){
            //开启通讯服务
            wosPlayerApp.startCommunicationService(this);
            try {
                ScheduleSaver.clear();
                ScheduleReader.clear();
                ScheduleReader.Start(false);
            } catch (Exception e) {
                log.e(TAG,"activity 开始执行读取排期 时 err:"+ e.getMessage());

            }
        }
    }


    //结束工作
    private void endWork(){
        try {
            wosPlayerApp.stopCommunicationService(this); //关闭服务
//            ScheduleSaver.clear();
//            ScheduleReader.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            finish();
        }

    }






    //--------------------------------------

    /**
     * 设置服务器信息成功 true
     * 失败 false
     * @param flag
     */
    private void settingServerInfo(boolean flag){
        SharedPreferences preferences = this.getSharedPreferences(DisplayActivity.this.getLocalClassName(), Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("flag", flag);
        editor.commit();
    }
    /**
     * 读取 是否设置服务器信息
     */
    private boolean isSettingServerInfo(){
        SharedPreferences preferences =this.getSharedPreferences(DisplayActivity.this.getLocalClassName(), Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        return preferences.getBoolean("flag", false);
    }




    //-------------------------------------------------wosTools -----------------------------------//
    private EditText serverip;
    private EditText      serverport;
    private EditText      companyid;
    private EditText      terminalNo;
    private EditText      BasePath;
    private EditText      heartbeattime;
    private EditText     StorageLimits;
    private LinearLayout restartLayout;

//    private Button btnGetID;
//    private Button        btnSaveData;
    private void settingServerInfoDialog() {
        //设置布局文件
        setContentView(R.layout.activity_wostools);
        //设置handle
        ToolsHandler.woshandler=new ToolsHandler(this);
        //加载配置
        InitConfig();
        //加载布局
        InitView();
        //初始化控件数据
        InitValue();
        //是否设置定时器
        initTimer();

    }

    private void initTimer() {
        if (wosPlayerApp.isWosToolsDataTranslation){
            log.e(TAG,"--- initTimer() ---");
            //有数据转移
            new Timer().schedule(
                    new TimerTask() {
                @Override
                public void run() {
                save();
                wosPlayerApp.isWosToolsDataTranslation =false;
                DisplayActivity.activityContext.finish();
                }
            },15*1000);
        }
    }

    /**
     * 加载布局
     */
    public void InitView()
    {
        try
        {
            serverip=(EditText)this.findViewById(R.id.serverip);
            serverport=(EditText)this.findViewById(R.id.serverport);

            companyid=(EditText)this.findViewById(R.id.companyid);
            terminalNo=(EditText)this.findViewById(R.id.terminalNo);
            BasePath=(EditText)this.findViewById(R.id.BasePath);
            heartbeattime=(EditText)this.findViewById(R.id.HeartBeatInterval);
            StorageLimits=(EditText)this.findViewById(R.id.StorageLimits);


//            btnGetID=(Button)this.findViewById(R.id.btnGetID);
//            btnSaveData=(Button)this.findViewById(R.id.btnSaveData);

            restartLayout = (LinearLayout)findViewById(R.id.layotu_restartbeattime);

        }catch(Exception e)
        {
            log.e("InitView() err:"+e.getMessage());
        }
    }
    public DisplayMetrics m_dm;
    public DataListEntiy dataList;
    /**
     *
     */
    /**
     * 加载配置
     */
    public  void InitConfig()
    {
        log.i("toolsActivity", "开始加载数据");
        m_dm = new DisplayMetrics();
        //获取手机分辨率
        this.getWindowManager().getDefaultDisplay().getMetrics(m_dm);
        dataList=new DataListEntiy();
        dataList.ReadShareData(true);
    }

    /**
     *  加载数据
     */
    public void InitValue()
    {
        try
        {
            serverip.setText(dataList.GetStringDefualt("serverip", "127.0.0.1"));
            serverport.setText(dataList.GetStringDefualt("serverport", "8000"));
            companyid.setText(dataList.GetStringDefualt("companyid", "999"));
            terminalNo.setText(dataList.GetStringDefualt("terminalNo", ""));
            heartbeattime.setText(dataList.GetStringDefualt("HeartBeatInterval", "30"));
            BasePath.setText(dataList.GetStringDefualt("basepath", "mnt/sdcard"));
            StorageLimits.setText(dataList.GetStringDefualt("storageLimits","50"));
            //焦点默认在这个控件上
            serverip.setFocusable(true);

        }catch(Exception e)
        {
            Log.e("ToolsActivity ", e.getMessage());
        }
    }


    /**
     * 获取控件传入的数据并封装
     */
    public void GetViewValue()
    {
        dataList.put("terminalNo",terminalNo.getText().toString());//终端id
        dataList.put("serverip",  serverip.getText().toString());//服务器ip
        dataList.put("serverport",  serverport.getText().toString());//终端端口
        dataList.put("companyid",  companyid.getText().toString());//公司id
        dataList.put("HeartBeatInterval",  heartbeattime.getText().toString());//心跳
        dataList.put("storageLimits",StorageLimits.getText().toString());//sdcard 清理阔值

        String basepath=BasePath.getText().toString();//资源存储的 文件名
//      例: xxx前缀 /basepath/资源1
        if (!basepath.startsWith("/")){
            basepath = "/"+basepath;
        }
        if(!basepath.endsWith("/"))
        {
            basepath=basepath+"/";
        }

        basepath = SdCardTools.appSourcePath+basepath;
        dataList.put("basepath",  basepath);

        //建设银行接口资源下载位置
        basepath = SdCardTools.appSourcePath+SdCardTools.Construction_Bank_dir_source;
        dataList.put("bankPathSource",basepath);

        basepath = SdCardTools.appSourcePath+SdCardTools.Construction_Bank_dir_xmlfile;
        dataList.put("bankPathXml",basepath);



    }


    /**
     * 保存
     */
    public void save(){
        GetViewValue();
        dataList.SaveShareData();
        if (!"".equals(terminalNo.getText().toString())){
            settingServerInfo(true);
        }
    }
    /**
     * 点击获取id
     * @param view
     */
    public void getId(View view){
        if(dataList!=null && m_dm!=null){
            //结束线程
            RequstTerminal.EndRequst();
            //把数据封装到集合中
            GetViewValue();
            //开启线程
            RequstTerminal.BeginRequst(dataList,m_dm);
        }
    }

    /**
     * 点击保存数据
     * @param view
     */
    public void saveData(View view){

        save();
        this.finish();
    }

    /**
     * 点击设置重启时间
     */
    public void restartTime(View view){
        //如果布局不存在
        if (restartLayout.getVisibility()==View.GONE){
            //显示出来
            restartLayout.setVisibility(View.VISIBLE);
            Button sure = (Button) findViewById(R.id.btnRestartComplete);
            sure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //保存数据
                   dataList.put("sleepTime",((EditText)findViewById(R.id.RestartBeatInterval)).getText().toString());
                    outText("已设置重启间隔: "+((EditText)findViewById(R.id.RestartBeatInterval)).getText().toString() );
                    restartLayout.setVisibility(View.GONE);
                }
            });
        }else{
            //隐藏起来
            restartLayout.setVisibility(View.GONE);
        }
    }

    //反射调用
    public void outText(String text)
    {
        Toast.makeText(this, text,Toast.LENGTH_LONG ).show();
    }
    public void setcompanyid(String value)
    {
        try
        {
            terminalNo.setText(value);
            dataList.put("terminalNo", value);
            outText("申请终端ID成功");

            terminalNo.setFocusable(true);

        }catch(Exception e)
        {
            Log.e("ToolsActivity", e.getMessage());
        }
    }



}
