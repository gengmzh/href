/**
 * 
 */
package cn.seddat.href.client.view;

import android.content.Context;
import android.widget.ListAdapter;

/**
 * @author mzhgeng
 * 
 */
public interface OnRefreshListener {

	public ListAdapter onRefresh(Context context, Object firstItem) throws Exception;

	public ListAdapter onLoadMore(Context context, Object lastItem) throws Exception;

}
