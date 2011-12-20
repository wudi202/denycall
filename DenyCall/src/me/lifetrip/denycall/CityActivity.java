package me.lifetrip.denycall;

import java.sql.DatabaseMetaData;

import me.lifetrip.denycall.DenyCall.DenyAreaMetaData;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CityActivity extends ListActivity {
	private SimpleCursorAdapter adapter;
    private MyAdapter myCursorAdapter;
    private String strProvince;
    Uri cur_uri;
    int iTempStatus = 0;
    int iCurStatus = -1;
    public static final int MENU_ITEM_DENYALL = Menu.FIRST+1;
    public static final int MENU_ITEM_PERMITALL = Menu.FIRST+2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent;
		try
		{				
		    intent  = getIntent();
		    cur_uri = intent.getData();
		    String temp0  = cur_uri.getPathSegments().get(0);
		    strProvince = cur_uri.getPathSegments().get(1);
		    
		    iTempStatus = intent.getIntExtra("ShowDenied", 0);

	     	//setContentView(R.layout.cityactivity);
		
		
		    if (null == strProvince)
		    {
		    	Log.e("Get cities error", "no province is input when create activity");
		    }
		    String selection = DenyAreaMetaData.PROVONCE + "= \"" + strProvince + "\"";
		    
		    if (0 != iTempStatus)
		    {
		    	if (1 == iTempStatus)  //deny
		    	{
		    		iCurStatus = 1;
		    	}
		    	else
		    	{
		    		iCurStatus = 0;
		    	}
		    	selection += " AND " + DenyAreaMetaData.ISDENIED + "=" + iCurStatus;
		    }
		    
		    String[] projection = new String[]{DenyAreaMetaData._ID, DenyAreaMetaData.CITY, DenyAreaMetaData.ISDENIED};

		    Cursor c = getContentResolver().query(cur_uri, projection, selection, null, null);
		    
			startManagingCursor(c);
			String[] cols = new String[]{DenyAreaMetaData.CITY, DenyAreaMetaData.ISDENIED};
			int[] names = new int[]{R.id.city, R.id.city_chbox};
			
			myCursorAdapter = new MyAdapter(this, c);
			this.setListAdapter(myCursorAdapter);
			//adapter = new SimpleCursorAdapter(this, R.layout.cityactivity, c, cols, names);
			//this.setListAdapter(adapter);		    
		}
		catch (Exception e)
		{
			Log.e("setContentView", e.getMessage());
			return;
		}
	}
	
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		CheckBox isdeny= (CheckBox)v.findViewById(R.id.city_chbox);
		isdeny.setChecked(!(isdeny.isChecked()));
		
		//TextView ccity = (TextView)v.findViewById(R.id.city);
		//String city = ccity.getText().toString();
	    //boolean ttt = isdeny.isChecked();
		//UpdateDenyArea(strProvince, city, isdeny.isChecked());
	}

    private void UpdateDenyArea(String strProvince, String strCity, boolean isDeny)
    {
    	try {
		String where = DenyAreaMetaData.PROVONCE + "=" + "\"" + strProvince + "\"" + " AND " + DenyAreaMetaData.CITY + "=" + "\""+strCity + "\"";	
		ContentValues values_denyCity = new ContentValues();
		int iIsDeny = 1;
		if (false == isDeny)
		{
			iIsDeny = 0;
		}
		values_denyCity.put(DenyAreaMetaData.ISDENIED, iIsDeny);
		
		//暂时表里面还没有存这一项
		getContentResolver().update(cur_uri, values_denyCity, where, null);   
    	}
    	catch (Exception e)
    	{
    		Log.e("city", "error in update city status"+e.getMessage());
    	}
    }

    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if (0 == iTempStatus)
		{
		    menu.add(1, MENU_ITEM_DENYALL, MENU_ITEM_DENYALL, "屏蔽所有");
		    menu.add(1, MENU_ITEM_PERMITALL, MENU_ITEM_PERMITALL, "允许所有");
		}
		return super.onCreateOptionsMenu(menu);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		ContentValues values_denyCity = new ContentValues();
		String where = DenyAreaMetaData.PROVONCE + "=" + "\"" + strProvince + "\"";
		switch (item.getItemId())
		{
		    case MENU_ITEM_DENYALL:
		    {
		    	values_denyCity.put(DenyAreaMetaData.ISDENIED, 1);
		    	getContentResolver().update(cur_uri, values_denyCity, where, null);	
			    break;
		    }
		    case MENU_ITEM_PERMITALL:
		    {
		    	values_denyCity.put(DenyAreaMetaData.ISDENIED, 0);
		    	getContentResolver().update(cur_uri, values_denyCity, where, null);			    	
		    	break;
		    }
		    default:
		    	break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class a extends Activity implements android.content.DialogInterface.OnClickListener
	{
		
	}


	public class MyAdapter extends CursorAdapter
	{
		public MyAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			try {
				Log.d("test for bindView", "test for bindView");
				/*****
			CheckBox isdeny = (CheckBox)view.findViewById(R.id.city_chbox);
			
			int columnIndex = cursor.getColumnIndex(DenyAreaMetaData.ISDENIED);
			if (1 == cursor.getInt(columnIndex))
			{
				isdeny.setChecked(true);
			}
			else
			{
			    isdeny.setChecked(false);
			}
			
			TextView city = (TextView)view.findViewById(R.id.city);
			columnIndex = cursor.getColumnIndex(DenyAreaMetaData.CITY);
			city.setText(cursor.getString(columnIndex));
			*//////
			}
			catch (Exception e)
			{
				Log.e("bindView_MyAdapter", e.getMessage());
			    java.util.Map<Thread, StackTraceElement[]> ts = Thread.getAllStackTraces();  
			      StackTraceElement[] ste = ts.get(Thread.currentThread());  
			      for (StackTraceElement s : ste) {  
			      Log.e("My_Trace", s.toString());  
			    }  				
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View vv = null;
			try {
			       LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			       vv = inflater.inflate(R.layout.cityactivity, null);
			       
					CheckBox isdeny = (CheckBox)vv.findViewById(R.id.city_chbox);
					isdeny.setTag(vv);
					
					int columnIndex = cursor.getColumnIndex(DenyAreaMetaData.ISDENIED);
					if (1 == cursor.getInt(columnIndex))
					{
						isdeny.setChecked(true);
					}
					else
					{
					    isdeny.setChecked(false);
					}
					TextView city = (TextView)vv.findViewById(R.id.city);
					columnIndex = cursor.getColumnIndex(DenyAreaMetaData.CITY);
					city.setText(cursor.getString(columnIndex));					
					
					isdeny.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							// TODO Auto-generated method stub
							View v5 = (View)buttonView.getTag();
							TextView city = (TextView)v5.findViewById(R.id.city);
							String p = city.getText().toString();
							UpdateDenyArea(strProvince, p, isChecked);
						}
					});			       
			}
			catch (Exception e)
			{
				Log.e("bindView_MyAdapter", e.getMessage());
			}
			
			return vv;
		}		
	}
	
    /* 定义一个item对象的类，包含了xml中的textview以及checkbox等 */
    private class ViewHolder {
    	    CheckBox chkBox;
            TextView text;
    }

}
