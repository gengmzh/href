/**
 * 
 */
package cn.seddat.href.crawler.service;

import java.security.MessageDigest;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import cn.seddat.href.crawler.Post;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * @author mzhgeng
 * 
 */
public class PostService implements Runnable {

	private final Log log = LogFactory.getLog(PostService.class);
	private BlockingQueue<Post> queue;
	private DBCollection dbColl;
	private final MessageDigest digester;

	public PostService(BlockingQueue<Post> queue, DBCollection dbColl) throws Exception {
		if (queue == null) {
			throw new IllegalArgumentException("queue is required");
		}
		this.queue = queue;
		if (dbColl == null) {
			throw new IllegalArgumentException("dbColl is required");
		}
		this.dbColl = dbColl;
		digester = MessageDigest.getInstance("MD5");
	}

	private String getId(String text) {
		StringBuffer buf = new StringBuffer();
		byte[] bytes = digester.digest(text.getBytes());
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1)
				buf.append("0").append(hex);
			else
				buf.append(hex);
		}
		return buf.toString();
	}

	@Override
	public void run() {
		while (true) {
			Post post;
			try {
				post = queue.take();
			} catch (InterruptedException e) {
				log.error("take queue failed", e);
				continue;
			}
			this.save(post);
		}
	}

	public void save(Post... posts) {
		for (Post post : posts) {
			if (post.getTitle() == null || post.getContent() == null || post.getSource() == null
					|| post.getLink() == null) {
				log.warn("illegal post " + post);
				continue;
			}
			String id = this.getId(post.getLink());
			DBObject query = new BasicDBObject("_id", new ObjectId(id));
			DBObject obj = dbColl.findOne(query, new BasicDBObject("sl", 1));
			if (obj == null) {
				obj = this.convert(post);
				obj.putAll(query);
				dbColl.insert(obj);
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
			doc.put("pt", post.getPubtime());
		}
		return doc;
	}

}
