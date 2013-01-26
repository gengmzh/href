/**
 * 
 */
package cn.seddat.href.crawler.spider;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import cn.seddat.href.crawler.Post;

/**
 * @author mzhgeng
 * 
 */
public class ShuimuSpiderTest extends TestCase {

	private ShuimuSpider spider;

	protected void setUp() throws Exception {
		spider = new ShuimuSpider();
	}

	public void testCrawl() throws Exception {
		List<Post> pl = spider.crawl();
		Assert.assertFalse(pl.isEmpty());
		System.out.println("total: " + pl.size());
		for (Post p : pl.subList(0, 10)) {
			System.out.println(p);
		}
	}

}
