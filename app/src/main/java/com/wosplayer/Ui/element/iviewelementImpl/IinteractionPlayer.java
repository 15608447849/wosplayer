package com.wosplayer.Ui.element.iviewelementImpl;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wosplayer.Ui.element.IPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.IviewPlayer;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.beads.LayoutActive;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.iCache.InteractionCache;
import com.wosplayer.Ui.element.iviewelementImpl.userDefinedView.interactivemode.xml.XmlParse;
import com.wosplayer.app.DataList;
import com.wosplayer.app.log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import rx.Scheduler;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/7/26.
 */

public class IinteractionPlayer extends AbsoluteLayout implements IPlayer{

    private static final java.lang.String TAG ="IinteractionPlayer" ;//IinteractionPlayer.class.getName();
    private Context mCcontext;
    private ViewGroup mfatherView = null;
    private int x=0;
    private int y=0;
    private int h=0;
    private int w=0;
    private boolean isExistOnLayout = false;
    public IinteractionPlayer(Context context,ViewGroup mfatherView) {
        super(context);
        mCcontext =context;
        this.mfatherView = mfatherView;
    }

    private DataList mp = null;
    private String mUri = null;
    private String name = null;
    @Override
    public void loadData(DataList mp, Object ob) {
        try {
            this.mp = mp;
            this.x = mp.GetIntDefualt("x", 0);
            this.y = mp.GetIntDefualt("y", 0);
            this.w = mp.GetIntDefualt("width", 0);
            this.h = mp.GetIntDefualt("height", 0);
            this.mUri = mp.GetStringDefualt("getcontents", "");

            InteractionCache.uid = mp.GetStringDefualt("uuks","ffffffff");
            name = mp.GetStringDefualt("contentsname","null");
        }catch (Exception e){
            log.e(TAG, "loaddata() " + e.getMessage());
        }

    }
    @Override
    public void setlayout() {
        log.d(TAG,"设置布局 isExistOnLayout :"+isExistOnLayout);
        try {
            if (!isExistOnLayout){
                this.setBackgroundColor(Color.BLACK);
                mfatherView.removeView(this);
                mfatherView.addView(this);
                isExistOnLayout = true;
                log.d(TAG,"添加到父布局上 成功");
            }
            log.d(TAG,"设置布局:"+x+"-"+y+"-"+w+"-"+h);
            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) this
                    .getLayoutParams();
            lp.x = x;
            lp.y = y;
            lp.width = w;
            lp.height = h;
            this.setLayoutParams(lp);

        } catch (Exception e) {
            log.e(TAG,"设置布局:" + e.getMessage());
        }
        /*finally {
            boolean f = false;
            for (int i=0;i<mfatherView.getChildCount();i++){
                if (mfatherView.getChildAt(i).equals(this)){
                    log.d(TAG,"************************");
                    f = true;
                    break;
                }
            }
            if (!f){
                log.d(TAG,"f "+f);
                this.setBackgroundColor(Color.RED);
                mfatherView.removeView(this);
                mfatherView.addView(this);
            }
        }*/
    }

    @Override
    public void start() {
        try{
            setlayout();//设置布局
            startActive();
            //开始互动模块
        }catch (Exception e){
            log.e(TAG,"开始:"+e.getMessage());
        }
    }

    @Override
    public void stop() {
        try {
            log.d(TAG,"清理视图 开始");
            //移除父视图
            mfatherView.removeView(this);
            isExistOnLayout = false;
            stopActive();
            // 移除 互动模块
            log.d(TAG,"清理视图 结束");
        }catch (Exception e){
            log.e(TAG,"停止:"+e.getMessage());
        }
    }
    @Override
    public DataList getDatalist() {
        return mp;
    }
    @Override
    public void downloadResult(String filePath) {
        //null
    }

    //执行互动加载工作
    public static final Scheduler.Worker worker = Schedulers.io().createWorker();
    private static ReentrantLock lock = new ReentrantLock();

    //开始 互动
    private void startActive(){
        try {
            lock.lock();
            worker.schedule(new Action0() {
                @Override
                public void call() {
                    log.i(TAG,IinteractionPlayer.this.toString()+"开始执行互动..." + Thread.currentThread().getName());
                    execute();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    };
    //停止互动
    private void stopActive(){
        worker.schedule(new Action0() {
            @Override
            public void call() {
                releasedResource();
            }
        });

    }

/////////////////////////////////////////////////////
/**
 * 结束执行
 */

//移除资源
private void releasedResource() {
    //查看 视图栈 ,存在的视图 全部 调用 自己的释放资源

    for (View ivp : displayedList) {

        if (ivp instanceof IviewPlayer) {

            ((IviewPlayer) ivp).removeMeToFather();
        }
    }
    displayedList.clear();
}






    //////////////////////////////////////////////////////////


    /**
     * 开始执行
     */
    public void execute() {
        if (mUri != null) {
            getXMLdata(mUri); //获取xml
        }
    }

    public static HttpUtils http = new HttpUtils();//网络连接使用
    /**
     * 获取xml
     *
     * @param uri
     */
    private void getXMLdata(String uri) {

        log.d(TAG," 互动布局name:"+name+"+uri"+uri);


        final String result = InteractionCache.pull(uri);
        if (result!=null){
           worker.schedule(new Action0() {
                @Override
                public void call() {
                    ParseResultXml(result,1);
                }
            });
            return;
        }
        final String key = uri;

        http.send(
                HttpRequest.HttpMethod.GET,
                uri,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        log.d(TAG, "互动模块 第一次 : " + responseInfo.result);
                        InteractionCache.push(key,responseInfo.result);//存
                        //进行XML解析
                        ParseResultXml(responseInfo.result,0);
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        log.e(TAG, "互动模块 获取 互动布局文件 失败 >> " + s);
                    }
                }
        );

    }

    private LayoutActive myBindView;
    /**
     * 解析
     *
     * @param xml 可得到 和我绑定的第一个view 是什么
     *            第一次 第一层 布局
     * @return
     */
    private void ParseResultXml(String xml,int source) {
        log.d(TAG," 互动执行者 解析 信息中...\n xml来源:"+(source==1?"本地缓存":"网络") );
        try {
            myBindView = XmlParse.interactionParse_one(xml);
        } catch (Exception e) {
          log.e(TAG,"err "+ e.getMessage());
            return;
        }
        if (myBindView != null) {
            log.i(TAG," 互动执行者 获取绑定的视图 - "+myBindView.toString());
            myBindView.addMeToFather(this);
            setmCurrentView(myBindView);//设置当前视图
        }
    }


    private View mCurrentView; //当前显示的视图
    /**
     * 设置当前显示的视图
     *
     * @param view
     */
    public void setmCurrentView(View view) {
        mCurrentView = view;
        addDisplay(mCurrentView);
        clearSingeView();
    }

    /**
     * 互动模块 维护一个 列表 存放 视图, 所有的视图 都是存放在 我上面的.  所有 这是一个 栈队列
     */
    private List<View> displayedList = Collections.synchronizedList(new LinkedList<View>());

    /**
     * 添加一个显示过的视图
     */
    private void addDisplay(View v) {
        if (displayedList.contains(v)) {
            //如果包含 删除~
            displayedList.remove(v);
        }
        displayedList.add(v);//增加到最后
    }
    /**
     * 清楚一个视图
     * 不删除视图队列
     */
    public void clearSingeView() {
        if (displayedList.size() < 2) {
            return;
        }
        View v = displayedList.get(displayedList.indexOf(mCurrentView) - 1); // 上一个
        this.removeView(v);
    }

    /**
     * 返回上一个视图
     */
    public void returnPrevionsView() {

        //找到当前视图的上一个视图de index
        View view = displayedList.get(displayedList.indexOf(mCurrentView) - 1);
        //杀死当前视图
        killSingeView(mCurrentView);

        //当前视图设为要显示的视图
        mCurrentView = view;
        //显示
        this.addView(mCurrentView);
        //判断 如果 是个 层布局 看看是不是带按钮 带按钮就添加按钮
        if(view instanceof LayoutActive){
            if (((LayoutActive)view).returnBtn !=null){
                this.removeView(((LayoutActive)view).returnBtn );
                this.addView(((LayoutActive)view).returnBtn );
            }
        }
    }



    /**
     * 删除一个 显示的视图,
     */
    private void removeDisplay(View v) {
        displayedList.remove(v);
        if (v instanceof IviewPlayer) {

            ((IviewPlayer) v).removeMeToFather();
        }

    }


    /**
     * 杀死一个视图　不存在队列站
     *
     * @param view
     */
    public void killSingeView(View view) {
        if (displayedList.contains(view)) {
            removeDisplay(view);
        }
    }










}
