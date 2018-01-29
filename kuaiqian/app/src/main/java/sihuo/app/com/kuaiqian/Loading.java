package sihuo.app.com.kuaiqian;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import sihuo.app.com.kuaiqian.service.TBSService;

/**
 * Created by 李宏阳 on 2018/1/22.
 */

public class Loading extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);




    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this.getApplicationContext(), TBSService.class));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Loading.this,BaseActivity.class));
                finish();
            }
        },getResources().getInteger(R.integer.loading_delay));
    }
}
