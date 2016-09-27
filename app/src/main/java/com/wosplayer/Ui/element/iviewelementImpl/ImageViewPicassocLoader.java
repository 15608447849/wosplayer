package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.wosplayer.R;
import com.wosplayer.app.log;

import java.io.File;

import it.sephiroth.android.library.picasso.MemoryPolicy;
import it.sephiroth.android.library.picasso.NetworkPolicy;
import it.sephiroth.android.library.picasso.Picasso;

/**
 * Created by user on 2016/9/27.
 */
public class ImageViewPicassocLoader {


    public static void loadImage(Context mContext, ImageView imageView,File tagerImageFile ,int [] sizeParam){

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

        if (sizeParam!=null &&  sizeParam.length>2){

            if (sizeParam[0] > 1 && sizeParam[1]> 1){

                Picasso
                        .with(mContext)
                        .load(tagerImageFile)
                        //.config(Bitmap.Config.RGB_565)
                        .config(Bitmap.Config.ARGB_8888)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不去内存读取,不缓存内存
                        .networkPolicy(NetworkPolicy.NO_CACHE,NetworkPolicy.NO_STORE)//跳过本地读取, 让Picasso不进行本地图片缓存,OFFLINE让Picasso加载图片的时候只从本地读取除非联网正常并且本地找不到资源的情况下
                        .resize(sizeParam[0],sizeParam[0])
                        .onlyScaleDown()
                        //.centerCrop()
                        .centerInside()
                        .noPlaceholder()
                        //.placeholder(R.drawable.loadding)
                        .error(R.drawable.error)
                        .into(imageView);

                return;
            }
    }




        Picasso
                .with(mContext)
                .load(tagerImageFile)
                .config(Bitmap.Config.ALPHA_8)
                //.config(Bitmap.Config.ARGB_8888)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //不去内存读取,不缓存内存
                //.networkPolicy(NetworkPolicy.NO_CACHE,NetworkPolicy.NO_STORE)//跳过本地读取, 让Picasso不进行本地图片缓存,OFFLINE让Picasso加载图片的时候只从本地读取除非联网正常并且本地找不到资源的情况下
                .fit()
                //.centerCrop()
                .centerInside()
                //.onlyScaleDown()
                //.placeholder(R.drawable.loadding)
                .noPlaceholder()
                .error(R.drawable.error)
                .into(imageView);

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