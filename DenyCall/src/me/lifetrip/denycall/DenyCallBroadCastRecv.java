package me.lifetrip.denycall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DenyCallBroadCastRecv extends BroadcastReceiver
{
    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (null == intent)
		{
			return;
		}
		
		if (false == DenyCallService.isServiceStarted)
		{
	        //if (BOOT_COMPLETED == intent.getAction())
	        {
	            Intent service = new Intent(context, DenyCallService.class);
	            context.startService(service);	        	
	        }
		}
	}
}
