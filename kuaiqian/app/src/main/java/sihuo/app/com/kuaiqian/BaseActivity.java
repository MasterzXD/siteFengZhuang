package sihuo.app.com.kuaiqian;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.webkit.ClientCertRequest;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import sihuo.app.com.kuaiqian.utils.ADFilterTool;
import sihuo.app.com.kuaiqian.utils.NewWindowView;
import sihuo.app.com.kuaiqian.utils.Share;
import sihuo.app.com.kuaiqian.utils.WebViewJavaScriptFunction;
import sihuo.app.com.kuaiqian.utils.X5WebView;

import static sihuo.app.com.kuaiqian.utils.FileUtils.getRealPathByUri;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    final boolean DEBUG_ALL = true;
    final boolean DEBUG = true;
    final int FILE_CHOOSER_RESULT_CODE = 40;
    final int FILE_CHOOSER_CAMERA = 22;
    final int QUCODE_REQUEST = 11;

    /*手势导航参数*/
    private int startX;
    private final int SCROLL_SIZE = 200;

    private X5WebView x5WebView;
    private TextView errorNotice;
    private String HOME;
    private RelativeLayout rootView, titleLayout;
    private LinearLayout sliderViewLayout;
    private SwipeRefreshLayout refeshLayout;
    private DrawerLayout drawerLayout;
    private FrameLayout sliderMenuParent;



    private ValueCallback<Uri[]> uploadMessage;
    private ValueCallback<Uri> singleUploadMessage;

    private TextView back, refresh, goForward, closeAp, home, shareBtn, moreBtn, youhui, kefu, loadview, xiazhu, zhibo;
    /*float navigation*/
    private LinearLayout floatLayout;
    private RelativeLayout.LayoutParams floatParams;
    private ImageView floatHome, floatBack;
    private int screenW, screenH;
    private float density;
    private FrameLayout topNavi, bottomNavi;

    private ProgressBar progressBarH;
    private boolean refreshable, hasDaoHang, guestureNavigation, fullScreen, floatNavigation, bottomNavigation, rightSliderMenu,hasguide;
    private int loadingTime, statusBarHeight;
    private ImageView loadingImage;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setContentView(R.layout.activity_base);

        initConfig();
        x5WebView = findViewById(R.id.x5webview);
        errorNotice = findViewById(R.id.errorNotice);
        loadingImage = findViewById(R.id.loadingImage);
        refeshLayout = findViewById(R.id.refesh_layout);
        rootView = findViewById(R.id.root_view);
        drawerLayout = findViewById(R.id.drawerLayout);
        sliderMenuParent = findViewById(R.id.slider_parent);
        progressBarH = findViewById(R.id.progressBar);
        progressBarH.setMax(100);

        refeshLayout.setEnabled(refreshable);
        if (refreshable) {
            refeshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    x5WebView.reload();
                }
            });
        }
        if (loadingTime == 0) {
            initFloatNavigation();
            if (!fullScreen) {
                fullscreenNo();
            }
        } else {
            loadingImage.setVisibility(View.VISIBLE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initFloatNavigation();
                    if (!fullScreen) {
                        fullscreenNo();
                    }
                    if(hasguide && getSharedPreferences("config",MODE_PRIVATE).getBoolean("isfirst",true)){
//                    if(hasguide){
                        SharedPreferences.Editor editor = getSharedPreferences("config",MODE_PRIVATE).edit();
                        editor.putBoolean("isfirst",false);
                        editor.commit();
                        startActivity(new Intent(BaseActivity.this,YinDaoActivity.class));
                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingImage.setVisibility(View.INVISIBLE);
                        }
                    },500);

                }
            }, loadingTime);
        }
        Log.d("----BaseActivity", "onCreate:开始timmer" );

        loadHome();
        setupWebview();
        if (hasDaoHang) {
            topNavi = findViewById(R.id.topNavi);
            bottomNavi = findViewById(R.id.bottomNavi);
            View view = getLayoutInflater().inflate(R.layout.title_layout, null);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (44 * density));
            if (bottomNavigation) {
                bottomNavi.addView(view, params);
                if (!fullScreen) {
                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) refeshLayout.getLayoutParams();
                    params1.topMargin = statusBarHeight;
                    refeshLayout.setLayoutParams(params1);
                }
            } else {
                topNavi.addView(view, params);
                if (!fullScreen) {
                    RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams) topNavi.getLayoutParams();
                    layout.topMargin = statusBarHeight;
                    topNavi.setLayoutParams(layout);
                }
            }
            loadTitle();
        } else {
            if (!fullScreen) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) refeshLayout.getLayoutParams();
                params.topMargin = statusBarHeight;
                refeshLayout.setLayoutParams(params);
            }
        }
        initSlider();
    }


    private void initSlider(){
        drawerLayout.setEnabled(rightSliderMenu);
        if(rightSliderMenu){
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.slider_menu_layout,null);
            sliderMenuParent.addView(linearLayout);
            for (int i = 1; i < linearLayout.getChildCount(); i++) {
                View child = linearLayout.getChildAt(i);
                child.setTag(i);
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer tag = (Integer) v.getTag();
                        switch (tag){
                            case 1:
                                x5WebView.loadUrl("https://m.51qubo.com/Kefu.html");

                                break;
                            case 2:
                                x5WebView.loadUrl("https://m.51qubo.com/rechangelist.html");
                                break;
                            case 3:
                                x5WebView.loadUrl("https://m.51qubo.com/bank/Add/0.html");
                                break;
                            case 4:
                                x5WebView.loadUrl("https://m.51qubo.com/mobile_user_account_invest_tz.html");
                                break;
                            case 5:
                                new AlertDialog.Builder(BaseActivity.this).setMessage("确认需要清理缓存？")
                                        .setNegativeButton("取消",null)
                                        .setPositiveButton("清理", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                clearWebViewCache();
                                                Toast.makeText(BaseActivity.this,"已成功清理缓存",Toast.LENGTH_SHORT).show();
                                            }
                                        }).show();
                                break;
                            case 6:
                                new AlertDialog.Builder(BaseActivity.this).setMessage("确认现在退出应用？")
                                        .setNegativeButton("取消",null)
                                        .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                                System.exit(0);
                                            }
                                        }).show();
                                break;
                        }
                        drawerLayout.closeDrawers();
                    }
                });
            }
        }
    }

    protected void loadTitle() {
        back = findViewById(R.id.back);
        if (back != null) back.setOnClickListener(this);
        home = findViewById(R.id.home);
        if (home != null) home.setOnClickListener(this);
        refresh = findViewById(R.id.refresh);
        if (refresh != null) refresh.setOnClickListener(this);
        shareBtn = findViewById(R.id.share);
        if (shareBtn != null) shareBtn.setOnClickListener(this);
        moreBtn = findViewById(R.id.more);
        if (moreBtn != null) moreBtn.setOnClickListener(this);
        goForward = findViewById(R.id.go_forward);
        if (goForward != null) goForward.setOnClickListener(this);
        closeAp = findViewById(R.id.close_app);
        if (closeAp != null) closeAp.setOnClickListener(this);
        youhui = findViewById(R.id.youhui);
        if (youhui != null) youhui.setOnClickListener(this);
        kefu = findViewById(R.id.kefu);
        if (kefu != null) kefu.setOnClickListener(this);
        xiazhu = findViewById(R.id.xiazhu);
        if (xiazhu != null) xiazhu.setOnClickListener(this);
        zhibo = findViewById(R.id.zhibo);
        if (zhibo != null) zhibo.setOnClickListener(this);
        loadview = findViewById(R.id.loadview);
        if (loadview != null) loadview.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == back) {
            if (x5WebView.canGoBack()) {
                x5WebView.goBack();
            }
        } else if (v == home) {
            HOME = getResources().getString(R.string.home_url);
            x5WebView.loadUrl(HOME);
        } else if (v == refresh) {
            x5WebView.reload();
        } else if (v == shareBtn) {
            Share.shareWebLink(BaseActivity.this, x5WebView.getUrl());
        } else if (v == moreBtn) {

        } else if (v == goForward) {

        } else if (v == closeAp) {

        } else if (v == youhui) {

        } else if (v == kefu) {
            x5WebView.loadUrl("https://kf.gzyscs.cn/");
        } else if (v == xiazhu) {
            x5WebView.loadUrl("https://www.hs551.com/ssc/wufen");
        } else if (v == zhibo) {
            x5WebView.loadUrl("http://zb.gzyscs.cn/room/m/?rid=1");
        }
    }

    /**
     * 加载悬浮导航
     */
    int floatViewDownX, floatViewDownY, finalFloatViewDownX, finalFloatViewDownY;

    protected void initFloatNavigation() {
        if (floatNavigation) {
            floatLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.float_layout, null);
            final int floatViewW = (int) (40 * density);
            final int floatViewH = (int) (100 * density);
            floatParams = new RelativeLayout.LayoutParams(floatViewW, floatViewH);
            floatParams.leftMargin = (int) (270 * density);
            floatParams.topMargin = (int) (300 * density);
            rootView.addView(floatLayout, floatParams);

            floatBack = floatLayout.findViewById(R.id.float_back);
            floatHome = floatLayout.findViewById(R.id.float_home);
            floatLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            floatViewDownX = (int) event.getRawX();
                            floatViewDownY = (int) event.getRawY();
                            finalFloatViewDownX = (int) event.getRawX();
                            finalFloatViewDownY = (int) event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:

                            int floatViewCurrentX = (int) event.getRawX();
                            int floatViewCurrentY = (int) event.getRawY();
                            if (Math.abs(floatViewCurrentX - finalFloatViewDownX) < 10
                                    && Math.abs(floatViewCurrentY - finalFloatViewDownY) < 10) {
                                return true;
                            }
                            floatParams.leftMargin += floatViewCurrentX - floatViewDownX;
                            floatParams.topMargin += floatViewCurrentY - floatViewDownY;

                            if (floatParams.leftMargin < 0) {
                                floatParams.leftMargin = 0;
                            }
                            if (floatParams.topMargin < 0) {
                                floatParams.topMargin = 0;
                            }
                            if (floatParams.leftMargin + floatViewW > screenW) {
                                floatParams.leftMargin = screenW - floatViewW;
                            }
                            if (floatParams.topMargin + floatViewH + 22 > screenH) {
                                floatParams.topMargin = screenH - floatViewH - 22;
                            }
                            floatViewDownX = floatViewCurrentX;
                            floatViewDownY = floatViewCurrentY;
                            rootView.updateViewLayout(floatLayout, floatParams);
                            break;
                        case MotionEvent.ACTION_UP:
                            if (Math.abs(event.getRawY() - finalFloatViewDownY) < 10) {
                                if (floatHome.getY() + floatHome.getHeight() >= event.getY()) {
                                    HOME = getResources().getString(R.string.home_url);
                                    x5WebView.loadUrl(HOME);
                                } else {
                                    if (x5WebView.canGoBack()) {
                                        x5WebView.goBack();
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


    void initConfig() {
        HOME = getResources().getString(R.string.start_url);
        refreshable = getResources().getBoolean(R.bool.pull_refresh_enable);
        hasDaoHang = getResources().getBoolean(R.bool.save_daohang);
        guestureNavigation = getResources().getBoolean(R.bool.gesture_navigation);
        loadingTime = getResources().getInteger(R.integer.loading_delay);
        fullScreen = getResources().getBoolean(R.bool.full_screen);
        floatNavigation = getResources().getBoolean(R.bool.float_navigation);
        bottomNavigation = getResources().getBoolean(R.bool.bottom_navigation);
        rightSliderMenu =  getResources().getBoolean(R.bool.slider_menu);
        hasguide = getResources().getBoolean(R.bool.need_guide);

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        DisplayMetrics dm = getResources().getDisplayMetrics();
        density = dm.density;
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;
    }

    void loadHome() {
        x5WebView.loadUrl(HOME);
    }

    void fullscreenNo() {
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(option);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= 21) {
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    String tempUrl;

    void setupWebview() {
        x5WebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        x5WebView.setmCallBack(new X5WebView.LongClickCallBack() {
            @Override
            public void onLongClickCallBack(final String imgUrl) {

                BaseActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        new AlertDialog.Builder(BaseActivity.this).setTitle("").setNegativeButton("保存图片到相册", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                if(imgUrl.startsWith("data:image/png;base64,")){
//                                    String tempimgUrl = imgUrl.replace("data:image/png;base64","");
//                                    BaseActivity.this.mBitmap = Base64.decode(tempimgUrl,Base64.DEFAULT);
//                                    saveMyBitmap(BaseActivity.this.mBitmap,""+System.currentTimeMillis());
////                            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
////                            Result result = DecodeImage.handleQRCodeFormBitmap(bitmap);
////                            if(result!=null){
////                                Log.e("----onLongClickCallBack", ""+result.getText());
////                                webview.loadUrl(result.getText());
////                            }
//                                }else if(imgUrl.startsWith("http")){
//                                    imageUrl=imgUrl;
//                                    // 获取到图片地址后做相应的处理
//                                    MainActivity.MyAsyncTask mTask = new MainActivity.MyAsyncTask();
//                                    mTask.execute(imgUrl);
//                                }
//                            }
//                        }).show();
                    }
                });
            }
        });
        if (refreshable) {
            x5WebView.setScrollChange(new X5WebView.ScrollChange() {
                @Override
                public void onScrollChanged(int l, int t, int oldl, int oldt) {
                    Log.e("----MainActivity", "onScrollChanged:" + t);
                    if (t == 0) {
                        refeshLayout.setEnabled(true);
                    } else {
                        refeshLayout.setEnabled(false);
                    }
                }
            });
        }
        if (guestureNavigation) {

            x5WebView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (guestureNavigation) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startX = (int) event.getX();
                                break;
                            case MotionEvent.ACTION_UP:

                                int endX = (int) event.getX();
                                if (endX > startX && x5WebView.canGoBack() && endX - startX > SCROLL_SIZE) {
                                    x5WebView.goBack();
                                } else if (endX < startX && x5WebView.canGoForward() && startX - endX > SCROLL_SIZE) {
                                    x5WebView.goForward();
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

        x5WebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
                jsResult.confirm();
                Log.e("----onJsAlert", "_" + s + "_" + s1);
                return super.onJsAlert(webView, s, s1, jsResult);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
//                Log.e("----MainActivity", "onConsoleMessage:" + consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onReceivedTouchIconUrl(WebView webView, String s, boolean b) {
                super.onReceivedTouchIconUrl(webView, s, b);
//                Log.e("----MainActivity", "onReceivedTouchIconUrl:" + s);
            }

            @Override
            public void onReachedMaxAppCacheSize(long l, long l1, WebStorage.QuotaUpdater quotaUpdater) {
                super.onReachedMaxAppCacheSize(l, l1, quotaUpdater);
                Log.e("----MainActivity", "onReachedMaxAppCacheSize:");
            }

            @Override
            public boolean onJsBeforeUnload(WebView webView, String s, String s1, JsResult jsResult) {
                Log.e("----MainActivity", "onJsBeforeUnload:");
                return super.onJsBeforeUnload(webView, s, s1, jsResult);
            }

            @Override
            public boolean onJsPrompt(WebView webView, String s, String s1, String s2, JsPromptResult jsPromptResult) {
                Log.e("----MainActivity", "onJsPrompt:");
                return super.onJsPrompt(webView, s, s1, s2, jsPromptResult);
            }

            @Override
            public boolean onJsConfirm(WebView webView, String s, String s1, JsResult jsResult) {
                Log.e("----MainActivity", "onJsConfirm:");
                return super.onJsConfirm(webView, s, s1, jsResult);
            }

            @Override
            public boolean onJsTimeout() {
                Log.e("----MainActivity", "onJsTimeout:");
                return super.onJsTimeout();
            }

            @Override
            public boolean onCreateWindow(WebView webView, boolean b, boolean b1, Message message) {
                Log.e("----onCreateWindow", "" + message);
//                NewWindowView newview = (NewWindowView) getLayoutInflater().inflate(R.layout.new_window,null);
//                rootView.addView(newview,new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                WebView.WebViewTransport transport = (WebView.WebViewTransport) message.obj;//以下的操作应该就是让新的webview去加载对应的url等操作。
//                transport.setWebView(newview.x5WebView);
//                message.sendToTarget();
                return true;
            }

            @Override
            public void onCloseWindow(WebView webView) {
                super.onCloseWindow(webView);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                Log.e("----onShowCustomView", "----1111");
            }

            @Override
            public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
                super.onShowCustomView(view, requestedOrientation, callback);
                Log.e("----onShowCustomView", "----2222");
            }

            @Override
            public void onProgressChanged(WebView webView, int progress) {
                super.onProgressChanged(webView, progress);
                if (progressBarH != null) {
//                    progressBarH.setProgress(progress);
//                    if(progress==100){
//                        progressBarH.setVisibility(View.INVISIBLE);
//                    }else{
//                        progressBarH.setVisibility(View.VISIBLE);
//                    }
                }

                if (refreshable) {
                    if (progress == 100) {
                        refeshLayout.setRefreshing(false);
                    }
                }
                if (getResources().getBoolean(R.bool.show_loadng)) {
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
                Log.e("----openFileChooser", "4");
                if(uploadMessage!=null){
                    uploadMessage.onReceiveValue(null);
                    return true;
                }
                if(Build.VERSION.SDK_INT>=21){
                    Intent intent = fileChooserParams.createIntent();
                    startActivityForResult(Intent.createChooser(intent, "选择图片"), FILE_CHOOSER_RESULT_CODE);
                }

                uploadMessage = valueCallback;
//                openImageChooserActivity();
                return true;
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.e("----openFileChooser", "3");
                if(singleUploadMessage!=null){
                    singleUploadMessage.onReceiveValue(null);
                    return;
                }
                singleUploadMessage = uploadMsg;
                openImageChooserActivity();
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                this.openFileChooser(uploadMsg,"");
                Log.e("----openFileChooser", "1");
            }
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
               this.openFileChooser(uploadMsg,"","");
                Log.e("----openFileChooser", "2");
            }

        });
        x5WebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                Log.d("----BaseActivity", "shouldOverrideUrlLoading:" +request.getRequestHeaders());
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
//                Uri uri = Uri.parse(url);

                Log.e("----should", "" + url);

                try {
                    if (url.toLowerCase().startsWith("intent://")) {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return true;
                    }else if (!url.toLowerCase().startsWith("http")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                    else if (url.toLowerCase().contains("https://qr.alipay.com")) {
                        int index = url.toLowerCase().indexOf("https://qr.alipay.com");
                        String newUrl = url.substring(index);
                        view.loadUrl(newUrl);
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view,url);
                } catch (Exception e) {
//                    Log.e("----should--error", ""+e.getMessage());
                    Toast.makeText(BaseActivity.this, "无法打开指定应用，请先确认应用是否安装！", Toast.LENGTH_SHORT).show();
                }
                return super.shouldOverrideUrlLoading(view, url);

            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                Log.e("----", "shouldInterceptRequest----" + url);
                if (url.toLowerCase().contains(".swf") || url.toLowerCase().contains(".mp4")) {
                    interceptVideo(url);
                    return new WebResourceResponse(null, null, null);
                }
                if (!url.toLowerCase().contains(HOME)) { //过滤广告
                    if (!ADFilterTool.hasAd(BaseActivity.this, url)) {
                        return super.shouldInterceptRequest(view, url);
                    } else {
                        return new WebResourceResponse(null, null, null);
                    }
                } else {

                    return super.shouldInterceptRequest(view, url);
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if(Build.VERSION.SDK_INT>=21){
                }
                return super.shouldInterceptRequest(view, request);
            }


            @Override
            public void onReceivedLoginRequest(WebView webView, String s, String s1, String s2) {
//                Log.e("----MainActivity", "onReceivedLoginRequest:" + s);
                super.onReceivedLoginRequest(webView, s, s1, s2);
            }

            @Override
            public void onFormResubmission(WebView webView, Message message, Message message1) {
//                Log.e("----MainActivity", "onFormResubmission:" );
                super.onFormResubmission(webView, message, message1);
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView webView, HttpAuthHandler httpAuthHandler, String s, String s1) {

                super.onReceivedHttpAuthRequest(webView, httpAuthHandler, s, s1);
//                Log.e("----MainActivity", "onReceivedHttpAuthRequest:");
            }

            @Override
            public void onLoadResource(WebView webView, String s) {
//                Log.e("----MainActivity", "onLoadResource:"+s );

                super.onLoadResource(webView, s);

            }

            @Override
            public void onReceivedClientCertRequest(WebView webView, ClientCertRequest clientCertRequest) {
                super.onReceivedClientCertRequest(webView, clientCertRequest);
//                Log.e("----MainActivity", "onReceivedClientCertRequest:" );
            }

            @Override
            public void onScaleChanged(WebView webView, float v, float v1) {
//                Log.e("----MainActivity", "onScaleChanged:" + v + "   v1:" + v1);
                super.onScaleChanged(webView, v, v1);
            }


            @Override
            public void onTooManyRedirects(WebView webView, Message message, Message message1) {
                super.onTooManyRedirects(webView, message, message1);
//                Log.e("----MainActivity", "onTooManyRedirects:");
            }


            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
//                Log.e("----onReceivedError", "");
            }


            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                sslErrorHandler.proceed();
                super.onReceivedSslError(webView, sslErrorHandler, sslError);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
//                Log.e("----onReceivedError", "failingUrl:" + failingUrl);
                errorNotice.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);
                errorNotice.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
    }

    private void interceptVideo(final String url) {
//        new AlertDialog.Builder(this).setMessage("请选择操作").setPositiveButton("在线播放", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                x5WebView.loadUrl(url);
//            }
//        }).setNegativeButton("取消",null).show();
    }

    private void openImageChooserActivity() {
        new AlertDialog.Builder(BaseActivity.this).setMessage("请选择方式")
                .setNegativeButton("相机", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            //根据路径实例化图片文件
                            File photoFile = getCameraTmpFile();
                            //设置拍照后图片保存到文件中
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            //启动拍照activity并获取返回数据
                            startActivityForResult(intent, FILE_CHOOSER_CAMERA);
                        } catch (Exception e) {
                            Toast.makeText(BaseActivity.this, "请打开相机权限", Toast.LENGTH_SHORT).show();
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
    }

    private File getCameraTmpFile() {
        return new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if(result!=null){
                String path = getRealPathByUri(BaseActivity.this,result);
                Log.e("----onActivityResult", ""+path);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (uploadMessage != null) {

                        Uri []uri = path==null?null:new Uri[]{Uri.fromFile(new File(path))};
//                        Uri []uri = path==null?null:new Uri[]{result};
                        Log.e("----onActivityResult", ""+path);
                        uploadMessage.onReceiveValue(uri);
                    }
                }
                if(singleUploadMessage!=null){
                    singleUploadMessage.onReceiveValue(path==null?null:Uri.fromFile(new File(path)));
                }
                singleUploadMessage = null;
                uploadMessage = null;
            }

        } else if (requestCode == FILE_CHOOSER_CAMERA) {
            File photoFile = getCameraTmpFile();
            if (uploadMessage != null) {
                Uri []uri = new Uri[]{Uri.fromFile(photoFile)};
                uploadMessage.onReceiveValue(uri);
            }
            if(singleUploadMessage!=null){
                singleUploadMessage.onReceiveValue(Uri.fromFile(photoFile));
            }
            singleUploadMessage = null;
            uploadMessage = null;
        } else if (requestCode == QUCODE_REQUEST) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
//                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
//                    String result = bundle.getString(CodeUtils.RESULT_STRING);
//                    Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();
//                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
//                    Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
//                }
            }
        }
        if (uploadMessage != null) {
            uploadMessage.onReceiveValue(null);
            uploadMessage = null;
        }
        if(singleUploadMessage!=null){
            singleUploadMessage.onReceiveValue(null);
            singleUploadMessage = null;
        }
    }

    public void clearWebViewCache(){
        //清理Webview缓存数据库
        try {
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e("----clearWebViewCache", ""+e.getMessage());
        }
    }

    long mills = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if ((getResources().getBoolean(R.bool.can_goback) && x5WebView.canGoBack()) || x5WebView.getUrl().equals(HOME)) {
//                Log.d("----BaseActivity", "onKeyDown:" + x5WebView.getUrl());
//                x5WebView.goBack();
//                return true;
//            }
            if (System.currentTimeMillis() - mills > 1000) {
                Toast.makeText(this, getString(R.string.exit), Toast.LENGTH_SHORT).show();
                mills = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
