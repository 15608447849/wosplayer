package com.wosplayer.download.ftp;


import com.wosplayer.app.Logs;
import com.wosplayer.download.ftpimps.IFtpClien;
import com.wosplayer.download.ftpimps.IFtpManager;
import com.wosplayer.download.ftpimps.IWatched;
import com.wosplayer.download.operation.Task;
import com.wosplayer.download.util.DownloadFileUtil;

import java.io.File;

/**
 * Created by Administrator on 2016/6/25.
 */

public class FtpHelper {
    private static final String TAG = "FTP下载助理"; //临时文件后缀
    private static final String tem = ".tem"; //临时文件后缀

          /**
         * 下载单个文件，可实现断点下载.
         */
    public static void downloadSingleFile(final Task task, final onListener listener){
        String remotePath =task.getRemotePath();
        final String remoteName = task.getRemoteName();
        String localPath = task.getLocalPath();
        String localName = task.getLocalName();

        final String localFilePath = localPath+localName ;//本地文件路径
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
        final IFtpManager ftpManager = MFManage.getManager();
        final IFtpClien ftpControl = ftpManager.getClient(task.getFtpUser());//获取ftp客户端
        if (ftpControl==null){
            listener.ftpConnectState(FTP_CONNECT_FAIL,task,"ftp client create failt.");
            return;
        }
        listener.ftpConnectState(FTP_CONNECT_SUCCESSS,task,"ftp client connected success");
        Logs.w(TAG,"当前使用的FTP客户端: " + ftpControl);
        //远程文件路径
        String remoteFilePath = remotePath+remoteName;
        final String tmp_localPath = localFilePath+tem;//临时文件路径
        // 先判断服务器文件是否存在
        long serverSize = ftpControl.getFileSize(remotePath,remoteName); // 获取远程文件的长度
        if (serverSize==0){
            //返回ftp客户端
            ftpManager.backClient(ftpControl);
            listener.ftpNotFountFile(task);
        }

       //创建本地文件夹
        File mkFile = new File(localPath);
        if (!mkFile.exists()) {
            mkFile.mkdirs();
        }

        boolean isDelete = false;
        boolean isLoad = true;

        //接着判断下载的文件是否能断点下载
        final File localFile = new File(localFilePath);//本地文件
        File tmp_localFile = new File(tmp_localPath);//临时文件
        if(localFile.exists()){
            //如果有同名文件
            long localfile_Size = localFile.length(); //获取本地文件的长度
            if( localfile_Size == serverSize){
                if (!task.isCover()){//不覆盖
                    //返回ftp客户端
                    ftpManager.backClient(ftpControl);
                    listener.downLoadSuccess(0,task);
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

        long localSize = 0;//临时文件大小
        if (tmp_localFile.exists()) { //如果 临时文件存在
            localSize = tmp_localFile.length(); // 获取 本地临时文件的长度
            if (localSize == serverSize) { //临时文件长度 和 服务器 的大小一样 >> 不下载
                isLoad = false;
            }
            if (localSize<serverSize){
                //判断是否断点续传 - 不断点续传 - 删除临时文件(未处理)
                isLoad = true;
            }
            if (localSize>serverSize){
                tmp_localFile.delete();//删除临时文件
                localSize = 0;
            }
        }
        if (isLoad) {
            final boolean flag = isDelete;
            ftpControl.loadFile(tmp_localFile, remoteName, localSize, serverSize,new IWatched() {

                @Override
                public void onStart() {
                    //开始下载
                }

                @Override
                public void onSuccess() {
                    if (flag){
                        //删除同名文件
                        localFile.delete();
                    }
                    DownloadFileUtil.renamefile(tmp_localPath,localFilePath);//转换名字
                    ftpManager.backClient(ftpControl);
                    listener.downLoadSuccess(1,task);
                }

                @Override
                public void onFailt(Exception e) {
                    ftpManager.backClient(ftpControl);
                    listener.downLoadFailt(task,e);
                }

                @Override
                public void transBuff(float progress, float speed) {
                    listener.downLoading(task, (long) (progress), String.format("%.2f",speed) + "kb/s", remoteName);
                }


            });

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
