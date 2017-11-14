package sihuo.app.com.kuaiqian;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

    WebView webview;
    ImageView back;
    ImageView home;
    ImageView refresh;
    final String HOME = "http://m.wangcai020.com";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        webview = findViewById(R.id.webview);
        back = findViewById(R.id.back);
        home = findViewById(R.id.home);
        refresh = findViewById(R.id.refresh);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(webview.canGoBack()){
                    webview.goBack();
                }
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webview.loadUrl(HOME);
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webview.reload();
            }
        });

        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
                jsResult.confirm();
                return super.onJsAlert(webView, s, s1, jsResult);
            }

        });
        webview.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                Log.e("----should", ""+url);
                try {
                    if(!url.startsWith("http")){
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                    view.loadUrl(url);
                    return true;
                }catch (Exception e){
//                    Log.e("----should--error", ""+e.getMessage());
                }
                return super.shouldOverrideUrlLoading(view,url);

            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest, Bundle bundle) {
                return super.shouldInterceptRequest(webView, webResourceRequest, bundle);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                Log.e("----Request", ""+url);
                return super.shouldInterceptRequest(view, url);

            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }


            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
//                Log.e("----onReceivedError", ""+error);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
//                Log.e("----onReceivedError", "failingUrl:"+failingUrl);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                Log.e("----onPageFinished", ""+url);
            }
        });

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setAllowContentAccess(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.loadUrl(HOME);
    }

    long mills = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
//            if(!webview.getUrl().equals(HOME) && webview.canGoBack()){
//                webview.goBack();
//                return true;
//            }
            if(System.currentTimeMillis()-mills>800){
                Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
                mills = System.currentTimeMillis();
            }else{
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
