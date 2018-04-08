package sihuo.app.com.kuaiqian;

import android.app.Application;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;


/**
 * Created by Administrator on 2017/11/14.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        UMConfigure.setLogEnabled(true);
//        UMConfigure.init(getApplicationContext(), UMConfigure.DEVICE_TYPE_PHONE,"");
//        MobclickAgent.setScenarioType(getApplicationContext(), MobclickAgent.EScenarioType.E_UM_NORMAL);
    }

}
