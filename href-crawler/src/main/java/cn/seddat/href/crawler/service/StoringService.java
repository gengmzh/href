/**
 * 
 */
package cn.seddat.href.crawler.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.href.crawler.Post;
import cn.seddat.href.crawler.cleaner.DefaultCleaner;
import cn.seddat.href.crawler.utils.DateHelper;
import cn.seddat.href.crawler.utils.MurmurHash;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * @author mzhgeng
 * 
 */
public class StoringService implements Runnable {

	private final Log log = LogFactory.getLog(StoringService.class.getSimpleName());
	private BlockingQueue<Post> queue;
	private DBCollection dbColl;
	private DefaultCleaner cleaner;

	public StoringService(BlockingQueue<Post> queue, DBCollection dbColl) throws Exception {
		if (queue == null) {
			throw new IllegalArgumentException("queue is required");
		}
		this.queue = queue;
		if (dbColl == null) {
			throw new IllegalArgumentException("dbColl is required");
		}
		this.dbColl = dbColl;
		cleaner = new DefaultCleaner();
	}

	@Override
	public void run() {
		while (true) {
			Post post;
			try {
				post = queue.take();
				log.info("[Queue] take post " + post.getTitle() + "," + post.getLink());
			} catch (InterruptedException e) {
				log.error("take queue failed", e);
				continue;
			}
			try {
				this.save(post);
			} catch (Exception e) {
				log.error("save post failed", e);
			}
		}
	}

	public void save(Post... posts) throws Exception {
		List<Post> pl = cleaner.clean(posts);
		if (pl.isEmpty()) {
			return;
		}
		for (Post post : pl) {
			String id = MurmurHash.getInstance().hash(post.getTitle() + post.getLink());
			DBObject p = this.convert(post);
			dbColl.update(new BasicDBObject("_id", id), new BasicDBObject("$set", p), true, false);
			log.info("update post " + id + " " + p);
		}
	}

	private DBObject convert(Post post) throws Exception {
		DBObject doc = new BasicDBObject();
		doc.put("ttl", post.getTitle());
		doc.put("ctt", post.getContent());
		doc.put("sn", post.getSource());
		doc.put("sl", post.getLink());
		if (post.getType() != null) {
			doc.put("tp", post.getType());
		}
		if (post.getCompany() != null) {
			doc.put("com", post.getCompany());
		}
		if (post.getAuthor() != null) {
			doc.put("au", post.getAuthor());
		}
		if (post.getPubtime() != null) {
			doc.put("pt", DateHelper.format(post.getPubtime()));
		}
		if (post.getCreateTime() != null) {
			doc.put("ct", DateHelper.format(post.getCreateTime()));
		} else {
			doc.put("ct", DateHelper.format(new Date()));
		}
		return doc;
	}

}
