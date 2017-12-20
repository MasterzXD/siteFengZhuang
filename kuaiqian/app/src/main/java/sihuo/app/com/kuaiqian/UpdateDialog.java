package sihuo.app.com.kuaiqian;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.FileOutputStream;
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

public class UpdateDialog extends Dialog {
    String content, url;
    TextView contentView, updateView, cancelView, progress;
    RelativeLayout download_view, notice_panel;
    ProgressBar progressBar;
    DownloadAPKTask downloadAPKTask;

    public UpdateDialog(@NonNull Context context, String message, String url) {
        this(context, R.style.LoadingDialog);
        this.content = message;
        this.url = url;
    }

    public UpdateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    void init() {

        setCancelable(false);
        setContentView(R.layout.update_dialog);
        contentView = findViewById(R.id.content);
        updateView = findViewById(R.id.update);
        cancelView = findViewById(R.id.not_now);
        download_view = findViewById(R.id.download_view);
        notice_panel = findViewById(R.id.notice_panel);
        progressBar = findViewById(R.id.progressBar);
        progress = findViewById(R.id.progress);
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        updateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notice_panel.setVisibility(View.INVISIBLE);
                download_view.setVisibility(View.VISIBLE);
                downloadAPKTask = new DownloadAPKTask();
                downloadAPKTask.execute(url);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private class DownloadAPKTask extends AsyncTask<String, Integer, String> {


        @Override
        protected String doInBackground(String... strings) {
            InputStream is = null;
            FileOutputStream file = null;
            try {
                URL url = new URL(strings[0]);
                //打开连接
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                int length = urlConnection.getContentLength();
                File savePath = new File(getContext().getExternalFilesDir(null), "temp.apk");
                int progress = 0;
                if (200 == urlConnection.getResponseCode()) {
                    //得到输入流
                    is = urlConnection.getInputStream();
                    file = new FileOutputStream(savePath);
                    byte temp[] = new byte[2 * 1024];
                    int i = 0;
                    while ((i = is.read(temp)) != -1) {
                        progress += i;
                        publishProgress(length, progress);
                        file.write(temp, 0, i);
                    }
                    file.flush();
                }
                return savePath.getAbsolutePath();
            } catch (IOException e) {
                return null;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if(file!=null){
                        file.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setMax(values[0]);
            progressBar.setProgress(values[1]);
            float pro = values[1]*100.0f/values[0];
            progress.setText(String.format("%.2f%%",pro));
//            if(pro>55){
//                progress.setTextColor(Color.parseColor("#FFFFFF"));
//            }else{
//                progress.setTextColor(Color.parseColor("#222222"));
//            }

        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(path)),
                    "application/vnd.android.package-archive");
            getContext().startActivity(intent);
            dismiss();
        }
    }


    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);

        contentView.setText(content);
    }

}
