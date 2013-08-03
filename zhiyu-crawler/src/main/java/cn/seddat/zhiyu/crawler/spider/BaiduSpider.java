/**
 * 
 */
package cn.seddat.zhiyu.crawler.spider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.zhiyu.crawler.Config;
import cn.seddat.zhiyu.crawler.Post;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * @author mzhgeng
 * 
 */
public class BaiduSpider implements Spider {

	private final Log log = LogFactory.getLog(BaiduSpider.class.getSimpleName());
	private static final String USERAGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";

	private final String seed;
	private final String host;
	private final String hrUrl;
	private DateFormat dateFormat;
	private Pattern contentPattern;
	private boolean crawlAllPages = true;

	public BaiduSpider() {
		this.seed = "baidu";
		this.host = Config.getInstance().getSpiderSeedHost(seed);
		this.hrUrl = Config.getInstance().getSpiderSeedUrls(seed)[0];
		dateFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("en"));
		contentPattern = Pattern.compile("<h4>工作职责：</h4><div[\\s\\S]*?</div><h4>职位要求：</h4><div[\\s\\S]*?</div>");
	}

	@Override
	public String getSeed() {
		return seed;
	}

	@Override
	public List<Post> crawl() throws Exception {
		log.info("crawl " + seed + " starts...");
		List<Post> posts = new ArrayList<Post>();
		int page = 1;
		boolean hasNextPage = true;
		while (hasNextPage) {
			log.info("current page " + page);
			String buf = this.crawl(page);
			// parse
			BasicDBObject result = (BasicDBObject) JSON.parse(buf.toString());
			BasicDBObject jobList = (BasicDBObject) result.get("jobList");
			BasicDBList jobs = (BasicDBList) jobList.get("jobMes");
			for (int i = 0; i < jobs.size(); i++) {
				Post post = this.parseJob((BasicDBObject) jobs.get(i));
				posts.add(post);
			}
			// next
			if (!crawlAllPages) {
				break;
			}
			String rows = jobList.getString("totalRows");
			if (rows != null && !rows.isEmpty()) {
				int total = (int) Math.ceil(Double.parseDouble(rows) / 10);
				hasNextPage = page < total;
			} else {
				hasNextPage = false;
			}
			if (hasNextPage) {
				page++;
				Thread.sleep(Config.getInstance().getSpiderPolite());
			}
		}
		log.info("crawl " + seed + " done");
		return posts;
	}

	private String crawl(int curPage) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) new URL(hrUrl).openConnection();
		conn.setRequestProperty("Host", "hr.baidu.com");
		conn.setRequestProperty("Origin", "http://hr.baidu.com");
		conn.setRequestProperty("Referer", "http://hr.baidu.com/static/jobList.html");
		conn.setRequestProperty("User-Agent", USERAGENT);
		conn.setRequestProperty("X-Request-With", "XMLHttpRequest");
		conn.setRequestProperty("Cookie", Config.getInstance().getSpiderCookie(seed));
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.getOutputStream().write(("place=全部&type=全部&currentPage=" + curPage).getBytes());
		conn.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuffer buf = new StringBuffer();
		String line = reader.readLine();
		while (line != null) {
			buf.append(line);
			line = reader.readLine();
		}
		reader.close();
		return buf.toString();
	}

	private Post parseJob(BasicDBObject job) throws Exception {
		Post post = new Post().setTitle(job.getString("jobName"));
		post.setCompany("百度").setDepartment(job.getString("jobDepartMent")).setAddress(job.getString("jobAera"));
		post.setType(job.getString("jobKind")).setSeed(seed).setHost(host).setLink(job.getString("jobUrl"));
		post.setAuthor(seed).setPubtime(dateFormat.parse(job.getString("jobTime"))).setCreateTime(new Date());
		// content
		if (post.getLink() != null && !post.getLink().isEmpty()) {
			HttpURLConnection conn = (HttpURLConnection) new URL(post.getLink()).openConnection();
			conn.setRequestProperty("Host", "hr.baidu.com");
			conn.setRequestProperty("Referer", "http://hr.baidu.com/static/jobList.html");
			conn.setRequestProperty("User-Agent", USERAGENT);
			conn.setRequestProperty("Cookie", Config.getInstance().getSpiderCookie(seed));
			conn.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer buf = new StringBuffer();
			String line = reader.readLine();
			while (line != null) {
				buf.append(line);
				line = reader.readLine();
			}
			reader.close();
			Matcher matcher = contentPattern.matcher(buf.toString());
			if (matcher.find()) {
				post.setContent(buf.substring(matcher.start(), matcher.end()));
			}
		}
		return post;
	}

	public void setCrawlAllPages(boolean crawlAllPages) {
		this.crawlAllPages = crawlAllPages;
	}

}
