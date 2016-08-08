package com.wosplayer.Ui.performer;

import android.content.Context;
import android.util.Log;
import android.util.LruCache;
import android.view.ViewGroup;

import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2016/7/25.
 */
public final class contentTanslater {
    private static final String TAG = "_contentTanslater";

    /**
     * 初始化 反射具体内容 的映射信息
     * key = 内容的类型
     * value = 内容对应的视图player的类名
     */
    private static Map<String,String> referenceViewMap = new HashMap<String,String>();
    static{
        String packageName = "com.wosplayer.Ui.element.iviewelementImpl.";
        referenceViewMap.put("image",packageName+"IImagePlayer");
        referenceViewMap.put("webpage",packageName+"IWebPlayer");
        referenceViewMap.put("video",packageName+"IVideoPlayer");
        referenceViewMap.put("text",packageName+"IrunTextPlayer");
        referenceViewMap.put("interactive",packageName+"IinteractionPlayer");
//        referenceViewMap.put("interactive","com.wosplayer.Ui.element.iviewelementImpl.actioner."+"Actioner");
    }

    //存储一部分视图
    private static LruCache<String,IPlayer> mLruCache = null;
    private static void putIplayerToCache(String key,IPlayer value){
        mLruCache.put(key,value);
    }
    private static IPlayer getIplayerToCache(String key){
        return  mLruCache.get(key);
    }

    public static void clearCache(){
        mLruCache = null;
        log.i(TAG,"清理缓存");
    }

    /**
     * 请放入 主线程
     * @param list
     * @return
     */
    public static IPlayer tanslationAndStart(DataList list,Object ob){
        if (mLruCache==null){
            mLruCache =  new LruCache<String,IPlayer>((int) (Runtime.getRuntime().maxMemory() / 8));//最大内存的1/3
        }
        log.i(TAG,"准备转换视图控件,所在线程:"+Thread.currentThread().getName());
        IPlayer iplay = null;

        try {
            //查看缓存是否存在
            String key = list.getKey();
            log.i(TAG,"iplayer key:"+key);
            iplay = getIplayerToCache(key);
            log.i(TAG,"视图 缓存 队列 是否存在 :"+iplay);
            if (iplay == null) {
                //先获取 type
                String fileproterty = list.GetStringDefualt("fileproterty","");
                log.i(TAG,"类型 :"+fileproterty);
                if (!referenceViewMap.containsKey(fileproterty)){
                    Log.e(TAG,"无法执行的类型" + fileproterty);
                    return iplay;
                }
                //创建iplayer
                //获取全类名
                String className = referenceViewMap.get(fileproterty);
                //创建 类实体
                Class cls = Class.forName(className);//得到类
                Constructor constructor = cls.getConstructor(Context.class, //得到构造
                        ViewGroup.class);
                if (DisplayActivity.activityContext == null || DisplayActivity.main ==null){
                    log.e(TAG,"无法创建 iplayer ,环境不正确,请初始化 Activity");
                    return iplay;
                }
                iplay = (IPlayer) constructor.newInstance(DisplayActivity.activityContext, (ViewGroup) DisplayActivity.main); //得到具体实例

                //添加到 缓存中
                putIplayerToCache(key,iplay);
            }
            //执行它
            iplay.loadData(list,ob);
            iplay.start();//主线程执行
        } catch (ClassNotFoundException e) {
            log.e(TAG,"无法找到这个类");
        }catch(NoSuchMethodException e){
            log.e(TAG,"不匹配类构造 ");
        }
        catch (InstantiationException e) {
           log.e(TAG," err:" + e.getMessage());
        } catch (IllegalAccessException e) {
            log.e(TAG," err:" + e.getMessage());
        } catch (InvocationTargetException e) {
            log.e(TAG," err:" + e.getMessage());
        }
        return iplay;
    }

}
