package com.wosTools;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.wosplayer.R;
import com.wosplayer.app.AdbCommand;
import com.wosplayer.app.AppUtils;
import com.wosplayer.app.BackRunner;
import com.wosplayer.app.DisPlayInterface;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayHandler;
import com.wosplayer.app.SystemConfig;
import com.wosplayer.tool.SdCardTools;

import java.util.ArrayList;

import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by 79306 on 2017/2/20.
 */

public class AppToolsFragment extends Fragment implements DisPlayInterface.onFragAction,
        View.OnClickListener,AdapterView.OnItemSelectedListener,RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "APP配置页面";
    public static final String FLAG ="wostools";
    private DisplayActivity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.activity = (DisplayActivity)activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private TextView localmac;
    private EditText serverip;
    private EditText serverport;
    private EditText companyid;
    private EditText terminalNo;
    private EditText basePath;
    private EditText heartbeattime;
    private EditText storageLimits;
    private EditText restartBeatInterval;
    private Button btnGetID;
    private Button btnSaveData;
    private Spinner playtype;
    private SpnnerAdpter adapter;
    private SystemConfig dataList;

    private Spinner storeSwitch;
    private SpnnerAdpter adapter2;

    //单选
    private RadioGroup cap_save;
    private RadioGroup cap_notify;

    //本机信息
    private TextView localip;
    private Button sure;
    private EditText command;
    private TextView version;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initView(inflater);
    }

    private View initView(LayoutInflater inflater) {
        ViewGroup vp = (ViewGroup) inflater.inflate(R.layout.activity_wostools,null);
        localmac =(TextView)vp.findViewById(R.id.localmac);
        serverip=(EditText)vp.findViewById(R.id.serverip);
        serverip.clearFocus();
        serverport=(EditText)vp.findViewById(R.id.serverport);
        serverport.clearFocus();
        companyid=(EditText)vp.findViewById(R.id.companyid);
        companyid.clearFocus();
        terminalNo=(EditText)vp.findViewById(R.id.terminalNo);
        terminalNo.clearFocus();
        basePath =(EditText)vp.findViewById(R.id.BasePath);
        basePath.clearFocus();
        heartbeattime=(EditText)vp.findViewById(R.id.HeartBeatInterval);
        heartbeattime.clearFocus();
        storageLimits =(EditText)vp.findViewById(R.id.StorageLimits);
        storageLimits.clearFocus();
        restartBeatInterval=(EditText)vp.findViewById(R.id.RestartBeatInterval);
        restartBeatInterval.clearFocus();
        btnGetID = (Button) vp.findViewById(R.id.btnGetID);
        btnGetID.setOnClickListener(this);
        btnSaveData = (Button) vp.findViewById(R.id.btnSaveData);
        btnSaveData.setOnClickListener(this);
        playtype = (Spinner) vp.findViewById(R.id.playtype);
        adapter = new SpnnerAdpter(activity);
        playtype.setAdapter(adapter);
        adapter.settingData(SystemConfig.playMode);//模式选择
        playtype.setOnItemSelectedListener(this);
        storeSwitch = (Spinner) vp.findViewById(R.id.store_switch);
        adapter2 = new SpnnerAdpter(activity);
        storeSwitch.setAdapter(adapter2);
        adapter2.settingDataList(SdCardTools.getAllStorePath(activity));//文件保存路径选择
        storeSwitch.setOnItemSelectedListener(this);
        localip = (TextView) vp.findViewById(R.id.localip);
        command = (EditText) vp.findViewById(R.id.command_input);
        version = (TextView)vp.findViewById(R.id.version);
        sure = (Button) vp.findViewById(R.id.sure);
        sure.setOnClickListener(this);
        //截屏设置
        cap_save = (RadioGroup) vp.findViewById(R.id.cap_save);
        cap_save.setOnCheckedChangeListener(this);
        cap_notify = (RadioGroup) vp.findViewById(R.id.cap_notify);
        cap_notify.setOnCheckedChangeListener(this);
        return vp;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        InitConfig();
        InitValue();
    }

    @Override
    public void onPause() {
        super.onPause();
        //结束线程
        RequstTerminal.EndRequst();
    }

    /**
     * 加载配置
     */
    public  void InitConfig()
    {
        //检测sd卡
        SdCardTools.checkSdCard(activity);
        Logs.i(TAG, "-------------------开始加载本机数据--------------------------------");
        dataList= SystemConfig.get().read();
        DisplayMetrics m_dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(m_dm);
        //获取手机分辨率
        dataList.put("width",Integer.toString(m_dm.widthPixels));
        dataList.put("height",Integer.toString(m_dm.heightPixels));
        //获取本机地址
        dataList.put("tip", AppUtils.getLocalIpAddress());
        //获取本机机器码
        dataList.put("mac", AppUtils.GetMac(activity));
    }

    /**
     *  加载数据
     */
    public void InitValue()
    {
        try
        {

            localip.setText(dataList.GetStringDefualt("tip","127.0.0.1"));
            localmac.setText(dataList.GetStringDefualt("mac", "00-00-00-00-00"));//本机mac地址
            serverip.setText(dataList.GetStringDefualt("serverip", "127.0.0.1"));
            serverport.setText(dataList.GetStringDefualt("serverport", "8000"));
            companyid.setText(dataList.GetStringDefualt("companyid", "999"));
            terminalNo.setText(dataList.GetStringDefualt("terminalNo", ""));
            heartbeattime.setText(dataList.GetStringDefualt("HeartBeatInterval", "30"));
            basePath.setText(catPathfile(dataList.GetStringDefualt("basepath", "")));//资源文件夹
            storageLimits.setText(dataList.GetStringDefualt("storageLimits","50"));
            restartBeatInterval.setText(dataList.GetStringDefualt("RestartBeatInterval","30"));
            version.setText("VERSION["+ AppUtils.getAppVersionCode(activity)+"]");//版本
            cap_save.check((dataList.GetIntDefualt("CaptureSave",0)==0)?R.id.cap_save_y:R.id.cap_save_n);
            cap_notify.check((dataList.GetIntDefualt("CaptureNoty",0)==0)?R.id.cap_notify_y:R.id.cap_notify_n);
        }catch(Exception e)
        {
            Log.e("ToolsActivity ", e.getMessage());
        }
    }
    private String catPathfile(String path){
        if (path.contains("/")){
            path = path.substring(0,path.lastIndexOf("/"));
            path = path.substring(path.lastIndexOf("/")+1);
        }
        return path.equals("")?"playlist":path;
    }

    /**
     * 获取控件传入的数据并封装
     */
    public void GetViewValue()
    {
        ArrayList<String> pathList = new ArrayList<>();
        String serverId = serverip.getText().toString();
        String serverPort = serverport.getText().toString();
        String CaptureURL = String.format("http://%s:%s/wos/captureManagerServlet", serverId, serverPort);
        dataList.put("terminalNo",terminalNo.getText().toString());//终端id
        dataList.put("serverip", serverId);//服务器ip
        dataList.put("serverport",serverPort);//终端端口
        dataList.put("CaptureURL", CaptureURL);//截图上传url
        dataList.put("companyid",  companyid.getText().toString());//公司id
        dataList.put("HeartBeatInterval",  heartbeattime.getText().toString());//心跳
        dataList.put("RestartBeatInterval",restartBeatInterval.getText().toString()); //重启时间
        dataList.put("storageLimits", storageLimits.getText().toString());//sdcard 清理阔值
        String basepath= basePath.getText().toString();//资源存储的文件名
//      例: xxx前缀 /basepath/资源1
        if (!basepath.startsWith("/")){
            basepath = "/"+basepath;
        }
        if(!basepath.endsWith("/"))
        {
            basepath=basepath+"/";
        }
        basepath = SdCardTools.getAppSourceDir(activity)+basepath;
        dataList.put("basepath",  basepath);
        pathList.add(basepath);
        //建设银行接口资源下载位置
        basepath = SdCardTools.getAppSourceDir(activity)+SdCardTools.Construction_Bank_dir_source;
        dataList.put("bankPathSource",basepath);
        pathList.add(basepath);
        basepath = SdCardTools.getAppSourceDir(activity)+SdCardTools.Construction_Bank_dir_xmlfile;
        dataList.put("bankPathXml",basepath);
        pathList.add(basepath);
        final ArrayList<String> mlist = pathList;
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (String path:mlist){
                    Log.d(TAG, "后台进程: 创建文件夹 "+path +" 结果："+FileUtils.makeDirs(path));
                }
            }
        };
        BackRunner.runBackground(runnable);
        dataList.save();//保存当前数据
    }
    /**
     * 保存 :1 获取值 2.创建文件路径 3.判断模式
     */
    public void save(){
        GetViewValue();
        String playmode = dataList.GetStringDefualt("playMode","");
        if (playmode.equals(SystemConfig.playMode[0])){ //网络模式

            if (!"".equals(terminalNo.getText().toString())){
                AppUtils.settingServerInfo(activity,true);
                activity.mHandler.sendEmptyMessage(PlayHandler.HandleEvent.close.ordinal());
            }else{
                AppUtils.Toals(activity,"配置信息不可用，请联系客服.");
            }
        }
        if (playmode.equals(SystemConfig.playMode[1])){
            //单机版
            //选择分屏显示
            //启动
            AppUtils.settingServerInfo(activity,true);
            activity.mHandler.sendEmptyMessage(PlayHandler.HandleEvent.close.ordinal());
        }

    }
    public void setcompanyid(String value)
    {
        try
        {
            terminalNo.setText(value);
            terminalNo.setFocusable(true);
            AppUtils.Toals(activity,"申请终端ID成功");
        }catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }


    @Override
    public void sendMessage(Object obj) {
        setcompanyid(obj.toString());
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.btnGetID){ //获取id
            if(dataList!=null){
                //把数据封装到集合中
                GetViewValue();
                InitValue();//刷新
                //开启线程
                RequstTerminal.BeginRequst(dataList,activity.mHandler);
            }
        }
        if (vid == R.id.btnSaveData){ //保存数据
            save();
        }
        if (vid == R.id.sure){
            final String cmd = command.getText().toString();
            if (cmd == null || cmd.equals("")) {
                AppUtils.Toals(activity,"请输入终端命令");
            }
            BackRunner.runBackground(new Runnable() {
                @Override
                public void run() {
                    //分解字符串
                    excute(cmd);
                }
            });
        }
    }
    public  void NotityActivty(String msg)
    {
        if(activity!=null)
        {
            AppUtils.NotifyHandle(activity.mHandler, PlayHandler.HandleEvent.outtext.ordinal(),msg);
        }
    }
    private void excute(String cmd) {
        //分解字符串
        String[] cmds = cmd.split("\\s+");
            String command = null;
            String option = null;
            String param = null;
            try {
                command = cmds[0];
                option = cmds[1];
                if (!command.equals("wos")) throw new IllegalStateException("error command");
            } catch (Exception e) {
                NotityActivty("无效命令或无效选项");
                return;
            }
            try{
               param = cmds[2];
            }catch (Exception e){}

            if (option.equals("screen")){
                    if (null != param && param.equals("draw")){
                        //设置绘制方案截屏
                        dataList.put("CaptureMode","1");
                    }else{
                        dataList.put("CaptureMode","0");
                    }
                NotityActivty("执行成功");
            }else
            if(option.equals("simt")){
                if (null != param){

                    try {
                        int time = Integer.parseInt(param);
                        if (time<5) throw new NumberFormatException("error time");
                        //单机播放器图片时间设置
                        dataList.put("SingImageTime",param);
                        NotityActivty("执行成功,单机版本播放器图片播放时间:"+time);
                    } catch (NumberFormatException e) {
                        NotityActivty("执行失败,参数错误 : "+param);
                    }
                }
            }
            else
            if(option.equals("close")){
                if (null != param && param.equals("1")){
                    //设置绘制方案截屏
                    dataList.put("AccessClose","1");//1 打开监听
                }else{
                    dataList.put("AccessClose","0");//0 关闭监听
                }
                NotityActivty("执行成功");
            }else
            {
                String result = AdbCommand.inputCommand(option,param);
                if (result.equals("")) {
                    NotityActivty("没有可行操作匹配命令或选项");
                }
                else{
                    //执行
                    ShellUtils.CommandResult rt = ShellUtils.execCommand(result,true);
                    if (rt.result == 0){
                        NotityActivty("执行成功");
                    }else{
                        NotityActivty("执行失败");
                    }
                }
            }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //选择完成
        view.setBackgroundColor(Color.WHITE);
        int vid = parent.getId();
        if (vid == playtype.getId()){
            dataList.put("playMode",adapter.getDataOnIndex(position));//关于模式选择
        }
        if (vid == storeSwitch.getId()){
            if (SdCardTools.testDirc(adapter2.getDataOnIndex(position),true)){
                AppUtils.Toals(activity,"设置文件存贮主目录:"+SdCardTools.getAppSourceDir(activity));
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    //单选框使用
    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (group.getId() == cap_save.getId()){
            if (checkedId == R.id.cap_save_y){
                dataList.put("CaptureSave","0"); //截屏保存
            }else if (checkedId == R.id.cap_save_n){
                dataList.put("CaptureSave","1");
            }
        }
        if (group.getId() == cap_notify.getId()){
            if (checkedId == R.id.cap_notify_y){
                dataList.put("CaptureNoty","0"); //截屏通知
            }else if (checkedId == R.id.cap_notify_n){
                dataList.put("CaptureNoty","1");
            }
        }
    }

}
