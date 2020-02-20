package iyzan.notifier;

import java.util.HashMap;
import java.util.List;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotiService extends NotificationListenerService {
	static final public String TAG="iyzan.notifier";
	private class Item{
		public long tm;
		public int n;
		public String actName;
		Item(String actName){
			this.actName=actName;
			tm=0;n=1;
		}
	}
	private HashMap<String, Item> amap=new HashMap<String, Item>();
	private Item getItem(String name,int n){
		Item val;
		long lg;
		int m;
		val=amap.get(name);
		if(n>0){
			if(val==null){
				val=new Item(getActivities(name));
			}else{
				lg=val.tm;
				m=val.n;
				if(lg!=0 && System.currentTimeMillis()-lg>500L)m=1;
				else if(m<=1000)m++;
				val.tm=0;
				val.n=m;
			}
			amap.put(name, val);
		}else{
			if(val!=null){
				val.tm=System.currentTimeMillis();
				amap.put(name, val);
			}
		}
		return val;
	}	
	@Override
    public void onNotificationPosted(StatusBarNotification sbn) {
		countNotis(sbn,1);
	}
	@Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
		countNotis(sbn,0);
	}
	
	private void countNotis(StatusBarNotification sbn,int num){
		
		if(!sbn.isClearable())return;

		String pkgName=sbn.getPackageName();
		if(pkgName==null)return;
		Item itm=getItem(pkgName,num);
		if(itm==null)return;
		if(num!=0)num=itm.n;
		String actName=itm.actName;
		if(actName==null)return;
		sendNum(actName,num);
	}
	private void sendNum(String actName,int n){
		try{
	        Intent localIntent = new Intent();
	        localIntent.setAction("com.mobint.notifier.SEND");
	        localIntent.putExtra("PNAME", actName);
	        localIntent.putExtra("TYPE", 11);
	        localIntent.putExtra("COUNT", n);
	        sendBroadcast(localIntent);
		}catch(Exception e){
			Log.e(TAG,e.getMessage());
		}
	}
	private String getActivities(String packageName){
        Intent localIntent = new Intent("android.intent.action.MAIN", null);
        localIntent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> appList = getPackageManager().queryIntentActivities(localIntent, 0);
        if(appList!=null){
        	int n=appList.size();
	        for (int i = 0; i < n; i++) {
	            ResolveInfo resolveInfo = appList.get(i);
	            ActivityInfo ai=resolveInfo.activityInfo;
	            String packageStr = ai.packageName;
	            if (packageName.equals(packageStr)) {
	                return packageName+"/"+ai.name;
	            }
	        }
        }
        return null;
      }	
}
