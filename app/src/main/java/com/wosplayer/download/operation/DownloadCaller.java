package com.wosplayer.download.operation;

import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;

/**
 * Created by user on 2016/11/25.
 */

public class DownloadCaller {


    /**
     * 生成 进度
     *
     * 2 3 4
     *
     */
    public void nitifyMsg(String terminalNo,String filename, int type){
        PlayApplication.sendMsgToServer("FTPS:"+terminalNo+";" + filename+ ";"+type);
    }

    public void notifyProgress(String terminalNo,String filename, String process, String speed){
        PlayApplication.sendMsgToServer("PRGS:" +  terminalNo+ "," + filename + ","+ process + "," + speed);
    }

    /**
     *
     * @param task
     * @param state 0 success , 1 failt
     */
    public void downloadResult(Task task,int state){
        if (state == -1){
            //连接资源成功
//            nitifyMsg(task.getTerminalNo(),task.getRemoteName(),1);
            nitifyMsg(task.getTerminalNo(),task.getRemoteName(),2);
        }else{
            if (state == 0){ //成功
                nitifyMsg(task.getTerminalNo(),task.getLocalName(),3);
            }
            if (state == 1){//失败
                nitifyMsg(task.getTerminalNo(),task.getLocalName(),4);
            }
            //删除任务
            TaskQueue.getInstants().finishTask(task);
        }

    }




















}
