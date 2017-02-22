package com.wosTools;

import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;

import com.wosplayer.app.AppTools;
import com.wosplayer.app.DataList;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;

import org.dom4j.Element;

/**
 * Created by Administrator on 2016/10/19.
 *
 */
public class RequstTerminal extends Thread{



    public static RequstTerminal requstterminal = null;
    private DataList dataList=null;

    /**
     * 初始化的时候，就会生成url
     * 数据
     * 窗口尺寸
     */
    public RequstTerminal(DataList dataList) {
        this.dataList = dataList;
    }

    /**
     * 开启请求
     */
    public static void BeginRequst(DataList dataList,Handler handler) {
            EndRequst();
            Logs.i("-------------------------------------------------------申请终端中-------------------------------------------------------");
            woshandler = handler;
            requstterminal = new RequstTerminal(dataList);
            requstterminal.start();

    }
    /**
     * 结束线程
     */
    public static void EndRequst() {
            if (requstterminal != null) {
                requstterminal.isrunning = false;
                requstterminal.interrupt();
                requstterminal = null;
                woshandler = null;
            }
    }


    private boolean isrunning = true;

    // 获取url
    private String getTerminalIdUrl() {
        String url = "http://{serverinfo}/wos/terminals/getAndroidlNoAction.action?method=getAndroidlNo&mac={mac}&corpId={companyid}&version={version}&ip={tip}&screenResolutionWidth={w}&screenResolutionHeight={h}";
        url = url.replace("{serverinfo}",dataList.GetStringDefualt("serverip", "127.0.0.1") + ":"+ dataList.GetStringDefualt("serverport", "8080"));
        url = url.replace("{companyid}", dataList.GetStringDefualt("companyid", "999")); // 后台识别码
        url = url.replace("{version}", ToolsUtils.getAppVersionName());
        url = url.replace("{mac}",dataList.GetStringDefualt("mac", ""));
        url = url.replace("{tip}",dataList.GetStringDefualt("tip", ""));
        url = url.replace("{w}", dataList.GetStringDefualt("width", "1080")); //widthPixelsInteger.toString(m_dm.)
        url = url.replace("{h}", dataList.GetStringDefualt("height", "1920"));//heightPixels
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
                String xml = ToolsUtils.httpGetString(getTerminalIdUrl());
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
                if (keyText.equals(dataList.GetStringDefualt("companyid","999"))) {
                    // 记录后台组识别码
                    dataList.put("terminalNo", terminalNo);
                    // 记录连接模式
                    dataList.put("connectionType", connectionType);
                    // 携带后台组识别码发送消息
                    NotityActivty(DisplayActivity.HandleEvent.success.ordinal(),terminalNo);

                }
            } catch (Exception e) {
                Logs.e("","RequstTreminal () error : "+e.getMessage());
                NotityActivty(DisplayActivity.HandleEvent.outtext.ordinal(), "申请终端ID失败");
            }finally {
                EndRequst();
            }
        }
    }



    private  static Handler woshandler=null;
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
