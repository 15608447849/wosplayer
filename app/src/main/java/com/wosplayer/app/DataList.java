package com.wosplayer.app;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by Administrator on 2016/7/19.
 *
 */

public class DataList implements Parcelable {

    private String key;
    public void setKey(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }
    protected HashMap<String, String> map = new HashMap<String, String>();



    public String GetStringDefualt(String key, String defualtValue) {
        try {
            String object = map.get(key);
            if (object == null) {
                return defualtValue;
            } else {
                return object;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defualtValue;
        }
    }



    public int GetIntDefualt(String key, int defualtValue) {
        try {
            Object object = map.get(key);
            if (object == null) {
                return defualtValue;
            } else {
                return Integer.parseInt(object.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defualtValue;
        }
    }


    public double GetdoubleDefualt(String key, double defualtValue) {
        try {
            Object object = map.get(key);
            if (object == null) {
                return defualtValue;
            } else {
                return Double.parseDouble(object.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defualtValue;
        }
    }

    public void put(String key, String value) {
        map.put(key, value);
    }
    public void setMap(HashMap<String, String> map) {
        this.map = map;
    }
    public void clear(){
        map.clear();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeSerializable(this.map);
    }

    public DataList() {
    }

    protected DataList(Parcel in) {
        this.key = in.readString();
        this.map = (HashMap<String, String>) in.readSerializable();
    }

    public static final Parcelable.Creator<DataList> CREATOR = new Parcelable.Creator<DataList>() {
        @Override
        public DataList createFromParcel(Parcel source) {
            return new DataList(source);
        }

        @Override
        public DataList[] newArray(int size) {
            return new DataList[size];
        }
    };
}
