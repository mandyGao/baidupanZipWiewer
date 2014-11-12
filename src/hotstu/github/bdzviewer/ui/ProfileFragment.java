package hotstu.github.bdzviewer.ui;

import hotstu.github.bdzviewer.App;
import hotstu.github.bdzviewer.FileViewerActivity;
import hotstu.github.bdzviewer.LoginActivity;
import hotstu.github.bdzviewer.R;
import android.content.Intent;
import android.os.Bundle;
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
                Toast.makeText(getActivity(), "plz login first", Toast.LENGTH_LONG).show();
            }
        }
        else if (v.getId() == btnLogout.getId()){
            Toast.makeText(getActivity(), "logout clicked", Toast.LENGTH_LONG).show();
        }
        
    }
    
    
}

