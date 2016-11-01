package com.muidea.magictool;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

public class ActivityRepository {
	private Map<String,Activity> mActivityIntentMap = new HashMap<String, Activity>();
	
	private static ActivityRepository sInstance = null;
	
	public static ActivityRepository instance() {
		if (sInstance == null) {
			sInstance = new ActivityRepository();
		}
		
		return sInstance;
	}
	
	public void register(String activityName, Activity activity) {
		mActivityIntentMap.put(activityName, activity);
	}
	
	public void unregister(String activityName) {
		mActivityIntentMap.remove(activityName);
	}
	
	public Activity findActivity(String activityName) {
		if (mActivityIntentMap.containsKey(activityName)) {
			return mActivityIntentMap.get(activityName);
		}
		
		return null;
	}
	
	public boolean activityExist(String activityName) {
		return mActivityIntentMap.containsKey(activityName);
	}
}
