package hotstu.github.bdzviewer.baiduapi;

import hotstu.github.bdzviewer.App;
import hotstu.github.bdzviewer.utils.HttpUtil;

import java.io.IOException;
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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

public class RESTBaiduPathLoader extends AsyncTaskLoader<List<Entity>> {
    private static final String URLBASE = "http://pan.baidu.com/api/list?channel=chunlei&clienttype=0&web=1&showempty=0&app_id=250528&";
    private final int page;
    private final int num;
    private final String dir;
    private final String order;
    
    /**
     * 如果bundle 不包含以下参数， 使用默认参数创loader
     * @param context
     * @param page start from 1
     * @param num default is 100
     * @param dir  default is "/"
     * @param order "time", "size", "name", default is name
     */
    public RESTBaiduPathLoader(Context context, Bundle bundle) {
        super(context);
        if (bundle == null) {
            this.page = 1;
            this.num = 100;
            this.dir = "/";
            this.order = "name";
        } 
        else {
            this.page = bundle.getInt("page", 1);
            this.num = bundle.getInt("num", 100);
            String swap = bundle.getString("dir");
            this.dir = swap == null ? "/" : swap;
            swap = bundle.getString("order");
            this.order = swap == null ? "name" : swap;
        }
        
    }
    
    public RESTBaiduPathLoader(Context context) {
        super(context);
        
        this.page = 1;
        this.num = 100;
        this.dir = "/";
        this.order = "name";
    }
    
    
    public String getPath() {
        return dir;
    }

