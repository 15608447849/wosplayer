package com.wosplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wosplayer.app.log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/7/19.
 */

public class CommunicationService extends Service{

    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private Socket socket;

    private String ip;
    private int port;// 6666
    private String terminalNo;
    private long HeartBeatTime;
    private boolean isConnected = false;
    private boolean isStartReceiveThread = false;
    private ReentrantLock msgLock = new ReentrantLock();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        broad = new CommunicationServiceReceiveNotification();
        IntentFilter filter=new IntentFilter();
        filter.addAction(CommunicationServiceReceiveNotification.action);
        getApplicationContext().registerReceiver(broad, filter); //只需要注册一次
        log.i("已注册 接受广播");
    }

    private Thread receiveThread = new Thread(){
        //receive service to me msg

        @Override
        public void run() {
            while(isConnected && isStartReceiveThread){
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
                    CommunicationService.this.stopSelf();
                }

            }
        }
    };

    /**
     * 分发任务
     * @param cmd
     * @param param
     */
    private void postTask(String cmd, String param) {
        log.i("派发任务:"+cmd);
    }

    /**
     * 返回值
     从Android官方文档中，知道onStartCommand有4种返回值：

     START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。

     START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务。

     START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。

     START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ip =  intent.getExtras().getString("ip");
        port = intent.getExtras().getInt("port");
        terminalNo = intent.getExtras().getString("terminalNo");
        HeartBeatTime = intent.getExtras().getLong("HeartBeatTime");

        if (ip==null || terminalNo==null){
           return START_STICKY;
        }

        Schedulers.newThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                init();
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }


    private void init() {
        //创建socket连接
        CreaterSocket();

        //开启 接受消息的线程
        if (!isStartReceiveThread){
            isStartReceiveThread=true;
            receiveThread.start();
        }

        //发送上线通知
        String msg = "ONLI:" + terminalNo;//
        sendMsgToService(msg);

//        msg = "GVAY:" + terminalNo+"#"+ Command_UPDC.getLocalVersionCode();//通知获取版本号信息
//        sendMsgToService(SengCmd);

        //创建定时器任务 发送心跳
        timer.schedule(task,100,HeartBeatTime);

        //注册 广播 ,用于接受 别人发给我的信息  ,并 发送给 外网 (在创建时候就ok了)






    }

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
            ReConnection();
        }


    }

    private int connectionCount = 3;//重连接次数
    private int tryReconnectionCount=0;
    private void ReConnection() {
        tryReconnectionCount++;
        if (tryReconnectionCount == connectionCount){
            stopSelf();//停止自己
            return;
        }

        try {
            Thread.sleep(1 * 500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        CreaterSocket();
    }

    /**
     *
     */
    private void desconnection(){

        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                log.e("Communication disconnect error :" + e.getMessage());
            }
        }
        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();
            } catch (Exception e) {
                log.e("Communication disconnect error :" + e.getMessage());
            }
        }

        if (dataInputStream != null) {
            try {
                dataInputStream.close();
            } catch (IOException e) {
                log.e("Communication disconnect error :" + e.getMessage());
            }
        }

        isConnected = false;
    }


    /**
     *
     */
    private void sendMsgToService(final String msg){

        if (isConnected){

                Schedulers.newThread().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        try {
                       msgLock.lock();

                        dataOutputStream.writeUTF(msg);
                        dataOutputStream.flush();
                        log.i("发送一条信息到服务器 :" + msg);

                        } catch (Exception e) {
                            e.printStackTrace();
                            log.e("发送信息失败 error :" + e.getMessage());
                        }finally {
                            msgLock.unlock();
                        }
                    }
                });



        }
    }


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
        log.i("收到一个广播..."+msg);
        if (msg==null) return;
        sendMsgToService(msg);
    }
}

    //发送心跳定时器
   private Timer timer = new Timer();

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {

            String msg = "HRBT:"+terminalNo;
            sendMsgToService(msg);
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();

        log.i("通讯服务 停止了");
        //注销广播
        if (broad != null) {
            getApplicationContext().unregisterReceiver(broad);
            broad = null;
        }

        if (isStartReceiveThread){
            isStartReceiveThread = false;
        }

        //关闭定时器
        timer.cancel();

        //发送下线通知
        String msg = "OFLI:" + terminalNo;
        sendMsgToService(msg);
        try {
            Thread.sleep(5*100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //关闭socket连接
        desconnection();
    }



}
