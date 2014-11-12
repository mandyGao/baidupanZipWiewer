package hotstu.github.bdzviewer;

import hotstu.github.bdzviewer.ui.FileinfoListFragment;
import hotstu.github.bdzviewer.ui.ProfileFragment;
import hotstu.github.bdzviewer.utils.Http;
import hotstu.github.bdzviewer.utils.Utils;

import org.apache.http.Header;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class MainActivity extends ActionBarActivity {
    private FileinfoListFragment fragment;
    private SlidingMenu menu;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragment = new FileinfoListFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.layout_main, fragment).commit();
        
        menu = new SlidingMenu(this);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.sliding_menu);
        getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.menu_content, new ProfileFragment())
        .commit();
                
    }

 

    @Override
    protected void onResume() {
        super.onResume();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.qrscan == item.getItemId()) {
            qrscan();
            return true;
        } 
//        else if(R.id.refresh == item.getItemId()) {
//            final ThumbnailTextAdapter adapter = (ThumbnailTextAdapter) fragment.getListAdapter();
//            if (null == App.sessionApi) {
//                Toast.makeText(this, getString(R.string.property_read_failed),
//                        Toast.LENGTH_LONG).show();
//                return true;
//            }
//            Http.setCookie(App.sessionApi);
//            Log.d("setcookie", App.sessionApi);
//            Http.get("http://ddddddd.jd-app.com/comic/api/", null,
//                    new AsyncHttpResponseHandler() {
//
//                        @Override
//                        @Deprecated
//                        public void onSuccess(int statusCode, Header[] headers,
//                                String content) {
//                            Log.d("onSuccess", content);
//                            adapter.setFileItems(MyListFragment.fileInfoFromJson(content));
//                            adapter.notifyDataSetChanged();
//                            Toast.makeText(MainActivity.this,
//                                    getString(R.string.refresh_sucess), Toast.LENGTH_LONG)
//                                    .show();
//
//                            //DBHelper dbh = new DBHelper(MainActivity.this);
//                            //dbh.addOrUpdate(content);
//                        }
//
//                        @Override
//                        @Deprecated
//                        public void onFailure(int statusCode, Header[] headers,
//                                Throwable error, String content) {
//                            Log.d("onfailure", "" + statusCode + content);
//                            Toast.makeText(
//                                    MainActivity.this,
//                                    getString(R.string.refresh_fail),
//                                    Toast.LENGTH_LONG).show();
//                        }
//
//                    });
//            return true;
//        } 
        else if (R.id.setting == item.getItemId()) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else {
            return false;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, intent);
        if (scanResult != null) {
            // handle scan result
            if (scanResult.getContents() == null
                    || scanResult.getContents().equals("")) {

            } else {
                String url = scanResult.getContents();
                Log.d("scaned url: ", "" + url);
                Http.get(this, url, new AsyncHttpResponseHandler() {
                    @Override
                    @Deprecated
                    public void onSuccess(int statusCode, Header[] headers,
                            String content) {
                        
                            try {
                                Utils.saveSessions(content);
                                Toast.makeText(MainActivity.this, getString(R.string.sync_cookie_sucess),
                                        Toast.LENGTH_LONG).show();
                                Utils.LoadSessions(MainActivity.this);
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, getString(R.string.sync_cookie_fail),
                                        Toast.LENGTH_LONG).show();
                            }
                    }

                    @Override
                    @Deprecated
                    public void onFailure(int statusCode, Header[] headers,
                            Throwable error, String content) {
                        Log.d("onfailure", "" + statusCode + content);
                        if (statusCode == 404) {
                            Toast.makeText(
                                    MainActivity.this, getString(R.string.sync_cookie_outdate),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.sync_cookie_fail),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }

        }
        // else continue with any other code you need in the method
    }

    //#########################################################
    
    
    
    //###########################################################
    private void qrscan() {
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.initiateScan();

    }


}
