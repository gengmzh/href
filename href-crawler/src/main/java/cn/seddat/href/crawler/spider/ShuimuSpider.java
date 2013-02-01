/**
 * 
 */
package cn.seddat.href.crawler.spider;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import cn.seddat.href.crawler.Config;
import cn.seddat.href.crawler.Post;
import cn.seddat.href.crawler.Source;

/**
 * @author mzhgeng
 * 
 */
public class ShuimuSpider implements Spider {

	private final Log log = LogFactory.getLog(ShuimuSpider.class.getSimpleName());
	private static final String USERAGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";

	private String[] rssUrls;
	private SAXParser saxParser;

	public ShuimuSpider() throws Exception {
		rssUrls = Config.getInstance().getShuimuRss();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setFeature("http://xml.org/sax/features/namespaces", true);
		saxParser = factory.newSAXParser();
	}

	@Override
	public List<Post> crawl() throws Exception {
		log.info("crawl " + Source.SHUIMU + " starts...");
		List<Post> posts = new LinkedList<Post>();
		PostHandler handler = new PostHandler();
		for (String url : rssUrls) {
			log.info("crawl " + url);
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestProperty("User-Agent", USERAGENT);
			conn.connect();
			InputSource inputSource = new InputSource(conn.getInputStream());
			saxParser.parse(inputSource, handler);
			conn.disconnect();
			posts.addAll(handler.getPosts());
		}
		log.info("crawl " + Source.SHUIMU + " done");
		return posts;
	}

	class PostHandler extends DefaultHandler {

		private final DateFormat dateFormat;
		private List<Post> posts;
		private String type;
		private String curTag;
		private Post curPost;

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
				curPost.setSource(Source.SHUIMU.getName());
			}
			curTag = localName;
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (curTag == null || curTag.isEmpty()) {
				return;
			}
			String value = new String(ch, start, length);
			if (curPost == null) {
				if ("title".equalsIgnoreCase(curTag)) {
					type = value;
				}
			} else if ("title".equalsIgnoreCase(curTag)) {
				curPost.setTitle(value);
			} else if ("link".equalsIgnoreCase(curTag)) {
				curPost.setLink(value);
			} else if ("description".equalsIgnoreCase(curTag)) {
				curPost.setContent(value);
			} else if ("author".equalsIgnoreCase(curTag)) {
				curPost.setAuthor(value);
			} else if ("pubDate".equalsIgnoreCase(curTag)) {
				try {
					curPost.setPubtime(dateFormat.parse(value));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("item".equalsIgnoreCase(localName)) {
				if (type != null) {
					curPost.setType(type);
				}
				posts.add(curPost);
				curPost = null;
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