package com.wosplayer.command.operation.other;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.command.operation.interfaces.iCommand;

/**
 * Created by user on 2016/9/24.
 *
 *  保存后台发送的密码
 */
public class Command_PASD implements iCommand {
    private static final  String TAG = "PASD";

    private static final String defpassword= "88888888";
    private static final String sharedPreferences_txt = "本地密码";
    private static final String PASSWORD_KEY = "unlock_pass";
    @Override
    public void execute(Activity activity, String param) {

        if (param==null || param.equals("") || param.equals("null")){
            return;
        }
        Logs.i(TAG,"param: " + param);
        boolean glag = savaUnlockPassword(activity,param);
        PlayApplication.sendMsgToServer("PASD:"+glag);
    }

    //保存密码
    private boolean savaUnlockPassword(Context context,String password) {
        int ret = 0;
        SharedPreferences sp = context.getSharedPreferences(sharedPreferences_txt, PlayApplication.appContext.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sp.edit();
        mEditor.putString(PASSWORD_KEY,password);
        return mEditor.commit();
    }


    //获取本地密码
    public static String getUnlockPassword(Context context){
        SharedPreferences sp = context.getSharedPreferences(sharedPreferences_txt, PlayApplication.appContext.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sp.edit();
        String psd = sp.getString(PASSWORD_KEY,defpassword);
        return psd==null?defpassword:psd;
    }

}
