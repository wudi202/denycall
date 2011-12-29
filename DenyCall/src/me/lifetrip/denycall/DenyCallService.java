package me.lifetrip.denycall;

import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DenyCallService extends android.app.Service 
{
	static boolean isServiceStarted = false;
	public DenyCallService()
	{}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		try {
		    Thread thr = new Thread(new ServiceWork(), "DenyCallService");
		    isServiceStarted = true;
		}
		catch (Exception e)
		{
			Log.e("DenyCallService", "service error" + e.toString());
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isServiceStarted = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	class ServiceWork implements Runnable
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
	        mPhoneCallListener myPhoneListener = new mPhoneCallListener(DenyCallService.this);
	        TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
	        telMgr.listen(myPhoneListener, myPhoneListener.LISTEN_CALL_STATE);			
		}
		
	}
}