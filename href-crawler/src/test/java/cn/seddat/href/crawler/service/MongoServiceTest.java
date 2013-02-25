/**
 * 
 */
package cn.seddat.href.crawler.service;

import java.io.InputStream;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

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

	public void test_image() throws Exception {
		InputStream img = MongoServiceTest.class.getClassLoader().getResourceAsStream(
				"cn/seddat/href/crawler/Michael_QRCode.png");
		GridFS gridfs = new GridFS(mongoService.getDatabase(), "test");
		GridFSInputFile file = gridfs.createFile(img);
		file.setFilename("Michael_QRCode.png");
		file.setMetaData(new BasicDBObject("sn", "test"));
		file.save();

		List<GridFSDBFile> files = gridfs.find("Michael_QRCode.png");
		for (GridFSDBFile f : files) {
			System.out.println(f);
			// f.writeTo(System.out);
			System.out.println();
			gridfs.remove(f.getFilename());
		}

	}

	// @Ignore
	// public void test_seq() throws Exception {
	// DBCollection coll = mongoService.getPostCollection();
	// DBObject keys = new BasicDBObject("ttl", 1);
	// keys.put("ct", 1);
	// DBCursor cursor = coll.find(null, keys).sort(new BasicDBObject("ct", 1));
	// long i = 0;
	// while (cursor.hasNext()) {
	// i++;
	// DBObject p = cursor.next();
	// BasicDBObject q = new BasicDBObject("_id", p.get("_id"));
	// coll.update(q, new BasicDBObject("$set", new BasicDBObject("seq", i)));
	// // System.out.println(p);
	// }
	// }

	@Override
	protected void tearDown() throws Exception {
		mongoService.close();
	}

}
