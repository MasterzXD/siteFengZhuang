package sihuo.app.com.kuaiqian;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Administrator on 2017/12/15.
 */

public class UpdateDialog extends Dialog
{
    UpdateDialog loadingDialog;
    String content,url;
    TextView contentView,updateView,cancelView,progress;
    RelativeLayout download_view;
    ProgressBar progressBar;

    public UpdateDialog(@NonNull Context context,String message,String url) {
        this(context,R.style.LoadingDialog);
        this.content = message;
        this.url = url;
    }

    public UpdateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    void init(){

        setCancelable(false);
        setContentView(R.layout.update_dialog);
        contentView = findViewById(R.id.content);
        updateView = findViewById(R.id.update);
        cancelView= findViewById(R.id.not_now);
        download_view = findViewById(R.id.download_view);
        progressBar = findViewById(R.id.progressBar);
        progress = findViewById(R.id.progress);
        loadingDialog = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private class DownloadAPKTask extends AsyncTask<String, Float, String> {


        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                //打开连接
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                int length = urlConnection.getContentLength();
                progressBar.setMax(length);
                publishProgress();
                if(200 == urlConnection.getResponseCode()){
                    //得到输入流
                    InputStream is =urlConnection.getInputStream();

                }
                return null;
            }catch (IOException e) {
                Log.d("----DownloadTask", "doInBackground:" + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
        }
    }


    @Override
    public void show() {
        super.show();
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.dismiss();
            }
        });
        updateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadAPKTask().execute(url);
            }
        });
        contentView.setText(content);
    }

}
