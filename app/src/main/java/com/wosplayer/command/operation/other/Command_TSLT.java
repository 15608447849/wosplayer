package com.wosplayer.command.operation.other;

import android.app.Activity;

import com.wosplayer.app.Logs;
import com.wosplayer.app.SystemConfig;
import com.wosplayer.command.operation.schedules.ScheduleSaver;
import com.wosplayer.command.operation.interfaces.iCommand;

/**
 * Created by user on 2016/10/25.
 * lzp
 * <p>
 * Android上可用的每种资源 - 图像、视频片段等都可以用Uri来表示
 * URI一般由三部分组成：
 * 　　　　访问资源的命名机制。
 * 　　　　存放资源的主机名。
 * 　　　　资源自身的名称，由路径表示。
 * <p>
 * content://"、数据的路径、标示ID(可选)
 * 联系人的Uri： content://contacts/people
 * 某个联系人的Uri: content://contacts/people/5
 * 所有图片Uri: content://media/external
 * 某个图片的Uri：content://media/external/images/media/4
 */
public class Command_TSLT implements iCommand {
    private static final String TAG = "_TSLT";
    private iCommand saver = null;
    //构造
    public Command_TSLT(iCommand saver) {
      this.saver = saver;
    }

    @Override
    public void execute(Activity activity, String param) {
        if (param.equals("default_")) {
            //执行默认节目
            getDefaultProg(activity);
        }
    }


    //本地默认排期
    public void getDefaultProg(Activity activity) {
        try {
            String defaultPath = SystemConfig.get().read().GetStringDefualt("default","");
            if (defaultPath.isEmpty()) return;

            if (!cn.trinea.android.common.util.FileUtils.isFileExist(defaultPath+"default_sche.xml")) {
                ///文件不存在 - 解压缩
//                AppTools.defaultProgram(activity,defaultPath);
                Logs.e(TAG,"默认排期不存在.");
                Thread.sleep(100);
                getDefaultProg(activity);
            }else{
                defaultPath = "file://"+defaultPath+"default_sche.xml";
                //发送广播 -> 排期
                if (saver == null) {
                    saver = new ScheduleSaver();
                }
                saver.execute(null,defaultPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //兼容建设银行 暂未启动
    public Object getSrcFile() {
        try {
            //sdcard -> mnt/internal_sd
            //mnt/external_sd
            //xml    1.ScheduleNode.txt  2.ProgramNode.txt
            //资源  source  -> /mnt/external_sd/wosplayer/construction_bank/source/
            // xml -> /mnt/external_sd/wosplayer/construction_bank/xml/
            String str = "file://" + SystemConfig.get().read().GetStringDefualt("bankPathXml", "") + "ScheduleNode.txt";
//        str =  ScheduleSaver.uriTranslationXml(str);
//        log.i(TAG," 排期 : \n" + str);
//
//        str = "file:///mnt/external_sd/xml/ProgramNode.txt";
//        str =  ScheduleSaver.uriTranslationXml(str);
//        log.i(TAG,"节目 : \n" + str);

            if (saver == null) {
                saver = new ScheduleSaver();
            }

            saver.execute(null,str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
