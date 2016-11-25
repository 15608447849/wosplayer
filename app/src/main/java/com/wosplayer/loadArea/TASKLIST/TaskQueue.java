package com.wosplayer.loadArea.TASKLIST;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

/**
 * Created by user on 2016/11/25.
 */

public class TaskQueue extends Observable {


    private static TaskQueue instants;
   private LoaderHelper helper;

    private TaskQueue(){
        queue = new LinkedList<Task>();
    }

    //获取队列实例
    public static TaskQueue  getInstants(){
        if (instants==null){
            instants = new TaskQueue();
        }
        return instants;
    }

    public void init(){

    }

    private List<Task> queue;//队列
    // 添加一项任务
    public synchronized void addTask(Task task) {
        if (task != null) {
            queue.add(task);
        }
    }
    // 完成任务后将它从任务队列中删除
    public synchronized void finishTask(Task task) {
        if (task != null) {
            task.setState(Task.State.FINISHED);
            queue.remove(task);
        }
    }
    // 取得一项待执行任务
    public synchronized Task getTask() {
                Iterator<Task> it = queue.iterator();
                Task task;
                while (it.hasNext()) {
                    task = it.next();
                    //寻找一个新建的任务
                    if (Task.State.NEW == task.getState()) {
                        //把任务状态置为运行中
                        task.setState(Task.State.RUNNING);
                        return task;
                    }
                }
                return null;
            }


    public void excute(){
        setChanged();
        notifyObservers(getTask());  //取出任务
    }
}
