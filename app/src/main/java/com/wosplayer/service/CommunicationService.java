package com.wosplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.CmdPostTaskCenter;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import rx.Scheduler;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/7/19.
 */

public class CommunicationService extends Service{

    private static final java.lang.String TAG = CommunicationService.class.getName();
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private Socket socket;
    private String ip;
    private int port;// 6666
    private String terminalNo;
    private long HeartBeatTime;
    private boolean isConnected = false;
    private ReentrantLock msgLock = new ReentrantLock();
    private boolean isReconnection = false;//是否可以尝试重连接

    private static final Scheduler.Worker connectHelper =  Schedulers.newThread().createWorker();


    private static ReentrantLock msgStoreLock = new ReentrantLock();//消息存储锁
    private List<String> msgWatiList = null;//消息过多时 存储消息
    private List<String> msgSendingList = null; //消息待发送队列
    private int sendCount = 10;

        //添加一个消息
        private void addMsgToSend(String msg){
            try {
                msgStoreLock.lock();
                //如果发送队列消息过多 进入存储
                if (msgSendingList!=null && msgSendingList.size()<10){
                    msgSendingList.add(msg);
                }else{
                    if (msgWatiList!=null){
                        msgWatiList.add(msg);
                    }
                }
            }catch (Exception e){
                log.e(TAG,e.getMessage());
            }finally {
                msgStoreLock.unlock();
            }
        }

        //获取一个消息
        private String getMsg(){
            String msg = null;
            try{
                msgStoreLock.lock();
                if (msgSendingList!=null && msgSendingList.size()>0){

                    Iterator<String> itr = msgSendingList.iterator();
                    if (itr.hasNext()){
                        msg = itr.next();
                        itr.remove();
                    }
                }
                else if (msgSendingList!=null && msgSendingList.size()==0){

                    if (msgWatiList!=null && msgWatiList.size()>0){
                        int index = 0;
                        Iterator<String> itr = msgWatiList.iterator();
                        while(itr.hasNext()){
                            if (index == sendCount){
                                break;
                            }
                            String str = itr.next();
                            msgSendingList.add(str);
                            itr.remove();
                            index++;
                        }
                    }
                }

            }catch (Exception e){
                log.e(e.getMessage());
            }finally {
                msgStoreLock.unlock();
            }
            return msg;
        }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        log.i("-------------------------------------------开启通讯服务-------------------------------------------------------------------------");
        isReconnection = true;
        msgWatiList = new LinkedList<String>();
        msgSendingList = new LinkedList<String>();
    }
/////////////////////////////////////////////////////////////////////////////////////
    /**
     * 接受消息 线程
     */
    private receiveThread receiveMsg = null;
    /**
     * 接受消息的线程
     */
    private class  receiveThread extends Thread{
        //receive service to me msg
        private volatile boolean isStart = false;
        public void startMe(){
            isStart =true;
        }
        public void stopMe(){
            isStart =false;
        }
        @Override
        public void run() {
            while(isConnected && isStart){
                //只要是连接中
                try {
                    if (dataInputStream.available() > 0) {
                        String msg = dataInputStream.readUTF();
                        log.i("收到 服务器 参数:" + msg);
                        String cmd = msg.substring(0, 5);
                        String param = msg.substring(5);
                        postTask(cmd,param);
                    }
                }catch (Exception e){
                    log.e("接受服务端消息 错误 :"+ e.getMessage());
                 //   CommunicationService.this.stopSelf();
                    reConnection();
                }
            }
        }
    }

    private sendThread sendmsgThread = null;
    //发送消息 线程
    private class sendThread extends Thread{

        private volatile boolean isStart = false;
        public void startMe(){
            isStart =true;
        }
        public void stopMe(){
            isStart =false;
        }

        @Override
        public void run() {
            while(isConnected && isStart){
                //在连接中 并且 开始了

                try{
                    msgLock.lock();

                    //获取一个消息
                  String msg =  getMsg();
                    if (msg != null){
                        dataOutputStream.writeUTF(msg);
                        dataOutputStream.flush();
                        log.v("发送一条信息到服务器 :" + msg);
                    }

                    Thread.sleep(1*500);//一秒发送两条信息

                }catch (Exception e){
                    log.e("发送消息到服务器 错误 :"+ e.getMessage());//尝试重新链接
                    //重连接
                    reConnection();
                }finally {
                    msgLock.unlock();
                }
            }
        }

    }



















    /**
     * 开启 接受
     */
    private void startReceiveThread(){
        stopReceiveThread();
        receiveMsg = new receiveThread();
        receiveMsg.startMe();
        receiveMsg.start();
        //发送线程
        sendmsgThread = new sendThread();
        sendmsgThread.startMe();
        sendmsgThread.start();
    }
    /**
     * 关闭接受
     */
    private void stopReceiveThread(){
        if (receiveMsg != null){
            receiveMsg.stopMe();
            receiveMsg = null;
            log.i("断开 接受消息 线程");
        }
        if (sendmsgThread!=null){
            sendmsgThread.stopMe();
            sendmsgThread = null;
            log.i("断开 发送消息 线程");
        }
    }
