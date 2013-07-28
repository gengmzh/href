/**
 * 
 */
package cn.seddat.zhiyu.client.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * @author mzhgeng
 * 
 */
public class ContentProvider extends android.content.ContentProvider {

	private static final String tag = ContentProvider.class.getSimpleName();

	public static final String AUTHORITY = "cn.seddat.zhiyu.client.provider";
	public static final Uri CONTENT_USER = Uri.parse("content://" + AUTHORITY + "/user");
	public static final Uri CONTENT_POST = Uri.parse("content://" + AUTHORITY + "/post");
	public static final Uri CONTENT_TRACK = Uri.parse("content://" + AUTHORITY + "/track");
	public static final String TABLE_POST = "post";
	public static final String TABLE_USER = "user";
	public static final String TABLE_TRACK = "track";

	private UriMatcher matcher;
	private DatabaseSupport databaseSupport;

	@Override
	public boolean onCreate() {
		// matcher
		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, "user", 1);
		matcher.addURI(AUTHORITY, "user" + "/#", 2);
		matcher.addURI(AUTHORITY, "post", 3);
		matcher.addURI(AUTHORITY, "post" + "/#", 4);
		matcher.addURI(AUTHORITY, "track", 5);
		matcher.addURI(AUTHORITY, "track" + "/#", 6);
		// database
		Context context = getContext();
		String packageName = context.getPackageName();
		int packageVersion = 1;
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
			packageVersion = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			Log.i(tag, "get app version failed", e);
		}
		databaseSupport = new DatabaseSupport(context, packageName, packageVersion);
		return true;
	}

	class DatabaseSupport extends SQLiteOpenHelper {

		public DatabaseSupport(Context context, String name, int version) {
			super(context, name, null, version);
		}

		@Override
		public void onCreate(SQLiteDatabase sqlite) {
			String sql = "create table if not exists " + TABLE_USER + "( " + BaseColumns._ID + " integer primary key, "
					+ User.COL_ID + " text, " + User.COL_NAME + " text, " + User.COL_ICON_URI + " text, "
					+ User.COL_ICON + " text, " + User.COL_CACHE_TIME + " text " + "); ";
			sqlite.execSQL(sql);
			sql = "create table if not exists " + TABLE_POST + "( " + BaseColumns._ID + " integer primary key, "
					+ Post.COL_ID + " text, " + Post.COL_USER_ID + " text, " + Post.COL_TITLE + " text, "
					+ Post.COL_CONTENT + " text, " + Post.COL_SOURCE + " text, " + Post.COL_LINK + " text, "
					+ Post.COL_TYPE + " text, " + Post.COL_COMPANY + " text, " + Post.COL_ADDRESS + " text, "
					+ Post.COL_CREATE_TIME + " text, " + Post.COL_MARK + " text, " + Post.COL_LIKE + " text, "
					+ Post.COL_CACHE_TIME + " text " + "); ";
			sqlite.execSQL(sql);
			sql = "create table if not exists " + TABLE_TRACK + "( " + BaseColumns._ID + " integer primary key, "
					+ Track.COL_ACTION + " text, " + Track.COL_VALUE + " text, " + Track.COL_CACHE_TIME + " text "
					+ "); ";
			sqlite.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqlite, int arg1, int arg2) {
			sqlite.execSQL("drop table if exists " + TABLE_USER);
			sqlite.execSQL("drop table if exists " + TABLE_POST);
			sqlite.execSQL("drop table if exists " + TABLE_TRACK);
			onCreate(sqlite);
		}

	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (uri == null || values == null || values.size() == 0) {
			return null;
		}
		SQLiteDatabase sqlite = databaseSupport.getWritableDatabase();
		long id = -1;
		switch (matcher.match(uri)) {
		case 1:
		case 2:
			id = sqlite.insert(TABLE_USER, null, values);
			break;
		case 3:
		case 4:
			id = sqlite.insert(TABLE_POST, null, values);
			break;
		case 5:
		case 6:
			id = sqlite.insert(TABLE_TRACK, null, values);
			break;
		}
		return Uri.withAppendedPath(uri, String.valueOf(id));
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String table = null;
		switch (matcher.match(uri)) {
		case 1:
		case 2:
			table = TABLE_USER;
			break;
		case 3:
		case 4:
			table = TABLE_POST;
			break;
		case 5:
		case 6:
			table = TABLE_TRACK;
			break;
		}
		SQLiteDatabase sqlite = databaseSupport.getWritableDatabase();
		int res = sqlite.delete(table, selection, selectionArgs);
		return res;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		String table = null;
		switch (matcher.match(uri)) {
		case 1:
		case 2:
			table = TABLE_USER;
			break;
		case 3:
		case 4:
			table = TABLE_POST;
			break;
		case 5:
		case 6:
			table = TABLE_TRACK;
			break;
		}
		SQLiteDatabase sqlite = databaseSupport.getWritableDatabase();
		int res = sqlite.update(table, values, selection, selectionArgs);
		return res;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String table = null;
		switch (matcher.match(uri)) {
		case 1:
		case 2:
			table = TABLE_USER;
			break;
		case 3:
		case 4:
			table = TABLE_POST;
			break;
		case 5:
		case 6:
			table = TABLE_TRACK;
			break;
		}
		SQLiteDatabase sqlite = databaseSupport.getReadableDatabase();
		return sqlite.query(table, projection, selection, selectionArgs, null, null, sortOrder);
	}

}
