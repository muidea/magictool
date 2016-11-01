package com.muidea.magictool.nethelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpHelper {
	public BasicHttpParams mHttpParams;
	public DefaultHttpClient mHttpClient;

	public HttpHelper() {
	}

	private class SimpleNameValuePair implements NameValuePair {
		private String mName;
		private String mValue;

		public SimpleNameValuePair(String name, String value) {
			mName = name;
			mValue = value;
		}

		@Override
		public String getName() {
			return mName;
		}

		@Override
		public String getValue() {
			return mValue;
		}

	}

	private List<NameValuePair> consturctPostData(Map<String, String> dataMap) {
		List<NameValuePair> result = new ArrayList<NameValuePair>();

		Iterator<Entry<String, String>> iter = dataMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			NameValuePair item = new SimpleNameValuePair(entry.getKey(), entry.getValue());
			result.add(item);
		}

		return result;
	}

	public String doGet(String url, Map<String, String> params) throws Exception {
		String paramStr = "";
		Iterator<Entry<String, String>> iter = params.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			paramStr += paramStr = "&" + key + "=" + val;
		}
		if (!paramStr.equals("")) {
			paramStr = paramStr.replaceFirst("&", "?");
			url += paramStr;
		}
		HttpGet httpRequest = new HttpGet(url);
		String strResult = "doGetError";
		try {
			HttpResponse httpResponse = mHttpClient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String charset = new String("UTF-8");
				org.apache.http.Header[] heads = httpResponse.getHeaders("Charset");
				if (heads.length > 0) {
					charset = heads[0].getValue();
				}

				strResult = EntityUtils.toString(httpResponse.getEntity(), charset);
			} else {
				strResult = "Error Response: " + httpResponse.getStatusLine().toString();
				throw new Exception(strResult);
			}
		} catch (ClientProtocolException e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
			throw new Exception(strResult);
		} catch (IOException e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
			throw new Exception(strResult);
		} catch (Exception e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
			throw new Exception(strResult);
		}

		return strResult;
	}

	public String doPost(String url, Map<String, String> params) throws Exception {
		List<NameValuePair> postData = consturctPostData(params);
		HttpPost httpRequest = new HttpPost(url);
		String strResult = "doPostError";
		try {
			httpRequest.setEntity((HttpEntity) new UrlEncodedFormEntity(postData, HTTP.UTF_8));
			HttpResponse httpResponse = mHttpClient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String charset = new String("UTF-8");
				org.apache.http.Header[] heads = httpResponse.getHeaders("Charset");
				if (heads.length > 0) {
					charset = heads[0].getValue();
				}

				strResult = EntityUtils.toString(httpResponse.getEntity(), charset);
			} else {
				strResult = "Error Response: " + httpResponse.getStatusLine().toString();

				throw new Exception(strResult);
			}
		} catch (ClientProtocolException e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
			throw new Exception(strResult);
		} catch (IOException e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
			throw new Exception(strResult);
		} catch (Exception e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
			throw new Exception(strResult);
		}

		return strResult;
	}

	public String uploadFile(String url, File file) throws Exception {
		HttpPost httpRequest = new HttpPost(url);
		String strResult = "doPostError";
		try {
			MultipartEntity mpEntity = new MultipartEntity();
			ContentBody cbFile = new FileBody(file);
			mpEntity.addPart("userfile", cbFile);
			httpRequest.setEntity(mpEntity);
			HttpResponse httpResponse = mHttpClient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String charset = new String("UTF-8");
				org.apache.http.Header[] heads = httpResponse.getHeaders("Charset");
				if (heads.length > 0) {
					charset = heads[0].getValue();
				}

				strResult = EntityUtils.toString(httpResponse.getEntity(), charset);
			} else {
				strResult = "Error Response: " + httpResponse.getStatusLine().toString();

				throw new Exception(strResult);
			}
		} catch (ClientProtocolException e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
			throw new Exception(strResult);
		} catch (IOException e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
			throw new Exception(strResult);
		} catch (Exception e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
			throw new Exception(strResult);
		}

		return strResult;
	}

	public HttpClient getHttpClient(int timeOut) {
		mHttpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(mHttpParams, timeOut * 1000);
		HttpConnectionParams.setSoTimeout(mHttpParams, timeOut * 1000);
		HttpConnectionParams.setSocketBufferSize(mHttpParams, 8192);
		HttpClientParams.setRedirecting(mHttpParams, true);
		String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
		HttpProtocolParams.setUserAgent(mHttpParams, userAgent);
		mHttpClient = new DefaultHttpClient(mHttpParams);
		return mHttpClient;
	}

	public void release() {
		mHttpClient = null;
		mHttpParams = null;
	}
}