package wosTools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.wosplayer.app.wosPlayerApp.config;

/**
 * Created by Administrator on 2016/10/19.
 */

public class DataListEntiy {
    private HashMap<String,String> map=new HashMap<String,String>();
    /**
     * 从封装的数据集合中获取需要的字段
     * @param key
     * @param defualtValue
     * @return
     */
    public String GetStringDefualt(String key,String defualtValue)
    {
        try
        {
            String object=map.get(key);
            if (object==null)
            {
                return defualtValue;
            }else
            {
                return object;
            }
        }catch(Exception e)
        {
            Log.e("GetStringDefualt", e.getMessage());
            return defualtValue;
        }
    }
    //
    public int GetIntDefualt(String key,int defualtValue)
    {
        try
        {
            Object object=map.get(key);
            if (object==null)
            {
                return defualtValue;
            }else
            {
                return Integer.parseInt(object.toString());
            }
        }catch(Exception e)
        {
            Log.e("GetStringDefualt", e.getMessage());
            return defualtValue;
        }
    }
    //
    public void put(String key,String value)
    {
        map.put(key, value);
    }
    //
    public void SaveShareData()
    {
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            ToolsUtils.writeShareData(wosPlayerApp.appContext,key.toString(), val.toString());
        }
        ToolsHandler.NotityActivty(ToolsHandler.usermessage.outtext, "保存成功！");
    }



    /**
     * 读取封装配置文档
     */
    public void ReadShareData()
    {
        map.put("connectionType",ToolsUtils.GetKey("connectionType", "socket"));
        //map.put("keyText", ToolsUtils.GetKey("keyText", "999"));
        map.put("mac",  ToolsUtils.GetMac());
        //ui配置项
        map.put("terminalNo",ToolsUtils.GetKey("terminalNo", ""));
        map.put("serverip",  ToolsUtils.GetKey("serverip", "192.168.1.178"));
        map.put("serverport",  ToolsUtils.GetKey("serverport", "8000"));
        map.put("companyid",  ToolsUtils.GetKey("companyid", "999"));
        map.put("HeartBeatInterval",  ToolsUtils.GetKey("HeartBeatInterval", "30"));
        map.put("basepath",  ToolsUtils.GetKey("basepath", "/mnt/sdcard/"));
        //获取本地ip
        String LocalIpAddress=ToolsUtils.getLocalIpAddress();
        map.put("tip",  (LocalIpAddress=="")?"127.0.0.1":LocalIpAddress);
        log.i("DataListEntiy_ReadShareData() ", "--------------------------------------------------读取配置信息------------------------------- \n 成功");
    }






}
