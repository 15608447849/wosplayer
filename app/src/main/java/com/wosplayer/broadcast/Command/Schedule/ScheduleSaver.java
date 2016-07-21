package com.wosplayer.broadcast.Command.Schedule;

import android.util.Log;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wosplayer.app.log;
import com.wosplayer.broadcast.Command.Schedule.correlation.XmlHelper;
import com.wosplayer.broadcast.Command.Schedule.correlation.XmlNodeEntity;
import com.wosplayer.broadcast.Command.Schedule.correlation.Xmlparse;
import com.wosplayer.broadcast.Command.iCommand;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/7/20.
 */

public class ScheduleSaver implements iCommand {
    private final String TAG = ScheduleSaver.class.getName();


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


    private HttpUtils http = new HttpUtils();//网络连接使用
    public static XmlNodeEntity rootNode = new XmlNodeEntity();//只存在一个

    /**
     * 序列化排期
     */
    public static void Serialize() {
        rootNode.SettingNodeEntitySave();
    }

    /**
     * 执行
     *
     * @param param
     * @param obj
     */
    @Override
    public void Execute(String param, Object obj) {

    }


    /**
     * 存储数据
     */
    private void saveData(String uri) {

        try {
            rootNode.Clear();
            rootNode.ftplist.clear();
        } catch (Exception e) {
            log.e(TAG, " - - " + e.getMessage());
        }

        log.i(TAG," root uri:"+ uri);
        getXMLdata(uri, ROOT_PARSE, null);

    }

