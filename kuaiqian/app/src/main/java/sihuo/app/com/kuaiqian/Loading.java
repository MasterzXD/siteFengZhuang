package sihuo.app.com.kuaiqian;

import android.content.Intent;
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
    private ViewPager viewPager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        viewPager = findViewById(R.id.viewpager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this.getApplicationContext(), TBSService.class));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getResources().getBoolean(R.bool.need_guide)){
                    showGuide();
                }else{
                    startActivity(new Intent(Loading.this,BaseActivity.class));
                    finish();
                }
            }
        },getResources().getInteger(R.integer.loading_delay));
    }

    void showGuide(){
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return false;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                return super.instantiateItem(container, position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);
            }
        });
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
