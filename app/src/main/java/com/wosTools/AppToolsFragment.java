package com.wosTools;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wosplayer.R;
import com.wosplayer.app.AppTools;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.tool.SdCardTools;

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
    public DisplayMetrics m_dm;
    public DataListEntiy dataList;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initView(inflater);
    }

    private View initView(LayoutInflater inflater) {
        ViewGroup vp = (ViewGroup) inflater.inflate(R.layout.activity_wostools,null);
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
        return vp;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        InitConfig();
        InitValue();
    }

    /**
     * 加载配置
     */
    public  void InitConfig()
    {
        Logs.i(TAG, "开始加载数据");
        m_dm = new DisplayMetrics();
        //获取手机分辨率
        activity.getWindowManager().getDefaultDisplay().getMetrics(m_dm);
        //检测sd卡
        SdCardTools.checkSdCard(activity);
        dataList=new DataListEntiy();
        dataList.ReadShareData();
        RequstTerminal.setHandler(activity.mHandler);
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
            BasePath.setText(catPathfile(dataList.GetStringDefualt("basepath", "")));
            StorageLimits.setText(dataList.GetStringDefualt("storageLimits","50"));
            RestartBeatInterval.setText(dataList.GetStringDefualt("RestartBeatInterval","30"));
            //焦点默认在这个控件上
            serverip.setFocusable(true);

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
     * 保存
     */
    public void save(){
        GetViewValue();
        dataList.SaveShareData();
        if (!"".equals(terminalNo.getText().toString())){
            AppTools.settingServerInfo(activity,true);
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
        dataList.put("RestartBeatInterval",RestartBeatInterval.getText().toString()); //重启时间
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

        basepath = SdCardTools.getAppSourceDir(activity)+basepath;
        dataList.put("basepath",  basepath);

        //建设银行接口资源下载位置
        basepath = SdCardTools.getAppSourceDir(activity)+SdCardTools.Construction_Bank_dir_source;
        dataList.put("bankPathSource",basepath);

        basepath = SdCardTools.getAppSourceDir(activity)+SdCardTools.Construction_Bank_dir_xmlfile;
        dataList.put("bankPathXml",basepath);
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
        if (vid == R.id.btnGetID){
            if(dataList!=null && m_dm!=null){
                //结束线程
                RequstTerminal.EndRequst();
                //把数据封装到集合中
                GetViewValue();
                //开启线程
                RequstTerminal.BeginRequst(dataList,m_dm);
            }
        }
        if (vid == R.id.btnSaveData){ //保存数据
            save();
            activity.mHandler.sendEmptyMessage(DisplayActivity.HandleEvent.close.ordinal());
        }
    }


}
