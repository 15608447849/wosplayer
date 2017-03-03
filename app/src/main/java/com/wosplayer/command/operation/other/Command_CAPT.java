package com.wosplayer.command.operation.other;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import com.wosplayer.Ui.element.definedView.MyVideoView;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;
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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/7/29.
 * catch screen
 */
public class Command_CAPT implements iCommand {
    private static final String TAG = "截全屏";
    @Override
    public void Execute(String param) {
//        String command = "screencap -p "+ basepath;
        String capturePath = PlayApplication.config.GetStringDefualt("CapturePath","");
        String uploadUrl = PlayApplication.config.GetStringDefualt("CaptureURL","");
        String terminalNo = PlayApplication.config.GetStringDefualt("terminalNo","");
        liunxCommadScreen(terminalNo,capturePath,uploadUrl);
    }


    private synchronized void liunxCommadScreen(String terminalNo, String savePath, String url) {
        Logs.d(TAG,"开始截屏 - 保存:"+terminalNo+"\n 上传地址: "+url);
        String cmd = "screencap -p "+savePath;
        ShellUtils.CommandResult result = ShellUtils.execCommand(cmd,true,true);
        if (result.result == 0){
            Logs.d(TAG,"liunx 命令(screencap -p)截屏成功");
        }else{
            catchScreen(terminalNo,savePath,url);
        }
        //上传
        uploadImage(terminalNo,savePath,url);
    }

    private void catchScreen(String terminalNo,String savePath,String url){
        try {
            if (cn.trinea.android.common.util.FileUtils.isFileExist(savePath)){
                cn.trinea.android.common.util.FileUtils.deleteFile(savePath);
            }
            Bitmap bgbmp = null;
            // 设置主屏界面，作为一个控件
            View view = DisplayActivity.activityContext.getWindow().getDecorView();
            // 获去屏幕的宽高
            Display display = DisplayActivity.activityContext.getWindowManager().getDefaultDisplay();
            view.layout(0, 0, display.getWidth(), display.getHeight());
            view.destroyDrawingCache();//or setDrawingCacheEnabled(false)
            view.setDrawingCacheEnabled(true);//提高绘图速度
            bgbmp = Bitmap.createBitmap(view.getDrawingCache());

            List<View> vList = getAllChildViews(DisplayActivity.main);
            // 视频截图
            for (View z : vList) {
                if (z instanceof MyVideoView) {
                    Bitmap videoImage = null;
                    MyVideoView cvv = (MyVideoView) z;
                    videoImage = getVideoImage(cvv);

                    if (videoImage != null) {
                        bgbmp = composeImage(z.getLeft(), z.getTop(), bgbmp, videoImage);
                    }
                }
            }
            //縮放
            bgbmp = resizeImage(bgbmp);
            // 保存新的位图到本地路径
            FileOutputStream fos = new FileOutputStream(savePath);
            if (null != fos) {
                bgbmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
  }


    /**
     * get video image
     */
    private Bitmap getVideoImage(MyVideoView cvv){
        // 获得媒体信息的类
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            // 设置媒体文件
            Logs.d(" * :"+cvv.getFileName());
            mmr.setDataSource(cvv.getFileName());

            // 获取播放点
            long pos = cvv.getCurrentPosition();

            // 获取播放点的缩略图
            Bitmap bmp = mmr.getFrameAtTime(pos * 1000,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (bmp != null) {

                // 根据播放器大小返回图
                return Bitmap.createScaledBitmap(bmp, cvv.getViewHeight(),
                        cvv.getViewWidth(), true);
            }

        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                mmr.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
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
        try {
            if (bgbmp == null)
                return null;
            if (videoImage == null)
                return null;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 缩放图片
     */
    private Bitmap resizeImage(Bitmap image) {
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        float scaleFactor = 1f;

        float scaleX = 360f / (float) imgWidth;
        float scaleY = 480f / (float) imgHeight;


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
            Logs.d("#& f: "+vp.toString());
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewchild = vp.getChildAt(i);
                Logs.d("#& :"+viewchild.toString());
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
    private void uploadImage(String terminalNo,String filePath,String url){
        File image = new File(filePath);
        try {
            if (!image.exists()){
               new IllegalStateException("截图文件["+filePath+"]未找到.");
            }
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
        } catch (Exception e) {
            Logs.e("上传截图失败:"+e.getMessage());
        }finally{
           image.delete();
        }
    }






}
