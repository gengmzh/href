/**
 * 
 */
package cn.seddat.href.crawler.service;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.href.crawler.Post;
import cn.seddat.href.crawler.spider.Spider;

/**
 * @author mzhgeng
 * 
 */
public class SpiderService implements Runnable {

	private final Log log = LogFactory.getLog(SpiderService.class);
	private BlockingQueue<Post> queue;
	private Spider spider;

	public SpiderService(BlockingQueue<Post> queue, Spider spider) throws Exception {
		if (queue == null) {
			throw new IllegalArgumentException("queue is required");
		}
		this.queue = queue;
		if (spider == null) {
			throw new IllegalArgumentException("crawler is required");
		}
		this.spider = spider;
	}

	@Override
	public void run() {
		List<Post> posts;
		try {
			posts = spider.crawl();
		} catch (Exception e) {
			log.error(spider.getClass().getSimpleName() + " crashed", e);
			return;
		}
		for (Post post : posts) {
			try {
				queue.put(post);
			} catch (InterruptedException e) {
				log.error("put queue failed", e);
			}
		}
	}

}
