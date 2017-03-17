package com.wos.play.rootdir.model_monitor.soexcute;


/**
 * Created by user on 2017/1/18.
 */

public class RunJniHelper {
    static {
        try {
            System.loadLibrary("serverHelper"); //加载so库
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //单例
    private static RunJniHelper theInstance = null;
    private RunJniHelper() {
    }
    public static RunJniHelper getInstance() {
        if (theInstance == null)
            theInstance = new RunJniHelper();
        return theInstance;
    }
    public native void startMservice(String srvName, String acty,String path,String infos,int sleep);
    public native void stopMservice(String sdcard);
    public native void liveAll(String ctrl);
    public native void killAll(String ctrl);

}
