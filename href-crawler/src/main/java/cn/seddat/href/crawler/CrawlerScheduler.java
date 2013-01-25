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

import cn.seddat.href.crawler.service.PostService;
import cn.seddat.href.crawler.service.SpiderService;
import cn.seddat.href.crawler.spider.Spider;
import cn.seddat.href.crawler.spider.ShuimuSpider;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

/**
 * @author mzhgeng
 * 
 */
public class CrawlerScheduler {

	private static final Log log = LogFactory.getLog(CrawlerScheduler.class.getSimpleName());
	private BlockingQueue<Post> queue;
	private Mongo mongo;
	private DB db;
	private ScheduledExecutorService scheduledExecutor;

	public CrawlerScheduler() throws Exception {
		queue = new ArrayBlockingQueue<Post>(Config.getInstance().getQueueSize());
		// mongo
		String addr = Config.getInstance().getMongoUri();
		log.info("mongo address " + addr);
		MongoURI uri = new MongoURI(addr);
		mongo = new Mongo(uri);
		log.info("open mongo");
		db = mongo.getDB(uri.getDatabase() != null ? uri.getDatabase() : "href");
		log.info("init database " + db.getName());
		// service
		scheduledExecutor = Executors.newScheduledThreadPool(Config.getInstance().getThreadPoolSize());
	}

	public Mongo getMongo() {
		return mongo;
	}

	public DB getDatabase() {
		return db;
	}

	public void start() throws Exception {
		// post
		scheduledExecutor.submit(new PostService(queue, db.getCollection("post")));
		// crawler
		long delay = Config.getInstance().getPoliteTime();
		Spider crawler = new ShuimuSpider();
		scheduledExecutor.scheduleWithFixedDelay(new SpiderService(queue, crawler), 0, delay, TimeUnit.SECONDS);
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
				if (scheduledExecutor != null) {
					scheduledExecutor.shutdown();
					log.info("close ThreadPool");
				}
				if (mongo != null) {
					mongo.close();
					log.info("close mongo");
				}
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
