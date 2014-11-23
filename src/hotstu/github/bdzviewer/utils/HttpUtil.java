package hotstu.github.bdzviewer.utils;

import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

public class HttpUtil {
    public static final String UA_FIREFOX = "Mozilla/5.0 (Windows NT 6.1; rv:32.0) Gecko/20100101 Firefox/34.0";
    public static final String UA_NOKIA = "Mozilla/5.0 (Symbian/3; Series60/5.3 Nokia701/111.020.0307; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/533.4 (KHTML, like Gecko) NokiaBrowser/7.4.1.14 Mobile Safari/533.4 3gpp-gba";
    public static final String REFERER_DEFAULT = "http://pan.baidu.com/disk/home";
    private static OkHttpClient client;
    private static CookieManager cm;

    /**
     * 获得一个OkHttpClient实例
     * 
     * @return OkHttpClient
     */
    public static OkHttpClient getOkHttpClientinstance() {
        if (client == null) {
            client = new OkHttpClient();
        }
        client.setCookieHandler(null);
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setWriteTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);
        return client;
    }
    
    /**
     * 获得一个cookie容器实例
     * 
     * @return CookieManager
     */
    public static CookieManager getCookieMgrinstance() {
        if (cm == null) {
            cm = new CookieManager();
        }
        return cm;
    }

    /**
     * 对键值对进行百分号编码
     * 
     * @param kv
     * @param charset
     *            default is "UTF-8"
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String urlEncode(Map<String, Object> kv, String charset)
            throws UnsupportedEncodingException {
        String charsetimpl = charset == null ? "UTF-8" : charset;
        StringBuilder sb = new StringBuilder();
        for (Entry<String, Object> el : kv.entrySet()) {
            sb.append(URLEncoder.encode(el.getKey(), charsetimpl)
                    + "="
                    + URLEncoder.encode(String.valueOf(el.getValue()),
                            charsetimpl) + "&");

        }
        if (sb.length() > 1)
            sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    /**
     * 打印okhttp header
     * @param resp
     */
    public static void debugHeaders(Response resp) {
        Headers headers = resp.headers();
        System.out.println(resp.code());
        
        System.out.println("########response header###########");
        for (String name : headers.names()) {
            for (String v : headers.values(name)) {
                System.out.println(String.format("%s : %s", name, v));
            }
        }
    }

}
