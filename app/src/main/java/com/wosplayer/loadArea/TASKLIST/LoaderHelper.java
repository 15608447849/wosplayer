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

/**
 * Created by user on 2016/11/25.
 */

public class LoaderHelper implements Observer {

    private static final java.lang.String TAG = "LoaderHelper";
    private DownloadCallImp caller = null;
    private ExecutorService singleThreadExecutor;
    private HttpUtils http = null;
    public LoaderHelper() {
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        http = new HttpUtils();
        caller = new DownloadCallImp();
    }


    /**
     * 执行下载任务
     */
    private void excuteDownLoad(Task task) {
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                try {

                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 解析数据
     * @param task
     */
    private synchronized void parseDatas(Task task){

        int type = task.getType();

        if (type == Task.Type.HTTP){
            httpDownload(task);
        }

        if (type == Task.Type.FTP){
            ftpDownload(task);
        }

        if (type == Task.Type.FILE){
        }
    }

    private void ftpDownload(final Task task) {
        String uri = task.getUrl();
        String str = uri.substring(uri.indexOf("//") + 2);
        final String user = str.substring(0, str.indexOf(":"));
        final String pass = str.substring(str.indexOf(":") + 1, str.indexOf("@"));
        final String host = str.substring(str.indexOf("@") + 1, str.indexOf("/"));
        final String remotePath = str.substring(str.indexOf("/"), str.lastIndexOf("/") + 1);
        final String remoteFileName = str.substring(str.lastIndexOf("/") + 1);
        final String localPath = task.getSavePath();
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


                                //下载中
                                if (currentStep.equals(ActiveFtpUtils.FTP_DOWN_LOADING)){
                                    caller.notifyProgress(remoteFileName,downProcess+"",speed);
                                }

                                //ftp远程文件不存在
                                if (currentStep.equals(ActiveFtpUtils.FTP_FILE_NOTEXISTS)){
                                    log.e(TAG,"ftp服务器 不存在文件 - " + remotePath+remoteFileName);

                                    caller.downloadResult(task,1);
                                    caller.nitifyMsg(remoteFileName,4);
                                }
                                //连接失败
                                if(currentStep.equals(ActiveFtpUtils.FTP_CONNECT_FAIL)){
                                    log.e(TAG,"ftp 连接失败 - ");
                                    caller.downloadResult(task,1);
                                    caller.nitifyMsg(remoteFileName,4);
                                }
                                //下载失败
                                if (currentStep.equals(ActiveFtpUtils.FTP_DOWN_FAIL)){
                                    log.e(TAG,"ftp 下载失败 - "+remotePath+remoteFileName);
                                    caller.downloadResult(task,1);
                                    caller.nitifyMsg(remoteFileName,4);

                                }
                                if (currentStep.equals(ActiveFtpUtils.FTP_CONNECT_SUCCESSS)){
                                    log.d(TAG,"ftp 连接成功 - ");
                                    caller.nitifyMsg(remoteFileName,1);
                                    caller.nitifyMsg(remoteFileName,2);
                                }

                                if(currentStep.equals(ActiveFtpUtils.FTP_DOWN_SUCCESS)){
                                    //成功
                                    log.i(TAG, "ftp下载succsee -"+ remoteFileName+" - 线程 - "+ Thread.currentThread().getName());
                                    caller.downloadResult(task,0);
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
        //获取到一个任务 -> 执行一个线程
        if (data!=null){
            parseDatas((Task) data);
        }
    }

    /**
     * 判断文件是不是存在
     */
    public boolean fileIsExist(String filename){
        return   fileUtils.checkFileExists(filename);
    }








}
