package com.wosplayer.download.ftp;

import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

/**
 * Created lzp user on 2017/4/13.
 *
 */
public class FtpControl extends FTPClient {


    private static final java.lang.String TAG = "FTP客户端程序";

    private FtpUser finfo = null;

    public void setFtpConfig(FtpUser finfo) {
        if (isConnected) disconnectFTP();
        this.finfo = finfo;
    }

    //当前是否连接
    private boolean isConnected = false;
    //当前状态 2使用中 1待使用 0未使用
    private int state = 0;

    public int getState() {
        return state;
    }

    public FtpControl setState(int state) {
        this.state = state;
        return this;
    }

    public FtpUser getFinfo() {
        return finfo;
    }

    //打开连接
    public boolean connectFTP() throws Exception {
        if (finfo==null) throw  new IllegalStateException("ftp config is error .");
        if (isConnected) throw  new IllegalStateException("["+this+"] is connecting.");
        // 中文转码
        setControlEncoding("UTF-8");
        setDataTimeout(60000);       //设置传输超时时间为60秒
        setConnectTimeout(60000);       //连接超时为60秒

        // 连接至服务器
        connect(finfo.getHost(), finfo.getPort());
        int reply = getReplyCode();// 服务器响应值
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            disconnectFTP();
            throw new IOException("FTP connect is failt, reply = " + reply );
        }
        // 登录到服务器
        login(finfo.getUserName(), finfo.getPassword());
        reply = getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            disconnectFTP();
            throw new IOException("FTP login failt, reply = " + reply);
        }
        // 获取登录信息
        FTPClientConfig config =new FTPClientConfig(getSystemType().split("\\s+")[0]);
        config.setServerLanguageCode("zh"); //设置配置类型
        configure(config);//配置
        // 使用被动模式设为默认
        enterLocalPassiveMode();
        // 二进制文件支持
        setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        return (isConnected=true);
    }

    //断开当前链接
    public void disconnectFTP() {
        try {
            // 退出FTP
            logout();
        } catch (IOException e) {
            Log.e(TAG,"["+this+"]登出FTP失败,"+e.getMessage());
        } finally {
            try {
                disconnect();
            } catch(IOException ioe) {
            }
            isConnected = false;
        }
    }

    //检测当前链接的ftp
    public boolean checkConnect(FtpUser ftp){
        if (isConnected){
           return this.finfo.equals(ftp);
        }
        return false;
    }



















}
