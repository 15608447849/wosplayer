package com.wosplayer.app;

import android.annotation.SuppressLint;

import com.wosplayer.tool.SdCardTools;

import java.util.HashMap;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by 79306 on 2017/2/20.
 */

public class SystemConfig extends DataList{
    private static final String TAG = "播放器系统配置";
    private static final String DIR = "/mnt/sdcard/wosplayer/";
    public static final String [] PLAY_MODE_ARR = {"未选择","网络模式","单机模式"};
    //文件保存路径
    private static final String PATH = DIR+"config.conf";
    private static SystemConfig systemConfig = null;

    private SystemConfig()
    {
     SdCardTools.MkDir(DIR);
     check();
    }

    public static SystemConfig get(){
        if (systemConfig==null){
            systemConfig = new SystemConfig();
        }

        return systemConfig;
    }



    public SystemConfig putOr(String key, String value) {
        this.put(key, value);
        return this;
    }

    //检测配置文件是否存在
    private void  check(){
        //查看文件是否存在
        if (!FileUtils.isFileExist(PATH)){
            //创建文件夹并且成功设置默认值
            DefaultValue();
        }
        else{
            read();
        }
    }

    //设置默认值
    @SuppressLint("SdCardPath")
    private void DefaultValue() {
        HashMap<String,String> map = getMap();
        map.clear();
        //终端id
        map.put("terminalNo","");
        //通信模式
        map.put("connectionType","socket");
        //socket端口
        map.put("socketport","6666");
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
        map.put("RestartBeatInterval","30");

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
        //apk下载路径
        map.put("updatepath", "/data/local/tmp/");
        //建设银行接口资源下载位置
        map.put("bankPathSource","mnt/sdcard/wosplayer"+SdCardTools.Construction_Bank_dir_source);
        map.put("bankPathXml","mnt/sdcard"+SdCardTools.Construction_Bank_dir_xmlfile);

        //默认的本地资源路径
        map.put("default","/mnt/sdcard/wosplayer/default/");//默认资源路径
        map.put("defaultVideo", "/mnt/sdcard/wosplayer/default/dvideo.mp4");//默认视频本地位置
        map.put("defaultImage","/mnt/sdcard/wosplayer/default/dimage.png");//默认图片
        map.put("fudianpath","/mnt/sdcard/wosplayer/ffbk/");//富颠银行本地web资源
        map.put("CapturePath", "/mnt/sdcard/wosplayer/screen.png");//截图本地位置

        //系统使用参数
        map.put("watchValue","0");  //监听服务是否监听
        map.put("uuks",""); //当前播放的节目uuks标识
        //截屏参数
        map.put("CaptureSave","0");//截屏是否保存
        map.put("CaptureNoty","0");//截屏是否通知
        map.put("CaptureMode","0");// ,命令截屏优先
        //是否关闭本地监听播放器值
        map.put("AccessClose","1");//默认不关闭
        //播放器类型选择
        map.put("playMode", PLAY_MODE_ARR[0]);// 1网络模式 2单机模式
        //单机播放时间 默认 10秒
        map.put("SingImageTime","10");// 单一图片播放时间
        //保存数据
        boolean isWrite = FileUtils.writeFile(PATH, AppUtils.mapToJson(map));
        if (isWrite){Logs.d(TAG, " ---播放器配置已还原默认设置 ---");}
    }


    //读取数据file->list
    public SystemConfig read(){
        try {
            String content =  FileUtils.readFile(PATH,"utf-8").toString();
            //Logs.d(TAG,"读取配置文件:\n"+content);
            if (!content.isEmpty()) {
                HashMap<String, String> map = AppUtils.jsonTxtToMap(content);
                this.setMap( map);
//                Logs.i(TAG,"系统配置文件读取成功" );
            }
        } catch (Exception e) {
            Logs.e(TAG,"系统配置文件读取异常" );
            //恢复默认值
            DefaultValue();
        }
        return this;
    }
    //保存数据 list->file
    public void save(){
        if (map.isEmpty()) return;
        String content = AppUtils.mapToJson(map);
//        Logs.d(TAG,"保存配置文件："+content);
        boolean flag =  FileUtils.writeFile(PATH,content);
        //Logs.d(TAG,"存储系统配置文件["+PATH+"]结果:"+(flag?" 成功":" 失败"));
    }

}
