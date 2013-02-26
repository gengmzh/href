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
		listView.init(new RefreshPostListener(), R.layout.post_item, new String[] { "unm", "com", "pt", "ttl", "sn",
				"mrk" }, new int[] { R.id.author_name, R.id.post_company, R.id.post_time, R.id.post_title,
				R.id.post_source, R.id.post_mark });
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
