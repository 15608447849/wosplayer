package com.wosplayer.Ui.element.interfaces;

import android.view.View;

/**
 * Created by user on 2016/7/6.
 */
public interface IviewPlayer {
    void AotuLoadingResource();
    void addMeToFather(View view);
    void removeMeToFather();
    int getPlayDration(IviewPlayer iviewPlayer);
    void otherMother(Object object);
}
