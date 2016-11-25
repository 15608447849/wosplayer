package com.wosplayer.loadArea.TASKLIST;

import com.wosplayer.app.WosApplication;
import com.wosplayer.loadArea.excuteBolock.LoaderCall;

import static com.wosplayer.R.id.terminalNo;

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
    public void nitifyMsg(String filename, int type){
        WosApplication.sendMsgToServer("FTPS:"+terminalNo+";" + filename+ ";"+type);
    }

    public void notifyProgress(String filename, String process, String speed){
        String command = "PRGS:" +  terminalNo
                + "," + filename + ","
                + process + "," + speed;
        WosApplication.sendMsgToServer(command);
    }

    /**
     *
     * @param task
     * @param DownloadState
     */
    public void downloadResult(Task task,int DownloadState){
        if (DownloadState==0){ //成功
            nitifyMsg(task.getFileName(),1);
        }
        if (DownloadState==1){
            nitifyMsg(task.getFileName(),4);
        }
    }

    @Override
    public void downloadResult(String filePath, String state) {

    }


















}
