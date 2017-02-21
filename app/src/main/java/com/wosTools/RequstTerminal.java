package com.wosTools;

import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;

import com.wosplayer.app.AppTools;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;

import org.dom4j.Element;

/**
 * Created by Administrator on 2016/10/19.
 *
 */
public class RequstTerminal extends Thread{

    public static Object Lock = new Object();

    public static RequstTerminal requstterminal = null;

    private static DataListEntiy dataList;
    private DisplayMetrics m_dm;
    /**
     * 初始化的时候，就会生成url
     * 数据
     * 窗口尺寸
     */
    public RequstTerminal(DataListEntiy dataList, DisplayMetrics m_dm) {
        this.dataList = dataList;
        this.m_dm = m_dm;
        this.url = getTerminalIdUrl();
    }

    /**
     * 开启请求
     */
    public static void BeginRequst(DataListEntiy dataList, DisplayMetrics m_dm) {
        synchronized (Lock) {
            Logs.i("-------------------------------------------------------申请终端中-------------------------------------------------------");
            requstterminal = new RequstTerminal(dataList,m_dm);
            requstterminal.start();
        }
    }
    /**
     * 结束线程
     */
    public static void EndRequst() {
        synchronized (Lock) {
            if (requstterminal != null) {
                requstterminal.isrunning = false;
                requstterminal.interrupt();
                requstterminal = null;
            } else {
            }
        }
    }

    private String url = "http://{serverinfo}/wos/terminals/getAndroidlNoAction.action?method=getAndroidlNo&mac={mac}&corpId={companyid}&version={version}&ip={tip}&screenResolutionWidth={w}&screenResolutionHeight={h}";
    private String companyid = "";
    private boolean isrunning = true;

    // 获取url
    private String getTerminalIdUrl() {
        // 后台识别码
        companyid = dataList.GetStringDefualt("companyid", "999");
        url = url.replace("{serverinfo}",
                dataList.GetStringDefualt("serverip", "127.0.0.1") + ":"
                        + dataList.GetStringDefualt("serverport", "8000"));
        url = url.replace("{companyid}", companyid);
        url = url.replace("{version}", ToolsUtils.getAppVersionName());
        url = url.replace("{mac}",
                dataList.GetStringDefualt("mac", "0000-0000-0000-0000"));
        url = url.replace("{tip}",
                dataList.GetStringDefualt("tip", "127.0.0.1"));
        url = url.replace("{w}", Integer.toString(m_dm.widthPixels));
        url = url.replace("{h}", Integer.toString(m_dm.heightPixels));
        Logs.i("获取终端 url :\n "+url);
        return url;
    }


    public void run() {
        super.run();
        // 设置线程名字
        this.setName("RequstTerminal");
        // 发送消息个handler
        NotityActivty(DisplayActivity.HandleEvent.outtext.ordinal(), "开始申请终端ID");
        if (isrunning) {
            try {
                // 网络请求获取数据
                String xml = ToolsUtils.httpGetString(url);
                Logs.i("服务器返回信息:\n"+xml);
                // 解析获取的xml数据
                Element root = ToolsUtils.GetXmlRoot(xml);
                String terminalNo = root.elementText("terminalNo");
                terminalNo = (terminalNo == null) ? "" : terminalNo;
                String connectionType = root.elementText("connectionType");
                connectionType = (connectionType == null) ? "" : connectionType;
                String keyText = root.elementText("keyText");
                keyText = (keyText == null) ? "" : keyText;
                // 后台识别码匹配
                if (keyText.equals(companyid)) {
                    // 记录后台组识别码
                    dataList.put("terminalNo", terminalNo);
                    // 记录连接模式
                    dataList.put("connectionType", connectionType);
                    // config.dataList.put("companyid",keyText);
                    // 携带后台组识别码发送消息
                    NotityActivty(DisplayActivity.HandleEvent.success.ordinal(),terminalNo);
                    synchronized (Lock) {
                        requstterminal = null;
                    }
                    return;
                } else {
                    sleep(1000 * 6);
                }
            } catch (Exception e) {
                Logs.e("","RequstTreminal () err: "+e.getMessage());
                NotityActivty(DisplayActivity.HandleEvent.outtext.ordinal(), "申请终端ID失败");
            }
        }

    }



    private  static Handler woshandler=null;

    public static void setHandler(Handler handler){
        RequstTerminal.woshandler = handler;
    }


    /**
     * 收到一个消息 > 传递给毁掉接口的方法
     * @param what
     * @param Msg
     */
    public  static  void NotityActivty(int what, String Msg)
    {
            if(woshandler!=null)
            {
                AppTools.NotifyHandle(woshandler,what,Msg);
            }
    }

}
