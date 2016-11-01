package com.muidea.magictool.media_util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class ZipUtil {
	static class FileIterator {
		private List<String> mFileList = new ArrayList<String>();

		private void getFiles(String strFile) {
			File file = new File(strFile);
			if (file.isFile()) {
				mFileList.add(strFile);
				return;
			}
			
			String[] subPath = file.list();
			if (subPath == null) {
				return;
			}
			
			if (subPath.length == 0) {
				mFileList.add(strFile + "/");
				return ;
			}
			
			for (int i=0; i < subPath.length; ++i) {
				File curFile = new File(strFile, subPath[i]);
				getFiles(curFile.toString());
			}
		}
		
		public List<String> getFileList(String strFile) {
			getFiles(strFile);
			
			return mFileList;
		}
	}
	
	public static void compress(File srcFile, File dstFile) throws Exception {
		FileIterator iterator = new FileIterator();
		List<String> fileList = iterator.getFileList(srcFile.toString());
		if (!dstFile.getParentFile().exists()) {
			dstFile.getParentFile().mkdirs();
		}
		
		try {
			OutputStream oStream = new FileOutputStream(dstFile);
			ZipOutputStream zos = new ZipOutputStream(oStream);
			
			Iterator<String> iter = fileList.iterator();
			while(iter.hasNext()) {
				String fileName = iter.next();
				String subFile = fileName.substring(srcFile.toString().length() + 1);
				
				File file = new File(fileName);
				ZipEntry entry = new ZipEntry(subFile);
				zos.putNextEntry(entry);
				
				if (file.isFile()) {
					byte[] buffer = new byte[128];
					FileInputStream fis = new FileInputStream(file);
					while (true) {
						int readSize = fis.read(buffer);
						if (readSize <= 0) {
							break;
						}
						
						zos.write(buffer, 0, readSize);
					}
					fis.close();										
				}
				
				zos.closeEntry();
			}
			zos.close();
			oStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			
			throw e;
		}
	}
	
	public static boolean uncompress(File srcFile, File dstPath) {
		boolean result = false;
		
		byte[] buffer = new byte[256];
		try {
			ZipFile zipFile = new ZipFile(srcFile);
			Enumeration<?> e = zipFile.entries();
			while(e.hasMoreElements()) {
				ZipEntry ze2 = (ZipEntry) e.nextElement();
				File dstFile = new File(dstPath, ze2.getName());
				if (ze2.isDirectory()) {
					dstFile.mkdirs();
				} else {
					dstFile.getParentFile().mkdirs();
					FileOutputStream fos = new FileOutputStream(dstFile);
					InputStream is = zipFile.getInputStream(ze2);
					while(true) {
						int readSize = is.read(buffer);
						if (readSize <= 0) {
							break;
						}
						
						fos.write(buffer, 0, readSize);
					}
					
					fos.close();
				}
			}
			
			zipFile.close();
			
			result = true;
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
