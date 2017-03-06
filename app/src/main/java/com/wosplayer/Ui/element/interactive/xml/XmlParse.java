package com.wosplayer.Ui.element.interactive.xml;


import android.content.Context;

import com.wosplayer.Ui.element.interactive.beads.ButtonActive;
import com.wosplayer.Ui.element.interactive.beads.FileActive;
import com.wosplayer.Ui.element.interactive.beads.LayoutActive;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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
    public static LayoutActive interactionParse_one(Context context, String inter_Xml)throws Exception {
        InputStream inStream = new ByteArrayInputStream(inter_Xml.getBytes());
        // 创建saxReader对象
        SAXReader reader = new SAXReader();
        Document document = reader.read(inStream);
        //获取根节点元素对象
        Element interaction = document.getRootElement();
        String layoutUri = interaction.element(XML_NOTE.interactive_layouturl).getText();
        String folderurl = interaction.element(XML_NOTE.interactive_folderurl).getText();
        String thumbnailurl = interaction.element(XML_NOTE.interactive_thumbnailurl).getText();

        Element layout = interaction.element(XML_NOTE.interactive_layout);
        String layoutId = layout.element(XML_NOTE.interactive_layout_id).getText();

        Element items = layout.element(XML_NOTE.interactive_layout_items);
        int layout_w =  Integer.parseInt(items.element(XML_NOTE.interactive_layout_items_totalwidth).getText());
        int layout_h =  Integer.parseInt(items.element(XML_NOTE.interactive_layout_items_totalheight).getText());
        int layout_bgtye =  Integer.parseInt(items.element(XML_NOTE.interactive_layout_items_bgmode).getText());
        String bgname = items.element(XML_NOTE.interactive_layout_items_bg).getText();

        List item  =  items.elements(XML_NOTE.interactive_layout_items_item);
        List<ButtonActive> layoutSubs =
        Collections.synchronizedList(new ArrayList<ButtonActive>());

        for (Iterator its = item.iterator(); its.hasNext(); ) {
            Element button = (Element) its.next();

            int bx = Integer.parseInt(button.element(XML_NOTE.interactive_layout_items_item_xpos).getText());
            int by = Integer.parseInt(button.element(XML_NOTE.interactive_layout_items_item_ypos).getText());
            int bw = Integer.parseInt(button.element(XML_NOTE.interactive_layout_items_item_width).getText());
            int bh = Integer.parseInt(button.element(XML_NOTE.interactive_layout_items_item_height).getText());
            int bindType = Integer.parseInt(button.element(XML_NOTE.interactive_layout_items_item_bindtype).getText());

            String bindId = button.element(XML_NOTE.interactive_layout_items_item_bindid).getText();

            Logs.i(TAG," 获取 到一个 按钮  bindtype=>"+bindType+"绑定的 id"+ bindId );
            ButtonActive buttonActive = new ButtonActive(context,
                    bx,
                    by,
                    bw,
                    bh,
                    bindType,
                    bindId,
                    layoutUri,
                    folderurl);

            layoutSubs.add(buttonActive);
        }

        //创建 层布局对象
        LayoutActive inter_layout  = new LayoutActive(context,
                thumbnailurl,
                layoutId,
                layout_w,
                layout_h,
                layout_bgtye,
                bgname,
                layoutSubs);
    return inter_layout;
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
    public static List<FileActive> interactionParse_Button_Item_View_FildType(String Xml)throws Exception {
        List<FileActive> arr = Collections.synchronizedList(new ArrayList<FileActive>());
        InputStream inStream = new ByteArrayInputStream(Xml.getBytes());
        // 创建saxReader对象
        SAXReader reader = new SAXReader();
        Document document = reader.read(inStream);
        //获取根节点元素对象
        Element interaction = document.getRootElement();
        Element folder  = interaction.element(XML_NOTE.interactive_folder);
        //得到type
        String type = folder.elementText(XML_NOTE.interactive_folder_type);


        //得到所有的item节点
        List items = folder.elements(XML_NOTE.interactive_folder_item);
        for (Iterator its = items.iterator(); its.hasNext(); ) {
            Element item =    (Element) its.next();

            //filetype
            String filetype = item.elementText(XML_NOTE.interactive_folder_item_filetype);
            //filepath
            String filepath = item.elementText(XML_NOTE.interactive_folder_item_filepath);
            //web_url
            String web_url = item.elementText(XML_NOTE.interactive_folder_item_web_url);

            String m_video_image_url = item.elementText(XML_NOTE.interactive_folder_item_video_image_url);

            FileActive file = new FileActive();

            file.type = type==null? Integer.parseInt(filetype) : Integer.parseInt(type);
            file.filetype = Integer.parseInt(filetype);
            file.filepath  = web_url==null || web_url.equals("") ?filepath:web_url;
            file.video_image_url = m_video_image_url;
            Logs.i(TAG,"解析 button 下面 文件 的 xml type=>"+type+" filetype:"+filetype+"filepath:"+filepath);
            arr.add(file);
        }
        return arr;
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