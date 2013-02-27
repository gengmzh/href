/**
 * 
 */
package cn.seddat.href.client.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mzhgeng
 * 
 */
public class HttpRequest {

	private String userAgent = "HrefClient/0.1.0 Android";

	public HttpRequest() {

	}

	public byte[] request(String url, Parameter args) throws Exception {
		if (url == null) {
			return null;
		}
		if (args != null) {
			url += "?" + args.toString();
		}
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestProperty("User-Agent", userAgent);
		conn.setRequestProperty("Content-Type", "text/html; charset=utf-8");
		conn.connect();
		// read
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		InputStream ins = conn.getInputStream();
		byte[] b = new byte[1024];
		int len = 0;
		while ((len = ins.read(b)) > -1) {
			ous.write(b, 0, len);
		}
		ins.close();
		conn.disconnect();
		b = ous.toByteArray();
		ous.close();
		return b;
	}

	public static class Parameter {

		private Map<String, String> args;

		public Parameter() {
			args = new HashMap<String, String>();
		}

		public String get(String name) {
			return args.get(name);
		}

		public Parameter set(String name, String value) {
			args.put(name, value);
			return this;
		}

		public Parameter remove(String name) {
			args.remove(name);
			return this;
		}

		public Parameter clear() {
			args.clear();
			return this;
		}

		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			for (String name : args.keySet()) {
				if (buf.length() > 0) {
					buf.append("&");
				}
				buf.append(name).append("=").append(args.get(name));
			}
			return buf.toString();
		}

	}

}
