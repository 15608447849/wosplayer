package com.wosplayer.download.ftp;

import android.util.Log;

import com.wosplayer.download.ftpimps.IFtpClien;
import com.wosplayer.download.ftpimps.IWatched;

import java.io.File;
import java.io.IOException;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

/**
 * Created by user on 2017/4/17.
 */
public class MFtp  implements IFtpClien{
    private static final java.lang.String TAG = "FTP客户端程序";

    private it.sauronsoftware.ftp4j.FTPClient client ;
    private FtpUser info;
    private int state = 0;

    public MFtp(FtpUser ftpUser) {
        setFtpInfo(ftpUser);
        client = new FTPClient();
        client.setPassive(true);//被动模式
        client.setType(FTPClient.TYPE_BINARY);//二进制传输

    }

    @Override
    public void setFtpInfo(FtpUser ftpUser) {
        this.info = ftpUser;
    }

    @Override
    public boolean connect(String host, int port) {

        if (client.isConnected()) return false;
        if (host==null)
            host = info.getHost();
        if (port==0)
            port = info.getPort();

        try {
            client.connect(host,port);
            Log.w(TAG,"连接服务器 "+host+":"+port);
            return true;
        } catch (IOException | FTPIllegalReplyException | FTPException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean login(String user, String pass) {
        if (user==null)
            user = info.getUserName();
        if (pass==null)
            pass = info.getPassword();

        try {
            client.login(user,pass);
            boolean compressionSupported = client.isCompressionSupported();
            if (compressionSupported)
                client.setCompressionEnabled(true); //如果服务器端支持压缩,就可通过下面的调用来启用压缩

            client.setAutoNoopTimeout(20 * 1000);//通常情况下， FTP服务器会自动断开非活跃客户端. 为了避免超时，你可以发送NOOP命令- 向服务器说明：客户端仍然还活着，请重围超时计数器.
            Log.w(TAG,"登陆服务器 ("+user+" # "+pass+")");
            return true;
        } catch (IOException | FTPIllegalReplyException | FTPException e) {
            e.printStackTrace();
        }

        return false;
    }
    public void logout(){
        try {
            client.logout();
            Log.w(TAG,"登出服务器");
        } catch (IOException | FTPIllegalReplyException | FTPException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void disconnect() {
        if (client.isConnected()){
            try {
                client.disconnect(true);
                Log.w(TAG,"断开连接");
            } catch (IOException | FTPIllegalReplyException | FTPException e) {
                e.printStackTrace();
            } finally {
                try {
                    client.disconnect(false);
                } catch (Exception e) {
                }
            }
        }

    }

    @Override
    public boolean changeTargetDirectory(String absulutePath) {
        if (absulutePath==null)
            absulutePath = "/";
        // 当前文件夹
        String dir = null;
        try {
            dir = client.currentDirectory();
            Log.w(TAG,"当前所在目录:"+dir+",目标目录:"+absulutePath);
            if (!dir.equals(absulutePath)){
                client.changeDirectory(absulutePath);//改变目录
            }
            return true;
        } catch (IOException | FTPIllegalReplyException | FTPException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public long getFileSize(String absulutePath) {
        long size = 0;
        try {
            size = client.fileSize(absulutePath);
        } catch (IOException | FTPIllegalReplyException | FTPException e) {
            if (!e.getMessage().contains("File not found")){
                e.printStackTrace();
            }
            size = 0;
        }
        return size;
    }



    @Override
    public void loadFile(File localFile, final String remoteFile, final long atPoint,final long serverFileSize, final IWatched watche) {

        try {
            client.download(remoteFile, localFile, atPoint, new FTPDataTransferListener() {
                private long oldTime = 0;
                private long curTime = 0;
                private long temTime = 0;
                private long curSize = 0;//当前大小
                private long fSize = 0;
                private long temSize = 0;
                private long changeBuff;
                @Override
                public void started() {
                    curTime = System.currentTimeMillis();//当前毫秒数
                    curSize = atPoint;
                    fSize =serverFileSize;
                    temSize = 0;
                    if (watche!=null){
                        watche.onStart();
                    }
                }

                @Override
                public void transferred(int i) {
                    curSize += i;
                    temSize += i;
                    temTime = System.currentTimeMillis();//当前毫秒数
                    if (( temTime - curTime )>= 1000){
                        oldTime = curTime;
                        curTime = temTime;
                        changeBuff = temSize;
                        temSize = 0;
                        if (watche!=null){
                            float chang =  (changeBuff /(( curTime - oldTime) / 1000f )) /1024; //每秒该变量
                            //百分比
                            float progress =  ( curSize/(float)fSize ) *100f;
                            watche.transBuff(progress,chang);
                        }
                    }
                }

                @Override
                public void completed() {
                    if (watche!=null){
                        watche.onSuccess();
                    }
                }

                @Override
                public void aborted() {
                    if (watche!=null){
                        watche.onFailt(new IllegalStateException("Ftp download '"+remoteFile+"' aborted."));
                    }
                }

                @Override
                public void failed() {
                    if (watche!=null){
                        watche.onFailt(new IllegalStateException("Ftp download '"+remoteFile+"' failed."));
                    }
                }
            });


        } catch (Exception e) {
            if (watche!=null){
                watche.onFailt(e);
            }
        }
    }

    @Override
    public void setState(int state) {
        this.state = state;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public boolean check(FtpUser ftpUser) {
        return this.info.equals(ftpUser);
    }

    @Override
    public String toString() {
        return "["+hashCode()+"]";
    }
}
