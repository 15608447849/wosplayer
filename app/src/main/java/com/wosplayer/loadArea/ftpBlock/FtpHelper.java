package com.wosplayer.loadArea.ftpBlock;


import com.wosplayer.app.Logs;
import com.wosplayer.loadArea.TASKLIST.Task;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import org.apache.commons.io.FileUtils;
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

public class FtpHelper {

    private static final java.lang.String TAG = "_ftp";
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
    private static final String tem = ".tem"; //临时文件后缀
    /**
     * FTP重新链接次数
     */
    private int reConnectCount = 0;
    public FtpHelper(Task.FtpUser ftpUser) {
        init(ftpUser.getHost(),ftpUser.getPort(),ftpUser.getUserName(),ftpUser.getPassword());
    }
    private void init(String hostName, int serverPort, String userName, String password){
        this.reConnectCount = 0;
        this.hostName = hostName;
        this.serverPort = serverPort==0?21:serverPort;
        this.userName = userName;
        this.password = password;
    }
    /**
     * 打开FTP服务.
     *
     * @throws IOException
     */
    public void openConnect() throws IOException {
//        Logs.i(TAG,"连接ftp...");
        if (ftpClient==null){
            ftpClient = new FTPClient();
        }
        // 中文转码
        ftpClient.setControlEncoding("UTF-8");
        ftpClient.setDataTimeout(60000);       //设置传输超时时间为60秒
        ftpClient.setConnectTimeout(60000);       //连接超时为60秒

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
    public void closeConnect(){
        try {
            if (ftpClient != null) {
                // 退出FTP
                ftpClient.logout();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ftpClient != null) {
//                    Logs.i(TAG,"关闭ftp连接");
                    ftpClient.disconnect();
                    ftpClient=null;
                }
            } catch(IOException ioe) {
                // do nothing
            }
        }
    }








