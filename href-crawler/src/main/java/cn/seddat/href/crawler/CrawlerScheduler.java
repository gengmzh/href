/**
 * 
 */
package cn.seddat.href.crawler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.href.crawler.service.MongoService;
import cn.seddat.href.crawler.service.SpiderService;
import cn.seddat.href.crawler.service.StoringService;
import cn.seddat.href.crawler.spider.BaiduSpider;
import cn.seddat.href.crawler.spider.ShuimuSpider;

/**
 * @author mzhgeng
 * 
 */
public class CrawlerScheduler {

	private static final Log log = LogFactory.getLog(CrawlerScheduler.class.getSimpleName());
	private MongoService mongoService;
	private BlockingQueue<Post> queue;
	private ScheduledExecutorService scheduledExecutor;

	public CrawlerScheduler() throws Exception {
		// mongo
		mongoService = new MongoService();
		// service
		queue = new ArrayBlockingQueue<Post>(Config.getInstance().getQueueSize());
		scheduledExecutor = Executors.newScheduledThreadPool(Config.getInstance().getThreadPoolSize());
	}

	public void start() throws Exception {
		// post
		scheduledExecutor.submit(new StoringService(queue, mongoService.getPostCollection(), mongoService
				.getUserCollection()));
		// crawler
		scheduledExecutor.scheduleWithFixedDelay(new SpiderService(queue, new ShuimuSpider()), 0, Config.getInstance()
				.getShuimuPeriod(), TimeUnit.SECONDS);
		scheduledExecutor.scheduleWithFixedDelay(new SpiderService(queue, new BaiduSpider()), 0, Config.getInstance()
				.getBaiduPeriod(), TimeUnit.SECONDS);
		log.info("crawler starts...");
	}

	public void shutdown(boolean isNow) throws Exception {
		if (isNow) {
			scheduledExecutor.shutdownNow();
		} else {
			scheduledExecutor.shutdown();
		}
	}

	public void registerShutdownHook() {
		Runnable hook = new Runnable() {
			@Override
			public void run() {
				scheduledExecutor.shutdown();
				log.info("close scheduled executor");
				mongoService.close();
				log.info("close mongo service");
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread(hook));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CrawlerScheduler scheduler;
		try {
			scheduler = new CrawlerScheduler();
		} catch (Exception e) {
			log.error("create scheduler failed", e);
			System.exit(1);
			return;
		}
		scheduler.registerShutdownHook();
		try {
			scheduler.start();
		} catch (Exception e) {
			log.error("start crawlers failed", e);
			System.exit(1);
		}
	}

}
