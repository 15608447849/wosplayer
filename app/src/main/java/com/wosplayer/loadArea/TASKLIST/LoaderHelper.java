package com.wosplayer.loadArea.TASKLIST;

import android.util.Log;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.ftpBlock.FtpHelper;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URI;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import installUtils.MD5Util;

/**
 * Created by user on 2016/11/25.
 */

public class LoaderHelper implements Observer {//观察者

    private static final java.lang.String TAG = "LoaderHelper";
    private DownloadCallImp caller = null;
    //private ExecutorService singleThreadExecutor;
    private HttpUtils http = null;
    ExecutorService fixedThreadPool;
    public LoaderHelper() {

    }
    public void initWord(){
//        singleThreadExecutor = Executors.newSingleThreadExecutor();
        fixedThreadPool = Executors.newFixedThreadPool(10);
        http = new HttpUtils();
        caller = new DownloadCallImp();
    }


    /**
     * 执行下载任务
     */
    private void excuteDownLoad(final Task task) {
        fixedThreadPool.execute(new Runnable() {
            public void run() {
                try {
                    parseDatas(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 解析数据
     * @param task
     */
    private void parseDatas(Task task){

        int type = task.getType();

        if (type == Task.Type.HTTP){
            httpDownload(task);
        }

        if (type == Task.Type.FTP){
            ftpDownloadSetting(task);
        }

        if (type== Task.Type.FILE){
            cpFile(task);
        }
    }
    //复制本地文件到 -app 资源目录下
    private void cpFile(Task task) {
        File jhFile = null;
        try {
            jhFile = new File( new URI(task.getUrl()));
            if (jhFile.exists()){
                Log.i(TAG,"复制建行资源文件到app资源目录 - \n"+jhFile.getAbsolutePath()+" -> "+task.getSavePath()+task.getFileName());
                FileUtils.copyFile(jhFile, new File(task.getSavePath()+task.getFileName()));
                caller.downloadResult(task,0);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
         Log.e(TAG,"建行资源文件不存在 或者 复制失败 - "+jhFile);
         caller.downloadResult(task,1);
    }

    //设置ftp值
    private void ftpDownloadSetting(final Task task) {
        String uri = task.getUrl();
        String str = uri.substring(uri.indexOf("//") + 2);
        String user = str.substring(0, str.indexOf(":"));
        String pass = str.substring(str.indexOf(":") + 1, str.indexOf("@"));
        String host = str.substring(str.indexOf("@") + 1, str.indexOf("/"));
        String remotePath = str.substring(str.indexOf("/"), str.lastIndexOf("/") + 1);
        String remoteFileName = str.substring(str.lastIndexOf("/") + 1);
        String localPath = task.getSavePath();
        ftpDownloadImp(task,user,pass,host,remotePath,remoteFileName,localPath);
    }

    //Ftp 下载助手
    private void ftpDownloadImp(final Task task, final String user, final String pass, final String host, final String remotePath, final String fileName, final String localPath) {
        new FtpHelper(host,21,user,pass)
                .downloadSingleFile(
                        remotePath,
                        localPath,
                        fileName,
                        3,
                        new FtpHelper.OnFtpListener() {

                            @Override
                            public void ftpConnectState(int stateCode, String ftpHost, int port, String userName, String ftpPassword, String fileName) {
                                log.i(TAG,"连接服务器 : ip:"+ ftpHost+" port:"+port  +"\nuser:"+userName+" password:"+ftpPassword);
                                if (stateCode==FtpHelper.FTP_CONNECT_SUCCESSS){
                                    log.i(TAG,"ftp 连接成功");
                                    caller.nitifyMsg(task.getTerminalNo(),fileName,1);
                                    caller.nitifyMsg(task.getTerminalNo(),fileName,2);
                                }
                                if (stateCode==FtpHelper.FTP_CONNECT_FAIL){
                                    log.e(TAG,"ftp 连接失败");
                                    caller.nitifyMsg(task.getTerminalNo(),fileName,4);
                                    caller.downloadResult(task,1,fileName,".md5");
                                }
                            }

                            @Override
                            public void ftpNotFountFile(String remoteFileName, String fileName) {
                                log.e(TAG,"ftp 服务器未发现文件 : "+remoteFileName+fileName);
                                caller.downloadResult(task,1);
                                caller.nitifyMsg(task.getTerminalNo(),fileName,4);
                            }

                            @Override
                            public void localNotFountFile(String localFilePath, String fileName) {
                                log.e(TAG,"本地文件不存在或无法创建 : "+localFilePath);
                                caller.nitifyMsg(task.getTerminalNo(),fileName,4);
                                caller.downloadResult(task,1,fileName,".md5");

                            }

                            @Override
                            public void downLoading(long downProcess, String speed, String fileName) {
                                caller.notifyProgress(task.getTerminalNo(),fileName,downProcess+"%",speed);
                            }



                            @Override
                            public void downLoadFailt(String remotePath, String fileName) {
                                log.e(TAG,"ftp 下载失败 : "+fileName);
                                caller.nitifyMsg(task.getTerminalNo(),fileName,4);
                                caller.downloadResult(task,1,fileName,".md5");
                            }

                            @Override
                            public void error(Exception e) {
                                e.printStackTrace();
                            }
                            @Override
                            public void downLoadSuccess(File localFile, String remotePath, String localPath, String fileName, String ftpHost, int port, String userName, String ftpPassword) {
                                log.i(TAG, "ftp下载succsee -"+ localFile.getAbsolutePath() +" - 线程 - "+ Thread.currentThread().getName());
                                if (caller.downloadResult(task,0,fileName,".md5")){

                                        //获取源文件 code
                                        String sPath = localPath+fileName.substring(0,fileName.lastIndexOf("."));
                                        log.e(TAG,"文件 - "+sPath +" 比较 MD5值");
                                        String sCode = MD5Util.getFileMD5String(new File(sPath));
                                        if (sCode!=null){
                                            //比较md5
                                            if(MD5Util.FTPMD5(sCode, localFile.getAbsolutePath()) == 1){
                                                //md5 效验失败 删除文件
                                                cn.trinea.android.common.util.FileUtils.deleteFile(sPath);
                                                log.e(TAG,"文件 - "+sPath +" 比较 MD5值 失败 已删除");
                                            }
                                        }
                                }else{
                                    ftpDownloadImp(task,user,pass,host,remotePath,fileName+".md5",localPath);
                                }

                            }
                        }
                );
    }

    // http xiazai
    private void httpDownload(final Task task) {
       final String url = task.getUrl();
        final String filePath = task.getSavePath()+task.getFileName();
      http.download(url,
              filePath,
              false,
              false,
              new RequestCallBack<File>() {
                  long currentTime = 0;
                  long oldTime = 0;
                  long oldLoadingSize = 0;
                  String speed =null;
                  @Override
                  public void onStart() {
                      log.i(TAG,"启动http下载:"+ url+" on Thread : "+Thread.currentThread().getName());
                      caller.nitifyMsg(task.getTerminalNo(),url.substring(url.lastIndexOf("/")+1),1);
                      caller.nitifyMsg(task.getTerminalNo(),url.substring(url.lastIndexOf("/")+1),2);
                      currentTime = System.currentTimeMillis();
                  }
                  @Override
                  public void onLoading(long total, long current, boolean isUploading) {
                      oldTime = currentTime;
                      currentTime = System.currentTimeMillis();
                      long temSize = current-oldLoadingSize;
                      oldLoadingSize = current;
                      double speedTem = (temSize/(1024 * 1.0))/((currentTime-oldTime)/(1000*1.0)) ;
                      speed = String.format("%f",speedTem)+"kb/s";
                      caller.notifyProgress(task.getTerminalNo(),url.substring(url.lastIndexOf("/")+1),(current/total)+"",(speedTem/(1024 * 1.0))+" kb");
                  }
                  @Override
                  public void onSuccess(ResponseInfo<File> responseInfo) {
                      final String path  =responseInfo.result.getPath();
                      caller.downloadResult(task,0);
                      caller.nitifyMsg(task.getTerminalNo(),url.substring(url.lastIndexOf("/")+1),3);
                  }
                  @Override
                  public void onFailure(HttpException e, String s) {
                      caller.downloadResult(task,1);
                      caller.nitifyMsg(task.getTerminalNo(),url.substring(url.lastIndexOf("/")+1),4);
                  }
              });
    }


    /**
     * 被观察者
     * 数据
     */
    @Override
    public void update(Observable observable, Object data) {
//        log.i(TAG,"update - "+data);
        //获取到一个任务 -> 执行一个线程
        if (data!=null){
            excuteDownLoad((Task) data);
        }
    }

    /**
     * 判断文件是不是存在
     */
    public boolean fileIsExist(String filename){
        return   fileUtils.checkFileExists(filename);
    }








}
