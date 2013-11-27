/**
 * 
 */
package cn.seddat.zhiyu.crawler.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.zhiyu.crawler.Config;
import cn.seddat.zhiyu.crawler.Post;
import cn.seddat.zhiyu.crawler.cleaner.BYRCleaner;
import cn.seddat.zhiyu.crawler.cleaner.Cleaner;
import cn.seddat.zhiyu.crawler.cleaner.SJTUCleaner;
import cn.seddat.zhiyu.crawler.cleaner.ShuimuCleaner;
import cn.seddat.zhiyu.crawler.utils.DateHelper;
import cn.seddat.zhiyu.crawler.utils.MurmurHash;
import cn.seddat.zhiyu.crawler.utils.QRcoder;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * @author mzhgeng
 * 
 */
public class CleanerService implements Runnable {

	private final Log log = LogFactory.getLog(CleanerService.class.getSimpleName());
	private BlockingQueue<Post> queue;
	private Map<String, Cleaner> cleaners;
	private DBCollection postColl, userColl;
	private QRcoder qrcoder;

	public CleanerService(BlockingQueue<Post> queue) throws Exception {
		if (queue == null) {
			throw new IllegalArgumentException("queue is required");
		}
		this.queue = queue;
		// cleaner
		this.cleaners = new HashMap<String, Cleaner>();
		ShuimuCleaner shuimuCleaner = new ShuimuCleaner();
		cleaners.put(shuimuCleaner.getSeed(), shuimuCleaner);
		BYRCleaner byrCleaner = new BYRCleaner();
		cleaners.put(byrCleaner.getSeed(), byrCleaner);
		SJTUCleaner sjtuCleaner = new SJTUCleaner();
		cleaners.put(sjtuCleaner.getSeed(), sjtuCleaner);
		// mongo
		this.postColl = MongoService.getInstance().getPostCollection();
		this.userColl = MongoService.getInstance().getUserCollection();
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
			post = this.clean(post);
			if (post != null) {
				try {
					this.saveUser(post);
					this.savePost(post);
				} catch (Exception e) {
					log.error("save post failed", e);
				}
			}
		}
	}

	protected Post clean(Post post) {
		if (post == null) {
			return null;
		}
		Cleaner cleaner = cleaners.get(post.getSeed());
		if (cleaner == null) {
			return post;
		}
		List<Post> list = null;
		try {
			list = cleaner.clean(post);
		} catch (Exception e) {
			log.error("clean " + post + " failed", e);
		}
		return list == null || list.isEmpty() ? null : list.get(0);
	}

	protected void saveUser(Post post) throws Exception {
		if (post.getHost() == null || post.getHost().isEmpty() || post.getAuthor() == null
				|| post.getAuthor().isEmpty()) {
			log.warn("author is required");
			return;
		}
		String id = MurmurHash.getInstance().hash(post.getHost() + post.getAuthor());
		DBObject q = new BasicDBObject("_id", id);
		DBObject u = BasicDBObjectBuilder.start("uid", post.getAuthor()).add("sn", post.getHost()).get();
		BufferedImage img = qrcoder.encode(JSON.serialize(u));
		File iconPath = new File(Config.getInstance().getUserIconPath());
		iconPath.mkdirs();
		String icon = id + ".png";
		ImageIO.write(img, "png", new File(iconPath, icon));
		u.put("icon", icon);
		userColl.update(q, new BasicDBObject("$set", u), true, false);
		log.info("update user " + id + " " + post.getAuthor() + " " + icon);
		post.setAuthor(id);
	}

	protected void savePost(Post post) throws Exception {
		if (post.getTitle() == null || post.getTitle().isEmpty() || post.getLink() == null || post.getLink().isEmpty()
				|| post.getHost() == null || post.getHost().isEmpty()) {
			log.warn("title, link and source are required");
			return;
		}
		DBObject doc = new BasicDBObject();
		doc.put("ttl", post.getTitle());
		if (post.getContent() != null && !post.getContent().isEmpty()) {
			doc.put("ctt", post.getContent());
		}
		doc.put("seed", post.getSeed());
		doc.put("sn", post.getHost());
		doc.put("sl", post.getLink());
		if (post.getType() != null && !post.getType().isEmpty()) {
			doc.put("tp", post.getType());
		}
		if (post.getCompany() != null && !post.getCompany().isEmpty()) {
			doc.put("com", post.getCompany());
		}
		if (post.getDepartment() != null && !post.getDepartment().isEmpty()) {
			doc.put("dpt", post.getDepartment());
		}
		if (post.getAddress() != null && !post.getAddress().isEmpty()) {
			doc.put("addr", post.getAddress());
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
