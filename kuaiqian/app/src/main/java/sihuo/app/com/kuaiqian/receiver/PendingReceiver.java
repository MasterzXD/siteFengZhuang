package sihuo.app.com.kuaiqian.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hzzh.wawaji.Constant;
import com.hzzh.wawaji.MainActivity;
import com.hzzh.wawaji.R;

import cn.jpush.android.api.JPushInterface;

public class PendingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("----MyReceiver", "PendingReceiver");
        String content = intent.getExtras().getString(JPushInterface.EXTRA_MESSAGE);
        if(!content.startsWith("catch_success:")){
            sendNotification(context,content);
        }
    }

    private void sendNotification(Context context, String message){
        NotificationManager notifyMgr= (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.icon)
                .setTicker(message)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setContentIntent(pi)
                .getNotification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL; //点击后删除，如果是FLAG_NO_CLEAR则不删除，FLAG_ONGOING_EVENT用于某事正在进行，例如电话，具体查看参考。
        notifyMgr.notify(Constant.JPUSH_NOTICE_ID, notification);
    }
}
