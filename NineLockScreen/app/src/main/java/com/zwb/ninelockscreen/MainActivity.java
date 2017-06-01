package com.zwb.ninelockscreen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView tvPwdSet, tvPwdGet;
    private NineLockScreenView nlc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvPwdSet = (TextView) findViewById(R.id.tvPwdSet);
        tvPwdGet = (TextView) findViewById(R.id.tvPwdGet);
        nlc = (NineLockScreenView) findViewById(R.id.nlc);
        nlc.setOnCallBack(new NineLockScreenView.OnCallBack() {
            @Override
            public void result(String password, boolean isRight) {
                if (isRight) {
                    tvPwdGet.setText("密码正确:" + password);
                } else {
                    tvPwdGet.setText("密码错误:" + password);
                }
            }
        });
        resetPwd();
    }

    public void reset(View view) {
        resetPwd();
    }

    private void resetPwd() {
        tvPwdGet.setText("");
        tvPwdSet.setText("");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int k = new Random().nextInt(8);
            if (!builder.toString().contains(k + "")) {
                builder.append(k + "");
            }
        }
        tvPwdSet.setText(builder.toString());
        nlc.setPassword(builder.toString());
    }
}
