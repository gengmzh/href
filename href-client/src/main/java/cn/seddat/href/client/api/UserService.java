/**
 * 
 */
package cn.seddat.href.client.api;

/**
 * @author mzhgeng
 * 
 */
public class UserService {

	private String api = "http://42.96.143.229";

	public UserService() {
	}

	public byte[] getUserIcon(String uri) throws Exception {
		if (uri == null || uri.isEmpty()) {
			return null;
		}
		HttpRequest http = new HttpRequest();
		String url = api + (uri.startsWith("/") ? uri : "/" + uri);
		byte[] bytes = http.request(url, null);
		return bytes;
	}

}
