package com.wosplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.CmdPostTaskCenter;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_UPDC;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

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
    private boolean send_offline = true;//发送下线通知
    private ReentrantLock msgStoreLock = new ReentrantLock();//消息队列锁
    private List<String> msgSendingList = Collections.synchronizedList(new LinkedList<String>()); //消息待发送队列

        //添加一个消息
        private void addMsgToSend(String msg){
            try {
                msgStoreLock.lock();
//                log.i(TAG,"准备添加消息: "+ msg);
                //如果发送队列消息过多 进入存储
                if (msgSendingList!=null ){
//                    log.i(TAG,"添加消息到 发送队列");
                    msgSendingList.add(msg);
                }
            }catch (Exception e){
              e.printStackTrace();
            }finally {
                msgStoreLock.unlock();
            }
        }
        //获取一个消息
        private String getMsg(){
            String message = null;
            try{
                msgStoreLock.lock();
                if (msgSendingList!=null && msgSendingList.size()>0){
                    Iterator<String> itr = msgSendingList.iterator();
                    if (itr.hasNext()){
                        message = itr.next();
                        itr.remove();
//                        log.i(TAG,"获取 到 发送队列消息:"+message);
                    }
                }
            }catch (Exception e) {
               e.printStackTrace();
            }finally {
                msgStoreLock.unlock();
            }
            return message;
        }
    /**
     * 绑定
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    /**
     * 创建
     */
    @Override
    public void onCreate() {
        super.onCreate();
        log.i("----------------------onCreate---------------------开启通讯服务-------------------------------------------------------------------------");

    }
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
            log.e(TAG,"連接服務 未傳遞 intent,启动失败");
        }else{
            ip =  intent.getExtras().getString("ip");
            port = intent.getExtras().getInt("port");
            terminalNo = intent.getExtras().getString("terminalNo");
            HeartBeatTime = intent.getExtras().getLong("HeartBeatTime") * 1000;
            log.i(TAG,"ip: "+ip+"\n端口: "+port+"\n终端号: "+terminalNo+"\n心跳时间 :"+ HeartBeatTime);
            if (ip==null || terminalNo==null){
                log.e(TAG,"連接服務  intent 參數不正確");
            }else{
               openConnServerThread();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 结束
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        log.e("--------------------------------------------------------------通讯服务 停止-------------------------------------------------------------------------");
        stopCommunication();
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
            String msg = null;
            String cmd = null;
            String param = null;
            while(isStart && isConnected){
                //只要是连接中
                try {
                    if (dataInputStream.available() > 0) {
                        msg = dataInputStream.readUTF();
                        log.i(" 收到 服务器 参数: " + msg);
                       cmd = msg.substring(0, 5);
                        param = msg.substring(5);
                        postTask(cmd,param);
                    }
                }catch (Exception e){
                    log.e("接受服务端消息 错误 :"+ e.getMessage());
                    reConnection();
                }
            }
        }
    }
/**************************************************************************/
    private sendThread sendmsgThread = null;
    //发送 消息 线程
    private class sendThread extends Thread{

        private volatile boolean isStart = false;
        public void startMe(){
            isStart =true;
        }
        public void stopMe(){
            isStart =false;
        }

        private  String msg = null;
        @Override
        public void run() {
            while(isConnected && isStart){
                //在连接中 并且 开始了
                try{

                    msg = null;
                    //获取一个消息
                    msg =  getMsg();
                    if (msg != null){
                        dataOutputStream.writeUTF(msg);
                        dataOutputStream.flush();
                        log.w(TAG," 发送一条信息到服务器 :" + msg);
//                        Thread.sleep(10);
                    }
                }catch (Exception e){
                    log.e(TAG,"发送消息到服务器 错误 :\n"+ e.getMessage());//尝试重新链接
                    e.printStackTrace();
                    //重连接
                    reConnection();
                }
            }
        }

    }
