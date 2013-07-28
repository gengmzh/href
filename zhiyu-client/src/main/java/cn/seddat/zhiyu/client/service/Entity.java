/**
 * 
 */
package cn.seddat.zhiyu.client.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;

/**
 * @author mzhgeng
 * 
 */
public class Entity implements Map<String, String> {

	private Map<String, String> value;

	public Entity() {
		value = new HashMap<String, String>();
	}

	@Override
	public boolean containsKey(Object arg0) {
		return value.containsKey(arg0);
	}

	@Override
	public boolean containsValue(Object arg0) {
		return value.containsValue(arg0);
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return value.entrySet();
	}

	@Override
	public Set<String> keySet() {
		return value.keySet();
	}

	@Override
	public Collection<String> values() {
		return value.values();
	}

	@Override
	public String get(Object arg0) {
		return value.get(arg0);
	}

	public long getLong(String key, long defaultValue) {
		String value = get(key);
		return value != null ? Long.parseLong(value) : defaultValue;
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> arg0) {
		value.putAll(arg0);
	}

	@Override
	public String put(String arg0, String arg1) {
		return value.put(arg0, arg1);
	}

	@Override
	public boolean isEmpty() {
		return value.isEmpty();
	}

	@Override
	public int size() {
		return value.size();
	}

	@Override
	public String remove(Object arg0) {
		return value.remove(arg0);
	}

	@Override
	public void clear() {
		value.clear();
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !Entity.class.isInstance(o)) {
			return false;
		}
		Entity other = (Entity) o;
		return value.equals(other.value);
	}

	public ContentValues toValues() {
		ContentValues values = new ContentValues();
		for (String key : this.keySet()) {
			values.put(key, this.get(key));
		}
		return values;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" + value.toString() + "}";
	}

}
