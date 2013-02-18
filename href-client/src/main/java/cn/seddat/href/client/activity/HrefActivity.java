/**
 * 
 */
package cn.seddat.href.client.activity;

import android.app.Activity;
import android.os.Bundle;
import cn.seddat.href.client.R;
import cn.seddat.href.client.view.RefreshableListView;

/**
 * @author mzhgeng
 * 
 */
public class HrefActivity extends Activity {

	// private final String tag = HrefActivity.class.getSimpleName();
	private RefreshableListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// show
		listView = (RefreshableListView) findViewById(R.id.post_list);
		listView.setOnRefreshListener(new RefreshPostListener());
		this.listView.refreshing();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// this.listView.refreshing();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
