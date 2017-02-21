package com.wosplayer.cmdBroadcast.Command.Schedule.correlation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.wosplayer.app.Logs;
import com.wosplayer.app.WosApplication;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/7/16.
 */
public class XmlNodeEntity implements Parcelable {


    private static final String TAG = XmlNodeEntity.class.getName();
    public String Level;

    public static ReentrantLock lock = new ReentrantLock();//同步锁

    public List<String> getFtplist() {
        return ftplist;
    }

    public void setFtplist(List<String> ftplist) {
        this.ftplist = ftplist;
    }

    public HashMap<String, String> getXmldata() {
        return xmldata;
    }

    public void setXmldata(HashMap<String, String> xmldata) {
        this.xmldata = xmldata;
    }

    public ArrayList<XmlNodeEntity> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<XmlNodeEntity> children) {
        this.children = children;
    }

    private ArrayList<XmlNodeEntity> children;
    private HashMap<String,String> xmldata;
    private List<String> ftplist = new ArrayList<String>(); //对应的资源下载

    private  static JsonBinder binder = JsonBinder.buildNonDefaultBinder();





    public void addUriTast(String uri){
        Logs.i(TAG,"准备添加一个uri :"+uri);
        if (uri.equals("") || uri.equals("null") || uri==null) return;

        if (ftplist.contains(uri)){
            Logs.i(TAG,"准备添加一个uri"+uri+"---- 已存在");
            return;
        }
        ftplist.add(uri);

    }





    //添加属性
    public void AddProperty(String key, String value)
    {
        try
        {
            if (xmldata == null)
            {
                xmldata = new HashMap<String,String> ();
            }
            xmldata.put(key, value);
        }
        catch (Exception e)
        {

            Logs.e(TAG, "AddProperty() :"+e.getMessage());
        }
    }

    public void AddPropertyList(HashMap<String,String> list)
    {
        if (list.size() > 0)
        {
            xmldata = list;
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
     * 所有排期节点保存
     */
    public void SettingNodeEntitySave()
    {
        this.lock.lock();
        try
        {
            xmldata=null;
            Level = null;
            String md5=binder.toJson(this);
            md5=MD5(md5);

            this.AddProperty("md5", md5);
            String savedata = binder.toJson(this);
            Logs.i(TAG,"序列化数据:"+savedata);
            writeShareDataSelf("settingNodeEntity", savedata);
        }catch(Exception e)
        {
            Logs.e(TAG ,e.getMessage());
        }finally {
            this.lock.unlock();
        }

    }

    /**
     * 节点读取
     *
     */
    private static String SettingNodeEntityRead()
    {
        lock.lock();
        String result="";
        try
        {
            result= readShareDataSelf("settingNodeEntity") ;
        }catch(Exception e)
        {
            Logs.e(TAG ,e.getMessage());
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
            if(children!=null) {
                children.clear();
            }

            if (ftplist!=null){
                ftplist.clear();
            }

        }catch(Exception e)
        {
            Logs.e(TAG, e.getMessage());
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
            Logs.e(TAG ,"e.getMessage()");

        }
        return settingnodeentity;
    }

    /**
     *获取全部排期
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
                Logs.i(TAG,"本地保存-全部的排期信息: \n 节点等级: "+ settingnodeentity.Level+"-> childs size:"+settingnodeentity.getChildren().size());

                if(settingnodeentity.xmldata!=null&&settingnodeentity.xmldata.containsKey("md5")){
                    Current_Read_md5=settingnodeentity.xmldata.get("md5");
                }

                else
                    Current_Read_md5="";
            }
        }catch(Exception e)
        {
            Logs.e(TAG,"获取 本地排期xml数据失败: " + e.getMessage());
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
            Logs.e("md5 加密失败:"+e.getMessage());
            return null;
        }
    }




    /**
     * share保存数据
     * @param key
     * @param value
     */
    public static void writeShareDataSelf(String key, String value) {
        SharedPreferences preferences = WosApplication.appContext.getSharedPreferences(
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
            SharedPreferences preferences = WosApplication.appContext
                    .getSharedPreferences(XmlNodeEntity.class.getName(),
                            Context.MODE_PRIVATE);
            result = preferences.getString(key, "");
        } catch (Exception e) {
            Logs.e(TAG,e.getMessage());
        }
        return result;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.Level);
        dest.writeList(this.children);
        dest.writeSerializable(this.xmldata);
        dest.writeStringList(this.ftplist);
    }

    public XmlNodeEntity() {
    }

    protected XmlNodeEntity(Parcel in) {
        this.Level = in.readString();
        this.children = new ArrayList<XmlNodeEntity>();
        in.readList(this.children, XmlNodeEntity.class.getClassLoader());
        this.xmldata = (HashMap<String, String>) in.readSerializable();
        this.ftplist = in.createStringArrayList();
    }

    public static final Parcelable.Creator<XmlNodeEntity> CREATOR = new Parcelable.Creator<XmlNodeEntity>() {
        @Override
        public XmlNodeEntity createFromParcel(Parcel source) {
            return new XmlNodeEntity(source);
        }

        @Override
        public XmlNodeEntity[] newArray(int size) {
            return new XmlNodeEntity[size];
        }
    };
}
