/**
 * 
 */
package cn.seddat.href.client.api;

import java.util.List;

import junit.framework.Assert;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * @author mzhgeng
 * 
 */
public class PostServiceTest extends AndroidTestCase {

	private PostService postService;

	@Override
	protected void setUp() throws Exception {
		postService = new PostService();
	}

	public void testQuery() throws Exception {
		List<Post> posts = postService.query(0, null, 0);
		Assert.assertTrue(posts.isEmpty() == false);
		for (Post post : posts) {
			Log.i(PostServiceTest.class.getSimpleName(), post.toString());
		}
	}

}
