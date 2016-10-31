package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.Schedule.ScheduleSaver;
import com.wosplayer.cmdBroadcast.Command.iCommand;

/**
 * Created by user on 2016/10/25.
 * lzp
 *
 *      Android上可用的每种资源 - 图像、视频片段等都可以用Uri来表示
 *          URI一般由三部分组成：
 　　　　访问资源的命名机制。
 　　　　存放资源的主机名。
 　　　　资源自身的名称，由路径表示。

 content://"、数据的路径、标示ID(可选)
 联系人的Uri： content://contacts/people
 某个联系人的Uri: content://contacts/people/5
 所有图片Uri: content://media/external
 某个图片的Uri：content://media/external/images/media/4
 */
public class Command_TSLT implements iCommand {
    private static final String TAG = "建行数据对接测试";
    ScheduleSaver saver = null;
    @Override
    public void Execute(String param) {

            //获取指定目录下面的文件 通过file:///
            getSrcFile();


    }

    public Object getSrcFile() {
        try {


        //sdcard -> mnt/internal_sd
        //mnt/external_sd
            //xml    1.ScheduleNode.txt  2.ProgramNode.txt

            //资源  source  -> /mnt/external_sd/wosplayer/construction_bank/source/
            // xml -> /mnt/external_sd/wosplayer/construction_bank/xml/
        String str = "file://"+ wosPlayerApp.config.GetStringDefualt("bankPathXml","/mnt/external/")+"ScheduleNode.txt";
//        str =  ScheduleSaver.uriTranslationXml(str);
//        log.i(TAG," 排期 : \n" + str);
//
//        str = "file:///mnt/external_sd/xml/ProgramNode.txt";
//        str =  ScheduleSaver.uriTranslationXml(str);
//        log.i(TAG,"节目 : \n" + str);

            if(saver==null){
                saver = new ScheduleSaver();
            }
            ScheduleSaver.clear();
            saver.Execute(str);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }









}
