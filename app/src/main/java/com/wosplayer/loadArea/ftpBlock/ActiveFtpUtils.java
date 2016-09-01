package com.wosplayer.loadArea.ftpBlock;


import com.wosplayer.app.log;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/6/25.
 */

public class ActiveFtpUtils {

    private static final java.lang.String TAG = ActiveFtpUtils.class.getName();
    /**
     * 服务器名.
     */
    private String hostName;
    /**
     * 端口号
     */
    private int serverPort;
    /**
     * 用户名.
     */
    private String userName;

    /**
     * 密码.
     */
    private String password;
    /**
     * FTP连接.
     */
    private FTPClient ftpClient;

    /**
     * FTP重新链接次数
     */
    private int reConnectCount = 0;

    public ActiveFtpUtils(String hostName, int serverPort, String userName, String password) {
        this.hostName = hostName;
        this.serverPort = serverPort==0?21:serverPort;
        this.userName = userName;
        this.password = password;
        this.ftpClient = new FTPClient();
    }



    /**
     * 打开FTP服务.
     *
     * @throws IOException
     */
    public void openConnect() throws IOException {

        // 中文转码
        ftpClient.setControlEncoding("UTF-8");
        int reply; // 服务器响应值
        // 连接至服务器

        ftpClient.connect(hostName, serverPort);

        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("FTP connect fail code : " + reply );
        }
        // 登录到服务器
        ftpClient.login(userName, password);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        } else {
            // 获取登录信息
            FTPClientConfig config =new FTPClientConfig(ftpClient.getSystemType().split(" ")[0]);
            config.setServerLanguageCode("zh"); //设置配置类型
            ftpClient.configure(config);//配置
            // 使用被动模式设为默认
            ftpClient.enterLocalPassiveMode();
            // 二进制文件支持
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        }
    }
    /**
     * 关闭FTP服务.
     *
     * @throws IOException
     */
    public void closeConnect() throws IOException {
        if (ftpClient != null) {
            // 退出FTP
            ftpClient.logout();
            // 断开连接
            ftpClient.disconnect();
        }
    }













    public static final String FTP_CONNECT_SUCCESSS = "ftp连接成功";
    public static final String FTP_CONNECT_FAIL = "ftp连接失败";
//    public static final String FTP_DISCONNECT_SUCCESS = "ftp断开连接";
    public static final String FTP_DOWN_LOADING = "ftp文件正在下载";
    public static final String FTP_DOWN_SUCCESS = "ftp文件下载成功";
    public static final String FTP_DOWN_FAIL = "ftp文件下载失败";
    public static final String FTP_FILE_NOTEXISTS = "ftp文件不存在";
