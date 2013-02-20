/**
 * 
 */
package cn.seddat.href.crawler.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.href.crawler.Config;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

/**
 * @author mzhgeng
 * 
 */
public class MongoService {

	private static final Log log = LogFactory.getLog(MongoService.class.getSimpleName());
	private Mongo mongo;
	private DB db;

	public MongoService() throws Exception {
		String addr = Config.getInstance().getMongoUri();
		log.info("mongo address " + addr);
		MongoURI uri = new MongoURI(addr);
		mongo = new Mongo(uri);
		log.info("open mongo connection");
		db = mongo.getDB(uri.getDatabase() != null ? uri.getDatabase() : "href");
		log.info("init database " + db.getName());
	}

	public Mongo getMongo() {
		return mongo;
	}

	public DB getDatabase() {
		return db;
	}

	public DBCollection getPostCollection() {
		return db.getCollection("post");
	}

	public void close() {
		if (mongo != null) {
			mongo.close();
			log.info("close mongo connection");
		}
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
	}

}
