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
import cn.seddat.href.crawler.utils.DateHelper;
import cn.seddat.href.crawler.utils.MurmurHash;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * @author mzhgeng
 * 
 */
public class PostService implements Runnable {

	private final Log log = LogFactory.getLog(PostService.class.getSimpleName());
	private BlockingQueue<Post> queue;
	private DBCollection dbColl;
	private CleanerService cleanerService;

	public PostService(BlockingQueue<Post> queue, DBCollection dbColl) throws Exception {
		if (queue == null) {
			throw new IllegalArgumentException("queue is required");
		}
		this.queue = queue;
		if (dbColl == null) {
			throw new IllegalArgumentException("dbColl is required");
		}
		this.dbColl = dbColl;
		cleanerService = new CleanerService();
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
		List<Post> pl = cleanerService.clean(posts);
		for (Post post : pl) {
			String id = MurmurHash.getInstance().hash(post.getTitle() + post.getLink());
			DBObject query = new BasicDBObject("_id", id);
			DBObject obj = dbColl.findOne(query, new BasicDBObject("sl", 1));
			if (obj == null) {
				obj = this.convert(post);
				obj.putAll(query);
				dbColl.insert(obj);
				log.info("insert post " + post.getTitle() + "," + post.getLink());
			}
		}
	}

	private DBObject convert(Post post) {
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
		doc.put("ct", DateHelper.format(new Date()));
		return doc;
	}

}
