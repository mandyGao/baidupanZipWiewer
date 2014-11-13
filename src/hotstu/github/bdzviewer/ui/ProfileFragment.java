package hotstu.github.bdzviewer.ui;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import hotstu.github.bdzviewer.App;
import hotstu.github.bdzviewer.FileViewerActivity;
import hotstu.github.bdzviewer.LoginActivity;
import hotstu.github.bdzviewer.R;
import hotstu.github.bdzviewer.utils.HttpUtil;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileFragment extends Fragment implements OnClickListener {
    private TextView btnLogin;
    private TextView btnFileMange;
    private TextView btnLogout;
    private String username;
    private static Handler mHandler = new Handler();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_myprofile, container, false);
        
                                    //检测app中baiduapi变量值，
                 //不为空，                                                                                           如果为空，则点击登陆可选
         //         |                                                   |
        //则执行任务 获取百度头像、id，  《---------------                登陆成功
        
        btnLogin = (TextView) rootView.findViewById(R.id.btn_login);
        btnLogout = (TextView) rootView.findViewById(R.id.btn_logout);
        btnFileMange = (TextView) rootView.findViewById(R.id.btn_file_manage);
        
        btnLogin.setText("点击登录");
        btnFileMange.setText("浏览网盘");
        btnLogout.setText("退出");
        
        btnLogin.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        btnFileMange.setOnClickListener(this);
        
        return rootView;
    }
    
    

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if (App.sessionBaiduPan == null)
            return;
        if( savedInstanceState != null && savedInstanceState.getString("username")!= null){
            showUsername(savedInstanceState.getString("username"));
        }
        
        else {
            //
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    OkHttpClient client = HttpUtil.getOkHttpClientinstance();
                    Request req = new Request.Builder().url("http://passport.baidu.com/center")
                            .addHeader("Cookie", App.sessionBaiduPan)
                            .addHeader("User-Agent", HttpUtil.UA_FIREFOX)
                            .build();
                    try {
                        Response resp = client.newCall(req).execute();
                        String html = resp.body().string();
                        Document soup = Jsoup.parse(html);
                        final String name = soup.select("div#displayUsername").text();
                        mHandler.postDelayed(new Runnable() {
                            
                            @Override
                            public void run() {
                                showUsername(name);
                            }
                        }, 1000);
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                }
            }).start();
            
        }
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (username != null) {
            outState.putString("username", username);
        }
    }

   private void showUsername(String username) {
       this.username = username;
       btnLogin.setText(username+" - 已登录");
   }

    @Override
    public void onClick(View v) {
        Log.d("onClick", v.getId()+"");
        if (v.getId() == btnLogin.getId()){
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == btnFileMange.getId()){
            if (App.sessionBaiduPan != null && App.sessionBaiduPan != ""){
                Intent intent = new Intent(getActivity(), FileViewerActivity.class);
                startActivity(intent);
            } else {
                // plz login first
                Toast.makeText(getActivity(), "请先登陆", Toast.LENGTH_LONG).show();
            }
        }
        else if (v.getId() == btnLogout.getId()){
            btnLogin.setText("点击登录");
            this.username = null;
            //Toast.makeText(getActivity(), "logout clicked", Toast.LENGTH_LONG).show();
        }
        
    }
    
    
}

