/**
 * 
 */
package cn.seddat.href.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import cn.seddat.href.client.R;

/**
 * @author mzhgeng
 * 
 */
public class HrefActivity extends Activity {

	// private final String tag = HrefActivity.class.getSimpleName();
	private AsyncPostService asyncPostService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// show
		ListView listView = (ListView) findViewById(R.id.post_list);
		this.asyncPostService = new AsyncPostService(listView);
		this.asyncPostService.execute();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
