package com.muidea.magictool;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

public abstract class BindServiceActivity extends Activity {
	protected Handler mHandler = new Handler();
	protected CoreService mCoreService = null;
	protected boolean mCoreServiceBounded = false;
	
	protected String getActivityName() {
		return getClass().getName();
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mCoreServiceBounded = false;
			mCoreService = null;

			if (!ActivityRepository.instance().activityExist(getActivityName())) {
				return ;
			}
			
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					disconnectService();
				}
			});
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			CoreService.LocalBinder binder = (CoreService.LocalBinder)service;
			mCoreService = binder.getService();
			mCoreServiceBounded = true;
			
			if (!ActivityRepository.instance().activityExist(getActivityName())) {
				return ;
			}
			
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					connectService();
				}
			});
		}
	};
	
	protected abstract void connectService();

	protected abstract void disconnectService();

	protected abstract boolean checkServiceStatus();
	
	protected abstract void readyService();

	private void waitServiceReady() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (!checkServiceStatus()) {
					try {
						Thread.sleep(50, 0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (!ActivityRepository.instance().activityExist(getActivityName())) {
					return ;
				}				
				
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						readyService();
					}
				});
			}
		}).start();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActivityRepository.instance().register(getActivityName(), this);

		Intent intent = new Intent(this, CoreService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	
		waitServiceReady();
	}
	
	protected void onDestroy() {
    	unbindService(mServiceConnection);
    	
		ActivityRepository.instance().unregister(getActivityName());

    	super.onDestroy();
    }
    
	protected void onStart() {
    	super.onStart();
    	
    }
    
	protected void onStop() {
    	super.onStop();
    }	
}