    /**
         * 下载单个文件，可实现断点下载.

         * @throws IOException
         */
    public synchronized  void downloadSingleFile(Task task, int reconnectCount, onListener listener){

        String remotePath =task.getRemotePath();
        String remoteName = task.getRemoteName();
        String localPath = task.getLocalPath();
        String localName = task.getLocalName();

        String localFilePath = localPath+localName ;//本地文件路径
        //判断是否覆盖
        if (!task.isCover()){
            //不覆盖
            //判断本地文件是否存在
            boolean isExist = cn.trinea.android.common.util.FileUtils.isFileExist(localFilePath);
            if (isExist){
                //存在 直接返回
                listener.downLoadSuccess(task);
                return;
            }
        }

        //远程文件路径
        String remoteFilePath = remotePath+remoteName;


        String tmp_localPath = localFilePath+tem;//临时文件路径
        try {
            // 打开FTP服务
//            log.i(TAG,"连接服务器 : FTP://"+ hostName+":"+serverPort  +"\n user : "+userName+"  password : "+password);
            this.openConnect();
            listener.ftpConnectState(FTP_CONNECT_SUCCESSS,task);
        } catch (Exception e) {
            listener.error(e);
            reConnectionFTP(task, reconnectCount, listener);
            return;
        }

        // 先判断服务器文件是否存在
        FTPFile[] files = null;
        try {
            files =  ftpClient.listFiles(remoteFilePath); //远程服务器文件
            if (files==null || files.length == 0){
//                log.i(TAG,"服务器文件 不存在 : ["+ serverPath+"]");
                listener.ftpNotFountFile(task);
                closeConnect();
                return;
            }
        } catch (Exception e) {
            listener.error(e);
            reConnectionFTP(task,reconnectCount, listener);
            return;
        }

        //创建本地文件夹
        File mkFile = new File(localPath);
        if (!mkFile.exists()) {
            mkFile.mkdirs();
        }
        boolean isDelete = false;
        boolean isLoad = true;
        // 接着判断下载的文件是否能断点下载
        long serverSize = files[0].getSize(); // 获取远程文件的长度
//        log.i(TAG,"服务器文件长度 : "+ serverSize);
        File localFile = new File(localFilePath);//本地文件
        File tmp_localFile = new File(tmp_localPath);//临时文件
//        log.i("本地路径 :"+localFilePath);
//        log.i("临时文件路径:"+tmp_localPath);
        if(localFile!=null && localFile.exists()){
            //如果有同名文件
            long localfile_Size = localFile.length(); // 获取文件的长度
//            log.i(TAG,"本地 已存在文件 长度 :"+ localfile_Size);
            if( localfile_Size == serverSize){
//                log.i(TAG,"文件已存在"+localFile.getName());
                if (!task.isCover()){//不覆盖
                    listener.downLoadSuccess(task);
                    // 下载完成之后关闭连接
                    closeConnect();
                    return;
                }else{
                    //覆盖文件
                    isDelete = true;
                }
            }else{
//                log.i(TAG,"删除一个同名文件:"+localFile.getAbsolutePath());
//                localFile.delete();

            }
        }

        long localSize = 0;
        if (tmp_localFile.exists()) { //如果 临时文件存在
            localSize = tmp_localFile.length(); // 获取 本地临时文件的长度
//            log.i(TAG,"临时文件长度: "+localSize);
            if (localSize == serverSize) { //临时文件长度 和 服务器 的大小一样 不下载
                isLoad = false;
//                Logs.i(TAG,"不下载 - "+tmp_localFile.getName()+" 和服务器文件大小相同");
            }if (localSize<serverSize){ //断点续传
//                log.i(TAG,"临时文件:"+tmp_localPath+" - 大小:"+localSize+" \n 服务器存在大小:"+serverSize);
            }else if (localSize>serverSize){
//                log.i(TAG,"删除一个临时文件:"+tmp_localPath+" - 大小:"+localSize+"\n 服务器存在大小:"+serverSize);
                tmp_localFile.delete();
                localSize = 0;
            }
        }

        if (isLoad) {
            //如果　没有下载过　准备下载．

//            Logs.i(TAG,"准备下载文件 - "+remoteFilePath +" -> "+ localFilePath );
            //下载前　设置下载中所需值
            long step = serverSize / 100; //下标位置
            long process = 0; // 进度
            long currentSize = 0;//当前总大小
            long oldSize = 0;
            long currentTime = 0;
            long oldTime = 0;
            String speed =null;
            // 输出到本地文件流
            OutputStream out = null;
            try {
                out = new FileOutputStream(tmp_localFile, true); //本地文件输出流 - 临时文件
            } catch (FileNotFoundException e) {
                listener.error(e);
                closeConnect();
                return;
            }

            //取出 ftp文件流
            InputStream input  = null;
            try {
                ftpClient.setRestartOffset(localSize);  //设置下载点
                input = ftpClient.retrieveFileStream(remoteFilePath);//远程ftp 文件输入流
            } catch (Exception e) {
                listener.error(e);
                reConnectionFTP(task, reconnectCount, listener);
                return;
            }

            //下载中
            try {
                byte[] b = new byte[1024*10];//缓存大小
                int length = 0;
                currentTime = System.currentTimeMillis();//当前毫秒数
                double timeDiff = 0;
                double sizeDiff = 0;
                double speedTem = 0;
                while ((length = input.read(b)) != -1) {
                    out.write(b, 0, length);
                    out.flush();
                    currentSize += length; // 当前总大小 = 现在的大小 加上 写出来的大小
                    if (step<=0){
                        continue;
                    }
                    if (currentSize / step != process) { //如果 当前进度/下标 != 已有进度
                        process = currentSize / step;
                        if (process % 10 == 0) { //每隔%10的进度返回一次
                            oldTime = currentTime;//旧时间
                            currentTime = System.currentTimeMillis();//当前时间
                            timeDiff = (currentTime-oldTime) / (1000 * 1.0) ;
//                            log.d("时间差:"+ timeDiff +"秒");
                            sizeDiff = (currentSize-oldSize)/(1024 * 1.0);
//                            log.d(fileName +" - 当前下载量:" + sizeDiff + " kb");
                            speedTem = sizeDiff/timeDiff ;
                            oldSize = currentSize;
//                            log.d(fileName +" - 下载速度:" + speedTem +" kb/s");
                            speed = String.format("%f",speedTem) + "kb/s";
                            listener.downLoading(task,process, speed, remoteName);
                        }
                    }
                }


            } catch (IOException e) {
                listener.error(e);
                listener.downLoadFailt(task);
            }finally {
                try {
                    if (out!=null){
                        out.close();
                    }
                    if (input!=null){
                        input.close();
                    }
                    // 下载完成之后关闭连接
                    this.closeConnect();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }

        }
        if (isDelete){
            //删除同名文件
            if (localFile!=null){
                localFile.delete();
            }
        }

        fileUtils.renamefile(tmp_localPath,localFilePath);//转换名字
//                log.i(TAG,",转换前文件名["+tmp_localPath+"]\n新文件名: "+nNmae);
        listener.downLoadSuccess(task);
    }




    /**
     * 重新链接
     * @return
     */
    private void reConnectionFTP(Task task, int reconnectCount, onListener listener) {
        closeConnect();
        if (reConnectCount++<reconnectCount){
            try {
                Thread.sleep(10*1000);
            }catch (InterruptedException e){
                e.printStackTrace();
                listener.ftpConnectState(FTP_CONNECT_FAIL,task);
            }
            Logs.e(TAG,"重新连接FTP,当前第"+reConnectCount+"次尝试.");
            Logs.e(TAG,task.getFtpUser().toString());

            downloadSingleFile(task,reconnectCount,listener);
        }else{
            listener.ftpConnectState(FTP_CONNECT_FAIL,task);
        }
    }



    public static final int FTP_CONNECT_SUCCESSS = 1;//"ftp连接成功";
    public static final int FTP_CONNECT_FAIL = 2;//"ftp连接失败";
    public static final int FTP_DISCONNECT_SUCCESS = 3;//"ftp断开连接";
    public static final int FTP_DOWN_LOADING = 4;//"ftp文件正在下载";
    public static final int FTP_DOWN_SUCCESS = 5;//"ftp文件下载成功";
    public static final int FTP_DOWN_FAIL = 6;//"ftp文件下载失败";
    public static final int FTP_FILE_NOTEXISTS = 7;//"ftp文件不存在";
    public static final int FTP_LOCAL_FILE_NOTEXISIS = 8;//"本地文件不存在或者创建失败";

    public interface onListener{
        /**
         * 连接成功
         */
        public void ftpConnectState(int stateCode, Task task);//1 success 2failt

        //ftp 不存在文件
        public void ftpNotFountFile(Task task);

       /*
        *   下载进度监听
        */
       public void downLoading(Task task,long downProcess, String speed, String fileName);

        /**
         * 下载成功
         */
        public void downLoadSuccess(Task task);

        /**
         * 下载失败
         */
        public void downLoadFailt(Task task);

        /**
         * 错误
         * @param e
         */
        public void error(Exception e);
    }






































}
