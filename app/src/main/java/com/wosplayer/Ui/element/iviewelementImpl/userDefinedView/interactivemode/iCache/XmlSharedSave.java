package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.iCache;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by user on 2016/6/12.
 */
public class XmlSharedSave {

    /**
     * 保存xml数据
     */
    public static void  SaveXmlData(Context c, String key, String Value){

        SharedPreferences xmlShared = c.getSharedPreferences("xmlDataList", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = xmlShared.edit();
        editor.putString(key, Value);
        editor.commit();

    }


    /**
     * 获取xml数据
     */
public static String readXmlData(Context c, String key){
    String flg = null;
    SharedPreferences xmlShared = c.getSharedPreferences("xmlDataList", Activity.MODE_PRIVATE);
    flg = xmlShared.getString(key,"");

    return flg;
}

}
