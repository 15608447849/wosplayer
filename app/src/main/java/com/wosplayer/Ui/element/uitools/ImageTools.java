package com.wosplayer.Ui.element.uitools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/9/27.  /**
 * Android:scaleType=”center”
 * 保持原图的大小，显示在ImageView的中心。当原图的size大于ImageView的size，超过部分裁剪处理。
 * android:scaleType=”centerCrop”
 * 以填满整个ImageView为目的，将原图的中心对准ImageView的中心，等比例放大原图，直到填满ImageView为止（指的是ImageView的宽和高都要填满），原图超过ImageView的部分作裁剪处理。
 * android:scaleType=”centerInside”
 * 以原图完全显示为目的，将图片的内容完整居中显示，通过按比例缩小原图的size宽(高)等于或小于ImageView的宽(高)。如果原图的size本身就小于ImageView的size，则原图的size不作任何处理，居中显示在ImageView。
 * android:scaleType=”matrix”
 * 不改变原图的大小，从ImageView的左上角开始绘制原图，原图超过ImageView的部分作裁剪处理。
 * android:scaleType=”fitCenter”
 * 把原图按比例扩大或缩小到ImageView的ImageView的高度，居中显示
 * android:scaleType=”fitEnd”
 * 把原图按比例扩大(缩小)到ImageView的高度，显示在ImageView的下部分位置
 * android:scaleType=”fitStart”
 * 把原图按比例扩大(缩小)到ImageView的高度，显示在ImageView的上部分位置
 * android:scaleType=”fitXY”
 * 把原图按照指定的大小在View中显示，拉伸显示图片，不保持原比例，填满ImageView.
 */

public class ImageTools {

    private static int RATIO = 1920 * 1080;
    public static void setRatio(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        RATIO =  wm.getDefaultDisplay().getWidth() *  wm.getDefaultDisplay().getHeight();
    }
    public static Bitmap getBitmap(Context context, String imageFile, final ImageView imageView) {
        Bitmap bitmap =  ImageStore.getInstants().getBitmapCache(imageFile);//缓存取
        if (bitmap==null || bitmap.isRecycled()){
            bitmap = getBitmap(imageFile);
            if (bitmap!=null){
                ImageStore.getInstants().addBitmapCache(imageFile,bitmap);//添加到缓存
            }
        }
        if (bitmap==null) return null;
        final  Bitmap bp = bitmap;
        if (imageView != null) {
            //rxjava 线程回调到主线程
            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setImageBitmap(bp);
                }
            });
        }
        return bitmap;
    }
    /**
     *getMeasuredHeight()返回的是原始测量高度，与屏幕无关，
     * getHeight()返回的是在屏幕上显示的高度。实际上在当屏幕可以包裹内容的时候，
     * 他们的值是相等的，只有当view超出屏幕后，才能看出他们的区别。
     *
     * 当超出屏幕后，getMeasuredHeight()等于getHeight()加上屏幕之外没有显示的高度。
     */
    public static Bitmap getBitmap(String imageFile, final ImageView imageViewv) {
        return getBitmap(null,imageFile,imageViewv);
    }



    /**
     * 获取一个 bitmap 成功返回turn
     */
    private static Bitmap getBitmap(String filepath) {

        File file = new File(filepath);
        if (!file.exists()) return null;
        FileInputStream is = null;
        Bitmap bitmap = null;
        try {
                is = new FileInputStream(file);
                bitmap = createImageThumbnail(is);
                //Log.d("image utils ", "获取完毕 bitmap _success -\nfile -" + file.getAbsolutePath() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return bitmap;
    }
    //缩略图
    private static Bitmap createImageThumbnail(FileInputStream is) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;//设置压缩比例
            bitmap = BitmapFactory.decodeFileDescriptor(is.getFD(), null, opts);

            opts.inSampleSize = computeSampleSize(opts, -1,RATIO);
            opts.inJustDecodeBounds = false;

            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inDither = false; //是否开启抖动
            opts.inTempStorage = new byte[12 * 1024];

            bitmap = BitmapFactory.decodeFileDescriptor(is.getFD(),
                    null, opts);
        } catch (Exception e) {
            Log.e("", " create bitmap err:" + e.getMessage());
        }
        return bitmap;
    }

    private static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {// 最小边长 最大像素
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}