package wosTools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 2016/10/19.
 */

public class ToolsUtils {

    public static void writeShareData(Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences("wosplayer_Data", Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
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
        SharedPreferences preferences =context.getSharedPreferences("wosplayer_Data", Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
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
        String value=readShareData(wosPlayerApp.appContext,key).trim();
        return  (value!="")?value:defualtValue;
    }
    /**
     * 生成一个唯一识别码
     * @return
     */
    public static String GetMac(){
        return wosPlayerApp.GetMac();
    }
    /**
     * 生成ip
     */
    public static String getLocalIpAddress(){
        return wosPlayerApp.getLocalIpAddress();
    }

    public static String getAppVersionName() {
        String versionName = "";
        try {
            PackageManager pm = wosPlayerApp.appContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(wosPlayerApp.appContext.getPackageName(), 0);
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
            log.e("tools_utils err: http请求失败 -\n "+e.getMessage());
        }
        try {
            if(is != null)
                is.close();
            is=null;
        } catch (Exception e) {
            Log.e("CloseInputStream", e.getMessage());
        }

        log.i("tools_utils err: http请求成功 -\n "+xml);
        return xml;
    }

    public static Element GetXmlRoot(String xml)
    {
        Document doc=null;
        try {
            doc = DocumentHelper.parseText(xml);
        } catch (Exception e) {
            Log.e("GetXmlRoot", e.getMessage());
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
        Toast toast=Toast.makeText(wosPlayerApp.appContext, msg, Toast.LENGTH_SHORT);
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

}
