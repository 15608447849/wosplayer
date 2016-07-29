package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.xml;

/**
 * Created by user on 2016/6/23.
 */
public class XML_NOTE {
    public static final String root = "root";
    public static final String root_url = "url";
    public static final String root_schedule = "schedule";
    public static final String root_schedule_id = "id"; //1466580521298
    public static final String root_schedule_type = "type"; //1 <!-- 排期类型，5为默认，1为轮播，2为点播，3为重复，4为插播 -->

    public static final String root_schedule_termtype = "termtype"; //4
    public static final String root_schedule_clientid = "clientid";//1063
    public static final String root_schedule_clientname = "clientname";//TERM1063
    public static final String root_schedule_summary = "summary";
    public static final String root_schedule_color = "color";
    public static final String root_schedule_description = "description";
    public static final String root_schedule_modifydt = "modifydt";//2016-06-22 15:28:52
    public static final String root_schedule_startcron = "startcron";//2016-06-22 00:00:00
    public static final String root_schedule_endcron = "endcron";//2016 06 24 23:59:00
    public static final String root_schedule_cronexpression = "cronexpression";
    public static final String root_schedule_executeexpression = "executeexpression";
    public static final String root_schedule_allday = "allday";
    public static final String root_schedule_timelength = "timelength";
    public static final String root_schedule_start = "start";
    public static final String root_schedule_end = "end";
    public static final String root_schedule_oldStart = "oldStart";
    public static final String root_schedule_oldEnd = "oldEnd";
    public static final String root_schedule_program = "program";
    public static final String root_schedule_program_id = "id";//1054
    public static final String root_schedule_program_name = "name";//1920*1080-25252
    public static final String root_schedule_program_description = "description";



    public static final String root_programmes = "programmes";
    public static final String root_programmes_id = "id";
    public static final String root_programmes_thumbnailimage = "thumbnailimage"; //短文图片
    public static final String root_programmes_title = "title";
    public static final String root_programmes_width = "width";
    public static final String root_programmes_height = "height";
    public static final String root_programmes_fullimage = "fullimage";
    public static final String root_programmes_starttime = "starttime";
    public static final String root_programmes_endtime = "endtime";
    public static final String root_programmes_type = "type";
    public static final String root_programmes_bgcolor = "bgcolor";
    public static final String root_programmes_layout = "layout";
    public static final String root_programmes_layout_id = "id";
    public static final String root_programmes_layout_layoutname = "layoutname";
    public static final String root_programmes_layout_width = "width";
    public static final String root_programmes_layout_height = "height";
    public static final String root_programmes_layout_x = "x";
    public static final String root_programmes_layout_y = "y";
    public static final String root_programmes_layout_locked = "locked";
    public static final String root_programmes_layout_ontop = "ontop";
    public static final String root_programmes_layout_type = "type";
    public static final String root_programmes_layout_contents = "contents";
    public static final String root_programmes_layout_contents_id = "id";
    public static final String root_programmes_layout_contents_fileproterty = "fileproterty";
    public static final String root_programmes_layout_contents_playtimes = "playtimes";
    public static final String root_programmes_layout_contents_timelength = "timelength";
    public static final String root_programmes_layout_contents_fsize = "fsize";
    public static final String root_programmes_layout_contents_contentsname = "contentsname";
    public static final String root_programmes_layout_contents_contentsnewname = "contentsnewname";
    public static final String root_programmes_layout_contents_getcontents = "getcontents";







    public static final String interactive= "interactive";
    /**
     * 布局URL
     */
    public static final String interactive_layouturl= "layouturl";
    /**
     *  文件夹URL
     */
    public static final String interactive_folderurl= "folderurl";
    /**
     * 缩略图地址，背景图片地址前缀
     */
    public static final String interactive_thumbnailurl= "thumbnailurl";
    public static final String interactive_layout= "layout";
    public static final String interactive_layout_id= "id";
    public static final String interactive_layout_name= "name";

    /**
     * 封面图片文件名，完整路径需加前缀
     */
    public static final String interactive_layout_thumbnailpath= "thumbnailpath";
    /**
     * 互动模块 参数
     */
    public static final String interactive_layout_items= "items";
    public static final String interactive_layout_items_totalwidth= "totalwidth";
    public static final String interactive_layout_items_totalheight= "totalheight";
    /**
     * 背景类型，1纯色，2图片
    */
    public static final String interactive_layout_items_bgmode= "bgmode";
    /**
     * 背景图片文件名，完整路径需加前缀
     */
    public static final String interactive_layout_items_bg= "bg";
    public static final String interactive_layout_items_item= "item"; //按钮
    public static final String interactive_layout_items_item_name= "name";
    public static final String interactive_layout_items_item_xpos= "xpos";
    public static final String interactive_layout_items_item_ypos= "ypos";
    public static final String interactive_layout_items_item_width= "width";
    public static final String interactive_layout_items_item_height= "height";
    /**
     * 绑定类型，0 2 为排版，1为文件夹,3网页
     */
    public static final String interactive_layout_items_item_bindtype= "bindtype";
    public static final String interactive_layout_items_item_bindid= "bindid"; //按钮 绑定的 视图
    public static final String interactive_layout_items_item_layoutType= "layoutType"; //打开范围，可以是full全屏，parent父级区域和custom自定义





    public static final String interactive_folder= "folder";
    public static final String interactive_folder_id= "id";
    public static final String interactive_folder_name= "name";
    /**
     * 文件夹封面图片地址
     */
    public static final String interactive_folder_thumbnailpath= "thumbnailpath";
    /**
     * 文件夹类型，2广告，5办公文档，1普通，3网站
     */
    public static final String interactive_folder_type= "type";
    public static final String interactive_folder_item= "item";//文件
    public static final String interactive_folder_item_filetype= "filetype";//1002视频，1007图片,1001FLASH 1006网页
    /**
     * 资源 文件路径
     */
    public static final String interactive_folder_item_filepath= "filepath";
    public static final String interactive_folder_item_web_url= "web_url";//若文件夹类型是网站，则此处为地址
    public static final String interactive_folder_item_script1= "script1";//若类型为办公文档，此为多个图片的名称，中间用英文逗号隔开
    public static final String interactive_folder_item_video_image_url = "video_image_url";//视频第一帧地址











































































}
