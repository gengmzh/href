/**
 * 
 */
package cn.seddat.href.crawler;

import java.io.Serializable;
import java.util.Date;

/**
 * @author mzhgeng
 * 
 */
public class Post implements Serializable {

	private static final long serialVersionUID = 7634948950103889783L;
	private String title;
	private String content;
	private String company;
	private String type;
	private String source;
	private String link;
	private String author;
	private Date pubtime;

	public Post() {
		super();
	}

	public String getTitle() {
		return title;
	}

	public Post setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getType() {
		return type;
	}

	public Post setType(String type) {
		this.type = type;
		return this;
	}

	public String getLink() {
		return link;
	}

	public Post setLink(String link) {
		this.link = link;
		return this;
	}

	public String getContent() {
		return content;
	}

	public Post setContent(String content) {
		this.content = content;
		return this;
	}

	public String getAuthor() {
		return author;
	}

	public Post setAuthor(String author) {
		this.author = author;
		return this;
	}

	public Date getPubtime() {
		return pubtime;
	}

	public Post setPubtime(Date pubtime) {
		this.pubtime = pubtime;
		return this;
	}

	public String getCompany() {
		return company;
	}

	public Post setCompany(String company) {
		this.company = company;
		return this;
	}

	public String getSource() {
		return source;
	}

	public Post setSource(String source) {
		this.source = source;
		return this;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer().append(Post.class.getSimpleName()).append("{");
		buf.append("title:").append(getTitle()).append(", ");
		buf.append("content:").append(getContent()).append(", ");
		buf.append("company:").append(getCompany()).append(", ");
		buf.append("type:").append(getType()).append(", ");
		buf.append("source:").append(getSource()).append(", ");
		buf.append("link:").append(getLink()).append(", ");
		buf.append("author:").append(getAuthor()).append(", ");
		buf.append("pubtime:").append(getPubtime());
		buf.append("}");
		return buf.toString();
	}

}