package com.wosplayer.download.ftp;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by lzp on 2017/4/13.
 * 管理ftp客户端列表
 * 1. 每隔 30s秒检测一次列表 - 查看客户端使用情况
 * 2. 如果未使用 关闭客户端 移出列表
 */
public class FtpClientManager extends Thread {


    private static FtpClientManager manager;

    //单例
    public static FtpClientManager getInstant(){
        if (manager==null){
            manager = new FtpClientManager();
        }
        return manager;
    }
    //私有构造
    private FtpClientManager(){
        ftpClienList = new ArrayList<>();
        isStart = true;
        start();
    }

    private boolean isStart = false;
    //本地维护一个ftp客户端列表
    private ArrayList<FtpControl> ftpClienList;

    public void destory(){
        isStart = false;
        //断开所有客户端链接
        Iterator<FtpControl> itr = ftpClienList.iterator();
        FtpControl client;
        while (itr.hasNext()){
            client = itr.next();
            client.setState(0);
            client.disconnectFTP();
            itr.remove();
        }
        manager = null;
    }

    //获取一个客户端
    public FtpControl getFtpClient(FtpUser ftpUser){
        FtpControl client = null;

            Iterator<FtpControl> itr = ftpClienList.iterator();
            while (itr.hasNext()){
                client = itr.next();
                if (client.checkConnect(ftpUser)){
                 break;
                }else{
                    client = null;
                }
            }
            if (client==null){
                client = new FtpControl();
                ftpClienList.add(client);
            }

        return client.setState(2);
    }
    //返回一个不使用的客户端
    public void backFtpClient(FtpControl client){
        Iterator<FtpControl> itr = ftpClienList.iterator();
        while (itr.hasNext()){
            if (client.checkConnect(itr.next().getFinfo())){
                client.setState(1);//待使用
            }
        }
    }

    @Override
    public void run() {

        while (isStart){

            try {
                checkList();
                Thread.sleep(30 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void checkList() {
        Iterator<FtpControl> itr = ftpClienList.iterator();
        FtpControl ftp;
        while (itr.hasNext()){

            ftp = itr.next();
            if (ftp.getState() == 0){
                //未使用关闭链接
                ftp.disconnectFTP();
                itr.remove();
            }else if (ftp.getState() == 1){
                //设置为未使用
                ftp.setState(0);
            }
        }
    }
}
