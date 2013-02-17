/**
 * 
 */
package cn.seddat.href.client.view;

import android.widget.ListAdapter;

/**
 * @author mzhgeng
 * 
 */
public interface OnRefreshListener {

	public ListAdapter onRefreshing(RefreshableListView listView) throws Exception;

	public ListAdapter onLoading(RefreshableListView listView) throws Exception;

}
