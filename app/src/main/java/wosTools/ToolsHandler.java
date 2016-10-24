package wosTools;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wosplayer.app.log;

import java.util.HashMap;

/**
 * Created by Administrator on 2016/10/19.
 */

public class ToolsHandler extends Handler {



    public  static  ToolsHandler woshandler=null;
    private Context context;
    /**
     * 发送请求消息
     * @param what
     * @param Msg
     */
    public  static  void NotityActivty(usermessage what,String Msg)
    {
        try
        {
            Message msg=new Message();
            //ordinal返回的是枚举的位置
            msg.what=what.ordinal();
            msg.obj=Msg;
            if(woshandler!=null)
            {
                woshandler.sendMessage(msg);
            }
        }catch(Exception e)
        {
            Log.e("config", "NotityActivty."+e.getMessage());
        }
    }

    HashMap<usermessage,MethodEntiy> MethodList=new HashMap <usermessage,MethodEntiy>();

    @Override
    public void dispatchMessage(Message msg) {
        super.dispatchMessage(msg);
        usermessage usg=usermessage.values()[msg.what];
        if (MethodList.containsKey(usg))
        {
            MethodEntiy mte=(MethodEntiy)MethodList.get(usg);
            try {
                mte.Params=new Object[]{msg.obj};
                ToolsUtils.invokeMethod(mte.Aowner, mte.MethodName, mte.Params);
            } catch (Exception e) {
            }
        }
    }

    private MethodEntiy PutMethod(usermessage usg,Object Aowner,String MethodName)
    {
        MethodEntiy mte=new MethodEntiy();
        mte.Aowner=Aowner;
        mte.MethodName=MethodName;
        MethodList.put(usg, mte);
        return mte;
    }

    /**
     * 构造函数，一创建对象，就把数据存入PutMethod对象，再存入map集合MethodList中
     */
    public ToolsHandler(Context context)
    {
        this.context = context;
        RegisterMethod();
    }

    private void RegisterMethod()
    {
        PutMethod(usermessage.sucess,context,"setcompanyid");
        PutMethod(usermessage.outtext,context,"outText");

        log.i("", "ToolsHander 构造方法 \n sucess="+usermessage.sucess+"\n activity="+context+" \n 方法名 setcompanyid()");
    }

    public class MethodEntiy
    {
        Object Aowner;
        String MethodName;
        Object[] Params;
    }
    public enum usermessage
    {
        sucess,
        outtext
    }



}
