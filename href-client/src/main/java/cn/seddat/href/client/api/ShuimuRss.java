/**
 * 
 */
package cn.seddat.href.client.api;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author mzhgeng
 * 
 */
public class ShuimuRss {

	private String userAgent;

	public ShuimuRss() {
		userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
	}

	public List<Post> fetchRss(String url) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setFeature("http://xml.org/sax/features/namespaces", true);
		SAXParser parser = factory.newSAXParser();
		PostHandler handler = new PostHandler();
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestProperty("User-Agent", userAgent);
		conn.connect();
		InputSource inputSource = new InputSource(conn.getInputStream());
		inputSource.setEncoding("gb2312");
		parser.parse(inputSource, handler);
		conn.disconnect();
		return handler.getPosts();
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
