package com.wosplayer.loadArea.kernal;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.UPDCbroad;
import com.wosplayer.loadArea.TASKLIST.Task;
import com.wosplayer.loadArea.TASKLIST.TaskQueue;
import com.wosplayer.loadArea.excuteBolock.LoaderCall;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/7/21.
 *  1.是一个intentService
 *  2.维护一个count , 当count 与 当前所需要下载的任务相同的时候 发送一个 下载完成广播
 *
 */
public class loaderManager extends IntentService
{

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
    private String updcUrl;
    @Override
    protected void onHandleIntent(final Intent intent) {
        TaskList =   intent.getExtras().getCharSequenceArrayList(taskKey);
        terminalNo = intent.getExtras().getString("terminalNo","");
        savepath = intent.getExtras().getString("savepath","");

        if (TaskList == null || TaskList.size() == 0){
            updcUrl = intent.getExtras().getString("UPDC","");
            if (updcUrl==null || "".equals(updcUrl)){
                return;
            }else{
                Log.i(TAG,"下载升级apk - >>>"+updcUrl+"\nsavepath - "+savepath+"\nterminalNo - "+terminalNo);
                TaskQueue.getInstants().addTask(new Task(savepath, terminalNo, updcUrl, new LoaderCall() {
                    @Override
                    public void downloadResult(String filePath, String state) {
                        try {
                            log.i(TAG,"下载成功 升级apk - 路径- "+filePath );
                            sendUPDCBroad(filePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }));
            }
        }else{
            Log.i(TAG,"收到一个 下载队列, 队列大小:"+TaskList.size()+"\n terminalNo="+terminalNo+"\nsavepath="+savepath);
            testWork();
        }

    }

    private void testWork() {
        for (int i = 0;i<TaskList.size();i++){
            TaskQueue.getInstants().addTask(new Task(savepath,terminalNo,(String)TaskList.get(i),null));
        }
        Intent intent = new Intent();
        intent.setAction(completeTaskListBroadcast.action);
        getApplicationContext().sendBroadcast(intent);
    }

    public void sendUPDCBroad(String filepath){
        Intent intent = new Intent();
        intent.setAction(UPDCbroad.ACTION);
        Bundle bundle = new Bundle();
        bundle.putString("updc",filepath);
        intent.putExtras(bundle);
        getApplicationContext().sendBroadcast(intent);
    }
}
