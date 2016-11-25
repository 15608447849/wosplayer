package com.wosplayer.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.Toast;

import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_Close_App;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_PASD;

/**
 * Created by user on 2016/10/8.
 */
public class inputPassWordDialog {

//    private static EditText passwordInput = null;
//    private static AlertDialog alertDialog = null;

    public static void ShowDialog(final Activity m){

        final EditText passwordInput = new EditText(m);
        passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
             new AlertDialog.Builder(m)
                    .setTitle(WosApplication.getLocalIpAddress()+" 输入管理密码将结束应用")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(passwordInput).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //获取密码
                            String localPassword = Command_PASD.getUnlockPassword();
                            if (localPassword==null || localPassword.equals("")){
                                Toast.makeText(m,"未找到本地密码,无法匹配",Toast.LENGTH_LONG).show();
                                return;
                            }
                            //输入的密码
                            String inputPassword = passwordInput.getText().toString();

                            if (inputPassword.equals(localPassword)){
                                //关闭
                                new Command_Close_App().Execute("true");
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();

    }



}
