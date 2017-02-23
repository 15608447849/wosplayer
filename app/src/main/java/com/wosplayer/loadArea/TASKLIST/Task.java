package com.wosplayer.loadArea.TASKLIST;

import android.util.Log;

import com.wosplayer.app.Logs;
import com.wosplayer.loadArea.excuteBolock.LoaderCall;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2016/11/25.
 */

public class Task {
    public enum Type{
         HTTP,
         FTP ,
         FILE,
         NONE;
    }
   public enum  State {
            FINISHED,//完成
             NEW,//新任务
            RUNNING//运行中
        }
    //任务执行结果
    public interface TaskResult{
        void onComplete(Task task);
    }
    //ftp信息对象
    public static class FtpUser{
        private String host;
        private int port;
        private String userName;
        private String password;

        public FtpUser() {
        }

        public FtpUser(String host, int port, String userName, String password) {
            this.host = host;
            this.port = port;
            this.userName = userName;
            this.password = password;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("用户名: "+userName)
                    .append(",密码: "+password)
                    .append(",主机地址: "+host)
                    .append(",端口号: "+port);
            return sb.toString();
        }
    }



    //终端id
    private String terminalNo;
    //资源保存的本地路径
    private String localPath;
    //资源本地文件名
    private String localName;
    //远程目录路径
    private String remotePath;
    //远程文件名
    private String remoteName;
    //资源类型
    private Type type;
    //资源状态
    private State state;
    //资源完整路径
    private String url;
    //是否覆盖本地文件
    private boolean isCover;
    private FtpUser ftpUser;
    //执行结果
    private TaskResult result;

    private Task(String terminalNo) {
        this.state = Task.State.NEW;
        this.terminalNo = terminalNo;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isCover() {
        return isCover;
    }

    public void setCover(boolean cover) {
        isCover = cover;
    }

    public TaskResult getResult() {
        return result;
    }

    public void setResult(TaskResult result) {
        this.result = result;
    }

    public FtpUser getFtpUser() {
        return ftpUser;
    }

    public void setFtpUser(FtpUser ftpUser) {
        this.ftpUser = ftpUser;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("url - " + url);
        sb.append("; remote - " + remotePath+remoteName);
        sb.append("; local - " + localPath+localName);
        return sb.toString();
    }

    //获取下载类型
    private static Type getDownloadType(String url){
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

    /**
     * 任务工厂
     */
    public static class TaskFactory {
        /**
         *  //终端id
         private String terminalNo;
         //资源保存的本地路径
         private String localPath;
         //资源本地文件名
         private String localName;
         //远程目录路径
         private String remotePath;
         //远程文件名
         private String remoteName;
         //资源类型
         private Type type;
         //资源状态
         private State state;
         //资源完整路径
         private String url;
         //是否覆盖本地文件
         private boolean isCover;
         * @return
         */
        public static Task createTask(String terminalNo,String url,String remotePath,String remoteName,String localPath,String localName,boolean isCover,FtpUser ftpUser){
            Task task = new Task(terminalNo);
            task.setUrl(url);
            task.setType(getDownloadType(url));
            if (task.getType().equals(Type.FTP)){
                //ftp文件下载 - 远程路径 远程文件名 本地路径 本地文件名 是否覆盖 ftp信息
                task.setRemotePath(remotePath);
                task.setRemoteName(remoteName);

                task.setLocalPath(localPath);
                task.setLocalName(localName);

                task.setCover(isCover);
                task.setFtpUser(ftpUser);
            }
            if (task.getType().equals(Type.HTTP)){
                //远程文件url 本地文件路径 本地文件名
                task.setLocalPath(localPath);
                task.setLocalName(localName);
            }
            if (task.getType().equals(Type.FILE)){
                task.setLocalPath(localPath);
                task.setLocalName(localName);
            }
//            Log.i("创建下载任务",task.toString());
            return task;
        }
        //切割ftp字符串 -
        public static HashMap<String,String> parseFtpUrl(String uri){
            String str = uri.substring(uri.indexOf("//") + 2);
            String user = str.substring(0, str.indexOf(":"));
            String pass = str.substring(str.indexOf(":") + 1, str.indexOf("@"));
            String host = str.substring(str.indexOf("@") + 1, str.indexOf("/"));
            String remotePath = str.substring(str.indexOf("/"), str.lastIndexOf("/") + 1);
            String remoteFileName = str.substring(str.lastIndexOf("/") + 1);

            HashMap<String,String> map = new HashMap<>();
            map.put("user",user);
            map.put("pass",pass);
            map.put("host",host);
            map.put("remotePath",remotePath);
            map.put("remoteFileName",remoteFileName);
            return map;
        }

        /**
         * 创建ftp任务
         * @param terminalNo
         * @param url
         * @param localPath
         * @param localName
         * @param isCover
         * @return
         */
        public static Task createFtpTask(String terminalNo,String url,String localPath,String localName,boolean isCover){
            HashMap<String,String> map = parseFtpUrl(url);
            FtpUser user = new FtpUser(
                    map.get("host"),
                    21,
                    map.get("user"),
                    map.get("pass")
            );
            String remotePath = map.get("remotePath");

            String remoteName = map.get("remoteFileName");
            if (localName==null || localName.equals("")){
                localName = catPathLastPath(url);
            }
            return createTask(terminalNo,url,remotePath,remoteName,localPath,localName,isCover,user);
        }

        /**
         * http任务
         * @param terminalNo
         * @param url
         * @param localPath
         * @param localName
         * @param isCover
         * @return
         */
        public static Task createHttpTask(String terminalNo,String url,String localPath,String localName,boolean isCover){

            if (localName==null || localName.equals("")){
                localName = catPathLastPath(url);
            }
            return createTask(terminalNo,url,null,null,localPath,localName,isCover,null);
        }
        public static Task createFileTask(String terminalNo,String url,String localPath,String localName){
            if (localName==null || localName.equals("")){
                localName = catPathLastPath(url);
            }
            return createTask(terminalNo,url,null,null,localPath,localName,false,null);
        }

        /**
         * 截取文件路径最后面的文件名
         * @param path
         * @return
         */
        private static String catPathLastPath(String path){
                return path.substring(path.lastIndexOf("/") + 1);//文件名
        }

        public static Task createFtpTask(Task task,String otherRemoteName) {

            return createTask(task.getTerminalNo(),
                    task.getUrl(),
                    task.getRemotePath(),
                    otherRemoteName,
                    task.getLocalPath(),
                    otherRemoteName,
                    true,
                    task.getFtpUser()
            );
        }

        public static Task createMutTask(String terminalNo, String savepath, String url) {
            Type type = getDownloadType(url);
            Task task = null;
            if (type== Type.HTTP){
            task = createHttpTask(terminalNo,url,savepath,null,true);
            }
            if (type == Type.FTP){
                task = createFtpTask(terminalNo,url,savepath,null,false);//不覆盖存在的内容
            }
            if (type == Type.FILE){
                task = createFileTask(terminalNo,url,savepath,null);
            }
            return task;
        }
    }
}
