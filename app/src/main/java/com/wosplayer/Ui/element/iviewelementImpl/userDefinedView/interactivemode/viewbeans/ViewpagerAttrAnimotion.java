package com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.viewbeans;

import android.view.View;
import android.view.ViewGroup;

import com.wosplayer.app.log;

/**
 * Created by user on 2016/9/27.
 */
public class ViewpagerAttrAnimotion {

    private int random = 0;
    private static ViewpagerAttrAnimotion m ;
    private ViewpagerAttrAnimotion(){
        log.e("初始化 viewpage 切换动画 ");
    }
    public static ViewpagerAttrAnimotion getInstands(){
        if (m==null){
            m =new ViewpagerAttrAnimotion();
        }
        m.random = (int)(Math.random()* 8);// 1-7
        return m;
    }

    public void TranslationPager(View view, float position){
        //随机数

        switch (random){
            case 1:
                sink3D(view,position);
                break;
            case 2:
                raised3D(view,position);
                break;
            case 3:
                seightDis(view,position);
                break;
            case 4:
                imitateQQ(view,position);
                break;
            case 5:
                //rollingPage(view,position);
                Tanslate3D(view,position);
                break;
            case 6:
                ZoomOutPageTransformer(view,position);
                break;
            case 7:
                DepthPageTransformer(view,position);
                break;
            case 8:
                Tanslate3D(view,position);
                break;
        }
    }


/**
 * 3d旋转
 */
    private void Tanslate3D(View view, float position){
        int width = view.getWidth();
        int pivotX = 0;
        if (position <= 1 && position > 0) {// right scrolling
            pivotX = 0;
        } else if (position == 0) {

        } else if (position < 0 && position >= -1) {// left scrolling
            pivotX = width;
        }
        //设置x轴的锚点
        view.setPivotX(pivotX);
        //设置绕Y轴旋转的角度
        view.setRotationY(90f * position);

    }

    /**
     * 动画效果1  凹陷的3D效果
     */
    private  void sink3D(View view, float position){
        if(position>=-1&&position<=1){
            view.setPivotX(position<0?view.getWidth():0);
            view.setRotationY(-90*position);
        }
    }
    /**
     * 动画效果2  凸起的3D效果
     */
    private  void raised3D(View view,float position){
        if(position>=-1&&position<=1){
            view.setPivotX(position<0?view.getWidth():0);//设置要旋转的Y轴的位置
            view.setRotationY(90*position);//开始设置属性动画值
        }
    }
    /**
     * 动画效果3  视差的效果
     * 这个地方要注意此处的view不是单纯的手指滑动的那个RelativeLayout
     * 因为对于FragmentpagerAdapter,使用时默认会在fragment布局的最外层套上一层
     * FrameLayout,所以你要是使用view.getChildAt()得到的是一个外层的FrameLayout
     * 而不是我们要进行视差动画的ImageView,所以就是用findViewById()来拿到跟布局RelativeLayout
     * 然后再调用getChildAt()方法来得到所有的ImageView
     */
    private  void seightDis(View view,float position){
        if(position>=-1&&position<=1){
            ViewGroup vg = (ViewGroup) view;//.findViewById(R.id.rl);
            for(int i=0;i<vg.getChildCount();i++){
                View child=vg.getChildAt(i);
                child.setTranslationX(Math.abs(position)*child.getWidth()*2);
            }
        }
    }
    /**
     * 动画效果4  仿QQ的缩放动画效果
     */
    private  void imitateQQ(View view,float position){
        if(position>=-1&&position<=1){
            view.setPivotX(position>0?0:view.getWidth()/2);
            //view.setPivotY(view.getHeight()/2);
            view.setScaleX((float)((1-Math.abs(position)<0.5)?0.5:(1-Math.abs(position))));
            view.setScaleY((float)((1-Math.abs(position)<0.5)?0.5:(1-Math.abs(position))));
        }
    }
    /**
     * 不可用 有bug
     * 动画效果5  仿掌阅的翻书动画效果
     * 分析翻书的效果,可以分解为两部分:1.左边的view绕着左边的轴旋转,同时x方向上有缩放的效果
     * 要注意的是因为是viewpager左边的view在滑动的时候是要向左边移动的,但我们要的翻书效果在翻页完成前
     * 是一直在读者视角内的,所以左边的view在滑动的时候要进行向右的平移
     * 2.右边的view从可见的时候开始就一直在左view的下方,但是作为viewpager他是从右边慢慢滑到当前的位置的
     * 所以要达到这个效果就需要进行一个x方向的平移动画
     */
    private  void rollingPage(View view,float position){
        if(position>=-1&&position<=1){
            view.setPivotX(0);
            if(position<0){
                view.setTranslationX(-position*view.getWidth());
                view.setRotationY(90*position);
                view.setScaleX(1-Math.abs(position));
            }
            else{
                view.setTranslationX(-position*view.getWidth());
            }

        }
    }



    //ZoomOutPageTransformer
    private void ZoomOutPageTransformer(View view,float position){
        final float MIN_SCALE = 0.85f;
        final float MIN_ALPHA = 0.5f;
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1)
        { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 1) //a页滑动至b页 ； a页从 0.0 -1 ；b页从1 ~ 0.0
        { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0)
            {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else
            {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
                    / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        } else
        { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }


    //DepthPageTransformer
    private void DepthPageTransformer(View view,float position){
        final float MIN_SCALE = 0.75f;
        int pageWidth = view.getWidth();
        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }

    }
}
