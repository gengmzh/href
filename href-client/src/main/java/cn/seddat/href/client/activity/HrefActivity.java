package cn.seddat.href.client.activity;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
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
		this.showPostList(null);
	}

	public void onMenuClick(View view) {
		TableLayout menus = (TableLayout) view.getParent();
		for (int i = 0; i < menus.getChildCount(); i++) {
			View menu = menus.getChildAt(i);
			menu.setBackgroundColor(android.R.color.transparent);
		}
		view.setBackgroundColor(R.color.more_darker_gray);
		switch (view.getId()) {
		case R.id.sideslipping_menu_default:
			this.showPostList(view);
			break;
		case R.id.sideslipping_menu_recommend:
			ToastService.toast(this, "敬请期待", Toast.LENGTH_SHORT);
			break;
		case R.id.sideslipping_menu_mark:
			ToastService.toast(this, "敬请期待", Toast.LENGTH_SHORT);
			break;
		case R.id.sideslipping_menu_feedback:
			ToastService.toast(this, "敬请期待", Toast.LENGTH_SHORT);
			break;
		case R.id.sideslipping_menu_about:
			ToastService.toast(this, "敬请期待", Toast.LENGTH_SHORT);
			break;
		}
	}

	private void showPostList(View view) {
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
