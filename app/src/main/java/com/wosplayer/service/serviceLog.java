package com.wosplayer.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import com.wosplayer.app.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by user on 2016/7/19.
 */
public class serviceLog extends IntentService {

    public static final String action = "com.log.logSaveService";
    public static final String backageName = "com.log.serviceLog";
    /**
     * sd卡中日志文件的最多保存天数
     */
    private static final int SDCARD_LOG_FILE_SAVE_DAYS = 1;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 日志名称格式

    /**
     * 日志文件在sdcard中的路径
     */
    public static String LOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + "wosplayer"
            + File.separator
            + "log";


    public serviceLog() {
        // 必须定义一个无参数的构造方法，并调用super(name)进行初始化，否则出错。
        super("serviceLog");
    }

    /**
     * 创建日志目录
     */
    private void createLogDir() {
        File file = null;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            file = new File(LOG_PATH_SDCARD_DIR);
            if (!file.isDirectory()) {
                file.mkdirs();
            }
        }
    }
    private static final String TAG = "logSaveServer:";
    /**
     * 删除内存下过期的日志
     */
    private void deleteSDcardExpiredLog() {
        File file = new File(LOG_PATH_SDCARD_DIR);
        if (file.isDirectory()) {
            File[] allFiles = file.listFiles();
            for (File logFile : allFiles) {
                String fileName = logFile.getName();
                if (getLogPath().equals(fileName)) { // 如果当前文件名 和 现在的文件名 一样 跳过
                    continue;
                }
                String createDateInfo = getFileNameWithoutExtension(fileName);//过来后缀得到一个文件名
                if (canDeleteSDLog(createDateInfo)) { //判断 是不是可以删除了
                    logFile.delete();
                    log.e(TAG, "delete expired log success,the log path is:"
                            + logFile.getAbsolutePath());

                }
            }
        }
    }
    /**
     * 去除文件的扩展类型（.log）
     *
     * @param fileName
     * @return
     */
    private String getFileNameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.indexOf("."));
    }
    /**
     * 判断sdcard上的日志文件是否可以删除
     *
     * @param createDateStr
     * @return
     */
    private boolean canDeleteSDLog(String createDateStr) {
        boolean canDel = false;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1 * SDCARD_LOG_FILE_SAVE_DAYS);// 删除自定义天之前日志
        java.util.Date expiredDate = calendar.getTime();
        try {
            java.util.Date createDate = sdf.parse(createDateStr);
            canDel = createDate.before(expiredDate);
        } catch (ParseException e) {
            log.e(TAG, e.getMessage(), e);
            canDel = false;
        }
        return canDel;
    }

    /**
    * 根据当前的存储位置得到日志的绝对存储路径
    *
    * @return
     * */
    public String getLogPath() {
        createLogDir();
        String logFileName = sdf.format(new Date()) + ".log";// 日志文件名称
        return LOG_PATH_SDCARD_DIR + File.separator + logFileName;
    }

    /**
     * 写入sd卡
     * @param content
     */
    private void writeLog(String content){

        File file=new File(getLogPath());
        try {
            OutputStreamWriter outSw = new OutputStreamWriter(new FileOutputStream(file,true),"UTF-8");
            outSw.write(content);
            outSw.flush();
            outSw.write("\n\r");
            outSw.flush();
            outSw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final String serviceLogKey = "LogContent";
    @Override
    public void onCreate() {
        super.onCreate();
        //判断目录是否创建
        createLogDir();
        //判断是否有过期的 log文件
        deleteSDcardExpiredLog();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String saveLogMsg = intent.getExtras().getString(serviceLogKey);
        if (saveLogMsg.equals("")){
            log.e(TAG," log msg is null ,unwrite to file");
            return;
        }

        writeLog(saveLogMsg);
    }

}
