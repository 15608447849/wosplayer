package com.wosplayer.Ui.element.definedView;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wosplayer.app.Logs;

import java.io.IOException;

/**
 * Created by user on 2016/7/6.
 */
public class Mvideo extends SurfaceView implements SurfaceHolder.Callback{

    public static final String TAG = "视频播放器";
    private Context context;//上下文
    private String filename;
    private int mDuration;//持续时长

    public boolean isLoop;
    /**
     * 音频播放者
     */
    private MediaPlayer mMediaPlayer = null;
    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    public Mvideo(Context context) {
        super(context);
        this.context = context;
        SurfaceHolder holder = this.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.setSizeFromLayout();//设置surface大小来自布局
        holder.addCallback(this);//添加回掉接口
    }
    /**
     * 设置本地资源
     */
    public void setMedioFilePath(String filename) {
        this.filename = filename;
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); // 播放时长单位为毫秒
        Logs.d(TAG, "设置本地视频路径: " + filename +",时长 :"+duration+" 毫秒");
        mDuration = Integer.parseInt(duration);
    }
    //catch screen use
    public String getMedioFilePath() {
        return this.filename;
    }
    public void setLoop(boolean loop){
        this.isLoop = loop;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            createMedio();
        } catch (IOException e) {
            destoryMedio();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        destoryMedio();
    }
    /**
     * 播放器创建
     * @throws IOException
     */
    private void createMedio() throws IOException {
        if (mMediaPlayer!=null) return;
        mMediaPlayer = new MediaPlayer(); //新建播放器
        mMediaPlayer.setDataSource(context, Uri.parse(filename));
        mMediaPlayer.setDisplay(this.getHolder());//设置播放器图层显示在哪
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置播放器音频流
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.e(TAG,"媒体播放器准备完成.");
                mp.start();
            }
        }); //准备监听
        if (completionListener!=null){
            mMediaPlayer.setOnCompletionListener(completionListener);//播放完成
        }
        mMediaPlayer.setOnErrorListener(errorListener);
        mMediaPlayer.setOnInfoListener(infoListener);
        mMediaPlayer.setLooping(isLoop);
        mMediaPlayer.prepareAsync();//异步准备
    }
    /**
     * 播放器销毁
     */
    private void destoryMedio(){
        if (mMediaPlayer==null) return;
        mMediaPlayer.stop();
        mMediaPlayer.reset(); //重置释放
        mMediaPlayer.release();
        mMediaPlayer = null; //置为空
    }
    //获取当前帧
    public Bitmap getCurrentFrame(){
        Bitmap bitmap = null;
        if (mMediaPlayer!=null && mMediaPlayer.isPlaying() &&  null != filename && !"".equals(filename)){
            mmr.setDataSource(filename);
            long pos = mMediaPlayer.getCurrentPosition();
            bitmap = mmr.getFrameAtTime(pos * 1000,MediaMetadataRetriever.OPTION_CLOSEST);
            if (bitmap!=null){
                bitmap =  Bitmap.createScaledBitmap(
                        bitmap,
                        mMediaPlayer.getVideoWidth(),
                        mMediaPlayer.getVideoHeight(),
                        true);
            }
        }
        return bitmap;
    }



    //播放完成回掉接口
    private MediaPlayer.OnCompletionListener completionListener = null;
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener){
        this.completionListener = listener;
    }

    private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.d(TAG,"error( what:"+what+" - extra"+extra+" )");
            return false;
        }
    };
    private MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.d(TAG,"info( what:"+what+" - extra"+extra+" )");
            return false;
        }
    };


}
