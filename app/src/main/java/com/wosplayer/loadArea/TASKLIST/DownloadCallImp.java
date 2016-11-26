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
        if (DownloadState==1){//失败
            nitifyMsg(task.getFileName(),4);
        }
        if ( task.getCall()!=null){
            task.getCall().downloadResult(null,null);
        }
        //删除任务
        TaskQueue.getInstants().finishTask(task);

    }

    @Override
    public void downloadResult(String filePath, String state) {

    }


















}
