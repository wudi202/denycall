package me.lifetrip.denycall;

import java.util.HashMap;

import me.lifetrip.denycall.DenyCall.DenyAreaMetaData;
import android.R.bool;
import android.R.integer;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class DenyAreaActivity extends ListActivity {
	private static final String TAG = "DenyAreaActivity";
    private SimpleCursorAdapter adapter;
    
    // Menu item ids
    public static final int MENU_ITEM_DENYALL = Menu.FIRST;
    public static final int MENU_ITEM_PERMITALL = Menu.FIRST + 1;
    public static final int MENU_ITEM_REVOKE = Menu.FIRST + 2;
    public static final int MENU_ITEM_ADDDENY = Menu.FIRST + 3;
    public static final int MENU_ITEM_ADDPERMIT = Menu.FIRST + 4;
    
    int iCurStatus = -1;
    int iTempStatus = 0;

    private static final String[] PROJECTION_PROVINCE = new String[] {
    	DenyAreaMetaData._ID,
    	DenyAreaMetaData.PROVONCE,
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Cursor c = null;
		String selection = null;
		
		try{
		String strData = getIntent().getDataString();
		Log.d("test", strData);
		//获取数据并显示出来

		Intent intent = getIntent();
		iTempStatus = intent.getIntExtra("ShowDenied", 0);

		if (0 != iTempStatus)
		{
			if (1 == iTempStatus)
			{
				iCurStatus = 1;
			}
			else if (2 == iTempStatus)
			{
				iCurStatus = 0;
			}
		    selection = DenyAreaMetaData.ISDENIED + " = " + iCurStatus;
		}
		c = getContentResolver().query(DenyAreaMetaData.CONTENT_URI, PROJECTION_PROVINCE, selection, null, null);
			
		startManagingCursor(c);
		String[] cols = new String[]{DenyAreaMetaData.PROVONCE};
		int[] names = new int[]{R.id.area};
		adapter = new SimpleCursorAdapter(this, R.layout.deny_province, c, cols, names);
		this.setListAdapter(adapter);
		}
		catch (Exception e)
		{
			Log.e("DenyAreaAvtivity", "get data and initial error" + e.getMessage());
			return;
		}
	}

    //菜单项
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		int group1 = 1;
		if (0 == iTempStatus)
		{
		    menu.add(group1, MENU_ITEM_DENYALL, 1, "全部屏蔽");
		    menu.add(group1, MENU_ITEM_PERMITALL, 2, "全部取消屏蔽");
		    //menu.add(group1, MENU_ITEM_REVOKE, 3, "回退");
		}
		else if (1 == iTempStatus)
		{
			//menu.add(MENU_ITEM_ADDDENY, MENU_ITEM_ADDDENY, MENU_ITEM_ADDDENY, "增加屏蔽");
		}
		else
		{
			//menu.add(MENU_ITEM_ADDPERMIT, MENU_ITEM_ADDPERMIT, MENU_ITEM_ADDPERMIT, "增加允许");
		}
		return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ContentResolver cc = getContentResolver();
		try {
        switch (item.getItemId())
        {
            case MENU_ITEM_DENYALL:
            {
            	ContentValues values = null;
            	values.put(DenyAreaMetaData.ISDENIED, 1);
            	String where = DenyAreaMetaData.ISDENIED + "=0";
            	cc.update(DenyAreaMetaData.CONTENT_URI, values, where, null);
            	//拒绝所有
        	    break;
            }
            case MENU_ITEM_PERMITALL:
            {
            	//允许所有
            	ContentValues values = null;
            	values.put(DenyAreaMetaData.ISDENIED, 0);
            	String where = DenyAreaMetaData.ISDENIED + "=1";
            	cc.update(DenyAreaMetaData.CONTENT_URI, values, where, null);            	
                break;
            }
            case MENU_ITEM_REVOKE:
            {
            	//回退刚才操作
        	    break;
            }
            case MENU_ITEM_ADDDENY:
            {
            	Intent intent1 = new Intent();
            	intent1.setData(DenyAreaMetaData.CONTENT_URI);
            	intent1.setAction("android.intent.action.PICK");
            	ContentValues values = new ContentValues();
            	values.put(DenyAreaMetaData.ISDENIED, 0);            	
            	break;
            }
            case MENU_ITEM_ADDPERMIT:
            {
            	break;
            }
            default:
            	break;
        }
		}
		catch (Exception e)
		{
			Log.e(TAG, "error in handle menu" + e.getMessage());
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {		
		//super.onListItemClick(l, v, position, id);
		
		Cursor data = (Cursor)getListView().getItemAtPosition(position);
		
		int index = data.getColumnIndex(DenyAreaMetaData.PROVONCE);
		String province = data.getString(index);
			
		//Uri uri= ContentUris.withAppendedId(getIntent().getData(), id);
		Uri uri = Uri.withAppendedPath(DenyAreaMetaData.CONTENT_URI, province);
		
		String action = getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return a note selected by
            // the user.  The have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(uri));
        }
		else
		{
			String temp = "The province is " + province;
			Log.v(TAG, temp);
			//启动一个新的界面来显示对应的城市界面
			Intent intent = new Intent(DenyAreaActivity.this, CityActivity.class);
			intent.setData(uri);
			if (0 != iTempStatus)
			{
				intent.putExtra("ShowDenied", iTempStatus);
			}
			startActivity(intent);
		}
		return;
	}

}
