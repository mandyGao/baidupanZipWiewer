package hotstu.github.bdzviewer.baiduapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import hotstu.github.bdzviewer.App;
import hotstu.github.bdzviewer.db.ZipfileinfoDAO;
import hotstu.github.bdzviewer.model.FileInfo;
import hotstu.github.bdzviewer.utils.FileUtil;
import hotstu.github.bdzviewer.utils.HttpUtil;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

public class UnzipTask extends AsyncTask<String, String, FileInfo> {

    private static final String REQUESTURL = "http://pan.baidu.com/api/unzip?app_id=250528&channel=chunlei&clienttype=0&web=1&";
    private final Map<String, Object> queryDict;
    private final JsonParser json;
    private final String path;
    private final Context context;
    private final ProgressDialog mProgressDialog;

    public UnzipTask(Context context, String path) {
        super();
        this.path = path;
        this.context = context;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new OnCancelListener() {
            
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(true);
                
            }
        });
        queryDict = new HashMap<String, Object>();
        queryDict.put("subpath", "/");
        queryDict.put("limit", 500);
        queryDict.put("start", 0);
        queryDict.put("path", path);
        json = new JsonParser();
    }

    @Override
    protected FileInfo doInBackground(String... params) {
        try {
            String queryStr = HttpUtil.urlEncode(queryDict, null);
            OkHttpClient client = HttpUtil.getOkHttpClientinstance();
            client.setCookieHandler(null);
            
            Request req = new Request.Builder()
                    .url(REQUESTURL + queryStr)
                    .addHeader("Referer", "http://pan.baidu.com/disk/home")
                    .addHeader("User-Agent", HttpUtil.UA_FIREFOX)
                    .addHeader("Cookie", App.sessionBaiduPan)
                    .build();
            String jsonStr = null;
            for (int i = 0; i < 5; i++) {
                publishProgress(String.format("正在云解压...%d%%", i*25));
                if ((jsonStr = getResponseStr(client, req)) != null) {
                    publishProgress("云解压成功");
                    break;
                }
                else {
                    if (isCancelled())
                        return null;
                    sleep(5000 - i*1000);
                }
                
            }
            if (jsonStr == null) {
                publishProgress("云解压超时，请重试");
                return null;
            }
            JsonObject tmp = json.parse(jsonStr).getAsJsonObject();
            int total = tmp.get("total").getAsInt();
            if (total > 500) { // It's rare hanppen but reload it 
                publishProgress("正在获取更多...");
                queryDict.put("limit", total);
                queryStr = HttpUtil.urlEncode(queryDict, null);
                Request reqoncemore = new Request.Builder()
                .url(REQUESTURL + queryStr)
                .addHeader("Referer", "http://pan.baidu.com/disk/home")
                .addHeader("User-Agent", HttpUtil.UA_FIREFOX)
                .addHeader("Cookie", App.sessionBaiduPan)
                .build();
                if ((jsonStr = getResponseStr(client, reqoncemore)) == null){
                    publishProgress("失败...请重试");
                    return null;
                }
                tmp = json.parse(jsonStr).getAsJsonObject();
            }
            publishProgress("正在解析压缩文件信息...");
            List<String> fileArray = new ArrayList<String>();
            List<String> dirArray = new ArrayList<String>();
            JsonArray list = tmp.get("list").getAsJsonArray();
            for (JsonElement je : list) {
                int isdir = je.getAsJsonObject().get("isdir").getAsInt();
                String fileName = je.getAsJsonObject().get("file_name").getAsString();
                if (isdir == 0 && FileUtil.isImage(fileName)) {
                    //it is image file 
                    fileArray.add("/" + fileName);
                }
                else {
                    //it's a folder, we will explore the top level folders but ignore inner folders
                    dirArray.add("/" + fileName);
                }
            }
            
            for (String dirname : dirArray) {
                queryDict.put("subpath", dirname);
                String innerQueryStr = HttpUtil.urlEncode(queryDict, null);
                Request innerReq = new Request.Builder()
                .url(REQUESTURL + innerQueryStr)
                .addHeader("Referer", "http://pan.baidu.com/disk/home")
                .addHeader("User-Agent", HttpUtil.UA_FIREFOX)
                .addHeader("Cookie", App.sessionBaiduPan)
                .build();
                String innerJosnStr = getResponseStr(client, innerReq);
                if (innerJosnStr == null)
                    continue;
                JsonArray innerJa = json.parse(innerJosnStr).getAsJsonObject().get("list").getAsJsonArray();
                for (JsonElement je : innerJa) {
                    int isdir = je.getAsJsonObject().get("isdir").getAsInt();
                    String fileName = je.getAsJsonObject().get("file_name").getAsString();
                    if (isdir == 0 && FileUtil.isImage(fileName)) {
                        //it is image file 
                        fileArray.add(dirname + "/" + fileName);
                    }
                }
                
            }
            return new FileInfo(path, fileArray.size(), fileArray, 
                    String.valueOf(System.currentTimeMillis()), FileUtil.getfileName(path));
             
            
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected void onPostExecute(FileInfo result) {
        if (result != null) {
            ZipfileinfoDAO.insert(context, result);
            App.mainlistNeedReload = true;
            mProgressDialog.setMessage("成功导入文件:"+result.getName()+", 点击back键关闭本窗口");
        }
            //Toast.makeText(context, result.getName(), Toast.LENGTH_LONG).show();
//        if (mProgressDialog != null){
//            mProgressDialog.dismiss();
//        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        mProgressDialog.setMessage(values[0]);
    }

    @Override
    protected void onCancelled(FileInfo result) {
        super.onCancelled();
    }
    
    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private String getResponseStr(OkHttpClient client, Request req) throws IOException {
        Log.d("GET:", req.urlString());
        Response resp = client.newCall(req).execute();
        HttpUtil.debugHeaders(resp);
        if (resp.code() != 200) {
            return null;
        }
        String body = resp.body().string();
        JsonObject tmp = json.parse(body).getAsJsonObject();
        if (tmp.get("errno").getAsInt() != 0) {
            return null;
            //publishProgress("打开失败.请重试");
            //return null;
        }
        if (tmp.get("time") != null || tmp.get("total") == null) {
            return null;
        }
        return body;
    }
    
    

}
