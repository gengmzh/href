/**
 * 
 */
package cn.seddat.href.crawler.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.href.crawler.Config;
import cn.seddat.href.crawler.Post;
import cn.seddat.href.crawler.cleaner.DefaultCleaner;
import cn.seddat.href.crawler.utils.DateHelper;
import cn.seddat.href.crawler.utils.MurmurHash;
import cn.seddat.href.crawler.utils.QRcoder;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * @author mzhgeng
 * 
 */
public class StoringService implements Runnable {

	private final Log log = LogFactory.getLog(StoringService.class.getSimpleName());
	private BlockingQueue<Post> queue;
	private DBCollection postColl, userColl;
	private DefaultCleaner cleaner;
	private QRcoder qrcoder;

	public StoringService(BlockingQueue<Post> queue, DBCollection postColl, DBCollection userColl) throws Exception {
		if (queue == null) {
			throw new IllegalArgumentException("queue is required");
		}
		this.queue = queue;
		if (postColl == null || userColl == null) {
			throw new IllegalArgumentException("dbColl is required");
		}
		this.postColl = postColl;
		this.userColl = userColl;
		cleaner = new DefaultCleaner();
		qrcoder = new QRcoder();
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
			this.saveUser(post);
			this.savePost(post);
		}
	}

	private void saveUser(Post post) throws Exception {
		if (post.getSource() == null || post.getSource().isEmpty() || post.getAuthor() == null
				|| post.getAuthor().isEmpty()) {
			log.warn("author is required");
			return;
		}
		String id = MurmurHash.getInstance().hash(post.getSource() + post.getAuthor());
		DBObject q = new BasicDBObject("_id", id);
		DBObject u = BasicDBObjectBuilder.start("uid", post.getAuthor()).add("sn", post.getSource()).get();
		BufferedImage img = qrcoder.encode(JSON.serialize(u));
		String icon = id + ".png";
		ImageIO.write(img, "png", new File(Config.getInstance().getUserIconPath(), icon));
		u.put("icon", icon);
		userColl.update(q, new BasicDBObject("$set", u), true, false);
		log.info("update user " + id + " " + post.getAuthor() + " " + icon);
		post.setAuthor(id);
	}

	private void savePost(Post post) throws Exception {
		if (post.getTitle() == null || post.getTitle().isEmpty() || post.getLink() == null || post.getLink().isEmpty()
				|| post.getSource() == null || post.getSource().isEmpty()) {
			log.warn("title, link and source are required");
			return;
		}
		DBObject doc = new BasicDBObject();
		doc.put("ttl", post.getTitle());
		if (post.getContent() != null && !post.getContent().isEmpty()) {
			doc.put("ctt", post.getContent());
		}
		doc.put("sn", post.getSource());
		doc.put("sl", post.getLink());
		if (post.getType() != null && !post.getType().isEmpty()) {
			doc.put("tp", post.getType());
		}
		if (post.getCompany() != null && !post.getCompany().isEmpty()) {
			doc.put("com", post.getCompany());
		}
		if (post.getAuthor() != null && !post.getAuthor().isEmpty()) {
			doc.put("au", post.getAuthor());
		}
		if (post.getPubtime() != null) {
			doc.put("pt", DateHelper.format(post.getPubtime()));
		}
		String id = MurmurHash.getInstance().hash(post.getTitle() + post.getLink());
		DBObject q = new BasicDBObject("_id", id);
		// Post不存在时添加创建时间
		DBObject obj = postColl.findOne(q, new BasicDBObject("ttl", true));
		if (obj == null) {
			if (post.getCreateTime() != null) {
				doc.put("ct", DateHelper.format(post.getCreateTime()));
			} else {
				doc.put("ct", DateHelper.format(new Date()));
			}
		}
		postColl.update(q, new BasicDBObject("$set", doc), true, false);
		log.info("update post " + id + " " + post.getTitle() + " " + post.getLink());
	}

}
