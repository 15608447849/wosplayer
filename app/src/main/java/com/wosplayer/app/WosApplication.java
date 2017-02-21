package com.wosplayer.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.wosplayer.tool.SdCardTools;
import com.wosplayer.service.CommunicationService;

import com.wosTools.ToolsUtils;

/**
 * Created by Administrator on 2016/7/19.
 */

public class WosApplication extends Application {


    public static DataList config = new DataList();
    public static Context appContext ;
    @Override
    public void onCreate() {
        super.onCreate();
        Logs.e("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~wosPlayer app start~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        appContext = WosApplication.this.getApplicationContext();
        //捕获异常
        CrashHandler.getInstance().init(getApplicationContext());
    }

    public void startAppInit(Handler handler){
        //放入系统目录
        new AdbShellCommd(this.getApplicationContext(),handler,true,true).start();//会开端口,会重启
        //new AdbShellCommd(this.getApplicationContext(),true,false).start();//开端口,不重启
        //new AdbShellCommd(this.getApplicationContext(),false,true).start();//不开远程端口.会重启
//       new AdbShellCommd(this.getApplicationContext(),false,false).start();//不开远程端口,不重启
        //初始化 配置信息
        //init(false);

    }
    /**
     *   false 读取 com.wos.tools 下面的信息
     *   true 读取自己app下面的初始化信息
     */
    public void initConfig() {

        //终端id
        config.put("terminalNo", GetKey("terminalNo", ""));
        //通信模式
        config.put("connectionType", GetKey("connectionType", "socket"));
        //通信端口
        config.put("keyText", GetKey("keyText", "10000"));
        //服务端地址
        config.put("serverip", GetKey("serverip", "127.0.0.1"));
        //访问服务端端口
        config.put("serverport", GetKey("serverport", "6666"));
        //公司id
        config.put("companyid", GetKey("companyid", "999"));
        //心跳时间
        config.put("HeartBeatInterval", GetKey("HeartBeatInterval", "30"));
        //重启时间
        config.put("RestartBeatInterval", GetKey("RestartBeatInterval", "30"));//
        //sdcard 清理阔值
        config.put("storageLimits",GetKey("storageLimits","50"));
        //机器码
        config.put("mac",AppTools.GetMac(appContext));
        //本地ip
        String LocalIpAddress = AppTools.getLocalIpAddress();
        config.put("tip", (LocalIpAddress == "") ? "127.0.0.1"
                : LocalIpAddress);

        //访问服务端的URL
        String CaptureURL = String.format(
                "http://%s:%s/wos/captureManagerServlet",
                GetKey("serverip", "192.168.6.66"),
                GetKey("serverport", "8000"));
        config.put("CaptureURL", CaptureURL);
        //本地资源存储目录
        String basepath = GetKey("basepath", "mnt/sdcard/sourceList");
        config.put("basepath", basepath);
        //创建一个目录用于存储资源
        SdCardTools.MkDir(basepath);
        //将 默认图片 或者 视频 放入 指定 文件夹下
        if(AppTools.defautSource(appContext,basepath,"default.mp4")){
            config.put("defaultVideo", config.GetStringDefualt("basepath","") + "default.mp4");//默认视频本地位置
        }
        config.put("CAPTIMAGEPATH", config.GetStringDefualt("basepath","") + "screen.png");//截图本地位置
        //建设银行接口资源下载位置
        basepath =  GetKey("bankPathSource", "mnt/sdcard"+SdCardTools.Construction_Bank_dir_source);
        config.put("bankPathSource",basepath);
        SdCardTools.MkDir(basepath);
        basepath = GetKey("bankPathXml", "mnt/sdcard"+SdCardTools.Construction_Bank_dir_xmlfile);
        config.put("bankPathXml",basepath);
        SdCardTools.MkDir(basepath);

        Logs.i("--- app config init complete ---" );
//        config.printData();

    }
    //后台保存默认节目
    public void initDefaultSource() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //将默认排期 放入 指定文件夹下
                AppTools.defaultProgram(appContext);
            }
        }).start();
    }

    //获取配置信息值
    public static String getConfigValue(String key){
        if (config!=null){
            return config.GetStringDefualt(key,"");
        }
        return "";
    }


    //获取配置信息共享xml文件值，带默认值
    public static String GetKey(String key, String defualtValue) {
        String value = ToolsUtils.readShareData(appContext,key);
        return (value != "") ? value : defualtValue;
    }

    /**
     * 开启通讯服务
     *ip =  intent.getExtras().getString("ip");
     port = intent.getExtras().getInt("port");
     terminalNo = intent.getExtras().getString("terminalNo");
     HeartBeatTime = intent.getExtras().getLong("HeartBeatTime");
     */
    public static void startCommunicationService(Context mc) {
        try {
            Intent intent = new Intent(mc, CommunicationService.class);
            //传递参数
            Bundle b = new Bundle();
            b.putString("ip",config.GetStringDefualt("serverip","127.0.0.1"));
            b.putInt("port",6666);
            b.putString("terminalNo",config.GetStringDefualt("terminalNo","127.0.0.1"));
            b.putLong("HeartBeatTime",(config.GetIntDefualt("HeartBeatInterval",10)));
            intent.putExtras(b);
            Logs.i("wosPlayerApp: 尝试开启通讯服务...");
            mc.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 停止通讯服务
     */
    public static void stopCommunicationService(Context mc){
        try {
            Intent server = new Intent(mc, CommunicationService.class);
            mc.stopService(server);
        } catch (Exception e) {
            Logs.e("停止通讯服务Err:" +e.getMessage());
        }
    }
    //发送消息到通讯服务
    public static void sendMsgToServer(String msg){
        if (msg=="") return;
        //发送一个广播
        Intent intent = new Intent();
        intent.setAction(CommunicationService.CommunicationServiceReceiveNotification.action);
        intent.putExtra(CommunicationService.CommunicationServiceReceiveNotification.key,msg);
        appContext.sendBroadcast(intent);
    }
}
























////查看 老版本app 是否存在 存在 卸载
//new Thread(new Runnable() {
//@Override
//public void run() {
//
//        //预留远程端口号
//        String [] commands = {
//        "su\n",
//        "setprop service.adb.tcp.port 9999\n",
//        "stop adbd\n",
//        "start adbd\n"
//        };
//        ShellUtils.CommandResult cr = ShellUtils.execCommand(commands,true,true);
//
//        String strs = "远程端口开启结果: "+cr.result;
//        log.d("Remote",strs);
//        Toals.Say(strs);
//        strs = "本地ip: "+ getLocalIpAddress();
//        log.d("Remote",strs);
//
//        //卸载 旧app
//        ApkController.uninstall("com.wos",getApplicationContext());
//              /* int i = PackageUtils.uninstall(getApplicationContext(),"com.wos");
//                if (i==PackageUtils.DELETE_SUCCEEDED){
//                    Toals.Say("-- 卸载 com.wos success --");
//                }*/
//
//        //放入system
//        String packagepath = getApplicationInfo().sourceDir;
//        log.d("root",packagepath);
//        String paramString=// "adb push MySMS.apk /system/app" +"\n"+
//        "adb shell" +"\n"+
//        "su" +"\n"+
//        // "mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system" +"\n"+
//        "mount -o remount,rw /system" +"\n"+
//        "cp "+packagepath+" /system/app/wosplayer.apk" +"\n"+
//        //"mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system" +"\n"+
//        "mount -o remount,ro /system" +"\n"+
//        "reboot"+"\n"+
//        "exit" +"\n"+
//        "exit";
//
//        if(AppToSystem.haveRoot()){
//        if (packagepath.contains("/data/app")){
//        log.e("root","# "+paramString);
//        int res = -1;//AppToSystem.execRootCmdSilent(paramString);
//
//        if(res==-1){
//        log.e("root","安装不成功");
//        }else{
//        log.e("root","安装成功");
//        }
//        }else{
//        log.e("root","system/app 已经存在");
//        }
//
//        }else{
//        log.e("root","没有root权限");
//        }
//
//        }
//        }).start();