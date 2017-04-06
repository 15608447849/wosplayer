package com.wosplayer.app;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import static com.wosplayer.app.PlayApplication.appContext;

/**
 * Created by 79306 on 2017/3/6.
 */

public class PlayHandler extends Handler{
    private static final String TAG = "主线程handler";
    public enum HandleEvent{
        success,outtext,close,open
    }
    //对Activity的弱引用
    private WeakReference<DisplayActivity> mActivity;
    public PlayHandler(DisplayActivity activity){
        mActivity = new WeakReference<DisplayActivity>(activity);
        Log.i(TAG, "activity 关联 hanler 成功.");
    }
    @Override
    public void handleMessage(Message msg) {
       if (mActivity.get()==null) {
           Log.i(TAG, "handler 使用的 activity 不存在.");
           return;
       };
       final DisplayActivity activity = mActivity.get();
        if (msg.what == HandleEvent.outtext.ordinal()){
            AppUtils.Toals(activity,msg.obj.toString());
        }
        //配置页面消息显示获取终端id成功
        if (msg.what == HandleEvent.success.ordinal()){
            if (activity.mFragmentImp != null){
                activity.mFragmentImp.sendMessage(msg.obj);
            }
        }
        //关闭配置页面
        if (msg.what == HandleEvent.close.ordinal()){
            //关闭 配置服务信息的fragment
            activity.closeWosTools();
            //开始工作
            activity.start();
        }
        //打开配置页面
        if (msg.what == HandleEvent.open.ordinal()){
            if (activity.mFragmentImp !=null){
                AppUtils.Toals(appContext,"请设置播放器参数");
            }else{
                AppUtils.settingServerInfo(activity,false);
                //停止工作
                activity.stop(false);
                AppUtils.Toals(activity,"2秒后进入配置界面");
                //打开配置界面
                this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activity.openWosTools();
                    }
                },2*1000);
            }
        }
    }
}