    @Override
    public List<Entity> loadInBackground() {
        try {
            return load(fetch());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
    private String fetch() throws IOException {
        Map<String, Object> q = new HashMap<String, Object>();
        q.put("page", page);
        q.put("num", num);
        q.put("dir", dir);
        q.put("order", order);
        String queryUrl = URLBASE + HttpUtil.urlEncode(q, null);
        System.out.println("GET:" + queryUrl);
        Request req = new Request.Builder().url(queryUrl)
                .addHeader("Cookie", App.sessionBaiduPan)
                .addHeader("Referer", "http://pan.baidu.com/disk/home")
                .addHeader("User-Agent", HttpUtil.UA_FIREFOX)
                .build();
        OkHttpClient client = HttpUtil.getOkHttpClientinstance();
        Response resp = client.newCall(req).execute();
        HttpUtil.debugHeaders(resp);
        return resp.body().string();
        
    }
    private List<Entity> load(final String s) {
        //String s = "{\"errno\":0,\"list\":[{\"server_filename\":\"apps\",\"category\":6,\"fs_id\":776032763540511,\"dir_empty\":0,\"server_ctime\":1411432931,\"local_mtime\":1411432931,\"size\":0,\"path\":\"\\/apps\",\"local_ctime\":1411432931,\"empty\":0,\"server_mtime\":1411432931,\"isdir\":1},{\"server_filename\":\"shared\",\"category\":6,\"fs_id\":8154876055547,\"dir_empty\":1,\"server_ctime\":1410616514,\"local_mtime\":1410616514,\"size\":0,\"path\":\"\\/shared\",\"local_ctime\":1410616514,\"empty\":0,\"server_mtime\":1410616514,\"isdir\":1},{\"server_mtime\":1415222494,\"category\":6,\"fs_id\":664309793166151,\"server_ctime\":1415222494,\"local_mtime\":1415222494,\"size\":355765673,\"isdir\":0,\"path\":\"\\/[\\u58eb\\u90ce\\u6b63\\u5b97] W\\u30fbTAILS CAT 2.zip\",\"local_ctime\":1415222494,\"md5\":\"cc69f91600af20e940d7a8706c57ac6e\",\"server_filename\":\"[\\u58eb\\u90ce\\u6b63\\u5b97] W\\u30fbTAILS CAT 2.zip\"},{\"server_mtime\":1415222481,\"category\":7,\"fs_id\":10433063580594,\"server_ctime\":1415222481,\"local_mtime\":1415222481,\"size\":13978,\"isdir\":0,\"path\":\"\\/[\\u58eb\\u90ce\\u6b63\\u5b97] W\\u30fbTAILS CAT 2.zip.torrent\",\"local_ctime\":1415222481,\"md5\":\"d0ed20d1b2a65aea800545d7298de5fe\",\"server_filename\":\"[\\u58eb\\u90ce\\u6b63\\u5b97] W\\u30fbTAILS CAT 2.zip.torrent\"},{\"server_mtime\":1414867342,\"category\":6,\"fs_id\":905436685833674,\"server_ctime\":1414867342,\"local_mtime\":1414867342,\"size\":36159117,\"isdir\":0,\"path\":\"\\/[Masamune Shirow] GREASEBERRIES.rar\",\"local_ctime\":1414867342,\"md5\":\"6716436cdd87f8485fcfa4c2c5750e38\",\"server_filename\":\"[Masamune Shirow] GREASEBERRIES.rar\"},{\"server_mtime\":1414867332,\"category\":7,\"fs_id\":583283669443842,\"server_ctime\":1414867332,\"local_mtime\":1414867332,\"size\":6429,\"isdir\":0,\"path\":\"\\/[Masamune Shirow] GREASEBERRIES.rar.torrent\",\"local_ctime\":1414867332,\"md5\":\"de2f6aa36e8e7e32a174ea39863b00b2\",\"server_filename\":\"[Masamune Shirow] GREASEBERRIES.rar.torrent\"},{\"server_mtime\":1414862028,\"category\":6,\"fs_id\":10965986586875,\"server_ctime\":1414862028,\"local_mtime\":1414862028,\"size\":11465392,\"isdir\":0,\"path\":\"\\/(C80) (\\u540c\\u4eba\\u8a8c) [\\u5f69\\u753b\\u5802] F-NERD COLOR (\\u65b0\\u4e16\\u7d00\\u30a8\\u30f4\\u30a1\\u30f3\\u30b2\\u30ea\\u30aa\\u30f3).zip\",\"local_ctime\":1414862028,\"md5\":\"62498513a43774eb91f11c9290bf03e6\",\"server_filename\":\"(C80) (\\u540c\\u4eba\\u8a8c) [\\u5f69\\u753b\\u5802] F-NERD COLOR (\\u65b0\\u4e16\\u7d00\\u30a8\\u30f4\\u30a1\\u30f3\\u30b2\\u30ea\\u30aa\\u30f3).zip\"},{\"server_mtime\":1414862021,\"category\":7,\"fs_id\":1105158116108881,\"server_ctime\":1414862021,\"local_mtime\":1414862020,\"size\":4024,\"isdir\":0,\"path\":\"\\/c80.torrent\",\"local_ctime\":1414862020,\"md5\":\"850b1a1e1029a245a06daf03996d8035\",\"server_filename\":\"c80.torrent\"},{\"server_mtime\":1414861994,\"category\":6,\"fs_id\":183275215867972,\"server_ctime\":1414861994,\"local_mtime\":1414861994,\"size\":50184844,\"isdir\":0,\"path\":\"\\/(C86) [\\u3042\\u308a\\u3059\\u306e\\u5b9d\\u7bb1(\\u6c34\\u9f8d\\u656c)] MERCURY SHADOW5 (\\u30bb\\u30fc\\u30e9\\u30fc\\u30e0\\u30fc\\u30f3).zip\",\"local_ctime\":1414861994,\"md5\":\"05219db9bb8c4b06b15720d5fefeb727\",\"server_filename\":\"(C86) [\\u3042\\u308a\\u3059\\u306e\\u5b9d\\u7bb1(\\u6c34\\u9f8d\\u656c)] MERCURY SHADOW5 (\\u30bb\\u30fc\\u30e9\\u30fc\\u30e0\\u30fc\\u30f3).zip\"},{\"server_mtime\":1414861986,\"category\":7,\"fs_id\":736608198080645,\"server_ctime\":1414861986,\"local_mtime\":1414861986,\"size\":31008,\"isdir\":0,\"path\":\"\\/(C86) [\\u3042\\u308a\\u3059\\u306e\\u5b9d\\u7bb1(\\u6c34\\u9f8d\\u656c)] MERCURY SHADOW5 (\\u30bb\\u30fc\\u30e9\\u30fc\\u30e0\\u30fc\\u30f3).zip.torrent\",\"local_ctime\":1414861986,\"md5\":\"0d8395e3e877b635c7e6c9bdf30113c8\",\"server_filename\":\"(C86) [\\u3042\\u308a\\u3059\\u306e\\u5b9d\\u7bb1(\\u6c34\\u9f8d\\u656c)] MERCURY SHADOW5 (\\u30bb\\u30fc\\u30e9\\u30fc\\u30e0\\u30fc\\u30f3).zip.torrent\"},{\"server_mtime\":1414846963,\"category\":6,\"fs_id\":135460771449313,\"server_ctime\":1414846963,\"local_mtime\":1414846963,\"size\":31448409,\"isdir\":0,\"path\":\"\\/(C86) [\\u5f69\\u753b\\u5802] R-Lab.CS (\\u65b0\\u4e16\\u7d00\\u30a8\\u30f4\\u30a1\\u30f3\\u30b2\\u30ea\\u30aa\\u30f3).zip\",\"local_ctime\":1414846963,\"md5\":\"1edacc9e5f728e01cb7eea5f947b3d66\",\"server_filename\":\"(C86) [\\u5f69\\u753b\\u5802] R-Lab.CS (\\u65b0\\u4e16\\u7d00\\u30a8\\u30f4\\u30a1\\u30f3\\u30b2\\u30ea\\u30aa\\u30f3).zip\"},{\"server_mtime\":1414846953,\"category\":7,\"fs_id\":638403421644163,\"server_ctime\":1414846953,\"local_mtime\":1414846952,\"size\":38753,\"isdir\":0,\"path\":\"\\/(C86) [\\u5f69\\u753b\\u5802] R-Lab.CS (\\u65b0\\u4e16\\u7d00\\u30a8\\u30f4\\u30a1\\u30f3\\u30b2\\u30ea\\u30aa\\u30f3).zip.torrent\",\"local_ctime\":1414846952,\"md5\":\"17baf87f5ea7fafc1936919589e3f19e\",\"server_filename\":\"(C86) [\\u5f69\\u753b\\u5802] R-Lab.CS (\\u65b0\\u4e16\\u7d00\\u30a8\\u30f4\\u30a1\\u30f3\\u30b2\\u30ea\\u30aa\\u30f3).zip.torrent\"},{\"server_mtime\":1414838426,\"category\":6,\"fs_id\":57349067893809,\"server_ctime\":1414838426,\"local_mtime\":1414838426,\"size\":5959023,\"isdir\":0,\"path\":\"\\/(C63) [\\u5f69\\u753b\\u5802] \\u30a2\\u30c6\\u30ca&\\u30d5\\u30ec\\u30f3\\u30ba2002 (\\u30ad\\u30f3\\u30b0\\uff65\\u30aa\\u30d6\\uff65\\u30d5\\u30a1\\u30a4\\u30bf\\u30fc\\u30ba) [\\u7121\\u4fee\\u6b63].zip\",\"local_ctime\":1414838426,\"md5\":\"b91f1ff626b6b0af446e10c96ec6a258\",\"server_filename\":\"(C63) [\\u5f69\\u753b\\u5802] \\u30a2\\u30c6\\u30ca&\\u30d5\\u30ec\\u30f3\\u30ba2002 (\\u30ad\\u30f3\\u30b0\\uff65\\u30aa\\u30d6\\uff65\\u30d5\\u30a1\\u30a4\\u30bf\\u30fc\\u30ba) [\\u7121\\u4fee\\u6b63].zip\"},{\"server_mtime\":1414838418,\"category\":7,\"fs_id\":446515494461591,\"server_ctime\":1414838418,\"local_mtime\":1414838418,\"size\":7578,\"isdir\":0,\"path\":\"\\/(C63) [\\u5f69\\u753b\\u5802] \\u30a2\\u30c6\\u30ca&\\u30d5\\u30ec\\u30f3\\u30ba2002 (\\u30ad\\u30f3\\u30b0\\uff65\\u30aa\\u30d6\\uff65\\u30d5\\u30a1\\u30a4\\u30bf\\u30fc\\u30ba) [\\u7121\\u4fee\\u6b63].zip.torrent\",\"local_ctime\":1414838418,\"md5\":\"c2aa01f69221c69f53c1af745f46192a\",\"server_filename\":\"(C63) [\\u5f69\\u753b\\u5802] \\u30a2\\u30c6\\u30ca&\\u30d5\\u30ec\\u30f3\\u30ba2002 (\\u30ad\\u30f3\\u30b0\\uff65\\u30aa\\u30d6\\uff65\\u30d5\\u30a1\\u30a4\\u30bf\\u30fc\\u30ba) [\\u7121\\u4fee\\u6b63].zip.torrent\"},{\"server_mtime\":1414837003,\"category\":6,\"fs_id\":64407380767120,\"server_ctime\":1414837003,\"local_mtime\":1414837003,\"size\":20270509,\"isdir\":0,\"path\":\"\\/[\\u5f69\\u753b\\u5802] \\u4eba\\u59bb\\u5973\\u6559\\u5e2b\\u307e\\u3044\\u3093\\u3055\\u3093 \\u7b2c13\\u7ae0 [\\u4e2d\\u56fd\\u7ffb\\u8a33] [\\u7a7a\\u6c17\\u7cfb\\u2606\\u6f22\\u5316].zip\",\"local_ctime\":1414837003,\"md5\":\"7de654015198f6773a9084be24a492bf\",\"server_filename\":\"[\\u5f69\\u753b\\u5802] \\u4eba\\u59bb\\u5973\\u6559\\u5e2b\\u307e\\u3044\\u3093\\u3055\\u3093 \\u7b2c13\\u7ae0 [\\u4e2d\\u56fd\\u7ffb\\u8a33] [\\u7a7a\\u6c17\\u7cfb\\u2606\\u6f22\\u5316].zip\"},{\"server_mtime\":1414836997,\"category\":7,\"fs_id\":534903874413628,\"server_ctime\":1414836997,\"local_mtime\":1414836997,\"size\":12787,\"isdir\":0,\"path\":\"\\/[\\u5f69\\u753b\\u5802] \\u4eba\\u59bb\\u5973\\u6559\\u5e2b\\u307e\\u3044\\u3093\\u3055\\u3093 \\u7b2c13\\u7ae0 [\\u4e2d\\u56fd\\u7ffb\\u8a33] [\\u7a7a\\u6c17\\u7cfb\\u2606\\u6f22\\u5316].zip.torrent\",\"local_ctime\":1414836997,\"md5\":\"3b07abaef5e7d4747fdfedf7c24e5a6c\",\"server_filename\":\"[\\u5f69\\u753b\\u5802] \\u4eba\\u59bb\\u5973\\u6559\\u5e2b\\u307e\\u3044\\u3093\\u3055\\u3093 \\u7b2c13\\u7ae0 [\\u4e2d\\u56fd\\u7ffb\\u8a33] [\\u7a7a\\u6c17\\u7cfb\\u2606\\u6f22\\u5316].zip.torrent\"},{\"server_mtime\":1414836829,\"category\":6,\"fs_id\":694856030298619,\"server_ctime\":1414836829,\"local_mtime\":1414836829,\"size\":466979517,\"isdir\":0,\"path\":\"\\/(\\u6210\\u5e74\\u30b3\\u30df\\u30c3\\u30af) [\\u8349\\u6d25\\u3066\\u308b\\u306b\\u3087] \\u30d1\\u30b3\\u30d1\\u30b3\\u3057\\u3061\\u3083\\u3046 [2014-02-01].zip\",\"local_ctime\":1414836829,\"md5\":\"5bba468ac3b77c75f387ddfab4988a9c\",\"server_filename\":\"(\\u6210\\u5e74\\u30b3\\u30df\\u30c3\\u30af) [\\u8349\\u6d25\\u3066\\u308b\\u306b\\u3087] \\u30d1\\u30b3\\u30d1\\u30b3\\u3057\\u3061\\u3083\\u3046 [2014-02-01].zip\"},{\"server_mtime\":1414836816,\"category\":7,\"fs_id\":891418176891383,\"server_ctime\":1414836816,\"local_mtime\":1414836816,\"size\":22132,\"isdir\":0,\"path\":\"\\/(\\u6210\\u5e74\\u30b3\\u30df\\u30c3\\u30af) [\\u8349\\u6d25\\u3066\\u308b\\u306b\\u3087] \\u30d1\\u30b3\\u30d1\\u30b3\\u3057\\u3061\\u3083\\u3046 [2014-02-01].zip.torrent\",\"local_ctime\":1414836816,\"md5\":\"b3476c82749947490e56964d54d89d3a\",\"server_filename\":\"(\\u6210\\u5e74\\u30b3\\u30df\\u30c3\\u30af) [\\u8349\\u6d25\\u3066\\u308b\\u306b\\u3087] \\u30d1\\u30b3\\u30d1\\u30b3\\u3057\\u3061\\u3083\\u3046 [2014-02-01].zip.torrent\"},{\"server_mtime\":1413617910,\"category\":6,\"fs_id\":856723679090976,\"server_ctime\":1413617910,\"local_mtime\":1413617910,\"size\":74982754,\"isdir\":0,\"path\":\"\\/(\\u6210\\u5e74\\u30b3\\u30df\\u30c3\\u30af) [\\u5f69\\u753b\\u5802] \\u4eba\\u59bb\\u30aa\\u30fc\\u30c9\\u30ea\\u30fc\\u3055\\u3093\\u306e\\u79d8\\u5bc6 \\uff5e30\\u6b73\\u304b\\u3089\\u306e\\u4e0d\\u826f\\u59bb\\u8b1b\\u5ea7\\uff5e Vol.2.zip\",\"local_ctime\":1413617910,\"md5\":\"a3815736f98a270650992bd00aac8788\",\"server_filename\":\"(\\u6210\\u5e74\\u30b3\\u30df\\u30c3\\u30af) [\\u5f69\\u753b\\u5802] \\u4eba\\u59bb\\u30aa\\u30fc\\u30c9\\u30ea\\u30fc\\u3055\\u3093\\u306e\\u79d8\\u5bc6 \\uff5e30\\u6b73\\u304b\\u3089\\u306e\\u4e0d\\u826f\\u59bb\\u8b1b\\u5ea7\\uff5e Vol.2.zip\"},{\"server_mtime\":1413617900,\"category\":7,\"fs_id\":28067623693264,\"server_ctime\":1413617900,\"local_mtime\":1413617900,\"size\":23303,\"isdir\":0,\"path\":\"\\/(\\u6210\\u5e74\\u30b3\\u30df\\u30c3\\u30af) [\\u5f69\\u753b\\u5802] \\u4eba\\u59bb\\u30aa\\u30fc\\u30c9\\u30ea\\u30fc\\u3055\\u3093\\u306e\\u79d8\\u5bc6 \\uff5e30\\u6b73\\u304b\\u3089\\u306e\\u4e0d\\u826f\\u59bb\\u8b1b\\u5ea7\\uff5e Vol.2.zip.torrent\",\"local_ctime\":1413617900,\"md5\":\"4523fee72a6a224729e58016da9a5a55\",\"server_filename\":\"(\\u6210\\u5e74\\u30b3\\u30df\\u30c3\\u30af) [\\u5f69\\u753b\\u5802] \\u4eba\\u59bb\\u30aa\\u30fc\\u30c9\\u30ea\\u30fc\\u3055\\u3093\\u306e\\u79d8\\u5bc6 \\uff5e30\\u6b73\\u304b\\u3089\\u306e\\u4e0d\\u826f\\u59bb\\u8b1b\\u5ea7\\uff5e Vol.2.zip.torrent\"},{\"server_mtime\":1413552147,\"category\":6,\"fs_id\":920967674813023,\"server_ctime\":1413552147,\"local_mtime\":1413552147,\"size\":130003811,\"isdir\":0,\"path\":\"\\/(\\u4e00\\u822c\\u30b3\\u30df\\u30c3\\u30af) [\\u8aeb\\u5c71\\u5275] \\u9032\\u6483\\u306e\\u5de8\\u4eba \\u7b2c14\\u5dfb.zip\",\"local_ctime\":1413552147,\"md5\":\"102261e174fba46122cfb8af3c113ed9\",\"server_filename\":\"(\\u4e00\\u822c\\u30b3\\u30df\\u30c3\\u30af) [\\u8aeb\\u5c71\\u5275] \\u9032\\u6483\\u306e\\u5de8\\u4eba \\u7b2c14\\u5dfb.zip\"},{\"server_mtime\":1410615126,\"category\":6,\"fs_id\":294087493419971,\"server_ctime\":1410615126,\"local_mtime\":1410615125,\"size\":71413289,\"isdir\":0,\"path\":\"\\/(\\u4e00\\u822c\\u5c0f\\u8aac) [\\u7b52\\u4e95\\u5eb7\\u9686] \\u65e5\\u672c\\u4ee5\\u5916\\u5168\\u90e8\\u6c88\\u6ca1(\\u6587\\u5eab\\u7248).zip\",\"local_ctime\":1410615125,\"md5\":\"b8b86525c942350f84a408c5208635b9\",\"server_filename\":\"(\\u4e00\\u822c\\u5c0f\\u8aac) [\\u7b52\\u4e95\\u5eb7\\u9686] \\u65e5\\u672c\\u4ee5\\u5916\\u5168\\u90e8\\u6c88\\u6ca1(\\u6587\\u5eab\\u7248).zip\"}],\"request_id\":7535275735197359130}";
        //String s = "\"[\\u7121\\u4fee\\u6b63].zip\"";
        JsonParser parser = new JsonParser();
        JsonObject j = parser.parse(s).getAsJsonObject();
        
        //boolean haveError = j.get("errno").getAsBoolean();
        //if (haveError) return null;
        JsonArray ja = j.get("list").getAsJsonArray();
        List<Entity> results = new ArrayList<Entity>();
        for (JsonElement je : ja) {
            System.out.println(je.getAsJsonObject().get("server_filename").getAsString());
            Entity t;
            if (je.getAsJsonObject().get("isdir").getAsInt() == 1 ) {
                t = new DirEntity();
                ((DirEntity)t).isEmpty = je.getAsJsonObject().get("empty").getAsInt() == 1 ? true : false;
            }
            else {
                t = new FileEntity();
                ((FileEntity)t).md5 = je.getAsJsonObject().get("md5").getAsString();
            }
            t.category = je.getAsJsonObject().get("category").getAsInt();
            t.filename = je.getAsJsonObject().get("server_filename").getAsString();
            t.id = je.getAsJsonObject().get("fs_id").getAsString();
            t.isdir = je.getAsJsonObject().get("isdir").getAsInt() == 1 ? true : false;
            t.path = je.getAsJsonObject().get("path").getAsString();
            t.size = je.getAsJsonObject().get("size").getAsLong();
            results.add(t);
            //System.out.println(je.getAsJsonObject().get("path").getAsString());
        }
        return results;
    }

    /**
     * 这里可以做缓存
     */
    @Override
    public void deliverResult(List<Entity> data) {
        super.deliverResult(data);
    }

    @Override
    public void onCanceled(List<Entity> data) {
        // TODO Auto-generated method stub
        super.onCanceled(data);
    }

    @Override
    public void stopLoading() {
        // TODO Auto-generated method stub
        super.stopLoading();
    }

    @Override
    protected void onForceLoad() {
        // TODO Auto-generated method stub
        super.onForceLoad();
    }

    @Override
    protected List<Entity> onLoadInBackground() {
        return loadInBackground();
    }

    /**
     * 这里要执行forceLoad方法
     */
    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * 这里要显示调用cancelLoad()
     */
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onAbandon() {
        // TODO Auto-generated method stub
        super.onAbandon();
    }

    @Override
    protected void onReset() {
        cancelLoad();
    }
    

}
