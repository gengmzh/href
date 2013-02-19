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
public class MongoServiceTest extends TestCase {

	private MongoService mongoService;

	@Override
	protected void setUp() throws Exception {
		mongoService = new MongoService();
	}

	public void testMongo() {
		DBCollection coll = mongoService.getDatabase().getCollection("test");
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

	@Override
	protected void tearDown() throws Exception {
		mongoService.close();
	}

}
