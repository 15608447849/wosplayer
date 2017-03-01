package com.wosplayer.Ui.element.interactive.iCache;

import android.util.LruCache;

/**
 * Created by user on 2016/6/13.
 */
public class XMLCache {

    private static XMLCache xmlCache ;

    private LruCache<String,String> mLruCache;



    private XMLCache(){
       mLruCache  = new LruCache<String,String>((int) (Runtime.getRuntime().maxMemory() / 8));
    }

    public static XMLCache GetOb(){

        if (xmlCache==null){
            xmlCache = new XMLCache();
        }
        return  xmlCache;
    }


    //存 xml 文件
    public void saveCache(String key, String value){
        mLruCache.put(key,value);
    }

    //取 xml 文件
    public String getCache(String Key){
       return mLruCache.get(Key);

    }

}
