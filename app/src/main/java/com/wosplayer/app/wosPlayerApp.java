package com.wosplayer.app;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.wosplayer.service.CommunicationService;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

import installUtils.ApkController;

/**
 * Created by Administrator on 2016/7/19.
 */

public class wosPlayerApp extends Application {


    public static DataList config = new DataList();
    public static Context appContext ;
    @Override
    public void onCreate() {
        super.onCreate();
        //查看 老版本app 是否存在 存在 卸载
        new Thread(new Runnable() {
            @Override
            public void run() {
                ApkController.uninstall("com.wos",getApplicationContext());
            }
        });

       // CrashHandler.getInstance().init(getApplicationContext());
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

    public static void startCommunicationService() {

        Intent intent = new Intent(appContext, CommunicationService.class);
        //传递参数
        Bundle b = new Bundle();
        b.putString("ip",config.GetStringDefualt("serverip","127.0.0.1"));
        b.putInt("port",6666);
        b.putString("terminalNo",config.GetStringDefualt("terminalNo","127.0.0.1"));
        b.putLong("HeartBeatTime",(config.GetIntDefualt("HeartBeatInterval",50) * 500));
        intent.putExtras(b);
        appContext.startService(intent);
        log.i("尝试开启通讯服务...");
    }

    /**
     * 停止服务
     */
    public static void stopCommunicationService(){
        Intent server = new Intent(appContext, CommunicationService.class);
        appContext.stopService(server);
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
