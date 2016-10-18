package com.wosplayer.cmdBroadcast.Command.Schedule;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.wos.Toals;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.Schedule.correlation.XmlHelper;
import com.wosplayer.cmdBroadcast.Command.Schedule.correlation.XmlNodeEntity;
import com.wosplayer.cmdBroadcast.Command.Schedule.correlation.Xmlparse;
import com.wosplayer.cmdBroadcast.Command.iCommand;
import com.wosplayer.loadArea.loaderManager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2016/7/20.
 */

public class ScheduleSaver implements iCommand {
    private static ReentrantLock lock = new ReentrantLock();
    private static final String TAG = ScheduleSaver.class.getName();

    private final int ROOT_PARSE = 11;
    private final int SCHEDU_PROG_PARSE = 12;
    private final int ACTION_PARSE = 13;
    private final int I_LAYOUT_PARSE = 14;
    private final int I_FILE_PARSE = 15;

    public static enum ContentTypeEnum {
        interactive, webpage, url, rss, text, video, image, flash;

        public boolean needsDown() {
            return this.compareTo(video) >= 0;
        }
    }
    public static XmlNodeEntity rootNode = new XmlNodeEntity();//只存在一个
    /**
     * 序列化排期
     */
    public static void Serialize() {
        rootNode.SettingNodeEntitySave();
    }

    public static void clear(){
        uuks = null;
    }


//    private static Scheduler.Worker helper =  Schedulers.io().createWorker();
    /**
     * 执行
     *
     * @param param
     *
     */
    @Override
    public void Execute(String param) {
        log.i("获取排期信息,当前线程名:"+Thread.currentThread().getName());
        Toals.Say("排期地址 =  "+ param);


        saveData(param);
    }
    /**
     * 存储数据
     */
    private void saveData(String uri) {

        try {
            lock.lock();

            log.i(TAG," root uri:"+ uri);
            startWork(uri);
        } catch (Exception e) {
            log.e(TAG, " " + e.getMessage());
        }finally {
            lock.unlock();
        }
    }



    private void getXMLdata(String uri, final int callType, final Object Obj) {
        String result = uriTranslationXml(uri);
        if (result!=null && result.equals("")){
            isNextLoad = false;
            log.e(TAG," getXMLdata() result 不存在");
            return;
        }
        ParseResultXml(callType,result, Obj);
    }
    private static boolean isNextLoad = false;
    private void startWork(final String uri){

//        helper.schedule(new Action0() {
//            @Override
//            public void call() {




        isNextLoad = true;
        try {
            Long startTime = System.currentTimeMillis();
            getXMLdata(uri,ROOT_PARSE,null); //解析数据
            Long endTime = System.currentTimeMillis();
            log.e(TAG,"解析用时 : "+(endTime - startTime)+" 毫秒");
        } catch (Exception e) {
            e.printStackTrace();
            isNextLoad=false;
        }




                log.e(TAG,"是否开启下载:"+isNextLoad);
                if (isNextLoad){
                    //开启后台下载线程
                    log.i("当前的任务数:"+rootNode.getFtplist().size()+"-->"+rootNode.getFtplist().toString());
                    sendloadTask();
                }

//            }
//        });
    }

