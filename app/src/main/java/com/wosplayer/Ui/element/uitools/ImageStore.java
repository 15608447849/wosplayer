package com.wosplayer.Ui.element.uitools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import com.wosplayer.R;

/**
 * Created by user on 2016/12/2.
 */

public class ImageStore {
    private static final String TAG = "图片缓存";
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
                    if (evicted){
                        removeImageCache(key);
                    }

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
        Bitmap bitmap = CacheMap.get(tag);

        if (bitmap==null || bitmap.isRecycled()){
            return null;
        }
        return bitmap;
    }

    //添加 一个 缓存 view
    public  synchronized  void  addBitmapCache(String tag,Bitmap bitmap){
        try{
            if (bitmap==null){
                return;
            }
            if (CacheMap.get(tag) == null || CacheMap.get(tag).isRecycled()) {
               // Log.i(TAG,"添加 bitmap - key - "+tag +"\n bitmap - "+bitmap);
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


    /**
     * 获取 返回按钮
     */
    public Bitmap getButton_back(Context context){
        Bitmap bitmap = ImageStore.getInstants().getBitmapCache("returnBtn");
        if (bitmap==null || bitmap.isRecycled()){
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.back);
            ImageStore.getInstants().addBitmapCache("returnBtn",bitmap);
        }
        return bitmap;
    }



    /**
     * 获取左键
     */
    public Bitmap getButton_left(Context context){
        Bitmap bitmap = ImageStore.getInstants().getBitmapCache("btn_left");
        if (bitmap==null || bitmap.isRecycled()){
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.left);
            ImageStore.getInstants().addBitmapCache("btn_left",bitmap);
        }
        return bitmap;
    }

    /**
     * 获取右键
     */
    public Bitmap getButton_right(Context context){
        Bitmap bitmap = ImageStore.getInstants().getBitmapCache("btn_rigth");
        if (bitmap==null || bitmap.isRecycled()){
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.right);
            ImageStore.getInstants().addBitmapCache("btn_rigth",bitmap);
        }
        return bitmap;
    }


}
