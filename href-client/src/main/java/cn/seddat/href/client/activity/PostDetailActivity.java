/**
 * 
 */
package cn.seddat.href.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import cn.seddat.href.client.R;
import cn.seddat.href.client.api.Post;
import cn.seddat.href.client.api.PostService;

/**
 * @author mzhgeng
 * 
 */
public class PostDetailActivity extends Activity {

	private final String tag = PostDetailActivity.class.getSimpleName();
	private PostDetailTask postDetailTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.post_detail);
		postDetailTask = new PostDetailTask();
		// args
		Bundle bundle = getIntent().getExtras();
		String id = bundle.getString(Intent.EXTRA_UID);
		postDetailTask.execute(id);
	}

	class PostDetailTask extends AsyncTask<String, Integer, Post> {

		private PostService postService;

		public PostDetailTask() {
			postService = new PostService();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Post doInBackground(String... params) {
			String id = params.length > 0 ? params[0] : null;
			Post post = null;
			try {
				post = postService.findById(id);
			} catch (Exception e) {
				Log.e(tag, "find post by id failed", e);
			}
			return post;
		}

		@Override
		protected void onPostExecute(Post post) {
			super.onPostExecute(post);
			if (post == null) {
				return;
			}
			TextView text = (TextView) findViewById(R.id.post_title);
			text.setText(post.getTitle());
			text = (TextView) findViewById(R.id.post_source);
			text.setText(post.getSource());
			text = (TextView) findViewById(R.id.post_time);
			text.setText(post.getShowTime());
			text = (TextView) findViewById(R.id.post_content);
			text.setText(Html.fromHtml(post.getContent()));
			text = (TextView) findViewById(R.id.post_mark);
			text.setText(String.valueOf(post.getMark()));
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
	}

}
