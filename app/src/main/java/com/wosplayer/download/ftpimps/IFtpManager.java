package com.wosplayer.download.ftpimps;

import com.wosplayer.download.ftp.FtpUser;

/**
 * Created by user on 2017/4/17.
 */

public interface IFtpManager {
    //获取
    IFtpClien getClient(FtpUser ftpUser);
    //返回
    void backClient(IFtpClien client);
}