    /**
     * 获取数据
     *
     * @param uri
     * @param callType
     */
    private void getXMLdata(String uri, final int callType, final Object Obj) {
        http.send(
                HttpRequest.HttpMethod.GET,
                uri,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {

                        //进行XML解析
                        ParseResultXml(callType, responseInfo.result, Obj);

                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        log.i(TAG,"http err"+e.getMessage() + ">" + s);
                    }
                }
        );
    }


    String url = null;
    String uuks = null;//全局 唯一标识

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

                try {
                    url = XmlHelper.getFirstChildNodeValue(root, "url");
                    if (url.equals("")) throw new Exception("url is null");
                    uuks = XmlHelper.getFirstChildNodeValue(root, "uuks");
                    if (uuks.equals("")) throw new Exception("uuks  is null");
                } catch (Exception e) {
                    log.e(TAG,e.getMessage());
                    return;
                }


                NodeList scheduleList = root.getElementsByTagName("schedule");
                if (scheduleList.getLength() == 0) return;


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
                    schedule_node.Level = "schedule";
                    schedule_node.AddPropertyList(schedule_xmldata_map);
                    schedule_node.AddProperty("url", url);
                    schedule_node.AddProperty("uuks", uuks);

                    NodeList programsList = scheduleElement.getElementsByTagName("program"); //节目

                    for (int i1 = 0; i1 < programsList.getLength(); i1++) {
                        Element program_Element = (Element) programsList.item(i1);
                        String pId = XmlHelper.getFirstChildNodeValue(program_Element, "id");
                        if (pId.equals("")) continue;
                        Log.i(TAG, "now to parse program url :" + url + pId );
                        //解析 节目:
                        getXMLdata(url + pId, SCHEDU_PROG_PARSE, schedule_node.NewSettingNodeEntity());// 下一节点 实体
                    }
                }
                break;


            case SCHEDU_PROG_PARSE://解析 单个排期的一个节目

                String program_root_XmlData = result;
                if (program_root_XmlData.equals("")) return;

                Element program_root = XmlHelper.getDomXml(new ByteArrayInputStream(program_root_XmlData.getBytes()));
                if (program_root == null) return;

                Element programElement = (Element) program_root.getElementsByTagName("programmes").item(0);
                if (programElement == null) return;

                String program_XmlData = XmlHelper.getNodeToString(program_root);
                HashMap<String, String> program_xmldataMap = Xmlparse.ParseXml("/root/programmes", program_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);


                XmlNodeEntity program_node = (XmlNodeEntity) obj;
                program_node.Level = "programs";
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


                for (int i = 0; i < program_layout_List.getLength(); i++) {  //节目下面的布局循环
                    Element layout_Element = (Element) program_layout_List.item(i);
                    if (layout_Element==null) continue;

                    String layout_XmlData = XmlHelper.getNodeToString(layout_Element);
                    HashMap<String, String> layout_xmldataMap = Xmlparse.ParseXml("/layout", layout_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);

                    XmlNodeEntity program_layout_node = program_node.NewSettingNodeEntity();
                    program_layout_node.Level = "layout";
                    program_layout_node.AddPropertyList(layout_xmldataMap);
                    program_layout_node.AddProperty("uuks", uuks);


                    NodeList layout_contentList = layout_Element.getElementsByTagName("contents");//内容
                    if(layout_contentList.getLength()==0) return;


                    for (int j = 0; j < layout_contentList.getLength(); j++) {
                        Element content_Element = (Element) layout_contentList.item(j);
                        if (content_Element==null) continue;


                        String content_XmlData = XmlHelper.getNodeToString(content_Element);
                        HashMap<String, String> content_xmlDataMap = Xmlparse.ParseXml("/contents", content_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);

                        XmlNodeEntity layout_content_Node
                                = program_layout_node.NewSettingNodeEntity();

                        layout_content_Node.Level = "Content";
                        layout_content_Node.AddPropertyList(content_xmlDataMap);
                        layout_content_Node.AddProperty("uuks", uuks);


                        //内容类型
                        String content_type = XmlHelper.getFirstChildNodeValue(content_Element, "fileproterty");

                        ContentTypeEnum contentType;
                        try {
                            contentType = ContentTypeEnum.valueOf(content_type);
                        } catch (IllegalArgumentException e) {
                            log.e(TAG, "ScheduleSaver_>readContentXmlData_> contentType msg wrong:" + e.getMessage());
                            return;
                        }

                        String getcontents = ""; //资源地址

                        if (contentType.equals(ContentTypeEnum.text))//文本
                        {
                            getcontents = XmlHelper.getFirstChildToString(content_Element, "getcontents");
                            content_xmlDataMap.put("getcontents", getcontents.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""));

                        } else if (contentType.equals(ContentTypeEnum.rss)) {

                            Element childfile = (Element) content_Element.getElementsByTagName("childfile").item(0);
                            String xfile = XmlHelper.getFirstChildNodeValue(childfile, "xfile");
                            String rss_programXmlData = XmlHelper.getXmlDataFromUrl(xfile);
                            if (rss_programXmlData.equals("")) return;

                            Element rssRoot = XmlHelper.getDomXml(new ByteArrayInputStream(rss_programXmlData.getBytes()));
                            if (rssRoot==null) return;

                            HashMap<String, String> rss_data_map = Xmlparse.ParseXml("/rss", rss_programXmlData, Xmlparse.parseType.OnlyLeaf).get(0);
                            content_xmlDataMap.putAll(rss_data_map);

                        } else if (contentType.equals(ContentTypeEnum.interactive)) {
                            //再解析 继续解析
                            getcontents = XmlHelper.getFirstChildNodeValue(content_Element, "getcontents");
                            getXMLdata(getcontents, ACTION_PARSE, layout_content_Node.NewSettingNodeEntity());//进入互动
                        } else {
                            getcontents = XmlHelper.getFirstChildNodeValue(content_Element, "getcontents");
                        }


                        if (contentType.needsDown()) {
                            try {
                                getcontents = getcontents.trim();
                                if (!getcontents.equals("")) {
                                    rootNode.addUriTast(getcontents);
                                }
                            } catch (Exception e) {
                                log.e(TAG, e.getMessage());
                            }
                        }
                        log.d(TAG, "a schedule on program to end parse");
                    }
                }
                break;


            case ACTION_PARSE:
                //互动
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

                //ad
                Element adElement = (Element) action_layoutElement.getElementsByTagName("ad").item(0);
                if (adElement == null) return;
                String ad_myXmlData = XmlHelper.getNodeToString(adElement);
                HashMap<String, String> ad_xmldataList = Xmlparse.ParseXml("/ad", ad_myXmlData, Xmlparse.parseType.OnlyLeaf).get(0);
                XmlNodeEntity interaction_layout_ad_node = interaction_layout_node.NewSettingNodeEntity();
                interaction_layout_ad_node.Level = "interaction_layout_ad";
                interaction_layout_ad_node.AddPropertyList(ad_xmldataList);
                interaction_layout_ad_node.AddProperty("uuks", uuks);


                //items
                Element layout_ItemsElement = (Element) action_layoutElement.getElementsByTagName("items").item(0);
                if (layout_ItemsElement == null) return;

                String layout_Items_XmlData = XmlHelper.getNodeToString(layout_ItemsElement);
                HashMap<String, String> layout_items_xmldatamap = Xmlparse.ParseXml("/items", layout_Items_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);

                XmlNodeEntity interaction_layout_items_node = interaction_layout_node.NewSettingNodeEntity();
                interaction_layout_items_node.Level = "interaction_layout_items";
                interaction_layout_items_node.AddPropertyList(layout_items_xmldatamap);
                interaction_layout_items_node.AddProperty("uuks", uuks);

                String bgmode = XmlHelper.getFirstChildNodeValue(layout_ItemsElement, "bgmode");//布局背景图

                int bgmode_type = -1;

                try {
                    bgmode_type = Integer.parseInt(bgmode);
                } catch (Exception e) {
                    log.e(TAG, e.getMessage());
                }
                if (bgmode_type == 2) {
                    String bg = XmlHelper.getFirstChildNodeValue(layout_ItemsElement, "bg");
                    String bgmode_uri = thumbnailurl + bg;
                    //创建ftp
                    rootNode.addUriTast(bgmode_uri); //创建一个ftp任务
                }

                //循环得到按钮的信息
                NodeList items_itemNodeList = layout_ItemsElement.getElementsByTagName("item"); //子按钮
                if (items_itemNodeList.getLength() == 0) return;


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
                if (thumbnailpath != null) {
                    rootNode.addUriTast(thumbnailpath);//创建一个ftp任务 绑定他的按钮的背景图片
                }else{
                    log.i(TAG,"按钮不存在背景");
                }

                //循环得到子内容信息
                NodeList floder_item_List = folderElement.getElementsByTagName("item"); //子信息
                if (floder_item_List.getLength() == 0) return;

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
                        return; //网页
                    }

                    //资源路径
                    String filepath = XmlHelper.getFirstChildNodeValue(floder_item_Element, "filepath");//资源路径
                    rootNode.addUriTast(filepath); //创建一个ftp任务

                    //还有第一帧的图像
                    if (filetype.equals("1002")) {
                        filepath = XmlHelper.getFirstChildNodeValue(floder_item_Element, "video_image_uri");//视频第一帧路径
                        rootNode.addUriTast(filepath); //创建一个ftp任务
                    }
 //
                }
                log.d(TAG, "a interaction to end parse");
                break;
            case I_LAYOUT_PARSE ://布局
                ParseResultXml(ACTION_PARSE,result,obj);
                break;
        }








    }
}











