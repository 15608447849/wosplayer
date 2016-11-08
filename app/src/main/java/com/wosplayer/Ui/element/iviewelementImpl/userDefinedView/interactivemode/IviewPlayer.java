package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode;

import android.view.View;

import com.wosplayer.loadArea.excuteBolock.LoaderCall;

/**
 * Created by user on 2016/7/6.
 */
public interface IviewPlayer extends LoaderCall {
    void AotuLoadingResource();
    void addMeToFather(View view);
    void addMeToFather(View view,boolean f);
    void removeMeToFather();
    void removeMeToFather(boolean f);
    int getPlayDration(IviewPlayer iviewPlayer);
    void otherMother(Object object);
}
