  package me.lifetrip.denycall;

import me.lifetrip.denycall.DenyCall.FilterListMetaData;
import android.R.integer;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class AllBlackWhiteList extends ListActivity {

	private Uri cur_uri;
	private SimpleCursorAdapter filterAdpater;
	int iBlack = 1;
	final static int MENU_BASE = Menu.FIRST + 1;
	final static String TAG = "AllBlackWhiteList";
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try 
        {
            Intent intent = getIntent();
            cur_uri = intent.getData();
            iBlack = intent.getIntExtra("ShowBlack", 0);
            
            String[] projection = new String[] {FilterListMetaData._ID, FilterListMetaData.PHONE_NUMBER};
            String selection = FilterListMetaData.IS_BLACK + "=" + iBlack;
            String sortOrder = FilterListMetaData.PHONE_NUMBER+" ASC";
            //第一次运行的时候，cFilterList为null
            Cursor cFilterList = getContentResolver().query(cur_uri, projection, selection, null, sortOrder);
            
            if ((null != cFilterList) && (cFilterList.getCount() > 0))
            {
            	startManagingCursor(cFilterList);
                filterAdpater = new SimpleCursorAdapter(this, R.layout.deny_province, cFilterList, new String[]{FilterListMetaData.PHONE_NUMBER}, new int []{R.id.area});
                this.setListAdapter(filterAdpater);
            }
            //cFilterList.close();
            registerForContextMenu(getListView());
            /*
	            ListView ll = (ListView)this.getListView();
	            if (null != ll)
	            {
	            	ll.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
						
						@Override
						public void onCreateContextMenu(ContextMenu menu, View v,
								ContextMenuInfo menuInfo) {
							// TODO Auto-generated method stub
							menu.add(MENU_BASE+1, MENU_BASE+1, MENU_BASE+1, "删除");
						}
					});
	            }*/
        }
        catch (Exception e)
        {
        	Log.e(TAG, "Error in onCreate"+e.getMessage());
        }
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		String strFilterListName;
		if (1 == iBlack)
		{	
			strFilterListName = getResources().getString(R.string.addblackList);
		}
		else
		{
			strFilterListName = getResources().getString(R.string.addWhiteList);
		}
		MenuItem itemFilte = menu.add(MENU_BASE, MENU_BASE, MENU_BASE, strFilterListName);	
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		try {
		switch (item.getItemId())
		{
			case MENU_BASE:
			{
                Intent intent = new Intent(this, BlackListAcitvity.class);
                intent.setData(cur_uri);
                Bundle cTransBundle = new Bundle();
                cTransBundle.putInt("isBlack", iBlack);
                intent.putExtras(cTransBundle);
                startActivity(intent);
				break;
			}
			default:
				break;
		}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString());
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		//点击对应的黑名单，则弹出黑名单的页面
		try {
		Intent intent = getIntent();
		
		Intent curFilterInfo = new Intent(l.getContext(), BlackListAcitvity.class);
		curFilterInfo.setData(cur_uri);
		
		TextView curPhone = (TextView)v.findViewById(R.id.area);
		Bundle cTransBundle = new Bundle();
		cTransBundle.putString("phone_number", curPhone.getText().toString());
		cTransBundle.putInt("isBlack", iBlack);
		curFilterInfo.putExtras(cTransBundle);
		startActivity(curFilterInfo);
		}
		catch (Exception e)
		{
			Log.e(TAG, "somthing error when select a filter phone number");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
		// TODO Auto-generated method stub
	    switch(item.getItemId())
	    {
	        case MENU_BASE+1:  //删除
	        {
	        	View vv = menuInfo.targetView;
	        	TextView curPhone = (TextView)vv.findViewById(R.id.area);
	        	if (null != curPhone)
	        	{
	        		String where = FilterListMetaData.PHONE_NUMBER + "=" + "\"" + curPhone.getText().toString() + "\"";
	        		getContentResolver().delete(cur_uri, where, null);
	        	}
	    	    break;
	        }
	    	default:
	    		break;
	    }
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		MenuItem item = menu.add(MENU_BASE+1, MENU_BASE+1, MENU_BASE+1, "删除");
		menu.setHeaderTitle("Delete!");
		super.onCreateContextMenu(menu, v, menuInfo);				
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		try
		{
		    super.onDestroy();
		}
		catch (Exception e)
		{
			Log.e(TAG, "destroy error");
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}  
	
	
}
