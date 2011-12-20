package com.me.lifetrip;

import android.net.Uri;
import android.provider.BaseColumns;

public class DenyCall {
    //provider��Ȩ��
	public static final String AUTHORITY = "com.me.lifetrip.DenyCall";
    
    //���ݿ���
    public static final String DATABASE_NAME = "denycall.db";
    public static final int DATABASE_VERSION = 1;
    
    //����
    //phone areaid
    public static final String AREA_PHONE_TABLE_NAME = "phone_city";
    
    //areaID province city isdenied
    public static final String AREA_TABEL_NAME = "area";
    
    //phone isblack
    public static final String FILTERLIST_TABLE_NAME = "filterList";

    //�绰ǰ׺����
    public static final String FILTERPREFIX_TABLE_NAME = "filter_prefix";

    private DenyCall() {}
    
    public static final class DenyAreaMetaData implements BaseColumns
    {
    	private DenyAreaMetaData() {}
    	    	
    	//���ʱ��URI
    	public static final Uri CONTENT_URI = Uri.parse("content://com.me.lifetrip.DenyCall/area");
    	
    	public static final String DEFAULT_SORT_ORDER = "province ASC";
    	
    	//������̳���BaseColumns�Ѿ�Ĭ�ϵ�������_ID�ֶ�
    	public static final String AREAID = "areaid";
    	public static final String PROVONCE = "province";
    	public static final String CITY = "city";
    	public static final String ISDENIED = "isdenied";
    }
 
    public static final class PhoneToCity implements BaseColumns
    {
    	private PhoneToCity() {}
    	    	
    	//���ʱ��URI
    	public static final Uri CONTENT_URI = Uri.parse("content://com.me.lifetrip.DenyCall/phone_city");
    	
    	public static final String DEFAULT_SORT_ORDER = "phone ASC";
    	
    	//������̳���BaseColumns�Ѿ�Ĭ�ϵ�������_ID�ֶ�
    	public static final String phone = "phone";
    	public static final String cityid = "city_id";
    }
    
    public static final class FilterListMetaData implements BaseColumns
    {
    	private FilterListMetaData() {}
    	
    	//����
    	public static final String TABLE_NAME = "filterList";
    	
    	//���ʱ��URI
    	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/filterList");
   	
    	//MIMA
    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lifetrip.filterList";
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.lifetrip.filterList";
    	
    	//
    	public static final String DEFAULT_SORT_ORDER = "modified DESC";
    	
    	//������̳���BaseColumns�Ѿ�Ĭ�ϵ�������_ID�ֶ�
    	public static final String PHONE_NUMBER = "phone_numner"; 
    	//���Ϊtrue��ʾ�Ǻ������������ʾΪ������������ͨѶ¼�ĵ绰Ĭ��Ϊ������
    	public static final String IS_BLACK = "isblack";
    }
    
    public static final class FilterPrefixMetaData implements BaseColumns
    {
    	private FilterPrefixMetaData() {}
    	
    	//����
    	public static final String TABLE_NAME = "filter_prefix";
    	
    	//���ʱ��URI
    	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/filterPrefix");
   	
    	//MIMA
    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lifetrip.filterPrefix";
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.lifetrip.filterPrefix";
    	
    	//
    	public static final String DEFAULT_SORT_ORDER = "phone_prefix ASC";
    	
    	//������̳���BaseColumns�Ѿ�Ĭ�ϵ�������_ID�ֶ�
    	public static final String PHONE_city = "cityid";
    	public static final String PHONE_PREFIX = "phone_prefix";
    	//��Ӧ�ĵ绰����Ϣ
    	public static final String PREFIX_LEN = "prefixlen"; 
    	//���Ϊtrue��ʾ�Ǻ������������ʾΪ������������ͨѶ¼�ĵ绰Ĭ��Ϊ������
    	public static final String IS_BLACK = "isdeny";
    }    
}
