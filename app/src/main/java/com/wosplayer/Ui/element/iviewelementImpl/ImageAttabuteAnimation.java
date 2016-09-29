package com.wosplayer.Ui.element.iviewelementImpl;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by user on 2016/9/27.
 */
public class ImageAttabuteAnimation {


    public static final int ANIM_SNUB =  14;

    public static final int INTER_SNUB =  7;

    public static AnimatorSet getOneAnimation(int number, View view, int[] point) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animation = null;


        switch (number){
            case 1://旋转 x 顺时针
                animation =
                        ObjectAnimator
                                .ofFloat(view, "rotationX", 0.0F, 360.0F);

                set.setDuration(500);
                set.play(animation);
                break;

            case 2://旋转 x 逆时针
                animation =
                        ObjectAnimator
                                .ofFloat(view, "rotationX", 360.0F, 0.0F);

                set.setDuration(500);
                set.play(animation);
                break;
            case 3://旋转 y 顺时针
                animation =
                        ObjectAnimator
                                .ofFloat(view, "rotationY", 0.0F, 360.0F);
                set.setDuration(500);
                set.play(animation);

                break;


            case 4://旋转Y 逆时针
                animation =
                        ObjectAnimator
                                .ofFloat(view, "rotationY", 360.0F, 0.0F);

                set.setDuration(500);
                set.play(animation);
                break;


            case 5://旋转 顺时针
                animation =
                        ObjectAnimator
                                .ofFloat(view, "rotation", 0.0F, 360.0F);

                set.setDuration(500);
                set.play(animation);

                break;
            case 6://旋转 逆时针
                animation =
                        ObjectAnimator
                                .ofFloat(view, "rotation", 360.0F, 0.0F);
                set.setDuration(500);
                set.play(animation);


                break;
            case 7: //透明度
                ObjectAnimator animator =
                        ObjectAnimator.ofFloat(view,"alpha",0F,1F);

                set.setDuration(500);
                set.play(animation);
                break;

            case 8://透明 + 旋转

                set.play(ObjectAnimator.ofFloat(view,"alpha",0F,1F))
                        .with(ObjectAnimator.ofFloat(view,"rotation",0.0F,360.0F));// with  同时,支持,随着
                set.setDuration(500);
                break;
            case 9://透明 + 旋转 + 比例缩放


                set.play( ObjectAnimator.ofFloat(view,"alpha",0F,1F))
                        .with(ObjectAnimator.ofFloat(view,"rotation",0.0F,360.0F))
                        .with(ObjectAnimator.ofFloat(view,"scale",0.5F,1.0F));
                set.setDuration(500);
                break;
            case 10://透明 +比例缩放

                set.play( ObjectAnimator.ofFloat(view,"alpha",0F,1F))
                        .with(ObjectAnimator.ofFloat(view,"scale",1.5F,0.5F))
                        .after(ObjectAnimator.ofFloat(view,"scale",0.0F,1.0F));
                set.setDuration(500);
                break;
            case 11://View 移动到下方300  同时旋转

               ObjectAnimator moveMyBn_300_1 =  ObjectAnimator.ofFloat(view, "y",
                        point[1], point[1]+300.0f);
                moveMyBn_300_1.setRepeatCount(1);
                moveMyBn_300_1.setRepeatMode(ValueAnimator.REVERSE);
                set.play(moveMyBn_300_1).with(ObjectAnimator.ofFloat(view,"rotation",360.0f,0.0F));
                set.setStartDelay(200);
                set.setDuration(1000);

                break;

            case 12://View 移动到下方300  同时透明

                ObjectAnimator moveMyBn_300_2 =  ObjectAnimator.ofFloat(view, "y",
                        point[1], point[1]+300.0f);
                moveMyBn_300_2.setRepeatCount(1);
                moveMyBn_300_2.setRepeatMode(ValueAnimator.REVERSE);
                set.play(moveMyBn_300_2).with(ObjectAnimator.ofFloat(view,"alpha",0.0f,1.0F));
                set.setStartDelay(200);
                set.setDuration(1000);

                break;
            case 13://View 移动到下方300  同时缩放

                ObjectAnimator moveMyBn_300_3 =  ObjectAnimator.ofFloat(view, "y",
                        point[1], point[1]+300.0f);
                moveMyBn_300_3.setRepeatCount(1);
                moveMyBn_300_3.setRepeatMode(ValueAnimator.REVERSE);
                set.play(moveMyBn_300_3).with(ObjectAnimator.ofFloat(view,"scale",2.0F,0.5F)).after(ObjectAnimator.ofFloat(view,"scale",0.5F,1.0F));
                set.setStartDelay(200);
                set.setDuration(1000);
                break;

            case 14://view 从 -width -> width    渐渐透明

                set.play(ObjectAnimator.ofFloat(view, "translationX",
                        -point[2], 0.0f))
                        .with(ObjectAnimator.ofFloat(view,"alpha",0.0f,1.0F))
                        .with(ObjectAnimator.ofFloat(view,"scaleX",0.0f,1.0F));
                set.setDuration(2000);
                break;


        }
        return set;
    }


    public static TimeInterpolator getOneTimeInterpolator(int selectNunber) {
        TimeInterpolator v = null;
        switch (selectNunber){
            case 1: //自由落体
                v =  new BounceInterpolator();
                break;
            case 2:
                v = new OvershootInterpolator();
                break;
            case 3:
                v = new DecelerateInterpolator();
                break;
            case 4:
                v = new AnticipateInterpolator();
                break;
            case 5:
                v= new AccelerateInterpolator();
                break;
            case 6 :
                v =new AccelerateDecelerateInterpolator();
                break;
            case 7:
                v = new CycleInterpolator(selectNunber);
                break;
        }
        return v;
    }


    /**
     * 设置属性动画
     */

    public static void SttingAnimation(Context mCcontext, final View view,int [] point){

        //属性动画
        int selectNunber = (int)(Math.random()*ImageAttabuteAnimation.ANIM_SNUB);
        AnimatorSet amt = ImageAttabuteAnimation.getOneAnimation(selectNunber,view,point);
        if(amt!=null){
            selectNunber = (int)(Math.random()*ImageAttabuteAnimation.INTER_SNUB);
            amt.setInterpolator(ImageAttabuteAnimation.getOneTimeInterpolator(selectNunber));
            amt.start();
        }


       /* if (view instanceof ImageButton) {
            //淡入淡出
            final Animation animationFadeIn = AnimationUtils.loadAnimation(mCcontext, R.anim.fadein);
            final Animation animationFadeOut = AnimationUtils.loadAnimation(mCcontext, R.anim.fadeout);
            Animation.AnimationListener animListener = new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.startAnimation(animationFadeIn);
                }
            };
            view.startAnimation(animationFadeOut);
            animationFadeOut.setAnimationListener(animListener);

            return;
        }*/




    }



}

//
//       /* final Animation animationFadeIn = AnimationUtils.loadAnimation(mCcontext, R.anim.fadein);
//        final Animation animationFadeOut = AnimationUtils.loadAnimation(mCcontext, R.anim.fadeout);
//        Animation.AnimationListener animListener = new Animation.AnimationListener(){
//
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                IImagePlayer.this.startAnimation(animationFadeIn);
//            }
//        };
//        this.startAnimation(animationFadeOut);
//        animationFadeOut.setAnimationListener(animListener);*/
//        //属性动画
//        int selectNunber = (int)(Math.random()*ImageAttabuteAnimation.ANIM_SNUB);
//
//        AnimatorSet amt = ImageAttabuteAnimation.getOneAnimation(selectNunber,this);
//        if(amt!=null){
//            selectNunber = (int)(Math.random()*ImageAttabuteAnimation.INTER_SNUB);
//            amt.setInterpolator(ImageAttabuteAnimation.getOneTimeInterpolator(selectNunber));
//            amt.start();
//        }