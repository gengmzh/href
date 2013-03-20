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
	private String department;
	private String address;
	private String type;
	private String source;
	private String link;
	private String author;
	private Date pubtime;
	private Date createTime;

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

	public Date getCreateTime() {
		return createTime;
	}

	public Post setCreateTime(Date createTime) {
		this.createTime = createTime;
		return this;
	}

	public String getDepartment() {
		return department;
	}

	public Post setDepartment(String department) {
		this.department = department;
		return this;
	}

	public String getAddress() {
		return address;
	}

	public Post setAddress(String address) {
		this.address = address;
		return this;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer().append(Post.class.getSimpleName()).append("{");
		buf.append("title:").append(getTitle()).append(", ");
		buf.append("content:").append(getContent()).append(", ");
		buf.append("company:").append(getCompany()).append(", ");
		buf.append("department:").append(getDepartment()).append(", ");
		buf.append("address:").append(getAddress()).append(", ");
		buf.append("type:").append(getType()).append(", ");
		buf.append("source:").append(getSource()).append(", ");
		buf.append("link:").append(getLink()).append(", ");
		buf.append("author:").append(getAuthor()).append(", ");
		buf.append("pubtime:").append(getPubtime()).append(", ");
		buf.append("createTime:").append(getCreateTime());
		buf.append("}");
		return buf.toString();
	}

}
