package sihuo.app.com.kuaiqian;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.google.zxing.Result;
import com.tencent.smtt.export.external.interfaces.ClientCertRequest;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.HttpAuthHandler;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebStorage;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import sihuo.app.com.kuaiqian.utils.CheckUpdate;
import sihuo.app.com.kuaiqian.utils.NewWindowView;
import sihuo.app.com.kuaiqian.utils.WebViewJavaScriptFunction;
import sihuo.app.com.kuaiqian.utils.X5WebView;
import sihuo.app.com.kuaiqian.zxing.DecodeImage;

public class MainActivity extends Activity {
    final  int FILE_CHOOSER_RESULT_CODE = 40;
    final  int FILE_CHOOSER_CAMERA = 22;
    final  int QUCODE_REQUEST = 11;
    ValueCallback<Uri[]> uploadMessage;
    X5WebView webview;
    ImageView floatHome,floatBack;
    TextView back,refresh,goForward,closeAp,home,shareBtn,moreBtn,youhui,kefu,loadview;
    String HOME;
    SwipeRefreshLayout refreshLayout;
    LinearLayout floatLayout;
    RelativeLayout rootView,titleLayout;
    RelativeLayout.LayoutParams floatParams;
    PageLoadingDialog loadingDialog;
    FrameLayout customViewLayout;

