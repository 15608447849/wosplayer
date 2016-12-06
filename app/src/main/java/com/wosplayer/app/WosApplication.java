package com.wosplayer.app;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.wos.SdCardTools;
import com.wosplayer.service.CommunicationService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

import cn.trinea.android.common.util.FileUtils;
import wosTools.DataListEntiy;
import wosTools.ToolsUtils;

/**
 * Created by Administrator on 2016/7/19.
 */

public class WosApplication extends Application {


    public static DataList config = new DataList();
    public static Context appContext ;
    public static boolean isReadMeInfo = true;
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        log.e("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~wosPlayer app start~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        appContext = WosApplication.this.getApplicationContext();
        //捕获异常
        //CrashHandler.getInstance().init(getApplicationContext());
    }

    public void startAppInit(){
        //数据转移
        translationWosToolsData();
        //放入系统目录
        //new AdbShellCommd(this.getApplicationContext(),true,true).start();//会开端口,会重启
        //new AdbShellCommd(this.getApplicationContext(),true,false).start();//开端口,不重启
        //new AdbShellCommd(this.getApplicationContext(),false,true).start();//不开远程端口.会重启
       new AdbShellCommd(this.getApplicationContext(),false,false).start();//不开远程端口,不重启
        //初始化 配置信息
        //init(false);
    }
    /**
     *   false 读取 com.wos.tools 下面的信息
     *   true 读取自己app下面的初始化信息
     */
    public void init(boolean flag) {
        isReadMeInfo = flag;
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
        config.put("mac", GetMac());
        //本地ip
        String LocalIpAddress = getLocalIpAddress();
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
        if(defautSource(basepath,"default.mp4")){
            config.put("defaultVideo", config.GetStringDefualt("basepath","") + "default.mp4");//默认视频本地位置
        }

        config.put("CAPTIMAGEPATH", config.GetStringDefualt("basepath","") + "screen.png");//截图本地位置
        //将 默认图片 或者 视频 放入 指定 文件夹下

        //建设银行接口资源下载位置
        basepath =  GetKey("bankPathSource", "mnt/sdcard"+SdCardTools.Construction_Bank_dir_source);
        config.put("bankPathSource",basepath);
        SdCardTools.MkDir(basepath);
        basepath = GetKey("bankPathXml", "mnt/sdcard"+SdCardTools.Construction_Bank_dir_xmlfile);
        config.put("bankPathXml",basepath);
        SdCardTools.MkDir(basepath);

        log.i("--- app config init complete ---" );
        config.printData();
    }

    public static String getConfigValue(String key){
        if (config!=null){
            return config.GetStringDefualt(key,"");
        }
        return "";
    }

    private boolean defautSource(String basepath,String filename) {
        String path=basepath.endsWith("/")?basepath+filename:basepath+filename;
        if(!FileUtils.isFileExist(basepath)){
            return ToolsUtils.ReadAssectsDataToSdCard(getApplicationContext(),basepath,filename);
        }
        return true;
    }

    public static String GetKey(String key, String defualtValue) {
        String value = isReadMeInfo? ToolsUtils.readShareData(appContext,key):appTools.readShareDataTools(appContext,key).trim();
        return (value != "") ? value : defualtValue;
    }

    /**
     * 是否存在初始数据转移
     */
    public static boolean isWosToolsDataTranslation = false;
    /**
     * 数据 转移
     */
    public void translationWosToolsData(){
        try{
            Context wosToolsContext = appContext.createPackageContext("com.wos.tools", Context.CONTEXT_IGNORE_SECURITY);
            if (wosToolsContext!=null){
                log.d("#  WosTools Context 存在");
                //转移数据
                DataListEntiy toolsdataList = new DataListEntiy();
                toolsdataList.ReadShareData(false);
                toolsdataList.SaveShareData();
                isWosToolsDataTranslation = true;
            }else{
                log.d("#  WosTools Context 不存在");
            }
        }catch (Exception e){
            log.e("启动 转移数据 异常 : " + e.getMessage());
        }
    }

    /**
     * get mac
     */
    public static String getLocalMacAddressFromBusybox(){
        String result = "";
        String Mac = "";
        result = callCmd("busybox ifconfig","HWaddr");

        //如果返回的result == null，则说明网络不可取
        if(result==null){
            return "网络出错，请检查网络";
        }

        //对该行数据进行解析
        //例如：eth0      Link encap:Ethernet  HWaddr 00:16:E8:3E:DF:67
        if(result.length()>0 && result.contains("HWaddr")==true){
            Mac = result.substring(result.indexOf("HWaddr")+6, result.length()-1);
            Log.i("test","Mac:"+Mac+" Mac.length: "+Mac.length());

             if(Mac.length()>1){
                 Mac = Mac.replaceAll(" ", "");
                 result = "";
                 String[] tmp = Mac.split(":");
                 for(int i = 0;i<tmp.length;++i){
                     result +=tmp[i]+"-";
                 }
             }
            result = Mac;
            Log.i("test",result+" result.length: "+result.length());
        }
        return result;
    }

    private static String callCmd(String cmd,String filter) {
        String result = "";
        String line = "";
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(is);

            //执行命令cmd，只取结果中含有filter的这一行
            while ((line = br.readLine ()) != null && line.contains(filter)== false) {
                //result += line;
                Log.i("test","line: "+line);
            }

            result = line;
            Log.i("test","result: "+result);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return result;
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

        log.e("机器码 唯一识别码:"+uniqueId);
        uniqueId = getLocalMacAddressFromBusybox();
        log.e("物理地址:"+uniqueId);
        return uniqueId;
    }

    /**
     * ip
     * @return
     */
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
                        log.i("getLocalIpAddress() _ local IP : "+ inetAddress.getHostAddress().toString());
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
			log.e("","WifiPreference IpAddress :"+ex.toString());
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

        try {
            if (mc instanceof WosApplication){
                ((WosApplication)mc).init(true);
            }

            Intent intent = new Intent(mc, CommunicationService.class);
            //传递参数
            Bundle b = new Bundle();
            b.putString("ip",config.GetStringDefualt("serverip","127.0.0.1"));
            b.putInt("port",6666);
            b.putString("terminalNo",config.GetStringDefualt("terminalNo","127.0.0.1"));
            b.putLong("HeartBeatTime",(config.GetIntDefualt("HeartBeatInterval",10)));
            intent.putExtras(b);
            log.i("wosPlayerApp: 尝试开启通讯服务...");
            mc.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止服务
     */
    public static void stopCommunicationService(Context mc){

        try {
            Intent server = new Intent(mc, CommunicationService.class);
            mc.stopService(server);
        } catch (Exception e) {
            log.e("停止通讯服务Err:" +e.getMessage());
        }
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