package com.wosplayer.Ui.element.iviewelementImpl.actioner;

import com.wosplayer.app.DataList;
import com.wosplayer.app.log;

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

    public static Container TanslateDataToContainer(DataStore ds,Container pre){

            Container c = null;
            if (pre == null){
//                c = new Container();//第一次
            }

            DataList dl = ds.getData();
            String level = dl.GetStringDefualt("level","null");
            if (level.equals("null")){
                log.e(TAG," level is null");
                return c;
            }

            if (level.equals(DataSeparator.layoutLevel)){
                //布局容器
                createLayout(dl,c);
            }
            if (level.equals(DataSeparator.layoutItemLevel)){

            }
            if (level.equals(DataSeparator.folderLevel)){

            }
            if (level.equals(DataSeparator.folderItemLevel)){

            }
        return c;
    }

    /**
     * 创建布局
     * @param dl
     * @param c
     */
    private static void createLayout(DataList dl, Container c) {





    }


}
