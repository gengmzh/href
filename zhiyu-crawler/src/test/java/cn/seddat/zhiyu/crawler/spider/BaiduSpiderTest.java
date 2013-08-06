/**
 * 
 */
package cn.seddat.zhiyu.crawler.spider;

import java.util.List;

import org.junit.Ignore;

import junit.framework.Assert;
import junit.framework.TestCase;
import cn.seddat.zhiyu.crawler.Post;

/**
 * @author mzhgeng
 * 
 */
@Ignore
public class BaiduSpiderTest extends TestCase {

	private BaiduSpider spider;

	protected void setUp() throws Exception {
		spider = new BaiduSpider();
		spider.setCrawlAllPages(false);
	}

	public void testCrawl() throws Exception {
		List<Post> pl = spider.crawl();
		Assert.assertFalse(pl.isEmpty());
		System.out.println("total: " + pl.size());
		for (Post p : pl) {
			System.out.println(p);
		}
	}

}
