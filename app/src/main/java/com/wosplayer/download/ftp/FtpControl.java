package com.wosplayer.download.ftp;

import android.util.Log;

import com.wosplayer.app.Logs;

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
        Log.e(TAG,this+" 配置 FTP信息 完成");
    }

    //当前是否连接中
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
        if (!isConnected){
            // 中文转码
            setControlEncoding("UTF-8");
            setDataTimeout(30 * 1000);       //设置传输超时时间
            setConnectTimeout(15 * 1000);       //连接超时

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
            isConnected=true;
            Logs.e(TAG,this +" 登陆 ");
        }
        return isConnected;
    }

    //断开当前链接
    public void disconnectFTP() {
        if (isConnected){
            try {
                // 退出FTP
                logout();
            } catch (IOException e) {
                Log.e(TAG,"["+this+"]登出FTP错误:"+e.getMessage());
            } finally {
                try {
                    disconnect();
                } catch(IOException ioe) {
                }
                isConnected = false;
                Logs.e(TAG,this +" 登出 ");
            }
        }
    }

    //检测当前链接的ftp
    public boolean checkConnect(FtpUser ftp){
        if (finfo!=null)
           return this.finfo.equals(ftp);
        return false;
    }


    @Override
    public String toString() {
//        return super.toString();
        return "FTP@"+this.hashCode();
    }
}
