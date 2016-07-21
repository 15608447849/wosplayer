package com.wosplayer.broadcast.Command.Schedule.correlation;

import android.util.Log;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by user on 2016/7/16.
 */
public class JsonBinder {

    private ObjectMapper mapper;


    public JsonBinder(JsonSerialize.Inclusion inclusion) {
        mapper = new ObjectMapper();
        mapper.getSerializationConfig().setSerializationInclusion(inclusion);
        mapper.getDeserializationConfig()
                .set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
                        false);
    }




    public static JsonBinder buildNormalBinder() {
        return new JsonBinder(JsonSerialize.Inclusion.ALWAYS);
    }

    public static JsonBinder buildNonNullBinder() {
        return new JsonBinder(JsonSerialize.Inclusion.NON_NULL);
    }

    public static JsonBinder buildNonDefaultBinder() {
        return new JsonBinder(JsonSerialize.Inclusion.NON_DEFAULT);
    }

    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }

        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {

            return null;
        }
    }

    /**
     * 序列化成json格式
     *
     * @param object
     * @return
     */
    public String toJson(Object object) {

        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            Log.e("toJson", e.getMessage());
            return null;
        }
    }


    public void setDateFormat(String pattern) {
        if (StringUtils.isNotBlank(pattern)) {
            DateFormat df = new SimpleDateFormat(pattern);
            mapper.getSerializationConfig().setDateFormat(df);
            mapper.getDeserializationConfig().setDateFormat(df);
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }









}
