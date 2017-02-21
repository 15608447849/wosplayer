package com.wosplayer.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wosplayer.R;

/**
 * Created by user on 2016/8/9.
 */
public class imageloaderUtils {

    private static imageloaderUtils imageloaderUtils = null;
    private Context context;
    private imageloaderUtils(Context c){
        context = c;
        initImageloader();
    }
    public static imageloaderUtils getMe(Context c){
        if (imageloaderUtils==null){
            imageloaderUtils = new imageloaderUtils(c);
        }
        return  imageloaderUtils;
    }




    private void initImageloader(){
        //创建默认的ImageLoader配置参数
//        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
//                .createDefault(context);
//        ImageLoader.getInstance().init(configuration);


        //这个配置没有把图片缓存起来
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
//                .enableLogging() // Not necessary in common 1.8.6包，把这句删除
                .build();
        ImageLoader.getInstance().init(config);
    }

    private DisplayImageOptions getOption(){
        DisplayImageOptions  options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loadding)            //加载图片时的图片
                .showImageForEmptyUri(R.drawable.no_found)         //没有图片资源时的默认图片
                .showImageOnFail(R.drawable.error)              //加载失败时的图片
                .cacheInMemory(false)                               //启用内存缓存
                .cacheOnDisk(false)                                 //启用外存缓存
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(false)                          //启用EXIF和JPEG图像格式
                .displayer(new RoundedBitmapDisplayer(5))
                .build();
        return options;
    }


    public void getBitmapDisplayerBg(View view,String sdcardPath){
        String url = "file://"+sdcardPath;
        ImageLoader.getInstance().loadImage(url, new SimpleImageLoadingListener()
                {
                    public void onLoadingComplete(String imageUri, android.view.View view, android.graphics.Bitmap loadedImage) {
                        BitmapDrawable bd = new BitmapDrawable(loadedImage);
                        view.setBackgroundDrawable(bd);   //imageView，你要显示的imageview控件对象，布局文件里面//配置的
                    };
                }
        );

//还可以给它弄个监听事件SimpleImageLoadingListener，url还是图片url地址，SimpleImageLoadingListener里面有好几个方法，
// 上面这个是图片下载完成后，我们需要做什么操作。这里是，把获取的bitmap，显示在imageview上面。
        //也就是说，可以用这个方法获取一个bitmap对象
    }


}























//    public static void initImageLoader(Context context) {
//        //缓存文件的目录
//        File cacheDir = StorageUtils.getOwnCacheDirectory(context, "imageloader/Cache");
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
//                .memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽
//                .threadPoolSize(3) //线程池内加载的数量
//                .threadPriority(Thread.NORM_PRIORITY - 2)//线程优先级
//                .denyCacheImageMultipleSizesInMemory()//否认在内存中缓存图像多种尺寸
//                .diskCacheFileNameGenerator(new Md5FileNameGenerator()) //将保存的时候的URI名称用MD5 加密
//                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
//                .memoryCacheSize(2 * 1024 * 1024) // 内存缓存的最大值
//                .diskCacheSize(50 * 1024 * 1024)  // 50 Mb sd卡(本地)缓存的最大值
//                .tasksProcessingOrder(QueueProcessingType.LIFO)
//                // 由原先的discCache -> diskCache
//               // .diskCache(new UnlimitedDiscCache(cacheDir))//自定义缓存路径
//                .imageDownloader(new BaseImageDownloader(context, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
//                .writeDebugLogs() // Remove for release app
//                .build();
//        //全局初始化此配置
//        ImageLoader.getInstance().init(config);
//        //记得在AndroidManifest.xml中添加 android:name=com.xwj.imageloaderdemo.ImageLoaderApplication
//
//        // 使用DisplayImageOptions.Builder()创建DisplayImageOptions
//       /* options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.ic_stub) // 设置图片下载期间显示的图片
//                .showImageForEmptyUri(R.drawable.ic_empty) // 设置图片Uri为空或是错误的时候显示的图片
//                .showImageOnFail(R.drawable.ic_error) // 设置图片加载或解码过程中发生错误显示的图片
//                .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
//                .cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
//                .displayer(new RoundedBitmapDisplayer(20)) // 设置成圆角图片
//                .build(); // 构建完成*/
//        /*imageLoader.displayImage(imageUrls[position],
//                viewHolder.image, options);
//                */
//
//        /**
//         * public void onClearMemoryClick(View view) {
//         Toast.makeText(this, 清除内存缓存成功, Toast.LENGTH_SHORT).show();
//         ImageLoader.getInstance().clearMemoryCache();  // 清除内存缓存
//         }
//
//         public void onClearDiskClick(View view) {
//         Toast.makeText(this, 清除本地缓存成功, Toast.LENGTH_SHORT).show();
//         ImageLoader.getInstance().clearDiskCache();  // 清除本地缓存
//         }
//         */
//    }























