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

	private final Log log = LogFactory.getLog(SpiderService.class.getSimpleName());
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
		String tag = spider.getClass().getSimpleName();
		log.info(tag + " starts...");
		List<Post> posts = null;
		try {
			posts = spider.crawl();
			log.info(tag + " crawled " + posts.size() + " posts");
		} catch (Exception e) {
			log.error(spider.getClass().getSimpleName() + " crashed", e);
		}
		if (posts != null && !posts.isEmpty()) {
			for (Post post : posts) {
				try {
					queue.put(post);
					log.info("[Queue] put post " + post.getTitle() + "," + post.getLink());
				} catch (InterruptedException e) {
					log.error("put queue failed", e);
				}
			}
		}
		log.info(tag + " done");
	}

}
