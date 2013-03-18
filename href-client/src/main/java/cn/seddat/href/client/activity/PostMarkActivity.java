/**
 * 
 */
package cn.seddat.href.client.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import cn.seddat.href.client.R;
import cn.seddat.href.client.service.ContentService;
import cn.seddat.href.client.service.Post;
import cn.seddat.href.client.service.ToastService;

/**
 * @author mzhgeng
 * 
 */
public class PostMarkActivity extends Activity implements AdapterView.OnItemClickListener {

	private final String tag = PostMarkActivity.class.getSimpleName();
	private final int limit = 1000;
	private ContentService contentService;

	private List<Post> posts;
	private SimpleAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_mark);
		// init
		contentService = new ContentService(this);
		posts = new ArrayList<Post>();
		adapter = new SimpleAdapter(this, posts, R.layout.post_item, new String[] { "un", "ui", "com", "pt", "ttl",
				"sn", "mrk" }, new int[] { R.id.author_name, R.id.author_icon, R.id.post_company, R.id.post_time,
				R.id.post_title, R.id.post_source, R.id.post_mark });
		ListView listView = (ListView) findViewById(R.id.post_list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		List<Post> pl = null;
		try {
			pl = contentService.findMarkedPost(limit);
		} catch (Exception e) {
			Log.e(tag, "find post by cache failed", e);
		}
		if (pl != null) {
			posts.clear();
			if (!pl.isEmpty()) {
				posts.addAll(pl);
			} else {
				ToastService.toast(this, "没有收藏记录", Toast.LENGTH_SHORT);
			}
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Post post = (Post) adapter.getItem(position);
		Intent intent = new Intent(this, PostDetailActivity.class);
		for (String key : post.keySet()) {
			intent.putExtra(key, post.get(key));
		}
		this.startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		Activity parent = getParent();
		if (parent != null) {
			parent.onBackPressed();
		} else {
			super.onBackPressed();
		}
	}

}
