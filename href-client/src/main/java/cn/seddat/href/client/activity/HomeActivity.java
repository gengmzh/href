package cn.seddat.href.client.activity;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import cn.seddat.href.client.R;
import cn.seddat.href.client.service.ToastService;
import cn.seddat.href.client.service.TrackService;
import cn.seddat.href.client.view.RefreshableListView;
import cn.seddat.href.client.view.SideslippingView;

public class HomeActivity extends ActivityGroup {

	private final String tag = HomeActivity.class.getSimpleName();
	private String appName;
	private TextView title;
	private SideslippingView slidingMenuView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		this.setContentView(R.layout.home);
		this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_home);
		// title
		title = (TextView) this.findViewById(android.R.id.title);
		title.setText(R.string.app_name);
		appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
		slidingMenuView = (SideslippingView) findViewById(R.id.sideslipping_view);
		slidingMenuView.init();
		this.onMenuClick(findViewById(R.id.menu_default));
		this.sendTrack();
	}

	public void goBack(View view) {
		if (!slidingMenuView.isMenuShowing()) {
			slidingMenuView.scrollLeft();
		}
	}

	private View curMenu = null;

	public void onMenuClick(View view) {
		switch (view.getId()) {
		case R.id.menu_default:
			if (curMenu != null) {
				curMenu.setBackgroundColor(android.R.color.transparent);
			}
			view.setBackgroundColor(R.color.gray_dark);
			curMenu = view;
			title.setText(R.string.app_name);
			this.showContent(PostListActivity.class);
			break;
		case R.id.menu_recommend:
			if (curMenu != null) {
				curMenu.setBackgroundColor(android.R.color.transparent);
			}
			view.setBackgroundColor(R.color.gray_dark);
			curMenu = view;
			title.setText(R.string.page_label_recommend);
			ToastService.toast(this, "敬请期待", Toast.LENGTH_SHORT);
			break;
		case R.id.menu_mark:
			if (curMenu != null) {
				curMenu.setBackgroundColor(android.R.color.transparent);
			}
			view.setBackgroundColor(R.color.gray_dark);
			curMenu = view;
			title.setText(R.string.page_label_mark);
			this.showContent(PostMarkActivity.class);
			break;
		case R.id.menu_feedback:
			this.sendTrack();
			this.startActivity(new Intent(this, FeedbackActivity.class));
			break;
		case R.id.menu_about:
			this.sendTrack();
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
		if (curMenu == null || curMenu.getId() == R.id.menu_default || curMenu.getId() == R.id.menu_mark) {
			if (slidingMenuView.isMenuShowing()) {
				Log.i(tag, "hide left menu");
				slidingMenuView.scrollRight();
			} else {
				long time = System.currentTimeMillis();
				if (time - backTime > 2000) {
					backTime = time;
					ToastService.toast(this, "再按一次退出" + appName, Toast.LENGTH_SHORT);
				} else {
					this.addShortcut();
					super.onBackPressed();
				}
			}
		} else {
			this.onMenuClick(findViewById(R.id.menu_default));
		}
	}

	public void addShortcut() {
		SharedPreferences pref = getSharedPreferences(RefreshableListView.class.getSimpleName(), Context.MODE_PRIVATE);
		if (pref.getBoolean("shortcut-installed", false)) {
			return;
		}
		Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(this, R.drawable.icon));
		intent.putExtra(
				Intent.EXTRA_SHORTCUT_INTENT,
				new Intent(this, SplashActivity.class).setAction("android.intent.action.MAIN").addCategory(
						"android.intent.category.LAUNCHER"));
		intent.putExtra("duplicate", false);
		this.sendBroadcast(intent);
		Editor editor = pref.edit();
		editor.putBoolean("shortcut-installed", true);
		editor.commit();
		Log.i(tag, "install shortcut for " + appName);
	}

	private void sendTrack() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					TrackService.sendTrack(HomeActivity.this);
				} catch (Exception e) {
					Log.e(tag, "send track failed", e);
				}
			}
		}).start();
	}

}
