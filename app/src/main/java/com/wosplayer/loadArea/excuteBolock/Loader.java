package com.wosplayer.loadArea.excuteBolock;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.loadArea.ftpBlock.ActiveFtpUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by user on 2016/6/24.
 */
public class Loader {
    private static final String TAG = "_Loader";
    private static ReentrantLock locker = new ReentrantLock();//同步锁
    private LoaderCaller other_caller;
    public void settingCaller(LoaderCaller calle){
        this.other_caller = calle;
    }
    private static List<String> loadingTaskList = Collections.synchronizedList(new LinkedList<String>());
    /**
     * 添加一个任务
     * @param Task
     */
    public boolean addTask(String Task){

        if (!loadingTaskList.contains(Task)){
            loadingTaskList.add(Task);
            log.i("下载任务队列 (添加) :"+TAG,Task);
            return true;
        }
        else {
            log.i("已存在,不要重复添加"+TAG,Task);
            addRepeatTask(this);
            return false;
        }

    }
    /**
     * 删除一个任务
     */
    public  void complateTask(String Task, final String filepath){

        if (loadingTaskList.contains(Task)){
            loadingTaskList.remove(Task);
            log.i("下载任务队列 (移除) :"+TAG,Task);
        }
        else {
            log.i(TAG,Task + "不存在 任务队列 ,不需要删除");

            log.e(TAG,"当前重复任务队列对象:"+repeatTaskList.toString());
        }

        Schedulers.newThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                //异步通知所有人 一个任务完成
                notifyRepatList(Loader.this.muri,filepath);
            }
        });
    }

    /**
     * 请放入io 线程
     * @param uri
     */
    public void LoadingUriResource(String uri,String mySettingFileName) {

        try {
            locker.lock();
            muri = uri;
            if(!addTask(uri)){

                return;
            };

            log.i(TAG, Loader.this.toString()+" -> 加载资源 ->"+uri );

            String localFileDir =  wosPlayerApp.config.GetStringDefualt("basepath", "/sdcard/mnt/playlist");
        //判断路径i
        if (uri.startsWith("http://")) {
            String fns = uri.substring(uri.lastIndexOf("/") + 1);
            String fps = localFileDir + fns;
            if (mySettingFileName!=null){
                fps = mySettingFileName;
            }
            HttpLoad(uri, fps);

        } else if (uri.startsWith("ftp://")) {
            // ftp://ftp:FTPmedia@21.89.68.163/uploads/1466573392435.png

            String str = uri.substring(uri.indexOf("//") + 2);
            final String name = str.substring(0, str.indexOf(":"));
            final String password = str.substring(str.indexOf(":") + 1, str.indexOf("@"));
            final String host = str.substring(str.indexOf("@") + 1, str.indexOf("/"));
            final String path = str.substring(str.indexOf("/"), str.lastIndexOf("/") + 1);
            final String filename = str.substring(str.lastIndexOf("/") + 1);
            final String localPath = localFileDir;
            Schedulers.newThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    FTPload(host, name, password, path, filename, localPath);
                }
            });
        }
        }catch (Exception e){
            log.e(TAG,e.getMessage());
    }finally {
        locker.unlock();
    }
    }


    /**
     * Http
     */
    private void HttpLoad(final String url, final String Filepath){
        HttpUtils http = new HttpUtils();
        HttpHandler handler =http.download(
                url,
                Filepath,
                false,// 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                false,//如果从请求返回信息中获取到文件名，下载完成后自动重命名
                new RequestCallBack<File>() {

                    @Override
                    public void onStart() {
                        log.i(TAG,"启动一个http下载任务:"+ url);

                        nitifyMsg(url.substring(url.lastIndexOf("/")+1),1);
                        nitifyMsg(url.substring(url.lastIndexOf("/")+1),2);

                    }
                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        notifyProgress(url.substring(url.lastIndexOf("/")+1),(current/total)+"",(current/1024)+" kb");
                    }
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        log.i(TAG,"http 下载完成" + url);
                        final String path  =responseInfo.result.getPath();
                        Schedulers.newThread().createWorker().schedule(new Action0() {
                            @Override
                            public void call() {
                                caller.Call(path);
                                nitifyMsg(url.substring(url.lastIndexOf("/")+1),3);
                            }
                        });
                    }
                    @Override
                    public void onFailure(HttpException error, String msg) {
                        log.e(TAG,"http 下载失败:"+msg + url);
                        Schedulers.newThread().createWorker().schedule(new Action0() {
                            @Override
                            public void call() {
                                loadFileRecall("loaderr");
                                nitifyMsg(url.substring(url.lastIndexOf("/")+1),4);
                            }
                        });

//                        if (msg.equals("maybe the file has downloaded completely")){
//                            caller.Call(Filepath);
//                        }
//                        if (msg.equals("Not Found")){
//                            loadFileRecall(Filepath);
//                        }

                    }
                }
        );
    }

    private void loadFileRecall(String Filepath) {
        log.d("服务端 未找到文件:"+ Filepath);
        caller.Call("404");
//        if (new File(Filepath).exists()){//存在  直接回调
//            log.w("本地存在 文件:"+Filepath);
//            caller.Call(Filepath);
//            return;
//        }
//        //下载404图片
//        String nofoundUri = "http://pic.qiantucdn.com/58pic/17/68/32/5578959f3ec76_1024.jpg";
//        this.LoadingUriResource(nofoundUri,Filepath);
    }


    /**
     *
     * @param host 服务器ip
     * @param user 用户名
     * @param pass  密码
     * @param remotePath 远程目录
     * @param fileName  要下载的文件名
     * @param localPath 本地路径
     */
    private void FTPload(final String host, String user, String pass, String remotePath, final String fileName, String localPath){
        log.i(TAG, "启动一个FTP任务,所在线程:"+ Thread.currentThread().getName()+",任务名:"+fileName );

            ActiveFtpUtils ftp = new ActiveFtpUtils(host,21,user,pass);
            ftp.downloadSingleFile(remotePath + fileName,
                    localPath,
                    fileName,
                    1000,
                    new ActiveFtpUtils.DownLoadProgressListener() {
                @Override
                public void onDownLoadProgress(String currentStep, long downProcess, String speed, File file) {

                    if(currentStep.equals(ActiveFtpUtils.FTP_DOWN_SUCCESS)){
                        //成功
                        log.i(TAG, file.getAbsolutePath()+"下载完成,当前线程 -:"+ Thread.currentThread().getName());
                        caller.Call(file.getAbsolutePath());
                        nitifyMsg(fileName,3);

                    }

                    if (currentStep.equals(ActiveFtpUtils.FTP_DOWN_LOADING)){
                        //下载中
                        notifyProgress(fileName,downProcess+"",speed);
                    }

                    if(currentStep.equals(ActiveFtpUtils.FTP_CONNECT_FAIL)){
                        //连接失败
                        log.e(TAG,"ftp 连接失败");
                        loadFileRecall("loaderr");
                        nitifyMsg(fileName,4);
                    }
                    if (currentStep.equals(ActiveFtpUtils.FTP_DOWN_FAIL)){
                        //下载失败
                        log.e(TAG,"ftp 下载失败"+fileName);
                        loadFileRecall("loaderr");
                        nitifyMsg(fileName,4);

                    }

                    if (currentStep.equals(ActiveFtpUtils.FTP_CONNECT_SUCCESSS)){
                        nitifyMsg(fileName,1);
                        nitifyMsg(fileName,2);
                    }
                }
            });

        }


    /**
     * 生成 进度
     *
     * 2 3 4
     *
     */
    private void nitifyMsg(String filename, int type){

        String TERMINAL_NO = wosPlayerApp.config.GetStringDefualt("terminalNo","0000");

        wosPlayerApp.sendMsgToServer("FTPS:"+TERMINAL_NO+";" + filename+ ";"+type);

    }

    private void notifyProgress(String filename, String process, String speed){

        String command = "PRGS:" +  wosPlayerApp.config.GetStringDefualt("terminalNo", "0000") + "," + filename + ","
                + process + "," + speed;
        wosPlayerApp.sendMsgToServer(command);
    }
