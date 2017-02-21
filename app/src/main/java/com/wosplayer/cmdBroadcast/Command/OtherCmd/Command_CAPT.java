package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import com.wosplayer.Ui.element.iviewelementImpl.mycons_view.MyVideoView;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.app.WosApplication;
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

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/7/29.
 * catch screen
 */
public class Command_CAPT implements iCommand {
   private static String picpath = WosApplication.config.GetStringDefualt("CAPTIMAGEPATH","");//path
   private String basepath = WosApplication.config.GetStringDefualt("basepath","")+"screen0.png";;
    @Override
    public void Execute(String param) {

        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {

                Logs.i("截屏準備中..."+Thread.currentThread().getName());
                catchScreen();
                Logs.i("截屏完成..."+Thread.currentThread().getName());
            }
        });

    }



    private void catchScreen(){
       Logs.d(" catch file path :"+ basepath);

       String command = "screencap -p "+basepath;
        executeLiunx(command);


        File file = new File(basepath);
        Bitmap bgbmp = null;

        if (file.exists()){
            Logs.d("file is exitsts :"+ basepath);
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
        Logs.d("------------------");
        try{
        List<View> vList = getAllChildViews(DisplayActivity.main);
          Logs.d("sub view size :" + vList.size());
            // 视频截图
            for (View z : vList) {
                Logs.d(" & "+ z.toString());
                if (z instanceof MyVideoView) {

                    Logs.d(" video start draw ...");
                    Bitmap videoImage = null;
                    MyVideoView cvv  = (MyVideoView)z;
                    videoImage = getVideoImage(cvv);

                    if (videoImage != null) {

                        Logs.d("video point:("+z.getLeft()+","+z.getTop()+")");
                        bgbmp = composeImage(z.getLeft(), z.getTop(), bgbmp,videoImage);
                        Logs.d("bgbmp size:("+bgbmp.getWidth()+","+bgbmp.getHeight()+")");
                    }else{
                        Logs.e(" video bitmap get err = "+bgbmp);
                    }
                }
            }

        }catch (Exception e){
            Logs.e("video bitmap err:" + e.getMessage());
        }
        Logs.d("------------------ 1 -");
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
                Logs.e("截屏保存失败 :"+e.getMessage());
            }
        }
        Logs.d("------------------ 2 -");
        synchronized (this) {
            // 向服务端上传图片
            String FilePath = picpath;
            final File uploadFile  = new File(FilePath);
            final String uri = WosApplication.config.GetStringDefualt("CaptureURL", "");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean flag = uploadImage(uploadFile,uri);
                    if (flag){
                        Logs.i("catch screen success");
                    }else{
                        Logs.e("catch screen faild");
                    }
                }
            }).start();

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
            Logs.e(" *& : "+ pos);
            // 获取播放点的缩略图
            Bitmap bmp = mmr.getFrameAtTime(pos * 1000,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (bmp != null) {
                Logs.e(" *& size {"+ cvv.getViewWidth()+","+ cvv.getViewHeight()+"}");
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

            Logs.d("s bitmap:"+sw+","+sh);
            // 根据原图宽高创建新位图对象
            Bitmap newb = Bitmap.createBitmap(sw, sh, Bitmap.Config.ARGB_8888);
            // 根据新位图创建一个等大小的画布
            Canvas cv = new Canvas(newb);

            Logs.d("&&*("+videoImage.getWidth()+"&"+videoImage.getHeight()+")");
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
     *
     */
    private boolean uploadImage(File file,String url){

        if (file==null){
            Logs.e("bitmap file is null");
            return false;
        }

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            MultipartEntity entity = new MultipartEntity();

            String terminalNo = WosApplication.config.GetStringDefualt("terminalNo",
                    "");

            entity.addPart(terminalNo, new FileBody(file));
            httppost.setEntity(entity);

            HttpParams postParams = new BasicHttpParams();
            postParams.setParameter("tid", terminalNo);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            String result = httpclient.execute(httppost, responseHandler);


            if (result.equals("1\r\n")) {
                Logs.i("post screeshot bitmap success");
                return true;
            }

        } catch (Exception e) {
            Logs.e("post scrrshot bitmap error :"+e.getMessage());
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

            Logs.i("liunx command execute");
        } catch (Exception e) {
            Logs.e("执行命令失败："+e.getMessage());

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
