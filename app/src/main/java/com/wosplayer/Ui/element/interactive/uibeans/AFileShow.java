package com.wosplayer.Ui.element.interactive.uibeans;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.wosplayer.R;

import com.wosplayer.Ui.element.definedView.MWeb;
import com.wosplayer.Ui.element.definedView.Mimage;
import com.wosplayer.Ui.element.definedView.Mvideo;
import com.wosplayer.Ui.element.interactive.databeads.Acontent;
import com.wosplayer.Ui.element.uitools.ImageAttabuteAnimation;
import com.wosplayer.Ui.element.uitools.ImageViewPicassocLoader;
import com.wosplayer.Ui.performer.UiExcuter;

import java.util.ArrayList;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by 79306 on 2017/3/10.
 * 文件展示
 */
public class AFileShow extends FrameLayout implements View.OnClickListener{
    private static final String TAG = "互动文件显示层";


    private Context context;
    private MWeb web;
    private ProgressBar progress;
    private Mvideo video;
    private Mimage image;
    private ArrayList<Acontent> contentList;
    private int cindex = 0;
    private FrameLayout backLayer;
    private ImageButton cancelBtn ;

    private class loopSourceExist extends Thread{
        @Override
        public void run() {

        }
    }

    private FrameLayout.LayoutParams layoutParams;

    public AFileShow(@NonNull Context context) {
        super(context);
        this.context = context;
        onContentLayer(context);//创建显示层
    }

    public void onCreateContent(ArrayList<Acontent> contentList) {
        this.contentList = contentList;
    }

    public void onContentLayer(Context context) {
        layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        //背景层
        backLayer = new FrameLayout(context);
        backLayer.setLayoutParams(layoutParams);
        this.addView(backLayer);

        View v = LayoutInflater.from(context).inflate(R.layout.action_show_layer,null);
        this.addView(v);
        cancelBtn = (ImageButton) v.findViewById(R.id.btn_cancel);
        progress = (ProgressBar) findViewById(R.id.progress);
        ImageButton left =  (ImageButton) v.findViewById(R.id.btn_left);
        ImageButton right =  (ImageButton) v.findViewById(R.id.btn_right);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
    }


    public void onDestory() {
        context = null;
        if (web!=null) web.killSelf();
        //停止所有子控件活动
        backLayer.removeAllViews();
    }

    public void onAddCancelButton(OnClickListener onClickListener) {
        if (cancelBtn!=null) cancelBtn.setOnClickListener(onClickListener);
    }

    @Override
    public void onClick(View v) {
        //判断当前
        if ((contentList!=null) && contentList.size()>1){
            int vid = v.getId();
            int tem = cindex;
            if (vid == R.id.btn_left){//左边
               tem--;
                if (tem<=0){
                    tem = contentList.size()-1;
                }
            }
            if (vid == R.id.btn_right){// 右边
                tem++;
                if (tem>= contentList.size()){
                    tem = 0;
                }
            }
            cindex = tem;
            tanslationContent();
        }
    }

    public void tanslationContent() {
        backLayer.removeAllViews();//移出全部
        final Acontent content = contentList.get(cindex);
        String source = "";
        View view = null;
        if (content.type.equals("1007")){//图片
            if (image==null){
                image = new Mimage(context);
                image.setLayoutParams(layoutParams);
            }

            source = FileUtils.isFileExist(content.sourcePath)?content.sourcePath: UiExcuter.getInstancs().defImagePath;
            ImageViewPicassocLoader.getBitmap(source,image);
            view = image;
        }
        if (content.type.equals("1006")){
            if (web==null){
                //网页
                web = new MWeb(context,
                        new MWeb.MwebChrome(progress),
                null);
                web.setLayoutParams(layoutParams);
            }
            source = content.web_url;
            web.loadUrl(source);
            view = web;
        }
        if (content.type.equals("1002")){
            if (video==null){
                //视频
                video = new Mvideo(context);
                video.setLayoutParams(layoutParams);
                video.setLoop(false);
                video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        tanslationContent();
                    }
                });

            }
            source = FileUtils.isFileExist(content.sourcePath)?content.sourcePath : UiExcuter.getInstancs().defVideoPath;
            video.setMedioFilePath(source);
            view = video;
        }
        backLayer.addView(view);
        ImageAttabuteAnimation.SttingAnimation(null,view,null);
    }

}
