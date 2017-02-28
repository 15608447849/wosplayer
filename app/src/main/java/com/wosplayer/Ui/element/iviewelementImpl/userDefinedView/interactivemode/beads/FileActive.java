package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.beads;


import com.wosplayer.Ui.element.interfaces.IviewPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans.ActiveImagePlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans.ActiveVideoPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans.ActiveWebPlayer;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;

/**
 * Created by Administrator on 2016/6/28.
 *
 <type>1</type><!-- 文件夹类型，2广告，5办公文档，1普通，3网站 –>

 *<item>
 <filetype>1007</filetype><!—文件类型，1002视频，1007图片，1001FLASH –>
 <filename><![CDATA[bg_1.jpg]]></filename>
 <filepath><![CDATA[ftp://ftp:FTPmedia@218.89.68.163/uploads/1467079602838.jpg]]></filepath>
 <thumbnailpath><![CDATA[]]></thumbnailpath>
 <web_url><![CDATA[]]></web_url><!-- 若文件夹类型是网站，则此处为地址 -->
 <animation>0</animation>
 <title><![CDATA[bg_1]]></title>
 </item>
 */

public class FileActive {

    private static final java.lang.String TAG = "_FileActive";//FileActive.class.getName();
    public int type;
    public int filetype;
    public String filepath;
    public String video_image_url;
    /**
     * 转化视图
     * file -> 具体的view
     * @return
     */
    public IviewPlayer TanslateInfoToView() {

        try {
            //先判断资源文件 本地是否存在
            String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
            String localPath = PlayApplication.config.GetStringDefualt("basepath", "") + filename;

            Logs.i(TAG, "这个类型:" + type + "资源:" + filepath + "本地资源路径:" + localPath);

            if (type == 3) { //网页 //|| filetype == 1006
                Logs.i(TAG, Thread.currentThread().getName());
              return new ActiveWebPlayer(DisplayActivity.activityContext,filepath);

            } else if (filetype == 1007) { //图片
               return new ActiveImagePlayer(DisplayActivity.activityContext, filepath, localPath);
            } else if (filetype == 1002) {//video 1002
                return new ActiveVideoPlayer(DisplayActivity.activityContext,filepath,localPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
