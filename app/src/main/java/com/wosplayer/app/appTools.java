package com.wosplayer.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wosTools.ToolsUtils;
import com.wosplayer.tool.SdCardTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by Administrator on 2016/7/19.
 */

public class AppTools {

    private static final String TAG = AppTools.class.getName();



    /**
     * 生成机器码
     *
     * @return
     */
    public static String GetMac(Context context) {

        String mac = getLocalMacAddressFromWifiInfo(context);
        if (mac==null || "".equals(mac))
            mac = getMacAddress();
        if (mac==null || "".equals(mac))
            mac = getLocalMacAddressFromBusybox();
        return mac;
    }
    //本地以太网mac地址文件
    private static String getMacAddress()
    {
        String strMacAddr = "";
        byte[] b;
        try
        {
            NetworkInterface NIC = NetworkInterface.getByName("eth0");
            b = NIC.getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++)
            {
//                if (i != 0)
//                {
//                    buffer.append(':');
//                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toUpperCase();
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        Log.i(TAG, "--- DVB Mac Address : " + strMacAddr);

        return strMacAddr;
    }


    //根据Wifi信息获取本地Mac
    public static String getLocalMacAddressFromWifiInfo(Context context){
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
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
                        Logs.i("getLocalIpAddress() _ local IP : "+ inetAddress.getHostAddress().toString());
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Logs.e("","WifiPreference IpAddress :"+ex.toString());
        }
        return "";
    }

    //默认资源
    public static boolean defautSource(Context context,String basepath,String filename) {
        if(!FileUtils.isFileExist(basepath+filename)){
            return ToolsUtils.ReadAssectsDataToSdCard(context,basepath,filename);
        }
        return true;
    }


    //默认排期
    public static void defaultProgram(Context context,String dirpath) {
        try {
            // /mnt/sdcard/wosplayer/default/
            if (!FileUtils.isFileExist(dirpath+"def.mp4")
                    ||!FileUtils.isFileExist(dirpath+"default_sche.xml")
                    ||!FileUtils.isFileExist(dirpath+"default_prog.xml")
                    ){
                //如果一个 - 不存在 - 删除目录
                org.apache.commons.io.FileUtils.deleteDirectory(new File(dirpath));
                //放入zip包
                if(ToolsUtils.ReadAssectsDataToSdCard(context,dirpath,"default.zip")){
                    //解压缩
                    Logs.i("unzip","解压缩路径 : "+dirpath+"default.zip");
                    ToolsUtils.UnZip(dirpath+"default.zip",dirpath);
                    FileUtils.deleteFile(dirpath+"default.zip");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void fudianBankSource(Context context,String dirpath){
        try {
            //创建目录
            SdCardTools.MkDir(dirpath);
            //放入zip包
            if(ToolsUtils.ReadAssectsDataToSdCard(context,dirpath,"bank.zip")){
                //解压缩
                Logs.i("unzip","解压缩路径 : "+dirpath+"bank.zip");
                ToolsUtils.UnZip(dirpath+"bank.zip",dirpath);
                FileUtils.deleteFile(dirpath+"bank.zip");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置服务器信息成功 true
     * 失败 false
     * @param flag
     */
    public static synchronized void settingServerInfo(Context context,boolean flag){
        SharedPreferences preferences = context.getSharedPreferences("wos_server_conf_tag", Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("flag", flag);
        editor.commit();
    }
    /**
     * 读取是否设置服务器信息
     */
    public static synchronized boolean isSettingServerInfo(Context context){
        SharedPreferences preferences =context.getSharedPreferences("wos_server_conf_tag", Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        return preferences.getBoolean("flag", false);
    }

    public static void Toals(Context context,String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }
    public static void LongToals(Context context,String msg){
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
    }

    public static void NotifyHandle(Handler h,int what,Object data){
        if (h!=null){
            Message message = h.obtainMessage();
            message.what=what;
            message.obj=data;
            h.sendMessage(message);
        }
    }

    //将json字符串转换为map
    public static HashMap<String, String> jsonTxtToMap(String data){
        GsonBuilder gb = new GsonBuilder();
        Gson g = gb.create();
        HashMap<String, String> map = g.fromJson(data, new TypeToken<HashMap<String, String>>() {}.getType());
        return map;
    }
    /**
     * 将Map转化为Json文本
     *
     * @param map
     * @return String
     */
    public static <T> String mapToJson(Map<String, T> map) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(map);
        return jsonStr;
    }
    /**
     * 把url转化为xml格式数据
     *
     * @param urlString
     * @return the xml data or "" if catch Exception
     */
    public static String uriTranslationXml(String urlString) { // http:// xxxxx   -> file://xxx
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder xmlData = new StringBuilder();
            String temp;
            while ((temp = br.readLine()) != null)
                xmlData.append(temp).append("\n");
            return xmlData.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getAppVersion(Context context){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
