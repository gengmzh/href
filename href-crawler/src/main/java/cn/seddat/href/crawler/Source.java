/**
 * 
 */
package cn.seddat.href.crawler;

/**
 * @author mzhgeng
 * 
 */
public enum Source {

	SHUIMU("newsmth.net");

	private String name;

	private Source(String name) {
		this.name = name;
	}

	/**
	 * @return the source name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return Source.class.getSimpleName() + "(" + getName() + ")";
	}

}
