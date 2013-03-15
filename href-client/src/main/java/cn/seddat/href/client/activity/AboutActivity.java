/**
 * 
 */
package cn.seddat.href.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import cn.seddat.href.client.R;

/**
 * @author mzhgeng
 * 
 */
public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		this.setContentView(R.layout.about);
		this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_back);
		// title
		TextView title = (TextView) this.findViewById(android.R.id.title);
		title.setText(R.string.page_label_about);
	}

	public void goBack(View view) {
		this.onBackPressed();
	}

}
