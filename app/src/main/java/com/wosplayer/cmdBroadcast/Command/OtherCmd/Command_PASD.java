package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.content.SharedPreferences;

import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.iCommand;

/**
 * Created by user on 2016/9/24.
 *
 *  保存后台发送的密码
 */
public class Command_PASD implements iCommand {
    private static final  String TAG = "PASD";

    private static final String defpassword= "88888888";
    private static final String sharedPreferences_txt = Command_PASD.class.getName();
    private static final String PASSWORD_KEY = "unlock_pass";
    @Override
    public void Execute(String param) {

        if (param==null || param.equals("")){

            return;
        }
        log.v(TAG,"param: " + param);

        boolean glag = savaUnlockPassword(param);

        wosPlayerApp.sendMsgToServer("PASD:"+glag);
    }

    //保存密码
    private boolean savaUnlockPassword(String password) {

        int ret = 0;
        SharedPreferences sp = wosPlayerApp.appContext.getSharedPreferences(sharedPreferences_txt,wosPlayerApp.appContext.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sp.edit();
        mEditor.putString(PASSWORD_KEY,password);
        return mEditor.commit();

    }


    //获取本地密码
public static String getUnlockPassword(){
    SharedPreferences sp = wosPlayerApp.appContext.getSharedPreferences(sharedPreferences_txt,wosPlayerApp.appContext.MODE_PRIVATE);
    SharedPreferences.Editor mEditor = sp.edit();
    String psd = sp.getString(PASSWORD_KEY,defpassword);

    if (psd==null || psd.equals("")){
        psd = defpassword;
    }
    return psd;
}

}
