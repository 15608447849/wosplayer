package com.wosplayer.command.operation.schedules;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.wosplayer.Ui.performer.contentTanslater.ContentTypeEnum;
import com.wosplayer.app.AppTools;
import com.wosplayer.app.SystemConfig;
import com.wosplayer.command.operation.schedules.correlation.StringUtils;
import com.wosplayer.tool.SdCardTools;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.app.Logs;
import com.wosplayer.command.operation.schedules.correlation.XmlHelper;
import com.wosplayer.command.operation.schedules.correlation.XmlNodeEntity;
import com.wosplayer.command.operation.schedules.correlation.Xmlparse;
import com.wosplayer.command.operation.interfaces.iCommand;
import com.wosplayer.download.kernal.DownloadManager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lzp on 2016/7/20.
 * /*图片 视频 错误时 所需
 //图片
 String errImage = "http://e.hiphotos.baidu.com/zhidao/pic/item/9345d688d43f8794f8bb0d5bd61b0ef41bd53a7a.jpg";
 rootNode.addUriTast(errImage); //创建一个ftp任务
 //视频
 //String errVideo = "http://static.zqgame.com/html/playvideo.html?name=http://lom.zqgame.com/v1/video/LOM_Promo~2.flv";
 //rootNode.addUriTast(errVideo); //创建一个ftp任务*/

public class ScheduleSaver implements iCommand {

    private static final String TAG = "[排期解析保存]";

    private final int ROOT_PARSE = 11;
    private final int SCHEDU_PROG_PARSE = 12;
    private final int ACTION_PARSE = 13;
    private final int I_LAYOUT_PARSE = 14;
    private final int I_FILE_PARSE = 15;

    private static ReentrantLock lock = new ReentrantLock();
    private static XmlNodeEntity rootNode = new XmlNodeEntity();//只存在一个
    private boolean isNotify;
    /**
     * 序列化排期
     */
    private static void Serialize() {
        rootNode.SettingNodeEntitySave();
    }
    //清理
    private static void clear(){
        rootNode.Clear();
        Logs.e(TAG,"清理排期存储中");
    }

    /**
     * 执行
     *
     * @param param
     */
    @Override
    public void execute(Activity activity, String param) {

        if (param.equals("_notify")){
            //通知更新排期 刷新界面
            ScheduleReader.notifySchedule();
        }else {
            //Logs.i(TAG,"解析 排期 信息, 当前线程名:"+Thread.currentThread().getName());
            parseData(param);
        }
    }
    /**
     * 存储数据
     */
    private void parseData(String uri) {
        try {
            lock.lock();
            Logs.i(TAG," 排期链接地址: "+ uri);
            startWork(uri);
        } catch (Exception e) {
            Logs.e(TAG, "saveData() " + e.getMessage());
        }finally {
            lock.unlock();
        }
    }

    private void startWork(final String uri){

        try {
            isNotify = true;
            getXMLdata(uri,ROOT_PARSE,null); //解析数据
            //序列化数据
            Serialize();
            Logs.i(TAG,"执行排期数据解析,序列化保存完成");
        } catch (Exception e) {
            Logs.e(TAG,"解析排期失败: "+e.getMessage());
            return;
        }
        SystemConfig config = SystemConfig.get();
        String saveDir = config.GetStringDefualt("basepath","");
        String limits = config.GetStringDefualt("storageLimits","50");
        String telminalNo = config.GetStringDefualt("terminalNo","");
        //判断是否清理数据
        if( SdCardTools.justFileBlockVolume(saveDir,limits) ){
            SdCardTools.clearTargetDir(saveDir,rootNode.getFtplist());
        }

        if (isNotify){
            Logs.i(TAG,"执行排期读取,界面刷新.");
            //执行 数据读取者 界面刷新
            ScheduleReader.notifySchedule();
        }
        //开启后台下载线程
        if (rootNode.getFtplist().size()>0){
            Logs.i(TAG,"当前的任务数:"+rootNode.getFtplist().size()+"\n "+rootNode.getFtplist().toString());
            sendloadTask(saveDir,telminalNo,isNotify);
        }
    }


