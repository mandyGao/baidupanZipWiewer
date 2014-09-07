package com.jdapp.ddddddd.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.Header;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;


import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Editor;
import com.jakewharton.disklrucache.DiskLruCache.Snapshot;
import com.jdapp.ddddddd.App;
import com.jdapp.ddddddd.App.ZoomMode;
import com.jdapp.ddddddd.R;
import com.jdapp.ddddddd.model.FileInfo;
import com.jdapp.ddddddd.utils.Http;
import com.jdapp.ddddddd.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sonyericsson.zoom.DynamicZoomControl;
import com.sonyericsson.zoom.ImageZoomView;
import com.sonyericsson.zoom.ZoomState.AlignX;
import com.sonyericsson.zoom.ZoomState.AlignY;

public class ImageViewActivity extends Activity implements OnClickListener{

	private ZoomMode mZoomMode = ZoomMode.FIT_SCREEN;
	protected static final String TAG = "ImageBoxActivity";
	
	private DynamicZoomControl mZoomControl;
	private ImageZoomView mZoomView;
	private Bitmap mBitmap;
	private ZoomViewOnTouchListener mZoomListener;
	private Button btnNext;
	private Button btnPre;
	private FileInfo fileInfo;
	private ArrayList<String> imgUrls;
	private int currentPage;
	private DiskLruCache cache;
	private ProgressBar progressCircle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_imageview);
		progressCircle = (ProgressBar) findViewById(R.id.progress_circle);
		
		fileInfo = (FileInfo) this.getIntent().getExtras().getSerializable(App.bundleKeyFileinfo);
		btnNext = (Button) findViewById(R.id.mbtnNext);
		btnPre = (Button) findViewById(R.id.mbtnPrev);
		btnNext.setOnClickListener(this);
		btnPre.setOnClickListener(this);
		try {
			cache = DiskLruCache.open(App.APP_CACHE_DIR, 1, 1, 30 * 1024 * 1024);
		} catch (IOException e) {
			Toast.makeText(this, "init diskcache error: "+e.getMessage(), Toast.LENGTH_LONG).show();
		}
		if (null == App.sessionBaiduPan){
			Toast.makeText(this, "baidu cookie is empty,check it", Toast.LENGTH_LONG).show();
		}
		Http.setCookie(App.sessionBaiduPan);
		//load conf if exist
		SharedPreferences cur = getPreferences(MODE_PRIVATE);
		//
		currentPage = cur.getInt(fileInfo.getId(), 0);
		imgUrls = Utils.getUrls(fileInfo);
		
		mZoomControl = new DynamicZoomControl();
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hehe);
		mZoomListener = new ZoomViewOnTouchListener(getApplicationContext()){
			@Override
			public boolean onSingleTap() {
				LayoutInflater inflater = LayoutInflater.from(ImageViewActivity.this);
				View promptView = inflater.inflate(R.layout.seekbar_dialog, null);
				final TextView tv1 = (TextView) promptView.findViewById(R.id.seekbar_dialog_tv1);
				final SeekBar mSeekbar = (SeekBar) promptView.findViewById(R.id.seekbar_dialog_seekBar1);
				tv1.setText(currentPage + " / " + (imgUrls.size()-1));
				mSeekbar.setMax(imgUrls.size()-1);
				mSeekbar.setProgress(currentPage);
				mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						tv1.setText(progress + " / " + (imgUrls.size()-1));
						
					}
				});
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ImageViewActivity.this);
				alertDialogBuilder
				        .setView(promptView)
				        .setTitle(fileInfo.getName())
						.setPositiveButton("OK", new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								currentPage = mSeekbar.getProgress();
								downloadImgData(currentPage);
							}
						})
				.create()
				.show();
				return true;
			}
			
		};
		mZoomListener.setZoomControl(mZoomControl);
		
		mZoomView = (ImageZoomView) findViewById(R.id.mivPage);
		mZoomView.setZoomState(mZoomControl.getZoomState());
		mZoomView.setImage(mBitmap);
		mZoomView.setOnTouchListener(mZoomListener);
		mZoomControl.setAspectQuotient(mZoomView.getAspectQuotient());
		mZoomView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				resetZoomState();
			}
		});
		downloadImgData(currentPage);
		
	}


	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop()");
		SharedPreferences cur = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = cur.edit();
		editor.putInt(fileInfo.getId(), currentPage);
		editor.commit();
		Http.cancelAll(this);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


	private void resetZoomState() {
		Log.d(TAG,"ZoomView Width: " + mZoomView.getWidth());
		Log.d(TAG,"ZoomView Height: " + mZoomView.getHeight());
		Log.d(TAG,"AspectQuotient: " + mZoomView.getAspectQuotient().get());

		mZoomControl.getZoomState().setAlignX(AlignX.Right);
		mZoomControl.getZoomState().setAlignY(AlignY.Top);
		mZoomControl.getZoomState().setPanX(0.0f);
		mZoomControl.getZoomState().setPanY(0.0f);
		//mZoomControl.getZoomState().setZoom(2f);
		mZoomControl.getZoomState().setDefaultZoom(computeDefaultZoom(mZoomMode, mZoomView, mBitmap));
		mZoomControl.getZoomState().notifyObservers();
	}

	private float computeDefaultZoom(ZoomMode mode, ImageZoomView view, Bitmap bitmap) {
		if (view.getAspectQuotient() == null || view.getAspectQuotient().get() == Float.NaN) {
			return 1f;
		}
		if (view == null || view.getWidth() == 0 || view.getHeight() == 0) {
			return 1f;
		}
		if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
			return 1f;
		}

		if (mode == ZoomMode.FIT_SCREEN) {
			return 1f;
		}

		// aq = (bW / bH) / (vW / vH)
		float aq = view.getAspectQuotient().get();
		float zoom = 1f;

		if (mode == ZoomMode.FIT_WIDTH || mode == ZoomMode.FIT_WIDTH_AUTO_SPLIT) {
			// Over height
			if (aq < 1f) {
				zoom = 1f / aq;
			} else {
				zoom = 1f;
			}

			if (mode == ZoomMode.FIT_WIDTH_AUTO_SPLIT) {
				if (1f * bitmap.getWidth() / view.getWidth() > 1.5f && bitmap.getWidth() > bitmap.getHeight()) {
					zoom *= (2f + App.WIDTH_AUTO_SPLIT_MARGIN) / (1f + App.WIDTH_AUTO_SPLIT_MARGIN);
				}
			}
		} else if (mode == ZoomMode.FIT_HEIGHT) {
			// Over width
			if (aq > 1f) {
				zoom = aq;
			} else {
				zoom = 1f;
			}
		}

		return zoom;
	}

	/**
	 * this method implement from OnclickListener
	 * will handle btn's click event such as next page
	 */
	@Override
	public void onClick(View v) {
		Log.i(TAG, v.getId()+" onClick");
		switch(v.getId()){
		case R.id.mbtnNext:
			currentPage++;
			if (imgUrls.size() <= currentPage) currentPage = imgUrls.size()-1;
			downloadImgData(currentPage);
			break;
		case R.id.mbtnPrev:
			currentPage--;
			if (0 > currentPage) currentPage = 0;
			downloadImgData(currentPage);
			break;
		}
	}
	
	/**
	 * 首先检查缓存,没有则添加下载任务
	 * @param url
	 */
	private void downloadImgData(int index){
		int pageIndex = index;
		if (0 > index) pageIndex = 0;
		else if ( imgUrls.size() <= index) pageIndex = imgUrls.size()-1;
		final String downloadUrl = imgUrls.get(pageIndex);
		Log.d(TAG, "downloadImgData:"+downloadUrl);
		try {
			 Snapshot snapshot =  cache.get(Utils.md5(downloadUrl));
			if (null == snapshot){
				Http.get(this,downloadUrl, new AsyncHttpResponseHandler(){

					@Override
					public void onStart() {
						Log.d(TAG, "start mession: " + getRequestURI());
						if(imgUrls.get(currentPage).equals(getRequestURI().toString())){
							// current page is been loading,show progress circle
							progressCircle.setVisibility(View.VISIBLE);
						}
					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						
					}

					@Override
					public void onFinish() {
						if(imgUrls.get(currentPage).equals(getRequestURI().toString())){
							if(progressCircle.getVisibility() == View.VISIBLE){
								progressCircle.setVisibility(View.GONE);
							}
						}
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						String urlStr = getRequestURI().toString();
						Log.d(TAG, "onSuccess: " + urlStr);
						try {
							Editor editor = cache.edit(Utils.md5(urlStr));
							OutputStream out = editor.newOutputStream(0);
							out.write(responseBody);
							out.flush();
							editor.commit();
							cache.flush();
							notifyDataIsReady(urlStr);
						} catch (IOException e) {
							e.printStackTrace();
						} 
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						String response;
						try {
							response = responseBody == null ? "" : new String(responseBody, getCharset());
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							response = "";
						}
						final String curUrl = imgUrls.get(currentPage);
						if (curUrl.equals(downloadUrl)) {
							Toast.makeText(ImageViewActivity.this, "download failed, code:"+statusCode+" msg: "+response, 
									Toast.LENGTH_LONG).show();
						}
					}
					
				});
			} else {
				final String curUrl = imgUrls.get(currentPage);
				if (curUrl.equals(downloadUrl)) notifyDataIsReady(snapshot);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// only predownload 3 files
		if ( pageIndex - currentPage >= 2||pageIndex >= imgUrls.size()-1) return;
		else downloadImgData(pageIndex+1);
		
	}
	
	/**
	 * if img data is in cache and is current index
	 * switch to this img
	 */
	private void notifyDataIsReady(final String urlstr){
		final String curUrl = imgUrls.get(currentPage);
		try {
			if (curUrl.equals(urlstr)){
			    DiskLruCache.Snapshot snapShot = cache.get(Utils.md5(urlstr));  
			    if (snapShot != null) {  
			        InputStream is = snapShot.getInputStream(0);  
			        Bitmap bitmap = BitmapFactory.decodeStream(is);  
			        mZoomView.setImage(bitmap);  
			    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void notifyDataIsReady(final Snapshot snapShot){
	    InputStream is = snapShot.getInputStream(0);  
	    Bitmap bitmap = BitmapFactory.decodeStream(is);  
	    mZoomView.setImage(bitmap); 
	}
			 
	private final void debugHeaders(String TAG, Header[] headers) {
        if (headers != null) {
            StringBuilder builder = new StringBuilder();
            for (Header h : headers) {
                String _h = String.format("%s : %s", h.getName(), h.getValue());
                Log.d(TAG, _h);
                builder.append(_h);
                builder.append("\n");
            }
            Log.d(TAG, "Return Headers:"+builder.toString());
        }
    }


}
