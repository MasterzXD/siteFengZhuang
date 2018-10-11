package sihuo.app.com.kuaiqian;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.BuildConfig;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
//import com.tencent.smtt.export.external.interfaces.JsResult;
//import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
//import com.tencent.smtt.export.external.interfaces.WebResourceError;
//import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
//import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
//import com.tencent.smtt.sdk.DownloadListener;
//import com.tencent.smtt.sdk.TbsVideo;
//import com.tencent.smtt.sdk.ValueCallback;
//import com.tencent.smtt.sdk.WebChromeClient;
//import com.tencent.smtt.sdk.WebView;
//import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.TbsVideo;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import sihuo.app.com.kuaiqian.utils.ADFilterTool;
import sihuo.app.com.kuaiqian.utils.FileUtils;
import sihuo.app.com.kuaiqian.utils.LogUtil;
import sihuo.app.com.kuaiqian.utils.Share;
import sihuo.app.com.kuaiqian.utils.X5WebView;

import static sihuo.app.com.kuaiqian.utils.FileUtils.getRealPathByUri;

@RuntimePermissions
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

    private TextView back, refresh, goForward, showMenu, clearCache, closeAp, home,
            shareBtn, moreBtn, youhui, kefu, loadview, xiazhu, zhibo, liaotianshi, zaixiantouzhu,
            setting, xianlujiance;

    private AppCompatImageView back_img, refresh_img, goForward_img,home_img,clear_img;
    /*float navigation*/
    private LinearLayout floatLayout;
    private RelativeLayout.LayoutParams floatParams;
    private ImageView floatHome, floatBack;
    private FrameLayout topNavi, bottomNavi;
    private ProgressBar progressBarH;
    private LinearLayout alertProgress;

    private boolean refreshable, hasDaoHang, guestureNavigation, fullScreen, floatNavigation, bottomNavigation, rightSliderMenu, hasguide;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };


    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initConfig();
        if (fullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_base);
        JPushInterface.init(this.getApplicationContext());
        UMConfigure.setLogEnabled(BuildConfig.DEBUG);
        UMConfigure.init(getApplicationContext(), UMConfigure.DEVICE_TYPE_PHONE, "");
        MobclickAgent.setScenarioType(getApplicationContext(), MobclickAgent.EScenarioType.E_UM_NORMAL);

        x5WebView = findViewById(R.id.x5webview);
        errorNotice = findViewById(R.id.errorNotice);
        refeshLayout = findViewById(R.id.refesh_layout);
        rootView = findViewById(R.id.root_view);
        drawerLayout = findViewById(R.id.drawerLayout);
        sliderMenuParent = findViewById(R.id.slider_parent);
        progressBarH = findViewById(R.id.progressBar);
        alertProgress =findViewById(R.id.alertProgress);

        x5WebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        refeshLayout.setEnabled(refreshable);
        if (refreshable) {
            refeshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    x5WebView.reload();
                }
            });
        }
        Log.d("----BaseActivity", "onCreate:开始timmer");
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
                topNavi.setVisibility(View.VISIBLE);
                topNavi.addView(view, params);
            }
            loadTitle();
        }
        if(sihuo.app.com.kuaiqian.BuildConfig.DEBUG){
            Set<String> set = new HashSet<>();
            set.add("ceshi");
            JPushInterface.setTags(this,1,set);
        }
        if("zhinengliangzilian".equals(sihuo.app.com.kuaiqian.BuildConfig.FLAVOR)
                ||"iqc".equals(sihuo.app.com.kuaiqian.BuildConfig.FLAVOR)){
            clearWebViewCache();
        }
        initSlider();
        initFloatNavigation();
        if(!isNotificationEnabled(this)){
            new AlertDialog.Builder(this).setMessage("打开通知，可以获取最新消息").setNegativeButton("暂不",null).setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    toSetting();
                }
            }).show();
        }
        BaseActivityPermissionsDispatcher.getPermissionWithPermissionCheck(this);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void getPermission() {
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("请允许使用存储权限，部分功能才能生效！")
                .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .show();
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onDenied() {
//        Toast.makeText(this, "OnPermissionDenied", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onNever() {
        new AlertDialog.Builder(this).setMessage("请手动前往设置开启<权限>，以保证部分功能正常运行。")
                .setNegativeButton("暂不", null)
                .setPositiveButton("前往", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getAppDetailSettingIntent(BaseActivity.this);
                    }
                }).show();
    }

    private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(localIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BaseActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void initSlider() {
        if (!rightSliderMenu) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        if (rightSliderMenu) {
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.slider_menu_layout, null);
            sliderMenuParent.addView(linearLayout);
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                View child = linearLayout.getChildAt(i);
                child.setTag(i);
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer tag = (Integer) v.getTag();
                        switch (tag) {
                            case 0:
                                x5WebView.loadUrl(HOME);
                                break;
                            case 2:
                                x5WebView.loadUrl("http://yd.gc3333.com/Home/Recharge/recharge.html");
                                break;
                            case 4:
                                x5WebView.loadUrl("http://yd.gc3333.com/Home/Recharge/balance.html");
                                break;
                            case 6:
                                x5WebView.loadUrl("https://f18.livechatvalue.com/chat/chatClient/chatbox.jsp?companyID=677786&configID=61320&jid=3223891139&s=1");
                                break;
                            case 8:
                                new AlertDialog.Builder(BaseActivity.this).setMessage("确认需要清理缓存？")
                                        .setNegativeButton("取消", null)
                                        .setPositiveButton("清理", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                clearWebViewCache();
                                                Toast.makeText(BaseActivity.this, "已成功清理缓存", Toast.LENGTH_SHORT).show();
                                            }
                                        }).show();
                                break;
                            case 11:
                                x5WebView.loadUrl("http://aa.4144.me:3609/hb");
                                break;
                            case 13:
                                Share.shareWebLink(BaseActivity.this, "http://4155.me");
                                break;
                            case 15:
                                new AlertDialog.Builder(BaseActivity.this).setMessage("确认需要清理缓存？")
                                        .setNegativeButton("取消", null)
                                        .setPositiveButton("清理", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                clearWebViewCache();
                                                Toast.makeText(BaseActivity.this, "已成功清理缓存", Toast.LENGTH_SHORT).show();
                                            }
                                        }).show();
                                break;
                            case 16:
                                new AlertDialog.Builder(BaseActivity.this).setMessage("确认需要清理缓存？")
                                        .setNegativeButton("取消", null)
                                        .setPositiveButton("清理", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                clearWebViewCache();
                                                Toast.makeText(BaseActivity.this, "已成功清理缓存", Toast.LENGTH_SHORT).show();
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean isNotificationEnabled(Context context) {

        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;

        Class appOpsClass = null;
        /* Context.APP_OPS_MANAGER */
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);

            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void toSetting() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(localIntent);
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
        clearCache = findViewById(R.id.clear);
        if (clearCache != null) clearCache.setOnClickListener(this);
        liaotianshi = findViewById(R.id.liaotianshi);
        if (liaotianshi != null) liaotianshi.setOnClickListener(this);
        zaixiantouzhu = findViewById(R.id.zaixiantouzhu);
        if (zaixiantouzhu != null) zaixiantouzhu.setOnClickListener(this);
        setting = findViewById(R.id.setting);
        if (setting != null) setting.setOnClickListener(this);
        xianlujiance = findViewById(R.id.xljc);
        if (xianlujiance != null) xianlujiance.setOnClickListener(this);
        showMenu = findViewById(R.id.show_menu);
        if (showMenu != null) showMenu.setOnClickListener(this);

        back_img = findViewById(R.id.back_img);
        if (back_img != null) back_img.setOnClickListener(this);
        refresh_img = findViewById(R.id.refresh_img);
        if (refresh_img != null) refresh_img.setOnClickListener(this);
        goForward_img = findViewById(R.id.go_forward_img);
        if (goForward_img != null) goForward_img.setOnClickListener(this);
        home_img = findViewById(R.id.home_img);
        if (home_img != null) home_img.setOnClickListener(this);
        clear_img = findViewById(R.id.clear_img);
        if (clear_img != null) clear_img.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v == back|| v == back_img) {
            if (x5WebView.canGoBack()) {
                x5WebView.goBack();
            }
        } else if (v == home|| v == home_img) {
            HOME = getResources().getString(R.string.home_url);
            x5WebView.loadUrl(HOME);
        } else if (v == refresh || v == refresh_img) {
            x5WebView.reload();
        } else if (v == shareBtn) {
            Share.shareWebLink(BaseActivity.this, "https://w-5.net/7bWla");
        } else if (v == moreBtn) {
            drawerLayout.openDrawer(Gravity.END);
        } else if (v == goForward || v == goForward_img) {
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
        } else if (v == showMenu) {
            togoMenu();
        } else if (v == liaotianshi) {
            x5WebView.loadUrl("http://lcc108.com");
        } else if (v == zaixiantouzhu) {
            x5WebView.loadUrl("http://lcc13.com");
        } else if (v == clearCache || v == clear_img) {
            new AlertDialog.Builder(BaseActivity.this).setMessage("确认需要清理缓存？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("清理", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearWebViewCache();
                            Toast.makeText(BaseActivity.this, "已成功清理缓存", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        } else if (v == setting) {
//            final String items[] = {"清空缓存", "刷新", "主页", "取消"};
//            ArrayAdapter<String>  adapter= new ArrayAdapter<String>(BaseActivity.this,android.R.layout.simple_list_item_1,items);
            final View view = getLayoutInflater().inflate(R.layout.dialog_sheet_item, null);
            rootView.addView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            view.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rootView.removeView(view);
                    new AlertDialog.Builder(BaseActivity.this).setMessage("确认需要清理缓存？")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("清理", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    clearWebViewCache();
                                    Toast.makeText(BaseActivity.this, "已成功清理缓存", Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                }
            });
            view.findViewById(R.id.home).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rootView.removeView(view);
                    loadHome();
                }
            });
            view.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rootView.removeView(view);
                    x5WebView.reload();
                }
            });

            view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rootView.removeView(view);
                }
            });
        } else if (v == xianlujiance) {
            final ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_sheet_item1, null);
            rootView.addView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            ViewGroup child = (ViewGroup) view.getChildAt(0);
            for (int i = 2; i < child.getChildCount(); i += 2) {
                TextView textView = (TextView) child.getChildAt(i);
                textView.setText("线路" + (i / 2) + ": " + new Random().nextInt(500) + "ms");
                child.getChildAt(i).setTag(i);
                child.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer tag = (Integer) v.getTag();
                        String[] target = {"http://wp2828.com"
                                , "http://wpcp5.com"
                                , "http://wpcp7.com"
                                , "http://m.wp5858.com:8083/mobile/member/login"};
                        x5WebView.loadUrl(target[tag / 2 - 1]);
                        rootView.removeView(view);
                    }
                });

            }

            view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rootView.removeView(view);
                }
            });
        }
    }

    LinearLayout togoView;

    private void togoMenu() {
        if (togoView == null) {
            togoView = (LinearLayout) getLayoutInflater().inflate(R.layout.pop_layout, null);
            for (int i = 0; i < togoView.getChildCount(); i++) {
                View child = togoView.getChildAt(i);
                child.setTag(i);
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer index = (Integer) v.getTag();
                        if (index == 0) {
                            Share.shareWebLink(BaseActivity.this, "https://www.002211.com");
                        } else if (index == 1) {

                        } else if (index == 2) {
                            new AlertDialog.Builder(BaseActivity.this).setMessage("确认需要清理缓存？")
                                    .setNegativeButton("取消", null)
                                    .setPositiveButton("清理", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            clearWebViewCache();
                                            Toast.makeText(BaseActivity.this, "已成功清理缓存", Toast.LENGTH_SHORT).show();
                                        }
                                    }).show();
                        } else if (index == 3) {
                            x5WebView.loadUrl("http://wpa.qq.com/msgrd?v=3&uin=80056738&site=qq&menu=yes");
                        }
                        rootView.removeView(togoView);
                        togoView = null;
                    }
                });
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ABOVE, R.id.bottomNavi);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            params.bottomMargin = 20;
            params.rightMargin = 10;
            rootView.addView(togoView, params);
        } else {
            rootView.removeView(togoView);
            togoView = null;
        }

    }

    /**
     * 加载悬浮导航
     */
    int floatViewDownX, floatViewDownY, finalFloatViewDownX, finalFloatViewDownY;

    protected void initFloatNavigation() {
        if (floatNavigation) {
            DisplayMetrics metric = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metric);
            final int screenW = metric.widthPixels;  // 屏幕宽度（像素）
            final int screenH = metric.heightPixels;  // 屏幕高度（像素）
            float density = metric.density;  // 屏幕密度（0.75 / 1.0 / 1.5）
            int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
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
        fullScreen = getResources().getBoolean(R.bool.full_screen);
        floatNavigation = getResources().getBoolean(R.bool.float_navigation);
        bottomNavigation = getResources().getBoolean(R.bool.bottom_navigation);
        rightSliderMenu = getResources().getBoolean(R.bool.slider_menu);
        hasguide = getResources().getBoolean(R.bool.need_guide);
    }

    void loadHome() {
        x5WebView.loadUrl(HOME);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void savePic(final String imgUrl) {
        new AlertDialog.Builder(BaseActivity.this).setTitle("").setNegativeButton("保存图片到相册", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (imgUrl.startsWith("data:image/png;base64,")) {
                    String tempimgUrl = imgUrl.replace("data:image/png;base64", "");
//                                    BaseActivity.this.mBitmap = Base64.decode(tempimgUrl, Base64.DEFAULT);
//                                    saveMyBitmap(BaseActivity.this.mBitmap,""+System.currentTimeMillis());
//                            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
//                            Result result = DecodeImage.handleQRCodeFormBitmap(bitmap);
//                            if(result!=null){
//                                Log.e("----onLongClickCallBack", ""+result.getText());
//                                webview.loadUrl(result.getText());
//                            }
                } else if (imgUrl.startsWith("http")) {
                    FileUtils.savePicture(BaseActivity.this, "" + System.currentTimeMillis(), imgUrl);
                }
            }
        }).show();
    }

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
                        BaseActivityPermissionsDispatcher.savePicWithPermissionCheck(BaseActivity.this, imgUrl);
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
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                LogUtil.e("BaseActivity","---onShowCustomView");
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
            public void onProgressChanged(WebView webView, int progress) {
                super.onProgressChanged(webView, progress);
                if (progressBarH != null) {
                    progressBarH.setProgress(progress);
                    if (progress == 100) {
                        progressBarH.setVisibility(View.INVISIBLE);
                    } else {
                        progressBarH.setVisibility(View.VISIBLE);
                    }
                }
//                if (alertProgress != null) {
//                    if (progress == 100) {
//                        alertProgress.setVisibility(View.INVISIBLE);
//                    } else {
//                        alertProgress.setVisibility(View.VISIBLE);
//                    }
//                }

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
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }
                uploadMessage = valueCallback;
                openImageChooserActivity();
                return true;
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.e("----openFileChooser", "3");
                if (singleUploadMessage != null) {
                    singleUploadMessage.onReceiveValue(null);
                }
                singleUploadMessage = uploadMsg;
                openImageChooserActivity();
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
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
                boolean outer = false;
                if(getPackageName().equals("com.fgjh.botiantang") //博天堂
                        ||getPackageName().equals("com.axiba.chijiaa")//东方竞彩
                        ||getPackageName().equals("com.sdf.caibao")){//918金红
                    final String []outerFlag = {"/huayue/gatepay","/xinpai/unionpay","/shanzhu/unionpay",
                                                "/shanzhu/unionwap","/shanzhu/unionquick","/huayue/tenpay",
                                                "/shanzhu/tenpay","/shanzhu/tenwap","/huayue/aliwap","/kehui/aliwap"};
                    for (int i = 0; i < outerFlag.length; i++) {
                        if(url.contains(outerFlag[i])){
                            outer = true;
                            break;
                        }
                    }
                }

                if(outer){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                /*---------*/
                try {

                    if (url.toLowerCase().startsWith("about:blank")) {
                        x5WebView.goBack();
                        return true;
                    } else if (url.toLowerCase().startsWith("intent://")) {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return true;
                    } else if (!url.toLowerCase().startsWith("http")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } else if (url.toLowerCase().contains("https://qr.alipay.com")) {
                        int index = url.toLowerCase().indexOf("https://qr.alipay.com");
                        String newUrl = url.substring(index);
                        view.loadUrl(newUrl);
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, url);
                } catch (Exception e) {
                    Log.e("----should--error", "" + e.getMessage());
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
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
                super.onReceivedSslError(view, handler, error);
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
                if (failingUrl.endsWith("*.mp4")) {
                    if (TbsVideo.canUseTbsPlayer(BaseActivity.this)) {
                        TbsVideo.openVideo(BaseActivity.this, failingUrl);
                    }
                }
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

        x5WebView.addJavascriptInterface(new AndroidJs(), "AndroidJs");
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
            if (result != null) {

                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(new Uri[]{result});
                }
                if (singleUploadMessage != null) {
                    String path = getRealPathByUri(BaseActivity.this, result);
                    singleUploadMessage.onReceiveValue(path == null ? null : Uri.fromFile(new File(path)));
                }
                singleUploadMessage = null;
                uploadMessage = null;
            }

        } else if (requestCode == FILE_CHOOSER_CAMERA) {
            File photoFile = getCameraTmpFile();
            if (uploadMessage != null) {
                Uri[] uri = new Uri[]{Uri.fromFile(photoFile)};
                uploadMessage.onReceiveValue(uri);
            }
            if (singleUploadMessage != null) {
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
            }
        }
        if (uploadMessage != null) {
            uploadMessage.onReceiveValue(null);
            uploadMessage = null;
        }
        if (singleUploadMessage != null) {
            singleUploadMessage.onReceiveValue(null);
            singleUploadMessage = null;
        }
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
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }

    public void clearWebViewCache() {
        //清理Webview缓存数据库
        try {
            x5WebView.clearCache(true);
            x5WebView.clearFormData();
            File file = getCacheDir();
            if ((file != null && file.exists()) && file.isDirectory()) {
                for (File item : file.listFiles()) {
                    item.delete();
                }
                file.delete();
            }
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e("----clearWebViewCache", "" + e.getMessage());
        }
    }

    long mills = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((getResources().getBoolean(R.bool.can_goback) && x5WebView.canGoBack())) {
//                Log.d("----BaseActivity", "onKeyDown:" + x5WebView.getUrl());
                x5WebView.goBack();
                return true;
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onDestroy() {
        if("zhinengliangzilian".equals(sihuo.app.com.kuaiqian.BuildConfig.FLAVOR)
                ||"iqc".equals(sihuo.app.com.kuaiqian.BuildConfig.FLAVOR)){
            clearWebViewCache();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
//        clearWebViewCache();
    }

    public class AndroidJs {
        @JavascriptInterface
        public void share() {
            BaseActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Share.shareWebLinkWithIcon(BaseActivity.this, x5WebView.getTitle() + "\n" + x5WebView.getUrl());
                    LogUtil.e("--AndroidJs", "run:share");
                }
            });
        }
    }
}
