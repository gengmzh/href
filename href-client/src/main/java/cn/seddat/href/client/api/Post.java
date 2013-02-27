/**
 * 
 */
package cn.seddat.href.client.api;

/**
 * @author mzhgeng
 * 
 */
public class Post extends Entity {

	public Post() {
		super();
	}

	public String getId() {
		return get("id");
	}

	public Post setId(String id) {
		put("id", id);
		return this;
	}

	public String getTitle() {
		return get("ttl");
	}

	public Post setTitle(String title) {
		put("ttl", title);
		return this;
	}

	public String getContent() {
		return get("ctt");
	}

	public Post setContent(String content) {
		put("ctt", content);
		return this;
	}

	public String getSource() {
		return get("sn");
	}

	public Post setSource(String source) {
		put("sn", source);
		return this;
	}

	public String getLink() {
		return get("sl");
	}

	public Post setLink(String link) {
		put("sl", link);
		return this;
	}

	public String getType() {
		return get("tp");
	}

	public Post setType(String type) {
		put("tp", type);
		return this;
	}

	public String getCompany() {
		return get("com");
	}

	public Post setCompany(String company) {
		put("com", company);
		return this;
	}

	public String getUserId() {
		return get("uid");
	}

	public Post setUserId(String userId) {
		put("uid", userId);
		return this;
	}

	public String getUserName() {
		return get("un");
	}

	public Post setUserName(String name) {
		put("un", name);
		return this;
	}

	public String getUserIcon() {
		return get("ui");
	}

	public Post setUserIcon(String icon) {
		put("ui", icon);
		return this;
	}

	public String getUserIconUri() {
		return get("uiu");
	}

	public Post setUserIconUri(String uri) {
		put("uiu", uri);
		return this;
	}

	public long getCreateTime() {
		return getLong("ct", 0);
	}

	public Post setCreateTime(long createTime) {
		put("ct", String.valueOf(createTime));
		return this;
	}

	public String getShowTime() {
		return get("pt");
	}

	public Post setShowTime(String showTime) {
		put("pt", showTime);
		return this;
	}

	public long getPv() {
		return getLong("pv", 0);
	}

	public Post setPv(long pv) {
		put("pv", String.valueOf(pv));
		return this;
	}

	public long getClick() {
		return getLong("clk", 0);
	}

	public Post setClick(long click) {
		put("clk", String.valueOf(click));
		return this;
	}

	public long getMark() {
		return getLong("mrk", 0);
	}

	public Post setMark(long mark) {
		put("mrk", String.valueOf(mark));
		return this;
	}

}
