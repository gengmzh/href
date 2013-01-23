/**
 * 
 */
package cn.seddat.href.crawler.spider;

import java.util.List;

import cn.seddat.href.crawler.Post;
import cn.seddat.href.crawler.spider.ShuimuSpider;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author mzhgeng
 * 
 */
public class ShuimuSpiderTest extends TestCase {

	private ShuimuSpider spider;

	protected void setUp() throws Exception {
		spider = new ShuimuSpider();
	}

	public void testRun() throws Exception {
		List<Post> pl = spider.crawl();
		Assert.assertFalse(pl.isEmpty());
		System.out.println("total: " + pl.size());
		for (Post p : pl.subList(0, 10)) {
			System.out.println(p);
		}
	}

}
