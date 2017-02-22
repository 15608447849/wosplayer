package com.wosTools;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;



import java.util.ArrayList;

/**
 * Created by 79306 on 2017/2/21.
 */

public class SpnnerAdpter extends BaseAdapter{
        private  ArrayList<String> data_list;

        private Context context;

        public SpnnerAdpter(Context context){
            data_list = new ArrayList<String>();
            data_list.add("通用模式");
            //data_list.add("富滇银行");
            this.context = context;
        }

    public String getDataOnIndex(int position){
        return data_list.get(position);
    }
    public void setSelectIndex(int position){

    }
    @Override
    public int getCount() {
        return data_list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView==null){
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item,null);
            textView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(textView);
        }else{
            textView = (TextView) convertView.getTag();
        }
        textView.setText(data_list.get(position));
        textView.setBackgroundColor(Color.CYAN);
        return convertView;
    }
}
