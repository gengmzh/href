/**
 * 
 */
package cn.seddat.zhiyu.client.service;

import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import android.Manifest.permission;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.BaseColumns;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * @author mzhgeng
 * 
 */
public class TrackService {

	private static final String tag = TrackService.class.getSimpleName();

	public static String getDeviceId(Context context) {
		// imei
		int status = context.getPackageManager().checkPermission(permission.READ_PHONE_STATE, context.getPackageName());
		if (status == PackageManager.PERMISSION_GRANTED) {
			TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String id = mgr.getDeviceId();
			if (id != null) {
				return id;
			}
		}
		// mac
		status = context.getPackageManager().checkPermission(permission.ACCESS_WIFI_STATE, context.getPackageName());
		if (status == PackageManager.PERMISSION_GRANTED) {
			WifiManager mgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			return mgr.getConnectionInfo().getMacAddress();
		}
		return null;
	}

	public static String getDeviceModel(Context context) {
		return Build.MODEL;
	}

	public static String getLocation(Context context) {
		int status = context.getPackageManager().checkPermission(permission.ACCESS_COARSE_LOCATION,
				context.getPackageName());
		if (status == PackageManager.PERMISSION_GRANTED) {
			LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			List<String> providers = mgr.getAllProviders();
			if (providers != null && providers.contains(LocationManager.NETWORK_PROVIDER)) {
				Location loc = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (loc != null) {
					return loc.getLatitude() + "*" + loc.getLongitude();
				}
			}
		}
		return null;
	}

	public static String getPhoneNumber(Context context) {
		int status = context.getPackageManager().checkPermission(permission.READ_PHONE_STATE, context.getPackageName());
		if (status == PackageManager.PERMISSION_GRANTED) {
			TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			return mgr.getLine1Number();
		}
		return null;
	}

	public static void cacheTrack(Context context, String action, List<String> values) throws Exception {
		if (action == null || values == null || values.isEmpty()) {
			return;
		}
		StringBuffer value = new StringBuffer();
		value.append(values.get(0));
		for (int i = 1; i < values.size(); i++) {
			value.append(",").append(values.get(i));
		}
		cacheTrack(context, new Track().setAction(action).setValue(value.toString()));
	}

	public static void cacheTrack(Context context, Track track) throws Exception {
		if (track == null || track.getAction() == null || track.getValue() == null) {
			return;
		}
		ContentResolver resolver = context.getContentResolver();
		boolean insert = true;
		Cursor cursor = resolver.query(ContentProvider.CONTENT_TRACK, null, Track.COL_ACTION + "=?",
				new String[] { track.getAction() }, Track.COL_CACHE_TIME + " desc");
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				String value = cursor.getString(cursor.getColumnIndex(Track.COL_VALUE));
				if (track.getAction().length() + track.getValue().length() + value.length() < 512) {
					ContentValues values = new ContentValues();
					values.put(Track.COL_VALUE, value + "," + track.getValue());
					resolver.update(ContentProvider.CONTENT_TRACK, values, BaseColumns._ID + "=?",
							new String[] { cursor.getString(cursor.getColumnIndex(BaseColumns._ID)) });
					insert = false;
				}
			}
			cursor.close();
		}
		if (insert) {
			ContentValues values = new ContentValues();
			values.put(Track.COL_ACTION, track.getAction());
			values.put(Track.COL_VALUE, track.getValue());
			values.put(Track.COL_CACHE_TIME, String.valueOf(new Date().getTime()));
			resolver.insert(ContentProvider.CONTENT_TRACK, values);
		}
	}

	public static void sendTrack(Context context) throws Exception {
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(ContentProvider.CONTENT_TRACK, null, null, null, null);
		if (cursor == null) {
			return;
		}
		boolean hasNext = cursor.moveToFirst();
		int i = 0;
		while (hasNext) {
			String action = cursor.getString(cursor.getColumnIndex(Track.COL_ACTION));
			String value = cursor.getString(cursor.getColumnIndex(Track.COL_VALUE));
			HttpRequest.Parameter args = getTrackHeader(context);
			args.set("act", action).set("val", value);
			HttpRequest http = new HttpRequest();
			byte[] bytes = http.request(Config.getTrackApi(), args);
			JSONObject jo = new JSONObject(new String(bytes));
			if (jo.optInt("code", 1) != 0) {
				Log.w(tag, "send track failed, " + jo.optString("message"));
			} else {
				String id = cursor.getString(cursor.getColumnIndex(BaseColumns._ID));
				resolver.delete(ContentProvider.CONTENT_TRACK, BaseColumns._ID + "=?", new String[] { id });
			}
			hasNext = i++ < 10 && cursor.moveToNext();
		}
		cursor.close();
	}

	public static HttpRequest.Parameter getTrackHeader(Context context) {
		HttpRequest.Parameter args = new HttpRequest.Parameter();
		args.set("did", getDeviceId(context)).set("mdl", getDeviceModel(context)).set("loc", getLocation(context));
		return args;
	}

}
