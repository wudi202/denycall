package me.lifetrip.denycall;

import junit.framework.Assert;
import me.lifetrip.denycall.DenyCall.FilterListMetaData;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class BlackListAcitvity extends Activity {

	private Uri parUri = null;
	private static String TAG = "BlackListAcitvity";
	private String curPhone = null;
	private int iBlackPhone = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
		Log.v("BlackListAcitvity", "we have come here blacklistActivity init");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blacklist);
		
		Intent parIntent = getIntent();
		parUri = parIntent.getData();
		Bundle curBundle = (Bundle)parIntent.getExtras();
		String curPhoneInfo = null;
		if (null != curBundle)
		{
			if (null == parUri)
			{
				Log.e(TAG, "onCreate URI is null when the input phone is not null!");
				return;
			}
		    curPhone = curBundle.getString("phone_number");
		    iBlackPhone = curBundle.getInt("isBlack");		
		    
		    EditText uiEditPhoneNum = (EditText)findViewById(R.id.editDeniedPhone);
		    EditText uiEditPhoneInfo = (EditText)findViewById(R.id.editInfo);
		    
		    uiEditPhoneNum.setText(curPhone);
		    String[] projection = new String[] {FilterListMetaData._ID, FilterListMetaData.PHONE_INFO};
		    String selection = FilterListMetaData.PHONE_NUMBER+"="+"\""+curPhone+"\"";
		    
		    Cursor cc = getContentResolver().query(parUri, projection, selection, null, null);
		    startManagingCursor(cc);
		    cc.moveToFirst();
		    
		    if ((null != cc) && (0 != cc.getCount()))
		    {
		    	int columnIndex = cc.getColumnIndex(FilterListMetaData.PHONE_INFO);
		    	curPhoneInfo = cc.getString(columnIndex);
		    }
		    if (null != curPhoneInfo)
		    {
		    	uiEditPhoneInfo.setText(curPhoneInfo);
		    }
		    else
		    {
		    	String defHintPhoInfo = getResources().getString(R.string.hintPhoneInfo);
		    	uiEditPhoneInfo.setHint(defHintPhoInfo);
		    }
		}
				
		Button button_ok =  (Button)findViewById(R.id.button_phoneOK);
		Button button_del =  (Button)findViewById(R.id.button_phoneDel);
		
		button_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
				// ��ȡ����
				EditText editPhone = (EditText)findViewById(R.id.editDeniedPhone);
				String phoneNumber = editPhone.getText().toString();
				
				EditText editInfo  = (EditText)findViewById(R.id.editInfo);
				String info = editInfo.getText().toString();
				
				//���绰�������Ч��,����ѵ绰����ȫɾ�˵Ļ��Ͱ�����绰�ӹ���������ժ��
				if ((0 == phoneNumber.length()) && (null != curPhone))
				{
					String selection = FilterListMetaData.PHONE_NUMBER + "=" + curPhone;
					getContentResolver().delete(FilterListMetaData.CONTENT_URI, selection, null);
					finish();
					return;
				}
				else if (phoneNumber.length() < 11)
				{
					//�����ʾ����ĵ绰��������Ч�ġ����Ե�����ʾ���ڡ�
					new AlertDialog.Builder(BlackListAcitvity.this)
					.setTitle("error")
					.setMessage(R.string.invalidPhone)
					.setPositiveButton(
							R.string.str_ok, 
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
							})
					.show();
					return;
				}
				
				//�����ݲ������ݿ�
				ContentValues values = new ContentValues();
				values.put(FilterListMetaData.PHONE_NUMBER, phoneNumber);
				values.put(FilterListMetaData.PHONE_INFO, info);
				values.put(FilterListMetaData.IS_BLACK, iBlackPhone);

				if (null != curPhone)
				{
				    String whereUpdate = FilterListMetaData.PHONE_NUMBER + "=" + "\"" + curPhone + "\"";
				    getContentResolver().update(FilterListMetaData.CONTENT_URI, values, whereUpdate, null);
				}
				else
				{
					getContentResolver().insert(FilterListMetaData.CONTENT_URI, values);
				}
				
				//ȷ�����Ժ���Խ���ǰ��activity�ͷţ�Ӧ�ÿ��Իص���һ������ɣ�
				finish();
				return;
			}
		    catch (Exception e)
			{
			    Log.e(TAG, "error in confirm"+e.getMessage());
			}				
			}
		});
		
		button_del.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});	
		}
		catch (Exception e)
		{
			Log.e(TAG, "error in create"+e.getMessage());
		}		
	}
	
	

}
