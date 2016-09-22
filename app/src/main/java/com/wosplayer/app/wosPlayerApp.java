package com.wosplayer.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.wos.Toals;
import com.wosplayer.R;
import com.wosplayer.service.CommunicationService;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

import cn.trinea.android.common.util.ShellUtils;
import installUtils.ApkController;
import installUtils.AppToSystem;

/**
 * Created by Administrator on 2016/7/19.
 */

public class wosPlayerApp extends Application {


    public static DataList config = new DataList();
    public static Context appContext ;
    @Override
    public void onCreate() {
        super.onCreate();
        log.d("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~wosPlayer app start~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        //查看 老版本app 是否存在 存在 卸载
        new Thread(new Runnable() {
            @Override
            public void run() {
                ApkController.uninstall("com.wos",getApplicationContext());
              /* int i = PackageUtils.uninstall(getApplicationContext(),"com.wos");
                if (i==PackageUtils.DELETE_SUCCEEDED){
                    Toals.Say("-- 卸载 com.wos success --");
                }*/

                //放入system
                String packagepath = getApplicationInfo().sourceDir;
                    String paramString=// "adb push MySMS.apk /system/app" +"\n"+
                            "adb shell" +"\n"+
                                    "su" +"\n"+
                                    // "mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system" +"\n"+
                                    "mount -o remount,rw /system" +"\n"+
                                    "cp "+packagepath+" /system/app/wosplayer.apk" +"\n"+
                                    //"mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system" +"\n"+
                                    "mount -o remount,ro /system" +"\n"+
                                    "reboot"+"\n"+
                        "exit" +"\n"+
                        "exit";

                    log.e("root","# "+paramString);

                    if(AppToSystem.haveRoot()){
                        if (packagepath.contains("/data/app")){
                        if(AppToSystem.execRootCmdSilent(paramString)==-1){

                            log.e("root","安装不成功");
                        }else{

                            log.e("root","安装成功");
                        }
                    }else{
                        log.e("root","没有root权限");
                    }

                }else{
                    //创建桌面图标

                    Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
                    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
                    // 是否可以有多个快捷方式的副本，参数如果是true就可以生成多个快捷方式，如果是false就不会重复添加
                    intent.putExtra("duplicate", false);

                    Intent intent2 = new Intent(Intent.ACTION_MAIN);
                    intent2.addCategory(Intent.CATEGORY_LAUNCHER);
                    // 删除的应用程序的ComponentName，即应用程序包名+activity的名字
                    intent2.setComponent(new ComponentName(wosPlayerApp.this.getPackageName(), wosPlayerApp.this.getPackageName() + ".DisplayActivity"));
                    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent2);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(wosPlayerApp.this,
                            R.drawable.ic_launcher));
                    sendBroadcast(intent);
                }


                //预留远程端口号
                String [] commands = {
                        "su\n",
                        "setprop service.adb.tcp.port 9999\n",
                        "stop adbd\n",
                        "start adbd\n"
                };
                ShellUtils.CommandResult cr = ShellUtils.execCommand(commands,true,true);

                String strs = "远程端口开启结果: "+cr.result;
                log.e(strs);
                Toals.Say(strs);
                strs = "本地ip: "+ getLocalIpAddress();
                log.d(strs);





            }
        }).start();



        CrashHandler.getInstance().init(getApplicationContext());
        //检测sd卡

        //初始化 配置信息
        init();
    }




    /**
     *
     */
    private void init() {
        appContext = wosPlayerApp.this.getApplicationContext();
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
        config.put("sleepTime", GetKey("sleepTime", "30"));
        //本地资源存储目录
        String basepath = GetKey("basepath", "/wos/wos/source/");
        if (!basepath.endsWith("/")) {
            basepath = basepath + "/";
        }
        basepath = basepath + "playlist/";
        // 创建一个目录用于存储资源
        MkDir(basepath);
        config.put("basepath", basepath);
        //机器码
        config.put("mac", GetMac());
        //本地ip
        String LocalIpAddress = getLocalIpAddress();
        config.put("tip", (LocalIpAddress == "") ? "127.0.0.1"
                : LocalIpAddress);
        config.put("CAPTIMAGEPATH", config.GetStringDefualt("basepath","") + "screen.png");//截图本地位置
        //访问服务端的URL
        String CaptureURL = String.format(
                "http://%s:%s/wos/captureManagerServlet",
                GetKey("serverip", "192.168.1.60"),
                GetKey("serverport", "80"));
        config.put("CaptureURL", CaptureURL);
    }

    public static String GetKey(String key, String defualtValue) {
        String value = appTools.readShareDataTools(appContext,key).trim();
        return (value != "") ? value : defualtValue;
    }
    /**
     * 创建文件
     *
     * @param pathdir
     */
    public static void MkDir(String pathdir) {
        try {
            File file = new File(pathdir);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            Log.i("MkDir", e.getMessage());
        }
    }

    /**
     * 生成机器码
     *
     * @return
     */
    public static String GetMac() {
        final TelephonyManager tm = (TelephonyManager) ((ContextWrapper) appContext)
                .getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, tmPhone, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = ""
                + android.provider.Settings.Secure.getString(
                appContext.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(),
                ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String uniqueId = deviceUuid.toString();
        return uniqueId;
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && (inetAddress instanceof Inet4Address)) {
                        log.i("local ip: "+ inetAddress.getHostAddress().toString());
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
			log.e("WifiPreference IpAddress", ex.toString());
        }
        return "";
    }


    /**
     *   ip =  intent.getExtras().getString("ip");
     port = intent.getExtras().getInt("port");
     terminalNo = intent.getExtras().getString("terminalNo");
     HeartBeatTime = intent.getExtras().getLong("HeartBeatTime");
     */

    public static void startCommunicationService(Context mc) {

        Intent intent = new Intent(mc, CommunicationService.class);
        //传递参数
        Bundle b = new Bundle();
        b.putString("ip",config.GetStringDefualt("serverip","127.0.0.1"));
        b.putInt("port",6666);
        b.putString("terminalNo",config.GetStringDefualt("terminalNo","127.0.0.1"));
        b.putLong("HeartBeatTime",(config.GetIntDefualt("HeartBeatInterval",50) * 500));
        intent.putExtras(b);
        log.i("wosPlayerApp: 尝试开启通讯服务...");
        mc.startService(intent);

    }

    /**
     * 停止服务
     */
    public static void stopCommunicationService(Context mc){
        Intent server = new Intent(mc, CommunicationService.class);
        mc.stopService(server);
    }


    public static void sendMsgToServer(String msg){
        if (msg=="") return;

        //发送一个广播
        Intent intent = new Intent();
        intent.setAction(CommunicationService.CommunicationServiceReceiveNotification.action);
        intent.putExtra(CommunicationService.CommunicationServiceReceiveNotification.key,msg);
        appContext.sendBroadcast(intent);
//        log.i("发送出去一个广播");

    }


}
