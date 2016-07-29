package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.beads;


import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans.ActiveImagePlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans.ActiveVideoPlayerAndImage;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans.ActiveWebPlayer;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;

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

    private static final java.lang.String TAG = FileActive.class.getName();
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

        //先判断资源文件 本地是否存在
        String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
        String localPath = wosPlayerApp.config.GetStringDefualt("basepath", "") + filename;

        log.i(TAG, "这个类型:" + type + "资源:" + filepath + "本地资源路径:" + localPath);

        if (type == 3) { //网页 //|| filetype == 1006
            log.i(TAG, Thread.currentThread().getName());
          return new ActiveWebPlayer(DisplayActivity.activityContext,filepath);

        } else if (filetype == 1007) { //图片
           return new ActiveImagePlayer(DisplayActivity.activityContext, filepath, localPath);
        } else if (filetype == 1002) {//video 1002

          // return new VideoPlayer(config.activity,filepath,localPath);

            if(video_image_url.equals("") || video_image_url==null || video_image_url.equals("null")){
                video_image_url = "http://desk.fd.zol-img.com.cn/t_s1920x1080c5/g5/M00/08/04/ChMkJleF7WeIdaxTABNpaaUXOW8AATeTwERcIwAE2mB274.jpg?downfile=146891502194.jpg";
            }

            String imagefn = video_image_url.substring(video_image_url.lastIndexOf("/") + 1);
            String imageLocalPath =  wosPlayerApp.config.GetStringDefualt("basepath", "")+imagefn;
            return  new ActiveVideoPlayerAndImage(DisplayActivity.activityContext,video_image_url,imageLocalPath,filepath,localPath);

        }
        return null;

    }
}
