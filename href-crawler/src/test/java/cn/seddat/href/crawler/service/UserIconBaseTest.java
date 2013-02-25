/**
 * 
 */
package cn.seddat.href.crawler.service;

import java.util.concurrent.ArrayBlockingQueue;

import junit.framework.TestCase;
import cn.seddat.href.crawler.Post;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

/**
 * @author mzhgeng
 * 
 */
public class UserIconBaseTest extends TestCase {

	private DBCollection postColl;
	private StoringService storingService;

	public void setUp() throws Exception {
		MongoService mongo = new MongoService();
		postColl = mongo.getPostCollection();
		storingService = new StoringService(new ArrayBlockingQueue<Post>(10), mongo.getPostCollection(),
				mongo.getUserCollection());
	}

	public void test_icon() throws Exception {
		DBCursor cursor = postColl.find();
		while (cursor.hasNext()) {
			Post p = this.post((BasicDBObject) cursor.next());
			storingService.save(p);
		}
	}

	private Post post(BasicDBObject obj) {
		Post p = new Post();
		p.setSource(obj.getString("sn")).setLink(obj.getString("sl"));
		p.setTitle(obj.getString("ttl")).setContent(obj.getString("ctt"));
		p.setAuthor(obj.getString("au"));
		return p;
	}

}
