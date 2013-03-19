/**
 * 
 */
package cn.seddat.href.client.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * @author mzhgeng
 * 
 */
public class CacheService {

	private static final String tag = CacheService.class.getSimpleName();
	private Context context;

	public CacheService(Context context) {
		super();
		this.context = context;
	}

	public static File getCacheDir(Context context) {
		File cache = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			cache = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
			cache = new File(cache, "cache");
			cache.mkdirs();
		} else {
			cache = context.getCacheDir();
		}
		Log.i(tag, "using cache " + cache.getAbsolutePath());
		return cache;
	}

	public Map<String, String> findUserIcon(List<String> uris) throws Exception {
		File cache = getCacheDir(context);
		Map<String, String> result = new HashMap<String, String>();
		for (String uri : uris) {
			// cache
			int index = uri.lastIndexOf("/");
			String icon = index > 0 ? uri.substring(index + 1) : uri;
			File file = new File(cache, icon);
			if (file.exists()) {
				if (file.isFile()) {
					result.put(uri, file.getAbsolutePath());
					Log.i(tag, "find " + file.getName() + " by cache");
					continue;
				} else {
					this.delete(file);
					Log.w(tag, "delete illegal cache file " + file.getAbsolutePath());
				}
			}
			// server
			HttpRequest http = new HttpRequest();
			String url = Config.getBaseApi() + (uri.startsWith("/") ? uri : "/" + uri);
			byte[] bytes = http.request(url, null);
			FileOutputStream out = new FileOutputStream(file);
			out.write(bytes);
			out.close();
			result.put(uri, file.getAbsolutePath());
			Log.i(tag, "find " + file.getName() + " by server");
		}
		return result;
	}

	private void delete(File file) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				this.delete(f);
			}
		}
		file.delete();
	}

	public void clearUserIcon(Date beforeTime, List<String> reservedIcons) throws Exception {
		long time = beforeTime != null ? beforeTime.getTime() : 0;
		if (reservedIcons != null) {
			for (int i = 0; i < reservedIcons.size(); i++) {
				String file = reservedIcons.get(i);
				int index = file.lastIndexOf("/");
				if (index > 0) {
					reservedIcons.set(i, file.substring(index + 1));
				}
			}
		}
		// cache
		File cache = context.getCacheDir();
		for (File icon : cache.listFiles()) {
			if (icon.isFile() && (time == 0 || icon.lastModified() < time)
					&& (reservedIcons == null || !reservedIcons.contains(icon.getName()))) {
				icon.delete();
			}
		}
		// sdcard
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			cache = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
			cache = new File(cache, "cache");
			if (cache.exists()) {
				for (File icon : cache.listFiles()) {
					if (icon.isFile() && (time == 0 || icon.lastModified() < time)
							&& (reservedIcons == null || !reservedIcons.contains(icon.getName()))) {
						icon.delete();
					}
				}
			}
		}
	}

}
