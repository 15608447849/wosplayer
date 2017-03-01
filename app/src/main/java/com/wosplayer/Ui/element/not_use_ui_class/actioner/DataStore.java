package com.wosplayer.Ui.element.not_use_ui_class.actioner;

import com.wosplayer.app.DataList;

import java.util.ArrayList;

/**
 * Created by user on 2016/8/3.
 */
public class DataStore {
    private DataList data;
    private ArrayList<DataStore> children;

    public DataStore() {
        this.data = new DataList();
        this.children = new ArrayList<DataStore>();
    }

    public DataList getData() {
        return data;
    }

    public ArrayList<DataStore> getStores() {
        return children;
    }



    public DataStore NewSettingNodeEntity()
    {
        if (children == null)
        {
            children =  new ArrayList<DataStore>();
        }
        DataStore  item = new DataStore();
        children.add(item);
        return item;
    }


}
