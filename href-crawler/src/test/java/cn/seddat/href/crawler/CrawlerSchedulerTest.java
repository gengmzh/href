/**
 * 
 */
package cn.seddat.href.crawler;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * @author mzhgeng
 * 
 */
public class CrawlerSchedulerTest extends TestCase {

	private CrawlerScheduler scheduler;

	@Override
	protected void setUp() throws Exception {
		scheduler = new CrawlerScheduler();
	}

	public void testMongo() {
		DBCollection coll = scheduler.getDatabase().getCollection("test");
		// save
		DBObject doc = new BasicDBObject();
		doc.put("ttl", "sdfsdfs");
		doc.put("ctt", "sfsdfsdfdsfsdfsfds");
		coll.save(doc);
		System.out.println(doc);
		// find
		doc = coll.findOne(doc);
		Assert.assertNotNull(doc);
		System.out.println(doc);
		// del
		coll.remove(doc);
	}

	public void testStart() throws Exception {
		scheduler.start();
		// find
		Thread.sleep(10000);
		scheduler.shutdown(false);
	}

}
