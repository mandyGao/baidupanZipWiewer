package hotstu.github.bdzviewer.baiduapi;

import hotstu.github.bdzviewer.utils.HttpUtil;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import android.os.AsyncTask;

public class LoginWithNoParamTask extends AsyncTask<String, String, String> {

    public interface OnLoginListener {
        /**
         * 更新
         * 
         * @param msg
         */
        public void onProgress(String msg);

        /**
         * 需要验证码 image src = "http://wappass.baidu.com/cgi-bin/genimage?" +
         * vcodestr
         */
        public void onVcodeIsneeded(Map<String, String> params);

        /**
         * 失败
         */
        public void onFailed();

        public void onFailed(String msg);

        /**
         * 成功
         * 
         * @param result
         */
        public void onSuccess(String result);
        
        public void onFinish();
    }

    private String username;
    private String password;
    private Map<String, String> cachedParams;
    private String erroInfo;
    private final OnLoginListener mListener;
    private final OkHttpClient client;
    private final String nokia;
    /**
     *  1 需要验证码<br> 2 已知错误(账号密码错误) <br>  3 未知错误
     */
    private int errno = -1;

    public LoginWithNoParamTask(OnLoginListener mListener) {
        super();
        this.mListener = mListener;
        this.client = HttpUtil.getOkHttpClientinstance();
        this.client.setCookieHandler(HttpUtil.getCookieMgrinstance());
        this.nokia = HttpUtil.UA_NOKIA;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * @param params
     *            [0] = username
     * @param params
     *            [1] = password
     */
    @Override
    protected String doInBackground(String... params) {
        if (params.length < 2)
            throw new IllegalArgumentException(
                    "need at least 2 parametor, current is " + params.length);
        this.username = params[0];
        this.password = params[1];

        publishProgress("正在下载登陆页面...");
        // ####################################################################
        Request req = new Request.Builder()
                .url("http://wappass.baidu.com/passport/login")
                .addHeader("Referer", "").addHeader("User-Agent", nokia)
                .build();
        Response resp = null;
        try {
            resp = client.newCall(req).execute();
            HttpUtil.debugHeaders(resp);
            if (resp.code() != 200) {
                publishProgress("下载登陆页面失败.请检查网络连接");
                return null;
            }
            publishProgress("正在提取登陆页面信息...");
            Map<String, String> paramsDict = parseInput(resp.body().string());
            if (paramsDict == null)
                publishProgress("提取登陆页面信息失败 :(");
            paramsDict.put("username", this.username);
            paramsDict.put("password", this.password);

            StringBuilder payload = new StringBuilder();
            for (String k : paramsDict.keySet()) {
                payload.append(k + "=" + paramsDict.get(k) + "&");
            }
            payload.deleteCharAt(payload.length() - 1);
            // ####################################################################
            publishProgress("正在提交登陆...");
            RequestBody body = RequestBody.create(MediaType
                    .parse("application/x-www-form-urlencoded; charset=utf-8"),
                    payload.toString());
            Request post = new Request.Builder()
                    .url("http://wappass.baidu.com/passport/login")
                    .addHeader("Referer",
                            "http://wappass.baidu.com/passport/login")
                    .addHeader("User-Agent", nokia).post(body).build();
            Response afterPost = client.newCall(post).execute();
            HttpUtil.debugHeaders(afterPost);

            List<HttpCookie> cookies = ((CookieManager) this.client
                    .getCookieHandler()).getCookieStore().getCookies();
            for (HttpCookie c : cookies) {
                if ("BDUSS".equals(c.getName())) {
                    return String.format("%s=%s", c.getName(), c.getValue());
                }
            }
            // ##########没能获取到BDUSS###########################
            // 1 需要验证码
            // 2 账号密码错误
            // 3 未知错误
            Document soup = Jsoup.parse(afterPost.body().string());
            Elements errors = soup.select("div#error_area");
            if (errors.size() > 0) {
                erroInfo = errors.first().text();
                cachedParams = parseInput(soup);
                if (cachedParams.containsKey("verifycode")) {
                    // 1 需要验证码
                    // image src = "http://wappass.baidu.com/cgi-bin/genimage?"
                    // +
                    // vcodestr
                    errno = 1;
                } else
                    errno = 2;
            } else {
                erroInfo = "未知错误";
                errno = 3;
            }
            return null;
        } catch (IOException e) {
            publishProgress("意外错误 :" + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            // 成功
            mListener.onSuccess(result);
            mListener.onFinish();
            return;
        } else {
            if (errno == 1) {
                // 需要验证码，使用cachedParams
                mListener.onVcodeIsneeded(cachedParams);
                mListener.onFinish();
                return;
            }

            if (errno == 2) {
                // 已知错误，使用erroinfo
                mListener.onFailed(erroInfo);
                mListener.onFinish();
                return;
            } else {
                mListener.onFailed();
                mListener.onFinish();
                // 未知错误
            }
        }
        
    }

    @Override
    protected void onCancelled(String result) {
        mListener.onFinish();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        mListener.onProgress(values[0]);
    }

    private Map<String, String> parseInput(String html) {
        Document document = Jsoup.parse(html);
        return parseInput(document);
    }

    private Map<String, String> parseInput(Document document) {
        System.out.println(document.title()
                + document.outputSettings().charset());
        Element elementForm = document.select("form[action=/passport/login]")
                .first();
        if (elementForm == null) {
            return null;
        } 
        Elements inputs = elementForm.select("input");
        Map<String, String> params = new HashMap<String, String>();
        for (Element element : inputs) {
            System.out.println(String.format("form element:%s = %s",
                    element.attr("name"), element.attr("value")));
            params.put(element.attr("name"), element.attr("value"));
        }
        return params;
    }

    
}
