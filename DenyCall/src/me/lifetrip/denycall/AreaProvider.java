package me.lifetrip.denycall;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream.GetField;
import java.io.OutputStream;
import java.sql.SQLData;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.me.lifetrip.DenyCall.FilterPrefixMetaData;

import me.lifetrip.denycall.DenyCall.DenyAreaMetaData;
import me.lifetrip.denycall.DenyCall.FilterListMetaData;
import me.lifetrip.denycall.DenyCall.PhoneToCity;

import android.R.integer;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AreaProvider extends ContentProvider {
	private static String TAG = "AreaProvider";
    private static HashMap<String, String> sAreaProjectionMap;
    private static HashMap<String, String> sFilterProjectMap;
    private static HashMap<String, String> sPhone_CityProjectMap;
    private static HashMap<String, String> sFilter_PrefixProjectMap;
    public static int MIN_DB_LEN = 4096;
    public static int rawDB[] = {R.raw.db1, R.raw.db2};
    
    static int areaID = 1;
    static
    {
    	sAreaProjectionMap = new HashMap<String, String>();
    	sAreaProjectionMap.put(DenyAreaMetaData._ID, DenyAreaMetaData._ID);
    	sAreaProjectionMap.put(DenyAreaMetaData.AREAID, DenyAreaMetaData.AREAID);
    	sAreaProjectionMap.put(DenyAreaMetaData.PROVONCE, DenyAreaMetaData.PROVONCE);
    	sAreaProjectionMap.put(DenyAreaMetaData.CITY, DenyAreaMetaData.CITY);
    	sAreaProjectionMap.put(DenyAreaMetaData.ISDENIED, DenyAreaMetaData.ISDENIED);
    	
    	sFilterProjectMap = new HashMap<String, String>();
    	sFilterProjectMap.put(FilterListMetaData._ID, FilterListMetaData._ID);
    	sFilterProjectMap.put(FilterListMetaData.PHONE_NUMBER, FilterListMetaData.PHONE_NUMBER);
    	sFilterProjectMap.put(FilterListMetaData.PHONE_INFO, FilterListMetaData.PHONE_INFO);
    	sFilterProjectMap.put(FilterListMetaData.IS_BLACK, FilterListMetaData.IS_BLACK);
    	
    	sPhone_CityProjectMap = new HashMap<String, String>();
    	sPhone_CityProjectMap.put(PhoneToCity._ID, PhoneToCity._ID);
    	sPhone_CityProjectMap.put(PhoneToCity.PHONE, PhoneToCity.PHONE);
    	sPhone_CityProjectMap.put(PhoneToCity.CITYID, PhoneToCity.CITYID);    	
    	
    	sFilter_PrefixProjectMap = new HashMap<String, String>();
    	sFilter_PrefixProjectMap.put(FilterPrefixMetaData._ID, FilterPrefixMetaData._ID);
    	sFilter_PrefixProjectMap.put(FilterPrefixMetaData.PHONE_PREFIX, FilterPrefixMetaData.PHONE_PREFIX);
    	sFilter_PrefixProjectMap.put(FilterPrefixMetaData.PREFIX_LEN, FilterPrefixMetaData.PREFIX_LEN);
    	sFilter_PrefixProjectMap.put(FilterPrefixMetaData.IS_BLACK, FilterPrefixMetaData.IS_BLACK);
    }
    
    private static final UriMatcher sUriMatcher;
    private static final int INCOMING_AREA_URI_INDICATOR = 1;
    private static final int INCOMING_PROVINCE_URI_INDICATOR = 2;
    
    private static final int BLACKLIST_URI_INDICATOR = 3;
    private static final int BLACKLIST_VERBOSE_URI_INDICATOR = 4; //对应具体的电话号码
    
    private static final int PHONE_CITY_URI_URI_INDICATOR = 5;
    private static final int PHONE_CITY_VERBOSE_URI_INDICATOR = 6;
    
    private static final int FILTER_PREFIX_URI_URI_INDICATOR = 7;
    private static final int FILTER_PREFIX_VERBOSE_URI_INDICATOR = 8;   

    private static final String AREA_TABLE_NAME = "area";
    private static final String BLACK_LIST_TABLE_NAME = "filterList";
    private static final String PHONE_CITY_TABLE_NAME = "phone_city";
    private static final String FILTER_PREFIX_TABLE_NAME = FilterPrefixMetaData.TABLE_NAME;

    static
    {
    	sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    	sUriMatcher.addURI(DenyCall.AUTHORITY, "area", INCOMING_AREA_URI_INDICATOR);
    	sUriMatcher.addURI(DenyCall.AUTHORITY, "area/*", INCOMING_PROVINCE_URI_INDICATOR);
    	sUriMatcher.addURI(DenyCall.AUTHORITY, "filterList", BLACKLIST_URI_INDICATOR);
    	sUriMatcher.addURI(DenyCall.AUTHORITY, "filterList/#", BLACKLIST_VERBOSE_URI_INDICATOR);
    	sUriMatcher.addURI(DenyCall.AUTHORITY, "phone_city", PHONE_CITY_URI_URI_INDICATOR);
    	sUriMatcher.addURI(DenyCall.AUTHORITY, "phone_city/*", PHONE_CITY_VERBOSE_URI_INDICATOR);
    	sUriMatcher.addURI(DenyCall.AUTHORITY, "filterPrefix", FILTER_PREFIX_URI_URI_INDICATOR);
    	sUriMatcher.addURI(DenyCall.AUTHORITY, "filterPrefix/*", FILTER_PREFIX_VERBOSE_URI_INDICATOR);
    }
    
    private class DatabaseHelper extends SQLiteOpenHelper
    {
    	DatabaseHelper(Context context)
    	{
    		super(context, DenyCall.DATABASE_NAME, null, DenyCall.DATABASE_VERSION);
    	}
    	int bufferSize = 4096;
    	byte[] buffer = new byte[bufferSize];
    	
		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			
			File dbfile = new File(db.getPath());			
			try {
				//拷贝一下数据库就可以了 
				if (dbfile.length() < MIN_DB_LEN) //数据库文件太小的话说明没有复制过
				{
					if (!dbfile.delete())
					{
						Log.e(TAG, "delete the origin file errir in create databaseHelper");
						return;
					}
					Context context = AreaProvider.this.getContext();
					BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(db.getPath()));
					for (int rawFile : rawDB) {
						InputStream in = context.getResources().openRawResource(rawFile);
						UnzipFiles(in, bout);
						in.close();
					}
					bout.close();                  
				}
				
				//String Program_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
				//String Database_path = Program_PATH + "11";
				/***
		    //id, province, city
			db.execSQL("CREATE TABLE " + DenyAreaMetaData.TABLE_NAME + " ("
					+ DenyAreaMetaData._ID
					+ " INTEGER PRIMARY KEY, "
					+ DenyAreaMetaData.AREAID + " INTEGER, "
					+ DenyAreaMetaData.PROVONCE + " text, "
					+ DenyAreaMetaData.CITY + " text, "
					+ DenyAreaMetaData.ISDENIED + " INTEGER"
					+ ");");

			//black/white list which contains id, phone_number and isblack
			db.execSQL("CREATE TABLE " + FilterListMetaData.TABLE_NAME + " ("
					+ FilterListMetaData._ID
					+ " INTEGER PRIMARY KEY, "
					+ FilterListMetaData.PHONE_NUMBER + " text, "
					+ FilterListMetaData.PHONE_INFO + " text, "
					+ FilterListMetaData.IS_BLACK + " INTEGER"
					+ ");");
			
			//这个表应该实在系统初始话的时候就已经创建成功了的
			db.execSQL("CREATE TABLE " + PhoneToCity.TABLE_NAME + " ("
					+ PhoneToCity._ID
					+ " INTEGER PRIMARY KEY, "
					+ PhoneToCity.PHONE + " text, "
					+ PhoneToCity.CITYID + " INTEGER "
					+ ");");
					***/
			}
			catch (Exception e)
			{
				Log.e(TAG, "error in init database: " + e.getMessage());
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXITS" + DenyAreaMetaData.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXITS" + FilterListMetaData.TABLE_NAME);
			onCreate(db);
		}
		
		private void UnzipFiles(InputStream in, BufferedOutputStream bout)
		{
			ZipInputStream zin = null;
			ZipEntry zipEntry = null;
			int byteRead = 0;			
			zin = new ZipInputStream(in);
			try {
			while ((byteRead = zin.read(buffer, 0, bufferSize)) != -1)
			{
				bout.write(buffer, 0, byteRead);
				//offset_read += byteRead;
				//offset_write += byteRead;
				bout.flush();
			}
			}
			catch (Exception e)
			{
				
			}
		}
    }
    
    private DatabaseHelper mOpenHelper;
    
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case INCOMING_AREA_URI_INDICATOR:
        case INCOMING_PROVINCE_URI_INDICATOR:
            count = db.delete(AREA_TABLE_NAME, selection, selectionArgs);
            break;
	    case BLACKLIST_URI_INDICATOR:
	    case BLACKLIST_VERBOSE_URI_INDICATOR:
	    {
	    	count = db.delete(BLACK_LIST_TABLE_NAME, selection, selectionArgs);
	    	break;
	    }
	    case PHONE_CITY_URI_URI_INDICATOR:
	    {
	    	count = db.delete(PHONE_CITY_TABLE_NAME, selection, selectionArgs);
	    	break;
	    }
	    case FILTER_PREFIX_URI_URI_INDICATOR:
	    case FILTER_PREFIX_VERBOSE_URI_INDICATOR:
	    {
	    	count = db.delete(FILTER_PREFIX_TABLE_NAME, selection, selectionArgs);
	    	break;
	    }
        default:
            throw new IllegalArgumentException("Delete: Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case INCOMING_AREA_URI_INDICATOR:
            return DenyCall.DenyAreaMetaData.CONTENT_TYPE;
        case INCOMING_PROVINCE_URI_INDICATOR:
        	return DenyCall.DenyAreaMetaData.CONTENT_ITEM_TYPE;
        	
        case BLACKLIST_URI_INDICATOR:
        	return FilterListMetaData.CONTENT_TYPE;
        case BLACKLIST_VERBOSE_URI_INDICATOR:
        	return FilterListMetaData.CONTENT_ITEM_TYPE;
        	
        case PHONE_CITY_URI_URI_INDICATOR:
        	return PhoneToCity.CONTENT_TYPE;
        case PHONE_CITY_VERBOSE_URI_INDICATOR:
        	return PhoneToCity.CONTENT_ITEM_TYPE;
        	
        case FILTER_PREFIX_URI_URI_INDICATOR:
            return FilterPrefixMetaData.CONTENT_TYPE;
        case FILTER_PREFIX_VERBOSE_URI_INDICATOR:
        	return FilterPrefixMetaData.CONTENT_ITEM_TYPE;        	

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		try
		{
		switch(sUriMatcher.match(uri))
		{
		    //插入的是城市信息
		    case INCOMING_AREA_URI_INDICATOR:
		    case INCOMING_PROVINCE_URI_INDICATOR:
		    {
		        if (initialValues.containsKey(DenyCall.DenyAreaMetaData.PROVONCE) == false) {
		        	throw new IllegalArgumentException("NO province insert");
		        }
		        if (initialValues.containsKey(DenyCall.DenyAreaMetaData.CITY) == false) {
		        	throw new IllegalArgumentException("NO city insert");
		        }
		        
		        //如果已经有了这个城市的信息，就不用再新增了
		        SQLiteDatabase db_read = mOpenHelper.getReadableDatabase();
		        String strCondition  = DenyAreaMetaData.PROVONCE + "=" + "\"" + initialValues.getAsString(DenyAreaMetaData.PROVONCE) + "\" AND "
		                             + DenyAreaMetaData.CITY + "=" + "\"" + initialValues.getAsString(DenyAreaMetaData.CITY) + "\"";
		        Cursor temp = db_read.query(AREA_TABLE_NAME, null, strCondition, null, null, null, DenyAreaMetaData.DEFAULT_SORT_ORDER);
		        if (0 != temp.getCount())
		        {
		        	Log.d("area", "try to insert a existed city");
		        	return null;
		        }
		        
		        ContentValues values;
		        values = new ContentValues(initialValues);

		        // Make sure that the fields are all set
		        if (values.containsKey(DenyCall.DenyAreaMetaData.AREAID) == false) {
		            values.put(DenyCall.DenyAreaMetaData.AREAID, areaID++);
		        }
		        else
		        {
		        	areaID = values.getAsInteger(DenyCall.DenyAreaMetaData.AREAID);
		        	areaID ++;
		        }
		        
		        if (values.containsKey(DenyCall.DenyAreaMetaData.ISDENIED) == false) {
		            values.put(DenyCall.DenyAreaMetaData.ISDENIED, true);
		        }
		        
		        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		        long rowId = db.insert(AREA_TABLE_NAME, null, values);
		        if (rowId > 0) {
		            Uri noteUri = ContentUris.withAppendedId(DenyCall.DenyAreaMetaData.CONTENT_URI, rowId);
		            getContext().getContentResolver().notifyChange(uri, null);
		            return noteUri;
		        }		    	
		    	break;
		    }
		    //插入的是黑/白名单信息
		    case BLACKLIST_URI_INDICATOR:
		    case BLACKLIST_VERBOSE_URI_INDICATOR:
		    {
		    	if (false == initialValues.containsKey(FilterListMetaData.PHONE_NUMBER))
		    	{
		    		throw new SQLException("insert the black/white filterlist but didn't input the phone number");
		    	}
		    			        
		        ContentValues values;
		        values = new ContentValues(initialValues);	
		        
		    	if (false == initialValues.containsKey(FilterListMetaData.IS_BLACK))
		    	{
		    		values.put(FilterListMetaData.IS_BLACK, false);
		    	}
		    	if (false == initialValues.containsKey(FilterListMetaData.PHONE_INFO))
		    	{
		    		values.put(FilterListMetaData.PHONE_INFO, "备注");
		    	}

		        //如果已经有了这个黑/白名单
		        SQLiteDatabase db_read = mOpenHelper.getReadableDatabase();
		        String strCondition  = FilterListMetaData.PHONE_NUMBER + "=" + "\"" + initialValues.getAsString(FilterListMetaData.PHONE_NUMBER)+"\"";
		        Cursor temp = db_read.query(BLACK_LIST_TABLE_NAME, null, strCondition, null, null, null, FilterListMetaData.DEFAULT_SORT_ORDER);
		        if (0 != temp.getCount())
		        {
		        	int columnIndex = temp.getColumnIndex(FilterListMetaData.IS_BLACK);
		        	boolean isblacklist = (1==temp.getInt(columnIndex))?true:false;
		        	
		        	columnIndex = temp.getColumnIndex(FilterListMetaData.PHONE_INFO);
		        	String info = temp.getString(columnIndex);
		        	
		        	if ((values.getAsBoolean(FilterListMetaData.IS_BLACK) == isblacklist) && (info.equals(values.getAsString(FilterListMetaData.PHONE_INFO))))
		        	{
		        	    return null;
		        	}
		        	else
		        	{
		        		String selection = FilterListMetaData.PHONE_NUMBER + "=" + initialValues.getAsString(FilterListMetaData.PHONE_NUMBER);
		        		update(uri, values, selection, null);
		        		return uri;
		        	}
		        }
		        
		    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		    	long rowId = db.insert(BLACK_LIST_TABLE_NAME, null, initialValues);
		    	
		        if (rowId > 0)
		        {
		        	Uri noteUri = ContentUris.withAppendedId(FilterListMetaData.CONTENT_URI, rowId);
		        	getContext().getContentResolver().notifyChange(noteUri, null);
		        	return noteUri;
		        }
		    	break;
		    }
		    case PHONE_CITY_URI_URI_INDICATOR:
		    case PHONE_CITY_VERBOSE_URI_INDICATOR:
		    {
		    	if (false == initialValues.containsKey(PhoneToCity.PHONE))
		    	{
		    		Log.v(TAG, "insert a phone_city error: no input phone_number");
		    		return null;
		    	}
		    	else if (false == initialValues.containsKey(PhoneToCity.CITYID))
		    	{
		    		Log.v(TAG, "insert a phone_city error: no input phone_number");
		    	}

		    	int iPhonelen = initialValues.getAsString(PhoneToCity.PHONE).length();
		    	if (11 > iPhonelen)
		    	{
		    		Log.v(TAG, "insert a phone_city error: the input phone_number is not a mobile phone number");
		    		return null;		    		
		    	}
		        ContentValues values;
		        values = new ContentValues();
		        String strPhoneFiltebyCity = initialValues.getAsString(PhoneToCity.PHONE);
		        strPhoneFiltebyCity = strPhoneFiltebyCity.substring(0, iPhonelen-4);
		        //strPhoneFiltebyCity = strPhoneFiltebyCity.concat("0000");
		        values.put(PhoneToCity.PHONE, strPhoneFiltebyCity);
		        values.put(PhoneToCity.CITYID, initialValues.getAsInteger(PhoneToCity.CITYID));
		        
		    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		    	long rowId = db.insert(PHONE_CITY_TABLE_NAME, null, values);
		    	if (rowId > 0)
		    	{
		        	Uri noteUri = ContentUris.withAppendedId(PhoneToCity.CONTENT_URI, rowId);
		        	getContext().getContentResolver().notifyChange(noteUri, null);
		        	return noteUri;		    		
		    	}
		    	break;
		    }
		    case FILTER_PREFIX_URI_URI_INDICATOR:
		    case FILTER_PREFIX_VERBOSE_URI_INDICATOR:
		    {
		    	if (false == initialValues.containsKey(FilterPrefixMetaData.PHONE_PREFIX))
		    	{
		    		Log.v(TAG, "insert a filte_prefix error: no input phone_prefix");
		    		return null;
		    	}
		        ContentValues values;
		        values = new ContentValues(initialValues);
		    	if (false == initialValues.containsKey(FilterPrefixMetaData.PREFIX_LEN))
		    	{
		    		values.put(FilterPrefixMetaData.PREFIX_LEN, 
		    				values.getAsString(FilterPrefixMetaData.PHONE_PREFIX).length());
		    	}
		    	if (false == initialValues.containsKey(FilterPrefixMetaData.IS_BLACK))
		    	{
		    		values.put(FilterPrefixMetaData.IS_BLACK, 1);
		    	}
		    	
		    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		    	long rowId = db.insert(PHONE_CITY_TABLE_NAME, null, values);
		    	if (rowId > 0)
		    	{
		        	Uri noteUri = ContentUris.withAppendedId(FilterPrefixMetaData.CONTENT_URI, rowId);
		        	getContext().getContentResolver().notifyChange(noteUri, null);
		        	return noteUri;		    		
		    	}		    	
		    	break;
		    }
		    default:
		    	break;
		}
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error in insert data" + e.getMessage()+": "+uri);
		}
        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor c = null;
		String sql=null;
		try {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 
        // If no sort order is specified use the default
        String orderBy;
        
        switch (sUriMatcher.match(uri)) {
        //获取所有的省名
        case INCOMING_AREA_URI_INDICATOR:
        {
        	qb.setTables(AREA_TABLE_NAME);
            qb.setProjectionMap(sAreaProjectionMap);
            //默认按照省名升序排列
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = DenyAreaMetaData.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }
            sql = qb.buildQuery(projection, selection, selectionArgs, DenyAreaMetaData.PROVONCE, null, orderBy, null);
            break;
        }
        //获取省里面的市名，需要由传入的selection保证
        case INCOMING_PROVINCE_URI_INDICATOR:
        {
        	qb.setTables(AREA_TABLE_NAME);
        	if (null == selection)
        	{
        		Log.e("get city from province", "the province is null");
        		throw new IllegalArgumentException("the province is null and try to get it's cities");
        	}
            //默认按照省名升序排列
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = "city ASC";
            } else {
                orderBy = sortOrder;
            }
        	qb.setProjectionMap(sAreaProjectionMap);
        	sql = qb.buildQuery(projection, selection, selectionArgs, null, null, orderBy, null);
            break;
        }
        case BLACKLIST_URI_INDICATOR:
        {
            //默认按照省名升序排列
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = FilterListMetaData.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }        	
        	qb.setTables(BLACK_LIST_TABLE_NAME);
        	qb.setProjectionMap(sFilterProjectMap);
        	sql = qb.buildQuery(projection, selection, selectionArgs, null, null, orderBy, null);
        	break;
        }
        case PHONE_CITY_URI_URI_INDICATOR:
        {
        	if (TextUtils.isEmpty(sortOrder))
        	{
        		orderBy = PhoneToCity.DEFAULT_SORT_ORDER;
        	}
        	else
        	{
        		orderBy = sortOrder;
        	}
        	qb.setTables(PHONE_CITY_TABLE_NAME);
        	qb.setProjectionMap(sPhone_CityProjectMap);
        	sql = qb.buildQuery(projection, selection, selectionArgs, null, null, orderBy, null);
        	break;
        }
	    case FILTER_PREFIX_URI_URI_INDICATOR:
	    case FILTER_PREFIX_VERBOSE_URI_INDICATOR:
	    {
        	if (TextUtils.isEmpty(sortOrder))
        	{
        		orderBy = FilterPrefixMetaData.DEFAULT_SORT_ORDER;
        	}
        	else
        	{
        		orderBy = sortOrder;
        	}
        	qb.setTables(FILTER_PREFIX_TABLE_NAME);
        	qb.setProjectionMap(sFilter_PrefixProjectMap);
        	sql = qb.buildQuery(projection, selection, selectionArgs, null, null, orderBy, null);
        	break;
	    }
        default:
            throw new IllegalArgumentException("Query: Unknown URI" + uri + "match result: " + sUriMatcher.match(uri));
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        //Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        c = db.rawQuery(sql, null); 
        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error in query data: "+e.getMessage()+" sql: "+ sql);
		}

        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        switch (sUriMatcher.match(uri))
        {
            case INCOMING_AREA_URI_INDICATOR:
            case INCOMING_PROVINCE_URI_INDICATOR:
            {
            	count = db.update(AREA_TABLE_NAME, values, selection, selectionArgs);
            	break;
            }
            case BLACKLIST_URI_INDICATOR:
            {
            	count = db.update(BLACK_LIST_TABLE_NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	break;
            }
            case PHONE_CITY_URI_URI_INDICATOR:
            {
            	count = db.update(PHONE_CITY_TABLE_NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);
            	break;            	
            }
    	    case FILTER_PREFIX_URI_URI_INDICATOR:
    	    case FILTER_PREFIX_VERBOSE_URI_INDICATOR:
    	    {
            	count = db.update(FILTER_PREFIX_TABLE_NAME, values, selection, selectionArgs);
            	getContext().getContentResolver().notifyChange(uri, null);   	
            	break;
    	    }
            default:
            	throw new IllegalArgumentException("update: Unknown URI" + uri + "match result: " + sUriMatcher.match(uri));
        }
          
        return count;
	}

}
