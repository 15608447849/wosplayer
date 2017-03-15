package com.wosplayer.download.kernal;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.wosplayer.app.Logs;
import com.wosplayer.command.kernal.CommandCenter;
import com.wosplayer.command.operation.interfaces.CommandType;
import com.wosplayer.download.operation.Task;
import com.wosplayer.download.operation.TaskQueue;
import com.wosplayer.service.UpdateApkServer;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/7/21.
 *  1.是一个intentService
 *  2.维护一个count , 当count 与 当前所需要下载的任务相同的时候 发送一个 下载完成广播
 *
 */
public class DownloadManager extends IntentService
{

    public static final String KEY_TYPE="type";
    public static final int KEY_TYPE_SCHDULE = 1;
    public static final int KEY_TYPE_UPDATE_APK = 2;
    public static final int KEY_TYPE_FFBK = 3;


    public static final String KEY_TERMINAL_NUM="terminalNo";
    public static final String KEY_SAVE_PATH="savepath";
    public static final String KEY_ALIAS="alias";
    public static final String KEY_TASK_LIST="loaderTaskArr";
    public static final String KEY_TASK_SINGLE="loaderTasksingle";

    private static final String TAG = "_loaderManager";
    public DownloadManager() {
        super(TAG);
    }
    private static ReentrantLock lock = new ReentrantLock();
    @Override
    public void onCreate() {
        super.onCreate();
        Logs.i("--------------------------------------------下载管理员 onCreate--------------------------------------------------------");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.i("-------------------------------------------下载管理员 onDestroy----------------------------------------------------------");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Bundle bundle = intent.getExtras();
        int loadType = bundle.getInt(KEY_TYPE);
        String terminalNo = bundle.getString(KEY_TERMINAL_NUM,"");//终端id
        String savepath = bundle.getString(KEY_SAVE_PATH,"");//保存路径
        String alias = bundle.getString(KEY_ALIAS,"");//别名
        ArrayList<CharSequence> taskList = bundle.getCharSequenceArrayList(KEY_TASK_LIST);//任务列表
        String singUrl = bundle.getString(KEY_TASK_SINGLE,"");//单一任务


        if (loadType == KEY_TYPE_SCHDULE){
            //排期发来的资源下载列表
            scheduleSourceLoad(terminalNo,savepath,taskList);
        }else
        if (loadType == KEY_TYPE_UPDATE_APK){
            //更新apk
            updateAPK(terminalNo,savepath,singUrl);
        }else
        if (loadType == KEY_TYPE_FFBK){
            //富滇银行 资源文件下载
            loadResource(terminalNo,savepath,singUrl,alias);
        }else{
           Logs.d(TAG, "onHandleIntent: 错误的下载任务类型");
        }
    }

    //下载富癫银行的资源文件
    private void loadResource(String terminalNo, String savepath, String singUrl, String alias) {
        Logs.i(TAG,"下载 web resouce - "+singUrl+"\n savepath - "+savepath+"\n terminalNo - "+terminalNo+"\n alias: "+alias);


       Task task =  Task.TaskFactory.createFtpTask(terminalNo,singUrl,savepath,alias,true);
        TaskQueue.getInstants().addTask(task);
    }

    //更新apk
    private void updateAPK(String terminalNo, String savepath, String singUrl) {
        Logs.i(TAG,"下载 apk - "+singUrl+"\n savepath - "+savepath+"\n terminalNo - "+terminalNo);
        Task task =  Task.TaskFactory.createFtpTask(terminalNo,singUrl,savepath,"update.apk",true);
        task.setResult(new Task.TaskResult() {
            @Override
            public void onComplete(Task task) {
                try {
                    String lpath = task.getLocalPath()+task.getLocalName();
                    Logs.i(TAG,"下载apk成功  - 路径- "+ task.getLocalPath()+task.getLocalName() );

                    Bundle bundle = new Bundle();
                    bundle.putString(UpdateApkServer.APK_PATH,lpath);
                    Intent intent = new Intent(getApplicationContext(),UpdateApkServer.class);
                    intent.putExtras(bundle);
                    getApplicationContext().startService(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        TaskQueue.getInstants().addTask(task);
    }

    private void scheduleSourceLoad(String terminalNo,String savepath,ArrayList<CharSequence> taskList) {
        Log.i(TAG,"收到一个 排期资源下载队列, 队列大小:"+taskList.size()+" ;terminalNo="+terminalNo+"\n savepath = "+savepath);
        for (int i = 0;i<taskList.size();i++){

            Task task = Task.TaskFactory.createMutTask(terminalNo,savepath,(String)taskList.get(i));
            if (task!=null){
                if (i == taskList.size()-1){
                    task.setResult(new Task.TaskResult() {
                        @Override
                        public void onComplete(Task task) {
                            //发送广播通知下Ui 下载素材完毕
                            Intent intent = new Intent();
                            intent.setAction(CommandCenter.action);
                            Bundle bundle = new Bundle();
                            bundle.putString(CommandCenter.cmd, CommandType.UPSC);
                            bundle.putString(CommandCenter.param,"_notify");
                            intent.putExtras(bundle);
                            getApplicationContext().sendBroadcast(intent);
                        }
                    });
                }
                TaskQueue.getInstants().addTask(task);
            }
        }
    }


}
