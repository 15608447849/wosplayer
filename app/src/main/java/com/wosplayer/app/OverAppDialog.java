package com.wosplayer.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;

import com.wosplayer.command.kernal.CommandStore;
import com.wosplayer.command.operation.interfaces.CommandType;
import com.wosplayer.command.operation.other.Command_PASD;

/**
 * Created by user on 2016/10/8.
 */
public class OverAppDialog {

//    private static EditText passwordInput = null;
//    private static AlertDialog alertDialog = null;

    public static void ShowDialog(final Activity m){

        final EditText passwordInput = new EditText(m);
        passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
             new AlertDialog.Builder(m)
                    .setTitle(AppUtils.getLocalIpAddress()+" 输入管理密码将结束应用")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(passwordInput).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //输入的密码
                            String inputPassword = passwordInput.getText().toString();
                            //获取密码
                            String localPassword = Command_PASD.getUnlockPassword(m);
                            if (localPassword.equals(inputPassword)){
                                //关闭
                                CommandStore.getInstands().opration(CommandType.SHDP,"local");
                            }else{
                                AppUtils.LongToals(m,"密码错误,请联系客服或登陆后台(终端管理-电子屏-修改)查看");
                            }
                        }
                    })
                    /*.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })*/.show();

    }


    public static void popWind(final Activity context,final String s,final int time) {
        if (context==null) return;
       context.runOnUiThread(new Runnable() {
           @Override
           public void run() {
               showProgressDialog(context,s,time,1000);
           }
       });
    }

    public static void showProgressDialog(Context context, String s, final int max, final int stup) {
    /* @setProgress 设置初始进度
     * @setProgressStyle 设置样式（水平进度条）
     * @setMax 设置进度最大值
     */
        final ProgressDialog progressDialog =  new ProgressDialog(context);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgress(0);
        progressDialog.setTitle(s);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(max);
        progressDialog.show();
    /* 模拟进度增加的过程
     * 新开一个线程，每个1000ms，进度增加1
     */
        new Thread(new Runnable() {
            @Override
            public void run() {
                int progress= 0;
                while (progress < max){
                    try {
                        Thread.sleep(stup);
                        progress++;
                        progressDialog.setProgress(progress);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                // 进度达到最大值后，窗口消失
                progressDialog.cancel();
            }
        }).start();
    }






}
