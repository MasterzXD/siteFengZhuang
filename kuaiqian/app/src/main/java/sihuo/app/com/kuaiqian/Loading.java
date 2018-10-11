package sihuo.app.com.kuaiqian;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import sihuo.app.com.kuaiqian.service.TBSService;

/**
 * Created by 李宏阳 on 2018/1/22.
 */

public class Loading extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        setContentView(R.layout.activity_loading);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this.getApplicationContext(), TBSService.class));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = getSharedPreferences("first",MODE_PRIVATE);
                if(getResources().getBoolean(R.bool.need_guide)){
//                if(getResources().getBoolean(R.bool.need_guide) && sp.getBoolean("first_install",true)){
//                    showGuide();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("first_install",false);
                    editor.commit();
                    startActivity(new Intent(Loading.this,YinDaoActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(Loading.this,BaseActivity.class));
                    finish();
                }
            }
        },getResources().getInteger(R.integer.loading_delay));
    }

    @Override
    protected void onDestroy() {
        releaseImageViewResouce((ImageView) findViewById(R.id.loading));
        super.onDestroy();
    }

    public void releaseImageViewResouce(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }
}
