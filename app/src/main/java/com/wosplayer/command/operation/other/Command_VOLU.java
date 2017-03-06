package com.wosplayer.command.operation.other;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.command.operation.interfaces.iCommand;

/**
 * Created by user on 2016/7/30.
 *
 */
public class Command_VOLU implements iCommand {

    private static final java.lang.String TAG = "设置音量";

    @Override
    public void execute(Activity activity, String param) {
        Logs.i(TAG,"大小:"+ param);
        int percent = 0;
        try {
            percent = Integer.valueOf(param);
            SetSystemVolume(percent);
        } catch (NumberFormatException e) {
        }
    }
    public void SetSystemVolume(int percent)
    {
        AudioManager audioManager = (AudioManager) PlayApplication.appContext.getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (max*percent)/100, 0);
    }












}