/////////////////////////////////////
    /**
     * 数据回调
      */
    public interface LoaderCaller{
        void Call(String filePath);
    }
////////////////////////////////

    private String muri = null;
    private boolean existRepeatList = false;
    private LoaderCaller caller = new LoaderCaller() {
        @Override
        public void Call(String filePath) {

            log.i(TAG, "Call: 当前一个回调结果"+Loader.this.toString()+"-> "+Loader.this.muri);
            log.i(TAG, "Call: 执行线程"+ Thread.currentThread().getName());

            if (other_caller!=null){

                    try {
                        log.i(TAG,"Call:传递到子监听回调中...");
                        other_caller.Call(filePath);
                    }catch (Exception e){
                        log.e(TAG,"传递子监听回调err:"+e.toString());
                    }
            }

                if (muri!=null){
                    if (!existRepeatList){ // 不存在 重复列表
                        log.i(TAG, "不在重复任务队列,可以发出通告");
                        complateTask(muri,filePath);//完成任务
                    }
                    existRepeatList = false;
                }


        }
    };


    /**
     * 重复任务队列
     */
    private static Map<String,ArrayList<Loader>> repeatTaskList = Collections.synchronizedMap(new HashMap<String,ArrayList<Loader>>());

    private void addRepeatTask(Loader l){
        try{
            lock_repeat.lock();

            if(repeatTaskList.containsKey(l.muri)){
                ArrayList<Loader> list = repeatTaskList.get(l.muri);
                list.add(l);
                log.d(TAG,"找到存在的key:"+l.muri+" 对应的arr"+ list.toString());
            }else{
                ArrayList<Loader> arr = new ArrayList<Loader>();
                arr.add(l);
                repeatTaskList.put(l.muri,arr);
                log.d(TAG,"没有找到存在的key :"+l.muri+" 对应的arr,创建:"+ arr.toString());
            };
            l.existRepeatList = true;

        }catch (Exception e){

        }finally {
            lock_repeat.unlock();
        }
    }


    private static ReentrantLock lock_repeat = new ReentrantLock();

    private static void notifyRepatList(final String uri, final String filepath){

        try{
//            lock_repeat.lock();
            log.d(TAG,  " 开始通知 ");

            final ArrayList<Loader> arr = repeatTaskList.get(uri);

            if (arr!=null){
                log.d(TAG, uri+ " 存在 重复列表,取出映射的value :"+ arr.toString());
                repeatTaskList.remove(uri);

                for (final Loader l:arr){
                    Schedulers.newThread().createWorker().schedule(new Action0() {
                        @Override
                        public void call() {
                            l.receivedNotifi(uri,filepath);
                        }
                    });
                }

            }else{
                log.d(TAG, uri+ "  不存在 重复列表");
            }

            log.d(TAG,  "结束通知 ");

        }catch (Exception e){
            log.e(TAG,  "通告 下载 重复任务队列 Err :"+ e.toString());
        }finally {
//            lock_repeat.unlock();
        }

    }

    /**
     * 接收一个通告广播
     * @param uri
     * @param filePath
     */
    private void receivedNotifi(String uri, String filePath){

        String u = muri.trim();
        String t = uri.trim();
        boolean f =  u.equals(t);
        log.d(TAG,  u+ " < - >"+t+"{"+f+"}");
        if(f){
         caller.Call(filePath);
        }
    }
}




