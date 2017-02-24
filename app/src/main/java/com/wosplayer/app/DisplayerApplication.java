package com.wosplayer.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;

import com.wos.play.rootdir.model_monitor.soexcute.RunJniHelper;
import com.wosplayer.tool.SdCardTools;
import com.wosplayer.service.CommunicationService;

import com.wosTools.ToolsUtils;

import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Administrator on 2016/7/19.
 */

public class DisplayerApplication extends Application {








    public static Context appContext ;
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this.getApplicationContext();
        //系统配置监听值
        SystemConfig.get().put("watchValue","0").save();
        //捕获异常
        //CrashHandler.getInstance().init(appContext);
    }

    public void startAppInit(Handler handler){

        //放入系统目录
        new AdbCommand(appContext,handler,true,true).start();//会开端口,会重启
        //new AdbShellCommd(this.getApplicationContext(),true,false).start();//开端口,不重启
        //new AdbShellCommd(this.getApplicationContext(),false,true).start();//不开远程端口.会重启
//       new AdbShellCommd(this.getApplicationContext(),false,false).start();//不开远程端口,不重启
        //初始化 配置信息
        //init(false);
    }




    //本机信息
    public static DataList config = null;
    /**
     *
     */
    public void initConfig() {
      config = SystemConfig.get().read();;
      final String defaultPath = config.GetStringDefualt("default","");
        final String fudianpath = config.GetStringDefualt("fudianpath","");
        if (defaultPath.isEmpty()||fudianpath.isEmpty()) return;
      BackRunner.runBackground(new Runnable() {
          @Override
          public void run() {
              //将默认排期放入指定文件夹下
              AppTools.defaultProgram(appContext,defaultPath);
              Logs.i("后台任务","默认排期解压缩完成");
              //将默认图片或者视频放入指定 文件夹下
             // Logs.i("后台任务","默认资源放入指定目录下 - "+resourcePath+"default.mp4 成功");
               AppTools.fudianBankSource(appContext,fudianpath);
              Logs.i("后台任务","富颠金融网页模板解压缩完成");
          }
      });
        config.printData();
    }


    //获取配置信息值
    public static String getConfigValue(String key){
        if (config!=null){
            return config.GetStringDefualt(key,"");
        }
        return "";
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
            if (mc==null){
                mc = appContext;
            }
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