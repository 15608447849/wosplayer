package com.wosuis.newperf;


import android.graphics.Typeface;

import com.wosplayer.app.SystemConfig;
import com.wosuis.beans.ViewData;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.command.operation.schedules.correlation.XmlNodeEntity;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.trinea.android.common.util.StringUtils;

/**
 * Created by 79306 on 2017/3/1.
 */

public class UiDataTanslation {
    private static final String TAG = "xml数据->ui数据对象";
    //数据保存 - 存放节目
    public static ArrayList<String> proglist = new ArrayList<>();
    public static HashMap<String,ViewData> map = new HashMap<>();
    //资源文件目录
    private static String sourcePath;
    private static String ffbkPath;
    private static boolean isInit = false;
    public static void initParam(){
        sourcePath = SystemConfig.get().GetStringDefualt("basepath","");
        ffbkPath = SystemConfig.get().GetStringDefualt("fudianpath","");
        isInit = true;
    }
    //添加数据
    private static void add(ViewData data){
        map.put(data.getTags(),data);
    };
    private static void clear(){
        map.clear();
        proglist.clear();
    }

    //入口
    public static void tanslation(XmlNodeEntity schedule) {
        Logs.i(TAG, "线程名:" + Thread.currentThread().getName());
        try {
            if (!isInit){
                Logs.e(TAG, "初始化参数");
                initParam();
            }
            if (schedule == null) {
                Logs.e(TAG, "不执行空排期数据");
                return;
            }
            clear();
            Logs.i(TAG, "开始关联数据中");
            connProgram(schedule);
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
    //关联节目数据
    private static void connProgram(XmlNodeEntity schedule) {
        //得到节目数组
        ArrayList<XmlNodeEntity> programArr = schedule.getChildren();
        if (isEntry(programArr)) {
            Logs.e(TAG, "当前排期无节目列表");
            return;
        }
        ViewData data = null;
        for (XmlNodeEntity program : programArr) { //有多个节目
            data = new ViewData();
            settingProgramAttr(data,program);
            connLayout(data,program);
            add(data);
            proglist.add(data.getTags());
        }
    }
    //设置节目的属性值
    private static void settingProgramAttr(ViewData data, XmlNodeEntity program) {
        data.setTags(program.getXmldata().get("id"));
        data.setWidghtType(ViewData.Widget.program);
        data.setTitle(program.getXmldata().get("title"));
        data.setWidth(program.getXmldata().get("width"));
        data.setWidth(program.getXmldata().get("height"));
        String bgInfo = program.getXmldata().get("bgimage");
        //假设如果是图片
        if (StringUtils.isEmpty(bgInfo)){
            //假设如果是颜色
            bgInfo = program.getXmldata().get("bgcolor");
            if (StringUtils.isEmpty(bgInfo)){
                data.setBgType(ViewData.COLOR_NONE);
            }else{
                data.setBgType(ViewData.COLOR_COLOR);
                data.setBgColorCode(bgInfo);
            }
        }else{
            bgInfo = sourcePath+bgInfo;
            data.setBgType(ViewData.COLOR_IMAGE);
            data.setBgImagePath(bgInfo);
        }
    }

    //连接布局数据
    private static void connLayout(ViewData progData,XmlNodeEntity program) {
        //得到布局的数组
        ArrayList<XmlNodeEntity> layoutArr = program.getChildren();
        if (isEntry(layoutArr)) return;
        ArrayList<ViewData> layoutList = new ArrayList<>();

        ViewData data;
        for (XmlNodeEntity layout : layoutArr){
            data = new ViewData();
            settingLayoutAttr(data,layout);
            connContent(data,layout);
            progData.connect(data); //设置 节目 - 布局 的关联
            add(data);
            layoutList.add(data);
        }
        //1.布局下的内容的总时长得到布局的时长 2.布局时长最长的就是节目的时长
        progData.setPlayLength(getLayoutTimeOnMaxTime(layoutList));
    }
    //设置布局属性
    private static void settingLayoutAttr(ViewData data, XmlNodeEntity layout) {
        String x = layout.getXmldata().get("x");
        String y = layout.getXmldata().get("y");
        String width = layout.getXmldata().get("width");
        String height = layout.getXmldata().get("height");
        data.setSize(x,y,width,height);
        data.setTags(layout.getXmldata().get("id"));
        data.setTitle(layout.getXmldata().get("layoutname"));
        data.setWidghtType(ViewData.Widget.layout);
    }

    //关联内容
    private static void connContent(ViewData layoutData,XmlNodeEntity layout) {
        ArrayList<XmlNodeEntity> contentArr = layout.getChildren();
        if (isEntry(contentArr)) return;
        ViewData data;
        long layoutTime = 0;//布局时间
        for (XmlNodeEntity content : contentArr){
            data = new ViewData();
            settingContentAttr(data,content);
            layoutTime+=data.getPlayLength();
            layoutData.connect(data);//布局 关联 内容
            add(data); //加入 视图组
        }
        layoutData.setPlayLength(layoutTime);
    }
    //设置内容属性
    private static void settingContentAttr(ViewData data, XmlNodeEntity content) {
        String id = content.getXmldata().get("id");//id
        String materialid = content.getXmldata().get("materialid");//materialid
        data.setTags(id+"#"+materialid);
        String contentTimeText = content.getXmldata().get("timelength");//时长
        try {
            long contentTime = Long.parseLong(contentTimeText);
            data.setPlayLength(contentTime);//内容时长
        } catch (Exception e) {
            Logs.e(TAG, "获取节目-布局-内容 下的时长 , 解析错误:" + contentTimeText);
        }
        String fileproterty = content.getXmldata().get("fileproterty");//类型
        data.setWidghtType(ViewData.getWidgetType(fileproterty));
        data.setResourceAddress(content.getXmldata().get("getcontents"));//资源地址
        data.setLocalResource(sourcePath+content.getXmldata().get("contentsnewname"));//本地路径
        data.setShowName(content.getXmldata().get("contentsname"));//别名
        data.setPlayCount(content.getXmldata().get("playtimes"));//播放次数

        //根据类型 设置 额外参数
        if (data.getWidghtType() == ViewData.Widget.text){
            settingTextAttr(data,content);
        }
        if (data.getWidghtType() == ViewData.Widget.fudianbank){
            settingFFBKAttr(data,content);
        }
        if (data.getWidghtType() == ViewData.Widget.interactive){
            settingInterActiveAtte(data,content);
        }
    }

    //富癫银行
    private static void settingFFBKAttr(ViewData data, XmlNodeEntity content) {
        DataList datalist = new DataList();
        datalist.put("fudianpath", ffbkPath);
    }

    //设置文本类型参数
    private static void settingTextAttr(ViewData data, XmlNodeEntity content) {
        ArrayList<XmlNodeEntity> textArr = content.getChildren();
        if (isEntry(textArr)){
            Logs.e(TAG,"一个文本类型的 属性内容 不存在");
            return;
        }
        XmlNodeEntity textcontent = textArr.get(0);
        String boldstr = textcontent.getXmldata().get("fontweight");
        int boldvalue = Typeface.NORMAL;
        if (boldstr.equals("bold")){
            boldvalue = Typeface.BOLD;
        }
        boldstr = String.valueOf(boldvalue);
        String speed = textcontent.getXmldata().get("txtspeed");
        String fontcolor = textcontent.getXmldata().get("fontcolor");
        String bgcolor = textcontent.getXmldata().get("backgroudcolor");
        String texttype = textcontent.getXmldata().get("txtfont");
        String fontsize = textcontent.getXmldata().get("fontsize");
        String textContent = textcontent.getXmldata().get("txtcontents");
        DataList datalist = new DataList();
        datalist.put("bgcolor",bgcolor);
        datalist.put("fontcolor",fontcolor);
        datalist.put("fontsize",fontsize);
        datalist.put("textcontent",textContent);
        datalist.put("texttype",texttype);
        datalist.put("textstyle",boldstr);
        datalist.put("speed",speed);
        data.setDataList(datalist);
    }
    //互动层 数据组装.
    private static void settingInterActiveAtte(ViewData abuttonData, XmlNodeEntity content) {
        ArrayList<XmlNodeEntity> actionArr = content.getChildren();
        if (isEntry(actionArr)){
            return;
        }
        XmlNodeEntity entity = actionArr.get(0);
        ViewData layoutData = new ViewData();
        setActionLayoutAttr(layoutData,entity);//设置互动布局属性
        String homeurl = entity.getXmldata().get("homeurl");
        if (!StringUtils.isEmpty(homeurl) && homeurl.equals(abuttonData.getResourceAddress())){
            //判断字段 - homeurl 存在 , 第一层
            layoutData.setIAHome(true);
        }
        //互动布局 - 关联 上一层(互动按钮)
        abuttonData.connect(layoutData);
        //加入试图集合
        add(layoutData);
        //循环遍历下一层
        forecheNext(layoutData,entity);
    }

    //循环遍历 布局下面的 文件或者布局内容
    private static void forecheNext(ViewData layoutData, XmlNodeEntity entity) {
        ArrayList<XmlNodeEntity>  actionArr = entity.getChildren();
        if (isEntry(actionArr)){
            return;
        }
        ViewData buttonData ;
        for (XmlNodeEntity eny : actionArr){
                //创建广告视图数据 - 文件类型
                String adBindid = eny.getXmldata().get("id");
                if (!StringUtils.isEmpty(adBindid) && adBindid.equals(layoutData.getAdBindId()+"")){
                    setActionFilesAttr(layoutData,eny);
                    continue;
                }
            //一个按钮信息
            buttonData = new ViewData();
            settingActionButtonsAttr(buttonData,eny);
            //布局关联按钮
            layoutData.connect(buttonData);
            //加入视图组
            add(buttonData);

            //根据按钮信息 -> 关联 文件 或者 视图
            if (buttonData.getNextType() == 0){
                //布局类型下一级
                settingInterActiveAtte(buttonData,eny);
            }else if (buttonData.getNextType() == 1){
                //文件类型下一级
                setActionFilesAttr(layoutData,eny.getChildren().get(0));
            }
        }
    }
    //设置互动文件夹 属性
    private static void setActionFilesAttr(ViewData preData,XmlNodeEntity eny) {

        ViewData fileData = new ViewData();
        fileData.setTags(eny.getXmldata().get("id"));
        //设置类型
        fileData.setWidghtType(ViewData.Widget.afiles);
        //封面图
        String thumbnailpath = eny.getXmldata().get("thumbnailpath");
        if (thumbnailpath.contains("/")){
            thumbnailpath = thumbnailpath.substring(thumbnailpath.lastIndexOf("/")+1);
        }
        fileData.setThumbnail(sourcePath+thumbnailpath);

        //关联上一层视图
        preData.connect(fileData);
        //加入视图组
        add(fileData);
        ArrayList<XmlNodeEntity>  actionArr = eny.getChildren();
        if (isEntry(actionArr)){
         return;
        }
        for(XmlNodeEntity feny : actionArr ){
            settingFileCntents(fileData,feny);
        }
    }

    private static void settingFileCntents(ViewData fileData, XmlNodeEntity feny) {
        //图片image 1007 视频video 1002 网页web 1006
        ArrayList<XmlNodeEntity>  actionArr = feny.getChildren();
        if (isEntry(actionArr)){
            return;
        }
        String filetype;
        String filepath;
        String web_url;
        for (XmlNodeEntity e : actionArr){
            filetype = e.getXmldata().get("filetype");
            filepath = e.getXmldata().get("filepath");
            web_url =  e.getXmldata().get("web_url");
            if (filetype.equals("1007")){
                fileData.getActionFileContent().add("image#"+sourcePath+filepath);
            }
            if (filetype.equals("1002")){
                fileData.getActionFileContent().add("video#"+sourcePath+filepath);
            }
            if (filetype.equals("1006")){
                fileData.getActionFileContent().add("web#"+web_url);
            }
        }
    }

    //设置互动按钮属性
    private static void settingActionButtonsAttr(ViewData buttonData, XmlNodeEntity eny) {
        int x = 0,y = 0,w = -1,h = -1;
        try {
            x = Integer.parseInt(eny.getXmldata().get("xpos"));
            y = Integer.parseInt(eny.getXmldata().get("ypos"));
            w = Integer.parseInt(eny.getXmldata().get("width"));
            h = Integer.parseInt(eny.getXmldata().get("height"));
        } catch (Exception e) {
        }
        buttonData.setWidghtType(ViewData.Widget.abutton);
        buttonData.setSize(x,y,w,h);
        buttonData.setTags(eny.getXmldata().get("name"));
        buttonData.setNextTags(eny.getXmldata().get("bindid"));//按钮的下一级
        buttonData.setNextType(eny.getXmldata().get("bindtype"));
    }
    //设置互动 布局属性
    private static void setActionLayoutAttr(ViewData layoutData, XmlNodeEntity entity) {
            layoutData.setTags(entity.getXmldata().get("id"));
            //设置类型 - 互动布局
            layoutData.setWidghtType(ViewData.Widget.alayout);
            //设置封面图层thumbnailpath
            layoutData.setThumbnail(sourcePath+entity.getXmldata().get("thumbnailpath"));
            layoutData.setAdEnable(entity.getXmldata().get("adenable"));//广告授权
            if (layoutData.isAdEnable()) {
                layoutData.setAdBindId(entity.getXmldata().get("bindid"));
                layoutData.setAdwaittime(entity.getXmldata().get("waittime"));//广告等待时间
                layoutData.setAdinterval(entity.getXmldata().get("interval"));//广告间隔
            }
            layoutData.setWidth(entity.getXmldata().get("totalwidth"));//宽高
            layoutData.setHeight(entity.getXmldata().get("totalheight"));
            String bgModel = entity.getXmldata().get("bgmode");//背景相关
            if (bgModel.equals("0")){
            layoutData.setBgType(ViewData.COLOR_NONE);
            }else if (bgModel.equals("1")){
                layoutData.setBgType(ViewData.COLOR_COLOR);
                layoutData.setBgColorCode(entity.getXmldata().get("bg"));
            }else if (bgModel.equals("2")){
                layoutData.setBgType(ViewData.COLOR_IMAGE);
                layoutData.setBgImagePath(sourcePath+entity.getXmldata().get("bg"));
            }
    }

    //判断列表空白
    private static boolean isEntry(List list){
        if (list == null || list.size() == 0){
            return true;
        }
        return false;
    }

    public static long getLayoutTimeOnMaxTime(ArrayList<ViewData> layoutList) {
        //排序
        Collections.sort(layoutList, new Comparator<ViewData>() {

            @Override
            public int compare(ViewData lhs, ViewData rhs) {
                return lhs.getPlayLength() - rhs.getPlayLength() > 0 ? -1 : lhs.getPlayLength() - rhs.getPlayLength() == 0 ? 0 : 1;  //-1代表前者小，0代表两者相等，1代表前者大。
            }
        });
        return layoutList.get(0).getPlayLength();
    }
}
