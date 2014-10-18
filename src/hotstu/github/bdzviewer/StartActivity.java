package hotstu.github.bdzviewer;

import hotstu.github.bdzviewer.R;
import hotstu.github.bdzviewer.db.DBHelper;
import hotstu.github.bdzviewer.utils.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * the password first time typed in will be the password.
 * @author foo
 *
 */
public class StartActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_login);
		Log.d("WatchdogActivity", "onCreate");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Handler mhandler = new Handler();
		mhandler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                SharedPreferences pre = App.getSharedPreferences();
                if (pre != null && !pre.getBoolean(SettingsActivity.KEY_PREF_USE_PASSWORD, false)){
                    Intent i = new Intent(getApplicationContext(),
                            MainActivity.class);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    return;
                }
                if (null == App.key || "".equals(App.key)) {
                    login();
                }else{
                    Intent i = new Intent(getApplicationContext(),
                            MainActivity.class);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
                
            }
        }, 1000);
		
	}

	private void login() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View promptView = inflater.inflate(R.layout.pwdform, null);
		final EditText et1 = (EditText) promptView.findViewById(R.id.etPassword);
		final CheckBox ckbShowPwd = (CheckBox) promptView.findViewById(R.id.cbShowPwd);
		ckbShowPwd.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked){
					et1.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
					et1.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}else {
					et1.setInputType(InputType.TYPE_CLASS_TEXT);
					et1.setTransformationMethod(null);
				}
				
			}
		});
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setView(promptView);
		alertDialogBuilder
				.setOnCancelListener(new OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						StartActivity.this.finish();
					}
				})
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (et1.getText().toString() != null
								&& !"".equals(et1.getText().toString())) {
							DBHelper dbh = new DBHelper(StartActivity.this);
							String hash = dbh.checkPass();
							String input = Utils.md5(Utils.md5(App.salt)+ Utils.md5(et1.getText().toString()));
							if (!"".equals(hash) && hash.equals(input)) {
								App.key = et1.getText().toString();
								dialog.dismiss();
								Intent i = new Intent(getApplicationContext(),
										MainActivity.class);
								startActivity(i);
							} else if ("".equals(hash)) {
								//the first time running this app
								String newhash = Utils.md5(Utils.md5(App.salt)+ Utils.md5(et1.getText().toString()));
								dbh.createPass(newhash);
								App.key = et1.getText().toString();
								dialog.dismiss();
								Intent i = new Intent(getApplicationContext(),
										MainActivity.class);
								startActivity(i);
							} else {
								dialog.dismiss();
							}

						} else
							dialog.dismiss();
							StartActivity.this.finish();
					}

				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								StartActivity.this.finish();
							}
						});
		AlertDialog dialog = alertDialogBuilder.create();
		dialog.show();
	}
	
	
}
