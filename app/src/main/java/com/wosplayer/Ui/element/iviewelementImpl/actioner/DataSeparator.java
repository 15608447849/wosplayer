package com.wosplayer.Ui.element.iviewelementImpl.actioner;

import com.wosplayer.app.DataList;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.Schedule.correlation.XmlNodeEntity;

import java.util.ArrayList;

/**
 * Created by user on 2016/8/3.
 */
public class DataSeparator {

    public static final  String layoutLevel = "interaction_layout";
    public static final String layoutItemLevel = "interaction_layout_items_item";
    public static final String folderLevel = "interaction_layout_items_item_folder";
    public static final String folderItemLevel = "interaction_layout_items_item_floder_item";
    private static String sd_path  = wosPlayerApp.config.GetStringDefualt("basepath","");

    public  static  String errImageName = "9345d688d43f8794f8bb0d5bd61b0ef41bd53a7a.jpg";//http://e.hiphotos.baidu.com/zhidao/pic/item/9345d688d43f8794f8bb0d5bd61b0ef41bd53a7a.jpg
    public  static  String videoNotFount = "LOM_Promo~2.flv";//http://static.zqgame.com/html/playvideo.html?name=http://lom.zqgame.com/v1/video/LOM_Promo~2.flv
    public  static  String videoFristImageNotFount = "31.jpg";//http://img15.3lian.com/2015/f1/59/d/31.jpg
    public  static  String buttonBgerr = "269759ee3d6d55fb1fa4ca646d224f4a20a4dd16.jpg";//
    public static String layoutBgerr = "20120507220903_VR22y.thumb.600_0.jpeg";//http://cdn.duitang.com/uploads/item/201205/07/20120507220903_VR22y.thumb.600_0.jpeg


    private DataStore dataStore = null;
    public DataStore getDataStore() {
        return dataStore;
    }


    public void Split(XmlNodeEntity entity,DataStore ds){

        log.e(" ",entity.Level+" ------start--------- ");

        if (ds==null){
            //第一次
            dataStore = new DataStore();
            ds = dataStore;
        }

        //判断
        if (entity.Level.equals(layoutLevel)){
            log.e(" "," ----布局-----");
            layoutDataParse(entity,ds);
        }
        if (entity.Level.equals(layoutItemLevel)){
            log.e(" "," ----布局-----按钮");
            buttonDataParse(entity,ds);
        }
        if (entity.Level.equals(folderLevel)){
            log.e(" "," ----布局-----按钮 ---- 文件夹");
            folderDataParse(entity,ds);
        }
        if (entity.Level.equals(folderItemLevel)){
            log.e(" "," ----布局-----按钮 ---- 文件夹 ---内容");
            contentDataParse(entity,ds);
        }
        //循环
        ArrayList<XmlNodeEntity> child = entity.getChildren();
        if (child!=null && child.size()>0){
            log.e(" "," 子对象数量:"+child.size());
            for (XmlNodeEntity e : child){
                Split(e,ds.NewSettingNodeEntity());
            }
            log.e(" "," -----over ------" );
        }
    }

    /**
     * 具体的内容
     * @param entity
     */
    private void contentDataParse(XmlNodeEntity entity,DataStore ds) {
        try{
            if (ds==null){
                log.e("ds is null");
            }
        //网页 视屏 图片
        DataList data = ds.getData();
        String level = entity.Level;
        String filetype =  entity.getXmldata().get("filetype");//1006 网页 1002视频 1007 图片

        String web_url = entity.getXmldata().get("web_url");
            if (web_url==null){
                log.e("","web_url is null");
                web_url = "";
            }

        String filename = entity.getXmldata().get("filename");//"filename" -> "zhoudongyiu.web"
            if (filename==null){
                log.e(" ","filename is null");
                filename = "";
            }

        String video_image_url = entity.getXmldata().get("video_image_url");//视频第一帧 不存在时,设置为 player.png "video_image_url" -> "null"  "video_image_url" -> "ftp://ftp:FTPmedia@218.89.68.163/uploads/1469783401641.png"

            if (video_image_url == null || video_image_url.equals("")){
                 video_image_url = videoFristImageNotFount;
            }else {
                video_image_url = video_image_url.substring(video_image_url.lastIndexOf("/") + 1);
                if (video_image_url.equals("null")) {
                    video_image_url = videoFristImageNotFount;
                }
            }
        String filepath = entity.getXmldata().get("filepath");//"filepath" -> "ftp://ftp:FTPmedia@218.89.68.163/uploads/1469783401641new.mp4"

        if (filepath==null || filename.equals("null") || filename.equals("")){
            filepath = filetype.equals("1007")?videoNotFount:errImageName;
        }else{
            filepath = filepath.substring(filepath.lastIndexOf("/")+1);
        }


        data.put("level",level);
        data.put("filetype",filetype);
        data.put("web_url",web_url);
        data.put("filename",filename);
        data.put("video_image_url",video_image_url);
        data.put("filepath",filepath);

        }catch (Exception e){
            log.e("contentDataParse()",e.getMessage());
        }

        log.e(" "," -- -- 布局 按钮 文件夹 内容 解析完毕----");
    }

