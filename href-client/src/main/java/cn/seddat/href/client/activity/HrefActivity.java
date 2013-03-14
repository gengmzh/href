package cn.seddat.href.client.activity;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import cn.seddat.href.client.R;
import cn.seddat.href.client.service.ToastService;
import cn.seddat.href.client.view.SideslippingView;

public class HrefActivity extends ActivityGroup {

	private final String tag = HrefActivity.class.getSimpleName();
	private String appName;
	private SideslippingView slidingMenuView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.href);
		appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
		slidingMenuView = (SideslippingView) findViewById(R.id.sideslipping_view);
		slidingMenuView.init();
		this.onMenuClick(findViewById(R.id.sideslipping_menu_default));
	}

	private View curMenu = null;

	public void onMenuClick(View view) {
		switch (view.getId()) {
		case R.id.sideslipping_menu_default:
			if (curMenu != null) {
				curMenu.setBackgroundColor(android.R.color.transparent);
			}
			view.setBackgroundColor(R.color.more_darker_gray);
			curMenu = view;
			this.showContent(PostActivity.class);
			break;
		case R.id.sideslipping_menu_recommend:
			if (curMenu != null) {
				curMenu.setBackgroundColor(android.R.color.transparent);
			}
			view.setBackgroundColor(R.color.more_darker_gray);
			curMenu = view;
			ToastService.toast(this, "敬请期待", Toast.LENGTH_SHORT);
			break;
		case R.id.sideslipping_menu_mark:
			if (curMenu != null) {
				curMenu.setBackgroundColor(android.R.color.transparent);
			}
			view.setBackgroundColor(R.color.more_darker_gray);
			curMenu = view;
			ToastService.toast(this, "敬请期待", Toast.LENGTH_SHORT);
			break;
		case R.id.sideslipping_menu_feedback:
			this.startActivity(new Intent(this, FeedbackActivity.class));
			break;
		case R.id.sideslipping_menu_about:
			this.startActivity(new Intent(this, AboutActivity.class));
			break;
		}
	}

	private void showContent(Class<? extends Activity> activityClass) {
		Intent i = new Intent(this, activityClass);
		Window window = getLocalActivityManager().startActivity(activityClass.getName(), i);
		slidingMenuView.showContent(window.getDecorView());
	}

	private long backTime = 0;

	@Override
	public void onBackPressed() {
		if (curMenu == null || curMenu.getId() == R.id.sideslipping_menu_default) {
			if (slidingMenuView.isMenuShowing()) {
				Log.i(tag, "hide left menu");
				slidingMenuView.scrollRight();
			} else {
				long time = System.currentTimeMillis();
				if (time - backTime > 2000) {
					backTime = time;
					ToastService.toast(this, "再按一次退出" + appName, Toast.LENGTH_SHORT);
				} else {
					super.onBackPressed();
				}
			}
		} else {
			this.onMenuClick(findViewById(R.id.sideslipping_menu_default));
		}
	}

}
