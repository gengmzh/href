/**
 * 
 */
package cn.seddat.href.client.activity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import cn.seddat.href.client.R;
import cn.seddat.href.client.service.CacheService;
import cn.seddat.href.client.service.Config;

/**
 * @author mzhgeng
 * 
 */
public class SplashActivity extends Activity {

	private final String tag = SplashActivity.class.getSimpleName();
	private CacheService cacheService;
	private ImageView image;
	private long lastModified = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.splash);
		cacheService = new CacheService(this);
		image = (ImageView) this.findViewById(R.id.splash_image);
		try {
			File file = new File(CacheService.getCacheDir(this), Config.getSplashImageName());
			if (file.exists() && file.isFile()) {
				lastModified = file.lastModified();
				image.setImageURI(Uri.parse(file.getAbsolutePath()));
			}
		} catch (Exception e) {
			Log.e(tag, "get splash image failed", e);
		}
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
				startActivity(intent);
			}
		}, 3000);
		new SplashImageTask().execute();
	}

	class SplashImageTask extends AsyncTask<Integer, Integer, String> {
		@Override
		protected String doInBackground(Integer... params) {
			String file = null;
			try {
				file = cacheService.findSplashImage();
			} catch (Exception e) {
				Log.e(tag, "find splash image failed", e);
			}
			return file;
		}

		@Override
		protected void onPostExecute(String file) {
			if (file == null) {
				return;
			}
			try {
				File f = new File(file);
				if (f.lastModified() > lastModified) {
					image.setImageURI(Uri.parse(file));
				}
			} catch (Exception ex) {
				Log.e(tag, "find splash image failed", ex);
			}
		}
	}

}
