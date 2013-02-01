/**
 * 
 */
package cn.seddat.href.client.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import cn.seddat.href.client.R;

/**
 * @author mzhgeng
 * 
 */
public class RefreshableListView extends LinearLayout {

	private View header;
	private ListView listView;
	private View footer;

	public RefreshableListView(Context context) {
		super(context);
		this.init();
	}

	public RefreshableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	private void init() {
		header = LayoutInflater.from(getContext()).inflate(R.layout.refreshable_header, null);
		addView(header);
		listView = new ListView(getContext());
		addView(listView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		footer = LayoutInflater.from(getContext()).inflate(R.layout.refreshable_footer, null);
		addView(footer);
		// event
		listView.setOnTouchListener(new TouchListListener());
	}

	class TouchListListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				this.onPressDown();
				break;
			case MotionEvent.ACTION_MOVE:
				this.onMove();
				break;
			case MotionEvent.ACTION_UP:
				this.onPressUp();
				break;
			}
			return false;
		}

		private void onPressDown() {
		}

		private void onMove() {
		}

		private String onPressUp() {
			return null;
		}

	}

}
