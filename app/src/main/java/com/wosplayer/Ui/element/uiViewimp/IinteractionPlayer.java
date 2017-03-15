package com.wosplayer.Ui.element.uiViewimp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.wosplayer.R;
import com.wosplayer.Ui.element.interactive.databeads.Abutton;
import com.wosplayer.Ui.element.interactive.databeads.Acontent;
import com.wosplayer.Ui.element.interactive.databeads.Afile;
import com.wosplayer.Ui.element.interactive.databeads.Alayout;
import com.wosplayer.Ui.element.interactive.uibeans.AFileShow;
import com.wosplayer.Ui.element.interactive.uibeans.AImageButton;
import com.wosplayer.Ui.element.interfaces.IPlayer;
import com.wosplayer.Ui.element.interactive.iCache.InteractionCache;
import com.wosplayer.Ui.element.interactive.xml.XmlParse;
import com.wosplayer.Ui.element.interfaces.TimeCalls;
import com.wosplayer.Ui.element.uitools.ImageAttabuteAnimation;
import com.wosplayer.Ui.element.uitools.ImageViewPicassocLoader;
import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.app.AppTools;
import com.wosplayer.app.BackRunner;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;
import com.wosplayer.command.operation.schedules.correlation.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cn.trinea.android.common.util.FileUtils;

public class IinteractionPlayer extends AbsoluteLayout implements IPlayer,View.OnClickListener{

    private static final java.lang.String TAG ="互动底层" ;
    private Context context;
    private ViewGroup superView = null;
    private boolean isLayout = false;
    private FrameLayout back;
    public IinteractionPlayer(Context context,ViewGroup superView) {
        super(context);
        this.context =context;
        this.superView = superView;

    }
    @Override
    public void loadData(DataList mp, Object ob) {
        try {
            int w = mp.GetIntDefualt("width", 0);
            int h = mp.GetIntDefualt("height", 0);
            int x = mp.GetIntDefualt("x", 0);
            int y = mp.GetIntDefualt("y", 0);
            Logs.d(TAG,"设置布局大小: x="+x+";y="+y+";w="+w+";h="+h);
            this.setLayoutParams(new AbsoluteLayout.LayoutParams(w,h,x,y));

            if (back==null){
                createBack(x,y,w,h);
            }
            final String mUri = mp.GetStringDefualt("getcontents", "");//互动xml的布局文件
            final String name = mp.GetStringDefualt("contentsname","null");//内容名字
            //后台生成数据
            if(!StringUtils.isEmpty(mUri)){
                BackRunner.runBackground(new Runnable() {
                    @Override
                    public void run() {
                        genarateViewData(mUri,name);
                    }
                });
            }
        }catch (Exception e){
            Logs.e(TAG, "loaddata() " + e.getMessage());
        }
    }

