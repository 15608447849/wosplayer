package com.wosplayer.loadArea.TASKLIST;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.wosplayer.app.log;
import com.wosplayer.loadArea.ftpBlock.ActiveFtpUtils;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import java.io.File;
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
    private ExecutorService singleThreadExecutor;
    private HttpUtils http = null;
    public LoaderHelper() {

    }
    public void initWord(){
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        http = new HttpUtils();
        caller = new DownloadCallImp();
    }


    /**
     * 执行下载任务
     */
    private void excuteDownLoad(final Task task) {
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                try {
                    log.i(TAG,"###########################" + task);
                    parseDatas(task);
                    log.i(TAG,"$$$$$$$$$$$$$$$$$$$$$$$$$$$" + task);
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

        if (type == Task.Type.FILE){
        }
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
    private void ftpDownloadImp(final Task task, final String user, final String pass, final String host, final String remotePath, final String remoteFileName, final String localPath) {
        ActiveFtpUtils
                .getInstants(host,21,user,pass)
                .downloadSingleFile(
                        remotePath + remoteFileName,
                        localPath,
                        remoteFileName,
                        3,
                        new ActiveFtpUtils.DownLoadProgressListener() {
                            @Override
                            public void onDownLoadProgress(String currentStep, long downProcess, String speed, File file) {
                                if (currentStep.equals(ActiveFtpUtils.FTP_CONNECT_SUCCESSS)){
                                    log.d(TAG,"ftp 连接成功 - ");
                                    caller.nitifyMsg(remoteFileName,1);
                                    caller.nitifyMsg(remoteFileName,2);
                                }
                                //下载中
                                if (currentStep.equals(ActiveFtpUtils.FTP_DOWN_LOADING)){
                                    caller.notifyProgress(remoteFileName,downProcess+"",speed);
                                }

                                //ftp远程文件不存在
                                if (currentStep.equals(ActiveFtpUtils.FTP_FILE_NOTEXISTS)){
                                    log.e(TAG,"ftp服务器 不存在文件 - " + remotePath+remoteFileName);

                                    if (!remoteFileName.endsWith(".md5")){
                                        caller.downloadResult(task,1);
                                    }
                                    caller.nitifyMsg(remoteFileName,4);
                                }
                                //连接失败
                                if(currentStep.equals(ActiveFtpUtils.FTP_CONNECT_FAIL)){
                                    log.e(TAG,"ftp 连接失败 - ");
                                    if (!remoteFileName.endsWith(".md5")){
                                        caller.downloadResult(task,1);
                                    }
                                    caller.nitifyMsg(remoteFileName,4);
                                }
                                //下载失败
                                if (currentStep.equals(ActiveFtpUtils.FTP_DOWN_FAIL)){
                                    log.e(TAG,"ftp 下载失败 - "+remotePath+remoteFileName);
                                    if (!remoteFileName.endsWith(".md5")){
                                        caller.downloadResult(task,1);
                                    }
                                    caller.nitifyMsg(remoteFileName,4);
                                }
                                //下载成功
                                if(currentStep.equals(ActiveFtpUtils.FTP_DOWN_SUCCESS)){
                                    log.i(TAG, "ftp下载succsee -"+ remoteFileName+" - 线程 - "+ Thread.currentThread().getName());
                                    if (!remoteFileName.endsWith(".md5")){
                                        caller.downloadResult(task,0);
                                        ftpDownloadImp(task,user,pass,host,remotePath,remoteFileName+".md5",localPath);
                                    }else{
                                        //获取源文件 code
                                        String sPath = localPath+remoteFileName.substring(0,remoteFileName.lastIndexOf("."));
                                        log.e(TAG,"文件 - "+sPath +" 比较 MD5值");
                                        String sCode = MD5Util.getFileMD5String(new File(sPath));
                                        if (sCode!=null){
                                            //比较md5
                                            if(MD5Util.FTPMD5(sCode, file.getAbsolutePath()) == 1){
                                                //md5 效验失败 删除文件
                                                cn.trinea.android.common.util.FileUtils.deleteFile(sPath);
                                                cn.trinea.android.common.util.FileUtils.deleteFile(file.getAbsolutePath());
                                                log.e(TAG,"文件 - "+sPath +" 比较 MD5值 失败 已删除");
                                            }
                                        }
                                    }
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
                      caller.nitifyMsg(url.substring(url.lastIndexOf("/")+1),1);
                      caller.nitifyMsg(url.substring(url.lastIndexOf("/")+1),2);
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
                      caller.notifyProgress(url.substring(url.lastIndexOf("/")+1),(current/total)+"",(speedTem/(1024 * 1.0))+" kb");
                  }
                  @Override
                  public void onSuccess(ResponseInfo<File> responseInfo) {
                      final String path  =responseInfo.result.getPath();
                      caller.downloadResult(task,0);
                      caller.nitifyMsg(url.substring(url.lastIndexOf("/")+1),3);
                  }
                  @Override
                  public void onFailure(HttpException e, String s) {
                      caller.downloadResult(task,1);
                      caller.nitifyMsg(url.substring(url.lastIndexOf("/")+1),4);
                  }
              });
    }


    /**
     * 被观察者
     * 数据
     */
    @Override
    public void update(Observable observable, Object data) {
        log.i(TAG,"update - "+data);
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
