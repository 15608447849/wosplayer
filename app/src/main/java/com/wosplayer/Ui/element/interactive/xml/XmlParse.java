package com.wosplayer.Ui.element.interactive.xml;


import com.wosplayer.Ui.element.interactive.databeads.Abutton;
import com.wosplayer.Ui.element.interactive.databeads.Acontent;
import com.wosplayer.Ui.element.interactive.databeads.Afile;
import com.wosplayer.Ui.element.interactive.databeads.Alayout;

import com.wosplayer.app.AppUtils;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Created by user on 2016/6/23.
 */
public class XmlParse {

    private static final java.lang.String TAG = "XmlParse : ";//XmlParse.class.getName();

    /**
     * 解析  互动
     *  第一次
     *
     */
    public static Alayout interactionParse_one(String inter_Xml)throws Exception {
        InputStream inStream = new ByteArrayInputStream(inter_Xml.getBytes());
        // 创建saxReader对象
        SAXReader reader = new SAXReader();
        Document document = reader.read(inStream);
        //获取根节点元素对象
        Element interaction = document.getRootElement();
        //创建布局数据对象
        Alayout alayout = new Alayout();

        alayout.layoutUri = interaction.element(XML_NOTE.interactive_layouturl).getText();//布局地址
        alayout.folderurl = interaction.element(XML_NOTE.interactive_folderurl).getText();//文件夹地址
        alayout. thumbnailurl = interaction.element(XML_NOTE.interactive_thumbnailurl).getText();//封面图地址

        Element layout = interaction.element(XML_NOTE.interactive_layout);//<layout></layout>
        alayout.tag = layout.element(XML_NOTE.interactive_layout_id).getText();//布局标识
        alayout.thumbnail = layout.element(XML_NOTE.interactive_layout_thumbnailpath).getText();//封面图名字
        Element items = layout.element(XML_NOTE.interactive_layout_items);//<items></items>
        alayout.totalwidth =  Integer.parseInt(items.element(XML_NOTE.interactive_layout_items_totalwidth).getText());//布局宽
        alayout.totalheight =  Integer.parseInt(items.element(XML_NOTE.interactive_layout_items_totalheight).getText());//布局高
        alayout.bgType =  Integer.parseInt(items.element(XML_NOTE.interactive_layout_items_bgmode).getText());//布局背景类型
        alayout.bgValue = items.element(XML_NOTE.interactive_layout_items_bg).getText();//布局属性
        List item  =  items.elements(XML_NOTE.interactive_layout_items_item);
        // 多个 item 节点 (对应按钮)
        for (Iterator its = item.iterator(); its.hasNext(); ) {
            Element button = (Element) its.next();
            Abutton abutton = new Abutton();//创建一个按钮
            abutton.tag = button.element(XML_NOTE.interactive_layout_items_item_name).getText();//按钮标识
            abutton.x = Integer.parseInt(button.element(XML_NOTE.interactive_layout_items_item_xpos).getText());
            abutton.y = Integer.parseInt(button.element(XML_NOTE.interactive_layout_items_item_ypos).getText());
            abutton.width = Integer.parseInt(button.element(XML_NOTE.interactive_layout_items_item_width).getText());
            abutton.height = Integer.parseInt(button.element(XML_NOTE.interactive_layout_items_item_height).getText());
            abutton.bindType = button.element(XML_NOTE.interactive_layout_items_item_bindtype).getText();//下一级绑定类型
            abutton.nextTag = button.element(XML_NOTE.interactive_layout_items_item_bindid).getText();//绑定id
            alayout.buttonList.add(abutton);
        }
        return alayout;
    }


    /**
     * 解析出 互动 按钮  的 背景图
     * @param Xml
     * @return
     * @throws Exception
     */
    public static String interactionParse_ButtonSrcUri_FildType(String Xml)throws Exception {
        InputStream inStream = new ByteArrayInputStream(Xml.getBytes());
        // 创建saxReader对象
        SAXReader reader = new SAXReader();
        Document document = reader.read(inStream);
        //获取根节点元素对象
        Element interaction = document.getRootElement();
        Element folder  = interaction.element(XML_NOTE.interactive_folder);
        String uri = folder.elementText(XML_NOTE.interactive_folder_thumbnailpath);
        return uri;

    }

    /**
     * 按钮下面 是文件
     * @param Xml
     * @return     * @throws Exception
     */
    public static Afile interactionParse_Button_Item_View_FildType(String Xml)throws Exception {
        Afile afile = new Afile();
        InputStream inStream = new ByteArrayInputStream(Xml.getBytes());
        // 创建saxReader对象
        SAXReader reader = new SAXReader();
        Document document = reader.read(inStream);
        //获取根节点元素对象
        Element interaction = document.getRootElement();
        Element folder  = interaction.element(XML_NOTE.interactive_folder);//节点
        afile.tag = folder.elementText(XML_NOTE.interactive_folder_id);//id
        afile.thumbnailpath = folder.elementText(XML_NOTE.interactive_folder_thumbnailpath);//封面图
        afile.thumbnailpath = AppUtils.subLastString(afile.thumbnailpath,"/");
        //得到所有的item节点
        List items = folder.elements(XML_NOTE.interactive_folder_item);
        for (Iterator its = items.iterator(); its.hasNext(); ) {
            Element item =    (Element) its.next();
            Acontent acontent = new Acontent();
            //filetype
            acontent.type = item.elementText(XML_NOTE.interactive_folder_item_filetype);
            //filepath
            acontent.sourcePath = item.elementText(XML_NOTE.interactive_folder_item_filepath);
            //web_url
            acontent.web_url = item.elementText(XML_NOTE.interactive_folder_item_web_url);
            afile.contentList.add(acontent);
        }
        return afile;
    }

    public static String interactionParse_ButtonSrcUri_LayoutType(String Xml)throws Exception {
        InputStream inStream = new ByteArrayInputStream(Xml.getBytes());
        // 创建saxReader对象
        SAXReader reader = new SAXReader();
        Document document = reader.read(inStream);
        //获取根节点元素对象
        Element interaction = document.getRootElement();
        String thumbnailurl  = interaction.elementText(XML_NOTE.interactive_thumbnailurl); //前缀

        Element layout = interaction.element(XML_NOTE.interactive_layout);
        String filename = layout.elementText(XML_NOTE.interactive_layout_thumbnailpath);//具体文件名字

        String uri = thumbnailurl+filename;
        return uri;
    }




}