    /**
     * 通知 开始 下载 资源
     */
    private void sendloadTask() {

        ArrayList<CharSequence> tasklist = new ArrayList<CharSequence>();

        for (int i = 0; i<rootNode.getFtplist().size();i++){
            tasklist.add(rootNode.getFtplist().get(i));
        }
        Intent intent = new Intent(wosPlayerApp.appContext, loaderManager.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putCharSequenceArrayList(loaderManager.taskKey,tasklist);
        intent.putExtras(bundle);
        wosPlayerApp.appContext.startService(intent);
    }


    private static String url = null;
    private static String uuks = null;//全局 唯一标识

    /**
     * 解析 result
     */
    private void ParseResultXml(int callType, String result, Object obj) { //返回的xml数据


        switch (callType) {


            case ROOT_PARSE://root xml

                String scheduleXmlData = result;
                if (scheduleXmlData.equals("")) return;

                Element root = XmlHelper.getDomXml(new ByteArrayInputStream(scheduleXmlData.getBytes()));
                if (root == null) return;
                rootNode.Level="root";

                try {
                    String ruuks = XmlHelper.getFirstChildNodeValue(root, "uuks");
                    if (ruuks.equals("")) throw new Exception("uuks  is null");

                    Toals.Say("Lc:"+uuks+";Rm:"+ruuks);
                    if (uuks==null || !uuks.equals(ruuks)){
                        log.i(TAG," muuks is null OR local uuks != remote UUKS"+uuks+","+ruuks);
                        rootNode.Clear();//清楚数据
                        uuks = ruuks;

                    }else{
                        throw new Exception("排期无改变.uuks 无变化");
                    }

                    url = null;
                    url = XmlHelper.getFirstChildNodeValue(root, "url");
                    if (url.equals("")) throw new Exception("url is null");

                } catch (Exception e) {
                    log.e(TAG,e.getMessage());
                    isNextLoad = false;
                    return;
                }

                //图片 视频 错误时 所需

                //图片
                String errImage = "http://e.hiphotos.baidu.com/zhidao/pic/item/9345d688d43f8794f8bb0d5bd61b0ef41bd53a7a.jpg";
                rootNode.addUriTast(errImage); //创建一个ftp任务
                //视频
              /*  String errVideo = "http://static.zqgame.com/html/playvideo.html?name=http://lom.zqgame.com/v1/video/LOM_Promo~2.flv";
                rootNode.addUriTast(errVideo); //创建一个ftp任务*/

                NodeList scheduleList = root.getElementsByTagName("schedule");
                if (scheduleList.getLength() == 0) return;

                log.i(TAG,"当前排期总数:"+scheduleList.getLength());

                for (int i = scheduleList.getLength() - 1; i > -1; i--) {
                    Element scheduleElement = (Element) scheduleList.item(i);
                    Short type = null;
                    try {
                        type = Short.valueOf(XmlHelper.getFirstChildNodeValue(scheduleElement, "type"));
                    } catch (NumberFormatException e) {
                        type = 1;
                    }

                    String schedule_XmlData = XmlHelper.getNodeToString(scheduleList.item(i));

                    HashMap<String, String> schedule_xmldata_map = Xmlparse.ParseXml("/schedule", schedule_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);//排期

                    XmlNodeEntity schedule_node = rootNode.NewSettingNodeEntity();//添加一个子节点
                    schedule_node.Level = "root_schedule";
                    schedule_node.AddPropertyList(schedule_xmldata_map);
                    schedule_node.AddProperty("url", url);
                    schedule_node.AddProperty("uuks", uuks);

                    NodeList programsList = scheduleElement.getElementsByTagName("program"); //节目
                    if(programsList.getLength()==0) continue;

                    log.i(TAG,schedule_xmldata_map.get("id")+" 这个排期下面的 节目总数:"+programsList.getLength());
                    for (int i1 = 0; i1 < programsList.getLength(); i1++) {
                        Element program_Element = (Element) programsList.item(i1);
                        String pId = XmlHelper.getFirstChildNodeValue(program_Element, "id");
                        if (pId.equals("")) continue;

                        Log.i(TAG, "now to parse program url :" + url + pId );
                        //解析 节目:
                        getXMLdata(url + pId, SCHEDU_PROG_PARSE, schedule_node.NewSettingNodeEntity());// 下一节点 实体
                        log.i(TAG,"解析完一个节目\n\r");
                    }
                    log.i(TAG,"解析完一个排期\n\r");
                }
                break;


            case SCHEDU_PROG_PARSE://解析 一个排期下的其中一个节目

                log.i(TAG,"开始解析一个节目");
                String program_root_XmlData = result;
                if (program_root_XmlData.equals("")) return;

                Element program_root = XmlHelper.getDomXml(new ByteArrayInputStream(program_root_XmlData.getBytes()));
                if (program_root == null) return;

                Element programElement = (Element) program_root.getElementsByTagName("programmes").item(0);
                if (programElement == null) return;

                String program_XmlData = XmlHelper.getNodeToString(program_root);
                HashMap<String, String> program_xmldataMap = Xmlparse.ParseXml("/root/programmes", program_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);


                XmlNodeEntity program_node = (XmlNodeEntity) obj;
                program_node.Level = "root_schedule_programs";
                program_node.AddPropertyList(program_xmldataMap);
                program_node.AddProperty("uuks", uuks);


                if (program_xmldataMap.containsKey("src")) {
                    String url = program_xmldataMap.get("src").trim();
                    if (!url.equals("")) {
                        rootNode.addUriTast(url); //创建一个ftp任务
                    }
                }

                NodeList program_layout_List = programElement.getElementsByTagName("layout");
                if (program_layout_List.getLength()==0 ) return;

                log.i(TAG,"某节目下面的 布局 总数:"+program_layout_List.getLength());

                for (int i = 0; i < program_layout_List.getLength(); i++) {  //节目下面的布局循环
                    Element layout_Element = (Element) program_layout_List.item(i);
                    if (layout_Element==null) continue;

                    String layout_XmlData = XmlHelper.getNodeToString(layout_Element);
                    HashMap<String, String> layout_xmldataMap = Xmlparse.ParseXml("/layout", layout_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);

                    XmlNodeEntity program_layout_node = program_node.NewSettingNodeEntity();
                    program_layout_node.Level = "root_schedule_programs_layout";
                    program_layout_node.AddPropertyList(layout_xmldataMap);
                    program_layout_node.AddProperty("uuks", uuks);

                    NodeList layout_contentList = layout_Element.getElementsByTagName("contents");//内容
                    if(layout_contentList.getLength()==0) continue;
                    log.i(TAG,"节目下面的 布局 的 内容总数:"+layout_contentList.getLength());

                    for (int j = 0; j < layout_contentList.getLength(); j++) {//循环 节目_布局_内容
                        Element content_Element = (Element) layout_contentList.item(j);
                        if (content_Element==null) continue;

                        String content_XmlData = XmlHelper.getNodeToString(content_Element);
                        HashMap<String, String> content_xmlDataMap = Xmlparse.ParseXml("/contents", content_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);

                        XmlNodeEntity layout_content_Node
                                = program_layout_node.NewSettingNodeEntity();

                        layout_content_Node.Level = "root_schedule_programs_layout_content";
                        layout_content_Node.AddPropertyList(content_xmlDataMap);
                        layout_content_Node.AddProperty("uuks", uuks);

                        //内容类型
                        String content_type = XmlHelper.getFirstChildNodeValue(content_Element, "fileproterty");

                        ContentTypeEnum contentType;
                        try {
                            contentType = ContentTypeEnum.valueOf(content_type);
                        } catch (IllegalArgumentException e) {
                            log.e(TAG, "ScheduleSaver_>readContentXmlData_> contentType msg wrong:" + e.getMessage());
                            continue;
                        }

                        String getcontents = ""; //资源地址

                        if (contentType.equals(ContentTypeEnum.text))//文本
                        {
                             // getcontents = XmlHelper.getFirstChildToString(content_Element, "getcontents");
                            //content_xmlDataMap.put("getcontents", getcontents.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""));

                            String text_xml=  getcontents = XmlHelper.getFirstChildToString(content_Element, "getcontents");

                            HashMap<String, String> text_xmlDataMap = Xmlparse.ParseXml("/getcontents", text_xml, Xmlparse.parseType.OnlyLeaf).get(0);

                            XmlNodeEntity layout_content_text_Node
                                    = layout_content_Node.NewSettingNodeEntity();

                            layout_content_text_Node.Level = "root_schedule_programs_layout_content_text";
                            layout_content_text_Node.AddPropertyList(text_xmlDataMap);
                            layout_content_text_Node.AddProperty("uuks", uuks);

                            log.i(TAG,"\"-----------text end------\"");
                        } else if (contentType.equals(ContentTypeEnum.rss)) {

                            Element childfile = (Element) content_Element.getElementsByTagName("childfile").item(0);
                            String xfile = XmlHelper.getFirstChildNodeValue(childfile, "xfile");
                            String rss_programXmlData = XmlHelper.getXmlDataFromUrl(xfile);
                            if (rss_programXmlData.equals("")) continue;

                            Element rssRoot = XmlHelper.getDomXml(new ByteArrayInputStream(rss_programXmlData.getBytes()));
                            if (rssRoot==null) continue;

                            HashMap<String, String> rss_data_map = Xmlparse.ParseXml("/rss", rss_programXmlData, Xmlparse.parseType.OnlyLeaf).get(0);
                            content_xmlDataMap.putAll(rss_data_map);
                            log.i(TAG,"-----------rss end------");

                        } else if (contentType.equals(ContentTypeEnum.interactive)) {
                            //再解析 继续解析
                            getcontents = XmlHelper.getFirstChildNodeValue(content_Element, "getcontents");
                            log.i(TAG,"interaction uri :"+ getcontents);
                            getXMLdata(getcontents, ACTION_PARSE, layout_content_Node.NewSettingNodeEntity());//进入互动
                            log.i(TAG,"-----------interaction end------");
                        } else {
                            getcontents = XmlHelper.getFirstChildNodeValue(content_Element, "getcontents");
                            log.i(TAG,"不在需要判断的特殊类型中");
                        }

                        if (contentType.needsDown()) {

                            try {
                                getcontents = getcontents.trim();
                                if (!getcontents.equals("")) {
                                    rootNode.addUriTast(getcontents);
                                }
                            } catch (Exception e) {
                                log.e(TAG, e.getMessage());
                                continue;
                            }
                        }
                        log.i(TAG, "a schedule on program to end parse");
                    }
                }
                break;


            case ACTION_PARSE:
                //互动
                log.i(TAG,"  - -开始一个互动解析 - -");
                String interaction_XmlData = result;
                if (interaction_XmlData.equals("")) return;

                Element interaction_root = XmlHelper.getDomXml(new ByteArrayInputStream(interaction_XmlData.getBytes()));
                if (interaction_root == null) return;

                String layouturl = null;
                String folderurl = null;
                String thumbnailurl = null;
                try {
                    layouturl = XmlHelper.getFirstChildNodeValue(interaction_root, "layouturl");
                    if (layouturl.equals("")) throw new Exception("layouturl is null");

                    folderurl = XmlHelper.getFirstChildNodeValue(interaction_root, "folderurl");
                    if (folderurl.equals("")) throw new Exception("folderurl  is null");

                    thumbnailurl = XmlHelper.getFirstChildNodeValue(interaction_root, "thumbnailurl");
                    if (thumbnailurl.equals("")) throw new Exception("thumbnailurl  is null");

                } catch (Exception e) {
                        log.e(TAG,e.getMessage());
                    return;
                }

                Element action_layoutElement = (Element) interaction_root.getElementsByTagName("layout").item(0);
                if (action_layoutElement == null) return;

                String action_layout_XmlData = XmlHelper.getNodeToString(action_layoutElement);

                HashMap<String, String> layout_xmldataMap = Xmlparse.ParseXml("/layout", action_layout_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);

                XmlNodeEntity interaction_layout_node = (XmlNodeEntity) obj;//
                interaction_layout_node.Level = "interaction_layout";
                interaction_layout_node.AddPropertyList(layout_xmldataMap);
                interaction_layout_node.AddProperty("uuks", uuks);
                interaction_layout_node.AddProperty("layouturl", layouturl);
                interaction_layout_node.AddProperty("folderurl", folderurl);
                interaction_layout_node.AddProperty("thumbnailurl", thumbnailurl);

                //获取 缩略图 的地址
                String layout_thumbnailpath = XmlHelper.getFirstChildNodeValue(action_layoutElement, "thumbnailpath");//封面图片文件名，完整路径需加前缀
                if (layout_thumbnailpath==null || layout_thumbnailpath.equals("")) {
                    log.e(TAG,"按钮 背景 解析错误:背景图片名不存在");
                    String errImageBgUri = "http://imgsrc.baidu.com/forum/w%3D580/sign=55460f6a367adab43dd01b4bbbd5b36b/269759ee3d6d55fb1fa4ca646d224f4a20a4dd16.jpg";
                    rootNode.addUriTast(errImageBgUri);
                }else{
                    String bgmode_uri = thumbnailurl + layout_thumbnailpath;
                    //创建ftp
                    rootNode.addUriTast(bgmode_uri); //创建一个ftp任务
                }
                //ad
              Element adElement = (Element) action_layoutElement.getElementsByTagName("ad").item(0);
                if (adElement != null) {
                    String ad_myXmlData = XmlHelper.getNodeToString(adElement);
                    HashMap<String, String> ad_xmldataList = Xmlparse.ParseXml("/ad", ad_myXmlData, Xmlparse.parseType.OnlyLeaf).get(0);
                    XmlNodeEntity interaction_layout_ad_node = interaction_layout_node.NewSettingNodeEntity();
                    interaction_layout_ad_node.Level = "interaction_layout_ad";
                    interaction_layout_ad_node.AddPropertyList(ad_xmldataList);
                    interaction_layout_ad_node.AddProperty("uuks", uuks);
                }



                //items 包含了 布局的东西
                Element layout_ItemsElement = (Element) action_layoutElement.getElementsByTagName("items").item(0);
                if (layout_ItemsElement == null) return;

                String layout_Items_XmlData = XmlHelper.getNodeToString(layout_ItemsElement);
                HashMap<String, String> layout_items_xmldatamap = Xmlparse.ParseXml("/items", layout_Items_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);

               /*XmlNodeEntity interaction_layout_items_node = interaction_layout_node.NewSettingNodeEntity();
                interaction_layout_items_node.Level = "interaction_layout_items";
                interaction_layout_items_node.AddPropertyList(layout_items_xmldatamap);
                interaction_layout_items_node.AddProperty("uuks", uuks);*/

                interaction_layout_node.getXmldata().putAll(layout_items_xmldatamap);
                XmlNodeEntity interaction_layout_items_node = interaction_layout_node;

                String bgmode = XmlHelper.getFirstChildNodeValue(layout_ItemsElement, "bgmode");//布局背景图

                int bgmode_type = -1;

                try {
                    bgmode_type = Integer.parseInt(bgmode);
                } catch (Exception e) {
                    log.e(TAG, e.getMessage());
                    return;
                }
                if (bgmode_type == 2) {
                    String bg = XmlHelper.getFirstChildNodeValue(layout_ItemsElement, "bg");
                    if (bg == null || bg.equals("")) {
                        log.e(TAG,"一个布局背景解析错误:背景图片名不存在") ;
                        String layoutBgerr = "http://cdn.duitang.com/uploads/item/201205/07/20120507220903_VR22y.thumb.600_0.jpeg";
                        rootNode.addUriTast(layoutBgerr); //创建一个ftp任务
                    }else{
                        String bgmode_uri = thumbnailurl + bg;
                        //创建ftp
                        rootNode.addUriTast(bgmode_uri); //创建一个ftp任务
                    }
                }



                //循环得到按钮的信息
                NodeList items_itemNodeList = layout_ItemsElement.getElementsByTagName("item"); //子按钮
                if (items_itemNodeList.getLength() == 0) return;

                log.i(TAG,"一个互动下面的 按钮总数:"+items_itemNodeList.getLength());

                for (int i1 = 0; i1 < items_itemNodeList.getLength(); i1++) {
                    Element item_Element = (Element) items_itemNodeList.item(i1);

                    String Item_XmlData = XmlHelper.getNodeToString(item_Element);
                    HashMap<String, String> item_xmldataMap = Xmlparse.ParseXml("/item", Item_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);

                    XmlNodeEntity interaction_layout_items_item_node = interaction_layout_items_node.NewSettingNodeEntity();
                    interaction_layout_items_item_node.Level = "interaction_layout_items_item";
                    interaction_layout_items_item_node.AddPropertyList(item_xmldataMap);
                    interaction_layout_items_item_node.AddProperty("uuks", uuks);

                    //再次访问网络获取xml文件
                    String bindtype = XmlHelper.getFirstChildNodeValue(item_Element, "bindtype");
                    int bindtype_id = -1;
                    try {
                        bindtype_id = Integer.parseInt(bindtype);
                    } catch (Exception e) {
                        log.e(TAG, e.getMessage());
                    }

                    String uri_Id = XmlHelper.getFirstChildNodeValue(item_Element, "bindid");
                    if (uri_Id.equals("")) continue;

                    String item_uri = null;
                    if (bindtype_id == 0 || bindtype_id == 2) {  //布局类型 0 2
                        item_uri = layouturl + uri_Id;
                        log.i(TAG, "一个按钮 -布局- url :" + item_uri);
                        getXMLdata(item_uri, I_LAYOUT_PARSE, interaction_layout_items_item_node.NewSettingNodeEntity());

                    } else if (bindtype_id == 1 || bindtype_id == 3) { // 文件1 3网页
                        item_uri = folderurl + uri_Id;
                        log.i(TAG, "一个按钮 -文件- url :" + item_uri);
                        getXMLdata(item_uri, I_FILE_PARSE, interaction_layout_items_item_node.NewSettingNodeEntity());

                    }
                }

                break;


            case I_FILE_PARSE://文件

                log.i(TAG,"开始解析一个文件");
                String interaction_fl_XmlData = result;
                if (interaction_fl_XmlData.equals("")) return;

                Element folder_root = XmlHelper.getDomXml(new ByteArrayInputStream(interaction_fl_XmlData.getBytes()));
                if (folder_root == null) return;

                Element folderElement = (Element) folder_root.getElementsByTagName("folder").item(0);//wenjian yuansu
                if (folderElement == null) return;

                String folder_XmlData = XmlHelper.getNodeToString(folderElement);
                HashMap<String, String> folder_xmldataList = Xmlparse.ParseXml("/folder", folder_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);

                XmlNodeEntity interaction_layout_items_item_folder_node = (XmlNodeEntity) obj;//父节点实体
                interaction_layout_items_item_folder_node.Level = "interaction_layout_items_item_folder";
                interaction_layout_items_item_folder_node.AddPropertyList(folder_xmldataList);
                interaction_layout_items_item_folder_node.AddProperty("uuks", uuks);

                //创建ftp对象

                String thumbnailpath = XmlHelper.getFirstChildNodeValue(folderElement, "thumbnailpath");
                if (thumbnailpath != null && !thumbnailpath.equals("")) {
                    rootNode.addUriTast(thumbnailpath);//创建一个ftp任务 绑定他 的 按钮的背景图片
                }else{
                    log.i(TAG,"按钮 不存在 背景");
                    String layoutBgerr = "http://cdn.duitang.com/uploads/item/201205/07/20120507220903_VR22y.thumb.600_0.jpeg";
                    rootNode.addUriTast(layoutBgerr); //创建一个ftp任务
                }

                //循环得到子内容信息
                NodeList floder_item_List = folderElement.getElementsByTagName("item"); //子信息
                if (floder_item_List.getLength() == 0) return;
                log.i(TAG,"一个按钮下面绑定文件夹 包含的内容的总数:"+floder_item_List.getLength());

                for (int i1 = 0; i1 < floder_item_List.getLength(); i1++) {
                    Element floder_item_Element = (Element) floder_item_List.item(i1);

                    String floder_Item_XmlData = XmlHelper.getNodeToString(floder_item_Element);
                    HashMap<String, String> item_xml_dataMap = Xmlparse.ParseXml("/item", floder_Item_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);

                    XmlNodeEntity interaction_layout_items_item_floder_item_node = interaction_layout_items_item_folder_node.NewSettingNodeEntity();
                    interaction_layout_items_item_floder_item_node.Level = "interaction_layout_items_item_floder_item";
                    interaction_layout_items_item_floder_item_node.AddPropertyList(item_xml_dataMap);
                    interaction_layout_items_item_floder_item_node.AddProperty("uuks", uuks);

                    //文件类型
                    String filetype = XmlHelper.getFirstChildNodeValue(floder_item_Element, "filetype");
                    if (filetype.equals("1006")) {
                        log.i(TAG,"一个网页资源类型");
                        continue; //网页
                    }

                    //资源路径
                    String filepath = XmlHelper.getFirstChildNodeValue(floder_item_Element, "filepath");//资源路径
                    if(filepath.equals("")||filepath.equals("null")){
                       log.e(TAG,"互动 下面 的 文件 所需 资源 路径 错误 filepath:"+ filepath);
                        continue;
                    }else{
                        rootNode.addUriTast(filepath); //创建一个ftp任务
                    }
                    //还有第一帧的图像
                    if (filetype.equals("1002")) {
                        filepath = XmlHelper.getFirstChildNodeValue(floder_item_Element, "video_image_url");//视频第一帧路径
                        if(filepath.equals("")||filepath.equals("null")){

                            filepath="http://img15.3lian.com/2015/f1/59/d/31.jpg";
                            log.e("视频第一帧不存在,下载默认图片:"+ filepath);
                        }else {
                           String  mfilepath = filepath.substring(filepath.lastIndexOf("/")+1);
                            log.i(TAG,"mfilepath:"+mfilepath);
                            if (mfilepath.equals("null")){
                                filepath="http://img15.3lian.com/2015/f1/59/d/31.jpg";
                                log.e("视频第一帧不存在,下载默认图片:"+ filepath);
                            }
                        }
                        rootNode.addUriTast(filepath); //创建一个ftp任务
                    }
 //
                }
                log.i(TAG, "a interaction to end parse");
                break;
            case I_LAYOUT_PARSE ://布局
                ParseResultXml(ACTION_PARSE,result,obj);
                break;
        }
    }

    ///////////////////////////


    /**
     * 把url转化为xml格式数据
     *
     * @param urlString
     * @return the xml data or "" if catch Exception
     */
    public String uriTranslationXml(String urlString) {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e1) {
            log.e(TAG,""+ "url wrong:" +urlString +"cause: "+ e1.getMessage());
            return "";
        }
        URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
        } catch (IOException ioe) {

            log.e(TAG,""+ "url connect failed:" +  ioe.getMessage());
            return "";
        }

        InputStreamReader in = null;
        BufferedReader br = null;
        try {
            in = new InputStreamReader(urlConnection.getInputStream());
            br = new BufferedReader(in);
            StringBuilder xmlData = new StringBuilder();
            String temp;
            while ((temp = br.readLine()) != null)
                xmlData.append(temp).append("\n");
            return xmlData.toString();
        } catch (IOException e) {

            log.e(TAG,""+ "get input stream error:" +  e.getMessage());

            return "";
        } finally {
            try {
                if (br != null)
                    br.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {

                log.e(TAG,""+ "close input stream error:" +  urlString+"cause:" +e.getMessage());
                e.printStackTrace();
            }
        }
    }

    ///////////////////////











}











