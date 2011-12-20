package me.lifetrip.denycall;

import android.net.Uri;
import android.provider.BaseColumns;

public final class DenyCall {
    //provider的权限
	public static final String AUTHORITY = "com.me.lifetrip.DenyCall";
    
    //数据库名
    public static final String DATABASE_NAME = "denycall.db";
    public static final int DATABASE_VERSION = 1;
    
    //表名
    //phone areaid
    public static final String AREA_PHONE_TABLE_NAME = "phone_city";
    
    //areaID province city isdenied
    public static final String AREA_TABEL_NAME = "area";
    
    //phone isblack
    public static final String FILTERLIST_TABLE_NAME = "filterList";
    
    //电话前缀过滤
    public static final String FILTERPREFIX_TABLE_NAME = "filter_prefix";
    
    private DenyCall() {}
    
    public static final class DenyAreaMetaData implements BaseColumns
    {
    	private DenyAreaMetaData() {}
    	
    	//表名
    	public static final String TABLE_NAME = "area";
    	
    	//访问表的URI
    	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/area");
    	
    	//MIMA
    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lifetrip.area";
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.lifetrip.area";
    	
    	//
    	public static final String DEFAULT_SORT_ORDER = "province ASC";
    	
    	//各表项，继承自BaseColumns已经默认的声明了_ID字段
    	public static final String AREAID = "areaid";
    	public static final String PROVONCE = "province";
    	public static final String CITY = "city";
    	public static final String ISDENIED = "isdenied";
    }

    public static final class PhoneToCity implements BaseColumns
    {
    	private PhoneToCity() {}
    	    	
    	public static final String TABLE_NAME = "phone_city";
    	
    	//访问表的URI
    	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/phone_city");
    	
    	public static final String DEFAULT_SORT_ORDER = "phone ASC";

    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lifetrip.phone_city";
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.lifetrip.phone_city";
    	
    	//各表项，继承自BaseColumns已经默认的声明了_ID字段
    	public static final String PHONE = "phone";
    	public static final String CITYID = "city_id";
    }
    
    public static final class FilterListMetaData implements BaseColumns
    {
    	private FilterListMetaData() {}
    	
    	//表名
    	public static final String TABLE_NAME = "filterList";
    	
    	//访问表的URI
    	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/filterList");
   	
    	//MIMA
    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lifetrip.filterList";
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.lifetrip.filterList";
    	
    	//
    	public static final String DEFAULT_SORT_ORDER = "phone_numner ASC";
    	
    	//各表项，继承自BaseColumns已经默认的声明了_ID字段
    	public static final String PHONE_NUMBER = "phone_numner";
    	//对应的电话的信息
    	public static final String PHONE_INFO = "phone_info"; 
    	//如果为true表示是黑名单，否则表示为白名单，所有通讯录的电话默认为白名单
    	public static final String IS_BLACK = "isblack";
    }

    public static final class FilterPrefixMetaData implements BaseColumns
    {
    	private FilterPrefixMetaData() {}
    	
    	//表名
    	public static final String TABLE_NAME = "filter_prefix";
    	
    	//访问表的URI
    	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/filterPrefix");
   	
    	//MIMA
    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lifetrip.filterPrefix";
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.lifetrip.filterPrefix";
    	
    	//
    	public static final String DEFAULT_SORT_ORDER = "phone_prefix ASC";
    	
    	//各表项，继承自BaseColumns已经默认的声明了_ID字段
    	public static final String PHONE_city = "cityid";
    	
    	public static final String PHONE_PREFIX = "phone_prefix";
    	//对应的电话的信息
    	public static final String PREFIX_LEN = "prefixlen"; 
    	//如果为true表示是黑名单，否则表示为白名单，所有通讯录的电话默认为白名单
    	public static final String IS_BLACK = "isdeny";
    }
}
