package com.wosplayer.loadArea;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.wosplayer.app.log;
import com.wosplayer.loadArea.excuteBolock.Loader;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/7/21.
 *  1.是一个intentService
 *  2.维护一个count , 当count 与 当前所需要下载的任务相同的时候 发送一个 下载完成广播
 *
 */
public class loaderManager extends IntentService implements Loader.LoaderCaller{

    public static final String taskKey = "loaderTaskArr";
    private static final String TAG = "_loaderManager";
    public loaderManager() {
        super("loaderManager");
    }
    private static ReentrantLock lock = new ReentrantLock();
    @Override
    public void onCreate() {
        super.onCreate();
        log.i("--------------------------------------------loaderManager create--------------------------------------------------------");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        log.i("-------------------------------------------loaderManager destroy----------------------------------------------------------");
    }
    private String terminalNo ;
    private String savepath ;
    private ArrayList<CharSequence> TaskList = null;
    @Override
    protected void onHandleIntent(Intent intent) {
        TaskList =   intent.getExtras().getCharSequenceArrayList(taskKey);
        if (TaskList == null || TaskList.size() == 0) return;
        terminalNo = intent.getExtras().getString("terminalNo","0000");
        savepath = intent.getExtras().getString("savepath","0000");
        Log.i(TAG,"收到一个 下载队列, 队列大小:"+TaskList.size()+"\n terminalNo="+terminalNo+"\nsavepath="+savepath);
        WorkEvent();
    }
    private void WorkEvent() {
        try {
            lock.lock();

            Loader loader = null;
            for (int i = 0;i<TaskList.size();i++){
                log.d(TAG,"--- 下載任务名["+TaskList.get(i) +"]  - index [ "+i+" ] ---");
                loader = new Loader(savepath,terminalNo);
                loader.settingCaller(this);//设置回调
                loader.LoadingUriResource((String) TaskList.get(i),null);// 开始任务
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    private int SuccessCount = 0;
    @Override
    public void Call(String filePath) {
        log.i(TAG,"current count :["+ SuccessCount++ +"] ,sumCount:["+TaskList.size()+"]");
        if(filePath.equals("404")){
            log.e(TAG,"load faild :["+filePath +"]-\n\r");
        }
            if (SuccessCount == TaskList.size()){
            log.i(TAG,"任务完成 发送通知");
            //发送完成通知
            Intent intent = new Intent();
            intent.setAction(completeTaskListBroadcast.action);
            getApplicationContext().sendBroadcast(intent);
        }
    }
}
