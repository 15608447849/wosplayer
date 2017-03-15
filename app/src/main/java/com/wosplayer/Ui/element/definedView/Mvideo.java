package com.wosplayer.Ui.element.definedView;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wosplayer.app.Logs;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by user on 2016/7/6.
 */
public class Mvideo extends SurfaceView implements SurfaceHolder.Callback{

    public static final String TAG = "视频播放器";
    private Context context;//上下文
    private String filename;
    private int mDuration;//持续时长
    public boolean isLoop;//循环播放
    /**
     * 音频播放者
     */
    private MediaPlayer mMediaPlayer = null;

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
        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(filename);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); // 播放时长单位为毫秒
            Logs.i(TAG, "设置本地视频路径: " + filename +",时长 :"+duration+" 毫秒");
            mDuration = Integer.parseInt(duration);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                mmr.release();
            } catch (Exception e) {
            }
        }
    }
    public String getMediaoFile(){
        return filename;
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
        mMediaPlayer = getMediaPlayer(context); //新建播放器
        mMediaPlayer.setDataSource(context, Uri.parse(filename));
        mMediaPlayer.setDisplay(this.getHolder());//设置播放器图层显示在哪
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置播放器音频流
        mMediaPlayer.setLooping(isLoop);
        //Logs.e(TAG,"isLoop: "+isLoop+"循环属性:"+mMediaPlayer.isLooping());
        if (completionListener!=null){
            mMediaPlayer.setOnCompletionListener(completionListener);//播放完成
        }
        mMediaPlayer.setOnPreparedListener(preparedListener); //准备监听
        mMediaPlayer.setOnErrorListener(errorListener);
        mMediaPlayer.setOnInfoListener(infoListener);
        mMediaPlayer.prepareAsync();//异步准备
    }
    /**
     * 播放器销毁
     */
    private void destoryMedio(){
        if (mMediaPlayer==null) return;
        if (mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }
        mMediaPlayer.reset(); //重置释放
        mMediaPlayer.release();
        mMediaPlayer = null; //置为空
    }
    //获取当前帧
    public Bitmap getCurrentFrame(){
        Bitmap bitmap = null;
        MediaMetadataRetriever mmr = null;
        try {

            if (mMediaPlayer!=null && mMediaPlayer.isPlaying() &&  null != filename && !"".equals(filename)){
                mmr = new MediaMetadataRetriever();
                mmr.setDataSource(filename);
                long pos = mMediaPlayer.getCurrentPosition();
                bitmap = mmr.getFrameAtTime(pos * 1000,MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
//                Logs.e(TAG,"截图 视频 大小 ( "+mMediaPlayer.getVideoWidth()+" , "+mMediaPlayer.getVideoHeight()+" )");
//                Logs.e(TAG,"截图 视频 大小 ( "+bitmap.getWidth()+" , "+bitmap.getHeight()+" )");
//                Logs.e(TAG,"截图 视频 大小 ( "+this.getWidth()+" , "+this.getHeight()+" )");



//                if (bitmap!=null){
//                    bitmap =  Bitmap.createScaledBitmap(
//                            bitmap,
//                            mMediaPlayer.getVideoWidth(),
//                            mMediaPlayer.getVideoHeight(),
//                            true);
//                }
                if (bitmap!=null){
                    bitmap =  Bitmap.createScaledBitmap(
                            bitmap,
                            this.getWidth(),
                            this.getHeight(),
                            true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mmr.release();
            } catch (Exception e) {
            }
        }
        return bitmap;
    }



    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
//            Log.e(TAG,"媒体播放器准备完成.");
            mp.start();
        }
    };
    //播放完成回掉接口
    private MediaPlayer.OnCompletionListener completionListener = null;
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener){
        this.completionListener = listener;
    }

    public MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e(TAG,"error( what: "+what+" - extra: "+extra+" )");
            return true;
        }
    };
    public void setOnErrorListener(MediaPlayer.OnErrorListener errorListener){
        this.errorListener = errorListener;
    }
    public MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            //Log.e(TAG,"info( what: "+what+" - extra: "+extra+" )");
            return false;
        }
    };


    public Object getDuration() {
        return mDuration;
    }


    private MediaPlayer getMediaPlayer(Context context){

        MediaPlayer mediaplayer = new MediaPlayer();

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            return mediaplayer;
        }

        try {
            Class<?> cMediaTimeProvider = Class.forName( "android.media.MediaTimeProvider" );
            Class<?> cSubtitleController = Class.forName( "android.media.SubtitleController" );
            Class<?> iSubtitleControllerAnchor = Class.forName( "android.media.SubtitleController$Anchor" );
            Class<?> iSubtitleControllerListener = Class.forName( "android.media.SubtitleController$Listener" );

            Constructor constructor = cSubtitleController.getConstructor(new Class[]{Context.class, cMediaTimeProvider, iSubtitleControllerListener});

            Object subtitleInstance = constructor.newInstance(context, null, null);

            Field f = cSubtitleController.getDeclaredField("mHandler");

            f.setAccessible(true);
            try {
                f.set(subtitleInstance, new Handler());
            }
            catch (IllegalAccessException e) {return mediaplayer;}
            finally {
                f.setAccessible(false);
            }

            Method setsubtitleanchor = mediaplayer.getClass().getMethod("setSubtitleAnchor", cSubtitleController, iSubtitleControllerAnchor);

            setsubtitleanchor.invoke(mediaplayer, subtitleInstance, null);
            //Log.e("", "subtitle is setted :p");
        } catch (Exception e) {}

        return mediaplayer;
    }

}
