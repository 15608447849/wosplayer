package com.wosplayer.Ui.performer;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.Ui.element.uitools.IplayerStore;
import com.wosplayer.app.AppUtils;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2016/7/25.
 */
public final class ContentFactory {
    private static final String TAG = "contentTanslater";


    /**
     * 节目类型
     */
    public static enum  ContentTypeEnum {
        interactive, fudianbank,webpage, url, rss,time, text, video, image;
        public boolean needsDown() {
            return this.compareTo(video) >= 0;
        }
    }

    private static String getType(ContentTypeEnum typeEnum){
        return String.valueOf(typeEnum);
    }

    /**
     * 初始化 反射具体内容 的映射信息
     * key = 内容的类型
     * value = 内容对应的视图player的类名
     */
    private static Map<String,String> referenceViewMap = new HashMap<String,String>();
    static{
        String packageName = "com.wosplayer.Ui.element.uiViewimp.";
        referenceViewMap.put(getType(ContentTypeEnum.image),packageName+"IImagePlayer");//图片
        referenceViewMap.put(getType(ContentTypeEnum.webpage),packageName+"IWebPlayer");//网页
        referenceViewMap.put(getType(ContentTypeEnum.fudianbank),packageName+"IWebPlayer");//富癫银行项目
        referenceViewMap.put(getType(ContentTypeEnum.video),packageName+"IVideoPlayer");//视频
        referenceViewMap.put(getType(ContentTypeEnum.text),packageName+"ITextPlayer");//走马灯
        referenceViewMap.put(getType(ContentTypeEnum.time),packageName+"ITimePlayer");//时间
        referenceViewMap.put(getType(ContentTypeEnum.interactive),packageName+"IinteractionPlayer");//互动
    }



    /**
     * 请放入 主线程
     */
    public static IPlayer tanslationAndStart(Context context,ViewGroup vp,DataList data, TimeCalls timrmanager){
        if (context==null || vp == null){
            Logs.e(TAG,"无法创建 iplayer ,环境不正确,请初始化 Activity");
            return null;
        }
        if (!AppUtils.checkUiThread()){
            Logs.e(TAG,"无法创建 iplayer ,请在U线程中使用");
            return null;
        }

        IPlayer iplay = null;
        try {
            //查看缓存是否存在
            String key = data.getKey();
            //Logs.i(TAG,"iplayer key:"+key);
            iplay = IplayerStore.getInstants().getIplayerToCache(key);
            //Logs.i(TAG,"视图 缓存 队列 是否存在 :"+iplay);
            if (iplay == null) {
                //先获取 type
                String fileproterty = data.GetStringDefualt("fileproterty","");
                //Logs.i(TAG,"类型 :"+fileproterty);
                if (!referenceViewMap.containsKey(fileproterty)){
                    Log.e(TAG,"无法执行转换的数据的类型:[" + fileproterty+"]");
                    return iplay;
                }
                //创建iplayer
                //获取全类名
                String className = referenceViewMap.get(fileproterty);
                //创建 类实体
                Class cls = Class.forName(className);//得到类
                Constructor constructor = cls.getConstructor(Context.class, //得到构造
                        ViewGroup.class);

                iplay = (IPlayer) constructor.newInstance(context,vp); //得到具体实例

                //添加到 缓存中
                IplayerStore.getInstants().putIplayerToCache(key,iplay);
            }

            //Logs.i(TAG,"执行对象:[" + iplay +"]");
            //执行它
            iplay.loadData(data);
            iplay.setTimerCall(timrmanager);
            iplay.start();//主线程执行

        } catch (ClassNotFoundException e) {
            Logs.e(TAG,"无法找到这个类:"+e.getMessage());
        }catch(NoSuchMethodException e){
            Logs.e(TAG,"不匹配类构造 : "+e.getMessage());
        }
        catch (Exception e) {
           Logs.e(TAG," err:" + e.getMessage());
        }
        return iplay;
    }

}
