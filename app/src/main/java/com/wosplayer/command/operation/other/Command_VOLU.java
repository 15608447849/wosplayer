package com.wosplayer.command.operation.other;

import android.content.Context;
import android.media.AudioManager;

import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.command.kernal.iCommand;

/**
 * Created by user on 2016/7/30.
 *
 */
public class Command_VOLU implements iCommand {

    private static final java.lang.String TAG = "_Command_VOLU";

    @Override
    public void Execute(String param) {
        Logs.i(TAG,"音量设置 param :"+ param +"ThreadName:"+Thread.currentThread().getName()+" ");
        int percent = Integer.valueOf(param);
        SetSystemVolume(percent);
    }
    public void SetSystemVolume(int percent)
    {
        AudioManager audioManager = (AudioManager) PlayApplication.appContext.getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (max*percent)/100, 0);
    }












}