    /**
     * 文件
     * @param entity
     *
     *
     */
    private void folderDataParse(XmlNodeEntity entity,DataStore ds) {
        DataList data = ds.getData();
        String level = entity.Level;
        String name = entity.getXmldata().get("name");
        String type = entity.getXmldata().get("type");//1 视频 图片  3 网页
        data.put("level",level);
        data.put("name",name);
        data.put("type",type);
    }

    /**
     * 分解按钮
     *
     * @param entity
     *
     * x y w h
     */
    private void buttonDataParse(XmlNodeEntity entity,DataStore ds) {

        DataList data = ds.getData();
        String level = entity.Level;
        String w = entity.getXmldata().get("width");
        String h = entity.getXmldata().get("height");
        String x = entity.getXmldata().get("xpos");
        String y = entity.getXmldata().get("ypos");

        //绑定的按钮的类型
        String bindtype =  entity.getXmldata().get("bindtype");    //绑定类型，0为布局，1为文件夹 3网页

        String bgimage = null;
        XmlNodeEntity xnd =  entity.getChildren().get(0);
        if (xnd==null){
            log.e("互动按钮 背景图片名 错误");
            bgimage = buttonBgerr;
        }else{
            if (bindtype.equals("0")){
                //获取 背景图片
                bgimage = xnd.getXmldata().get("thumbnailpath"); //"thumbnailpath" -> "1469784284986.jpg"
            }else if (bindtype.equals("1")|| bindtype.equals("3")){
                bgimage = xnd.getXmldata().get("thumbnailpath"); //"thumbnailpath" -> "http://218.89.68.163:8080/wos/video/1469783944466.jpg"
                bgimage = bgimage.substring(bgimage.lastIndexOf("/")+1);
            }
        }

        bgimage = sd_path+bgimage;
        data.put("level",level);
        data.put("w",w);
        data.put("h",h);
        data.put("x",x);
        data.put("y",y);
        data.put("bgimage",bgimage);
    }


    //分解布局
    private void layoutDataParse(XmlNodeEntity entity,DataStore ds) {
        //level  w h bgmodel bgimage bgcolor

        DataList data = ds.getData();
        String level = entity.Level;
        String w = entity.getXmldata().get("totalwidth");
        String h = entity.getXmldata().get("totalheight");
        String bgmodel = entity.getXmldata().get("bgmode");

        String bg = entity.getXmldata().get("bg");

        if (bgmodel.equals("1")){
            if (bg==null){
                bg = "#FFFFFF";
            }
            else{
                bg = TanslateColor(bg);
            }
        }
        if (bgmodel.equals("2")){
            if (bg==null || bg.equals("") || bg.equals("null")){
                bg = layoutBgerr;
            }
            bg = sd_path+bg;
        }

        data.put("level",level);
        data.put("w",w);
        data.put("h",h);
        data.put("bgmodel",bgmodel);
        data.put("bg",bg);
    }

















    /**
     * 转换背景颜色代码
     * @param colorValue
     * @return
     */
    public static String TanslateColor(String colorValue){
        String color = null ;
        try {
            String tem = Integer.toHexString(Integer.parseInt(colorValue));
            if (tem.length() == 6) {
                color = tem;
            } else {
                StringBuffer addZeor = new StringBuffer();
                for (int i = 0; i < 6 - tem.length(); i++) {
                    addZeor.append("0");
                }
                color = addZeor + tem;
            }
            return "#" + color;
        }catch (Exception e){
            return null;
        }
    }
}
