package com.wosplayer.download.operation;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.wosplayer.app.Logs;
import com.wosplayer.download.ftp.FtpHelper;
import com.wosplayer.download.util.DownloadFileUtil;
import com.wosplayer.download.util.MD5Util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user on 2016/11/25.
 */

public class DownloadHelper implements Observer {//观察者

    private static final java.lang.String TAG = "下载助手";
    private DownloadCaller caller = null;
    //private ExecutorService singleThreadExecutor;
    private HttpUtils http = null;
    ExecutorService fixedThreadPool;
    public DownloadHelper() {

    }
    public void initWord(){
//        singleThreadExecutor = Executors.newSingleThreadExecutor();
        fixedThreadPool = Executors.newFixedThreadPool(5);
        http = new HttpUtils();
        caller = new DownloadCaller();

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
        task.setDownloadFailtTime(new Date());
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
                FileUtils.copyFile(jhFile, new File(desPath));
                caller.downloadResult(task,0,"成功复制文件:"+jhFile.getAbsolutePath()+" -> "+desPath);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
         caller.downloadResult(task,1,"复制文件失败:"+jhFile);
    }

    //Ftp 下载助手
    private void ftpDownloadImp(Task task) {
        FtpHelper.downloadSingleFile(
                        task,
                        new FtpHelper.onListener() {
                            @Override
                            public void ftpConnectState(int stateCode,Task task,String message) {
                                if (stateCode==FtpHelper.FTP_CONNECT_SUCCESSS){
                                    caller.downloadResult(task,-1,"ftp 连接成功>>"+task.getFtpUser().toString());

                                }
                                if (stateCode==FtpHelper.FTP_CONNECT_FAIL){
                                    caller.downloadResult(task,1,"ftp 连接失败>>"+task.getFtpUser().toString()+" - "+message);
                                }
                            }
                            @Override
                            public void ftpNotFountFile(Task task) {
                                caller.downloadResult(task,1,"ftp 服务器未发现文件 : "+ task.getRemotePath()+task.getRemoteName());
                            }

                            @Override
                            public void downLoading(Task task,long downProcess, String speed, String fileName) {
                                caller.notifyProgress(task.getTerminalNo(),fileName,downProcess+"%",speed);
                            }
                            @Override
                            public void downLoadFailt(Task task,Exception error) {
                                caller.downloadResult(task,1,"ftp 下载失败 : "+task.toString()+" ;"+error.getMessage());
                            }
                            @Override
                            public void error(Exception e) {
                                e.printStackTrace();
//                                Logs.w(TAG,"\n"+e.getMessage()+"\n");
                            }
                            @Override
                            public void downLoadSuccess(int type,Task task) {
                                if (type == 0){
                                    //本地存在
                                    caller.downloadResult(task,0,"["+task.toString()+"] - 本地已经存在 ");
                                }
                                if (type == 1){
                                    //网络下载成功
                                    caller.downloadResult(task,0,"["+task.toString()+"] - ftp 成功下载 ");
                                }

                                if (DownloadFileUtil.isValidSuffix(task.getRemoteName(),".png",".jpg",".mp4")){
                                    Task ntask = Task.TaskFactory.createFtpTask(task,task.getRemoteName()+".md5");
                                    //下载MD5值
                                    TaskQueue.getInstants().addTask(ntask);

                                }else if (DownloadFileUtil.isValidSuffix(task.getRemoteName(),".md5")){
                                    //md5文件
                                    //获取源文件code
                                    String md5Path = task.getLocalPath()+task.getLocalName();
                                    String sPath = task.getLocalPath()+task.getLocalName().substring(0,task.getLocalName().lastIndexOf("."));

                                    String sCode = MD5Util.getFileMD5String(new File(sPath));
                                    if (sCode!=null){
                                        //比较md5
                                        if(MD5Util.FTPMD5(sCode, md5Path) == 1){
                                            //md5效验失败 删除文件
                                            cn.trinea.android.common.util.FileUtils.deleteFile(sPath);
                                            Logs.e(TAG,"文件 - ["+sPath +"] 比较 MD5值 失败 已删除");
                                        }else{
                                            Logs.i(TAG,"文件 - ["+sPath +"] 比较 MD5值 正确");
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
                      //Logs.i(TAG,"启动http下载:"+ url+" on Thread : "+Thread.currentThread().getName());
                      caller.downloadResult(task,-1,"启动http下载:"+ url+" on Thread : "+Thread.currentThread().getName());
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
                      caller.downloadResult(task,0,"[ http下载成功 : "+task.toString()+" ]");
                  }
                  @Override
                  public void onFailure(HttpException e, String s) {
                      caller.downloadResult(task,1,"[ http下载成功 : "+task.toString()+" ; "+e.getMessage() );
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
        return   DownloadFileUtil.checkFileExists(filename);
    }



}
