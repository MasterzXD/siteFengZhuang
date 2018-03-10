package sihuo.app.com.kuaiqian;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.TbsVideo;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import sihuo.app.com.kuaiqian.utils.ADFilterTool;
import sihuo.app.com.kuaiqian.utils.FileUtils;
import sihuo.app.com.kuaiqian.utils.Share;
import sihuo.app.com.kuaiqian.utils.X5WebView;

import static sihuo.app.com.kuaiqian.utils.FileUtils.getRealPathByUri;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    final boolean DEBUG_ALL = true;
    final boolean DEBUG = true;
    final int FILE_CHOOSER_RESULT_CODE = 40;
    final int FILE_CHOOSER_CAMERA = 22;
    final int FILE_CHOOSER_CUT = 30;
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

    private TextView back, refresh, goForward, closeAp, home, shareBtn, moreBtn, youhui, kefu, loadview, xiazhu, zhibo,titleView;
    /*float navigation*/
    private FrameLayout topNavi, bottomNavi;
    private ProgressBar progressBarH;
    private JpushCustomReceiver jpushCustomReceiver;
    private boolean refreshable, hasDaoHang, guestureNavigation, fullScreen, floatNavigation, bottomNavigation, rightSliderMenu,hasguide;
    private boolean isUserCenter;
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
        initConfig();
        if(fullScreen){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_base);
        jpushCustomReceiver = new JpushCustomReceiver();
        x5WebView = findViewById(R.id.x5webview);
        errorNotice = findViewById(R.id.errorNotice);
        refeshLayout = findViewById(R.id.refesh_layout);
        rootView = findViewById(R.id.root_view);
        drawerLayout = findViewById(R.id.drawerLayout);
        sliderMenuParent = findViewById(R.id.slider_parent);
        progressBarH = findViewById(R.id.progressBar);

        refeshLayout.setEnabled(refreshable);
        if (refreshable) {
            refeshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    x5WebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                    x5WebView.reload();
                }
            });
        }
        loadHome();
        setupWebview();
        if (hasDaoHang) {
            topNavi = findViewById(R.id.topNavi);
            bottomNavi = findViewById(R.id.bottomNavi);
            View view = getLayoutInflater().inflate(R.layout.title_layout, null);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (bottomNavigation) {
                bottomNavi.setVisibility(View.VISIBLE);
                bottomNavi.addView(view, params);
            } else {
//                topNavi.setVisibility(View.VISIBLE);
                topNavi.addView(view, params);
            }
            loadTitle();
        }
        initSlider();
    }


    private void initSlider(){
        if(!rightSliderMenu){
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        if(rightSliderMenu){
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.slider_menu_layout,null);
            sliderMenuParent.addView(linearLayout);
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                View child = linearLayout.getChildAt(i);
                child.setTag(i);
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer tag = (Integer) v.getTag();
                        switch (tag){
                            case 0:
                                x5WebView.loadUrl(getString(R.string.home_url));
                                break;
                            case 2:
                                x5WebView.loadUrl("https://vv666h.com/Home/Recharge/recharge_online.html");
                                break;
                            case 4:
                                x5WebView.loadUrl("https://vv666h.com/Home/Recharge/balance.html");
                                break;
                            case 6:
                                x5WebView.loadUrl("https://vv666h.com/Home/Favorable/serve_online.html");
                                break;
//                            case 8:
//                                x5WebView.loadUrl("http://6666.KQ888888888.com:5555/kf");
//                                break;
                            case 10:
                                x5WebView.loadUrl("http://6666.KQ888888888.com:5555/gg");
                                break;
                            case 12:
                                x5WebView.loadUrl("http://6666.KQ888888888.com:5555/hb");
                                break;
                            case 14:
                                Share.shareWebLink(BaseActivity.this, "https://kq2666.com");
                                break;
                            case 8:
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
        titleView = findViewById(R.id.title);
    }

    @Override
    public void onClick(View v) {
        if (v == back) {
            if (x5WebView.canGoBack()) {
                x5WebView.goBack();
            }
        } else if (v == home) {
//            HOME = getResources().getString(R.string.home_url);
//            x5WebView.loadUrl(HOME);
//            isUserCenter = true;
            openImageChooserActivity();
        } else if (v == refresh) {
            x5WebView.reload();
        } else if (v == shareBtn) {
//            testSendPushWithCallback("测试标题","测试内容",null);
            Share.shareWebLink(BaseActivity.this, x5WebView.getUrl());
        } else if (v == moreBtn) {
            drawerLayout.openDrawer(Gravity.END);
        } else if (v == goForward) {
            if (x5WebView.canGoForward()) {
                x5WebView.goForward();
            }
        } else if (v == closeAp) {
            finish();
            System.exit(0);
        } else if (v == youhui) {

        } else if (v == kefu) {
            x5WebView.loadUrl("https://kf.gzyscs.cn/");
        } else if (v == xiazhu) {
            x5WebView.loadUrl("https://www.hs551.com/ssc/wufen");
        } else if (v == zhibo) {
            x5WebView.loadUrl("http://zb.gzyscs.cn/room/m/?rid=1");
        }
    }

    void initConfig() {
        HOME = getResources().getString(R.string.start_url);
        refreshable = getResources().getBoolean(R.bool.pull_refresh_enable);
        hasDaoHang = getResources().getBoolean(R.bool.save_daohang);
        guestureNavigation = getResources().getBoolean(R.bool.gesture_navigation);
        fullScreen = getResources().getBoolean(R.bool.full_screen);
        floatNavigation = getResources().getBoolean(R.bool.float_navigation);
        bottomNavigation = getResources().getBoolean(R.bool.bottom_navigation);
        rightSliderMenu =  getResources().getBoolean(R.bool.slider_menu);
        hasguide = getResources().getBoolean(R.bool.need_guide);
    }

    void loadHome() {
        x5WebView.loadUrl(HOME+"?lx=1");
    }


    class JsJiaoHu {

        @JavascriptInterface
        public void setJpushTag(String tag){
            Log.e("----setJpushTag", ""+tag);
            if(!TextUtils.isEmpty(tag)){
                Set set =new HashSet();
                set.add(tag);
                JPushInterface.setTags(BaseActivity.this,2,set);
            }
        }
        @JavascriptInterface
        public void sendPush(String tags,String title,String content,String targetUrl){
            Log.e("----sendPush", ""+tags);
            if(TextUtils.isEmpty(tags)){
                tags = null;
            }
            if(tags.endsWith(",")){
                tags = tags.substring(0,tags.length()-1);
            }
            if(!TextUtils.isEmpty(targetUrl)){
                content = content + "&&" +targetUrl;
            }
            testSendPushWithCallback(title,content,tags.split(","));
        }
    }

    public static void testSendPushWithCallback(final String title,final String msg,final String[] tags) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ClientConfig clientConfig = ClientConfig.getInstance();
                final JPushClient jpushClient = new JPushClient("3f927f3a609531b7ebce40d2", "8ec1e9dcffbdbd1521389cd5", null, clientConfig);
                final PushPayload payload = buildPushObject_all_alias_alert(title,msg,tags);
                try {
                    PushResult result = jpushClient.sendPush(payload);
                    System.out.println(result);
                } catch (APIConnectionException e) {

                } catch (APIRequestException e) {
                }
            }
        }).start();

    }

    public static PushPayload buildPushObject_all_alias_alert(final String title,final String msg,final String[] tags) {
//        Audience audience = Audience.tag("11985");
        Audience audience = Audience.all();
        if(tags!=null && tags.length>0){
            audience = Audience.tag(tags);
        }
        return PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(audience)
                .setMessage(cn.jpush.api.push.model.Message.newBuilder().setTitle(title).setMsgContent(msg).build())
                .build();
    }

    void setupWebview() {
        x5WebView.addJavascriptInterface(new JsJiaoHu(),"AndroidJS");
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
                        new AlertDialog.Builder(BaseActivity.this).setTitle("").setNegativeButton("保存图片到相册", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(imgUrl.startsWith("data:image/png;base64,")){
                                    String tempimgUrl = imgUrl.replace("data:image/png;base64","");
//                                    BaseActivity.this.mBitmap = Base64.decode(tempimgUrl, Base64.DEFAULT);
//                                    saveMyBitmap(BaseActivity.this.mBitmap,""+System.currentTimeMillis());
//                            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
//                            Result result = DecodeImage.handleQRCodeFormBitmap(bitmap);
//                            if(result!=null){
//                                Log.e("----onLongClickCallBack", ""+result.getText());
//                                webview.loadUrl(result.getText());
//                            }
                                }else if(imgUrl.startsWith("http")){
                                    FileUtils.savePicture(BaseActivity.this,""+System.currentTimeMillis(),imgUrl);
                                }
                            }
                        }).show();
                    }
                });
            }
        });
        if (refreshable) {
            x5WebView.setScrollChange(new X5WebView.ScrollChange() {
                @Override
                public void onScrollChanged(int l, int t, int oldl, int oldt) {
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
            public boolean onJsAlert(WebView webView, String url, String message,final JsResult jsResult) {

                new AlertDialog.Builder(BaseActivity.this).setMessage(message)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                jsResult.confirm();
                            }
                        }).show();
                return true;
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
            public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                Log.e("----onShowCustomView", "----1111");
            }

            @Override
            public void onShowCustomView(View view, int requestedOrientation, IX5WebChromeClient.CustomViewCallback callback) {
                super.onShowCustomView(view, requestedOrientation, callback);
                Log.e("----onShowCustomView", "----2222");
            }

            @Override
            public void onReceivedTitle(WebView webView, String s) {
                super.onReceivedTitle(webView, s);
                titleView.setText(s);
            }

            @Override
            public void onProgressChanged(WebView webView, int progress) {
                super.onProgressChanged(webView, progress);
                if (progressBarH != null) {
                    progressBarH.setProgress(progress);
                    if(progress==100){
                        progressBarH.setVisibility(View.INVISIBLE);
                        x5WebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                    }else{
                        progressBarH.setVisibility(View.VISIBLE);
                    }
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
                }
                uploadMessage = valueCallback;
                openImageChooserActivity();
                return true;
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.e("----openFileChooser", "3");
                if(singleUploadMessage!=null){
                    singleUploadMessage.onReceiveValue(null);
                }
                singleUploadMessage = uploadMsg;
                openImageChooserActivity();
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                this.openFileChooser(uploadMsg, "*/*");
            }

            // For Android >= 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType) {
                this.openFileChooser(uploadMsg, acceptType, null);
            }

        });

        x5WebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                Log.d("----BaseActivity", "shouldOverrideUrlLoading:" + url);

                if(url.endsWith(".mp4")){
                        TbsVideo.openVideo(BaseActivity.this,url);
                    return true;
                }
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
                    Log.e("----should--error", ""+e.getMessage());
