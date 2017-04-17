package com.wosplayer.download.ftp;


import com.wosplayer.download.operation.Task;
import com.wosplayer.download.util.DownloadFileUtil;

import org.apache.commons.net.ftp.FTPFile;

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

    private static final String tem = ".tem"; //临时文件后缀
          /**
         * 下载单个文件，可实现断点下载.
         */
    public static void downloadSingleFile(Task task,onListener listener){

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
                listener.downLoadSuccess(0,task);
                return;
            }
        }
        FtpControl ftpControl = FtpClientManager.getInstant().getFtpClient(task.getFtpUser());
        if (ftpControl==null){
            listener.ftpConnectState(FTP_CONNECT_FAIL,task,"ftp client create failt.");
            return;
        }

        //远程文件路径
        String remoteFilePath = remotePath+remoteName;
        String tmp_localPath = localFilePath+tem;//临时文件路径
        try {
            ftpControl.connectFTP();
            listener.ftpConnectState(FTP_CONNECT_SUCCESSS,task,null);
        } catch (Exception e) {
            listener.error(e);
            listener.ftpConnectState(FTP_CONNECT_FAIL,task,e.getMessage());
            return;
        }

        // 先判断服务器文件是否存在
        FTPFile[] files = null;
        try {

            files =  ftpControl.listFiles(remoteFilePath); //远程服务器文件

            if (files==null || files.length == 0){
                listener.ftpNotFountFile(task);
                FtpClientManager.getInstant().backFtpClient(ftpControl);//返回ftp客户端
                return;
            }

        } catch (Exception e) {
            listener.error(e);
            FtpClientManager.getInstant().backFtpClient(ftpControl);//返回ftp客户端
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
        File localFile = new File(localFilePath);//本地文件
        File tmp_localFile = new File(tmp_localPath);//临时文件
        if(localFile.exists()){
            //如果有同名文件
            long localfile_Size = localFile.length(); // 获取文件的长度
            if( localfile_Size == serverSize){
                if (!task.isCover()){//不覆盖
                    listener.downLoadSuccess(0,task);
                    FtpClientManager.getInstant().backFtpClient(ftpControl);//返回ftp客户端
                    return;
                }else{
                    //覆盖文件
                    isDelete = true;
                }
            }else{
                //删除一个大小不同的同名文件
                localFile.delete();
            }
        }
        long localSize = 0;
        if (tmp_localFile.exists()) { //如果 临时文件存在
            localSize = tmp_localFile.length(); // 获取 本地临时文件的长度
            if (localSize == serverSize) { //临时文件长度 和 服务器 的大小一样 不下载
                isLoad = false;
            }
            if (localSize<serverSize){
                //判断是否断点续传 - 不断点续传 - 删除临时文件(未处理)
                isLoad = true;
            }
            if (localSize>serverSize){
                tmp_localFile.delete();
                localSize = 0;
            }
        }
        if (isLoad) {
            //如果　没有下载过　准备下载．
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
                listener.downLoadFailt(task,e);
                FtpClientManager.getInstant().backFtpClient(ftpControl);//返回ftp客户端
                return;
            }
            //取出 ftp文件流
            InputStream input  = null;
            try {
                ftpControl.setRestartOffset(localSize);  //设置下载点
                input = ftpControl.retrieveFileStream(remoteFilePath);//远程ftp 文件输入流
            } catch (Exception e) {
                listener.error(e);
                listener.downLoadFailt(task,e);
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
                listener.downLoadFailt(task,e);
            }finally {
                try {
                    if (out!=null){
                        out.close();
                    }
                    if (input!=null){
                        input.close();
                    }
                    // 下载完成返回客户端
                    FtpClientManager.getInstant().backFtpClient(ftpControl);//返回ftp客户端
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }

        }
        if (isDelete){
            //删除同名文件
            localFile.delete();
        }

        DownloadFileUtil.renamefile(tmp_localPath,localFilePath);//转换名字
//                log.i(TAG,",转换前文件名["+tmp_localPath+"]\n新文件名: "+nNmae);
        listener.downLoadSuccess(1,task);
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
        public void ftpConnectState(int stateCode, Task task,String message);//1 success 2failt

        //ftp 不存在文件
        public void ftpNotFountFile(Task task);

       /*
        *   下载进度监听
        */
       public void downLoading(Task task,long downProcess, String speed, String fileName);

        /**
         * 下载成功
         */
        public void downLoadSuccess(int type,Task task);

        /**
         * 下载失败
         */
        public void downLoadFailt(Task task,Exception error);

        /**
         * 错误
         * @param e
         */
        public void error(Exception e);
    }






































}
