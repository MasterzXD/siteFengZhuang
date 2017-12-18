package sihuo.app.com.kuaiqian.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Administrator on 2017/12/19.
 */

public class CheckUpdate {

    CheckUpdateCallBack callBack;

    public CheckUpdate(){

    }

    public void check(Context context, CheckUpdateCallBack callBack){
        this.callBack = callBack;
        new DownloadTask(context).execute("http://app.trking.today/app/update.xml");

    }
    private class DownloadTask extends AsyncTask<String, Object, HashMap> {

        Context context;
        public DownloadTask(Context context){
            this.context = context;
        }

        @Override
        protected HashMap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                //打开连接
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                if(200 == urlConnection.getResponseCode()){
                    //得到输入流
                    InputStream is =urlConnection.getInputStream();
                    HashMap map = readXML(is);
                    PackageManager pm = context.getPackageManager();
                    PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
                    if(map.get("version").toString().equalsIgnoreCase(pi.versionName)){
                        return null;
                    }else{
                        return map;
                    }
                }

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e) {
                Log.d("----DownloadTask", "doInBackground:" + e.getMessage());
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap hashMap) {
            super.onPostExecute(hashMap);
            if(callBack!=null){
                if(hashMap!=null){
                    callBack.onResult(true,hashMap.get("showVersion").toString(),hashMap.get("downloadUrl").toString());
                }else
                    callBack.onResult(false,null,null);
            }
        }
    }

    public static HashMap readXML(InputStream inStream) {

        HashMap result = new HashMap();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(inStream);

            Element root = dom.getDocumentElement();
            String version =  root.getElementsByTagName("version").item(0).getNodeValue();
            String downloadUrl =  root.getElementsByTagName("downloadurl").item(0).getNodeValue();
            String showVersion =  root.getElementsByTagName("showversion").item(0).getNodeValue();
            result.put("version",version);
            result.put("showVersion",showVersion);
            result.put("downloadUrl",downloadUrl);
            inStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public interface CheckUpdateCallBack{
        void onResult(boolean update,String newVersion,String url);
    }
}
