/**
 * 
 */
package cn.seddat.href.client.activity;

import java.util.List;

import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import cn.seddat.href.client.R;
import cn.seddat.href.client.api.Post;
import cn.seddat.href.client.api.PostService;
import cn.seddat.href.client.view.OnRefreshListener;
import cn.seddat.href.client.view.RefreshableListView;

/**
 * @author mzhgeng
 * 
 */
public class RefreshPostListener implements OnRefreshListener {

	private PostService postService;
	private String[] from;
	private int[] to;

	public RefreshPostListener() {
		postService = new PostService();
		from = new String[] { "au", "com", "pt", "ttl", "sn", "pv", "clk", "mrk" };
		to = new int[] { R.id.author_name, R.id.post_company, R.id.post_time, R.id.post_title, R.id.post_source,
				R.id.post_pv, R.id.post_click, R.id.post_mark };
	}

	@Override
	public ListAdapter onRefreshing(RefreshableListView listView) throws Exception {
		// first
		long st = 0;
		ListAdapter adapter = listView.getListAdapter();
		if (adapter != null && adapter.getCount() > 0) {
			Post item = (Post) adapter.getItem(0);
			st = item.getCreateTime();
		}
		// query
		List<Post> posts = postService.query(st, 0);
		if (adapter != null) {
			for (int i = 0; i < adapter.getCount(); i++) {
				posts.add((Post) adapter.getItem(i));
			}
		}
		return new SimpleAdapter(listView.getContext(), posts, R.layout.post_item, from, to);
	}

	@Override
	public ListAdapter onLoading(RefreshableListView listView) throws Exception {
		// last
		long et = 0;
		ListAdapter adapter = listView.getListAdapter();
		if (adapter != null && adapter.getCount() > 0) {
			Post item = (Post) adapter.getItem(adapter.getCount() - 2);
			et = item.getCreateTime();
		}
		// query
		List<Post> posts = postService.query(0, et);
		if (adapter != null) {
			for (int i = adapter.getCount() - 1; i >= 0; i--) {
				posts.add(0, (Post) adapter.getItem(i));
			}
		}
		return new SimpleAdapter(listView.getContext(), posts, R.layout.post_item, from, to);
	}

}
