package com.wosplayer.download.ftp;

import com.wosplayer.app.Logs;
import com.wosplayer.download.ftpimps.IFtpClien;
import com.wosplayer.download.ftpimps.IFtpManager;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by user on 2017/4/18.
 */

public class MFManage extends Thread implements IFtpManager {

    private final static String TAG = "FTP管理器";

    private  static MFManage manage ;
    public static MFManage getManager(){
        if (manage == null){
            manage = new MFManage();
        }
        return manage;
    }
    private boolean isStart = false;
    private final int checkTime = 60 * 1000;
    private  MFManage(){
        isStart = true;
        start();
    }

    //本地维护一个ftp客户端列表
    private ArrayList<IFtpClien> ftpClienList;

    //添加客户端
    private synchronized void addClt(IFtpClien client){
        if (ftpClienList==null){
            ftpClienList = new ArrayList<>();
        }
        ftpClienList.add(client);
    }





    @Override
    public synchronized IFtpClien getClient(FtpUser ftpUser) {
        IFtpClien client = null;

        if (ftpClienList!=null){
            Iterator<IFtpClien> itr = ftpClienList.iterator();
            while (itr.hasNext()){
                client = itr.next();
                Logs.w(TAG,"查看 : " + client +" 状态:"+ (client.getState()==0?"未使用":"使用中"));
                if ( client.getState()==0 && client.check(ftpUser)){
                    Logs.w(TAG,"队列中获取到可用客户端 : " +client);
                    break;
                }else{
                    client = null;
                }
            }
        }
        if (client == null){
            client = new MFtp(ftpUser);
            addClt(client);
            Logs.w(TAG,"新建客户端 : " +client);
        }

        Logs.w(TAG,"当前队列大小 : " + ftpClienList.size());
        client.setState(1);//使用中

        if (client.connect(null,0))
                client.login(null,null);

        return client;
    }

    @Override
    public synchronized void backClient(IFtpClien client) {
        client.setState(0);
    }

    @Override
    public void run() {
        while (isStart){
            try {
                checkList();
                Thread.sleep(checkTime);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }
    private synchronized void checkList() {
        if (ftpClienList==null) return;
        Iterator<IFtpClien> itr = ftpClienList.iterator();
        IFtpClien ftp;
        while (itr.hasNext()){
            ftp = itr.next();
            if (ftp.getState() == 0){
                itr.remove();
                //未使用或者错误,关闭链接
//                ftp.logout();
                ftp.disconnect();
                Logs.e("FTP管理器","队列移除 : "+ftp);
            }
        }
    }
}
