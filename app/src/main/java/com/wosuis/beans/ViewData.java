package com.wosuis.beans;

import com.wosplayer.app.DataList;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 79306 on 2017/3/1.
 */
public class ViewData {




    public enum  Widget {
        none,program,layout,interactive,abutton,alayout,afiles, fudianbank,webpage, url, rss, text, video, image;
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
    private String nextTags;//连接的下一个标识
    private ArrayList<String> nextTagsArr;//连接的下一个标识组
    private String pervTags;//连接的上一个标识
    public void connect(ViewData data){
        data.setPervTags(this.getTags());
        this.getNextTagsArr().add(data.getTags());
    }
    //播放次数
    private int playCount;
    //显示名
    private String showName;
    //其他选项
    private DataList dataList;

    public ArrayList<String> getNextTagsArr() {
        if (nextTagsArr==null) nextTagsArr = new ArrayList<>();
        return nextTagsArr;
    }

    public void setNextTagsArr(ArrayList<String> nextTagsArr) {
        this.nextTagsArr = nextTagsArr;
    }

    public String getNextTags() {
        return nextTags;
    }

    public void setNextTags(String nextTags) {
        this.nextTags = nextTags;
    }

    public String getPervTags() {
        return pervTags;
    }

    public void setPervTags(String pervTags) {
        this.pervTags = pervTags;
    }

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
    public void setWidth(String width) {
        int i  = 0;
        try {
            i = Integer.parseInt(width);
        } catch (NumberFormatException e) {
        }
        setWidth(i);
    }


    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setHeight(String height) {
        int i  = 0;
        try {
            i = Integer.parseInt(height);
        } catch (NumberFormatException e) {
        }
        setHeight(i);
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

    public DataList getDataList() {
        return dataList;
    }

    public void setDataList(DataList dataList) {
        this.dataList = dataList;
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


    //互动相关
    private boolean isIAHome; //互动主页

    private int nextType;
    private int nextId;
    private String nextKey; // url+bindid 向下链接
    private String thumbnail;//封面图
    private int AIid;
    private ViewData pevView;//向上链接
    private ViewData nextView;

    public ViewData getNextView() {
        return nextView;
    }

    public void setNextView(ViewData nextView) {
        this.nextView = nextView;
    }

    HashMap<String,ViewData> dataMaps;

    public boolean isIAHome() {
        return isIAHome;
    }

    public void setIAHome(boolean IAHome) {
        isIAHome = IAHome;
    }

    public int getNextType() {
        return nextType;
    }

    public void setNextType(int nextType) {
        this.nextType = nextType;
    }
    public void setNextType(String nextType) {
        int i = -1;
        try {
            i = Integer.parseInt(nextType);
        } catch (NumberFormatException e) {
        }
        setNextType(i);
    }
    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }
    public void setNextId(String nextId) {
        int aid = -1;
        try {
            aid =  Integer.parseInt(nextId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        setNextId(aid);
    }

    public HashMap<String, ViewData> getDataMaps() {
        if (dataMaps==null){
            dataMaps = new HashMap<>();
        }
        return dataMaps;
    }

    public void setDataMaps(HashMap<String, ViewData> dataMaps) {
        this.dataMaps = dataMaps;
    }

    public ViewData getPevView() {
        return pevView;
    }

    public void setPevView(ViewData pevView) {
        this.pevView = pevView;
    }

    public int getAIid() {
        return AIid;
    }

    public void setAIid(int AIid) {
        this.AIid = AIid;
    }
    public void setAIid(String id) {
        int aid = -1;
        try {
            aid =  Integer.parseInt(id);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        setAIid(aid);
    }
    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getNextKey() {
        return nextKey;
    }

    public void setNextKey(String nextKey) {
        this.nextKey = nextKey;
    }

    //广告
    private boolean adEnable;
//    <adenable>true</adenable>
//    <waittime>30</waittime>
//    <fullscrmode>1</fullscrmode>
//    <interval>10</interval>  //间隔
//    <bindtype>1</bindtype>
//    <bindid>1056</bindid>
    private long adwaittime;//等待时间
    private int adinterval;//播放间隔
    private int adBindId;

    public int getAdBindId() {
        return adBindId;
    }

    public void setAdBindId(int adBindId) {
        this.adBindId = adBindId;
    }

    public void setAdBindId(String adBindId) {
    int i =  -1;
        try {
            i = Integer.parseInt(adBindId);
        } catch (NumberFormatException e) {
        }
        setAdBindId(i);
    }
    public boolean isAdEnable() {
        return adEnable;
    }

    public void setAdEnable(boolean adEnable) {
        this.adEnable = adEnable;
    }

    public void setAdEnable(String adEnable) {
        if (adEnable==null) return;
        if (adEnable.equals("true")) this.adEnable =true;
    }
    public long getAdwaittime() {
        return adwaittime;
    }

    public void setAdwaittime(long adwaittime) {
        this.adwaittime = adwaittime;
    }
    public void setAdwaittime(String adwaittime) {
        long i  = 0;
        try {
            i = Long.parseLong(adwaittime);
        } catch (NumberFormatException e) {
        }
        setAdwaittime(i);
    }

    public int getAdinterval() {
        return adinterval;
    }

    public void setAdinterval(int adinterval) {
        this.adinterval = adinterval;
    }
    public void setAdinterval(String adinterval) {
        int i  = 0;
        try {
            i = Integer.parseInt(adinterval);
        } catch (NumberFormatException e) {
        }
        setAdinterval(i);
    }
    private ArrayList<ViewData> viewList;
    private ArrayList<String> viewTags;

    public ArrayList<String> getViewTags() {
        if (viewTags==null) viewTags = new ArrayList<>();
        return viewTags;
    }

    public void setViewTags(ArrayList<String> viewTags) {
        this.viewTags = viewTags;
    }

    public ArrayList<ViewData> getViewList() {
        if (viewList==null) viewList = new ArrayList<>();
        return viewList;
    }
    private int preViewId;

    public int getPreViewId() {
        return preViewId;
    }

    public void setPreViewId(int preViewId) {
        this.preViewId = preViewId;
    }

    private int bindPreViewId;

    public int getBindPreViewId() {
        return bindPreViewId;
    }

    public void setBindPreViewId(int bindPreViewId) {
        this.bindPreViewId = bindPreViewId;
    }

    public void setViewList(ArrayList<ViewData> viewList) {
        this.viewList = viewList;
    }

    private ArrayList<String> actionFileContent;

    public ArrayList<String> getActionFileContent() {
        if (actionFileContent == null) actionFileContent = new ArrayList<>();
        return actionFileContent;
    }

    public void setActionFileContent(ArrayList<String> actionFileContent) {
        this.actionFileContent = actionFileContent;
    }
}
