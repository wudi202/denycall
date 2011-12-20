package me.lifetrip.denycall;

import java.lang.reflect.Method;
import java.util.ArrayList;

import com.android.internal.telephony.ITelephony;

import me.lifetrip.denycall.DenyCall.DenyAreaMetaData;
import me.lifetrip.denycall.DenyCall.FilterListMetaData;
import me.lifetrip.denycall.DenyCall.PhoneToCity;
import android.R.bool;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class DenycallActivity extends Activity {
    /** Called when the activity is first created. */
	public static String Tag = "DenycallActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
        setContentView(R.layout.main);
        
        Intent intent = getIntent();
        
        if (intent.getData() == null)
        {
        	intent.setData(DenyAreaMetaData.CONTENT_URI);
        }
        
        
        
        //各省市及对应的电话号码的数据库应该是在程序安装的时候就创建起来的，这里为了调试，在create的时候创建一下这个数据库
        /*
        ContentValues values = new ContentValues();
        values.put(DenyAreaMetaData.AREAID, 1);
        values.put(DenyAreaMetaData.PROVONCE, "北京");
        values.put(DenyAreaMetaData.CITY, "北京");
        getContentResolver().insert(DenyAreaMetaData.CONTENT_URI, values);
        
        values.put(DenyAreaMetaData.AREAID, 2);
        values.put(DenyAreaMetaData.PROVONCE, "湖北");
        values.put(DenyAreaMetaData.CITY, "武汉");
        getContentResolver().insert(DenyAreaMetaData.CONTENT_URI, values);
        
        values.put(DenyAreaMetaData.AREAID, 3);
        values.put(DenyAreaMetaData.PROVONCE, "湖北");
        values.put(DenyAreaMetaData.CITY, "咸宁");
        getContentResolver().insert(DenyAreaMetaData.CONTENT_URI, values);        
        
        values.clear();
        values.put(PhoneToCity.PHONE, "13810680000");
        values.put(PhoneToCity.CITYID, 1);
        getContentResolver().insert(PhoneToCity.CONTENT_URI, values);  
        */
        //创建省市对应的数据库        
        TextView denycall = (TextView)this.findViewById(R.id.denycall);
        denycall.setClickable(true);
        denycall.setFocusable(true);
        
        //这里应该启动一个屏蔽区域的页面
        denycall.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v)
        	{
        		Intent intent = new Intent();
        		intent.setClass(DenycallActivity.this, DenyAreaActivity.class);
        		intent.setData(getIntent().getData());
        		intent.putExtra("ShowDenied", 0); //0表示需要显示所有，1表示只需要显示屏蔽的，2表示显示没有屏蔽的
        		startActivity(intent);
        		return;
        	}
        });
        
        TextView denyArea = (TextView)this.findViewById(R.id.deniedarea);
        denycall.setClickable(true);
        denycall.setFocusable(true);
        denyArea.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
        		Intent intent = new Intent();
        		intent.setClass(DenycallActivity.this, DenyAreaActivity.class);
        		intent.setData(getIntent().getData());
        		intent.putExtra("ShowDenied", 1);
        		startActivity(intent);				
			}
		});
        
        TextView permitArea = (TextView)this.findViewById(R.id.permittedarea);
        permitArea.setClickable(true);
        permitArea.setFocusable(true);
        permitArea.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
        		Intent intent = new Intent();
        		intent.setClass(DenycallActivity.this, DenyAreaActivity.class);
        		intent.setData(getIntent().getData());
        		intent.putExtra("ShowDenied", 2);
        		startActivity(intent);				
			}
		});
        
        TextView blackList = (TextView)this.findViewById(R.id.blacklist);
        blackList.setClickable(true);
        blackList.setFocusable(true);
        blackList.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v(Tag, "we have come here blacklistActivity check");
        		Intent intent = new Intent();
        		intent.setClass(DenycallActivity.this, AllBlackWhiteList.class);
        		intent.setData(FilterListMetaData.CONTENT_URI);
        		intent.putExtra("ShowBlack", 1);
        		startActivity(intent);				
			}
		});
        
        TextView whitelist = (TextView)this.findViewById(R.id.whitelist);
        whitelist.setClickable(true);
        whitelist.setFocusable(true);
        whitelist.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v(Tag, "we have come here whitelistActivity check");
        		Intent intent = new Intent();
        		intent.setClass(DenycallActivity.this, AllBlackWhiteList.class);
        		intent.setData(FilterListMetaData.CONTENT_URI);
        		intent.putExtra("ShowBlack", 0);
        		startActivity(intent);				
			}
		});        
        
        mPhoneCallListener myPhoneListener = new mPhoneCallListener();
        TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        telMgr.listen(myPhoneListener, myPhoneListener.LISTEN_CALL_STATE);
        
        }
        catch (Exception e)
        {
        	Log.v("Error in initial main activity", e.getMessage());
        }
        
    }

    public enum PHONETYPE
    {
    	MOBILEPHONENUM,
    	TELEPHONE_LOCAL,
    	TELEPHONE_REMOTE_1,   //3位区号+7位电话号码
    	TELEPHONE_REMOTE_2,   //4位区号+7位电话
    	TELEPHONE_REMOTE_3,   //3位区号+8位电话
    	TELEPHONE_REMOTE_4,   //4位区号+8位电话
    	PHONE_UNKONWN         //其他未知电话，比如国外电话等
    }
    
    public class mPhoneCallListener extends PhoneStateListener 
    {
        private int lastRingMode = -1;
        int columnIndex = 0;
        boolean bCallTrans = false;
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			try
			{
				switch (state)
				{
				    case TelephonyManager.CALL_STATE_IDLE: //挂机
				    {
				    	if (-1 != lastRingMode)
				    	{
			    			TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
			    			AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			    			if (null != audio)
			    			{
			    			    audio.setRingerMode(lastRingMode);
			    			}				    		
				    		lastRingMode = -1;
				    	}
				    	
				    	if (true == bCallTrans)
				    	{
				    		bCallTrans = false;
				    		CancelCallTrans();
				    	}
				    	break;
				    }
				    case TelephonyManager.CALL_STATE_RINGING: //响铃
				    {
				    	//看电话是否是黑/白名单
				    	ContentResolver CurResolver = getContentResolver();
				    	//是否有前面的+86的问题？
				    	String selection = FilterListMetaData.PHONE_NUMBER + "=" + "\"" + incomingNumber + "\"";
				    	Cursor cFilterPhone = CurResolver.query(FilterListMetaData.CONTENT_URI, null, selection, null, FilterListMetaData.DEFAULT_SORT_ORDER);
				    	 
				    	if (0 != cFilterPhone.getCount()) //白名单或者黑名单的电话
				    	{
				    	    cFilterPhone.moveToFirst();
				    	    int iColumnIndex = cFilterPhone.getColumnIndex(FilterListMetaData.IS_BLACK);
				    	    int iBlack = cFilterPhone.getInt(iColumnIndex);
				    		 
				    	    if (1 == iBlack) //黑名单,暂时先设置为静音
				    		{
				    	    	DenyTheCall(incomingNumber, 1);
				    			break;
				    		}
				    		else //白名单
				    		{
				    		    break;
				    		}
				    	}
				    	else if (isPhoneInContacts(incomingNumber))//看来电是否在电话本里面
				    	{
				    		break;
				    	}
				    	
				    	//这里可以增加一段，判断来电是否在屏蔽/允许的打电话列表中，比如固话可以制定某一个
                        
				    	//看电话是否是在屏蔽区域中
				    	selection = PhoneToCity.PHONE + " = " + "\"" + incomingNumber.subSequence(0, incomingNumber.length()-4) + "\"";
				    	Cursor cGetCityID = CurResolver.query(PhoneToCity.CONTENT_URI, null, selection, null, PhoneToCity.DEFAULT_SORT_ORDER);
				    	
				    	if ((null != cGetCityID) && (0 != cGetCityID.getCount()))
				    	{
				    		cGetCityID.moveToFirst();
				    		columnIndex = cGetCityID.getColumnIndex(PhoneToCity.CITYID);
				    		int iCityID = cGetCityID.getInt(columnIndex);
				    		selection = DenyAreaMetaData.AREAID + "=" + iCityID;
				    		
				    		Cursor cFilter = CurResolver.query(DenyAreaMetaData.CONTENT_URI, null, selection, null, DenyAreaMetaData.DEFAULT_SORT_ORDER);
				    		if ((cFilter != null) && (0 != cFilter.getCount()))
				    		{
				    			cFilter.moveToFirst();
				    			columnIndex = cFilter.getColumnIndex(DenyAreaMetaData.ISDENIED);
				    			int iDenied = cFilter.getInt(columnIndex);
				    			if (1 == iDenied)  //deny
				    			{
				    				DenyTheCall(incomingNumber, 2);
				    			}
				    		}
				    		cFilter.close();
				    	}
				    	else
				    	{
				    		Log.v(Tag, "can not find the city for the coming call");
				    	}				    	
				        break;
				    }
				    case TelephonyManager.CALL_STATE_OFFHOOK: //通话中
				    {
				    	break;
				    }
				    default:
				    	break;
				}
				super.onCallStateChanged(state, incomingNumber);
			}
			catch (Exception e)
			{
				Log.e(Tag, "PhoneCall listener creat error" + e.getMessage());
			}	
		}
    	
		void DenyTheCall(String PhoneNumber, int iDenyType)
		{
			String strTrans = null;
			switch (iDenyType)
			{
			    case 1:   //静音
			    {
	    			TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
	    		    AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	    			if (null != audio)
	    			{
	    			    lastRingMode = audio.getRingerMode();
	    				audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
	    			}	
	    			
			    	break;
			    }
			    case 2:   //直接挂断，返回忙音
			    {
			    	HangUpPhone();
			    }
			    case 3:    //呼叫转移，返回空号
			    {
			    	bCallTrans = true;
			    	strTrans = "**67#13800000000#";
			    }
			    case 4:    //呼叫转移，返回暂时无法接通
			    {
			    	bCallTrans = true;
			    	strTrans = "**67#13642952697#";
			    }
			    case 5:    //呼叫转移，返回停机
			    {
			    	bCallTrans = true;
			    	strTrans = "**67#13701110216#";
			    }
			    case 6:    //呼叫转移，返回关机
			    {
			    	bCallTrans = true;
			    	strTrans = "**67#13810538911#";
			    }
			    default:
			    	break;
			}
			
		    if (true == bCallTrans)
		    {
		    	CallTrans(strTrans);
		    }
			
			return;
		}
        
        void CallTrans(String strTrans)
        {
	    	Intent localIntent = new Intent();
	    	localIntent.setAction("android.intent.action.CALL");
	    	Uri uri = Uri.parse("tel:" + strTrans);
	    	localIntent.setData(uri);
	    	startActivity(localIntent);
	    	return;
        }
        
        void CancelCallTrans()
        {
        	Intent localIntent = new Intent();
        	localIntent.setAction("android.intent.action.CALL");
        	Uri uri = Uri.parse("tel:" + "##67#");
        	localIntent.setData(uri);
        	startActivity(localIntent);        	
        }
        
        boolean HangUpPhone()
        {
        	boolean bhangUpOK = true;
        	try {
	    	    TelephonyManager mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	    	
			    Class<TelephonyManager> c = TelephonyManager.class;
			    Method getITelephonyMethod = null;
				getITelephonyMethod = c.getDeclaredMethod("getITelephony",(Class[]) null);
				getITelephonyMethod.setAccessible(true);
				ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager, (Object[]) null);
				bhangUpOK = iTelephony.endCall();
				Log.v(this.getClass().getName(), "endCall......");
			} catch (Exception e) {
				Log.e(this.getClass().getName(), "endCallError", e);
			}  
			return bhangUpOK;
        }
        
        boolean isPhoneInContacts(String PhoneNum)
        {
           	boolean bInContacts = false;
        	String strCountry = null;
        	String strArea = null;
        	String strFinalNum = null;        	
        	try {
        	if (PHONETYPE.MOBILEPHONENUM == parsePhoneNum(PhoneNum, strCountry, strArea, strFinalNum))
        	{
        		String selection = ContactsContract.CommonDataKinds.Phone.NUMBER+" = "+'"' + strFinalNum +'"'
        		        + " OR " + ContactsContract.CommonDataKinds.Phone.NUMBER+" = "+"\"+86" + strFinalNum +'"';
        		Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,   
    	                null, selection, null, null);
        		if ((null != c) && (0 != c.getCount()))
        		{
        			bInContacts = true;
        		}
        	}
        	
        	}
        	catch (Exception e)
        	{
        		Log.e("DenycallActivity", "isPhoneInContacts " + e.getMessage());
        	}
        	return bInContacts;
        }
        
        private Cursor getContacts()
        {
            // Run query
        	int ColumnIndex = 0;
        	int iContact_ID = 0;
        	Cursor phonesNumber = null;
        	String strNum = null;
        	int ColIndex_num = 0;
        	Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        	if (null == cursor)
        	{
        		return null;
        	}
       	
        	if (cursor.moveToFirst())
        	{
        		ColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        		while (!cursor.isAfterLast())
        		{
        			iContact_ID = cursor.getInt(ColumnIndex);

        	        phonesNumber = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,   
        	                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = "+iContact_ID, null, null);  
        			
        	        if ((null != phonesNumber) && (phonesNumber.getCount() > 0))
        	        {
        	        	phonesNumber.moveToFirst();
        	        	while (!phonesNumber.isAfterLast())
        	        	{
        	        		ColIndex_num = phonesNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        	        		strNum = phonesNumber.getString(ColIndex_num);
        	        		if (null != getMobileFormPhoneNumber(strNum))
        	        		{
        	        			ContactPhone.add(getMobileFormPhoneNumber(strNum));
        	        		}
        	        		phonesNumber.moveToNext();
        	        	}
        	        	phonesNumber.close();        	        	
        	        }
        	        cursor.moveToNext();
        		}
        	}
        	cursor.close();
        	
            return cursor;
        }       
        
        String getMobileFormPhoneNumber(String strPhoneNum)
        {
        	String strMobileNum = null;
        	PHONETYPE enPhoneType = PHONETYPE.PHONE_UNKONWN;
        	String strCountry = null;
        	String strArea= null;
        	String strFinalNum = null;
        	if (null == strPhoneNum)
        	{
        		return null;
        	}
        	
        	enPhoneType = parsePhoneNum(strPhoneNum, strCountry, strArea, strFinalNum);
        	if (PHONETYPE.MOBILEPHONENUM == enPhoneType)
        	{
        		strMobileNum = strFinalNum;
        	}
        	return strMobileNum;
        }
      
        ArrayList<String> ContactPhone = new ArrayList<String>();
        
        String[] AreaID_3Bits = {"010", "020", "021", "022", "023", 
        		          "024", "025", "027", "028", "029"}; 
        PHONETYPE parsePhoneNum(String strOriginNum, String strCountry, String strArea, String strFinalNum)
        {
        	String strRealPhone = strOriginNum;
        	PHONETYPE enPhoneType = PHONETYPE.PHONE_UNKONWN;
        	strCountry = null;
        	strArea = null;
        	int num_len = strOriginNum.length();

            if (13 == num_len)
            {
            	if (strRealPhone.substring(0, 2).equals("+86"))
            	{
            		strRealPhone = strRealPhone.substring(3);
            	}
            }
            
            strFinalNum = strRealPhone;
            if (strRealPhone.length() > 11 || strRealPhone.length() < 7)
            {
            	return enPhoneType;
            }
            
            
            switch (strRealPhone.length())
            {
                case 7:
                case 8:
                {
                	enPhoneType = PHONETYPE.TELEPHONE_LOCAL;
                	break;
                }
                case 10:   //3+7
                {
                	enPhoneType = PHONETYPE.TELEPHONE_REMOTE_1;
                	strArea = strRealPhone.substring(0, 2);
                	strFinalNum = strRealPhone.substring(3);
                	break;
                }
                case 11:  //这个情况比价复杂一些：手机/4位区号+7位电话/3位区号+8位电话
                {
                	//取前面的三位或者四位去数据库里面去查的话会比较复杂。
                	//这样的话，因为三位的区号毕竟比较少，用一个数组直接把这个都给记下来
                	//这样的话，直接遍历这个数组就行了。
                	//如果需要考虑扩展的话，可以考虑让这个数组从数据库或者从文档中直接读出来
                	//这里为了简便就直接写死了
                	if ('1' == strRealPhone.charAt(0))   //手机第一位是1
                	{
                		enPhoneType = PHONETYPE.MOBILEPHONENUM;
                	}
                	else if ('0' == strRealPhone.charAt(0))
                	{
                		//3位区号
                		if (('1' == strRealPhone.charAt(1)) || ('2' == strRealPhone.charAt(1)))
                		{
                			enPhoneType = PHONETYPE.TELEPHONE_REMOTE_3;
                        	strArea = strRealPhone.substring(0, 2);
                        	strFinalNum = strRealPhone.substring(3);                			
                		}
                		else //4位区号
                		{
                        	enPhoneType = PHONETYPE.TELEPHONE_REMOTE_2;
                        	strArea = strRealPhone.substring(0, 3);
                        	strFinalNum = strRealPhone.substring(4);                			
                		}
                	}
                	break;
                }
                case 12:    //4位区号+8位电话
                {
                	enPhoneType = PHONETYPE.TELEPHONE_REMOTE_4;
                	strArea = strRealPhone.substring(0, 3);
                	strFinalNum = strRealPhone.substring(4);
                	break;
                }
                default:
                	break;               	
            }
        	
        	return enPhoneType;
        }
    }
}
