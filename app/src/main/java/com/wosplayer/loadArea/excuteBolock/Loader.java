package com.wosplayer.loadArea.excuteBolock;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.loadArea.ftpBlock.ActiveFtpUtils;
import com.wosplayer.loadArea.otherBlock.fileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import installUtils.MD5Util;
import rx.Scheduler;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by user on 2016/6/24.
 */
public class Loader {
    public static int loadcount = 2 ;
    private static final String TAG = "_Loader";
    private static ReentrantLock locker = new ReentrantLock();//同步锁
    private LoaderCaller other_caller;
    public void settingCaller(LoaderCaller calle){
        this.other_caller = calle;
    }
    private static List<String> loadingTaskList = Collections.synchronizedList(new LinkedList<String>());
    private final static Scheduler.Worker ioThread = Schedulers.io().createWorker();
    private final static Scheduler.Worker notifyThread = Schedulers.io().createWorker();
    /**
     * 添加一个任务
     * @param Task
     */
    private boolean addTask(String Task){
        log.e(TAG," 當前进行中 - 任務隊列數量:"+loadingTaskList.size()+" -> 添加任務:["+Task+"]");

        if (!loadingTaskList.contains(Task)){
            loadingTaskList.add(Task);
            log.i(TAG," (添加success)" );
            return true;
        }
        else {
            log.i(TAG,"(添加faild)");
            addRepeatTask(this);
            return false;
        }

    }
    /**
     * 删除一个任务
     */
    private  void complateTask(String Task, final String filepath){
//        log.i(TAG,"当前所在线程:"+Thread.currentThread().getName()+";总线程数量:"+Thread.getAllStackTraces().size());
        log.w(TAG,"一個任務完成["+ Task+"] - 準備刪除 ,當前 下載中 任務數量:"+loadingTaskList.size());
        if (loadingTaskList.contains(Task)){
            loadingTaskList.remove(Task);
            log.w(TAG, "loading 任务队列 (移除) :"+Task);
        }
        else {
            log.w(TAG,Task + "不在 loading任务队列 ,不需要删除");
            log.w(TAG,"当前 重复任务 队列size :"+repeatTaskList.size());
        }

        notifyThread.schedule(new Action0() {
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
    public void LoadingUriResource(final String uri, String mySettingFileName) {

        try {
            locker.lock();
            muri = uri;

            //是否 加入 等待 ,如果进行中的任务数量已经满足了
            if (loadingTaskList.size()>loadcount){
                addWaitList(this);
                return;
            }
            //是否 是 重复任务
            boolean f = addTask(uri);
            if(!f){
                log.e("重复任务");
                return;
            };

//            log.i(TAG, Loader.this.toString()+" -> 加载资源 ->"+uri );

            String fns = uri.substring(uri.lastIndexOf("/") + 1);//文件名
            String localFileDir =  wosPlayerApp.config.GetStringDefualt("basepath", "/sdcard/mnt/playlist");//本地路径
            String fps = localFileDir + fns;//全路径

            final String finalFps = fps;
            if (fileIsExist(fps)) {


                log.i(TAG, "任务:" + uri + "-> 本地所在路径: " + finalFps + " -> exist!");
                ioThread.schedule(new Action0() {
                    @Override
                    public void call() {
                        caller.Call(finalFps);
                        nitifyMsg(uri.substring(uri.lastIndexOf("/") + 1), 1);
                        nitifyMsg(uri.substring(uri.lastIndexOf("/") + 1), 2);
                        nitifyMsg(uri.substring(uri.lastIndexOf("/") + 1), 3);
                    }
                });

                return;
            }

    log.e(" ! -- -- -- -- ! " );

        //判断路径i
        if (uri.startsWith("http://")) {
            HttpLoad(uri, finalFps);

        } else if (uri.startsWith("ftp://")) {
            // ftp://ftp:FTPmedia@21.89.68.163/uploads/1466573392435.png

            String str = uri.substring(uri.indexOf("//") + 2);
            final String name = str.substring(0, str.indexOf(":"));
            final String password = str.substring(str.indexOf(":") + 1, str.indexOf("@"));
            final String host = str.substring(str.indexOf("@") + 1, str.indexOf("/"));
            final String path = str.substring(str.indexOf("/"), str.lastIndexOf("/") + 1);
            final String filename = str.substring(str.lastIndexOf("/") + 1);
            final String localPath = localFileDir;
            ioThread.schedule(new Action0() {
                @Override
                public void call() {
                    FTPload(host, name, password, path, filename, localPath,null);
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
    private synchronized void HttpLoad(final String url, final String Filepath){
        HttpUtils http = new HttpUtils();
        HttpHandler handler =http.download(
                url,
                Filepath,
                false,// 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                false,//如果从请求返回信息中获取到文件名，下载完成后自动重命名
                new RequestCallBack<File>() {

                    @Override
                    public void onStart() {
                        log.i(TAG,"启动http下载:"+ url+" on Thread : "+Thread.currentThread().getName());
                        nitifyMsg(url.substring(url.lastIndexOf("/")+1),1);
                        nitifyMsg(url.substring(url.lastIndexOf("/")+1),2);
                    }
                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        notifyProgress(url.substring(url.lastIndexOf("/")+1),(current/total)+"",(current/1024)+" kb");
                    }
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        log.i(TAG,"http 下载完成:[" + url + "]当前所在线程 : "+Thread.currentThread().getName()+"-Thread count :"+Thread.getAllStackTraces().size());
                        final String path  =responseInfo.result.getPath();

                        loadFileRecall(path);
                        nitifyMsg(url.substring(url.lastIndexOf("/")+1),3);
                    }
                    @Override
                    public void onFailure(HttpException error, String msg) {
                        log.e(TAG,"http 下载失败:"+msg +" url:[" + url +"],当前所在线程:"+Thread.currentThread().getName());

                        loadFileRecall("loaderr");
                        nitifyMsg(url.substring(url.lastIndexOf("/")+1),4);

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

    private void loadFileRecall(final String Filepath) {
        log.d(" & loadFileRecall() ,"+ Filepath);

        if (Filepath.equals("loaderr")){
            log.e("文件:"+ Filepath+" - load faild");
            notifyThread.schedule(new Action0() {
                @Override
                public void call() {
                    caller.Call("404");
                }
            });
        }else{
            //load success
            notifyThread.schedule(new Action0() {
                @Override
                public void call() {
                    caller.Call(Filepath);
                }
            });
        }


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
    private synchronized void FTPload(final String host, final String user, final String pass, final String remotePath, final String fileName, final String localPath, final Object ob){
        log.i(TAG, "FTP任务["+fileName+"],所在线程:"+ Thread.currentThread().getName());

            final ActiveFtpUtils ftp = new ActiveFtpUtils(host,21,user,pass);


            ftp.downloadSingleFile(remotePath + fileName,
                    localPath,
                    fileName,
                    3,//重新链接次数
                    new ActiveFtpUtils.DownLoadProgressListener() {
                @Override
                public void onDownLoadProgress(String currentStep, long downProcess, String speed, File file) {

                    if(currentStep.equals(ActiveFtpUtils.FTP_DOWN_SUCCESS)){
                        //成功
                        log.i(TAG, "ftp下载succsee -"+fileName+" - 线程 - "+ Thread.currentThread().getName());

                        if (fileName.contains(".apk")){
                        //caller.Call(file.getAbsolutePath());
                        loadFileRecall(file.getAbsolutePath());
                        nitifyMsg(fileName,3);
                        return;
                        }

                        if (fileName.contains(".md5")){

                            String sp = ((File)ob).getAbsolutePath();
                            String dp = file.getAbsolutePath();
                            int sut =  MD5Util.FTPMD5(sp,dp);

                        if (sut==0){
                        loadFileRecall(sp);
                        nitifyMsg(((File)ob).getName(),3);
                            }else{

                                log.e(TAG,"文件:"+((File)ob).getName()+ " md5 效验失败!");
                                loadFileRecall("loaderr");
                                nitifyMsg(((File)ob).getName(),4);
                            }
                        }else{
                            File msd5file = MD5Util.getFileMD5String(file);
                            //下载 md5
                            FTPload(host,user,pass,remotePath,fileName+".md5",localPath,msd5file);
                        }






//                        caller.Call(file.getAbsolutePath());
                     /*   loadFileRecall(file.getAbsolutePath());
                        nitifyMsg(fileName,3);*/
                    }

                    if (currentStep.equals(ActiveFtpUtils.FTP_DOWN_LOADING)){
                        //下载中
                        notifyProgress(fileName,downProcess+"",speed);
                    }

                    if (currentStep.equals(ActiveFtpUtils.FTP_FILE_NOTEXISTS)){
                        //ftp远程文件不存在
                        log.e(TAG,"ftp服务器 不存在文件");
                        loadFileRecall("loaderr");
                        nitifyMsg(fileName,4);
                    }
                    if(currentStep.equals(ActiveFtpUtils.FTP_CONNECT_FAIL)){
                        //连接失败
                        log.e(TAG,"ftp 连接失败 ");
                        loadFileRecall("loaderr");
                        nitifyMsg(fileName,4);
                    }
                    if (currentStep.equals(ActiveFtpUtils.FTP_DOWN_FAIL)){
                        //下载失败
                        log.e(TAG,"ftp 下载失败 :"+fileName);
                        loadFileRecall("loaderr");
                        nitifyMsg(fileName,4);

                    }

                    if (currentStep.equals(ActiveFtpUtils.FTP_CONNECT_SUCCESSS)){
                        log.d(TAG,"ftp 连接成功 - "+ fileName);
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
    private static int callCount = 0;
    private String muri = null;
    private boolean existRepeatList = false;

    private LoaderCaller caller = new LoaderCaller() {
        @Override
        public void Call(String filePath) {

            log.i(TAG, "Call: 当前一个回调结果[ "+Loader.this.muri+" ]");

            log.d(TAG, "loader Call(): 执行线程"+ Thread.currentThread().getName()+"\n\r thread size:"+ Thread.getAllStackTraces().size());

            if (other_caller!=null){
                    try {
                        log.d(TAG,"Call:传递到 子监听回调 , count:"+ callCount++);
                        other_caller.Call(filePath);

                    }catch (Exception e){
                        log.e(TAG,"传递子监听回调err:"+e.toString());
                    }
            }
                if (muri!=null){
                    if (!existRepeatList){ // 不存在 重复列表
                        log.d(TAG, "不在重复任务队列");
                        complateTask(muri,filePath);//完成任务
                    }
                    existRepeatList = false;
                }
            //通知等待队列
            notifyWaitList();
        }
    };


    /**
     * 重复任务队列
     */
    private static Map<String,ArrayList<Loader>> repeatTaskList = Collections.synchronizedMap(new HashMap<String,ArrayList<Loader>>());

    private void addRepeatTask(Loader l){
        try{
//            lock_repeat.lock();
            if(repeatTaskList.containsKey(l.muri)){
                ArrayList<Loader> list = repeatTaskList.get(l.muri);
                log.e(TAG,"repeat list key:"+l.muri+" value array:"+ list.toString());
                list.add(l);

            }else{
                ArrayList<Loader> arr = new ArrayList<Loader>();
                arr.add(l);
                log.e(TAG,"not fount key :"+l.muri+" create array:"+ arr.toString());
                repeatTaskList.put(l.muri,arr);
            }

            l.existRepeatList = true;

        }catch (Exception e){

        }finally {
//            lock_repeat.unlock();
        }
    }


    private static ReentrantLock lock_repeat = new ReentrantLock();

    private static void notifyRepatList(final String uri, final String filepath){

        try{
            lock_repeat.lock();
            log.w(TAG,  " 开始通知 "+Thread.currentThread().getName()+" <->thread size: "+Thread.getAllStackTraces().size());

            final ArrayList<Loader> arr = repeatTaskList.get(uri);

            if (arr!=null){
                log.w(TAG, uri + " 在 重复列表中,映射的value:"+ arr.toString());
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
                log.i(TAG, uri+ "  不在 重复列表");
            }
            log.w(TAG,  "结束通知 ");

        }catch (Exception e){
            log.e(TAG,  " 下载完成后  通告 重复任务队列 Err :"+ e.toString());
        }finally {
            lock_repeat.unlock();
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
        log.w(TAG,  u+ " < - >"+t+"结果:{"+f+"}");
        if(f){
         caller.Call(filePath);
        }
    }


    /**
     * 判断一个文件是不是已经存在
     */
    public boolean fileIsExist(String filename){
      return   fileUtils.checkFileExists(filename);
    }
    /**
     * 任務過多 等待隊列
     */
    private static List<Loader> waitList = Collections.synchronizedList(new LinkedList<Loader>());
    /**
     * 加入等待隊列
     */
    private static void addWaitList(Loader loader){
        log.i(TAG,"加入等待队列 : [" + loader.muri + "] ");
        waitList.add(loader);
    }
    /**
     * 通知 等待隊列 執行
     */
    private static void notifyWaitList(){
        //如果存在 每次只執行 至多 5 個
        if (loadingTaskList.size()==0){
            if (waitList.size()>0){
                //ArrayList<Loader> waitload = new ArrayList<Loader>();
                Iterator<Loader> itr = waitList.iterator();
                int i = 0;
                while(itr.hasNext()){
                    Loader o = itr.next();
//                    waitload.add(o);
                    o.LoadingUriResource(o.muri,null);
                    itr.remove();
                    i++;

                    if (i==loadcount){
                        break;
                    }
                }

               /* if (waitload.size() == 0){
                    waitload = null;
                    return;
                }
                for (Loader loader:waitload){

                    loader.LoadingUriResource(loader.muri,null);
                }*/
//                waitload.clear();
//                waitload = null;
                log.i(TAG," ----------------------- 完成一次 等待隊列的執行 ----------------------------------------");
            }
        }
    }


}




