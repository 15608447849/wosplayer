package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.MyVideoView;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.iCommand;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016/7/29.
 * catch screen
 */
public class Command_CAPT implements iCommand {
   private static String picpath = wosPlayerApp.config.GetStringDefualt("CAPTIMAGEPATH","");//path
    String basepath = wosPlayerApp.config.GetStringDefualt("basepath","")+"screen0.png";
    @Override
    public void Execute(String param) {

        log.i("截屏準備中..."+Thread.currentThread().getName());
        catchScreen();
        log.i("截屏完成..."+Thread.currentThread().getName());
    }



    private void catchScreen(){

        String command = "screencap -p "+basepath;
        executeLiunx(command);

        File file = new File(basepath);
        Bitmap bgbmp = null;

        if (file.exists()){
            file.delete();
        }

        // 设置主屏界面，作为一个控件
        View view = DisplayActivity.activityContext.getWindow().getDecorView();
        // 获去屏幕的宽高
        Display display =  DisplayActivity.activityContext.getWindowManager().getDefaultDisplay();
        view.layout(0, 0, display.getWidth(), display.getHeight());

        view.destroyDrawingCache();//or setDrawingCacheEnabled(false)
        view.setDrawingCacheEnabled(true);//提高绘图速度

        bgbmp = Bitmap.createBitmap(view.getDrawingCache());

        List<View> vList = getAllChildViews(DisplayActivity.baselayout);
            // 视频截图
            for (View z : vList) {
                if (z instanceof MyVideoView) {

                    Bitmap videoImage = null;
                    MyVideoView cvv  = (MyVideoView)z;
                    videoImage = getVideoImage(cvv);

                    if (videoImage != null) {
                        bgbmp = composeImage(z.getLeft(), z.getTop(), bgbmp,videoImage);
                    }
                }
            }

        //縮放
        bgbmp = resizeImage(bgbmp);
        synchronized (this) {
            // 保存新的位图到本地路径
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(picpath);
                if (null != fos) {
                    bgbmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                }
            } catch (Exception e) {
                log.e("截屏保存失败 :"+e.getMessage());
            }
        }
        synchronized (this) {
            // 向服务端上传图片
            String FilePath = picpath;
            File uploadFile  = new File(FilePath);
            String uri = wosPlayerApp.config.GetStringDefualt("CaptureURL", "");
            boolean flag = uploadImage(uploadFile,uri);
            if (flag){
                log.i("catch screen success");
            }else{
                log.e("catch screen faild");
            }
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
            mmr.setDataSource(cvv.getFileName());

            // 获取播放点
            long pos = cvv.getCurrentPosition();

            // 获取播放点的缩略图
            Bitmap bmp = mmr.getFrameAtTime(pos * 1000,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (bmp != null) {

                // 根据播放器大小返回图
                return Bitmap.createScaledBitmap(bmp, cvv.getViewWidth(),
                        cvv.getViewHeight(), true);
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

        float scaleX = 480f / (float) imgWidth;
        float scaleY = 360f / (float) imgHeight;

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
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewchild = vp.getChildAt(i);
                allchildren.add(viewchild);
                allchildren.addAll(getAllChildViews(viewchild));
            }
        }
        return allchildren;
    }

    /**
     * upload
     *
     */
    private boolean uploadImage(File file,String url){
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            MultipartEntity entity = new MultipartEntity();

            String terminalNo = wosPlayerApp.config.GetStringDefualt("terminalNo",
                    "");

            entity.addPart(terminalNo, new FileBody(file));
            httppost.setEntity(entity);

            HttpParams postParams = new BasicHttpParams();
            postParams.setParameter("tid", terminalNo);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            String result = httpclient.execute(httppost, responseHandler);

            if (result.equals("1\r\n")) {
                log.i("post screeshot bitmap success");
                return true;
            }

        } catch (Exception e) {
            log.e("post scrrshot bitmap error :"+e.getMessage());
            return false;
        }
        return false;
    }



    //
    public static void executeLiunx(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            //获取最高权限
			os.writeBytes("chmod 777 " + "\n");
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();

            log.i("liunx command execute");
        } catch (Exception e) {
            log.e("执行命令失败："+e.getMessage());

        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
