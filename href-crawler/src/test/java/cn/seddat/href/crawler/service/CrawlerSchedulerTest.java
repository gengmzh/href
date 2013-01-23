/**
 * 
 */
package cn.seddat.href.crawler.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import junit.framework.Assert;
import junit.framework.TestCase;

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

}
