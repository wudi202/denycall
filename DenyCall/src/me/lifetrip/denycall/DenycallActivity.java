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
        
        
        
        //��ʡ�м���Ӧ�ĵ绰��������ݿ�Ӧ�����ڳ���װ��ʱ��ʹ��������ģ�����Ϊ�˵��ԣ���create��ʱ�򴴽�һ��������ݿ�
        /*
        ContentValues values = new ContentValues();
        values.put(DenyAreaMetaData.AREAID, 1);
        values.put(DenyAreaMetaData.PROVONCE, "����");
        values.put(DenyAreaMetaData.CITY, "����");
        getContentResolver().insert(DenyAreaMetaData.CONTENT_URI, values);
        
        values.put(DenyAreaMetaData.AREAID, 2);
        values.put(DenyAreaMetaData.PROVONCE, "����");
        values.put(DenyAreaMetaData.CITY, "�人");
        getContentResolver().insert(DenyAreaMetaData.CONTENT_URI, values);
        
        values.put(DenyAreaMetaData.AREAID, 3);
        values.put(DenyAreaMetaData.PROVONCE, "����");
        values.put(DenyAreaMetaData.CITY, "����");
        getContentResolver().insert(DenyAreaMetaData.CONTENT_URI, values);        
        
        values.clear();
        values.put(PhoneToCity.PHONE, "13810680000");
        values.put(PhoneToCity.CITYID, 1);
        getContentResolver().insert(PhoneToCity.CONTENT_URI, values);  
        */
        //����ʡ�ж�Ӧ�����ݿ�        
        TextView denycall = (TextView)this.findViewById(R.id.denycall);
        denycall.setClickable(true);
        denycall.setFocusable(true);
        
        //����Ӧ������һ�����������ҳ��
        denycall.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v)
        	{
        		Intent intent = new Intent();
        		intent.setClass(DenycallActivity.this, DenyAreaActivity.class);
        		intent.setData(getIntent().getData());
        		intent.putExtra("ShowDenied", 0); //0��ʾ��Ҫ��ʾ���У�1��ʾֻ��Ҫ��ʾ���εģ�2��ʾ��ʾû�����ε�
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
    	TELEPHONE_REMOTE_1,   //3λ����+7λ�绰����
    	TELEPHONE_REMOTE_2,   //4λ����+7λ�绰
    	TELEPHONE_REMOTE_3,   //3λ����+8λ�绰
    	TELEPHONE_REMOTE_4,   //4λ����+8λ�绰
    	PHONE_UNKONWN         //����δ֪�绰���������绰��
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
				    case TelephonyManager.CALL_STATE_IDLE: //�һ�
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
				    case TelephonyManager.CALL_STATE_RINGING: //����
				    {
				    	//���绰�Ƿ��Ǻ�/������
				    	ContentResolver CurResolver = getContentResolver();
				    	//�Ƿ���ǰ���+86�����⣿
				    	String selection = FilterListMetaData.PHONE_NUMBER + "=" + "\"" + incomingNumber + "\"";
				    	Cursor cFilterPhone = CurResolver.query(FilterListMetaData.CONTENT_URI, null, selection, null, FilterListMetaData.DEFAULT_SORT_ORDER);
				    	 
				    	if (0 != cFilterPhone.getCount()) //���������ߺ������ĵ绰
				    	{
				    	    cFilterPhone.moveToFirst();
				    	    int iColumnIndex = cFilterPhone.getColumnIndex(FilterListMetaData.IS_BLACK);
				    	    int iBlack = cFilterPhone.getInt(iColumnIndex);
				    		 
				    	    if (1 == iBlack) //������,��ʱ������Ϊ����
				    		{
				    	    	DenyTheCall(incomingNumber, 1);
				    			break;
				    		}
				    		else //������
				    		{
				    		    break;
				    		}
				    	}
				    	else if (isPhoneInContacts(incomingNumber))//�������Ƿ��ڵ绰������
				    	{
				    		break;
				    	}
				    	
				    	//�����������һ�Σ��ж������Ƿ�������/����Ĵ�绰�б��У�����̻������ƶ�ĳһ��
                        
				    	//���绰�Ƿ���������������
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
				    case TelephonyManager.CALL_STATE_OFFHOOK: //ͨ����
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
			    case 1:   //����
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
			    case 2:   //ֱ�ӹҶϣ�����æ��
			    {
			    	HangUpPhone();
			    }
			    case 3:    //����ת�ƣ����ؿպ�
			    {
			    	bCallTrans = true;
			    	strTrans = "**67#13800000000#";
			    }
			    case 4:    //����ת�ƣ�������ʱ�޷���ͨ
			    {
			    	bCallTrans = true;
			    	strTrans = "**67#13642952697#";
			    }
			    case 5:    //����ת�ƣ�����ͣ��
			    {
			    	bCallTrans = true;
			    	strTrans = "**67#13701110216#";
			    }
			    case 6:    //����ת�ƣ����عػ�
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
                case 11:  //�������ȼ۸���һЩ���ֻ�/4λ����+7λ�绰/3λ����+8λ�绰
                {
                	//ȡǰ�����λ������λȥ���ݿ�����ȥ��Ļ���Ƚϸ��ӡ�
                	//�����Ļ�����Ϊ��λ�����űϾ��Ƚ��٣���һ������ֱ�Ӱ��������������
                	//�����Ļ���ֱ�ӱ��������������ˡ�
                	//�����Ҫ������չ�Ļ������Կ����������������ݿ���ߴ��ĵ���ֱ�Ӷ�����
                	//����Ϊ�˼���ֱ��д����
                	if ('1' == strRealPhone.charAt(0))   //�ֻ���һλ��1
                	{
                		enPhoneType = PHONETYPE.MOBILEPHONENUM;
                	}
                	else if ('0' == strRealPhone.charAt(0))
                	{
                		//3λ����
                		if (('1' == strRealPhone.charAt(1)) || ('2' == strRealPhone.charAt(1)))
                		{
                			enPhoneType = PHONETYPE.TELEPHONE_REMOTE_3;
                        	strArea = strRealPhone.substring(0, 2);
                        	strFinalNum = strRealPhone.substring(3);                			
                		}
                		else //4λ����
                		{
                        	enPhoneType = PHONETYPE.TELEPHONE_REMOTE_2;
                        	strArea = strRealPhone.substring(0, 3);
                        	strFinalNum = strRealPhone.substring(4);                			
                		}
                	}
                	break;
                }
                case 12:    //4λ����+8λ�绰
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
