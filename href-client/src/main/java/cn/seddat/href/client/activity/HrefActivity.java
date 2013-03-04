package cn.seddat.href.client.activity;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import cn.seddat.href.client.R;
import cn.seddat.href.client.service.ToastService;
import cn.seddat.href.client.view.SlidingMenuView;

public class HrefActivity extends ActivityGroup {

	private final String tag = HrefActivity.class.getSimpleName();
	private String appName;
	private SlidingMenuView slidingMenuView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.href);
		appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
		slidingMenuView = (SlidingMenuView) findViewById(R.id.sliding_menu_view);
		slidingMenuView.init();
		this.showPostList(null);
	}

	public void showPostList(View view) {
		Intent i = new Intent(this, PostActivity.class);
		View pl = getLocalActivityManager().startActivity(PostActivity.class.getName(), i).getDecorView();
		slidingMenuView.showContent(pl);
	}

	private long backTime = 0;

	@Override
	public void onBackPressed() {
		if (slidingMenuView.isMenuShowing()) {
			Log.i(tag, "hide left menu");
			slidingMenuView.hideMenu();
		} else {
			long time = System.currentTimeMillis();
			if (time - backTime > 2000) {
				backTime = time;
				ToastService.toast(this, "再按一次退出" + appName, Toast.LENGTH_SHORT);
			} else {
				super.onBackPressed();
			}
		}
	}

}
