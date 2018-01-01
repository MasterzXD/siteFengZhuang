package sihuo.app.com.kuaiqian;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;

/**
 * Created by 李宏阳 on 2017/11/28.
 */

public class AdvanceLoadX5Service extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        initX5();
    }


    private void initX5() {
        //  预加载X5内核
        QbSdk.initX5Environment(getApplicationContext(), cb);
    }


    QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {


        @Override
        public void onViewInitFinished(boolean arg0) {
            Log.e("----onViewInitFinished", ""+arg0);
            //初始化完成回调
        }


        @Override
        public void onCoreInitFinished() {
            Log.d("----X5Service", "onCoreInitFinished:" );
            // TODO Auto-generated method stub
        }
    };
}
