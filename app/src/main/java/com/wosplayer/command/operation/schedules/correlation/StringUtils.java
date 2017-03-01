package com.wosplayer.command.operation.schedules.correlation;

/**
 * Created by user on 2016/7/16.
 */
public class StringUtils {
    public static boolean isEmpty(String inputstring)
    {
        if(inputstring==null||inputstring==""|| inputstring.isEmpty())
        {
            return true;
        }else
        {
            return false;
        }
    }
}
