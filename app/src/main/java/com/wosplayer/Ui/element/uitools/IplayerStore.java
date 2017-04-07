package com.wosplayer.Ui.element.uitools;

import android.util.LruCache;

import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.app.Logs;

/**
 * Created by user on 2017/4/6.
 */

public class IplayerStore extends LruCache{

    private static final java.lang.String TAG = "组件缓存";

    private IplayerStore(int maxSize) {
        super(maxSize);
    }

    private static IplayerStore cache;
    public static IplayerStore getInstants(){
        if (cache==null){
            cache = new IplayerStore((int) (Runtime.getRuntime().maxMemory() / 3));//最大内存的1/3
        }
        return cache;
    }

    public void putIplayerToCache(java.lang.String key,IPlayer value){

        if (key == null || value == null){
            Logs.e(TAG,"无法缓存iplayer对象,key值不正确或者value不正确." );
        }else{
            this.put(key,value);
        }
    }
    public IPlayer getIplayerToCache(String key){
        return (IPlayer) this.get(key);
    }

    //这里应该可以有别的办法清理 暂时没实现
    public void clearCache(){
        if (this.size()>0){
            this.evictAll();
        }
    }




}
