/**
 * 
 */
package cn.seddat.zhiyu.crawler.spider;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import cn.seddat.zhiyu.crawler.Config;
import cn.seddat.zhiyu.crawler.Post;

/**
 * @author mzhgeng
 * 
 */
public class BYRSpider implements Spider {

	private final Log log = LogFactory.getLog(BYRSpider.class.getSimpleName());

	private final String seed;
	private final String host;
	private final String[] rssUrls;
	private SAXParser saxParser;

	public BYRSpider() throws Exception {
		this.seed = "byr";
		this.host = Config.getInstance().getSpiderSeedHost(seed);
		this.rssUrls = Config.getInstance().getSpiderSeedUrls(seed);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setFeature("http://xml.org/sax/features/namespaces", true);
		this.saxParser = factory.newSAXParser();
	}

	@Override
	public String getSeed() {
		return seed;
	}

	@Override
	public List<Post> crawl() throws Exception {
		log.info("crawl " + seed + " starts...");
		List<Post> posts = new LinkedList<Post>();
		PostHandler handler = new PostHandler();
		for (int i = 0; i < rssUrls.length; i++) {
			log.info("crawl " + rssUrls[i]);
			HttpURLConnection conn = (HttpURLConnection) new URL(rssUrls[i]).openConnection();
			conn.setRequestProperty("User-Agent", Config.getInstance().getSpiderUserAgent(seed));
			conn.connect();
			InputSource inputSource = new InputSource(conn.getInputStream());
			saxParser.parse(inputSource, handler);
			conn.disconnect();
			posts.addAll(handler.getPosts());
			if (i < rssUrls.length - 1) {
				Thread.sleep(Config.getInstance().getSpiderPolite());
			}
		}
		log.info("crawl " + seed + " done");
		return posts;
	}

	class PostHandler extends DefaultHandler {

		private final DateFormat dateFormat;
		private List<Post> posts;
		private String type;
		private Post curPost;
		private String curTag;
		private StringBuffer buffer;

		public PostHandler() {
			dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", new Locale("en"));
		}

		@Override
		public void startDocument() throws SAXException {
			posts = new ArrayList<Post>();
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("item".equalsIgnoreCase(localName)) {
				curPost = new Post();
				curPost.setSeed(seed).setHost(host).setCreateTime(new Date());
			}
			curTag = localName;
			buffer = new StringBuffer();
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (curTag != null) {
				buffer.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (curPost == null) {
				if ("title".equalsIgnoreCase(localName)) {
					type = buffer.toString();
				}
			} else {
				if ("title".equalsIgnoreCase(localName)) {
					curPost.setTitle(buffer.toString());
				} else if ("link".equalsIgnoreCase(localName)) {
					curPost.setLink(buffer.toString());
				} else if ("description".equalsIgnoreCase(localName)) {
					curPost.setContent(buffer.toString());
				} else if ("author".equalsIgnoreCase(localName)) {
					curPost.setAuthor(buffer.toString());
				} else if ("pubDate".equalsIgnoreCase(localName)) {
					try {
						curPost.setPubtime(dateFormat.parse(buffer.toString()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else if ("item".equalsIgnoreCase(localName)) {
					if (type != null) {
						curPost.setType(type);
					}
					posts.add(curPost);
					curPost = null;
				}
			}
			curTag = null;
		}

		@Override
		public void endDocument() throws SAXException {
		}

		public List<Post> getPosts() {
			return posts;
		}

	}

}
