package com.wosplayer.loadArea.TASKLIST;

import com.wosplayer.loadArea.excuteBolock.LoaderCall;

/**
 * Created by user on 2016/11/25.
 */

public class Task {

    interface Type{
        int HTTP = 0;
        int FTP = 1;
        int FILE = 2;
        int NONE = 4;
    }

   interface State {
            int FINISHED = 0;
            int NEW = 1;
            int RUNNING = 2;
        }
    //资源保存的路径
    private String savePath ;
    //终端id
    private String terminalNo ;
    //文件名
    private String fileName;


    private int state;
    private String url;
    private LoaderCall call;

    public Task(String savePath, String terminalNo, String url, LoaderCall call) {
        state = State.NEW;
        this.savePath = savePath;
        this.terminalNo = terminalNo;
        this.url = url;
        this.call = call;
        tanslationUrlToAbsolutePath();
    }

    public void tanslationUrlToAbsolutePath(){
        fileName = url.substring(url.lastIndexOf("/") + 1);//文件名
    }

    //获取下载类型
    public int getType(){
        if (url.startsWith("http://")){
            return Type.HTTP;
        }
        if (url.startsWith("ftp://")){
            return Type.FTP;
        }
        if (url.startsWith("file://")){
            return Type.FILE;
        }
        return Type.NONE;
    }



    public int getState() {
        return state;
    }

    public String getUrl() {
        return url;
    }

    public LoaderCall getCall() {
        return call;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCall(LoaderCall call) {
        this.call = call;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


}