/////////////////////////////////////////////////////////////////////////////////////
    /**
     * 分发任务
     * @param cmd
     * @param param
     */
    private void postTask(String cmd, String param) {
        log.i("派发任务:"+cmd);
        Intent i = new Intent();
        i.setAction(CmdPostTaskCenter.action);
        Bundle b = new Bundle();
        b.putString(CmdPostTaskCenter.cmd,cmd);
        b.putString(CmdPostTaskCenter.param,param);
        i.putExtras(b);
        getApplicationContext().sendBroadcast(i);
    }
/////////////////////////////////////////////////////////////////////////////////////
    /**
     * 返回值
     从Android官方文档中，知道onStartCommand有4种返回值：
     START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
     START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务。
     START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
     START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         if (intent == null){
             log.e(TAG,"連接服務 未傳遞 intent");
             stopSelf();
             return START_REDELIVER_INTENT;
         }
        ip =  intent.getExtras().getString("ip");
        port = intent.getExtras().getInt("port");
        terminalNo = intent.getExtras().getString("terminalNo");
        HeartBeatTime = intent.getExtras().getLong("HeartBeatTime");
        if (ip==null || terminalNo==null){
            log.e(TAG,"連接服務  intent 參數不正確");
           return START_STICKY;
        }
        connectHelper.schedule(new Action0() {
            @Override
            public void call() {
                startCommunication();
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }
    /**
     * 开始通讯
     */
    private void startCommunication(){
        log.i(TAG,"当前所在线程:"+Thread.currentThread().getName()+"当前线程数量:"+Thread.getAllStackTraces().size());
        //连接
        CreaterSocket();
        if (!isConnected){ //如果没连接上 可以滚蛋了
             return;
        }

        //创建接受消息线程
        startReceiveThread();
        //注册发送消息的广播
        registSendBroad();
        //开始心跳
        startHeartbeat();
        //发送上线指令
        String msg = "ONLI:" + terminalNo;//
        sendMsgToService(msg);
    }
    /**
     * 结束通讯
     */
    private void stopCommunication(){
        //结束指令
        //发送下线通知
        String msg = "OFLI:" + terminalNo;
        sendMsgToService(msg);

        //注销广播
        unregistSSendBroad();
        //断开 接受消息 线程
        stopReceiveThread();
        //结束心跳
        stopHeartbeat();

        //结束链接
        desconnection();
    }
    /////////////////////////////////////////////////////////////////////////////////////
    //创建链接
    private void CreaterSocket() {
        //如果已连接 断开连接
        if (isConnected){
            desconnection();
        }
        try {
            socket = new Socket(ip , port);
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            dataInputStream    = new DataInputStream(socket.getInputStream());
            isConnected=true;
            log.i("Communication connectToServer :" +ip+":"+port);
        } catch (IOException e) {
            log.e(" socket connection err:"+ e.getMessage());
            isConnected=false;
            reConnection();//重新链接
        }
    }
    //尝试重新链接
    private void reConnection() {

        if (!isReconnection){
            log.i("不可连接");
            return;
        }

        log.i("尝试重新链接中...");
        stopCommunication();
        try {
            Thread.sleep(30*1000);
        } catch (InterruptedException e) {
            log.e("重新链接失败"+e.getMessage());
            reConnection();
            return;
        }
        connectHelper.schedule(new Action0() {
            @Override
            public void call() {
                startCommunication();
            }
        });
    }
    /**
     *断开链接
     */
    private void desconnection(){
        if (socket != null) {
            try {
                socket.close();
                socket=null;
            } catch (Exception e) {
                log.e("Communication disconnect error :" + e.getMessage());
            }
        }
        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();
                dataOutputStream=null;
            } catch (Exception e) {
                log.e("Communication disconnect error :" + e.getMessage());
            }
        }
        if (dataInputStream != null) {
            try {
                dataInputStream.close();
                dataInputStream=null;
            } catch (IOException e) {
                log.e("Communication disconnect error :" + e.getMessage());
            }
        }
        isConnected = false;
    }

    /**
     *发送信息
     */
    private void sendMsgToService(final String msg){

        //添加消息到消息队列
        addMsgToSend(msg);

      /*  if (isConnected){
            try {
            msgLock.lock();
            dataOutputStream.writeUTF(msg);
            dataOutputStream.flush();
            log.i("发送一条信息到服务器 :" + msg);
            } catch (Exception e) {
                e.printStackTrace();
                log.e("发送信息失败 error :" + e.getMessage());
                //重连接
                reConnection();
            }finally {
            msgLock.unlock();
            }
        }*/
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //广播
    private CommunicationServiceReceiveNotification broad = null;
    /**
     * 请注册在使用
     */
    public class CommunicationServiceReceiveNotification extends BroadcastReceiver{

        public static final String action = "com.send.message.broad";
        public static final String key = "toService";

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getExtras().getString(key);
//        log.i("收到一个广播..."+msg);
        if (msg==null) return;
        sendMsgToService(msg);
    }
}
    /**
     * 注册广播
     */
    private void registSendBroad() {
        unregistSSendBroad();
        broad = new CommunicationServiceReceiveNotification();
        IntentFilter filter=new IntentFilter();
        filter.addAction(CommunicationServiceReceiveNotification.action);
        getApplicationContext().registerReceiver(broad, filter); //只需要注册一次
        log.i("已注册 接受广播");
    }
    /**
     * 注销广播
     */
    private void unregistSSendBroad() {
        if (broad!=null){
            getApplicationContext().unregisterReceiver(broad);
            broad = null;
            log.i("注销 接受广播");
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    //发送心跳定时器
   private Timer timer = null;
    private TimerTask timertask = null;


    //开启 心跳
    private void startHeartbeat(){
        stopHeartbeat();
        timer = new Timer();
        timertask  =  new TimerTask() {
            @Override
            public void run() {
                    String msg = "HRBT:"+terminalNo;
                    sendMsgToService(msg);
            }
        };

        //创建定时器任务 发送心跳
        timer.schedule(timertask, 100, HeartBeatTime);
    }
    //关闭心跳
    private void stopHeartbeat(){
        if (timer != null){
            //关闭定时器
            timer.cancel();
            timer = null;
        }
        if (timertask!=null){
            timertask.cancel();
            timertask = null;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onDestroy() {
        super.onDestroy();
        isReconnection = false;
        log.e("--------------------------------------------------------------通讯服务 停止了-------------------------------------------------------------------------");
       stopCommunication();
    }
}
