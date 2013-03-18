/**
 * 
 */
package cn.seddat.href.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import cn.seddat.href.client.R;

/**
 * @author mzhgeng
 * 
 */
public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.splash);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
				startActivity(intent);
			}
		}, 3000);
	}

}
