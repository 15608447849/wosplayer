package com.wosplayer.command.kernal;

/**
 * Created by user on 2016/6/22.
 */
/**
 * 指令
 */
public final class CommandType {

    /**
     * HRBT
     描述：终端心跳
     方向：播放端至服务端
     端口：6666
     形式：前缀+终端编号
     前缀：HRBT:
     用例：HRBT:1053
     */
    public static final String  HRBT = "HRBT:";

    /**
     * UPSC
     描述：排期更新推送
     方向：服务端至播放端
     端口：6666
     形式：前缀+排期数据URL
     前缀：UPSC:
     用例：
     UPSC:http://127.0.0.1:8000/wos/schedules/getScheduleByIdxsAction.action?method=getScheduleByIdxs&idxs=1024

     */
    public static final String  UPSC = "UPSC:";

    /**
     * ONLI
     描述：终端上线
     方向：播放端至服务端
     端口：6666
     形式：前缀+终端编号
     前缀：ONLI:
     用例：ONLI:1053
     */
    public static final String  ONLI = "ONLI:";

    /**
     * OFLI
     描述：终端下线
     方向：播放端至服务端
     端口：6666
     形式：前缀+终端编号
     前缀：OFLI:
     用例：OFLI:1053
     */
    public static final String  OFLI = "OFLI:";

    /**
     * FTPS
     描述：文件下载情况上报
     方向：播放端至服务端
     端口：6666
     形式：
     开始下载：前缀+终端编号+;+文件名+;+2
     下载完成：前缀+终端编号+;+文件名+;+3
     下载失败：前缀+终端编号+;+文件名+;+4
     前缀：FTPS:
     用例：
     FTPS:1053;1004562123.jpg;2
     FTPS:1053;1004562123.jpg;3
     FTPS:1053;1004562124.jpg;4

     */
    public static final String  FTPS = "FTPS:";

    /**
     * PRGS
     描述：文件下载进度上报(需要收到DLPS与DLPT命令才开始和结束上报)
     方向：播放端至服务端
     端口：6666
     形式：前缀+终端编号+,+文件名+,+百分比+,+下载速度kb/s
     前缀：PRGS:
     说明：百分比为0-1之间的数，下载速度请在数值后面加kb/s单位
     用例：PRGS:1053,1004562123.jpg,0.56,200kb/s
     */
    public static final String  PRGS = "PRGS:";

    /**
     * DLPS
     描述：开始上报文件进度
     方向：服务端至播放端
     端口：6666
     形式：前缀
     前缀：DLPS:
     用例：DPLS:
     */
    public static final String  DLPS = "DLPS:";

    /**
     * DLPT
     描述：结束上报文件进度
     方向：播放端至服务端
     端口：6666
     形式：前缀
     前缀：DLPT:
     用例：DLPT:
     */
    public static final String  DLPT = "DLPT:";

    /**
     * UPLG
     描述：上传日志
     方向：服务端至播放端，播放端至服务端
     端口：6666
     形式：
     服务端至播放端：前缀+服务端FTP上传路径
     播放端至服务端：
     成功：前缀+终端编号+,+空格+文件名
     失败：前缀+终端编号+,+空格+0
     前缀：UPLG:
     用例：
     UPLG:ftp://ftp:FTPmedia@127.0.0.1/uploads/logs/
     UPLG:1053, 2015-6-7.sqlite
     UPLG:1053, 0

     */
    public static final String  UPLG = "UPLG:";

    /**
     * REBO
     描述：重启终端
     方向：服务端至播放端
     端口：6666
     形式：前缀
     前缀：REBO:
     用例：REBO:
     */
    public static final String  REBO = "REBO:";

    /**
     * UIRE
     描述：重启播发器程序
     方向：服务端至播放端
     端口：6666
     形式：前缀
     前缀：UIRE:
     用例：UIRE:
     */
    public static final String  UIRE = "UIRE:";


    /**
     * VOLU
     描述：设置声音
     方向：服务端至播放端
     端口：6666
     形式：前缀+声音数值
     前缀：VOLU:
     说明：声音数值为0-100之间的整数值
     用例：VOLU:10

     */
    public static final String  VOLU = "VOLU:";


    /**
     * SHDO
     描述：设定关机时间或立即关闭终端
     方向：服务端至播放端
     端口：6666
     形式：
     立即关机：前缀
     取消设定的关机时间：前缀+false
     设定一周每天的关机时间：前缀+周日+-+时间+;+周一+-+时间+;+周二+-+时间+;+周三+-+时间+;+周四+-+时间+;+周五+-+时间+;+周六+-+时间
     前缀：SHDO:
     说明：周几为0-6的数字，时间为24小时制，13:22:34形式。
     用例：
     立即关机：SHDO:
     取消设定的关机时间：SHDO:false
     设定一周每天的关机时间：SHDO:0-18:00:00;1-19:00:00; 2-19:00:00; 3-19:00:00; 4-19:00:00; 5-19:00:00;6-18:00:00

     */
    public static final String SHDO = "SHDO:";
    /**
     * 关闭程序
     */
    public static final String  SHDP="SHDP:";

    /**
     * SCRN
     描述：开始截图
     方向：服务端至播放端
     端口：6666
     形式：前缀
     前缀：SCRN:
     用例：SCRN:

     */
    public static final String  SCRN = "SCRN:";

    /***
     *CAPT
     描述：发送截图
     方向：播放端至服务端
     端口：6668
     形式：命令长度+前缀+终端编号+JPG格式图片字节流
     前缀：CAPT:
     说明：命令长度为前缀+终端编号的字符串长度
     用例：播放端写入代码为
     Socket.writeInt(cmd.length);
     Socket.writeUTFBytes(cmd);
     Socket.writeBytes(jpgBytes);
     Socket.flush();
     */
    public static final String  CAPT = "CAPT:";
    /**
     * SYTI
     描述：时间同步
     方向：服务端至播放端
     端口：6666
     形式：前缀+日期+空格+时间
     前缀：SYTI:
     用例：SYTI:2016-01-18 15:48:29
     */
    public static final String  SYTI = "SYTI:";
    /**
     *更行apk
     */
    public static final String UPDC = "UPDC:";

    /**
     * 设置 关闭app 密码
     *
     */
    public static String PASD = "PASD:";

    /**
     * 建行xml数据 转换 本地xml数据
     * 本地默认排期
     */
    public static String TSLT = "TSLT:";
    /**
    * 富滇银行金融模块
     */
    public static String FFBK = "FFBK:";
}