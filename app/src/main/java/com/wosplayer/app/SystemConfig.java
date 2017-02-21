package com.wosplayer.app;

import com.wosplayer.tool.SdCardTools;

/**
 * Created by 79306 on 2017/2/20.
 */

public class SystemConfig {
    private static final String TAG = "systemConfig";
    private static SystemConfig systemConfig = null;
    private SystemConfig() {
    }
    public static SystemConfig get(){
        if (systemConfig==null){
            systemConfig = new SystemConfig();
        }
        return systemConfig;
    }
























}
