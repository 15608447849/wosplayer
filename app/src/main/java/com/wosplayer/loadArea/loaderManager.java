package com.wosplayer.loadArea;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.wosplayer.app.log;
import com.wosplayer.loadArea.excuteBolock.Loader;

import java.util.ArrayList;

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

    private boolean isComplete = false;
    @Override
    public void onCreate() {
        super.onCreate();
        log.i("loaderManager create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.i("loaderManager destroy");
    }

    private ArrayList<CharSequence> TaskList = null;
    @Override
    protected void onHandleIntent(Intent intent) {


        TaskList =   intent.getExtras().getCharSequenceArrayList(taskKey);

        if (TaskList == null || TaskList.size() == 0) return;

        Log.i(TAG,"收到一个下载任务队列:"+TaskList.toString());

        for (int i = 0;i<TaskList.size();i++){

            Loader loader = new Loader();
            loader.settingCaller(this);
            loader.LoadingUriResource((String) TaskList.get(i),null);
            log.i(TAG," - - - 準備下載:"+TaskList.get(i) +" 次數:"+i);
        }

//        while(!isComplete){
//
//           log.i(TAG, "onHandleIntent: "+  Thread.currentThread().getName() +" 線程總數:"+Thread.getAllStackTraces().size());
//            try {
//                Thread.sleep(1*1000);
//            } catch (InterruptedException e) {
//             log.e(TAG,e.getMessage());
//            }
//        }
    }

    private int SuccessCount = 0;

    @Override
    public void Call(String filePath) {
        SuccessCount++;

        if(filePath.equals("404")){
            log.e(TAG,filePath + "-");
        }
        log.i(TAG,"current count :"+SuccessCount+" sumCount:"+TaskList.size());
        if (SuccessCount == TaskList.size()){
            log.i(TAG,"任务完成 发送通知");
            //发送完成通知
            Intent intent = new Intent();
            intent.setAction(completeTaskListBroadcast.action);
            getApplicationContext().sendBroadcast(intent);
            isComplete = true;
        }
    }

}