    String imageUrl;
    int screenW,screenH;
    float density;
    boolean refreshable,hasDaoHang,showLoading,guestureNavigation,floatNavigation;
    LinearLayout sliderViewLayout;
    ViewStub vs;
    int statusBarHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        if (!this.isTaskRoot()) { // 判断当前activity是不是所在任务栈的根
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        setContentView(R.layout.temp);
        vs = findViewById(R.id.viewStub);
        loadingDialog = new PageLoadingDialog(this);
        if(getResources().getInteger(R.integer.loading_delay)==0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    vs.inflate();
                    loadHome();

                }
            },100);

        }else{
            new LoadingDialog(this).showWithCallBack(new LoadingDialog.HideCallBack(){
                @Override
                public void onHide() {
                    doCallback();
                }
            });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    vs.inflate();
                    initView();
                }
            },500);
        }
    }

    void doCallback(){
        showLoading = getResources().getBoolean(R.bool.show_loadng);
        if(getResources().getBoolean(R.bool.save_daohang)){//保留导航
            if(!getResources().getBoolean(R.bool.full_screen)){
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                if(!getPackageName().equals("com.adgfsdf.weinisi")){
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) titleLayout.getLayoutParams();
                    //获取status_bar_height资源的ID
                    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                    if (resourceId > 0) {
                        //根据资源ID获取响应的尺寸值
                        statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                    }
                    params.topMargin = statusBarHeight==0?50:statusBarHeight;
                    titleLayout.setLayoutParams(params);
                }
            }
        }else{
            if(!getResources().getBoolean(R.bool.full_screen)){
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) titleLayout.getLayoutParams();
                //获取status_bar_height资源的ID
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    //根据资源ID获取响应的尺寸值
                    statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                }
                params.height = statusBarHeight==0?50:statusBarHeight;
                titleLayout.setLayoutParams(params);
            }else{
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) titleLayout.getLayoutParams();
                params.height=0;
                titleLayout.setLayoutParams(params);
            }
        }
        CheckUpdate.getInstance().check(MainActivity.this, new CheckUpdate.CheckUpdateCallBack() {
            @Override
            public void onResult(boolean update,String newVersion ,String url) {
                if(update){
                    new UpdateDialog(MainActivity.this,"New version "+newVersion+" detected\nUpdate Now？",url).show();
                }
            }
        });
    }

    void loadHome(){
        initConfig();
        HOME = getResources().getString(R.string.start_url);
        webview = findViewById(R.id.webview);
        webview.loadUrl(HOME);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initView();
                doCallback();
            }
        },500);
    }

    void initView(){
        DisplayMetrics dm = getResources().getDisplayMetrics();
        density = dm.density;
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;


        getWindow().setFormat(PixelFormat.TRANSLUCENT);


        findViewById(R.id.title_layout).setVisibility(hasDaoHang?View.VISIBLE:View.INVISIBLE);


        back = findViewById(R.id.back);
        home = findViewById(R.id.home);
        refresh = findViewById(R.id.refresh);
        rootView = findViewById(R.id.root_view);
        shareBtn = findViewById(R.id.share);
        moreBtn = findViewById(R.id.more);
        goForward = findViewById(R.id.go_forward);
        closeAp = findViewById(R.id.close_app);
        titleLayout = findViewById(R.id.title_layout);
        youhui = findViewById(R.id.youhui);
        kefu = findViewById(R.id.kefu);
        loadview = findViewById(R.id.loadview);

        youhui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webview.loadUrl("http://313224.com");
            }
        });

        kefu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this).setMessage("确认清除缓存?").setPositiveButton("取消",null).setNegativeButton("清除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,"清除缓存成功！",Toast.LENGTH_SHORT).show();
                    }
                }).show();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sliderViewLayout==null){
                    sliderViewLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.slider_menu_layout,null);
                    LinearLayout sublayout = (LinearLayout)sliderViewLayout.getChildAt(2);
                    sliderViewLayout.getChildAt(0).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideMenu();
                        }
                    });
                    for (int i = 0;i< sublayout.getChildCount();i++){
                        View view = sublayout.getChildAt(i);
                        if(view instanceof TextView){
                            view.setTag(i);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    switch ((int)v.getTag()){
                                        case 0:
                                            webview.loadUrl("http://2017.kq444.net/az");
                                            break;
                                        case 2:
                                            webview.loadUrl("http://2017.kq444.net/cz");
                                            break;
                                        case 4:
                                            webview.loadUrl("http://2017.kq444.net/jc");
                                            break;
                                        case 6:
                                            webview.loadUrl("http://2017.kq444.net/hb");
                                            break;
                                        case 8:
                                            shareWebLink("http://kq333.net");
                                            break;
                                        case 10:
                                            new AlertDialog.Builder(MainActivity.this).setMessage("确认清除缓存?").setPositiveButton("取消",null).setNegativeButton("清除", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(MainActivity.this,"清除缓存成功！",Toast.LENGTH_SHORT).show();
                                                }
                                            }).show();
                                            break;
                                    }
                                    hideMenu();
                                }
                            });
                        }
                    }

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenW,screenH);
                    params.leftMargin = screenW;
                    rootView.addView(sliderViewLayout, params);
                }
                showMenu();
            }
        });

        if(shareBtn!=null){
            shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareWebLink(webview.getUrl());
                }
            });
        }
        refreshLayout = findViewById(R.id.refesh_layout);
        refreshLayout.setEnabled(refreshable);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webview.reload();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(webview.canGoBack()){
                    webview.goBack();
                }
            }
        });

        closeAp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HOME = getResources().getString(R.string.home_url);
                webview.loadUrl(HOME);
            }
        });

        goForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(webview.canGoForward()){
                    webview.goForward();
                }
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webview.reload();
            }
        });
        setupWebview();

        setInitScale();

        initFloatView();
    }

    void showMenu(){
        TranslateAnimation translateAnimation = new TranslateAnimation(screenW,0,0,0);
        translateAnimation.setDuration(100);
        translateAnimation.setFillAfter(true);
        sliderViewLayout.startAnimation(translateAnimation);
        sliderViewLayout.postDelayed(new Runnable() {
            @Override
            public void run() {

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenW,screenH);
                params.leftMargin = 0;
                params.topMargin = statusBarHeight;
                sliderViewLayout.setLayoutParams(params);
            }
        },100);

    }

    void hideMenu(){
        TranslateAnimation translateAnimation = new TranslateAnimation(0,screenW,0,0);
        translateAnimation.setDuration(500);
        translateAnimation.setFillAfter(true);
        sliderViewLayout.startAnimation(translateAnimation);
        sliderViewLayout.postDelayed(new Runnable() {
            @Override
            public void run() {

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenW,screenH);
                params.leftMargin = screenW;
                params.rightMargin = -screenW;
                sliderViewLayout.setLayoutParams(params);
            }
        },600);

    }

    /**
     *
     */
    void shareWebLink(String link){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,getResources().getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT,link);
        startActivity(Intent.createChooser(intent,getResources().getString(R.string.app_name)));
    }

    /**
     * 设置webview初始化缩放比例，因为某些机型内容不适配,比如三星曲屏
     */
    void setInitScale(){
//        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
//        int width = wm.getDefaultDisplay().getWidth();
//        Log.e("----MainActivity", "setupWebview:" + width);
//        webview.setInitialScale(50);

    }

    int floatViewDownX,floatViewDownY,finalFloatViewDownX,finalFloatViewDownY;
    void initFloatView(){
        if(floatNavigation){
            floatLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.float_layout,null);
            final int floatViewW = (int)(40*density);
            final int floatViewH = (int)(100*density);
            floatParams = new RelativeLayout.LayoutParams(floatViewW,floatViewH);
            floatParams.leftMargin =  (int)(270*density);
            floatParams.topMargin = (int)(300*density);
            rootView.addView(floatLayout,floatParams);

            floatBack = floatLayout.findViewById(R.id.float_back);
            floatHome = floatLayout.findViewById(R.id.float_home);
            floatLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            floatViewDownX = (int)event.getRawX();
                            floatViewDownY = (int)event.getRawY();
                            finalFloatViewDownX = (int)event.getRawX();
                            finalFloatViewDownY = (int)event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:

                            int floatViewCurrentX = (int)event.getRawX();
                            int floatViewCurrentY = (int)event.getRawY();
                            if(Math.abs(floatViewCurrentX-finalFloatViewDownX)<10
                                    && Math.abs(floatViewCurrentY-finalFloatViewDownY)<10){
                                return true;
                            }
                            floatParams.leftMargin += floatViewCurrentX-floatViewDownX;
                            floatParams.topMargin += floatViewCurrentY-floatViewDownY;

                            if(floatParams.leftMargin<0){
                                floatParams.leftMargin=0;
                            }
                            if(floatParams.topMargin<0){
                                floatParams.topMargin=0;
                            }
                            if(floatParams.leftMargin+floatViewW>screenW){
                                floatParams.leftMargin = screenW-floatViewW;
                            }
                            if(floatParams.topMargin+floatViewH+22>screenH){
                                floatParams.topMargin = screenH-floatViewH-22;
                            }
                            floatViewDownX = floatViewCurrentX;
                            floatViewDownY = floatViewCurrentY;
                            rootView.updateViewLayout(floatLayout,floatParams);
                            break;
                        case MotionEvent.ACTION_UP:
                            if(Math.abs(event.getRawY()-finalFloatViewDownY)<10){
                                if(floatHome.getY()+floatHome.getHeight()>=event.getY()){
                                    HOME = getResources().getString(R.string.home_url);
                                    webview.loadUrl(HOME);
                                }else{
                                    if(webview.canGoBack()){
                                        webview.goBack();
                                    }
                                }
                            }
                            floatLayout.setLayoutParams(floatParams);
                            break;
                    }
                    return true;
                }
            });
        }
    }

    /**
     * 判断是否为二维码
     * param url 图片地址
     * return
     */
    private boolean decodeImage(String sUrl){
        Result result = DecodeImage.handleQRCodeFormBitmap(getBitmap(sUrl));

        return result != null;
    }

    /**
     * 根据地址获取网络图片
     * @param sUrl 图片地址
     * @return
     * @throws IOException
     */
    public Bitmap getBitmap(String sUrl){
        try {
            URL url = new URL(sUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if(conn.getResponseCode() == 200){
                InputStream inputStream = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mmbitmap = bitmap;
                return bitmap;
            }else{
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"请检查网络！",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * bitmap 保存为jpg 图片
     * @param mBitmap 图片源
     * @param bitName  图片名
     */
    public void saveMyBitmap(Bitmap mBitmap,String bitName)  {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){}
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 30);
        }else{
            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"存储卡不可用",Toast.LENGTH_SHORT).show();
                    }
                });

                return;
            }
            File path = new File( Environment.getExternalStorageDirectory()+"/DCIM/Camera");
            if(!path.exists()){
                path.mkdirs();
            }
            File file= new File( path,bitName + ".jpg");
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if(mBitmap==null){
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"没有获取到图片！",Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//根据请求是否通过的返回码进行判断，然后进一步运行程序
        if (grantResults.length > 0 && requestCode == 29 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveMyBitmap(mBitmap,""+System.currentTimeMillis());
        }else  if (grantResults.length > 0 && requestCode == 30 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveMyBitmap(mmbitmap,""+System.currentTimeMillis());
        }

    }
    Bitmap mmbitmap;
    byte[] mBitmap;

    /**
     * bitmap 保存为jpg 图片
     * @param mBitmap 图片源
     * @param bitName  图片名
     */
    public void saveMyBitmap(byte[] mBitmap,String bitName)  {
//        int i =ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        Toast.makeText(MainActivity.this,"checkSelfPermission:"+i,Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 29);
        }//REQUEST_EXTERNAL_STRONGE是自定义个的一个对应码，用来验证请求是否通过

        else {
            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                Toast.makeText(MainActivity.this,"存储卡不可用",Toast.LENGTH_SHORT).show();
                return;
            }
            File path = new File( Environment.getExternalStorageDirectory()+"/DCIM/Camera");
            if(!path.exists()){
                path.mkdirs();
            }
            File file= new File( path,bitName + ".jpg");
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);
                fOut.write(mBitmap,0,mBitmap.length);
                fOut.flush();
            } catch (FileNotFoundException e) {
                Toast.makeText(MainActivity.this,"FileNotFoundException",Toast.LENGTH_SHORT).show();
            } catch (IOException ex){
                Toast.makeText(MainActivity.this,"IOException:"+ ex.getMessage(),Toast.LENGTH_SHORT).show();
            }
            try {
                if(fOut!=null){
                    fOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
//                MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), bitName, "description");
                //保存图片后发送广播通知更新数据库
//                Uri uri = Uri.fromFile(file);
                MediaScannerConnection.scanFile(this,
                        new String[] { file.getAbsolutePath() }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        });
//                MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

            }catch (Exception e){

            }
            Toast.makeText(MainActivity.this,"正在保存...",Toast.LENGTH_LONG).show();
