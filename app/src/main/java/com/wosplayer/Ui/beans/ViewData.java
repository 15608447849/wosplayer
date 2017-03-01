package com.wosplayer.Ui.beans;

import com.wosplayer.app.DataList;

/**
 * Created by 79306 on 2017/3/1.
 */
public class ViewData {

    public enum  Widget {
        none,interactive, fudianbank,webpage, url, rss, text, video, image;
    }
    public static Widget getWidgetType(String var){
        Widget type = Widget.none;
        try {
            type = Widget.valueOf(var);
        } catch (IllegalArgumentException e) {
        }
        return type;
    }

    public static final int COLOR_NONE = -1;
    public static final int COLOR_IMAGE = 1;
    public static final int COLOR_COLOR = 0;

    //标题
    private String title;
    //持续时间
    private long playLength;
    //id
    private String id;
    //背景类型 0颜色 1图片 -1不存在
    private int bgType;
    //背景颜色
    private String bgColorCode;
    //背景图片
    private String bgImagePath;

    //大小
    private int xPoint,yPoint,width,height;

    //组件类型
    private Widget widghtType;
    //资源地址
    private String resourceAddress;
    //本地文件
    private String localResource;
    private String tags;//唯一标识
    //播放次数
    private int playCount;
    //显示名
    private String showName;
    //其他选项
    private DataList otherAttr;

    public Widget getWidghtType() {
        return widghtType;
    }

    public void setWidghtType(Widget widghtType) {
        this.widghtType = widghtType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPlayLength() {
        return playLength;
    }

    public void setPlayLength(long playLength) {
        this.playLength = playLength;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBgType() {
        return bgType;
    }

    public void setBgType(int bgType) {
        this.bgType = bgType;
    }

    public String getBgColorCode() {
        return bgColorCode;
    }

    public void setBgColorCode(String bgColorCode) {
        this.bgColorCode = bgColorCode;
    }

    public String getBgImagePath() {
        return bgImagePath;
    }

    public void setBgImagePath(String bgImagePath) {
        this.bgImagePath = bgImagePath;
    }

    public int getxPoint() {
        return xPoint;
    }

    public void setxPoint(int xPoint) {
        this.xPoint = xPoint;
    }

    public int getyPoint() {
        return yPoint;
    }

    public void setyPoint(int yPoint) {
        this.yPoint = yPoint;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    public void setSize(int x,int y,int width,int height){

    }
    public void setSize(String x,String y,String width,String height){
        int mx = 0,my = 0, mw = 0, mh = 0;
        try {
            mx = Integer.parseInt(x);
            my = Integer.parseInt(y);
            mw = Integer.parseInt(width);
            mh = Integer.parseInt(height);
        } catch (NumberFormatException e) {
        }
        setSize(mx,my,mw,mh);
    }

    public String getResourceAddress() {
        return resourceAddress;
    }

    public void setResourceAddress(String resourceAddress) {
        this.resourceAddress = resourceAddress;
    }

    public String getLocalResource() {
        return localResource;
    }

    public void setLocalResource(String localResource) {
        this.localResource = localResource;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public DataList getOtherAttr() {
        return otherAttr;
    }

    public void setOtherAttr(DataList otherAttr) {
        this.otherAttr = otherAttr;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }
    public void setPlayCount(String playCount) {
       int count = 0;
        try {
            count = Integer.parseInt(playCount);
        } catch (NumberFormatException e) {
        }
        setPlayCount(count);
    }
    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

}
