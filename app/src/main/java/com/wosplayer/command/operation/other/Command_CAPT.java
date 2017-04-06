package com.wosplayer.command.operation.other;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import com.wosplayer.Ui.element.definedView.Mtextscroll;
import com.wosplayer.Ui.element.definedView.Mvideo;
import com.wosplayer.app.AppUtils;
import com.wosplayer.app.Logs;
import com.wosplayer.app.SystemConfig;
import com.wosplayer.command.operation.interfaces.iCommand;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/7/29.
 * catch screen
 */
public class Command_CAPT  extends Thread implements iCommand {
    private static final String TAG = "截屏";
    private TimeScreen mThread;
    @Override
    public void execute(final Activity activity, String param) {
//        String command = "screencap -p "+ basepath;
        //参数 6 - 没过6秒截屏一次. 参数 0 停止截屏

        SystemConfig config = SystemConfig.get().read();
        String capturePath = config.GetStringDefualt("CapturePath","");
        String uploadUrl = config.GetStringDefualt("CaptureURL","");
        String terminalNo = config.GetStringDefualt("terminalNo","");
        boolean isDelete = (config.GetIntDefualt("CaptureSave",0) == 0) ;
        boolean isNotify = (config.GetIntDefualt("CaptureNoty",0) == 0);
        int mode = config.GetIntDefualt("CaptureMode",0);

        if (param == null || param.equals("")){
            liunxCommadScreen(activity,terminalNo,capturePath,uploadUrl,isDelete,isNotify,mode);
            return;
        }
        int time = 0;
        try {
            time = Integer.parseInt(param);
        } catch (NumberFormatException e) {
        }
        Logs.e(TAG,"定时截屏循环时间:"+time+" 秒/次");
        //停止截图线程
        if (mThread!=null){
            mThread.stopThread();
            mThread = null;
        }
        if (time > 0){
            mThread = new TimeScreen(activity,capturePath,uploadUrl,terminalNo,isDelete,isNotify,mode,time);
            mThread.startThread();
            mThread.start();
        }
    }

    private synchronized void liunxCommadScreen(Activity activity,String terminalNo, String savePath, String url,boolean isdelete,boolean isnoty,int mode) {

        Logs.d(TAG,"开始>> 保存:"+terminalNo+" - 本地截图:"+savePath+" - 上传地址: "+url);
        if (mode == 0){
            String cmd = "screencap -p "+savePath;
            ShellUtils.CommandResult result = ShellUtils.execCommand(cmd,true,true);
            if (result.result == 0){
                Logs.d(TAG,"liunx 命令(screencap -p) 截屏完成 - "+savePath);
            }
        }
        catchScreen(activity,terminalNo,savePath,url,mode);
        //上传
        uploadImage(activity,terminalNo,savePath,url,isdelete,isnoty);
    }

    private void catchScreen(Activity activity,String terminalNo,String savePath,String url,int mode){
        if (mode == 0){
            try {
                File image = new File(savePath);
                if (image.exists()){
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds=true;
                    FileInputStream fis = new FileInputStream(image);
                    BitmapFactory.decodeStream(fis,null,options);
                    if (options.outWidth*options.outHeight > 10) return;
                }

                if (cn.trinea.android.common.util.FileUtils.isFileExist(savePath)){
                    cn.trinea.android.common.util.FileUtils.deleteFile(savePath);
                }
            } catch (FileNotFoundException e) {
                Logs.e(TAG, "命令行截屏文件无效 : "+e.getMessage() );
            }
        }

        Bitmap bgbmp = null;
            FileOutputStream fos = null;
        try {
            // 设置主屏界面，作为一个控件
            View view = activity.getWindow().getDecorView();
            // 获去屏幕的宽高
            Display display = activity.getWindowManager().getDefaultDisplay();
//            view.layout(0, 0, display.getWidth(), display.getHeight());
            view.destroyDrawingCache();//or setDrawingCacheEnabled(false)
            view.setDrawingCacheEnabled(true);//提高绘图速度
            bgbmp = Bitmap.createBitmap(view.getDrawingCache());

            List<View> vList = getAllChildViews(view);
            // 视频截图
            Bitmap videoImage = null;
            for (View z : vList) {
                if (z instanceof Mvideo) {

                    Mvideo cvv = (Mvideo) z;
                    videoImage =  cvv.getCurrentFrame();
                    if (videoImage != null) {
                        bgbmp = composeImage(z.getLeft(), z.getTop(), bgbmp, videoImage);
                    }
                }else if(z instanceof Mtextscroll){
                    Mtextscroll cvv = (Mtextscroll) z;
                    videoImage = cvv.getBitmap();
                    if (videoImage != null) {
                        bgbmp = composeImage(z.getLeft(), z.getTop(), bgbmp, videoImage);
                    }
                }
            }

            if (bgbmp!=null){
                //縮放
                //bgbmp = resizeImage(bgbmp,360, 360);
                // 保存新的位图到本地路径
                fos = new FileOutputStream(savePath);
                if (fos != null) {
                    bgbmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                }
            }
        }catch(Exception e){
            cn.trinea.android.common.util.FileUtils.deleteFile(savePath);
            e.printStackTrace();
        }finally {
            try {
                if (bgbmp!=null) bgbmp.recycle();
                if (fos!=null) fos.close();
            } catch (IOException e) {
            }
        }
  }