    private void createBack(int x,int y,int w,int h) {
        back = new FrameLayout(context);
        back.setLayoutParams(new AbsoluteLayout.LayoutParams(50,50,0,h-50));
        back.setBackgroundResource(R.drawable.pre_image);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //返回上一级
                if (isLayout && isExcuteing){
                    Logs.e(TAG," 返回上一层  - task "+task.size());
                    if (orderKey.size()>1 && task.size()>1){
                        String key = task.get( (task.size()-2) );
                        if (key!=null && !key.equals("")){
                            excuteLayout(layoutMap.get(key));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void setTimerCall(TimeCalls timer) {
    }

    @Override
    public void start() {
        try{
            if (!isLayout){
                superView.addView(this);
                isLayout = true;
            }
            //开始互动模块
            startAction();
        }catch (Exception e){
            Logs.e(TAG,"开始:"+e.getMessage());
        }
    }



    @Override
    public void stop() {
        try {
            stopAction();

            if (isLayout){
                //移除视图
                superView.removeView(this);
                isLayout = false;
            }
            // 移除 互动模块
            Logs.d(TAG,"清理视图 结束");
        }catch (Exception e){
            Logs.e(TAG,"停止:"+e.getMessage());
        }
    }


    //存放所有视图数据
    private HashMap<String,Alayout> layoutMap = new HashMap<>();
    private HashMap<String,Afile> fileMap = new HashMap<>();
    private ArrayList<String> orderKey = new ArrayList<>();
    private ArrayList<String> task = new ArrayList<>();
    private boolean parseSuccess = false;

    private void genarateViewData(String mUri, String name) {
        arrClear();
        //解析数据
        paramXml(getXml(name,mUri));
        parseSuccess = true;
        if (!isExcuteing && isLayout){
            UiExcuter.getInstancs().runingMain(new Runnable() {
                @Override
                public void run() {
                    startAction();
                }
            });
        }
    }


    private String getXml(String tag, String mUri) {
       Logs.e(TAG,"标识: "+tag+" ;地址: "+mUri);
        //从网络获取数据
        String xml = AppTools.uriTranslationXml(mUri);
        if (xml == null) {
            //从缓存获取数据
            xml = InteractionCache.pull(tag+mUri);
            if (xml == null) return null;
        }else{
            //存入缓存
            InteractionCache.push(tag+mUri,xml);
        }
        return xml;
    }

    private void paramXml(String xml) {
        //第一个一定是布局文件
        if (xml == null) return;
        try {
            //获取一个 layout 数据
            Alayout aLayout = XmlParse.interactionParse_one(xml);
            orderKey.add(aLayout.tag);
            layoutMap.put(aLayout.tag,aLayout);
            //修改button 坐标属性
            float xScale = (float) this.getLayoutParams().width / (float) aLayout.totalwidth;
            float yScale = (float) this.getLayoutParams().height / (float) aLayout.totalheight;
            for (Abutton btn : aLayout.buttonList){
                btn.preTag = aLayout.tag;
                //循环 主页下面的按钮
                btn.setScale(xScale,yScale);
                if (btn.isNextFileType()){
                  paramFileXml(btn,getXml(btn.tag,aLayout.folderurl+btn.nextTag));
                }else{
                    //布局解析
                    paramXml(getXml(btn.tag,aLayout.layoutUri+btn.nextTag));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void paramFileXml(Abutton btn,String _xml) throws Exception {
        if (_xml==null || _xml.equals("")) return;
        //文件解析
        Afile afile = XmlParse.interactionParse_Button_Item_View_FildType(_xml);
        afile.perTag = btn.preTag;
        fileMap.put(afile.tag,afile);
        btn.image = UiExcuter.getInstancs().basepath + afile.thumbnailpath;
        for (Acontent content : afile.contentList){
            if (content.type.equals("1007") || content.type.equals("1002")){
                content.sourcePath = UiExcuter.getInstancs().basepath+AppTools.subLastString(content.sourcePath,"/");
            }
            //Logs.e(TAG,content.type+"; " +content.sourcePath+ "; "+ content.web_url);
        }
    }

    private boolean isExcuteing = false;
    private void startAction() {
        if (parseSuccess){
            isExcuteing = true;
            Alayout alayout = layoutMap.get(orderKey.get(0)); //从主页开始
            excuteLayout(alayout);
        };
    }
    //执行一个布局对象
    private void excuteLayout(Alayout alayout) {
        //清空所有
        this.removeAllViews();
        addOnTasks(alayout);
        //设置背景
        settingBg(alayout);
        //添加按钮
        addButtonOnView(alayout.buttonList);
        if (!alayout.tag.equals(orderKey.get(0))){
            //添加返回按钮
            this.addView(back);
        }
    }

    private synchronized void addOnTasks(Alayout alayout) {
        if (task.contains(alayout.tag)){
                task.remove(task.size()-1);
        }else{
            task.add(alayout.tag);
        }
    }

    //循环添加按钮到视图
    private void addButtonOnView(ArrayList<Abutton> buttonList) {
        for (Abutton button : buttonList){
//            获取一个按钮
            AImageButton btnview = new AImageButton(context);
            btnview.setTag(button);
            btnview.setLayoutParams(new AbsoluteLayout.LayoutParams(button.width,button.height,button.x,button.y));
            btnview.setOnClickListener(this);
            if(FileUtils.isFileExist(button.image)){
                ImageViewPicassocLoader.getBitmap(button.image,btnview);
            }else{
                ImageViewPicassocLoader.getBitmap(UiExcuter.getInstancs().defImagePath,btnview);
            }
            this.addView(btnview);
            ImageAttabuteAnimation.SttingAnimation(null,btnview,null);//new int[]{button.width,button.height,button.x,button.y}
        }
    }

    private void stopAction() {
            //移出自己的全部子视图
            this.removeAllViews();
            arrClear();
            System.gc();
    }

    private void arrClear() {
        layoutMap.clear();
        fileMap.clear();
        orderKey.clear();
        task.clear();
    }

    //设置背景
    public void settingBg(Alayout data) {
       if (data.bgType == 1){//纯色
           String colorValue = AppTools.TanslateColor(data.bgValue);
           try {
               this.setBackgroundColor(Color.parseColor(colorValue));
           } catch (Exception e) {
               this.setBackgroundColor(Color.WHITE);
           }
       }
       if (data.bgType == 2){
            String path = UiExcuter.getInstancs().basepath+data.bgValue;
            if (FileUtils.isFileExist(path)){
                Bitmap bitmap = ImageViewPicassocLoader.getBitmap(path);
                this.setBackground(new BitmapDrawable(bitmap));
            }
       }
    }

    //按钮的点击事件
    @Override
    public void onClick(View v) {
        //
        try {
            Abutton data = (Abutton) v.getTag();
            //获取一个 tag, 获取类型
            if (data.isNextFileType()){
                //如果是文件类型  去获取文件数据对象-> 去创建文件显示器
                Afile afile = fileMap.get(data.nextTag);
                if (afile!=null){
                 createFileLayer(afile);
                }
            }else{
                //如果是布局类型 获取布局数据对象->清空视图->执行布局显示 ,添加 回退按钮 -> 按钮的点击效果就是,回到上一层视图层,所以需要告诉按钮上一层视图tag
            Alayout lyt = layoutMap.get(data.nextTag);
                if (lyt!=null){
                    excuteLayout(lyt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //创建文件显示层
    private void createFileLayer(Afile afile) {
        final AFileShow layer = new AFileShow(context);
        layer.onCreateContent(afile.contentList);//创建内容 - 图片 视频 网页
        layer.onAddCancelButton(new OnClickListener() {
            @Override
            public void onClick(View v) {
                layer.onDestory();
                IinteractionPlayer.this.removeView(layer);
            }
        });//添加返回按钮
        layer.setBackgroundColor(Color.BLACK);
        layer.setLayoutParams(new AbsoluteLayout.LayoutParams(getWidth(),getHeight(),0,0));
        this.addView(layer);
        layer.tanslationContent();
    }
}
