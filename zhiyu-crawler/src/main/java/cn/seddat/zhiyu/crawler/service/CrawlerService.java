/**
 * 
 */
package cn.seddat.zhiyu.crawler.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.zhiyu.crawler.Config;
import cn.seddat.zhiyu.crawler.Post;
import cn.seddat.zhiyu.crawler.spider.BDWMSpider;
import cn.seddat.zhiyu.crawler.spider.BYRSpider;
import cn.seddat.zhiyu.crawler.spider.BaiduSpider;
import cn.seddat.zhiyu.crawler.spider.SJTUSpider;
import cn.seddat.zhiyu.crawler.spider.ShuimuSpider;
import cn.seddat.zhiyu.crawler.spider.Spider;

/**
 * @author mzhgeng
 * 
 */
public class CrawlerService {

	private static final Log log = LogFactory.getLog(CrawlerService.class.getSimpleName());
	private BlockingQueue<Post> queue;
	private ScheduledExecutorService scheduledExecutor;

	public CrawlerService() throws Exception {
		// service
		queue = new ArrayBlockingQueue<Post>(Config.getInstance().getQueueSize());
		scheduledExecutor = Executors.newScheduledThreadPool(Config.getInstance().getThreadPoolSize());
	}

	public void start() throws Exception {
		// crawler
		log.info("spider starts...");
		Map<String, Spider> spiders = new HashMap<String, Spider>();
		// shuimu spider
		ShuimuSpider shuimuSpider = new ShuimuSpider();
		spiders.put(shuimuSpider.getSeed(), shuimuSpider);
		// byr spider
		BYRSpider byrSpider = new BYRSpider();
		spiders.put(byrSpider.getSeed(), byrSpider);
		// baidu spider
		BaiduSpider baiduSpider = new BaiduSpider();
		spiders.put(baiduSpider.getSeed(), baiduSpider);
		// bdwm spider
		BDWMSpider bdwmSpider = new BDWMSpider();
		spiders.put(bdwmSpider.getSeed(), bdwmSpider);
		// sjtu spider
		SJTUSpider sjtuSpider = new SJTUSpider();
		spiders.put(sjtuSpider.getSeed(), sjtuSpider);
		// schedule
		String[] seeds = Config.getInstance().getSpiderSeed();
		for (String seed : seeds) {
			Spider spider = spiders.get(seed);
			if (spider != null) {
				log.info(seed + " spider starts...");
				SpiderService spiderService = new SpiderService(spider, queue);
				long delay = Config.getInstance().getSipderInitialDelay(seed);
				long period = Config.getInstance().getSpiderPeriod(seed);
				scheduledExecutor.scheduleWithFixedDelay(spiderService, delay, period, TimeUnit.MILLISECONDS);
			}
		}
		spiders.clear();
		// post
		log.info("cleaner starts...");
		scheduledExecutor.submit(new CleanerService(queue));
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
				log.info("Crawler is closed");
				try {
					MongoService.getInstance().close();
					log.info("mongo is closed");
				} catch (Exception e) {
					log.error("close mongo failed", e);
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread(hook));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CrawlerService crawler;
		try {
			crawler = new CrawlerService();
		} catch (Exception e) {
			log.error("create crawler failed", e);
			System.exit(1);
			return;
		}
		crawler.registerShutdownHook();
		try {
			crawler.start();
			log.info("crawler startup...");
		} catch (Exception e) {
			log.error("start crawler failed", e);
			System.exit(1);
		}
	}

}