    /**
     * 根据旧图的宽高做一个画布，把子图画到原图上，返回新图。
     * @param left
     * @param top
     * @param bgbmp old
     * @param videoImage child
     * @return
     */
    private Bitmap composeImage(int left, int top, Bitmap bgbmp, Bitmap videoImage) {
            if (bgbmp != null && videoImage != null ){
                // 获取原图宽高
                int sw = bgbmp.getWidth();
                int sh = bgbmp.getHeight();

                // 根据原图宽高创建新位图对象
                Bitmap newb = Bitmap.createBitmap(sw, sh, Bitmap.Config.ARGB_8888);
                // 根据新位图创建一个等大小的画布
                Canvas cv = new Canvas(newb);

                // 在画布上做新图
                cv.drawBitmap(bgbmp, 0, 0, null);
                cv.drawBitmap(videoImage, left, top, null);
                cv.save(Canvas.ALL_SAVE_FLAG);
                cv.restore();
                return newb;
            }
        return null;
    }

    /**
     * 缩放图片
     */
    private Bitmap resizeImage(Bitmap image,float actualX,float actualY) {
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        float scaleFactor = 1f;

        float scaleX = actualX / (float) imgWidth;
        float scaleY = actualY / (float) imgHeight;


        if (scaleX >= scaleY && scaleX < 1) {
            scaleFactor = scaleX;
        }

        if (scaleX < scaleY && scaleY < 1) {
            scaleFactor = scaleY;
        }

        Matrix scale = new Matrix();

        scale.postScale(scaleFactor, scaleFactor);
        final Bitmap scaledImage = Bitmap.createBitmap(image, 0, 0,
                imgWidth, imgHeight, scale, false);

        // 回收内存中的图片
        try {
            image.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scaledImage;

    }

    /**
     *
     * @param command
     */
    /**
     * 获取布局上的所有子view存入集合
     */
    private List<View> getAllChildViews(View view) {
        List<View> allchildren = new ArrayList<View>();
        getSubView(view,allchildren);
        return allchildren;
    }

    private void getSubView(View vg,List<View> list){
        if (vg instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) vg;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewchild = vp.getChildAt(i);
                if (viewchild instanceof ViewGroup){
                    getSubView(viewchild,list);
                }else {//视图 添加
                    list.add(viewchild);
                    list.addAll(getAllChildViews(viewchild));
                }
            }
        }
    }


    /**
     * upload
     *后台执行
     */
    private void uploadImage(final Activity activity, String terminalNo, String filePath, String url, boolean isDelete,boolean isNotify){
        File image = new File(filePath);
        if (!image.exists()){
            Logs.e("截图文件["+filePath+"]未找到.");
            return;
        }
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            MultipartEntity entity = new MultipartEntity();
            entity.addPart(terminalNo, new FileBody(image));
            httppost.setEntity(entity);

            HttpParams postParams = new BasicHttpParams();
            postParams.setParameter("tid", terminalNo);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            String result = httpclient.execute(httppost, responseHandler);
            if (!result.equals("1")) {
                new IllegalStateException("服务器返回值"+result);
            }
            if (isNotify) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AppUtils.Toals(activity,"上传截图完成");
                    }
                });
            }

        } catch (Exception e) {
            Logs.e("上传截图失败:"+e.getMessage());
        }finally{
           if (isDelete) image.delete();
        }
    }


    //循环截图线程
    private class TimeScreen extends Thread{
        private WeakReference<Activity> activity;
        private String capturePath;
        private String uploadUrl;
        private String terminalNo;
        private boolean isDelete ;
        private boolean isNotify ;
        private int mode ;
        private int time;
        private boolean flag;

        public TimeScreen(Activity activity, String capturePath, String uploadUrl, String terminalNo, boolean isDelete, boolean isNotify, int mode, int time) {
            this.activity = new WeakReference<Activity>(activity);
            this.capturePath = capturePath;
            this.uploadUrl = uploadUrl;
            this.terminalNo = terminalNo;
            this.isDelete = isDelete;
            this.isNotify = isNotify;
            this.mode = mode;
            this.time = time;
        }

        public void startThread(){
            flag = true;
        }
        public void stopThread(){
            flag = false;
        }
        @Override
        public void run() {
            while(flag){
                if (activity.get() == null){
                    stopThread();
                }else{
                    liunxCommadScreen(activity.get(),terminalNo,capturePath,uploadUrl,isDelete,isNotify,mode);
                    try {
                        sleep(time * 1000);
                    } catch (InterruptedException e) {
                        stopThread();
                    }
                }

            }
        }
    }


}
