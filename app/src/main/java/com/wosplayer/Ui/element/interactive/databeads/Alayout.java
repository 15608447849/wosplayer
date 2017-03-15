package com.wosplayer.Ui.element.interactive.databeads;



import java.util.ArrayList;
import java.util.List;

/**
 * Created by 79306 on 2017/3/10.
 */

public class Alayout {

    public String tag;//标识
    public String thumbnail;//封面图
    public int bgType;//背景类型
    public String bgValue;//背景值

    public String layoutUri;
    public String folderurl;
    public String thumbnailurl;

    public int totalwidth;
    public int totalheight;

    public ArrayList<Abutton> buttonList = new ArrayList<>();//按钮值
}
