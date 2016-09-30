package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode;

import android.view.View;

/**
 * Created by user on 2016/7/6.
 */
public interface IviewPlayer {
    void AotuLoadingResource();
    void addMeToFather(View view);
    void addMeToFather(View view,boolean f);
    void removeMeToFather();
    void removeMeToFather(boolean f);
}
