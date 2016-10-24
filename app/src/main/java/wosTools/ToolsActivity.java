package wosTools;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wosplayer.R;
import com.wosplayer.app.log;

/**
 * Created by Administrator on 2016/10/19.
 * 弹窗
 */

public class ToolsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wostools);

        ToolsHandler.woshandler=new ToolsHandler(this);

        //加载布局
        InitView();
        //读取配置文档并封装
        InitConfig();
        //加载数据
        InitValue();
    }

    private EditText      serverip;
    private EditText      serverport;
    private EditText      companyid;
    private EditText      terminalNo;
    private EditText      BasePath;
    private EditText      heartbeattime;
    private Button        btnGetID;
    private Button        btnSaveData;
    /**
     * 加载布局
     */
    public void InitView()
    {
        try
        {
            serverip=(EditText)this.findViewById(R.id.serverip);
            serverport=(EditText)this.findViewById(R.id.serverport);

            companyid=(EditText)this.findViewById(R.id.companyid);
            terminalNo=(EditText)this.findViewById(R.id.terminalNo);
            BasePath=(EditText)this.findViewById(R.id.BasePath);
            heartbeattime=(EditText)this.findViewById(R.id.HeartBeatInterval);

            btnGetID=(Button)this.findViewById(R.id.btnGetID);
            btnSaveData=(Button)this.findViewById(R.id.btnSaveData);

        }catch(Exception e)
        {
            log.e("InitView() err:"+e.getMessage());
        }
    }

    public  DisplayMetrics m_dm;
    public  DataListEntiy dataList;
    /**
     *
     */
    /**
     * 加载配置
     */
    public  void InitConfig()
    {
        log.i("toolsActivity", "开始加载数据");
        m_dm = new DisplayMetrics();
        //获取手机分辨率
        this.getWindowManager().getDefaultDisplay().getMetrics(m_dm);
        dataList=new DataListEntiy();
        dataList.ReadShareData(true);
    }

   /**
    *  加载数据
    */
    public void InitValue()
    {
        try
        {
            serverip.setText(dataList.GetStringDefualt("serverip", "127.0.0.1"));
            serverport.setText(dataList.GetStringDefualt("serverport", "8000"));
            companyid.setText(dataList.GetStringDefualt("companyid", "999"));
            terminalNo.setText(dataList.GetStringDefualt("terminalNo", ""));
            heartbeattime.setText(dataList.GetStringDefualt("HeartBeatInterval", "30"));
            BasePath.setText(dataList.GetStringDefualt("basepath", "mnt/sdcard"));
            //焦点默认在这个控件上
            serverip.setFocusable(true);
        }catch(Exception e)
        {
            Log.e("ToolsActivity ", e.getMessage());
        }
    }

    public void GetSetConnectInfo()
    {
        dataList.put("serverip",  serverip.getText().toString());
        dataList.put("serverport",  serverport.getText().toString());
    }

    public void outText(String text)
    {
        Toast.makeText(this, text,Toast.LENGTH_LONG ).show();
    }


    //反射调用
    public void setcompanyid(String value)
    {
        try
        {
            terminalNo.setText(value);
            dataList.put("terminalNo", value);
            outText("申请终端ID成功");
            terminalNo.setFocusable(true);

        }catch(Exception e)
        {
            Log.e("ToolsActivity", e.getMessage());
        }
    }


        /**
         * 获取传入的数据并封装
         */
        public void GetViewValue()
        {
            dataList.put("terminalNo",terminalNo.getText().toString());
            dataList.put("serverip",  serverip.getText().toString());
            dataList.put("serverport",  serverport.getText().toString());
            dataList.put("companyid",  companyid.getText().toString());
            dataList.put("HeartBeatInterval",  heartbeattime.getText().toString());
            String basepath=BasePath.getText().toString();
            if(!basepath.endsWith("/"))
            {
                basepath=basepath+"/";
            }
            dataList.put("basepath",  basepath);
        }






    @Override
    protected void onDestroy() {
        super.onDestroy();
        GetViewValue();
        dataList.SaveShareData();
        ToolsHandler.woshandler = null;
    }

    //点击事件
    /**
     * 点击获取id
     * @param view
     */
    public void getId(View view){

        if(dataList!=null && m_dm!=null){
            //结束线程
            RequstTerminal.EndRequst();
            //把数据封装到集合中
            GetViewValue();
            //开启线程
            RequstTerminal.BeginRequst(dataList,m_dm);
        }
    }
    /**
     * 点击保存数据
     * @param view
     */
    public void saveData(View view){
        GetViewValue();
        dataList.SaveShareData();
        this.finish();
    }
}
