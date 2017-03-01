package com.wosplayer.Ui.newperf;


import android.graphics.Typeface;

import com.wosplayer.Ui.beans.ViewData;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.command.operation.schedules.correlation.XmlNodeEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.trinea.android.common.util.StringUtils;

/**
 * Created by 79306 on 2017/3/1.
 */

public class UiDataTanslation {
    private static final String TAG = "数据->ui对象";
    //数据保存
    private static ArrayList<ViewData>  progList = new ArrayList<>();
    private static ArrayList<ViewData> layoutList = new ArrayList<>();
    private static ArrayList<ViewData> contentList = new ArrayList<>();
    //初始化参数
    private static String sourcePath;
    private static String ffbkPath;
    private static boolean isInit = false;
    public static void initParam(){
        sourcePath = PlayApplication.config.GetStringDefualt("basepath","");
        ffbkPath = PlayApplication.config.GetStringDefualt("fudianpath","");
        isInit = true;
    }

    private static void clear(){
        progList.clear();
        layoutList.clear();
        contentList.clear();
    }

    public static void tanslation(XmlNodeEntity schedule) {
        Logs.i(TAG, "线程名:" + Thread.currentThread().getName());
        try {
            if (!isInit){
                Logs.e(TAG, "请初始化参数");
                return;
            }
            if (schedule == null) {
                Logs.e(TAG, "不执行空排期数据");
                return;
            }
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
        for (XmlNodeEntity program : programArr) {
            data = new ViewData();
            connLayout(data,program);
            settingProgramAttr(data,program);
            progList.add(data);
        }
    }
    //设置节目的属性值
    private static void settingProgramAttr(ViewData data, XmlNodeEntity program) {
        data.setTitle(program.getXmldata().get("title"));
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
        ViewData data;
        for (XmlNodeEntity layout : layoutArr){
            data = new ViewData();
            connContent(data,layout);
            settingLayoutAttr(data,layout);
            layoutList.add(data);
        }
        //1.布局下的内容的总时长得到布局的时长 2.布局时长最长的就是节目的时长
        progData.setPlayLength(getLayoutTimeOnMaxTime());
    }
    //设置布局属性
    private static void settingLayoutAttr(ViewData data, XmlNodeEntity layout) {
        String x = layout.getXmldata().get("x");
        String y = layout.getXmldata().get("y");
        String width = layout.getXmldata().get("width");
        String height = layout.getXmldata().get("height");
        data.setSize(x,y,width,height);
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
            contentList.add(data);
        }
        layoutData.setPlayLength(layoutTime);
    }
    //设置内容属性
    private static void settingContentAttr(ViewData data, XmlNodeEntity content) {
        String contentTimeText = content.getXmldata().get("timelength");
        try {
            long contentTime = Long.parseLong(contentTimeText);
            data.setPlayLength(contentTime);//内容时长
        } catch (Exception e) {
            Logs.e(TAG, "获取节目-布局-内容 下的时长 , 解析错误:" + contentTimeText);
        }
        String fileproterty = content.getXmldata().get("fileproterty");//类型
        data.setWidghtType(ViewData.getWidgetType(fileproterty));
        String getcontents = content.getXmldata().get("getcontents");//资源地址
        data.setResourceAddress(getcontents);
        String localpath = sourcePath+content.getXmldata().get("contentsnewname");//本地路径
        data.setLocalResource(localpath);
        String contentsname = content.getXmldata().get("contentsname");//别名
        data.setShowName(contentsname);
        String playtimes = content.getXmldata().get("playtimes");//播放次数
        data.setPlayCount(playtimes);
        //根据类型 设置 额外参数
        if (data.getWidghtType() == ViewData.Widget.text){
            settingTextAttr(data,content);
        }
        if (data.getWidghtType() == ViewData.Widget.fudianbank){
            settingFFBKAttr(data,content);
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
        data.setOtherAttr(datalist);
    }


    //判断列表空白
    private static boolean isEntry(List list){
        if (list == null || list.size() == 0){
            return true;
        }
        return false;
    }

    public static long getLayoutTimeOnMaxTime() {
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
