package com.wosplayer.loadArea;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.wos.Toals;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.excuteBolock.Loader;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by user on 2016/7/21.
 *  1.是一个intentService
 *  2.维护一个count , 当count 与 当前所需要下载的任务相同的时候 发送一个 下载完成广播
 *
 */
public class loaderManager extends IntentService implements Loader.LoaderCaller{

    public static final String taskKey = "loaderTaskArr";
    private static final String TAG = "_loaderManager";

    private static boolean isLoadding = false;

    private static  ArrayList<ArrayList<CharSequence>>  storeList = new ArrayList<ArrayList<CharSequence>>();

    private static void addItemToStore(ArrayList<CharSequence> taskList){
        if (storeList.contains(taskList)){
            log.e(TAG,"已存在的任务队列");
            return;
        }
        storeList.add(taskList);
    }

    private static  ArrayList<CharSequence> getTaskAndDelete(){
        ArrayList<CharSequence> tasklist = null;
        if(storeList.size()==0){
            log.e(TAG,"没有 存储的 任务队列");
            return tasklist;
        }

        //每次取出一个
        if (storeList.size()>0){//至少存在一个
            Iterator<ArrayList<CharSequence>> itr = storeList.iterator();

            if (itr.hasNext()){//cun zai xia yi ge
                tasklist = itr.next();//get
                log.e(TAG,"存在 存储的任务队列");
                itr.remove();//delete
            }
        }
        return tasklist;
    }

    public loaderManager() {
        super("loaderManager");
    }

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

    private ArrayList<CharSequence> TaskList = null;
    @Override
    protected void onHandleIntent(Intent intent) {


        TaskList =   intent.getExtras().getCharSequenceArrayList(taskKey);

        if (TaskList == null || TaskList.size() == 0) return;
        Log.i(TAG,"收到一个 下载任务, 队列大小:"+TaskList.size());

        WorkEvent();

    }

    private void WorkEvent() {
        if (isLoadding){
            //下载中 存储
            addItemToStore(TaskList);
            return;
        }
        isLoadding = true;
        Loader loader = null;
        for (int i = 0;i<TaskList.size();i++){
            log.d(TAG,"--- 下載任务名["+TaskList.get(i) +"]  - index [ "+i+" ] ---");
            loader = new Loader();
            loader.settingCaller(this);//设置回调
            loader.LoadingUriResource((String) TaskList.get(i),null);// 开始任务
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
            Toals.Say("下载任务全部完成 发送通知");
            //发送完成通知
            Intent intent = new Intent();
            intent.setAction(completeTaskListBroadcast.action);
            getApplicationContext().sendBroadcast(intent);
            isLoadding = false;
            NotificationStoreList();
        }
    }

    private void NotificationStoreList() {
        if (storeList.size()==0){
            return;
        }
        if (TaskList!=null){
            TaskList = null;
        }

        TaskList = getTaskAndDelete();


    }

}
