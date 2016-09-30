package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wosplayer.R;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import it.sephiroth.android.library.picasso.MemoryPolicy;
import it.sephiroth.android.library.picasso.NetworkPolicy;
import it.sephiroth.android.library.picasso.Picasso;

/**
 * Created by user on 2016/9/27.
 */
public class ImageViewPicassocLoader {


    public static final int TYPE_IIMAGE = 0001;
    public static final int TYPE_ACTION_IIMAGE = 0000;

    //glide Debug
    static RequestListener<String, GlideDrawable> requestListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            // todo log exception

            log.e("Glide","---------------------------");
            log.e("Glide",e.getMessage()+","+e.getCause()+" \n model:"+model );
            log.e("Glide","---------------------------");
            // important to return false so the error placeholder can be placed
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };



    public static void loadImage(Context mContext, ImageView imageView,File tagerImageFile ,int [] sizeParam,int type){

        log.d("load image",tagerImageFile.getAbsolutePath()+ " \n length:["+( sizeParam==null?"null":sizeParam.length )+"]");
        if (tagerImageFile.exists()){
            log.d("文件存在");
        }else{
            log.e("文件不存在");
        }

        getBitmap(mContext,tagerImageFile,imageView);


        if (true){
            return;
        }



        /**
         * Android:scaleType=”center”
         保持原图的大小，显示在ImageView的中心。当原图的size大于ImageView的size，超过部分裁剪处理。
         android:scaleType=”centerCrop”
         以填满整个ImageView为目的，将原图的中心对准ImageView的中心，等比例放大原图，直到填满ImageView为止（指的是ImageView的宽和高都要填满），原图超过ImageView的部分作裁剪处理。
         android:scaleType=”centerInside”
         以原图完全显示为目的，将图片的内容完整居中显示，通过按比例缩小原图的size宽(高)等于或小于ImageView的宽(高)。如果原图的size本身就小于ImageView的size，则原图的size不作任何处理，居中显示在ImageView。
         android:scaleType=”matrix”
         不改变原图的大小，从ImageView的左上角开始绘制原图，原图超过ImageView的部分作裁剪处理。
         android:scaleType=”fitCenter”
         把原图按比例扩大或缩小到ImageView的ImageView的高度，居中显示
         android:scaleType=”fitEnd”
         把原图按比例扩大(缩小)到ImageView的高度，显示在ImageView的下部分位置
         android:scaleType=”fitStart”
         把原图按比例扩大(缩小)到ImageView的高度，显示在ImageView的上部分位置
         android:scaleType=”fitXY”
         把原图按照指定的大小在View中显示，拉伸显示图片，不保持原比例，填满ImageView.
         */
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (!DisplayActivity.isShowDialog){

            Picasso
                    .with(mContext)
                    .setIndicatorsEnabled(true);
            //蓝色 - 从内存中获取,是最佳性能展示
            //绿色 - 从本地获取,性能一般
            //红色 - 从网络加载,性能最差

            Picasso .with(mContext)
                    .setLoggingEnabled(true);//通过输出日志的方式查看每张网络请求的资源所用的时间

      /*  StatsSnapshot picassoStats = Picasso.with(mContext).getSnapshot();
        //然后打印
        log.d("Picasso Stats :", picassoStats.toString());*/


            if (type == TYPE_IIMAGE) {
                if (sizeParam != null && sizeParam.length > 0) {
                    log.d("picasso _" ,"w " + sizeParam[0] + "\n h " +sizeParam[1]);
                    if (sizeParam[0] > 1 && sizeParam[1] > 1) {
                        log.d("picasso "," tytp - 0 ");
                        Picasso
                                .with(mContext)
                                .load(tagerImageFile)
                                //.config(Bitmap.Config.RGB_565)
                                .config(Bitmap.Config.ARGB_8888)
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不去内存读取,不缓存内存
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)//跳过本地读取, 让Picasso不进行本地图片缓存,OFFLINE让Picasso加载图片的时候只从本地读取除非联网正常并且本地找不到资源的情况下
                                .resize(sizeParam[0], sizeParam[1])
                                .onlyScaleDown()
                                //.centerCrop()
                                //.centerInside()
                                .noPlaceholder()
                                //.placeholder(R.drawable.loadding)
                                .error(R.drawable.error)
                                .into(imageView);
                        return;
                    }
                }
                log.d("picasso "," tytp - 1 ");
                Picasso
                        .with(mContext)
                        .load(tagerImageFile)
                        //.config(Bitmap.Config.ALPHA_8)
                        .config(Bitmap.Config.RGB_565)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不去内存读取,不缓存内存
                        .networkPolicy(NetworkPolicy.NO_CACHE,NetworkPolicy.NO_STORE)//跳过本地读取, 让Picasso不进行本地图片缓存,OFFLINE让Picasso加载图片的时候只从本地读取除非联网正常并且本地找不到资源的情况下
                        .fit()
                        //.centerCrop()
                        //.centerInside()
                        //.onlyScaleDown()
                        //.placeholder(R.drawable.loadding)
                        .noPlaceholder()
                        .error(R.drawable.error)
                        .into(imageView);
            }
        }else{
            log.d("glide ", " -- -- --- -- -- -- -- -- -- glide loading image select");

            if (sizeParam != null && sizeParam.length > 0) {
                log.d("glide _" ,"w " + sizeParam[0] + "\n h " +sizeParam[1]);
                if (sizeParam[0] > 1 && sizeParam[1] > 1) {
                    log.d("glide ", " -- -- --- -- -- -- -- -- -- glide loading image  1");
                    Glide.with(mContext)
                            .load(tagerImageFile)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE )
                            //.diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(sizeParam[0],sizeParam[1])
                            .centerCrop()
                            //.fitCenter()
                            //.override(600, 300)
                            .crossFade()
                            //.placeholder(R.drawable.no_found)
                            .error(R.drawable.error)
                            .into(imageView);

                    return;
                }
            }
            log.d("glide ", " -- -- --- -- -- -- -- -- -- glide loading image  2");
            Glide.with(mContext)
                    .load(tagerImageFile)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE )
                    //.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    //.fitCenter()
                    //.override(600, 300)
                    //.crossFade()
                    //.placeholder(R.drawable.no_found)
                    .error(R.drawable.error)
                    .into(imageView);
        }
