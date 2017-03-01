package com.wosplayer.Ui.element.interactive.iCache;


import com.wosplayer.app.PlayApplication;

/**
 * Created by user on 2016/7/12.
 */
public class InteractionCache {
    private static final java.lang.String TAG = "互动缓存 : ";//InteractionCache.class.getName();
    public static String uid;

    //存
    public static void push(String key, String value){
        String k = key +"#"+uid+"#";
        XmlSharedSave.SaveXmlData(PlayApplication.appContext,k,value);
        XMLCache.GetOb().saveCache(k,value);
//        log.i(TAG,"本地存入:["+k+"],value:["+value+"]");
    }

    //取
    public static String pull(String key){
        String k = key +"#"+uid+"#";
//        log.i(TAG,"->  key:["+k+"]");
        String v = "";
       v = XMLCache.GetOb().getCache(k) ==null ? XmlSharedSave.readXmlData(PlayApplication.appContext,k) : XMLCache.GetOb().getCache(k) ;
//        log.i(TAG,"本地:["+v+"]");
        return v.equals("")? null:v;
    }

}
