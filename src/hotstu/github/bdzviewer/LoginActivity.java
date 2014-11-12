package hotstu.github.bdzviewer;

import hotstu.github.bdzviewer.baiduapi.LoginWithNoParamTask;
import hotstu.github.bdzviewer.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends ActionBarActivity implements OnClickListener{
    
    TextView tvUsername;
    TextView tvPassword;
    TextView tvPhilosophy;
    Button btnLogin;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        tvUsername = (TextView) findViewById(R.id.login_username);
        tvPassword = (TextView) findViewById(R.id.login_password);
        tvPhilosophy = (TextView) findViewById(R.id.login_philosophy);
        btnLogin = (Button) findViewById(R.id.login_button);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final CharSequence username = tvUsername.getText();
        final CharSequence password = tvPassword.getText();
        btnLogin.setClickable(false);
        if (username != null && password != null 
                && password.length() >= 6 && username.length() >= 5) {
            
            LoginWithNoParamTask task = new LoginWithNoParamTask(
                    new LoginWithNoParamTask.OnLoginListener() {
                
                @Override
                public void onSuccess(String result) {
                    try {
                        savebaduSessions(result);
                        Utils.LoadSessions(LoginActivity.this);
                        tvPhilosophy.setText("成功");
                    } catch (IOException e) {
                        tvPhilosophy.setText("获取成功，但是写入失败");
                        e.printStackTrace();
                    }
                    
                    
                }
                
                @Override
                public void onProgress(String msg) {
                    tvPhilosophy.setText(msg);
                    
                }
                
                @Override
                public void onFailed() {
                    tvPhilosophy.setText("出现了来历不明的错误");
                    //toast("onFailed");
                    
                }

                @Override
                public void onVcodeIsneeded(Map<String, String> params) {
                    tvPhilosophy.setText("出现了验证码，目前不支持,请选用其他方式登陆");
                    //toast(params.get("vcodestr"));
                    
                }

                @Override
                public void onFailed(String msg) {
                    tvPhilosophy.setText("出现错误:" + msg);
                    //toast(msg);
                    
                }

                @Override
                public void onFinish() {
                    btnLogin.setClickable(true);
                    
                }
            });
            task.execute(username.toString(), password.toString());
        }
        
    }
    

     private void toast(final String msg){
         String massage = msg == null ? "null" : msg;
         Toast.makeText(this, massage, Toast.LENGTH_LONG).show();
     }
     
     public static void savebaduSessions(String bduss) throws IOException {
         File file = new File(App.APP_EXTERNAL_FILES_DIR, "session.properties");
         OutputStream fos;
         fos = new FileOutputStream(file);
         Properties properties = new Properties();
         properties.setProperty("baidu", bduss);
         properties.store(fos, null);

     }
    // Clear activity stack after user is logged in or use noHistory flag. Start
    // login activity with intent that has these flags :
    // http://developer.android.com/reference/android/content/Intent.html
    //
    // Here is one way to do it
    //
    // Intent afterLogin = new Intent(this, AfterLoginActivity.class);
    // login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    // startActivity(afterLogin);


}