//    public static final String FTP_LOCAL_FILE_NOTEXISIS = "本地文件不存在或者创建失败";





    /**
         * 下载单个文件，可实现断点下载.
         *
         * @param serverPath
         *            Ftp目录及文件路径
         * @param localPath
         *            本地目录
         * @param fileName
         *            下载之后的文件名称
         * @param listener
         *            监听器
         * @throws IOException
         */
    public synchronized  void downloadSingleFile(String serverPath, String localPath, String fileName, int reconnectCount, DownLoadProgressListener listener){
        log.i(TAG,"准备下载 :"+ localPath+fileName);

        String localFilePath = localPath+fileName ;

        String tem = ".tem"; //临时文件后缀
        String tmp_localPath = localPath + fileName+tem;

        try {
            // 打开FTP服务
            log.i(TAG,"连接服务器 :"+ localPath+fileName);
            this.openConnect();
            listener.onDownLoadProgress(FTP_CONNECT_SUCCESSS, 0,null, null);
        } catch (IOException e1) {
            if (ReOpenConnectionFTP(serverPath, localPath, fileName, reconnectCount, listener)) {
                return;
            }
            return;
        }


        // 先判断服务器文件是否存在
        FTPFile[] files = null;

        try {
            files =  ftpClient.listFiles(serverPath); //远程服务器文件
            if (files.length == 0) {
                log.i(TAG,"服务器文件 不存在 : ["+ serverPath+"]");
                listener.onDownLoadProgress(FTP_FILE_NOTEXISTS, 0,null, null);
                return;
            }else{
                log.i(TAG,"服务器文件 存在 :"+ serverPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }



        //创建本地文件夹
        File mkFile = new File(localPath);
        if (!mkFile.exists()) {
            mkFile.mkdirs();
        }


        boolean isLoad = true;
        // 接着判断下载的文件是否能断点下载
        long serverSize = files[0].getSize(); // 获取远程文件的长度
        log.i(TAG,"# 服务器文件长度 :"+ serverSize);
        log.d("# :"+localFilePath + " #");
        log.d("# :"+tmp_localPath +" #");
        File localFile = new File(localFilePath);//本地文件
        File tmp_localFile = new File(tmp_localPath);//临时文件

        if(localFile.exists()){
            //如果有同名文件
           long localfile_Size = localFile.length(); // 获取文件的长度
            log.i(TAG,"本地 已存在文件 长度 :"+ localfile_Size);
           if( localfile_Size == serverSize){

               log.i(TAG,"文件已存在"+localFile.getName());
               listener.onDownLoadProgress(FTP_DOWN_SUCCESS, 0,null,localFile);
               // 下载完成之后关闭连接
               return;
           }else{
               log.i(TAG,"删除一个同名文件:"+localFile.getName());
               localFile.delete();
           }
        }else{
            log.d("本地无同名文件");
        }


        long localSize = 0;
        if (tmp_localFile.exists()) { //如果 临时文件存在
            localSize = tmp_localFile.length(); // 获取 本地临时文件的长度
            log.i(TAG,"临时文件长度:"+localSize);
            if (localSize == serverSize) { //临时文件长度 和 服务器 的大小一样 不下载
                isLoad = false;
                log.i(TAG,"不下载"+tmp_localFile.getName());
            }if (localSize<serverSize){
                log.i(TAG,"临时文件:"+tmp_localPath+",  大小:"+localSize+",  服务器存在大小:"+serverSize);
            }else if (localSize>serverSize){
                log.i(TAG,"删除一个临时文件:"+tmp_localPath+",  大小:"+localSize+",  服务器存在大小:"+serverSize);
                File file = new File(tmp_localPath);
                file.delete();
                localSize = 0;
            }
        }else{
            log.d("# tmp_localFile  临时文件不存在 - "+ tmp_localFile);
        }



        if (isLoad) {//如果　没有下载过　准备下载．
            log.i(TAG,"准备下载 ...  本地未下载文件 :"+ localFilePath);
                    //下载前　设置下载中所需值
                    long step = serverSize / 100; //下标位置
                    long process = 0; // 进度
                    long currentSize = 0;//已下载大小
                    String speed =null;

            // 输出到本地文件流
            OutputStream out = null;
            try {
                log.d("# && :"+tmp_localPath +",tmp_localFile:"+tmp_localFile);
                out = new FileOutputStream(tmp_localFile, true); //本地文件输出流

            } catch (FileNotFoundException e) {
                log.e("ftp err : "+e.getMessage());
                listener.onDownLoadProgress(FTP_DOWN_FAIL, 0,null, null);
                return;
            }

            //取出 ftp文件流
            InputStream input  = null;
            ftpClient.setRestartOffset(localSize);  //设置下载点
            try {
                input = ftpClient.retrieveFileStream(serverPath);//远程ftp 文件输入流
            } catch (IOException e) {
                log.e(TAG,"inputstream err :"+e.getMessage());
                if (ReOpenConnectionFTP(serverPath, localPath, fileName, reconnectCount, listener)) {
                    log.d("尝试重试");
                    return;
                }
                return;
            }

            try {

                byte[] b = new byte[1024];
                int length = 0;
                while ((length = input.read(b)) != -1) {
                    out.write(b, 0, length);
                    out.flush();

                    currentSize = currentSize + length; // 当前大小 = 现在的大小加上写出来的大小
                    if (currentSize / step != process) { //如果 当前进度/下标 != 已有进度
                        process = currentSize / step;
                        if (process % 2 == 0) {  //每隔%2的进度返回一次
                            speed = String.format("%.1f", currentSize / (1024 * 1.0)) + "kb";
                            listener.onDownLoadProgress(FTP_DOWN_LOADING, process, speed, null);
                        }
                    }
                }
                out.close();
                input.close();
            } catch (IOException e) {
                listener.onDownLoadProgress(FTP_CONNECT_FAIL, 0,null, null);
            }

        }


                String nNmae = tmp_localPath.substring(0,tmp_localPath.lastIndexOf("."));//正式文件名
        fileUtils.renamefile(tmp_localPath,nNmae);//转换名字
                File Nf = new File(nNmae);
        log.i(TAG,"新文件名:"+nNmae+",转换前"+tmp_localPath);

                listener.onDownLoadProgress(FTP_DOWN_SUCCESS, 0,null,Nf);
                // 下载完成之后关闭连接
        try {
            this.closeConnect();
        } catch (IOException e) {
            e.printStackTrace();
            log.e(TAG,"关闭ftp失败" + e.getMessage());
        }
    }




    /**
     * 重新链接
     * @param serverPath
     * @param localPath
     * @param fileName
     * @param reconnectCount
     * @param listener
     * @return
     */
    private boolean ReOpenConnectionFTP(String serverPath, String localPath, String fileName, int reconnectCount, DownLoadProgressListener listener) {
        if (reConnectCount++<reconnectCount){
            try {
                Thread.sleep(5*1000);
            }catch (InterruptedException e){
                listener.onDownLoadProgress(FTP_CONNECT_FAIL, 0,null, null);
                return true;
            }
            log.e(TAG,"连接不上服务器,重新链接中...当前第"+reConnectCount+"次 尝试 ,服务器:"+this.hostName+"端口:"+this.serverPort+"用户名:"+this.userName+"密码:"+this.password);
            downloadSingleFile(serverPath,localPath,fileName,reconnectCount,listener);
        }else{
            listener.onDownLoadProgress(FTP_CONNECT_FAIL, 0,null, null);
        }
        return false;
    }


    public interface DownLoadProgressListener{
       /*
        *   下载进度监听
        */
       public void onDownLoadProgress(String currentStep, long downProcess, String speed, File file);
   }






































}
