package com.wosplayer.Ui.element.interactive.databeads;

/**
 * Created by 79306 on 2017/3/10.
 */
public class Abutton {
    public String tag;
    //坐标
    public int x;
    public int y;
    public int width;
    public int height;
    public String bindType; // 1 指向类型
    //关联的 layout数据
    public String preTag ;
    //关联的 文件夹数据 或者 布局数据
    public String nextTag;

    //显示的图片 从下一级获取
    public String image;
    //修正宽高比例
    public void setScale(float xScale , float yScale){
        width = (int) ((float) this.width * xScale);
        height = (int) ((float) this.height * yScale);
        x = (int) ((float) this.x * xScale);
        y = (int) ((float) this.y * yScale);
    }

    public boolean isNextFileType(){
        return bindType.equals("1") || bindType.equals("3");
    }

}
