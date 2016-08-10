package com.wosplayer.Ui.element.iviewelementImpl.actioner.ContainerItem;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.wosplayer.R;
import com.wosplayer.Ui.element.iviewelementImpl.actioner.Container;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016/8/10.
 */
public class ListViewContainer extends Container{


    private Context context;
    private ViewGroup vp;

    private TextView title = null;
    private ListView contentListView = null;
    private Button returnBtn = null;
    private ListViewMadapte adapter;

    public ListViewContainer(Context context, DataList ls){
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.action_mode_listlayout,null);
        if (view == null){
            log.e(TAG," list create view is null");
            return;
        }

        String name = ls.GetStringDefualt("name","未命名文件夹");
        title = (TextView) view.findViewById(R.id.active_listlayout_fl_text);
        title.setText(name);

        contentListView = (ListView) view.findViewById(R.id.active_listlayout_lv);
        returnBtn = (Button) view.findViewById(R.id.active_listlayout_fl_btn);
        onBack(returnBtn);
    }


    @Override
    protected float[] onSettingScale(int fwidth, int fheight) {
        return new float[0];
    }

    @Override
    protected void onSettingScale(float widthScale, float heightScale) {
        //匹配 父容器
        //null
    }

    @Override
    protected void onBg(ViewGroup vp) {
        //null
    }

    @Override
    protected void onUnbg(ViewGroup vp) {
        //null
    }

    @Override
    protected void onLayout(ViewGroup vp) {
        this.vp = vp ;
        try {
            if (!isLayout){
                vp.addView(view);
                isLayout = true;
            }
            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) view
                    .getLayoutParams();
            lp.x = 0;
            lp.y = 0;
            lp.width = AbsoluteLayout.LayoutParams.MATCH_PARENT;
            lp.height = AbsoluteLayout.LayoutParams.MATCH_PARENT;
            view.setLayoutParams(lp);

        }catch (Exception e){
            log.e(TAG,"onLayout() err:" + e.getMessage());
        }
    }

    @Override
    protected void onUnlayout() {
        try{
            if (isLayout){
                vp.removeView(view);
                isLayout = false;
                this.vp = null;
            }
        }catch (Exception e){
            log.e(TAG,"onUnlayout() err:" + e.getMessage());
        }
    }

    @Override
    public void onBind(ViewGroup vp) {
        if (vp==null){
            log.e(TAG,"list onbind , vp is null");
            return;
        }

        try {
            // 1. 设置 view 宽高属性 添加到外层容器上
            onLayout(vp);
        }catch (Exception e){
            log.e(TAG,"onBind() err:" + e.getMessage());
        }

    }

    @Override
    public void onUnbind() {
        try {
            // 1. 删除视图
            onUnlayout();
        }catch (Exception e){
            log.e(TAG,"onUnbind() err:" + e.getMessage());
        }
    }

    @Override
    protected void onClick(View v) {
        contentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (vp!=null){
                    adapter.getList().get(position).onBind(vp);
                    //自己消失
                    onUnbind();
                }

            }
        });
    }

    @Override
    protected void onBack(View v) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"list layout return button clicking");
                if (previous!=null && previous instanceof LayoutContainer){
                    ((LayoutContainer)previous).onBind(vp);
                    onUnbind();
                }
            }
        });
    }

    @Override
    public void addChilds(Container child) {

        if (childs==null){
            childs = new ArrayList<Container>();
        }
        if (childs.contains(child)){
            log.e(TAG,"list add child is faild");
        }else{
            childs.add(child);
            log.i(TAG,"list add child is success");
        }
    }

    public void SetttingAdapter(){

        if (childs==null){
            log.e(TAG,"list childs is null");
            return;
        }
        List<ContentContainer> list = new ArrayList<ContentContainer>();
        for (Container child: childs){
            if (child instanceof  ContentContainer){
                list.add((ContentContainer) child);
            }
        }
        adapter = new ListViewMadapte(context,list);
        contentListView.setAdapter(adapter);
    }

}