//            Toast.makeText(MainActivity.this,"正在保存...",Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"成功保存图片到相册，请前往相册查看！",Toast.LENGTH_SHORT).show();
                }
            },5000);
        }


    }

    public class MyAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.this,"正在保存...",Toast.LENGTH_LONG).show();
            MediaScannerConnection.scanFile(MainActivity.this,
                    new String[] { s }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {

                        }
                    });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"成功保存图片到相册，请前往相册查看！",Toast.LENGTH_SHORT).show();
                }
            },5000);
        }

        @Override
        protected String doInBackground(String... params) {
            String name=System.currentTimeMillis()+"";
            File file= new File( Environment.getExternalStorageDirectory()+"/DCIM/Camera/"+name + ".jpg");
            saveMyBitmap(getBitmap(params[0]),name);
            return file.getAbsolutePath();
        }
    }

    int startX;
    final int scrollSize=200;
    String tempUrl;

    void setupWebview(){
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        webview.setmCallBack(new X5WebView.LongClickCallBack() {
            @Override
            public void onLongClickCallBack(final String imgUrl) {

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(MainActivity.this).setTitle("").setNegativeButton("保存图片到相册", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(imgUrl.startsWith("data:image/png;base64,")){
                                    String tempimgUrl = imgUrl.replace("data:image/png;base64","");
                                    MainActivity.this.mBitmap = Base64.decode(tempimgUrl,Base64.DEFAULT);
                                    saveMyBitmap(MainActivity.this.mBitmap,""+System.currentTimeMillis());
//                            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
//                            Result result = DecodeImage.handleQRCodeFormBitmap(bitmap);
//                            if(result!=null){
//                                Log.e("----onLongClickCallBack", ""+result.getText());
//                                webview.loadUrl(result.getText());
//                            }
                                }else if(imgUrl.startsWith("http")){
                                    imageUrl=imgUrl;
                                    // 获取到图片地址后做相应的处理
                                    MyAsyncTask	mTask = new MyAsyncTask();
                                    mTask.execute(imgUrl);
                                }
                            }
                        }).show();
                    }
                });
            }
        });
        if(refreshable){
            webview.setScrollChange(new X5WebView.ScrollChange() {
                @Override
                public void onScrollChanged(int l, int t, int oldl, int oldt) {
                    Log.e("----MainActivity", "onScrollChanged:" + t);
                    if(t==0){
                        refreshLayout.setEnabled(true);
                    }else{
                        refreshLayout.setEnabled(false);
                    }
                }
            });
        }
        if(guestureNavigation){

            webview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(guestureNavigation){
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startX = (int) event.getX();
                                break;
                            case MotionEvent.ACTION_UP:

                                int endX = (int) event.getX();
                                if (endX > startX && webview.canGoBack() && endX - startX > scrollSize) {
                                    webview.goBack();
                                } else if (endX < startX && webview.canGoForward() && startX - endX > scrollSize) {
                                    webview.goForward();
                                }
                                break;
                            default:
                                break;
                        }
                    }

                    return false;
                }
            });
        }

        webview.addJavascriptInterface(new WebViewJavaScriptFunction() {

            @Override
            public void onJsFunctionCalled(String tag) {

            }

            @JavascriptInterface
            public void toastMessage(String msg) {

            }
        }, "control");

        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
                jsResult.confirm();
                return super.onJsAlert(webView, s, s1, jsResult);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.e("----MainActivity", "onConsoleMessage:" );
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onReceivedTouchIconUrl(WebView webView, String s, boolean b) {
                super.onReceivedTouchIconUrl(webView, s, b);
                Log.e("----MainActivity", "onReceivedTouchIconUrl:" );
            }

            @Override
            public void onReachedMaxAppCacheSize(long l, long l1, WebStorage.QuotaUpdater quotaUpdater) {
                super.onReachedMaxAppCacheSize(l, l1, quotaUpdater);
                Log.e("----MainActivity", "onReachedMaxAppCacheSize:" );
            }

            @Override
            public boolean onJsBeforeUnload(WebView webView, String s, String s1, JsResult jsResult) {
                Log.e("----MainActivity", "onJsBeforeUnload:");
                return super.onJsBeforeUnload(webView, s, s1, jsResult);
            }

            @Override
            public boolean onJsPrompt(WebView webView, String s, String s1, String s2, JsPromptResult jsPromptResult) {
                Log.e("----MainActivity", "onJsPrompt:" );
                return super.onJsPrompt(webView, s, s1, s2, jsPromptResult);
            }

            @Override
            public boolean onJsConfirm(WebView webView, String s, String s1, JsResult jsResult) {
                Log.e("----MainActivity", "onJsConfirm:" );
                return super.onJsConfirm(webView, s, s1, jsResult);
            }

            @Override
            public boolean onJsTimeout() {
                Log.e("----MainActivity", "onJsTimeout:" );
                return super.onJsTimeout();
            }

            @Override
            public boolean onCreateWindow(WebView webView, boolean b, boolean b1, Message message) {
                NewWindowView newview = (NewWindowView) getLayoutInflater().inflate(R.layout.new_window,null);
                rootView.addView(newview,new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                WebView.WebViewTransport transport = (WebView.WebViewTransport) message.obj;//以下的操作应该就是让新的webview去加载对应的url等操作。
                transport.setWebView(newview.x5WebView);
                message.sendToTarget();
                return true;
            }

            @Override
            public void onCloseWindow(WebView webView) {
                super.onCloseWindow(webView);
            }

            @Override
            public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback customViewCallback) {

                customViewLayout = new FrameLayout(MainActivity.this);
                super.onShowCustomView(view, customViewCallback);
            }

            @Override
            public void onShowCustomView(View view, int i, IX5WebChromeClient.CustomViewCallback customViewCallback) {
                Toast.makeText(MainActivity.this,"onShowCustomView11",Toast.LENGTH_LONG).show();
                super.onShowCustomView(view, i, customViewCallback);
            }

            @Override
            public void onProgressChanged(WebView webView, int progress) {
                super.onProgressChanged(webView, progress);
                if(getResources().getBoolean(R.bool.pull_refresh_enable)){
                    if(progress==100){
                        refreshLayout.setRefreshing(false);
                    }
                }
                if(getResources().getBoolean(R.bool.show_loadng)){
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
    //            Log.e("----onShowFileChooser", ""+fileChooserParams);
                uploadMessage = valueCallback;
                openImageChooserActivity();
//                return super.onShowFileChooser(webView, valueCallback, fileChooserParams);
                return true;
            }

        });
        webview.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view,final String url) {
                Uri uri = Uri.parse(url);

                Log.e("----should", ""+url+"__"+tempUrl);

                try {
                    if(url.toLowerCase().startsWith("intent://")){
                        Intent intent = Intent.parseUri(url,Intent.URI_INTENT_SCHEME);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return true;
                    }else if(url.toLowerCase().startsWith("saoyisao")){
                        Intent intent = new Intent(MainActivity.this,CaptureActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.setData(Uri.parse(url));
                        startActivityForResult(intent,QUCODE_REQUEST);
                        return true;
                    }
                    else if(!url.toLowerCase().startsWith("http")){
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }else if(url.toLowerCase().contains("https://qr.alipay.com") ){
                        int index = url.toLowerCase().indexOf("https://qr.alipay.com");
                        String newUrl = url.substring(index);
                        view.loadUrl(newUrl);
                        return true;
                    }
                    else if(url.toLowerCase().contains("wx.tenpay.com")){
                         view.postDelayed(new Runnable() {
                             @Override
                             public void run() {
                                 Map<String, String> extraHeaders = new HashMap<String, String>();
                                 Log.e("----shouldOverrideU", ""+tempUrl);
                                 if(getPackageName().equalsIgnoreCase("com.dfhtfhdt.xinhaotiandi2")){
                                     tempUrl = "https://zhongxin.junka.com";
                                 }
                                 extraHeaders.put("Referer", tempUrl);
                                 view.loadUrl(url, extraHeaders);
                             }
                         },1000);

                        return true;
                    }
                    tempUrl = url;
//                    Map<String, String> extraHeaders = new HashMap<String, String>();
//                    extraHeaders.put("Referer", tempUrl);
//                    view.loadUrl(url,extraHeaders);
                    view.loadUrl(url);
                    return true;
                }catch (Exception e){
//                    Log.e("----should--error", ""+e.getMessage());
                    Toast.makeText(MainActivity.this, "无法打开指定应用，请先确认应用是否安装！", Toast.LENGTH_SHORT).show();
                }
                return super.shouldOverrideUrlLoading(view,url);

            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest, Bundle bundle) {
                Log.e("----MainActivity", "shouldInterceptRequest22222222:" +webResourceRequest.getMethod()
                        +"\n__"+webResourceRequest.getRequestHeaders()
                        +"\n__"+webResourceRequest.getMethod()
                        +"\n__"+webResourceRequest.getUrl());

                return super.shouldInterceptRequest(webView, webResourceRequest, bundle);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                Log.e("----Request", ""+url);
                Log.e("----MainActivity", "shouldInterceptRequest11111:"+url );
                return super.shouldInterceptRequest(view, url);

            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.e("----MainActivity", "shouldInterceptRequest:" +request.getUrl()
                        +"\n__"+request.getRequestHeaders()
                        +"\n__"+request.getMethod()
                        +"\n__"+request.getUrl());
                return super.shouldInterceptRequest(view, request);
            }
            @Override
            public void onReceivedLoginRequest(WebView webView, String s, String s1, String s2) {
                Log.e("----MainActivity", "onReceivedLoginRequest:" + s);
                super.onReceivedLoginRequest(webView, s, s1, s2);
            }

            @Override
            public void onFormResubmission(WebView webView, Message message, Message message1) {
                Log.e("----MainActivity", "onFormResubmission:" );
                super.onFormResubmission(webView, message, message1);
            }

            @Override
            public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
                super.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
                Log.e("----MainActivity", "onReceivedHttpError:"+webResourceRequest.getRequestHeaders()
                +"\n__"+webResourceRequest.getUrl().getHost()
                        +"\n__"+webResourceResponse.getReasonPhrase()
                        +"\n__"+webResourceResponse.getStatusCode()
                        +"\n__"+webResourceResponse.getResponseHeaders());
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView webView, HttpAuthHandler httpAuthHandler, String s, String s1) {

                super.onReceivedHttpAuthRequest(webView, httpAuthHandler, s, s1);
                Log.e("----MainActivity", "onReceivedHttpAuthRequest:");
            }

            @Override
            public void onLoadResource(WebView webView, String s) {
                super.onLoadResource(webView, s);
                Log.e("----MainActivity", "onLoadResource:" );
            }

            @Override
            public void onReceivedClientCertRequest(WebView webView, ClientCertRequest clientCertRequest) {
                super.onReceivedClientCertRequest(webView, clientCertRequest);
                Log.e("----MainActivity", "onReceivedClientCertRequest:" );
            }

            @Override
            public void onScaleChanged(WebView webView, float v, float v1) {
                Log.e("----MainActivity", "onScaleChanged:");
                super.onScaleChanged(webView, v, v1);
            }


            @Override
            public void onTooManyRedirects(WebView webView, Message message, Message message1) {
                super.onTooManyRedirects(webView, message, message1);
                Log.e("----MainActivity", "onTooManyRedirects:");
            }

            @Override
            public void onDetectedBlankScreen(String s, int i) {
                super.onDetectedBlankScreen(s, i);
                Log.e("----MainActivity", "onDetectedBlankScreen:");
            }





            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e("----onReceivedError", ""+error.getErrorCode());
            }

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                sslErrorHandler.proceed();
//                super.onReceivedSslError(webView, sslErrorHandler, sslError);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
//                Log.e("----onReceivedError", "failingUrl:"+failingUrl);
            }

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);
                webView.getSettings().setBlockNetworkImage(false);
                if(showLoading){
                    loadingDialog.show();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                Log.e("----onPageFinished", ""+url);
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadview.setVisibility(View.GONE);
                    }
                },800);

                if(showLoading){
                    loadingDialog.dismiss();
                }
            }
        });

    }


    void initConfig(){
        refreshable = getResources().getBoolean(R.bool.pull_refresh_enable);
        hasDaoHang = getResources().getBoolean(R.bool.save_daohang);
        guestureNavigation = getResources().getBoolean(R.bool.gesture_navigation);
        floatNavigation = getResources().getBoolean(R.bool.float_navigation);
    }

    private void openImageChooserActivity() {
        new AlertDialog.Builder(MainActivity.this).setMessage("请选择方式")
                .setNegativeButton("相机", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            //根据路径实例化图片文件
                            File photoFile=new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"temp.jpg");
                            //设置拍照后图片保存到文件中
                            intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photoFile));
                            //启动拍照activity并获取返回数据
                            startActivityForResult(intent,FILE_CHOOSER_CAMERA);
                        }catch (Exception e){
                            Toast.makeText(MainActivity.this,"请打开相机权限",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setPositiveButton("图库", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i,
                        "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
            }
            }).show();
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 启动系统相机
//        startActivityForResult(intent, 444);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(new Uri[]{result});
                uploadMessage = null;
            }
        }else if (requestCode == FILE_CHOOSER_CAMERA) {
//            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"temp.jpg");
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(new Uri[]{Uri.fromFile(photoFile)});
                uploadMessage = null;
            }
        }else if (requestCode == QUCODE_REQUEST) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    long mills = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(getResources().getBoolean(R.bool.can_goback)&& webview.canGoBack()){
//            if(getResources().getBoolean(R.bool.can_goback) && !webview.getUrl().equals(HOME) && webview.canGoBack()){
                webview.goBack();
                return true;
            }
            if(System.currentTimeMillis()-mills>1000){
                Toast.makeText(this,getString(R.string.exit),Toast.LENGTH_SHORT).show();
                mills = System.currentTimeMillis();
            }else{
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            webview.onResume();
        }catch (Exception e){

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            webview.onPause();
        }catch (Exception e){

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}