//                    Toast.makeText(BaseActivity.this, "无法打开指定应用，请先确认应用是否安装！", Toast.LENGTH_SHORT).show();
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
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, com.tencent.smtt.export.external.interfaces.SslError sslError) {
                sslErrorHandler.proceed();
                super.onReceivedSslError(webView, sslErrorHandler, sslError);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
//                Log.e("----onReceivedError", "");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
//                Log.e("----onReceivedError", "failingUrl:" + failingUrl);
                errorNotice.setVisibility(View.VISIBLE);
                if(failingUrl.endsWith("*.mp4")){
                    if(TbsVideo.canUseTbsPlayer(BaseActivity.this)){
                        TbsVideo.openVideo(BaseActivity.this,failingUrl);
                    }
                }
            }

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);
                if(topNavi!=null){
                    topNavi.setVisibility(s.contains(HOME)?View.GONE:View.VISIBLE);
                }
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
        isUserCenter = "http://www.cns-union.com/uploadmoviewxtx.asp?T=1".equals(x5WebView.getUrl())||(x5WebView.getUrl().contains("wxstzlxg.asp"));
//        isUserCenter = true;
        new AlertDialog.Builder(BaseActivity.this).setMessage("请选择方式")
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (uploadMessage != null) {
                            uploadMessage.onReceiveValue(null);
                            uploadMessage = null;
                        }
                        if(singleUploadMessage!=null){
                            singleUploadMessage.onReceiveValue(null);
                            singleUploadMessage = null;
                        }
                    }
                })
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
        }).setCancelable(false)
                .show();
    }

    private File getCameraTmpFile() {
        return new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if(result!=null){
                if(isUserCenter){
                    isUserCenter = false;
                    invokeSystemCrop(result);
                    return;
                }
                result = yasuoNormal(result);
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(new Uri[]{result});
                }
                if(singleUploadMessage!=null){
                    String path = getRealPathByUri(BaseActivity.this,result);
                    singleUploadMessage.onReceiveValue(path==null?null:Uri.fromFile(new File(path)));
                }
                singleUploadMessage = null;
                uploadMessage = null;
            }

        } else if (requestCode == FILE_CHOOSER_CAMERA) {
            File photoFile = getCameraTmpFile();
            if(isUserCenter){
                isUserCenter = false;
                invokeSystemCrop(Uri.fromFile(photoFile));
                return;
            }
            Uri result = yasuoNormal(photoFile.getAbsolutePath());
            if (uploadMessage != null) {
                Uri []uri = new Uri[]{result};
                uploadMessage.onReceiveValue(uri);
            }
            if(singleUploadMessage!=null){
                singleUploadMessage.onReceiveValue(result);
            }
            singleUploadMessage = null;
            uploadMessage = null;
        } else if (requestCode == FILE_CHOOSER_CUT) {

            String path = data == null || resultCode != RESULT_OK ? null : getTempFile();

            if(path!=null){
                Uri result = Uri.fromFile(new File(path));
                if(Build.VERSION.SDK_INT>19){
                    result = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(path));
                }
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(new Uri[]{result});
                }
                if(singleUploadMessage!=null){
                    singleUploadMessage.onReceiveValue(path==null?null:Uri.fromFile(new File(path)));
                }
                singleUploadMessage = null;
                uploadMessage = null;
            }else{
                Log.e("----getAction", ""+data.getAction());
            }
        }
        else if (requestCode == QUCODE_REQUEST) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
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

    /**
     * 调用系统照片的裁剪功能
     */
    public void invokeSystemCrop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        String realpath= FileUtils.getRealPathByUri(this,uri);
        intent.setDataAndType(Uri.fromFile(new File(realpath)), "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        intent.putExtra("scale", true);

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getTempFile())));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent,FILE_CHOOSER_CUT);
//        return intent;
    }

    private String getTempFile(){
        return Environment.getExternalStorageDirectory()  + "/" + "temp.jpg";
    }

    private Uri yasuoNormal(Uri uri){
        String realpath= FileUtils.getRealPathByUri(this,uri);
        return yasuoNormal(realpath);
    }
    private Uri yasuoNormal(String filepath){
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, newOpts);// 此时返回bm为空

        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        if(h/w>=1){
            new AlertDialog.Builder(this).setTitle("请上传横拍的照片！")
                    .setPositiveButton("知道了",null).show();
            return null;
        }
        Bitmap bitmap = FileUtils.getScaledimage(filepath);
        String temppath = Environment.getExternalStorageDirectory()  + "/" + "ttt.jpg";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(temppath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            if(Build.VERSION.SDK_INT>19){
                return FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(temppath));
            }
            return Uri.fromFile(new File(temppath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((getResources().getBoolean(R.bool.can_goback) && x5WebView.canGoBack())) {
//                Log.d("----BaseActivity", "onKeyDown:" + x5WebView.getUrl());
                x5WebView.goBack();
                return true;
            }
            new AlertDialog.Builder(BaseActivity.this).setMessage("现在要退出系统吗？")
                    .setNegativeButton("取消",null)
                    .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            System.exit(0);
                        }
                    }).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String url = getSharedPreferences("targeturl",Context.MODE_PRIVATE).getString("targeturl",null);
                if(url!=null){
                    if(x5WebView!=null){
                        x5WebView.loadUrl(url);
                    }
                    SharedPreferences.Editor editor = getSharedPreferences("targeturl",Context.MODE_PRIVATE).edit();
                    editor.clear();
                    editor.commit();
                }
            }
        },3000);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ACTION_JPUSH_CUSTOM);
        filter.setPriority(50);
        registerReceiver(jpushCustomReceiver,filter);
    }




    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(jpushCustomReceiver);
    }

    @Override
    protected void onDestroy() {
        releaseWebview();
        super.onDestroy();
    }

    void releaseWebview(){
        if(x5WebView!=null){
            x5WebView.loadUrl("about:blank");
            x5WebView.removeAllViews();
            x5WebView.destroy();
        }
    }

    public class JpushCustomReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();
            if(intent.getAction().equals(Constant.ACTION_JPUSH_CUSTOM)){
//                Log.e("----MyReceiver", "收到jpush自定义通知");
                String content = intent.getExtras().getString(JPushInterface.EXTRA_MESSAGE);
                String title = intent.getExtras().getString(JPushInterface.EXTRA_TITLE);
                final String url ;
                if(content.contains("&&")){
                    url = content.split("&&")[1];
                    content = content.split("&&")[0];
                }else {
                    url = null;
                }
                if(TextUtils.isEmpty(url)){
                    new android.support.v7.app.AlertDialog.Builder(BaseActivity.this).setTitle(title).setMessage(content)
                            .setPositiveButton("知道了",null).show();
                }else{
                    new android.support.v7.app.AlertDialog.Builder(BaseActivity.this).setTitle(title).setMessage(content)
                            .setNegativeButton("稍后",null).setPositiveButton("查看详情", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    x5WebView.loadUrl(url);
                                }
                            }).show();
                }

            }
        }
    }
}
