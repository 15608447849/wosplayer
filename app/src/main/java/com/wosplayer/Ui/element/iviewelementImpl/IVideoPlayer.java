package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.MyVideoView;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.excuteBolock.Loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by Administrator on 2016/7/26.
 * 播放视频  支持 多视频循环播放
 *
 */

public class IVideoPlayer extends AbsoluteLayout implements IPlayer{
    private static final java.lang.String TAG = IVideoPlayer.class.getName();
    private Loader loader;
    private Context mCcontext;
    private ViewGroup mfatherView = null;
    private int x=0;
    private int y=0;
    private int h=0;
    private int w=0;
    private boolean isExistOnLayout = false;//是否已经布局


    //播放器
    private MyVideoView video = null;
    public IVideoPlayer(Context context,ViewGroup vp) {
        super(context);
        this.mfatherView = vp;
        mCcontext =context;
        //资源加载者
        loader = new Loader();
        loader.settingCaller(this);
        video = new MyVideoView(context,this);
    }

    private String singleFileLocalPath = null;
    private String singleFileUri = null;
    private DataList mp = null;
    @Override
    public void loadData(DataList mp, Object ob) {
        try {
        this.mp = mp;
        this.x = mp.GetIntDefualt("x", 0);
        this.y = mp.GetIntDefualt("y", 0);
        this.w = mp.GetIntDefualt("width", 0);
        this.h = mp.GetIntDefualt("height", 0);

        this.singleFileLocalPath = mp.GetStringDefualt("localpath", "");
        this.singleFileUri = mp.GetStringDefualt("getcontents", "");
        //预留 多个视频 播放的数组
        multifileList = Collections.synchronizedList(new ArrayList<String>());

         addMultiFileItem(singleFileLocalPath,singleFileUri);
            //如果是多文件  设置false
            video.initVideoView(false);
            //自己完成播放完成监听
            log.d(TAG," MyVideoView : IVideoplayer srtting OnCompletionListener ...");
            video.setOnCompletionListener_ (new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    log.d(TAG," MyVideoView : IVideoplayer srtting OnCompletionListener _ onCompletion()");
                    currentIndex++;
                    if (currentIndex==multifileList.size()){
                        currentIndex = 0;
                    }
                    String filename = multifileList.get(currentIndex);
                    //设置循环播放
                    video.setVideoPath(filename);
                    start();
                }
            });


        }catch (Exception e){
            log.e(TAG, "loaddata() " + e.getMessage());
        }
    }

    /**
     * 预留 多视频 的 数组
     */
    private boolean playMultifile = false;
    private List<String> multifileList = null ;
    private Map<String,String> multiFileUriMap = null;
        public void addMultiFileItem(String filename,String Uri){
            if (multiFileUriMap==null){
                multiFileUriMap = Collections.synchronizedMap(new HashMap<String,String>());
            }
            if(multifileList.contains(filename)){
               return;
            }
            multifileList.add(filename);
            //uri
            multiFileUriMap.put(filename,Uri);
        }


    @Override
    public void setlayout() {
        try {
            if (!isExistOnLayout){
                mfatherView.addView(this);
                isExistOnLayout = true;
            }

            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) this
                    .getLayoutParams();
            lp.x = x;
            lp.y = y;
            lp.width = w;
            lp.height = h;
            this.setLayoutParams(lp);
            //设置播放器
            video.setMyLayout(x,y,w,h);
        } catch (Exception e) {
            log.e(TAG,"设置布局:" + e.getMessage());
        }
    }



    private int currentIndex = -1;

    @Override
    public void start() {
        try{
            setlayout();//设置布局

            //启动播放器 如果是多文件 从第一个开始
            if (multifileList.size()!=0){
                //效验文件
                for (String item : multifileList){
                    if(!loader.fileIsExist(item)){
                    //不存在
                        String uri = multiFileUriMap.get(item);
                        if (uri!=null && !uri.equals("")){
                            loader.LoadingUriResource(uri,null);
                        }
                 }
                }
                currentIndex =0 ;
                downloadResult(multifileList.get(currentIndex));
               //当前播放的下标 //使用时判断 如果 currentIndex == list的最大值 , 下次播放 从0 开始

            }
        }catch (Exception e){
            log.e(TAG,"开始:"+e.getMessage());
        }
    }

    @Override
    public void stop() {
        try {
            //移除父视图
            mfatherView.removeView(this);
            isExistOnLayout = false;
        }catch (Exception e){
            log.e(TAG,"停止:"+e.getMessage());
        }
    }



    @Override
    public DataList getDatalist() {
        return mp;
    }

    @Override
    public void downloadResult(final String filePath) {

        try {
            //播放
            AndroidSchedulers.mainThread().createWorker().schedule(
                    new Action0() {
                        @Override
                        public void call() {
                            playVideo(filePath);
                        }
                    }
            );
        }catch ( Exception e ){
            log.e(TAG,"call : "+ e.getMessage());
        }


    }

    private void playVideo(String filename){
        video.loadRouce(filename);//第一个开始 有多个的话每播放完一个 播下一个,到最后 跳到第一个
        video.start();
    }


}
