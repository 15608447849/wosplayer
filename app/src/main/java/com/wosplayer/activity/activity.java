package com.wosplayer.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wosplayer.R;
import com.wosplayer.app.wosPlayerApp;

/**
 *  Timer timer = new Timer();
 timer.schedule(new TimerTask() {
@Override
public void run() {
String terminalNo = wosPlayerApp.config.GetStringDefualt("terminalNo","00000");
String msg = "123:" + terminalNo;
wosPlayerApp.sendMsgToServer(msg);
}
},5000);
 */

public class activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //开启通讯服务
        wosPlayerApp.startCommunicationService();



    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wosPlayerApp.stopCommunicationService(); //关闭服务
    }


}
