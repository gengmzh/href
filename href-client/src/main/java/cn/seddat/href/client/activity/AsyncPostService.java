/**
 * 
 */
package cn.seddat.href.client.activity;

import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import cn.seddat.href.client.R;
import cn.seddat.href.client.api.Post;
import cn.seddat.href.client.api.PostService;

/**
 * @author mzhgeng
 * 
 */
public class AsyncPostService extends AsyncTask<String, Integer, List<Post>> {

	private final String tag = AsyncPostService.class.getSimpleName();
	private ListView listView;
	private PostService postService;

	public AsyncPostService(ListView listView) {
		this.listView = listView;
		this.postService = new PostService();
	}

	@Override
	protected List<Post> doInBackground(String... args) {
		this.onProgressUpdate(0);
		long startTime = 0, endTime = 0;
		if (args != null) {
			if (args.length > 0) {
				startTime = Long.parseLong(args[0]);
			}
			if (args.length > 1) {
				endTime = Long.parseLong(args[1]);
			}
		}
		this.onProgressUpdate(1);
		List<Post> posts;
		try {
			posts = postService.query(startTime, endTime);
		} catch (Exception ex) {
			Log.e(tag, "query post failed", ex);
			posts = Collections.emptyList();
		}
		this.onProgressUpdate(100);
		return posts;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		// TODO
	}

	@Override
	protected void onPostExecute(List<Post> posts) {
		super.onPostExecute(posts);
		SimpleAdapter adapter = new SimpleAdapter(listView.getContext(), posts, R.layout.post_item, new String[] {
				"au", "com", "pt", "ttl", "pv", "clk", "mrk" }, new int[] { R.id.author_name, R.id.post_company,
				R.id.post_time, R.id.post_title, R.id.post_pv, R.id.post_click, R.id.post_mark });
		listView.setAdapter(adapter);
	}

}