///////////////////////////////////////////////////////////////////////
    /**
     * 开启 接受消息,发送消息
     */
    private void startReceiveThreadAndSendThread(){
        stopReceiveThreadAndSendThread();
        receiveMsg = new receiveThread();
        receiveMsg.startMe();
        receiveMsg.start();
        //发送线程
        sendmsgThread = new sendThread();
        sendmsgThread.startMe();
        sendmsgThread.start();
    }
    /**
     * 关闭接受消息发送消息
     */
    private void stopReceiveThreadAndSendThread(){
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
    Intent i = new Intent();

    Bundle b = new Bundle();
    /**
     * 接收到服务器的命令,分发任务
     * @param cmd
     * @param param
     */
    private void postTask(String cmd, String param) {
        b.clear();
        log.i("命令:"+cmd+" 参数"+param);
        i.setAction(CmdPostTaskCenter.action);
        b.putString(CmdPostTaskCenter.cmd,cmd);
        b.putString(CmdPostTaskCenter.param,param);
        i.putExtras(b);
        getApplicationContext().sendBroadcast(i);
    }
/////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建连接
     */
    private boolean createConnect(){
        log.i(TAG,"创建连接中...createConnect()");
        //连接
        createrSocketConnect();
        if (!isConnected){ //如果没连接上
            //尝试重新链接
            log.i(TAG,"创建连接失败,稍后在尝试");
            return false;
        }
        return true;
    }
    /**
     * 开始通讯
     */
    private void startCommunication(){
        log.i(TAG,"startCommunication() >>>\n当前所在线程:"+Thread.currentThread().getName()+"\n当前线程数量:"+Thread.getAllStackTraces().size());
        //创建接受消息线程,发送消息线程
        startReceiveThreadAndSendThread();
        //注册发送消息的广播
        registSendBroad();
        //开始心跳
        startHeartbeat();
        //发送上线指令
        String msg = "ONLI:" + terminalNo;//
        sendMsgToService(msg);
        msg = "GVAY:" + terminalNo+"#"+ Command_UPDC.getLocalVersionCode();//通知获取版本号信息
        sendMsgToService(msg);
    }
    /**
     * 结束通讯
     */
    private void stopCommunication(){
        //结束心跳
        stopHeartbeat();
        //是否发送下线通知
        if(send_offline){
            String msg = "OFLI:" + terminalNo;
            sendMsgToService(msg);
        }
        //注销广播
        unregistSSendBroad();
        //断开 接受消息 线程,断开发送消息线程
        stopReceiveThreadAndSendThread();
        //结束链接
        desConnection();
        log.i(TAG,"---结束通讯---");
    }
    /////////////////////////////////////////////////////////////////////////////////////
    //创建链接
    private void createrSocketConnect() {
        //如果已连接 断开连接
        if (isConnected){
            desConnection();
        }
        try {
            if (socket==null){
                log.i(TAG,"尝试创建socket 连接...");
                socket = new Socket(ip , port);
                dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                dataInputStream    = new DataInputStream(socket.getInputStream());
                isConnected=true;
                log.i("Communication connectToServer success : \n" +ip+":"+port);
            }
        } catch (IOException e) {
            log.e(" socket connection err: "+ e.getMessage());
            e.printStackTrace();
            isConnected=false;//完成连接
        }
    }
    private boolean isReconne = false; //是否重新连接中
    //尝试重新链接
    private void reConnection() {
        if (isReconne){
            log.i("正在尝试连接,不要重复尝试...");
            return;
        }
        isReconne = true;
        log.i("尝试重新链接中...");
        send_offline = false;//不发送下线通知
        stopCommunication();
        openConnServerThread();
        isReconne =false;
    }
    /**
     *断开链接
     */
    private void desConnection(){
        log.i(TAG,"断开连接中....");
        if (socket != null) {
            try {
                socket.close();
                socket=null;
            } catch (Exception e) {
                log.e("Communication disconnect error : " + e.getMessage());
            }
        }
        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();
                dataOutputStream=null;
            } catch (Exception e) {
                log.e("Communication disconnect error : " + e.getMessage());
            }
        }
        if (dataInputStream != null) {
            try {
                dataInputStream.close();
                dataInputStream=null;
            } catch (IOException e) {
                log.e("Communication disconnect error : " + e.getMessage());
            }
        }
        isConnected = false;//不在连接中
    }
    /**
     *  发送信息
     */
    private void sendMsgToService(final String msg){
        //添加消息到消息队列
        addMsgToSend(msg);
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //广播
    private CommunicationServiceReceiveNotification broad = null;
    /**
     *  请注册在使用
     */
    public class CommunicationServiceReceiveNotification extends BroadcastReceiver{
        public static final String action = "com.send.message.broad";
        public static final String key = "toService";
        private String msg = null;
    @Override
    public void onReceive(Context context, Intent intent) {
        msg = intent.getExtras().getString(key);
        if (msg==null) return;
        sendMsgToService(msg);
        msg = null;
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
        log.i("已注册 接受本地 到服务器 ,广播");
    }
    /**
     * 注销广播
     */
    private void unregistSSendBroad() {
        if (broad!=null){
            getApplicationContext().unregisterReceiver(broad);
            broad = null;
            log.i("注销 接受本地消息到服务器 ,广播");
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    //发送心跳定时器
   private static Timer timer = null;
   private static TimerTask timertask = null;
    //开启 心跳
    private void startHeartbeat(){

        stopHeartbeat();
        Log.i(TAG,"- -开始心跳- - ");
        timer = new Timer();
        timertask  =  new TimerTask() {
            @Override
            public void run() {
//                    Log.i(TAG,"- -心跳中- - ");
                    sendMsgToService("HRBT:"+terminalNo);
            }
        };
        //创建定时器任务 发送心跳
        timer.schedule(timertask,HeartBeatTime, HeartBeatTime);//心跳中HeartBeatTime
    }
    //关闭心跳
    private void stopHeartbeat(){
        Log.i(TAG,"- - 结束心跳 - - ");
        if (timertask!=null){
            timertask.cancel();
            timertask = null;
        }
        if (timer != null){
            //关闭定时器
            timer.cancel();
            timer = null;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 连接服务器线程
     */
    private class ConnectServerThread extends Thread{
        private volatile boolean isStart = false;
        public void startMe(){
            isStart =true;
        }
        public void stopMe(){
            isStart =false;
        }
        private boolean isCreateConne = false;
        private int reCreateConnTime = 10 * 1000;
        @Override
        public void run() {
            while(isStart){
                if (isCreateConne){
                    //已经创建链接
                    //打开通讯
                    startCommunication();
                    stopMe();
                    log.i(TAG,"ConnectServerThread 完成任务");
                }else{
                    //创建链接
                    isCreateConne = createConnect();
                    try {
                        Thread.sleep(reCreateConnTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private ConnectServerThread connHelper = null;
    //开启 链接服务器的 助手
    private void openConnServerThread(){
        if (connHelper!=null){
            connHelper.stopMe();
            connHelper = null;
        }
        connHelper = new ConnectServerThread();
        connHelper.startMe();
        connHelper.start();
    }
}