//    /**
//     * imagerloader init
//     */
//    private void imageLoaderConfig() {
////        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);//使用默认设置
//
//          /*      File cacheDir = StorageUtils.getCacheDirectory(getApplicationContext());  //缓存文件夹路径
//                ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
//                .memoryCacheExtraOptions(480, 800) // default = device screen dimensions 内存缓存文件的最大长宽
//                .diskCacheExtraOptions(480, 800, null)  // 本地缓存的详细信息(缓存的最大长宽)，最好不要设置这个
////                .taskExecutor(...)
////                .taskExecutorForCachedImages(...)
//                .threadPoolSize(3) // default  线程池内加载的数量
//                .threadPriority(Thread.NORM_PRIORITY - 2) // default 设置当前线程的优先级
//                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
//                .denyCacheImageMultipleSizesInMemory()
//                .memoryCache(new LruMemoryCache(2 * 1024 * 1024)) //可以通过自己的内存缓存实现
//                .memoryCacheSize(2 * 1024 * 1024)  // 内存缓存的最大值
//                .memoryCacheSizePercentage(13) // default
////                .diskCache(new UnlimitedDiscCache(cacheDir)) // default 可以自定义缓存路径
//                .diskCacheSize(50 * 1024 * 1024) // 50 Mb sd卡(本地)缓存的最大值
//                .diskCacheFileCount(100)  // 可以缓存的文件数量
//                // default为使用HASHCODE对UIL进行加密命名， 还可以用MD5(new Md5FileNameGenerator())加密
//                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
//                .imageDownloader(new BaseImageDownloader(getApplicationContext())) // default
////                .imageDecoder(new BaseImageDecoder()) // default
//                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
//                .writeDebugLogs() // 打印debug log
//                .build(); //开始构建*/
//
////        ImageLoader.getInstance().init(configuration);
//    }
//
//    public void imageLoaderImageOptions(){
//        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.no_found) // 设置图片下载期间显示的图片
//                .showImageForEmptyUri(R.drawable.error) // 设置图片Uri为空或是错误的时候显示的图片
//                .showImageOnFail(R.drawable.no_found) // 设置图片加载或解码过程中发生错误显示的图片
//                .resetViewBeforeLoading(false)  // default 设置图片在加载前是否重置、复位
//                .delayBeforeLoading(1000)  // 下载前的延迟时间
//                .cacheInMemory(false) // default  设置下载的图片是否缓存在内存中
//                .cacheOnDisk(false) // default  设置下载的图片是否缓存在SD卡中
//                //        .preProcessor(...)//前置 处理器
//                //        .postProcessor(...)//发送处理器
//                //        .extraForDownloader(...)//额外的下载器
//                .considerExifParams(false) // default 考虑Exif参数 是否考虑JPEG图像EXIF参数（旋转，翻转）
//                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default 设置图片以如何的编码方式显示
//                .bitmapConfig(Bitmap.Config.ARGB_8888) // default 设置图片的解码类型
//                //      .decodingOptions(...)  // 图片的解码设置
//                .displayer(new SimpleBitmapDisplayer()) // default  还可以设置圆角图片new RoundedBitmapDisplayer(20) //.displayer(new FadeInBitmapDisplayer(100))// 图片加载好后渐入的动画时间
//                .handler(new Handler()) // default
//                .build();
//
//        /*
//         *如果DisplayImageOption没有传递给ImageLoader.displayImage(…)方法，
//         * 那么从配置默认显示选项
//            (ImageLoaderConfiguration.defaultDisplayImageOptions(…))将被使用。
//         */
//
//        /*
//                1）
//                .imageScaleType(ImageScaleType imageScaleType)  //设置图片的缩放方式
//                缩放类型mageScaleType:
//                EXACTLY :图像将完全按比例缩小的目标大小
//                EXACTLY_STRETCHED:图片会缩放到目标大小完全
//                IN_SAMPLE_INT:图像将被二次采样的整数倍
//                IN_SAMPLE_POWER_OF_2:图片将降低2倍，直到下一减少步骤，使图像更小的目标大小
//                NONE:图片不会调整
//        2）.displayer(BitmapDisplayer displayer)   //设置图片的显示方式
//            显示方式displayer：
//            RoundedBitmapDisplayer（int roundPixels）设置圆角图片
//            FakeBitmapDisplayer（）这个类什么都没做
//            FadeInBitmapDisplayer（int durationMillis）设置图片渐显的时间
//            SimpleBitmapDisplayer()正常显示一张图片
//         */
//
//            /*
//            imageUrl   图片的URL地址
//            imageView  显示图片的ImageView控件
//            options    DisplayImageOptions配置信息
//            listener   图片下载情况的监听
//            progressListener  图片下载进度的监听
//            1、  ImageLoader.getInstance().displayImage(uri, imageView);
//            2、  ImageLoader.getInstance().displayImage(uri, imageView, options);
//            3、  ImageLoader.getInstance().displayImage(uri, imageView, listener);
//            4、  ImageLoader.getInstance().displayImage(uri, imageView, options, listener);
//            5、  ImageLoader.getInstance().displayImage(uri, imageView, options, listener, progressListener);
//             */
//        /*
//                ImageLoader.getInstance().displayImage(uri, imageView, options,
//                new ImageLoadingListener() {
//
//                    @Override
//                    public void onLoadingStarted(String arg0, View arg1) {
//                        //开始加载
//                    }
//
//                    @Override
//                    public void onLoadingFailed(String arg0, View arg1,
//                            FailReason arg2) {
//                        //加载失败
//                    }
//
//                    @Override
//                    public void onLoadingComplete(String arg0, View arg1,
//                            Bitmap arg2) {
//                        //加载成功
//                    }
//
//                    @Override
//                    public void onLoadingCancelled(String arg0, View arg1) {
//                        //加载取消
//                    }
//                }, new ImageLoadingProgressListener() {
//
//                    @Override
//                    public void onProgressUpdate(String imageUri, View view,
//                            int current, int total) {
//                        //加载进度
//                    }
//                });
//         */
//        /*
//        * 只有在你需要让Image的尺寸比当前设备的尺寸大的时候，你才需要配置maxImageWidthForMemoryCach(...)和
//        maxImageHeightForMemoryCache(...)这两个参数，比如放大图片的时候。其他情况下，不需要做这些配置，因为默
//        认的配置会根据屏幕尺寸以最节约内存的方式处理Bitmap.
//
//        在设置中配置线程池的大小是非常明智的。一个大的线程池会允许多条线程同时工作，但是也会显著的影响到UI
//线程的速度。但是可以通过设置一个较低的优先级来解决：当ImageLoader在使用的时候，可以降低它的优先级，这
//样UI线程会更加流畅。在使用List的时候，UI 线程经常会不太流畅，所以在你的程序中最好设置threadPoolSize(...)和
//threadPriority(...)这两个参数来优化你的应用
//
//        memoryCache(...)和memoryCacheSize(...)这两个参数会互相覆盖，所以在ImageLoaderConfiguration中使用一个就好了
//
//        diskCacheSize(...)、diskCache(...)和diskCacheFileCount(...)这三个参数会互相覆盖，只使用一个
//注：不要使用discCacheSize(...)、discCache(...)和discCacheFileCount(...)这三个参数已经弃用
//
//
//        如果你的程序中使用displayImage（）方法时传入的参数经常是一样的，那么一个合理的解决方法是，把这些选项
//配置在ImageLoader的设置中作为默认的选项（通过调用defaultDisplayImageOptions(...)方法）。之后调用
//displayImage(...)方法的时候就不必再指定这些选项了，如果这些选项没有明确的指定给
//defaultDisplayImageOptions(...)方法，那调用的时候将会调用UIL的默认设置。
//        * */
//
//        /**
//         * 如果你经常出现oom，你可以尝试:
//         1)禁用在内存中缓存cacheInMemory(false)，
//         如果oom仍然发生那么似乎你的应用程序有内存泄漏，使用MemoryAnalyzer来检测它。否则尝试以下步骤(尝试所有或几个)
//
//         2)减少配置的线程池的大小(.threadPoolSize(...))，建议1~5
//         3)在显示选项中使用 .bitmapConfig(Bitmap.Config.RGB_565) . RGB_565模式消耗的内存比ARGB_8888模式少两倍.
//         4)配置中使用.diskCacheExtraOptions(480, 320, null)
//         5)配置中使用 .memoryCache(newWeakMemoryCache()) 或者完全禁用在内存中缓存(don't call .cacheInMemory()).
//         6)在显示选项中使用.imageScaleType(ImageScaleType.EXACTLY) 或 .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
//         7)避免使用 RoundedBitmapDisplayer.
//         调用的时候它使用ARGB-8888模式创建了一个新的Bitmap对象来显示，
//         对于内存缓存模式 (ImageLoaderConfiguration.memoryCache(...))
//         你可以使用已经实现好的方法.
//         */
//
//        /**
//         *ImageLoader是根据ImageView的height，width确定图片的宽高
//         * 一定要对ImageLoaderConfiguration进行初始化,否则会报错
//         *
//         * 启缓存后默认会缓存到外置SD卡如下地址(/sdcard/Android/data/[package_name]/cache).
//         * 如果外置SD卡不存在，会缓存到手机. 缓存到Sd卡需要在AndroidManifest.xml文件中进行如下配置
//         * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
//         *
//         * 内存缓存模式可以使用以下已实现的方法 (ImageLoaderConfiguration.memoryCache(...))
//         * 缓存只使用强引用
//         LruMemoryCache (缓存大小超过指定值时，删除最近最少使用的bitmap) --默认情况下使用
//
//         缓存使用弱引用和强引用:
//         UsingFreqLimitedMemoryCache (缓存大小超过指定值时,删除最少使的bitmap)
//         LRULimitedMemoryCache (缓存大小超过指定值时,删除最近最少使用的bitmap) --默认值
//         FIFOLimitedMemoryCache (缓存大小超过指定值时,按先进先出规则删除的bitmap)
//         LargestLimitedMemoryCache (缓存大小超过指定值时,删除最大的bitmap)
//         LimitedAgeMemoryCache (缓存对象超过定义的时间后删除)
//
//         WeakMemoryCache（没有限制缓存）缓存使用弱引用
//
//         UnlimitedDiskCache   不限制缓存大小（默认）
//         TotalSizeLimitedDiskCache (设置总缓存大小，超过时删除最久之前的缓存)
//         FileCountLimitedDiskCache (设置总缓存文件数量，当到达警戒值时，删除最久之前的缓存。如果文件的大小都一样的时候，可以使用该模式)
//         LimitedAgeDiskCache (不限制缓存大小，但是设置缓存时间，到期后删除)
//         */
//
//    }

