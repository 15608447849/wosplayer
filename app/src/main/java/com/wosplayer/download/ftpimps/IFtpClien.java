package com.wosplayer.download.ftpimps;

import com.wosplayer.download.ftp.FtpUser;

import java.io.File;

/**
 * Created by user on 2017/4/17.
 */

public interface IFtpClien {
    void setFtpInfo(FtpUser ftpUser);
    boolean connect(String host,int port);//连接
    boolean login(String user,String pass);//登陆
    void logout();//登出
    void disconnect();//断开
    //跳转到执行的目录下
    boolean changeTargetDirectory(String absulutePath);
    //如果文件存在,返回获取指定文件的大小
    long getFileSize(String absulutePath);
    //下载文件 成功或者失败
    void loadFile(File localFile, String remoteFile, long atPoint,long serverFileSize, IWatched watche);

    void setState(int state);
    int getState();
    boolean check(FtpUser ftpUser);
}
