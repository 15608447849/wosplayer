package com.wosplayer.loadArea.TASKLIST;

import android.util.Log;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.wosplayer.app.Logs;
import com.wosplayer.loadArea.ftpBlock.FtpHelper;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URI;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.installUtils.MD5Util;

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

        Task.Type type = task.getType();
        if (type.equals(Task.Type.HTTP) ){
            httpDownload(task);
        }
        if (type.equals(Task.Type.FTP)){
            ftpDownloadImp(task);
        }
        if (type.equals(Task.Type.FILE)){
            cpFile(task);
        }
    }
    //复制本地文件到 -app 资源目录下
    private void cpFile(Task task) {
        File jhFile = null;
        try {
            jhFile = new File( new URI(task.getUrl()));
            if (jhFile.exists()){

                String desPath = task.getLocalPath()+task.getLocalName();
                Log.i(TAG,"复制建行资源文件到app资源目录 - \n"+jhFile.getAbsolutePath()+" -> "+desPath);
                FileUtils.copyFile(jhFile, new File(desPath));
                caller.downloadResult(task,0);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
         Log.e(TAG,"建行资源文件不存在 或者 复制失败 - "+jhFile);
         caller.downloadResult(task,1);
    }



    //Ftp 下载助手
    private void ftpDownloadImp(Task task) {
        new FtpHelper(task.getFtpUser())
                .downloadSingleFile(
                        task,
                        3,
                        new FtpHelper.onListener() {

                            @Override
                            public void ftpConnectState(int stateCode,Task task) {
                                Logs.i(TAG,"连接服务器 - "+task.getFtpUser().toString());
                                if (stateCode==FtpHelper.FTP_CONNECT_SUCCESSS){
                                    Logs.i(TAG,"连接成功");
                                    caller.nitifyMsg(task.getTerminalNo(),task.getRemoteName(),1);
                                    caller.nitifyMsg(task.getTerminalNo(),task.getRemoteName(),2);
                                }
                                if (stateCode==FtpHelper.FTP_CONNECT_FAIL){
                                    Logs.e(TAG,"ftp 连接失败");
                                    caller.nitifyMsg(task.getTerminalNo(),task.getRemoteName(),4);
                                    caller.downloadResult(task,1,task.getRemoteName(),".md5");
                                }
                            }
                            @Override
                            public void ftpNotFountFile(Task task) {
                                Logs.e(TAG,"ftp 服务器未发现文件 : "+ task.getRemoteName());
                                caller.downloadResult(task,1);
                                caller.nitifyMsg(task.getTerminalNo(),task.getRemoteName(),4);
                            }

                            @Override
                            public void downLoading(Task task,long downProcess, String speed, String fileName) {
                                caller.notifyProgress(task.getTerminalNo(),fileName,downProcess+"%",speed);
                            }
                            @Override
                            public void downLoadFailt(Task task) {
                                Logs.e(TAG,"FTP 下载失败 : "+task.toString());
                                caller.nitifyMsg(task.getTerminalNo(),task.getRemoteName(),4);
                                caller.downloadResult(task,1,task.getRemoteName(),".md5");
                            }
                            @Override
                            public void error(Exception e) {
                                e.printStackTrace();
                            }
                            @Override
                            public void downLoadSuccess(Task task) {
                                Logs.i(TAG, "FTP 成功下载 -"+ task.toString() +" - 当前线程 - "+ Thread.currentThread().getName());
                                if (caller.downloadResult(task,0,task.getRemoteName(),".png")
                                        || caller.downloadResult(task,0,task.getRemoteName(),".jpg")
                                        || caller.downloadResult(task,0,task.getRemoteName(),".mp4")
                                        ){
                                    Task ntask = Task.TaskFactory.createFtpTask(task,task.getRemoteName()+".md5");
                                    //下载MD5值
                                    TaskQueue.getInstants().addTask(ntask);

                                }else if (caller.downloadResult(task,0,task.getRemoteName(),".md5")){
                                    //md5文件
                                    //获取源文件code

                                    String md5Path = task.getLocalPath()+task.getLocalName();
                                    String sPath = task.getLocalPath()+task.getLocalName().substring(0,task.getLocalName().lastIndexOf("."));

                                    String sCode = MD5Util.getFileMD5String(new File(sPath));
                                    if (sCode!=null){
                                        //比较md5
                                        if(MD5Util.FTPMD5(sCode, md5Path) == 1){
                                            //md5 效验失败 删除文件
                                            cn.trinea.android.common.util.FileUtils.deleteFile(sPath);
                                            Logs.e(TAG,"文件 - "+sPath +" 比较 MD5值 失败 已删除");
                                        }else{
                                            Logs.e(TAG,"文件 - "+sPath +" 比较 MD5值 正确");
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
       final String filePath = task.getLocalPath()+task.getLocalName();

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
                      Logs.i(TAG,"启动http下载:"+ url+" on Thread : "+Thread.currentThread().getName());
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
                      caller.notifyProgress(task.getTerminalNo(),task.getLocalName(),(current/total)+"",(speedTem/(1024 * 1.0))+" kb");
                  }
                  @Override
                  public void onSuccess(ResponseInfo<File> responseInfo) {
                      final String path  =responseInfo.result.getPath();
                      caller.downloadResult(task,0);
                      caller.nitifyMsg(task.getTerminalNo(),task.getLocalName(),3);
                  }
                  @Override
                  public void onFailure(HttpException e, String s) {
                      caller.downloadResult(task,1);
                      caller.nitifyMsg(task.getTerminalNo(),task.getLocalName(),4);
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
