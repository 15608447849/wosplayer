package com.wosplayer.app;

import android.util.Log;

import com.wosplayer.tool.SdCardTools;

import java.util.HashMap;
import java.util.Map;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by 79306 on 2017/2/20.
 */

public class SystemConfig extends DataList{
    private static final String TAG = "播放器系统配置";
    //文件保存路径
    private static final String PATH = "/mnt/sdcard/wosplayer/config.conf";
    private static SystemConfig systemConfig = null;

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {

        }
    };
    private SystemConfig()
    {
//        BackRunner.runBackground(runnable);
        check();
    }

    public static SystemConfig get(){
        if (systemConfig==null){
            systemConfig = new SystemConfig();
        }
        return systemConfig;
    }


    //检测配置文件是否存在
    private void  check(){
        //查看文件是否存在
        if (!FileUtils.isFileExist(PATH)){
            //创建文件夹并且成功设置默认值
            DefaultValue();
        };
    }

    //设置默认值
    private void DefaultValue() {
        HashMap<String,String> map = new HashMap<>();
        //终端id
        map.put("terminalNo","");
        //通信模式
        map.put("connectionType","socket");
        //通信端口
        map.put("keyText","10000");
        //服务端地址
        map.put("serverip", "172.16.0.53");
        //访问服务端端口
        map.put("serverport","8000");
        //公司id
        map.put("companyid","999");
        //心跳时间
        map.put("HeartBeatInterval","30");
        //重启时间
        map.put("RestartBeatInterval","30");//
        //sdcard 清理阔值
        map.put("storageLimits","50");
        //机器码
        map.put("mac","00-00-00-00-00");
        //本地ip
        map.put("tip","127.0.0.1");
        //访问服务端的URL
        map.put("CaptureURL", String.format("http://%s:%s/wos/captureManagerServlet","192.168.6.66","8000"));
        //本地资源存储目录
        map.put("basepath", "/mnt/sdcard/wosplayer/resource/");

        //建设银行接口资源下载位置
        map.put("bankPathSource","mnt/sdcard/wosplayer"+SdCardTools.Construction_Bank_dir_source);
        map.put("bankPathXml","mnt/sdcard"+SdCardTools.Construction_Bank_dir_xmlfile);

        //默认的本地资源路径
        map.put("default","/mnt/sdcard/wosplayer/default/");//默认资源路径
        map.put("defaultVideo", "/mnt/sdcard/wosplayer/default/def.mp4");//默认视频本地位置
        map.put("fudianpath","/mnt/sdcard/wosplayer/ffbk/");//富颠银行本地web资源
        map.put("CAPTIMAGEPATH", "/mnt/sdcard/wosplayer/screen.png");//截图本地位置
        this.setMap(map);
        //保存数据
        boolean isWrite = FileUtils.writeFile(PATH,AppTools.mapToJson(map));
        if (isWrite){Logs.d(TAG, " --- 还原默认设置 ---");}
    }


    //读取数据file->list
    public SystemConfig read(){
        try {
            String content =  FileUtils.readFile(PATH,"utf-8").toString();
            Logs.d(TAG,"读取配置文件："+content);
            if (!content.isEmpty()) {
                Map map = AppTools.jsonTxtToMap(content);
                this.setMap((HashMap<String, String>) map);
                Logs.i(TAG,"系统配置文件读取成功" );
            }
        } catch (Exception e) {
            Logs.e(TAG,"系统配置文件读取异常" );
            //恢复默认值
            DefaultValue();
        }
        return systemConfig;
    }
    //保存数据 list->file
    public void save(){
        if (map.isEmpty()) return;
        String content = AppTools.mapToJson(map);
        Logs.d(TAG,"保存配置文件："+content);
        boolean flag =  FileUtils.writeFile(PATH,content);
        Logs.i(TAG,"保存系统配置文件存储结果:"+flag );
    }

}
