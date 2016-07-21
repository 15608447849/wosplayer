package com.wosplayer.broadcast.Command.Schedule.correlation;

import android.content.Context;
import android.content.SharedPreferences;

import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/7/16.
 */
public class XmlNodeEntity {


    private static final String TAG = XmlNodeEntity.class.getName();
    public String Level;

    public static ReentrantLock lock = new ReentrantLock();//同步锁

    public ArrayList<XmlNodeEntity> children;
    public HashMap<String,String> xmlData;

    public List<String> ftplist = new ArrayList<String>(); //对应的资源下载
    private  static JsonBinder binder = JsonBinder.buildNonDefaultBinder();





    public void addUriTast(String uri){
        ftplist.add(uri);
    }

    //添加属性
    public void AddProperty(String key, String value)
    {
        try
        {
            if (xmlData == null)
            {
                xmlData = new HashMap<String,String> ();
            }
            xmlData.put(key, value);
        }
        catch (Exception e)
        {

            log.e(TAG, "AddProperty() :"+e.getMessage());
        }
    }

    public void AddPropertyList(HashMap<String,String> list)
    {
        if (list.size() > 0)
        {
            xmlData = list;
        }
    }


    public XmlNodeEntity NewSettingNodeEntity()
    {
        if (children == null)
        {
            children =  new ArrayList<XmlNodeEntity>();
        }
        XmlNodeEntity  result = new XmlNodeEntity();
        children.add(result);
        return result;
    }

    /**
     * 节点保存
     */
    public void SettingNodeEntitySave()
    {
        this.lock.lock();
        try
        {
            xmlData=null;
            String md5=binder.toJson(this);
            md5=MD5(md5);
            this.AddProperty("md5", md5);
            writeShareDataSelf("settingNodeEntity", binder.toJson(this));
        }catch(Exception e)
        {
            log.e(TAG ,e.getMessage());
        }finally {
            this.lock.unlock();
        }

    }

    /**
     * 节点读取
     *
     */
    public  static String SettingNodeEntityRead()
    {
        lock.lock();
        String result="";
        try
        {
            result= readShareDataSelf("settingNodeEntity") ;
        }catch(Exception e)
        {
            log.e(TAG ,e.getMessage());
        }finally {
            lock.unlock();
        }

        return result;
    }

    /**
     * 清除
     */
    public void Clear()
    {
        try
        {
            if(children!=null)
                children.clear();
        }catch(Exception e)
        {
            log.e(TAG, e.getMessage());
        }

    }

    public String area(boolean f) {
        try{
            return xmlData.get("id")+"#"+xmlData.get("uuks");
        }catch(Exception e)
        {
            return "0#0";
        }
    }
    public String area() {
        try{
            return xmlData.get("x")+"-"+xmlData.get("y")+"-"+xmlData.get("height")+"-"+xmlData.get("width");
        }catch(Exception e)
        {
            return "0-0-0-0";
        }
    }

    private  static String Current_Read_md5="";
    private  static String Current_Playing_md5="init";
    public   static boolean CheckPlayBillisPlaying()
    {
        if(Current_Read_md5.equals(Current_Playing_md5))
        {
            return true;
        }else
        {
            if(!Current_Read_md5.equals(""))
                Current_Playing_md5=Current_Read_md5;
            return false;
        }
    }

    /**
     * 获取
     * @return
     */
    public static XmlNodeEntity GetXmlNodeEntity()
    {
        XmlNodeEntity settingnodeentity=null;
        try
        {
            String SettingNodeEntityText= XmlNodeEntity.SettingNodeEntityRead();
            settingnodeentity=binder.fromJson(SettingNodeEntityText, XmlNodeEntity.class);
        }catch(Exception e)
        {
            log.e(TAG ,"e.getMessage()");

        }
        return settingnodeentity;
    }

    /**
     *获取全部
     * @return
     */
    public static ArrayList<XmlNodeEntity> GetAllNodeInfo()
    {
        XmlNodeEntity settingnodeentity=null;
        String SettingNodeEntityText= XmlNodeEntity.SettingNodeEntityRead();
        try
        {
            if(SettingNodeEntityText!="")
            {
                settingnodeentity=binder.fromJson(SettingNodeEntityText, XmlNodeEntity.class);
                if(settingnodeentity.xmlData!=null&&settingnodeentity.xmlData.containsKey("md5"))
                    Current_Read_md5=settingnodeentity.xmlData.get("md5");
                else
                    Current_Read_md5="";
            }
        }catch(Exception e)
        {
            log.e(TAG, e.getMessage());
        }
        return settingnodeentity==null?null:settingnodeentity.children;
    }

















    /**
     * MD5加密
     *
     * @param s
     * @return
     */
    public static String MD5(String s) {
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < md.length; i++) {
                int val = ((int) md[i]) & 0xff;
                if (val < 16)
                    sb.append("0");
                sb.append(Integer.toHexString(val));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }




    /**
     * share保存数据
     * @param key
     * @param value
     */
    public static void writeShareDataSelf(String key, String value) {
        SharedPreferences preferences = wosPlayerApp.appContext.getSharedPreferences(
               XmlNodeEntity.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 读取share数据
     * @param key
     * @return
     */
    public static String readShareDataSelf(String key) {
        String result = "";
        try {
            SharedPreferences preferences = wosPlayerApp.appContext
                    .getSharedPreferences(XmlNodeEntity.class.getName(),
                            Context.MODE_PRIVATE);
            result = preferences.getString(key, "");
        } catch (Exception e) {
            log.e(TAG,e.getMessage());
        }
        return result;
    }














}
