/**
 * 
 */
package cn.seddat.zhiyu.crawler.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.zhiyu.crawler.Config;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * @author mzhgeng
 * 
 */
public class MongoService {

	private static MongoService instance;

	public static MongoService getInstance() throws Exception {
		if (instance == null) {
			synchronized (Config.class) {
				if (instance == null) {
					instance = new MongoService();
				}
			}
		}
		return instance;
	}

	private static final Log log = LogFactory.getLog(MongoService.class.getSimpleName());
	private Mongo mongo;
	private DB db;

	private MongoService() throws Exception {
		String addr = Config.getInstance().getMongoUri();
		log.info("mongo address " + addr);
		MongoClientURI uri = new MongoClientURI(addr);
		mongo = new MongoClient(uri);
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

	public DBCollection getUserCollection() {
		return db.getCollection("user");
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
