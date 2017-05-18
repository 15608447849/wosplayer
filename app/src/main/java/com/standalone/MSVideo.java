package com.standalone;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.view.SurfaceView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

/**
 * Created by user on 2017/4/28.
 */

public class MSVideo extends SurfaceView implements MVideoInterface {
    //持有一个视频播放器
    private LibVLC libVLC;
    private Media media;
    private MediaPlayer video;
    public MSVideo(Context context) {
        super(context);
        getHolder().setFormat(PixelFormat.RGBX_8888);
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
        libVLC = new LibVLC(context);
    }

    @Override
    public void attch() {
        if (this.getVisibility() == GONE){
            this.setVisibility(VISIBLE);
        }
    }
    @Override
    public void deattch() {
        if (this.getVisibility() == VISIBLE){
            this.setVisibility(GONE);
        }
    }
    @Override
    public void startVideo(final String url, final VideoEvent event) {
        if (libVLC!=null){
            stopVideo();
            media = new Media(libVLC,url);
            media.setHWDecoderEnabled(true,true);

            media.setEventListener(new Media.EventListener() {
                @Override
                public void onEvent(Media.Event event1) {
//                    Log.i("视频","onEvent:" + event1);
                    if (video!= null) {
//                        Log.e("视频","当前 video :"+video);
                        if (video.getPlayerState() == 6 && !video.isReleased()){
                            event.onComplete();
                        }
                    }
                }
            });

            video = new MediaPlayer(media);
            video.getVLCVout().setVideoView(this);
            video.getVLCVout().attachViews();
            video.setMedia(media);
            video.setVolume(100);
            video.play();
        }else{
            event.onComplete();
        }

    }

    @Override
    public void stopVideo() {
        if (media!=null){
            media.parse();
            media.release();
            media.setEventListener(null);
            media = null;
        }
        if (video!=null){
            if (video.isPlaying()){
                video.play();
                video.stop();
            }

            video.getVLCVout().detachViews();//解除绑定
            video.release();
            video = null;
        }
    }
    @Override
    public void releaseVideo() {
            stopVideo();
        if (libVLC!=null){
            libVLC.release();
            libVLC = null;
//            Log.e("视频","vlc lib  释放");
        }
    }

}
