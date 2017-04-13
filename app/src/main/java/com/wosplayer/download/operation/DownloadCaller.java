package com.wosplayer.download.operation;

import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;

/**
 * Created by user on 2016/11/25.
 */

public class DownloadCaller {

    private static final String TAG ="下载监听";
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
    public void downloadResult(Task task,int state,String message){
        task.setDownloadCount((task.getDownloadCount()+1));
        if (state==1){ //失败
            Logs.e(TAG,message);
        }else{
            Logs.i(TAG,message);
        }
        if (state == -1){
            //连接资源成功
//          nitifyMsg(task.getTerminalNo(),task.getRemoteName(),1);
            nitifyMsg(task.getTerminalNo(),task.getRemoteName(),2);
        }else{
            if (state == 0){ //成功
                nitifyMsg(task.getTerminalNo(),task.getLocalName(),3);
            }
            if (state == 1){ //失败
                nitifyMsg(task.getTerminalNo(),task.getLocalName(),4);
            }
            //删除任务
            TaskQueue.getInstants().finishTask(task);
        }
        if (state == 1){
            task.setDownloadFailtCause(message);
            //失败任务 设置失败时间 设置已下载次数 设置失败原因 再次添加到下载队列
            //如果已下载次数>3次 添加到失败队列
            if (task.getDownloadCount()>3){
                Logs.e(TAG,"任务请添加到失败队列. 最后下载时间["+task.getDownloadFailtTime()+"] 下载失败原因:[ "+task.getDownloadFailtCause()+" ]");
            }else{
                task.setState(Task.State.NEW);
                TaskQueue.getInstants().addTask(task);
            }
        }
    }




















}
