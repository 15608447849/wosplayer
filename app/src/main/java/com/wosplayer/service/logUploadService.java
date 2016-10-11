package com.wosplayer.service;

import android.app.IntentService;
import android.content.Intent;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wos.Toals;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;

import java.io.File;

/**
 * Created by user on 2016/7/19.
 */
public class logUploadService extends IntentService {


    public logUploadService(){
        super("logUploadService");
    }
    public static final String uriKey = "uris";

    private int needFileCount = -1;
    private int beOnFileCount = 0;

    @Override
    protected void onHandleIntent(Intent intent) {

        log.i("日志文件开始上传服务准备");

        //获取uri
        String uri = intent.getExtras().getString(uriKey);
        if(uri.equals("") || uri==null) return;

      //停止日志写入
        log.isWrite = false;

     //获取日志文件列表
        File[] filelist = getLogFileList();
        if (filelist == null){
            Toals.Say("上传日志_ 无日志文件 ,列表空");
            return;
        }
        needFileCount = filelist.length;
        if (needFileCount == 0){
            Toals.Say("上传日志_ 无日志文件 ,列表长度 0");
            return;
        }



      //上传日志 ->全部 成功 删除所有日志文件
        for (int i=0;i<filelist.length;i++){
            uploadFile(uri,filelist[i].getAbsolutePath(),this);
        }

    }

    /**
     * 获取 所有日志文件
     */
    private  File[]  getLogFileList(){
        log.v("上传日志本地文件路径:"+serviceLog.LOG_PATH_SDCARD_DIR);
        File file = new File(serviceLog.LOG_PATH_SDCARD_DIR);
        if (file.isDirectory()) {
           return file.listFiles();
        }
         return  null;
    }


    private HttpUtils httpUtils;

    private void uploadFile(String uploadUrl, final String filePath, final logUploadService service){
        RequestParams params = new RequestParams(); // 默认编码UTF-8
//        params.addHeader("name", "value");
//        params.setContentType("text/html");
//        params.addBodyParameter("file",new File(filePath));

        params.addBodyParameter("cmd", "androidLogData");
        params.addBodyParameter("terminalId", wosPlayerApp.config.GetStringDefualt("terminalNo",""));
       // params.setBodyEntity(new FileUploadEntity(new File(filePath),"application/octet-stream"));//"binary/octet-stream"));//"binary/octet-stream"));//,
        params.addBodyParameter("file",new File(filePath));

        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST,
                uploadUrl,
                params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                        log.i("日志文件开始上传"+filePath);
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        if (isUploading) {
                         log.i("日志文件:"+filePath+"上传"+current + "/" + total);
                        } else {
                            log.i("日志文件:"+filePath+"上传"+current + "/" + total);
                        }
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        log.i("日志文件:"+filePath+"上传"+ responseInfo.result);
                        service.resultCount(filePath);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        log.i("日志文件:"+filePath+"上传:"+ error.getExceptionCode() + ":" + msg);
                    }
                });
    }

    private void resultCount(String filepath) {

      File f = new File(filepath);
        f.delete();
        log.i("删除日志文件:"+filepath);

        if (needFileCount <=0){
            return;
        }

        beOnFileCount++;
        if (beOnFileCount == needFileCount){

            //开启 日志写入
            log.isWrite = true;
            needFileCount = -1;
        }
    }


}
