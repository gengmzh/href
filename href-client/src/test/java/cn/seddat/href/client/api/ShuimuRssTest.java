/**
 * 
 */
package cn.seddat.href.client.api;

import java.util.List;

import junit.framework.TestCase;

/**
 * @author mzhgeng
 * 
 */
public class ShuimuRssTest extends TestCase {

	private ShuimuRss shuimuRss;

	@Override
	protected void setUp() throws Exception {
		shuimuRss = new ShuimuRss();
	}

	public void test_fetchRss() throws Exception {
		List<Post> posts = shuimuRss.fetchRss("http://www.newsmth.net/nForum/rss/topten");
		for (Post post : posts) {
			System.out.println(post);
		}
	}

}
