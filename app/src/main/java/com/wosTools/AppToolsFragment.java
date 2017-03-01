package com.wosTools;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wosplayer.R;
import com.wosplayer.app.AdbCommand;
import com.wosplayer.app.AppTools;
import com.wosplayer.app.BackRunner;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.app.SystemConfig;
import com.wosplayer.command.operation.other.Command_UPDC;
import com.wosplayer.tool.SdCardTools;

import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by 79306 on 2017/2/20.
 */

public class AppToolsFragment extends Fragment implements DisplayActivity.onFragAction,View.OnClickListener{
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

    private TextView storeDir;
    private EditText serverip;
    private EditText serverport;
    private EditText companyid;
    private EditText terminalNo;
    private EditText BasePath;
    private EditText heartbeattime;
    private EditText StorageLimits;
    private EditText RestartBeatInterval;
    private Button btnGetID;
    private Button btnSaveData;
    private Spinner playtype;
    private SpnnerAdpter adapter;
    private SystemConfig dataList;

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
        storeDir=(TextView)vp.findViewById(R.id.storedir);
        serverip=(EditText)vp.findViewById(R.id.serverip);
        serverport=(EditText)vp.findViewById(R.id.serverport);
        companyid=(EditText)vp.findViewById(R.id.companyid);
        terminalNo=(EditText)vp.findViewById(R.id.terminalNo);
        BasePath=(EditText)vp.findViewById(R.id.BasePath);
        heartbeattime=(EditText)vp.findViewById(R.id.HeartBeatInterval);
        StorageLimits=(EditText)vp.findViewById(R.id.StorageLimits);
        RestartBeatInterval=(EditText)vp.findViewById(R.id.RestartBeatInterval);
        btnGetID = (Button) vp.findViewById(R.id.btnGetID);
        btnGetID.setOnClickListener(this);
        btnSaveData = (Button) vp.findViewById(R.id.btnSaveData);
        btnSaveData.setOnClickListener(this);
        playtype = (Spinner) vp.findViewById(R.id.playtype);
        adapter = new SpnnerAdpter(activity);
        playtype.setAdapter(adapter);
        playtype.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //选择完成
                Logs.d(TAG,"选择模式 - "+adapter.getDataOnIndex(position));
                view.setBackgroundColor(Color.WHITE);
                dataList.put("playMode",adapter.getDataOnIndex(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        localip = (TextView) vp.findViewById(R.id.localip);
        command = (EditText) vp.findViewById(R.id.command_input);
        version = (TextView)vp.findViewById(R.id.version);
        sure = (Button) vp.findViewById(R.id.sure);
        sure.setOnClickListener(this);
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
        dataList.put("tip",AppTools.getLocalIpAddress());
        //获取本机机器码
        dataList.put("mac",AppTools.GetMac(activity));

    }

    /**
     *  加载数据
     */
    public void InitValue()
    {
        try
        {
            storeDir.setText(SdCardTools.getAppSourceDir(activity));
            serverip.setText(dataList.GetStringDefualt("serverip", "127.0.0.1"));
            serverport.setText(dataList.GetStringDefualt("serverport", "8000"));
            companyid.setText(dataList.GetStringDefualt("companyid", "999"));
            terminalNo.setText(dataList.GetStringDefualt("terminalNo", ""));
            heartbeattime.setText(dataList.GetStringDefualt("HeartBeatInterval", "30"));
            BasePath.setText(catPathfile(dataList.GetStringDefualt("basepath", "")));//资源地址
            StorageLimits.setText(dataList.GetStringDefualt("storageLimits","50"));
            RestartBeatInterval.setText(dataList.GetStringDefualt("RestartBeatInterval","30"));
            //焦点默认在这个控件上
            serverip.setFocusable(true);

            localip.setText(dataList.GetStringDefualt("tip","127.0.0.1"));
            version.setText(AppTools.getAppVersion(activity)+"");

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
        String CaptureURL = String.format("http://%s:%s/wos/captureManagerServlet",
                serverId,
                serverPort);

        dataList.put("terminalNo",terminalNo.getText().toString());//终端id
        dataList.put("serverip", serverId);//服务器ip
        dataList.put("serverport",serverPort);//终端端口

        dataList.put("CaptureURL", CaptureURL);//截图上传url
        dataList.put("companyid",  companyid.getText().toString());//公司id
        dataList.put("HeartBeatInterval",  heartbeattime.getText().toString());//心跳
        dataList.put("RestartBeatInterval",RestartBeatInterval.getText().toString()); //重启时间
        dataList.put("storageLimits",StorageLimits.getText().toString());//sdcard 清理阔值
        String basepath=BasePath.getText().toString();//资源存储的文件名
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
//        dataList.put("CAPTIMAGEPATH", basepath + "screen.png");//截图本地存放位置
//        dataList.put("defaultVideo", basepath + "default.mp4");//默认视频本地位置
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
    }
    /**
     * 保存 - 获取值 - 创建所有文件路径
     */
    public void save(){
        GetViewValue();
        dataList.save();
        if (!"".equals(terminalNo.getText().toString())){
            AppTools.settingServerInfo(activity,true);
            activity.mHandler.sendEmptyMessage(DisplayActivity.HandleEvent.close.ordinal());
        }else{
            AppTools.Toals(activity,"配置信息不可用，请联系客服");
        }
    }
    public void setcompanyid(String value)
    {
        try
        {
            terminalNo.setText(value);
            terminalNo.setFocusable(true);
            AppTools.Toals(activity,"申请终端ID成功");
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
                AppTools.Toals(activity,"请输入终端命令");
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
            AppTools.NotifyHandle(activity.mHandler, DisplayActivity.HandleEvent.outtext.ordinal(),msg);
        }
    }
    private void excute(String cmd) {
        //分解字符串
        String[] cmds = cmd.split("\\s+");
        if (cmds[0].equals("tcmd")){
            String option = null;
            String param = null;
            try {
                option = cmds[1];
            } catch (Exception e) {
                NotityActivty("无效选项");
            }
            try {
                param = cmds[2];
            } catch (Exception e) {
            }

            String result = AdbCommand.inputCommand(option,param);
            if (result.equals("")) {
                NotityActivty("不匹配命令或选项");
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
        }else{
            NotityActivty("无效命令");
        }
    }
}
