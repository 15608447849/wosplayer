package com.wosplayer.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wosplayer.app.log;

import java.io.File;

/**
 * Created by user on 2016/7/19.
 * 错误日志上传
 */
public class logUploadService extends IntentService {

    private static final String TAG = "_logUploadService";
    public logUploadService(){
        super("logUploadService");
    }
    public static final String uriKey = "uris";

    private int needFileCount = -1;//需要上传的文件数量
    private int beOnFileCount = 0;

    private String uri = null;
    private String terminalNo;
    @Override
    protected void onHandleIntent(Intent intent) {

        log.i(TAG,"------------------------------------------------------------onHandleIntent()日志文件开始上传服务准备----------------------------------------------------------------------------------");

        //获取uri
        uri = intent.getExtras().getString(uriKey);
        if(uri==null|| uri.equals("")) return;

        terminalNo = intent.getExtras().getString("terminalNo","0000");
     //获取日志文件列表
        File[] filelist = getLogFileList();
        if (filelist == null){
            Log.e(TAG,"获取日志文件列表失败");
            return;
        }
        needFileCount = filelist.length;
        if (needFileCount == 0){
            Log.e(TAG,"没有需要上传的错误日志");
            return;
        }
      //上传日志 -> 全部 成功 删除所有日志文件
        for (int i=0;i<filelist.length;i++){
            uploadFile(uri,filelist[i].getAbsolutePath(),this);
        }
    }

    /**
     * 获取 所有日志文件
     */
    private  File[]  getLogFileList(){
        log.v(TAG,"日志文件 本地路径 : " + serviceLog.LOG_PATH_SDCARD_DIR);
        File file = new File(serviceLog.LOG_PATH_SDCARD_DIR);
        if (file.isDirectory()) {
           return file.listFiles();
        }
         return  null;
    }

    /**
     * xutils  上传文件
     */
    private static HttpUtils http = new HttpUtils();
    private void uploadFile(String uploadUrl, final String filePath, final logUploadService service){
        RequestParams params = new RequestParams(); // 默认编码UTF-8
        params.addBodyParameter("cmd", "androidLogData");
        params.addBodyParameter("terminalId",terminalNo);
       // params.setBodyEntity(new FileUploadEntity(new File(filePath),"application/octet-stream"));//"binary/octet-stream"));//"binary/octet-stream"));//,
        params.addBodyParameter("file",new File(filePath));
        http.send(HttpRequest.HttpMethod.POST,
                uploadUrl,
                params,
                new RequestCallBack<String>() {
                    @Override
                    public void onStart() {
                        log.i("开始上传 : "+filePath);
                    }
                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        if (isUploading) {
                         log.i(TAG,"日志文件:"+filePath+"\n     上传进度"+current + "/" + total);
                        } else {
                          log.i(TAG,"日志文件:"+filePath+"- isUploading="+isUploading);
                        }
                    }
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        log.i(TAG,"日志文件: "+filePath+"\n上传成功:"+ responseInfo.result);
                        service.resultCount(filePath);
                    }
                    @Override
                    public void onFailure(HttpException error, String msg) {
                        log.e(TAG,"日志文件:"+filePath+"\n上传失败:("+ error.getExceptionCode() + "-" + msg+")");
                    }
                });
    }

    /**
     * 上传成功结果回调
     * @param filepath
     */
    private void resultCount(String filepath) {
        log.i(TAG,"删除日志文件:"+filepath);
        if(new File(filepath).delete()){
            log.i(" success ");
        }else{
            log.i(" failt ");
        }
    }
}