/************************************************************************/
        if (type == TYPE_ACTION_IIMAGE){
            log.d("picasso "," tytp - 2 ");
            if (DisplayActivity.isShowDialog){
                log.d("picasso ","type -------Picasso loadimage ---");
                Picasso
                        .with(mContext)
                        .load(tagerImageFile)
                        .config(Bitmap.Config.ALPHA_8)
                        //.config(Bitmap.Config.ARGB_8888)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不去内存读取,不缓存内存
                        .networkPolicy(NetworkPolicy.NO_CACHE,NetworkPolicy.NO_STORE)//跳过本地读取, 让Picasso不进行本地图片缓存,OFFLINE让Picasso加载图片的时候只从本地读取除非联网正常并且本地找不到资源的情况下
                        .fit()
                        //.centerCrop()
                        //.centerInside()
                        //.onlyScaleDown()
                        //.placeholder(R.drawable.loadding)
                        .noPlaceholder()
                        .error(R.drawable.error)
                        .into(imageView);
            }else{
                log.d("glide ","type -------glide loadimage ---");
                Glide.with(mContext)
                        .load(tagerImageFile)
                        .skipMemoryCache(true)
                        .diskCacheStrategy( DiskCacheStrategy.NONE)
                        //.diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        //.fitCenter()
                        //.override(600, 300)
                        //.crossFade()
                        .dontAnimate()
//                      .placeholder(R.drawable.no_found)
                        .error(R.drawable.error)
                        .into(imageView);
            }

        }

    }


    public static Bitmap loadImage (Context mContext,File tagerImageFile , int [] sizeParam){
        Bitmap bitmap = null;
        try {
//
            bitmap = Picasso.with(mContext)
                    .load(tagerImageFile)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .config(Bitmap.Config.RGB_565)
                    .resize(sizeParam[0], sizeParam[1])
                    .onlyScaleDown()
                    .get();
        }catch (Exception e){
          log.e(" picasso get bitmap err :"+e.getMessage());
        }
        return bitmap;

    }


    //清理缓存
    public static void clear(Context context,File file){
        Picasso.with(context).invalidate(file);
    }


    public static void getBitmap(Context context,File file,ImageView iv) {
        FileInputStream is = null;
        Bitmap bitmap = null;
       try{
           if (file != null && file.exists()) {


               /*is = new FileInputStream(file);
               log.e("###############");
               bitmap =  BitmapFactory.decodeStream(new FlushedInputStream(is));//BitmapFactory.decodeStream(is);
              // bitmap = TanslationBitmapStream(is);
               log.e("-------------");
               iv.setScaleType(ImageView.ScaleType.FIT_XY);
               iv.setImageBitmap(bitmap);
               log.e("###############***");
               bitmap=null;*/

               log.e("-------------");

             /*  BitmapFactory.Options options = getBitmapOption();
               is = new FileInputStream(file);

               bitmap = BitmapFactory.decodeFileDescriptor(is.getFD(),
                       null, options);
               iv.setScaleType(ImageView.ScaleType.FIT_XY);
               iv.setImageBitmap(bitmap);
               bitmap=null;*/

               is = new FileInputStream(file);
               bitmap = createImageThumbnail(is);
               iv.setScaleType(ImageView.ScaleType.FIT_XY);
               iv.setImageBitmap(bitmap);
               bitmap = null;
               log.e("###############***");

           }
       }catch (Exception e){
           log.e("loading image err: "+e.getMessage());
       }finally {
           if (is != null) {
               try {
                   is.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }
    }

    public static BitmapFactory.Options getBitmapOption() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = 10;
        options.outHeight = 10;
        options.inSampleSize = 2;// 特别注意，这个值越大，相片质量越差，图像越小
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;
        options.inTempStorage = new byte[12 * 1024];
        return options;
    }

    //创建静态类FlushedInputStream
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }


