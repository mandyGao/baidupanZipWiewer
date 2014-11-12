package hotstu.github.bdzviewer.utils;

import hotstu.github.bdzviewer.App;
import hotstu.github.bdzviewer.R;
import hotstu.github.bdzviewer.model.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class Utils {
    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(
                    string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static List<String> getUrls(FileInfo f) {
        String BAIDU_FILE_PRE = "http://pcs.baidu.com/rest/2.0/pcs/file?method=unzipdownload&app_id=250528";
        List<String> urls = new ArrayList<String>();
        List<String> fileNames = f.getList();
        String path = f.getPath();
        String fileNameTem;
        String urlTem;
        Iterator<String> it = fileNames.iterator();
        while (it.hasNext()) {
            fileNameTem = it.next();
            urlTem = BAIDU_FILE_PRE + "&path=" + Uri.encode(path) + "&subpath="
                    + Uri.encode(fileNameTem);
            urls.add(urlTem);
        }
        return urls;
    }

    public static void LoadSessions(Context context) {
        Properties properties = new Properties();
        File file = new File(App.APP_EXTERNAL_FILES_DIR, "session.properties");
        try {
            FileInputStream fis = new FileInputStream(file);
            properties.load(fis);
            fis.close();
            App.sessionApi = properties.getProperty("api");
            App.sessionBaiduPan = properties.getProperty("baidu");
        } catch (FileNotFoundException e) {
            Log.e("LoadSessions", "session.properties file not found");
        } catch (IOException e) {
            Toast.makeText(
                    context,
                    context.getString(R.string.property_read_failed)
                            + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    public static void saveSessions(String json) throws JSONException,
            IOException {
        JSONObject jo = new JSONObject(json);
        File file = new File(App.APP_EXTERNAL_FILES_DIR, "session.properties");
        OutputStream fos;
        fos = new FileOutputStream(file);
        Properties properties = new Properties();
        properties.setProperty("api", jo.getString("sessionid"));
        properties.setProperty("baidu", jo.getString("bdcookie"));
        properties.store(fos, null);

    }

}
