package sihuo.app.com.kuaiqian;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YinDaoActivity extends Activity{
    LinearLayout rootView;
    ViewPager viewpager;
    List<Bitmap> cache = new ArrayList<>();
    List<ImageView> views = new ArrayList<>();
    final String FOLDER_NAME = "guide";
    boolean isOpen,isPress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yin_dao);
        rootView = findViewById(R.id.root_layout);
        viewpager = findViewById(R.id.viewpager);

        try {
            String []files = getAssets().list(FOLDER_NAME);
            Arrays.sort(files);
            for (int i = 0; i < files.length ; i++) {
                cache.add(getImageFromAssetsFile(FOLDER_NAME+"/"+files[i]));
                ImageView imageView = new ImageView(YinDaoActivity.this);
                imageView.setImageBitmap(cache.get(i));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                views.add(imageView);
                if(i==files.length-1){
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(YinDaoActivity.this,BaseActivity.class));
                            finish();
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewpager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return cache.size();
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                ImageView imageView = views.get(position);
                container.addView(imageView);
                return imageView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view==object;
            }

        });
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d("addOnPageChangeListener", positionOffset+"onPageScrolled: "+positionOffsetPixels);
                if (views.size() - 1 == position && isPress &&positionOffsetPixels == 0 && !isOpen) {
                    isOpen = true;
                    startActivity(new Intent(YinDaoActivity.this, BaseActivity.class));
                    finish();
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    isPress = true;
                } else {//必须写else，不然的话，倒数第二页就开始自动跳转了
                    isPress = false;
                }
            }
        });
    }


    private Bitmap getImageFromAssetsFile(String fileName)
    {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();
        try
        {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return image;
    }

    @Override
    protected void onDestroy() {
        for (Bitmap b :cache) {
            if(b!=null){
                b.recycle();
            }
        }
        super.onDestroy();
    }
}
