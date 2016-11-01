package com.muidea.magictool;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.muidea.magictool.media_util.ZipUtil;
import com.muidea.magictool.nethelper.HttpHelper;
import com.muidea.magictool.SystemConfig.ServerInfo;

public class CoreService extends IntentService {	
	private final IBinder mBinder = (IBinder) new LocalBinder();

	private boolean mLoopThreadRun = false;

	private List<ServiceTask> mTaskList = new LinkedList<ServiceTask>();

	private HttpHelper mHttpHelper = new HttpHelper();
	
	private Thread mLoopThread = new Thread() {
		public void run() {
			while (mLoopThreadRun) {
				try {
					synchronized (mTaskList) {
						List<ServiceTask> removeList = new ArrayList<ServiceTask>();
						Iterator<ServiceTask> iter = mTaskList.iterator();
						while(iter.hasNext()) {
							ServiceTask task = iter.next();
							if (task.doTask()) {
								removeList.add(task);
							}
						}
						
						// 这里把已经执行完的清除掉
						iter = removeList.iterator();
						while(iter.hasNext()) {
							mTaskList.remove(iter.next());
						}						
					}
										
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	public CoreService() {
		super("CoreService");
	}
		
	public void postTask(ServiceTask task) {
		synchronized (mTaskList) {
			mTaskList.add(task);
		}
	}
	
	public void onCreate() {
		mLoopThreadRun = true;
		mLoopThread.start();
	}
	
	public void onDestroy() {
		try {
			mLoopThreadRun = false;
			mLoopThread.join();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
	}

	public boolean downloadFile(ServerInfo serverInfo, Map<String,String> data, File dataFile) {
		mHttpHelper.getHttpClient(serverInfo.timeOut);
				
		try {
			if (!dataFile.getParentFile().exists()) {
				dataFile.getParentFile().mkdirs();
			}
			
			String result = new String();
			
			switch (serverInfo.actionType) {
			case ServerInfo.POST:
				result = mHttpHelper.doPost(serverInfo.serverUrl, data);				
				break;
			case ServerInfo.GET:
				result = mHttpHelper.doGet(serverInfo.serverUrl, data);
				break;
			default:
				throw new Exception("invalid action type, value:" + String.format("%d", serverInfo.actionType));
			}
			
			FileOutputStream fos = new FileOutputStream(dataFile);
			fos.write(result.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mHttpHelper.release();
		
		return dataFile.exists();
	}
	
	public boolean uploadFile(ServerInfo serverInfo, File dataFile) {
		mHttpHelper.getHttpClient(serverInfo.timeOut);
		
		boolean ret = false;
		try {
			String result = mHttpHelper.uploadFile(serverInfo.serverUrl, dataFile);
			JSONObject retObj = new JSONObject(result);
			if (!retObj.isNull("ErrCode")) {
				ret = retObj.getInt("ErrCode") == 0;
			}
			Log.d("upload file", result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mHttpHelper.release();
		return ret;
	}

	public boolean compressFile(File rawFile, File dataFile) {
		boolean result = false;
		
		try {
			ZipUtil.compress(rawFile, dataFile);
			
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public void invokeActivity(final Intent activityIntent) {
		postTask(new ServiceTask() {
			
			@Override
			public boolean doTask() {
				activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(activityIntent);
				
				return true;
			}
		});
	}

	public class LocalBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}
	
	/*
	 * 返回true表示该Task已经结束
	 * */
	public interface ServiceTask {
		public boolean doTask();
	}	
}