    /**
     * 获取xml 数据
     * @param uri
     * @param callType
     * @param Obj
     */
    private void getXMLdata(String uri, int callType,Object Obj) {
        String result = AppTools.uriTranslationXml(uri);
        if (result==null){
            Logs.e(TAG,"getXMLdata() 返回值不存在");
            return;
        }
        parseXml(callType,result, Obj);
    }
    /**
     * 通知资源开始下载
     */
    private void sendloadTask(String sourceSavePath,String telminalNo,boolean isNotify) {
        ArrayList<CharSequence> tasklist = new ArrayList<CharSequence>();
        for (int i = 0; i<rootNode.getFtplist().size();i++){
            tasklist.add(rootNode.getFtplist().get(i));
        }

        Intent intent = new Intent(PlayApplication.appContext, DownloadManager.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putInt(DownloadManager.KEY_TYPE, DownloadManager.KEY_TYPE_SCHDULE);
        bundle.putCharSequenceArrayList(DownloadManager.KEY_TASK_LIST,tasklist);
        bundle.putString(DownloadManager.KEY_TERMINAL_NUM, telminalNo);
        bundle.putString(DownloadManager.KEY_SAVE_PATH, sourceSavePath);
        bundle.putBoolean(DownloadManager.KEY_TASK_NOTYFY_SCHEDULE,isNotify);
        intent.putExtras(bundle);
        PlayApplication.appContext.startService(intent);
    }

    /**
     * 解析 result
     */
    private void parseXml(int callType, String result, Object obj) { //返回的xml数据
        switch (callType) {
            case ROOT_PARSE://root xml
                parseSchduler(result);
                break;
            case SCHEDU_PROG_PARSE://解析 一个排期下的其中一个节目
                parseProgram(result, (XmlNodeEntity) obj);
                break;
            case ACTION_PARSE:
                parseAction(result, (XmlNodeEntity) obj);
                break;
            case I_FILE_PARSE://文件
                parseActionOnFileType(result, (XmlNodeEntity) obj);
                break;
            case I_LAYOUT_PARSE ://布局
                parseXml(ACTION_PARSE,result,obj);
                break;
        }
    }

    /**
     * 解析排期
     */
    private void parseSchduler(String scheduleXmlData) {
        Element root = XmlHelper.getDomXml(new ByteArrayInputStream(scheduleXmlData.getBytes()));
        if (root == null) return;
        rootNode.Level="root";//设置等级
            String preUrl = XmlHelper.getFirstChildNodeValue(root, "url");
            if (preUrl.equals("")) throw new IllegalStateException("不可执行的排期,获取'节目数据'的前缀url空值");
            String cuuks = SystemConfig.get().read().GetStringDefualt("uuks","");//当前执行中的uuks
            String ruuks = XmlHelper.getFirstChildNodeValue(root, "uuks");
            Logs.i(TAG,"[  当前播放的数据标号:"+cuuks+" - 最新数据标号:"+ruuks+"  ]");
            if (StringUtils.isEmpty(ruuks)) throw new IllegalStateException("不可识别的排期标识uuks空值");
           if(cuuks.equals(ruuks)) {
               //设置保存排期xml,读取排期 的标识 - false;
               isNotify = false;
           }
            clear(); //清理保存的排期

        //排期节点列表
        NodeList scheduleList = root.getElementsByTagName("schedule");
        if (scheduleList==null || scheduleList.getLength() == 0) return;
        parseSchdulerOnProgram(preUrl,ruuks, scheduleList);
        Logs.i(TAG,"===========================解析完成===========================");
    }

    private void parseSchdulerOnProgram(String preUrl,String uuks, NodeList scheduleList) {
        Logs.i(TAG,"当前排期总数: "+scheduleList.getLength());
        for (int i = scheduleList.getLength() - 1; i > -1; i--) {
            Element scheduleElement = (Element) scheduleList.item(i);
            String schedule_XmlData = XmlHelper.getNodeToString(scheduleList.item(i));
            HashMap<String, String> schedule_xmldata_map = Xmlparse.ParseXml("/schedule", schedule_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);//排期
            XmlNodeEntity schedule_node = rootNode.NewSettingNodeEntity();//添加一个子节点
            schedule_node.Level = "root_schedule";
            schedule_node.AddPropertyList(schedule_xmldata_map);
            schedule_node.AddProperty("url", preUrl);
            schedule_node.AddProperty("uuks", uuks);
            NodeList programsList = scheduleElement.getElementsByTagName("program"); //节目
            if(programsList==null || programsList.getLength()==0) continue;

            Logs.i(TAG,"排期id: "+schedule_xmldata_map.get("id")+";排期类型: " + schedule_xmldata_map.get("type")+";排期名: "+schedule_xmldata_map.get("summary")+
                    "这个排期下面的 节目总数: "+programsList.getLength());
            String progUri = null;
            for (int i1 = 0; i1 < programsList.getLength(); i1++) {
                Element program_Element = (Element) programsList.item(i1);
                String pId = XmlHelper.getFirstChildNodeValue(program_Element, "id");
                if (pId.equals("")) continue;

                progUri = preUrl.trim() + pId.trim();
                Log.i(TAG, "节目连接地址 : " + progUri);
                //转换节目url->xml
                getXMLdata(progUri, SCHEDU_PROG_PARSE, schedule_node.NewSettingNodeEntity());// 下一节点 实体
              //  Logs.i(TAG,"解析完一个节目\n\r");
            }
          //  Logs.i(TAG,"解析完一个排期\n\r");
        }
    }

    /**
     * 解析节目
     */
    private void parseProgram(String program_root_XmlData, XmlNodeEntity obj) {
  //      Logs.i(TAG,"开始解析一个节目");
//                Logs.i(TAG,program_root_XmlData);
        Element program_root = XmlHelper.getDomXml(new ByteArrayInputStream(program_root_XmlData.getBytes()));
        if (program_root == null) return;
        Element programElement = (Element) program_root.getElementsByTagName("programmes").item(0);
        if (programElement == null) return;
        try {
            String progBgImageUrl = programElement.getElementsByTagName("bgimage").item(0).getAttributes().getNamedItem("src").getNodeValue();
            //Logs.i(TAG,"存在背景图片 - url : "+ progBgImageUrl);
            rootNode.addUriTast(progBgImageUrl);
        } catch (Exception e) {
        }
        String program_XmlData = XmlHelper.getNodeToString(program_root);
        HashMap<String, String> program_xmldataMap = Xmlparse.ParseXml("/root/programmes", program_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);
        XmlNodeEntity program_node = obj;
        program_node.Level = "root_schedule_programs";
        program_node.AddPropertyList(program_xmldataMap);

        if (program_xmldataMap.containsKey("src")) {
            String url = program_xmldataMap.get("src").trim();
            rootNode.addUriTast(url); //创建一个ftp任务
        }
        NodeList program_layout_List = programElement.getElementsByTagName("layout");
        if (program_layout_List==null || program_layout_List.getLength()==0 ) return;
        //解析布局
        Logs.i(TAG,"节目下面的 布局 总数:"+program_layout_List.getLength());
        for (int i = 0; i < program_layout_List.getLength(); i++) {  //节目下面的布局循环
            Element layout_Element = (Element) program_layout_List.item(i);
            if (layout_Element==null) continue;
            String layout_XmlData = XmlHelper.getNodeToString(layout_Element);
            HashMap<String, String> layout_xmldataMap = Xmlparse.ParseXml("/layout", layout_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);
            XmlNodeEntity program_layout_node = program_node.NewSettingNodeEntity();
            program_layout_node.Level = "root_schedule_programs_layout";
            program_layout_node.AddPropertyList(layout_xmldataMap);

            NodeList layout_contentList = layout_Element.getElementsByTagName("contents");//内容
            if(layout_contentList==null || layout_contentList.getLength()==0) continue;
            Logs.i(TAG,"节目下面的 布局 的 内容总数:"+layout_contentList.getLength());
            for (int j = 0; j < layout_contentList.getLength(); j++) {//循环 节目_布局_内容
                Element content_Element = (Element) layout_contentList.item(j);
                if (content_Element==null) continue;
                String content_XmlData = XmlHelper.getNodeToString(content_Element);
                HashMap<String, String> content_xmlDataMap = Xmlparse.ParseXml("/contents", content_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);
                XmlNodeEntity layout_content_Node = program_layout_node.NewSettingNodeEntity();
                layout_content_Node.Level = "root_schedule_programs_layout_content";
                layout_content_Node.AddPropertyList(content_xmlDataMap);

                //内容类型
                String content_type = XmlHelper.getFirstChildNodeValue(content_Element, "fileproterty");
                Logs.i(TAG,"当前解析的 内容类型 : "+content_type);
                ContentTypeEnum contentType;
                try {
                    contentType = ContentTypeEnum.valueOf(content_type);
                } catch (IllegalArgumentException e) {
                    Logs.e(TAG, "内容类型错误,未知类型:" + e.getMessage());
                    continue;
                }
                if (contentType.equals(ContentTypeEnum.time)){
                    String text_xml=  XmlHelper.getFirstChildToString(content_Element, "getcontents");
                    HashMap<String, String> text_xmlDataMap = Xmlparse.ParseXml("/getcontents", text_xml, Xmlparse.parseType.OnlyLeaf).get(0);
                    if(text_xmlDataMap==null || text_xmlDataMap.size()==0) continue;
                    XmlNodeEntity layout_content_text_Node = layout_content_Node.NewSettingNodeEntity();
                    layout_content_text_Node.Level = "root_schedule_programs_layout_content_time";
                    layout_content_text_Node.AddPropertyList(text_xmlDataMap);
                    Logs.i(TAG,"time类型解析完成");
                }
                else if (contentType.equals(ContentTypeEnum.text))//文本
                {
                    String text_xml=  XmlHelper.getFirstChildToString(content_Element, "getcontents");
                    HashMap<String, String> text_xmlDataMap = Xmlparse.ParseXml("/getcontents", text_xml, Xmlparse.parseType.OnlyLeaf).get(0);
                   if(text_xmlDataMap==null || text_xmlDataMap.size()==0) continue;
                    XmlNodeEntity layout_content_text_Node = layout_content_Node.NewSettingNodeEntity();
                    layout_content_text_Node.Level = "root_schedule_programs_layout_content_text";
                    layout_content_text_Node.AddPropertyList(text_xmlDataMap);

                    Logs.i(TAG,"text类型解析完成");
                } else if (contentType.equals(ContentTypeEnum.rss)) {
                    Element childfile = (Element) content_Element.getElementsByTagName("childfile").item(0);
                    String xfile = XmlHelper.getFirstChildNodeValue(childfile, "xfile");
                    String rss_programXmlData = XmlHelper.getXmlDataFromUrl(xfile);
                    if (rss_programXmlData.equals("")) continue;
                    Element rssRoot = XmlHelper.getDomXml(new ByteArrayInputStream(rss_programXmlData.getBytes()));
                    if (rssRoot==null) continue;
                    HashMap<String, String> rss_data_map = Xmlparse.ParseXml("/rss", rss_programXmlData, Xmlparse.parseType.OnlyLeaf).get(0);
                    content_xmlDataMap.putAll(rss_data_map);
                    Logs.i(TAG,"rss类型解析完成");
                } else if (contentType.equals(ContentTypeEnum.interactive)) {//互动
                    String url = XmlHelper.getFirstChildNodeValue(content_Element, "getcontents").trim(); //资源地址 或者 xml数据
                    //再解析 继续解析
                    Logs.i(TAG,"互动文件链接url : "+ url);
                    XmlNodeEntity interaction = layout_content_Node.NewSettingNodeEntity();
                    interaction.AddProperty("homeurl",url);
                    getXMLdata(url, ACTION_PARSE,interaction );//进入互动
                    Logs.i(TAG,"interactive类型解析完成");
                } else if (contentType.needsDown()){
                    String resourceUrl = XmlHelper.getFirstChildNodeValue(content_Element, "getcontents").trim(); //资源地址 或者 xml数据
                    rootNode.addUriTast(resourceUrl);
                }
            }
        }
    }

    /**
     * 解析互动 或者 互动布局类型
     */
    private void parseAction(String interaction_XmlData, XmlNodeEntity obj) {
        //互动
        Logs.i(TAG,"  - -开始一个互动解析 - -");
        Element interaction_root = XmlHelper.getDomXml(new ByteArrayInputStream(interaction_XmlData.getBytes()));
        if (interaction_root == null) return;
        String layouturl =  XmlHelper.getFirstChildNodeValue(interaction_root, "layouturl");
        String folderurl =  XmlHelper.getFirstChildNodeValue(interaction_root, "folderurl");
        String thumbnailurl = XmlHelper.getFirstChildNodeValue(interaction_root, "thumbnailurl");
        try {
            if (layouturl.isEmpty() || folderurl.isEmpty() || thumbnailurl.isEmpty()) return;
        } catch (Exception e) {
            return;
        }
        Element action_layoutElement = (Element) interaction_root.getElementsByTagName("layout").item(0);
        if (action_layoutElement == null) return;
        String action_layout_XmlData = XmlHelper.getNodeToString(action_layoutElement);
        HashMap<String, String> layout_xmldataMap = Xmlparse.ParseXml("/layout", action_layout_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);
        XmlNodeEntity interaction_layout_node = obj;//
        interaction_layout_node.Level = "interaction_layout";
        interaction_layout_node.AddPropertyList(layout_xmldataMap);
        interaction_layout_node.AddProperty("layouturl", layouturl);
        interaction_layout_node.AddProperty("folderurl", folderurl);
        interaction_layout_node.AddProperty("thumbnailurl", thumbnailurl);
        //获取 缩略图 的地址
        String layout_thumbnailpath = XmlHelper.getFirstChildNodeValue(action_layoutElement, "thumbnailpath");//封面图片文件名，完整路径需加前缀
        if (layout_thumbnailpath==null || layout_thumbnailpath.equals("")) {
            Logs.e(TAG,"点击进入这个互动布局的按钮的略缩图解析错误,缩略图名不存在");
           // String errImageBgUri = "http://imgsrc.baidu.com/forum/w%3D580/sign=55460f6a367adab43dd01b4bbbd5b36b/269759ee3d6d55fb1fa4ca646d224f4a20a4dd16.jpg";
            //rootNode.addUriTast(errImageBgUri);
        }else{
            String bgmode_uri = thumbnailurl + layout_thumbnailpath;
            //创建ftp
            rootNode.addUriTast(bgmode_uri); //创建一个ftp任务
        }
        //ad广告
        Element adElement = (Element) action_layoutElement.getElementsByTagName("ad").item(0);
        if (adElement != null) {
            String ad_myXmlData = XmlHelper.getNodeToString(adElement);
            HashMap<String, String> ad_xmldataList = Xmlparse.ParseXml("/ad", ad_myXmlData, Xmlparse.parseType.OnlyLeaf).get(0);
            XmlNodeEntity interaction_layout_ad_node = interaction_layout_node;
            interaction_layout_ad_node.AddPropertyList(ad_xmldataList);
            //收集广告文件夹信息
            String bindid = ad_xmldataList.get("bindid");
            String adUrl = folderurl+bindid;
            getXMLdata(adUrl, I_FILE_PARSE, interaction_layout_ad_node.NewSettingNodeEntity());
        }
        //items 包含了 布局的东西
        Element layout_ItemsElement = (Element) action_layoutElement.getElementsByTagName("items").item(0);
        if (layout_ItemsElement == null) return;
        String layout_Items_XmlData = XmlHelper.getNodeToString(layout_ItemsElement);
        HashMap<String, String> layout_items_xmldatamap = Xmlparse.ParseXml("/items", layout_Items_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);
        interaction_layout_node.AddPropertyList(layout_items_xmldatamap);
        XmlNodeEntity interaction_layout_items_node = interaction_layout_node;//.NewSettingNodeEntity();//###
        String bgmode = XmlHelper.getFirstChildNodeValue(layout_ItemsElement, "bgmode");//布局背景图

        if (bgmode.equals("2")) {
            String bg = XmlHelper.getFirstChildNodeValue(layout_ItemsElement, "bg");
            if (bg == null || bg.equals("")) {
                Logs.e(TAG,"一个互动布局背景解析错误:背景图片名不存在") ;
//                        String layoutBgerr = "http://cdn.duitang.com/uploads/item/201205/07/20120507220903_VR22y.thumb.600_0.jpeg";
//                        rootNode.addUriTast(layoutBgerr); //创建一个ftp任务
            }else{
                String bgmode_uri = thumbnailurl + bg;
                //创建ftp
                rootNode.addUriTast(bgmode_uri); //创建一个ftp任务
            }
        }
        //循环得到按钮的信息
        NodeList items_itemNodeList = layout_ItemsElement.getElementsByTagName("item"); //子按钮
        if (items_itemNodeList.getLength() == 0) return;
        Logs.i(TAG,"一个互动布局layout下面的 按钮总数:"+items_itemNodeList.getLength());
        for (int i1 = 0; i1 < items_itemNodeList.getLength(); i1++) {
            Element item_Element = (Element) items_itemNodeList.item(i1);
            String Item_XmlData = XmlHelper.getNodeToString(item_Element);
            HashMap<String, String> item_xmldataMap = Xmlparse.ParseXml("/item", Item_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);
            XmlNodeEntity interaction_layout_items_item_node = interaction_layout_items_node.NewSettingNodeEntity();
            interaction_layout_items_item_node.Level = "interaction_layout_items_item";
            interaction_layout_items_item_node.AddPropertyList(item_xmldataMap);

            //再次访问网络获取xml文件
            String uri_Id = XmlHelper.getFirstChildNodeValue(item_Element, "bindid");
            if (uri_Id.equals("")) continue;

            String bindtype = XmlHelper.getFirstChildNodeValue(item_Element, "bindtype");
            int bindtype_id = -1;
            try {
                bindtype_id = Integer.parseInt(bindtype);
            } catch (Exception e) {
                continue;
            }
            String item_uri = null;
            if (bindtype_id == 0 || bindtype_id == 2) {  //布局类型 0 2
                item_uri = layouturl + uri_Id;
                Logs.i(TAG, "一个子 互动布局类型 url :" + item_uri);
                getXMLdata(item_uri, I_LAYOUT_PARSE, interaction_layout_items_item_node.NewSettingNodeEntity());
            } else if (bindtype_id == 1 || bindtype_id == 3) { // 文件1 3网页
                item_uri = folderurl + uri_Id;
                Logs.i(TAG, "一个子 互动文件类型- url :" + item_uri);
                getXMLdata(item_uri, I_FILE_PARSE, interaction_layout_items_item_node.NewSettingNodeEntity());
            }
        }
    }

    /**
     * 解析互动下文件夹类型
     * @param obj
     */
    private void parseActionOnFileType(String interaction_fl_XmlData, XmlNodeEntity obj) {
        Logs.i(TAG,"开始解析一个文件");
        Element folder_root = XmlHelper.getDomXml(new ByteArrayInputStream(interaction_fl_XmlData.getBytes()));
        if (folder_root == null) return;
        Element folderElement = (Element) folder_root.getElementsByTagName("folder").item(0);//wenjian yuansu
        if (folderElement == null) return;
        String folder_XmlData = XmlHelper.getNodeToString(folderElement);
        HashMap<String, String> folder_xmldataList = Xmlparse.ParseXml("/folder", folder_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);
        XmlNodeEntity interaction_layout_items_item_folder_node = obj;//父节点实体
        interaction_layout_items_item_folder_node.Level = "interaction_layout_items_item_folder";
        interaction_layout_items_item_folder_node.AddPropertyList(folder_xmldataList);

        //创建ftp对象
        String thumbnailpath = XmlHelper.getFirstChildNodeValue(folderElement, "thumbnailpath");//封面图
        if (thumbnailpath != null && !thumbnailpath.equals("")) {
            rootNode.addUriTast(thumbnailpath);//创建一个ftp任务 绑定他 的 按钮的封面图片
        }else{
            Logs.i(TAG,"点击进入这个互动文件夹的按钮不存在缩略图");
            //String layoutBgerr = "http://cdn.duitang.com/uploads/item/201205/07/20120507220903_VR22y.thumb.600_0.jpeg";
            //rootNode.addUriTast(layoutBgerr); //创建一个ftp任务
        }
        //循环得到子内容信息
        NodeList floder_item_List = folderElement.getElementsByTagName("item"); //子信息
        if (floder_item_List==null || floder_item_List.getLength() == 0) return;
        Logs.i(TAG,"这个文件夹 包含的内容的总数:"+floder_item_List.getLength());
        for (int i1 = 0; i1 < floder_item_List.getLength(); i1++) {
            Element floder_item_Element = (Element) floder_item_List.item(i1);
            String floder_Item_XmlData = XmlHelper.getNodeToString(floder_item_Element);
            HashMap<String, String> item_xml_dataMap = Xmlparse.ParseXml("/item", floder_Item_XmlData, Xmlparse.parseType.OnlyLeaf).get(0);
            XmlNodeEntity interaction_layout_items_item_floder_item_node = interaction_layout_items_item_folder_node.NewSettingNodeEntity();
            interaction_layout_items_item_floder_item_node.Level = "interaction_layout_items_item_floder_item";
            interaction_layout_items_item_floder_item_node.AddPropertyList(item_xml_dataMap);

            //文件内容类型
            String filetype = XmlHelper.getFirstChildNodeValue(floder_item_Element, "filetype");
            if (filetype.equals("1006")) {
                Logs.i(TAG,"互动资源 - 网页资源类型");
                continue; //网页
            }
            //资源路径
            String filepath = XmlHelper.getFirstChildNodeValue(floder_item_Element, "filepath");//资源路径
            if(filepath.equals("")||filepath.equals("null")){
                Logs.e(TAG,"互动资源 - 文件夹内容资源路径不存在");
                continue;
            }else{
                rootNode.addUriTast(filepath); //创建一个ftp任务
            }
            //还有第一帧的图像
            if (filetype.equals("1002")) {
                filepath = XmlHelper.getFirstChildNodeValue(floder_item_Element, "video_image_url");//视频第一帧路径
                if(filepath.equals("")||filepath.equals("null")){

                    filepath="http://img15.3lian.com/2015/f1/59/d/31.jpg";
                    Logs.e("视频第一帧不存在,下载默认图片:"+ filepath);
                }else {
                    String  filename = filepath.substring(filepath.lastIndexOf("/")+1);
                    if (filename.equals("null")){
                        filepath="http://img15.3lian.com/2015/f1/59/d/31.jpg";
                        Logs.e("视频第一帧不存在,下载默认图片:"+ filepath);
                    }
                }
                rootNode.addUriTast(filepath); //创建一个ftp任务
            }
//
        }
        Logs.i(TAG, "互动解析完成");
    }

}











