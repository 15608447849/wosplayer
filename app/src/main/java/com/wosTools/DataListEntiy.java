package com.wosTools;

import android.util.Log;

import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    /**
     * 保存数据到本地app的 "_Data"
     */
    public void SaveShareData()
    {
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            ToolsUtils.writeShareData(PlayApplication.appContext,key.toString(), val.toString());
        }
    }



    /**
     * 读取封装配置文档
     * true 本地
     */
    public void ReadShareData()
    {
        map.put("connectionType",ToolsUtils.GetKey("connectionType", "socket"));
        //ui配置项
        map.put("terminalNo",ToolsUtils.GetKey("terminalNo", ""));
        map.put("serverip",  ToolsUtils.GetKey("serverip", "127.0.0.1"));
        map.put("serverport",  ToolsUtils.GetKey("serverport", "8000"));
        map.put("companyid",  ToolsUtils.GetKey("companyid", "999"));
        map.put("HeartBeatInterval",  ToolsUtils.GetKey("HeartBeatInterval", "30"));
        map.put("basepath",  ToolsUtils.GetKey("basepath", "/playlist"));

        map.put("RestartBeatInterval",ToolsUtils.GetKey("RestartBeatInterval", "30"));
        map.put("storageLimits",ToolsUtils.GetKey("storageLimits","50"));//sdka 容量达到多少时 会清理资源
        //获取本地ip
        String LocalIpAddress=ToolsUtils.getLocalIpAddress();
        map.put("tip",  (LocalIpAddress=="")?"127.0.0.1":LocalIpAddress);
        //mac
        map.put("mac",  ToolsUtils.GetMac());

        Logs.i("DataListEntiy_ReadShareData() ", "--------------------------------------------------读取配置信息------------------------------- \n 成功");
    }






}
