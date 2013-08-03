/**
 * 
 */
package cn.seddat.zhiyu.crawler.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import junit.framework.TestCase;
import cn.seddat.zhiyu.crawler.Post;
import cn.seddat.zhiyu.crawler.spider.ShuimuSpider;

/**
 * @author mzhgeng
 * 
 */
public class SpiderServiceTest extends TestCase {

	private BlockingQueue<Post> queue;
	private SpiderService spiderService;

	@Override
	protected void setUp() throws Exception {
		queue = new ArrayBlockingQueue<Post>(10);
		spiderService = new SpiderService(new ShuimuSpider(), queue);
	}

	public void testRun() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.submit(spiderService);
		boolean pass = false;
		for (int i = 0; i < 30; i++) {
			Post post = queue.poll(10, TimeUnit.SECONDS);
			if (post != null) {
				pass = true;
				System.out.println(i + ": " + post);
				break;
			}
		}
		Assert.assertTrue(pass);
		executor.shutdownNow();
	}

}
