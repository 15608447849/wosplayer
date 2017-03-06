package com.wosplayer.Ui.element.not_use_ui_class.actioner;

import com.wosplayer.Ui.element.not_use_ui_class.actioner.ContainerItem.ButtonContainer;
import com.wosplayer.Ui.element.not_use_ui_class.actioner.ContainerItem.ContentContainer;
import com.wosplayer.Ui.element.not_use_ui_class.actioner.ContainerItem.LayoutContainer;
import com.wosplayer.Ui.element.not_use_ui_class.actioner.ContainerItem.ListViewContainer;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.DataList;
import com.wosplayer.app.Logs;

import java.util.ArrayList;

/**
 * Created by user on 2016/8/2.
 *
 *  public static final  String layoutLevel = "interaction_layout";
    public static final String layoutItemLevel = "interaction_layout_items_item";
     public static final String folderLevel = "interaction_layout_items_item_folder";
    public static final String folderItemLevel = "interaction_layout_items_item_floder_item";
 */
public class ContainerFactory {

    private static final String TAG = "container_factory";

    private static DataList param = null;
    public static void SettingParam(DataList dl){
        param = null;
        param = dl;
    }

    /**
     *
     * @param ds 当前 数据存储
     * @param prev 上一个视图
     * @return
     */
    public static Container TanslateDataToContainer(DataStore ds,Container prev){


            DataList dl = ds.getData();

            if (prev == null){  //如果不存在 上一个视图
               prev = createLayout(dl,true); //创建 第一层布局容器

                //创建它的button容器  //布局 - child-> button
                ArrayList<DataStore> arr = ds.getStores();
                if (arr !=null && arr.size()>0){
                    for (int i = 0 ;i<arr.size();i++){
                        Container buttonContainer = TanslateDataToContainer(arr.get(i),prev);
                        if (buttonContainer==null){
                            Logs.e(TAG," button container is null");
                            continue;
                        }
                        prev.addChilds(buttonContainer);//布局 添加 button 到 childs
                    }
                }
                Logs.e(TAG,"---- 容器 创建 完成 ----");
            }else{

                String level = dl.GetStringDefualt("level","");
                Container current = null;
                if (level.equals("")){
                    Logs.e(TAG," level is null ");
                    return current;
                }

                if (level.equals(DataSeparator.layoutItemLevel)){//按钮

                    current = currentButtonContainer(dl);//数据
                    if (current!=null){
                        current.previous = prev;
                        //设置按钮的下一个视图
                        ArrayList<DataStore> arr = ds.getStores();
                        if (arr!=null && arr.size()==1){
                            Logs.i(TAG,"按钮 next exist");
                            Container layout = TanslateDataToContainer(arr.get(0),prev);
                            if (layout!=null){
                                current.next = layout;//按钮 关联 的下一个视图 ->布局 或者 列表
                                //设置按钮的点击事件
                                current.onClick(null);
                            }

                        }else{
                            Logs.e(TAG,"button next container is err");
                        }
                    }
                    return current;
                }
                if (level.equals(DataSeparator.layoutLevel)){ //布局容器
                    //按钮绑定的 布局
                    //需要增加返回按钮
                    current = createLayout(dl,false);
                    if (current!=null){
                        current.previous = prev;// 下一个视图回到的上一个视图

                        //创建它的button容器  //布局 - child-> button
                        ArrayList<DataStore> arr = ds.getStores();
                        if (arr !=null && arr.size()>0){
                            for (int i = 0 ;i<arr.size();i++){
                                Container buttonContainer = TanslateDataToContainer(arr.get(i),current);
                                if (buttonContainer==null){
                                    Logs.e(TAG," button container is null,index ="+i);
                                    continue;
                                }
                                current.addChilds(buttonContainer);//布局 添加 button 到 childs
                            }
                        }else{
                            Logs.e(TAG,"layout child container is err: dataStore arr :"+arr);
                        }
                    }
                    return current;
                }
                if (level.equals(DataSeparator.folderLevel)){ // listView
                    current = createList(dl);
                    if (current!=null){
                        current.previous = prev;// 上一个视图

                        ArrayList<DataStore> arr = ds.getStores();
                        if (arr !=null && arr.size()>0){

                            for(int i=0;i<arr.size();i++){
                                Container content = TanslateDataToContainer(arr.get(i),current);//具体内容 web image video
                                if (content==null){
                                    Logs.e(TAG," list content container is null,index ="+i);
                                    continue;
                                }
                                current.addChilds(content);
                            }
                            //设置适配器
                            ((ListViewContainer)current).SetttingAdapter();
                            //设置子项点击时间
                            current.onClick(null);

                        }else{
                            Logs.e(TAG,"list child container is err");
                        }

                    }
                    return current;
                }
                if (level.equals(DataSeparator.folderItemLevel)){ // item (image video web)

                    current = createContent(dl);
                    if (current!=null){
                        current.previous =prev;
                    }
                    return current;
                }

            }


        //button - next - > 1.布局   2. filelist

        //filelist - chile -> image,video,web ...
        return prev;
    }

    /**
     * 创建具体内容 视图
     * @param dl
     * @return
     */
    private static Container createContent(DataList dl) {
        ContentContainer content = new ContentContainer(null,dl);

        return content;
    }

    private static Container createList(DataList dl) {
        ListViewContainer listContainer = new ListViewContainer(null,dl);
        return listContainer;
    }


    /**
     * 创建 某一个 按钮
     * @param dl
     * @return
     */
    private static Container currentButtonContainer(DataList dl) {

        ButtonContainer buttonContainer = new ButtonContainer(null,dl);
        if(param!=null){
            //设置按钮的宽高比例
            float wscale = (float) (param.GetdoubleDefualt("wscale", 1));
            float hscale = (float) (param.GetdoubleDefualt("hscale", 1));
            buttonContainer.onSettingScale(wscale,hscale);
        }
        return buttonContainer;
    }

    /**
     * 创建布局 第一次
     * @param dl
     */
    private static Container createLayout(DataList dl,boolean isFirst) {
        //创建布局容器
        LayoutContainer layoutContainer = new LayoutContainer(null,dl);

        if(param!=null && isFirst){
            //设置 布局背景的宽高比例
            int width = param.GetIntDefualt("width", 0);
            int height = param.GetIntDefualt("height", 0);
            float[] farr = layoutContainer.onSettingScale(width,height);
            String wscale = "1.0";
            String hscale = "1.0";
            try {
                wscale  = String.valueOf(farr[0]);
                hscale = String.valueOf(farr[1]);
            } catch (Exception e) {
              Logs.e(TAG," "+e.getMessage());
            }
            param.put("wscale",wscale);
            param.put("hscale",hscale);
        }else{
            //传递比例值

            float wscale = (float) (param.GetdoubleDefualt("wscale", 1));
            float hscale = (float) (param.GetdoubleDefualt("hscale", 1));
            layoutContainer.onSettingScale(wscale,hscale);
        }

        if (!isFirst){
            //添加返回按钮
            layoutContainer.addReturnButton(true);
        }


        return layoutContainer;
    }


}
