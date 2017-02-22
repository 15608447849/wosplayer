package com.wosplayer.Ui.element.iviewelementImpl.actioner.ContainerItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;

import com.wosplayer.R;
import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.actioner.Container;
import com.wosplayer.Ui.performer.contentTanslater;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;

/**
 * Created by user on 2016/8/10.
 */
public class ContentContainer extends Container{



    private ImageButton left;
    private ImageButton right;
    private ImageButton back;
    private AbsoluteLayout container;
    private IPlayer content;

    private DataList dl = null;
    public DataList getInfo(){
        return dl;
    }

    public ContentContainer(Context context, DataList ls){
        this.context =context;
        this.dl = ls;



        view = LayoutInflater.from(context).inflate(R.layout.content_frame,null);

        left = (ImageButton) view.findViewById(R.id.content_left);
        right = (ImageButton) view.findViewById(R.id.content_right);
        back = (ImageButton) view.findViewById(R.id.content_return);
        container = (AbsoluteLayout)view.findViewById(R.id.content_absolute);

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //自己消亡, 在父控件中 调用向左,把自己传递进去
                leftOrRight(0);//0左 1右
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftOrRight(1);//0左 1右
            }
        });

        onBack(back);
        initContent();
    }

    /**
     *
     * @param i 0 左 1 右
     */
    private void leftOrRight(int i) {
        if(previous!=null){
            if (previous instanceof ListViewContainer){

                ((ListViewContainer)previous).leftOrRight(this,i);
            }
        }
    }

    private String key = null;
    private void initContent(){
    try{
        String filetype = dl.GetStringDefualt("filetype","");
        String filepath = dl.GetStringDefualt("filepath","");;//资源路径
        String weburl = dl.GetStringDefualt("web_url","www.baidu.com");;
        if (key==null){
            key = System.currentTimeMillis()+"#"+filetype+"#"+dl.GetStringDefualt("filename","null");
        }

        String x = "0";
        String y = "0";
        String width = String.valueOf(AbsoluteLayout.LayoutParams.MATCH_PARENT);
        String height =  String.valueOf(AbsoluteLayout.LayoutParams.MATCH_PARENT);

        DataList datalist = new DataList();
        datalist.setKey(key);

        datalist.put("x",x);
        datalist.put("y",y);
        datalist.put("width",width);
        datalist.put("height",height);
        datalist.put("localpath",filepath);//图片 视频 使用
        datalist.put("getcontents",weburl);//网页使用
        datalist.put("fileproterty",filetype);
        content = contentTanslater.tanslationAndStart(datalist,null,false, container,null);
        Logs.i(TAG," content init over");
    }catch (Exception e){
        Logs.e(TAG,"content err:"+e.getMessage());
    }

    }





    @Override
    protected float[] onSettingScale(int fwidth, int fheight) {
        return new float[0];
    }

    @Override
    protected void onSettingScale(float widthScale, float heightScale) {

    }

    @Override
    protected void onBg(ViewGroup vp) {


    }

    @Override
    protected void onUnbg(ViewGroup vp) {

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
            Logs.e(TAG,"onLayout() err:" + e.getMessage());
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
            Logs.e(TAG,"onUnlayout() err:" + e.getMessage());
        }
    }

    @Override
    public void onBind(ViewGroup vp) {
        try {
            //自己布局
            onLayout(vp);
            //调用 iplayer start
            if (content!=null){
                content.start();
            }
        }catch (Exception e){
            Logs.e(TAG,"onBind() err:" + e.getMessage());
        }
    }

    @Override
    public void onUnbind() {
        try {
            // 1. 删除视图
            onUnlayout();
            if (content!=null){
                content.stop();
            }
        }catch (Exception e){
            Logs.e(TAG,"onUnbind() err:" + e.getMessage());
        }
    }

    @Override
    protected void onClick(View v) {

    }

    @Override
    protected void onBack(View v) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Logs.e(TAG,"list layout return button clicking");

                    //前一项 显示
                    if (previous!=null){
                        previous.onBind(vp);
                        onUnbind();// 让自己消失就可以了
                    }

            }
        });
    }

    @Override
    protected void addChilds(Container child) {
    //null
    }
}
