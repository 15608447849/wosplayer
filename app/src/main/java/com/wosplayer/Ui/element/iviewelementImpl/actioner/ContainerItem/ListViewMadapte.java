package com.wosplayer.Ui.element.iviewelementImpl.actioner.ContainerItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wosplayer.R;
import com.wosplayer.app.DataList;

import java.io.File;
import java.util.List;

import it.sephiroth.android.library.picasso.Picasso;

/**
 * Created by user on 2016/8/10.
 */
public class ListViewMadapte extends BaseAdapter{

    private List<ContentContainer> contentArr = null;
    private Context c;
    public ListViewMadapte(Context c,List<ContentContainer> contentArr){
        this.c = c ;
        this.contentArr = contentArr;
    }

    public List<ContentContainer> getList(){
        return contentArr;
    }

    @Override
    public int getCount() {
        return contentArr.size();
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

        mViewHolder holder = null;
        if (convertView==null){
          convertView = LayoutInflater.from(c).inflate(R.layout.action_mode_list_item,null);
            holder = new mViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.list_item_iv);
            holder.textView = (TextView) convertView.findViewById(R.id.list_item_text);
            convertView.setTag(holder);
        }else{
            holder = (mViewHolder) convertView.getTag();
        }

        DataList ls = contentArr.get(position).getInfo();

        String type = ls.GetStringDefualt("filetype","");
        String title = ls.GetStringDefualt("filename","");
        String path = "";
        if (type.equals("1006")){

        }else if (type.equals("1002")){//视频
        path = ls.GetStringDefualt("video_image_url","");
        }else if (type.equals("1007")){//图片
        path = ls.GetStringDefualt("filepath","");
        }


        holder.textView.setText(title);

        if (type.equals("1006")){

            Picasso.with(c)
                    .load(R.drawable.webicon)
//                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .fit().
                    centerCrop()
                    .placeholder(R.drawable.no_found)
                    .error(R.drawable.error)
                    .into(holder.imageView);
        }else{
            Picasso.with(c)
                    .load( new File(path))
//                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .fit().
                    centerCrop()
                    .placeholder(R.drawable.no_found)
                    .error(R.drawable.error)
                    .into(holder.imageView);
        }

        return convertView;
    }








    private class mViewHolder{
        ImageView imageView ;
        TextView textView;
    }




}
