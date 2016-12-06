package com.wosplayer.Ui.element.iviewelementImpl.uitools;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

/**
 * Created by user on 2016/12/2.
 */

public class ImageStore {
    private static final String TAG = "image_cache";
    private static ImageStore instant;
    private boolean isInit = false;
    private ImageStore(){
        init();
    }
    public static ImageStore getInstants(){
        if (instant==null){
            instant = new ImageStore();
        }
        return instant;
    }
    private LruCache<String,Bitmap> CacheMap = null;
    private int maxMemory = 0;
    private int mCacheSize = 0;
    //初始化
    private void init() {
        if (isInit){
            return;
        }
        maxMemory = (int) Runtime.getRuntime().maxMemory();
        mCacheSize = maxMemory / 4;
        if(CacheMap == null){
            CacheMap = new LruCache<String,Bitmap>(mCacheSize){
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // 重写此方法来衡量每张图片的大小，默认返回图片数量。
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                }
                //当item被回收或者删掉时调用。该方法当value被回收释放存储空间时被remove调用， 或者替换item值时put调用，默认实现什么都没做。
                //true: 为释放空间被删除；false: put或remove导致
                @Override
                protected void entryRemoved(boolean evicted, String key,
                                            Bitmap oldValue, Bitmap newValue) {
                    removeImageCache(key);
                }
            };
        }
        isInit = true;
    }

    //获取 一个 缓存 view
    public synchronized  Bitmap getBitmapCache(String tag){
        if (CacheMap==null){
            return null;
        }
//        Logs.i(TAG,"获取 bitmap - key - "+tag);
        return CacheMap.get(tag);
    }

    //添加 一个 缓存 view
    public  synchronized  void  addBitmapCache(String tag,Bitmap bitmap){
        try{
            if (bitmap==null){
                return;
            }
            if (CacheMap.get(tag) == null || CacheMap.get(tag).isRecycled()) {
                Log.i(TAG,"添加 bitmap - key - "+tag +"\n bitmap - "+bitmap);
                CacheMap.put(tag,bitmap);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //清理缓存
    public void clearCache() {
        if (CacheMap != null) {
            if (CacheMap.size() > 0) {
//                Log.d("CacheUtils",
//                        "mMemoryCache.size() " + mMemoryCache.size());
                CacheMap.evictAll();
//                Log.d("CacheUtils", "mMemoryCache.size()" + mMemoryCache.size());
            }
        }
    }

    /**
     * 移除缓存
     *
     * @param key
     */
    public synchronized void removeImageCache(String key) {
        if (key != null) {
            if (CacheMap != null) {
                Bitmap bm = CacheMap.remove(key);
                if (bm != null)
                    bm.recycle();
            }
        }
    }

}