public static Bitmap TanslationBitmapStream(InputStream is){
    BufferedInputStream bis = null;
    ByteArrayOutputStream out =null;
    try{
        bis = new BufferedInputStream(is,1024 * 8);
        bis = new BufferedInputStream(is,1024 * 8);
        out = new ByteArrayOutputStream();


        int len=0;
        byte[] buffer = new byte[1024];
        while((len = bis.read(buffer)) != -1){
            out.write(buffer, 0, len);
        }
        out.close();
        bis.close();

    }catch (MalformedURLException e1) {
        e1.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
    byte[] data = out.toByteArray();
    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
    return bitmap;
    }





//

    public static Bitmap createImageThumbnail(FileInputStream is){
        Bitmap bitmap = null;
        try {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeFileDescriptor(is.getFD(),
                null, opts);

        opts.inSampleSize = computeSampleSize(opts, -1, 1920*1080);
        opts.inJustDecodeBounds = false;

            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inDither = false;
            opts.inTempStorage = new byte[12 * 1024];

            bitmap =  BitmapFactory.decodeFileDescriptor(is.getFD(),
                    null, opts);
        }catch (Exception e) {
            // TODO: handle exception
            log.e(" create bitmap err:"+e.getMessage());
        }
        return bitmap;
    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {// 最小边长 最大像素
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

    private static int computeInitialSampleSize(BitmapFactory.Options options,int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :(int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
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

//纯用picasso 加载本地图片
    /*    Picasso.with(mCcontext)
                .load(new File(filePath))
//                .resize(w-1,h-1)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
               // .onlyScaleDown()
                .resize(this.getMeasuredWidth(), this.getMeasuredHeight())
                .resize(w,h)
                .placeholder(R.drawable.loadding)
                .error(R.drawable.error)
                .into(this);*/
// log.e(TAG," loader image --------------------------------- end 2");
/**.memoryPolicy(NO_CACHE, NO_STORE)
 * 其中memoryPolicy的NO_CACHE是指图片加载时放弃在内存缓存中查找，NO_STORE是指图片加载完不缓存在内存中。
 *        .transform(new Transformation(){

@Override
public Bitmap transform(Bitmap source) {
int size = Math.min(source.getWidth(), source.getHeight());
int x = (source.getWidth() - size) / 2;
int y = (source.getHeight() - size) / 2;
Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
if (result != source) {
source.recycle();
}
return result;
}

@Override
public String key() {
return "square()";
}
})
 */






//        Picasso
//                .with(mCcontext)
//                .setIndicatorsEnabled(true);
//        //蓝色 - 从内存中获取,是最佳性能展示
//        //绿色 - 从本地获取,性能一般
//        //红色 - 从网络加载,性能最差
//
//        Picasso .with(mCcontext)
//                 .setLoggingEnabled(true);//通过输出日志的方式查看每张网络请求的资源所用的时间
//
//
//
//            Picasso
//                    .with(mCcontext)
//                    .load(new File(filePath))
//                    //.config(Bitmap.Config.RGB_565)
//                    .config(Bitmap.Config.ARGB_8888)
//                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不去内存读取,不缓存内存
//                    .networkPolicy(NetworkPolicy.NO_CACHE,NetworkPolicy.NO_STORE)//跳过本地读取, 让Picasso不进行本地图片缓存,OFFLINE让Picasso加载图片的时候只从本地读取除非联网正常并且本地找不到资源的情况下
//                    .fit()
//                    //.centerCrop()
//                    .centerInside()
//                    //.onlyScaleDown()
//                    //.placeholder(R.drawable.loadding)
//                    .error(R.drawable.error)
//                    .into(this)
//            ;
//
//        StatsSnapshot picassoStats = Picasso.with(mCcontext).getSnapshot();
//        //然后打印
//        log.d(TAG,"Picasso Stats :"+ picassoStats.toString());