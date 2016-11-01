package com.muidea.magictool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;

public class SystemConfig {
	private static final String SERVER_ADDR = "addr";
    private static final String RECORD_PATH = "record";
	private static final String UPLOAD_FILE_URL = "uploadFileUrl";
	private static final String TIME_OUT = "timeOut";
	
	public static class ServerConfig {
		public String server;
		public String uploadFileUrl;
		public int timeOut;
	}
	
	public static class ServerInfo {
		public static final int POST = 0;
		public static final int GET = 1;
		
		public String serverUrl;
		public int timeOut;
		public int actionType;
	}	
	
	private ServerConfig mServerConfig = new ServerConfig();

    private String mRecordPath;

	private String mSystemPath;

	private int mReferenceCount = 0;
	private static SystemConfig sInstance = null;
	
	public static SystemConfig instance() {
		if (sInstance == null) {
			sInstance = new SystemConfig();
		}
		
		return sInstance;
	}

	public String getServerAddr() {return  mServerConfig.server;}

	public void setServerAddr(String svrAddr) {
		mServerConfig.server = svrAddr;
	}
	
	public String getSystemPath() {
		return mSystemPath;
	}

    public void setRecordPath(String recordPath) { mRecordPath = recordPath; }

    public String getRecordPath() { return mRecordPath; }

	public ServerConfig getSystemConfig() {
		return mServerConfig;
	}

	public void load(File rootPath) {
		if (mReferenceCount++ >0) {
			return;
		}

        mSystemPath = rootPath.toString();
        File recordPath = new File(rootPath, "record");
        mRecordPath = recordPath.toString();

		mServerConfig.server = "http://192.168.0.104:8000";
		mServerConfig.uploadFileUrl = "/upload/";
		mServerConfig.timeOut = 60;
		try {
			File configFile = new File(rootPath, "config/cfg.json");
			InputStream is = new FileInputStream(configFile);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			int readSize = 0;
			byte buffer[]=new byte[1024];
			while(true) {
				readSize = is.read(buffer);
				if (readSize == -1) {
					break;
				}
				
				os.write(buffer, 0, readSize);
			}
			is.close();

			JSONObject serverObject = new JSONObject(os.toString());
			mServerConfig.server = serverObject.getString(SERVER_ADDR);
			mServerConfig.uploadFileUrl = serverObject.getString(UPLOAD_FILE_URL);
			mServerConfig.timeOut = serverObject.getInt(TIME_OUT);

            String tmp = serverObject.getString(RECORD_PATH);
			File recordPathFile = new File(rootPath, tmp);
			mRecordPath = recordPathFile.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public ServerInfo getUploadFileServer() {
		ServerConfig svrConfig = getSystemConfig();
		ServerInfo serverInfo = new ServerInfo();
		serverInfo.actionType = ServerInfo.POST;
		serverInfo.timeOut = svrConfig.timeOut;
		serverInfo.serverUrl = svrConfig.server + svrConfig.uploadFileUrl;
		
		return serverInfo;
	}
}
