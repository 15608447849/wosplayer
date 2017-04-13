package com.wosplayer.download.ftp;

/**
 * Created by user on 2017/4/13.
 */ //ftp信息对象-----
public class FtpUser {
    private String host = "";
    private int port = 21;
    private String userName = "";
    private String password = "";

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
                sb.append("用户名: " + userName)
                .append(",密码: " + password)
                .append(",主机地址: " + host)
                .append(",端口号: " + port);
        return sb.toString();
    }

    //比较是不是同一个ftp的内容
    @Override
    public boolean equals(Object o) {
        FtpUser fuser = (FtpUser) o;
        if (this.host.equals(fuser.getHost()) &&
                this.port == fuser.getPort() &&
                this.userName.equals(fuser.getUserName()) &&
                this.password.equals(fuser.getPassword())) {
            return true;
        } else {
            return false;
        }
    }
}
