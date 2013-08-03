/**
 * 
 */
package cn.seddat.zhiyu.crawler.service;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.zhiyu.crawler.Post;
import cn.seddat.zhiyu.crawler.spider.Spider;

/**
 * @author mzhgeng
 * 
 */
public class SpiderService implements Runnable {

	private final Log log = LogFactory.getLog(SpiderService.class.getSimpleName());
	private BlockingQueue<Post> queue;
	private Spider spider;

	public SpiderService(Spider spider, BlockingQueue<Post> queue) throws Exception {
		if (spider == null) {
			throw new IllegalArgumentException("spider is required");
		}
		this.spider = spider;
		if (queue == null) {
			throw new IllegalArgumentException("queue is required");
		}
		this.queue = queue;
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
