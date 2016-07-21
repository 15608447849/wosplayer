package com.wosplayer.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by Administrator on 2016/7/19.
 */

public class appTools {

    private static final String TAG = appTools.class.getName();

    /**
     * 读取tools下的sharedPreferences文件
     * @param key
     * @returnc
     */
    public static String readShareDataTools(Context context,String key) {

        String result = "";
        Context wosTools_context = null;
        try {
            if (wosTools_context == null)
                wosTools_context = context.createPackageContext(
                        "com.wos.tools", Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            log.e(TAG,e.getMessage());
        }
        try {
            SharedPreferences sharedPreferences = wosTools_context
                    .getSharedPreferences("Data", Context.MODE_WORLD_READABLE
                            + Context.MODE_WORLD_WRITEABLE);
            result = sharedPreferences.getString(key, "");
        } catch (Exception e) {
            Log.e(TAG, "readToolsShareData." + e.getMessage());
        }
        return result;
    }









}
