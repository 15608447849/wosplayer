package com.wosplayer.broadcast.Command.Schedule.correlation;

/**
 * Created by user on 2016/7/16.
 */
public class StringUtils {
    public static boolean isEmpty(String inputstring)
    {
        if(inputstring==null||inputstring=="")
        {
            return true;
        }else
        {
            return false;
        }
    }
    public static boolean isNotBlank(String pattern)
    {
        if(pattern==null||pattern=="")
        {
            return true;
        }else
        {
            return false;
        }
    }
}
