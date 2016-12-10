package com.wosplayer.loadArea.TASKLIST;

import com.wosplayer.app.WosApplication;
import com.wosplayer.loadArea.excuteBolock.LoaderCall;

/**
 * Created by user on 2016/11/25.
 */

public class DownloadCallImp implements LoaderCall{


    /**
     * 生成 进度
     *
     * 2 3 4
     *
     */
    public void nitifyMsg(String terminalNo,String filename, int type){
        WosApplication.sendMsgToServer("FTPS:"+terminalNo+";" + filename+ ";"+type);
    }

    public void notifyProgress(String terminalNo,String filename, String process, String speed){
        String command = "PRGS:" +  terminalNo
                + "," + filename + ","
                + process + "," + speed;
        WosApplication.sendMsgToServer(command);
    }

    // 指定类型 不匹配
    public boolean downloadResult(Task task,int DownloadState,String fileName,String filterType){
        if (fileName.endsWith(filterType)){
            return true;
        }
        downloadResult(task,DownloadState);
        return false;
    }
    /**
     *
     * @param task
     * @param DownloadState
     */
    public void downloadResult(Task task,int DownloadState){
        if (DownloadState==0){ //成功
            nitifyMsg(task.getTerminalNo(),task.getFileName(),1);
        }
        if (DownloadState==1){//失败
            nitifyMsg(task.getTerminalNo(),task.getFileName(),4);
        }

        //删除任务
        TaskQueue.getInstants().finishTask(task);
    }

    @Override
    public void downloadResult(String filePath, String state) {

    }


















}
