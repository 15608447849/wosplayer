package com.wosplayer.Ui.element.iviewelementImpl.actioner;

import com.wosplayer.Ui.element.iviewelementImpl.actioner.ContainerItem.ButtonContainer;
import com.wosplayer.Ui.element.iviewelementImpl.actioner.ContainerItem.LayoutContainer;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;

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

    public static Container TanslateDataToContainer(DataStore ds,Container prev){


            DataList dl = ds.getData();

            if (prev == null){  //如果不存在 上一个视图
               prev = createLayoutFirst(dl); //创建 第一层布局容器

                //创建它的button容器  //布局 - child-> button

                ArrayList<DataStore> arr = ds.getStores();
                if (arr !=null && arr.size()>0){
                    for (int i = 0 ;i<arr.size();i++){
                        Container buttonContainer = TanslateDataToContainer(arr.get(i),prev);
                        if (buttonContainer==null){
                            log.e(TAG," button container is null");
                            continue;
                        }
                        prev.addChilds(buttonContainer);//布局 添加 button 到 childs
                    }
                }
                log.e(TAG,"---- 容器 创建 完成 ----");
            }else{

                String level = dl.GetStringDefualt("level","");
                Container current = null;
                if (level.equals("")){
                    log.e(TAG," level is null ");
                    return current;
                }

                if (level.equals(DataSeparator.layoutItemLevel)){//按钮

                    current = currentButtonContainer(dl);//数据
                    if (current!=null){
                        //设置按钮的下一个视图
                        ArrayList<DataStore> arr = ds.getStores();
                        if (arr!=null && arr.size()==1){
                            log.i(TAG,"按钮 next exist");
                            Container layout = TanslateDataToContainer(arr.get(0),prev);
                            current.next = layout;//按钮 关联 下一个视图
                            //设置按钮的点击事件
                            current.onClick(null);
                        }else{
                            log.e(TAG,"button next container is err");

                        }
                    }
                    return current;
                }
                if (level.equals(DataSeparator.layoutLevel)){ //布局容器

                    return current;
                }
                if (level.equals(DataSeparator.folderLevel)){ // listView
                    return current;
                }
                if (level.equals(DataSeparator.folderItemLevel)){ // item (image video web)
                    return current;
                }

            }


        //button - next - > 1.布局   2. filelist

        //filelist - chile -> image,video,web ...
        return prev;
    }

    /**
     * 创建 某一个 按钮
     * @param dl
     * @return
     */
    private static Container currentButtonContainer(DataList dl) {

        ButtonContainer buttonContainer = new ButtonContainer(DisplayActivity.activityContext,dl);
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
    private static Container createLayoutFirst(DataList dl) {
        //创建布局容器
        LayoutContainer layoutContainer = new LayoutContainer(DisplayActivity.activityContext,dl);

        if(param!=null){
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
              log.e(TAG," "+e.getMessage());
            }
            param.put("wscale",wscale);
            param.put("hscale",hscale);
        }
        return layoutContainer;
    }


}
