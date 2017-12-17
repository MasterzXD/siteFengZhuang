package sihuo.app.com.kuaiqian;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;

/**
 * Created by Administrator on 2017/11/14.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initTBS();
    }

    /**
     * 初始化TBS浏览服务X5内核
     */
    private void initTBS() {
        Intent intent = new Intent(this, AdvanceLoadX5Service.class);
        startService(intent);
    }
}
