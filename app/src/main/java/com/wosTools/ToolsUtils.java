package com.wosTools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wosplayer.tool.SdCardTools;
import com.wosplayer.app.WosApplication;
import com.wosplayer.app.AppTools;
import com.wosplayer.app.Logs;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Administrator on 2016/10/19.
 */

public class ToolsUtils {

    public static void writeShareData(Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences("_Data", Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 读取share存储数据
     * @param key
     * @return
     */
    public static String readShareData(Context context,  String key) {
        SharedPreferences preferences =context.getSharedPreferences("_Data", Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        String result = preferences.getString(key, "");
        return result;
    }

    /**
     * 获取key，没有获取就使用默认的
     * @param key
     * @param defualtValue
     * @return
     */
    public static String GetKey(String key,String defualtValue)
    {
        String value= readShareData(WosApplication.appContext,key).trim();
        return  (value!="")?value:defualtValue;
    }
    /**
     * 生成一个唯一识别码
     * @return
     */
    public static String GetMac(){
        return AppTools.GetMac(WosApplication.appContext);
    }
    /**
     * 生成ip
     */
    public static String getLocalIpAddress(){
        return AppTools.getLocalIpAddress();
    }

    public static String getAppVersionName() {
        String versionName = "";
        try {
            PackageManager pm = WosApplication.appContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(WosApplication.appContext.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception: \n"+e.getMessage());
        }
        return versionName;
    }


    public static String inputStream2String(InputStream is)
    {
        if(is==null) return "";
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            while ((line = in.readLine()) != null)
            {
                buffer.append(line);
            }
        } catch (Exception e) {
            Log.e("inputStream2String", e.getMessage());
            return "";
        }
        try {
            is.close();
        } catch (Exception e) {
            Log.e("inputStream2String", e.getMessage());
        }
        return buffer.toString();
    }



    /**
     * http请求，获取数据
     * @param p_src
     * @return
     */
    public static String httpGetString(String  p_src)
    {
        String xml = "";
        InputStream is = null;
        URLConnection conn = null;
        try {
            URL url = new URL(p_src);
            conn = (URLConnection) url.openConnection();
            conn.connect();
            is = conn.getInputStream();
            xml = inputStream2String(is);
        } catch (Exception e)
        {
            Logs.e("tools_utils err: http请求失败 -\n "+e.getMessage());
        }
        try {
            if(is != null)
                is.close();
            is=null;
        } catch (Exception e) {
            Log.e("CloseInputStream", e.getMessage());
        }

        Logs.i("ToolsUtils httpGetString() : -\n " + xml);
        return xml;
    }

    public static Element GetXmlRoot(String xml)
    {
        Document doc=null;
        try {
            doc = DocumentHelper.parseText(xml);
        } catch (Exception e) {
            Log.e("UtilsTools_GetXmlRoot()", e.getMessage());
            return null;
        }
        return doc.getRootElement();
    }

    public static Element GetXmlFileRoot(String filePath)
    {
        SAXReader reader = new SAXReader();
        Element root=null;
        Document document = null;
        File file = new File(filePath);
        if (!file.exists())
            return null;
        try {
            document = reader.read(file);
            root = document.getRootElement();
        } catch (DocumentException e) {
            Log.e("GetXmlFileRoot", e.getMessage());
        }
        return root;
    }
//**********************


    public static Object invokeMethod(Object owner, String methodName, Object[] args) throws Exception {
        Class ownerClass = owner.getClass();
        Class[] argsClass = new Class[args.length];
        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }
        Method method = ownerClass.getMethod(methodName,argsClass);
        return method.invoke(owner, args);
    }
    public  static void showToast(String msg)
    {
        Toast toast=Toast.makeText(WosApplication.appContext, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static int StrToIntDef(String value,int defualtvalue)
    {
        int  result=defualtvalue;
        try
        {
            result=Integer.parseInt(value);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }


    public static ImageButton mImageButton(Context context){
        return new ImageButton(context){
            @Override
            protected void onDraw(Canvas canvas) {
                try {
                    super.onDraw(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }



    /**
     *SDCard/ xxx / xx.txt
     *
     * 读取 acesst路径下的文件 -> 保存到sd卡中
     */

    public static boolean ReadAssectsDataToSdCard(Context c,String dirPath,String fileName) {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            // 得到资源中的assets数据流
            inputStream = c.getResources().getAssets().open(fileName);
            int length = inputStream.available();
            if (length==0){
                return false;
            }
            if(SdCardTools.MkDir(dirPath)){
                //目录创建 或者 存在
                fileOutputStream = new FileOutputStream(dirPath+fileName);
                byte[] buffer = new byte[1024];
                length = 0;
                while((length = inputStream.read(buffer)) > 0){
                    fileOutputStream.write(buffer, 0 ,length);
                }
                fileOutputStream.flush();
                System.out.println("ReadAssectsDataToSdCard() ----------success--------------");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream!=null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }



    /**
     * 解压缩
     */
    @SuppressWarnings("unchecked")
    public static void UnZip(String zipFileName, String outputDirectory)
            throws IOException {

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipFileName);
            Enumeration e = zipFile.entries();
            ZipEntry zipEntry = null;
            File dest = new File(outputDirectory);
            dest.mkdirs();
            while (e.hasMoreElements()) {
                zipEntry = (ZipEntry) e.nextElement();
                String entryName = zipEntry.getName();
                InputStream in = null;
                FileOutputStream out = null;
                try {
                    if (zipEntry.isDirectory()) {
                        String name = zipEntry.getName();
                        name = name.substring(0, name.length() - 1);
                        File f = new File(outputDirectory + File.separator
                                + name);
                        f.mkdirs();
                    } else {
                        int index = entryName.lastIndexOf("\\");
                        if (index != -1) {
                            File df = new File(outputDirectory + File.separator
                                    + entryName.substring(0, index));
                            df.mkdirs();
                        }
                        index = entryName.lastIndexOf("/");
                        if (index != -1) {
                            File df = new File(outputDirectory + File.separator
                                    + entryName.substring(0, index));
                            df.mkdirs();
                        }
                        File f = new File(outputDirectory + File.separator
                                + zipEntry.getName());
                        // f.createNewFile();
                        in = zipFile.getInputStream(zipEntry);
                        out = new FileOutputStream(f);
                        int c;
                        byte[] by = new byte[1024];
                        while ((c = in.read(by)) != -1) {
                            out.write(by, 0, c);
                        }
                        out.flush();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw new IOException("解压失败：" + ex.toString());
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IOException("解压失败：" + ex.toString());
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ex) {
                }
            }
        }
    }



}
