/**
 * 
 */
package cn.seddat.zhiyu.crawler.spider;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import cn.seddat.zhiyu.crawler.Post;

/**
 * @author mzhgeng
 * 
 */
public class BYRSpiderTest extends TestCase {

	private BYRSpider spider;

	protected void setUp() throws Exception {
		spider = new BYRSpider();
	}

	public void testCrawl() throws Exception {
		List<Post> pl = spider.crawl();
		Assert.assertFalse(pl.isEmpty());
		System.out.println("total: " + pl.size());
		for (Post p : pl.subList(0, 20)) {
			System.out.println(p);
		}
	}

}
