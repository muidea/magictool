package com.muidea.magictool.media_util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

public class MediaUtil {
	private static final String TAG = "MediaUtil";
	private static final int MEDIA_TYPE_INVALID = 0;
	private static final int MEDIA_TYPE_IMAGE = 1;
	private static final int MEDIA_TYPE_VIDEO = 2;
	private static final int MEDIA_TYPE_AUDIO = 3;

	private static final int STATUS_INVALID = 0x000;
	private static final int STATUS_CREATE = 0x001;
	private static final int STATUS_RUN = 0x002;
	private static final int STATUS_STOP = 0x004;
	private static final int STATUS_DESTROY = 0x008;
	
	private static int calculateInSampleSize(BitmapFactory.Options options, Rect rect) {
		
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;


		if (height > rect.height() || width > rect.width()) {


			final int heightRatio = Math.round((float) height / (float) rect.height());
			final int widthRatio = Math.round((float) width / (float) rect.width());

			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}

		return inSampleSize;
	}
	
	public static Bitmap getSnapshotImageFromPicture(String filePath, Rect rect) {
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		BitmapFactory.Options option = new BitmapFactory.Options();
		option.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, option);
		option.inSampleSize = calculateInSampleSize(option, rect);
		option.inJustDecodeBounds = false;
		
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, option);
		if (bitmap != null) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, rect.width(), rect.height(), ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		
		return bitmap;		
	}
	
	public static Bitmap getSnapshotImageFromVideo(String filePath, Rect rect) {
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
				
		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.toString(),Images.Thumbnails.MINI_KIND);
		if (bitmap != null) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, rect.width(), rect.height(), ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		
		return bitmap;
	}
	
	public static String getExternalStorageDir() {
		return Environment.getExternalStorageDirectory().toString();
	}
	
	public static void deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] subFile = dir.listFiles();
			if (subFile != null) {
				for(int ii =0; ii < subFile.length; ++ii) {
					File sub = subFile[ii];
					deleteDirectory(sub);
				}
			}
		}
		
		dir.delete();
	}
	
	
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

    public static void releaseCameraInstance(Camera c) {
        try {
            if (c != null) {
                c.release();
            }
        } catch (Exception e) {

        }
    }

	public static void setCameraDisplayOrientation(Display display,	Camera camera) {
		Camera.CameraInfo info = new Camera.CameraInfo();

		Camera.getCameraInfo(0, info);
		int rotation = display.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}

		camera.setDisplayOrientation(result);
	}

	/** Create a File for saving an image or video */
	protected static File getOutputMediaFile(String filePath, String preFix, int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(filePath);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (!preFix.isEmpty()) {
			if (type == MEDIA_TYPE_IMAGE) {
				mediaFile = new File(mediaStorageDir.getPath() + File.separator
						+ preFix + "_IMG_" + timeStamp + ".jpg");
			} else if (type == MEDIA_TYPE_VIDEO) {
				mediaFile = new File(mediaStorageDir.getPath() + File.separator
						+ preFix + "_VID_" + timeStamp + ".mp4");
			} else if (type == MEDIA_TYPE_AUDIO) {
				mediaFile = new File(mediaStorageDir.getPath() + File.separator
						+ preFix + "_AUI_" + timeStamp + ".amr");
			} else {
				return null;
			}
		} else {
			if (type == MEDIA_TYPE_IMAGE) {
				mediaFile = new File(mediaStorageDir.getPath() + File.separator
						 + "IMG_" + timeStamp + ".jpg");
			} else if (type == MEDIA_TYPE_VIDEO) {
				mediaFile = new File(mediaStorageDir.getPath() + File.separator
						 + "VID_" + timeStamp + ".mp4");
			} else if (type == MEDIA_TYPE_AUDIO) {
				mediaFile = new File(mediaStorageDir.getPath() + File.separator
						 + "AUI_" + timeStamp + ".amr");
			} else {
				return null;
			}
		}

		return mediaFile;
	}

	public static boolean savePicture(byte[] data, File saveFile) {
		try {
			FileOutputStream fos = new FileOutputStream(saveFile);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found: " + e.getMessage());
			return false;
		} catch (IOException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public static int getFileType(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index == -1) {
			return MEDIA_TYPE_INVALID;
		}
		
		String ext = fileName.substring(index+1); 
		if (ext.equals("jpg")) return MEDIA_TYPE_IMAGE;
		if (ext.equals("amr")) return MEDIA_TYPE_AUDIO;
		if (ext.equals("mp4")) return MEDIA_TYPE_VIDEO;
		
		return MEDIA_TYPE_INVALID;
	}
	
	/** A basic Camera preview class */
	static public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
		private SurfaceHolder mHolder;
		private Camera mCamera;

		public CameraPreview(Context context, Camera camera) {
			super(context);
			mCamera = camera;

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			// mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, now tell the camera where to draw
			// the preview.
			try {
				mCamera.setPreviewDisplay(holder);

                initCamera();
                mCamera.startPreview();
			} catch (IOException e) {
				Log.d(TAG, "Error setting camera preview: " + e.getMessage());
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// empty. Take care of releasing the Camera preview in your
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,	int h) {
			// If your preview can change or rotate, take care of those events
			// here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				mCamera.stopPreview();

                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if(success){
                            initCamera();//实现相机的参数初始化
                            mCamera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                        }
                    }
                });

				mCamera.startPreview();
			} catch (Exception e) {
				Log.d(TAG, "Error starting camera preview: " + e.getMessage());
			}
		}

        private void initCamera() {
            try {
                Camera.Parameters  parameters = mCamera.getParameters();
                parameters.setPictureFormat(ImageFormat.JPEG);
                parameters.setJpegQuality(100);
                List<Size> picSize = parameters.getSupportedPictureSizes();
                int fixPos = picSize.size() / 2;
                parameters.setPictureSize(picSize.get(fixPos).width, picSize.get(fixPos).height);

                //parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
                setDispaly(parameters, mCamera);

                mCamera.setParameters(parameters);
                mCamera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上

            } catch (Exception e) {
                Log.d(TAG, "Error init camera: " + e.getMessage());
            }
        }

        //控制图像的正确显示方向
        private void setDispaly(Camera.Parameters parameters,Camera camera)
        {
            if (Integer.parseInt(Build.VERSION.SDK) >= 8){
                setDisplayOrientation(camera,90);
            }
            else{
                parameters.setRotation(90);
            }

        }

        //实现的图像的正确显示
        private void setDisplayOrientation(Camera camera, int i) {
            Method downPolymorphic;
            try{
                downPolymorphic=camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
                if(downPolymorphic!=null) {
                    downPolymorphic.invoke(camera, new Object[]{i});
                }
            }
            catch(Exception e){
                Log.e("Came_e", "图像出错");
            }
        }
	}

	static public interface PhotoRecorderCallBack {
		
		public void takePicture(String fileName);
	}
	
	static public class PhotoRecorder {
		private Camera mCamera;
		private Context mRecorderContext;
		private Display mRecorderDisplay;
		private CameraPictureCallback mPictureCallback = new CameraPictureCallback();
		private CameraPreview mPicturePreview;
		private int mStatus;
		private String mSaveFilePath;

		private String mPrefixName;
		protected PhotoRecorderCallBack mCallBack;

		private FrameLayout mlayOut;

		public PhotoRecorder(Context context, String savePath, PhotoRecorderCallBack callBack) {
			mRecorderContext = context;
			mSaveFilePath = savePath;
			
			mCallBack = callBack;
			
			mStatus = STATUS_INVALID;
		}
		
		public PhotoRecorder() {
			mStatus = STATUS_INVALID;
		}

		public void create(Display display, FrameLayout layout) {
			Log.d(TAG, "create photorecorder, status:" + String.valueOf(mStatus));
			if (mStatus >= STATUS_CREATE) {
				return ;
			}

			mCamera = getCameraInstance();			
			mPicturePreview = new CameraPreview(mRecorderContext, mCamera);
		
			mRecorderDisplay = display;
			setCameraDisplayOrientation(mRecorderDisplay, mCamera);

			mlayOut = layout;

			mlayOut.addView(mPicturePreview);
						
			mStatus = STATUS_CREATE;
		}
		
		public void create(Context context, Display display) {
			Log.d(TAG, "create photorecorder, status:" + String.valueOf(mStatus));
			if (mStatus >= STATUS_CREATE) {
				return ;
			}
			
			mRecorderContext = context;
			mCamera = getCameraInstance();
			mPicturePreview = new CameraPreview(mRecorderContext, mCamera);
			
			mRecorderDisplay = display;
			setCameraDisplayOrientation(mRecorderDisplay, mCamera);
			mStatus = STATUS_CREATE;
		}
				
		public void destroy() {
			Log.d(TAG, "destroy photorecorder, status:" + String.valueOf(mStatus));
			
			if (mStatus == STATUS_INVALID) {
				return;
			}
			
			mStatus = STATUS_DESTROY;

			mlayOut.removeView(mPicturePreview);

			mPicturePreview = null;
			
			mCamera.release();
			
			mStatus = STATUS_INVALID;
		}
		
		public void resume() {
			Log.d(TAG, "resume photorecorder, status:" + String.valueOf(mStatus));

			if (mStatus != STATUS_CREATE && mStatus != STATUS_STOP) {
				return;
			}
				
			mStatus = STATUS_RUN;
			
			try {
				mCamera.reconnect();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            try {
                mCamera.lock();
            }catch (Exception e) {
                e.printStackTrace();
            }
		}

		public void pause() {
			Log.d(TAG, "pause photorecorder, status:" + String.valueOf(mStatus));

			if (mStatus != STATUS_RUN) {
				return;
			}
			
			mStatus = STATUS_STOP;
						
			mCamera.unlock();
		}

		public View getPreView() {
			return mPicturePreview;
		}
		
		public void takePhoto(String prefixName) {
			if (mStatus != STATUS_RUN) {
				return;
			}
			
			Log.i(TAG, "takePicture");

			mPrefixName = prefixName;
			mCamera.takePicture(null, null, mPictureCallback);
		}
		
		private class CameraPictureCallback implements Camera.PictureCallback {
						
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				Log.i(TAG, "save taken picture");
				File pictureFile = getOutputMediaFile(mSaveFilePath, mPrefixName, MEDIA_TYPE_IMAGE);

				if (savePicture(data, pictureFile)) {
					String saveFileName = pictureFile.toString();
					
					mCallBack.takePicture(saveFileName);
				}
                camera.startPreview();
			}
		}
	}
	
	static public class VideoRecorder {
		private Context mRecorderContext;
		private Display mRecorderDisplay;
		private MediaRecorder mMediaRecorder;
		private Camera mCamera;
		private CameraPreview mVideoPreview;
		private String mSaveFilePath;
		private String mSaveFileName;
		private int mStatus;
		
		public VideoRecorder() {
			mStatus = STATUS_INVALID;
		}

		public VideoRecorder(Context context, String savePath) {
			mRecorderContext = context;
			mSaveFilePath = savePath;
			mStatus = STATUS_INVALID;
		}
		
		public boolean create(Display display, FrameLayout layout) {
			Log.d(TAG, "create videorecorder, status:" + String.valueOf(mStatus));
			if (mStatus >= STATUS_CREATE) {
				return true;
			}

			mCamera = getCameraInstance();			
			mVideoPreview = new CameraPreview(mRecorderContext, mCamera);
		
			mRecorderDisplay = display;
			setCameraDisplayOrientation(mRecorderDisplay, mCamera);
						
			layout.addView(mVideoPreview);
						
			mStatus = STATUS_CREATE;
			return true;
		}
		
		public void create(Context context, Display display) {
			Log.d(TAG, "create videorecorder, status:" + String.valueOf(mStatus));
			if (mStatus >= STATUS_CREATE) {
				return ;
			}
			
			mRecorderContext = context;
			mCamera = getCameraInstance();
			mVideoPreview = new CameraPreview(mRecorderContext, mCamera);
			
			mRecorderDisplay = display;
			setCameraDisplayOrientation(mRecorderDisplay, mCamera);
			mStatus = STATUS_CREATE;
		}
		
		public void destroy() {
			Log.d(TAG, "destroy videorecorder, status:" + String.valueOf(mStatus));
			
			if (mStatus == STATUS_INVALID) {
				return;
			}
			
			mStatus = STATUS_DESTROY;

			mVideoPreview = null;

			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			
			mStatus = STATUS_INVALID;			
		}
		
		public void resume() {
			Log.d(TAG, "resume videorecorder, status:" + String.valueOf(mStatus));
			
			try {
				mCamera.reconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
			mCamera.lock();
		}

		public void pause() {
			Log.d(TAG, "pause videorecorder, status:" + String.valueOf(mStatus));

			if (mStatus == STATUS_RUN) {
				stop();
			}
			
			mCamera.unlock();
		}
		
		public void start() {
			Log.d(TAG, "start videorecorder, status:" + String.valueOf(mStatus));

			if (mStatus != STATUS_CREATE && mStatus != STATUS_STOP) {
				return;
			}
				
			try {
				mCamera.reconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mMediaRecorder = new MediaRecorder();
			if (!prepare()) {
				mMediaRecorder = null;
				return ;
			}
									
			mMediaRecorder.start();
			
			mStatus = STATUS_RUN;			
		}
		
		public void stop() {
			Log.d(TAG, "stop videorecorder, status:" + String.valueOf(mStatus));

			if (mStatus != STATUS_RUN) {
				return;
			}
			
			mMediaRecorder.stop();
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
			
			try {
				mCamera.reconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			mStatus = STATUS_STOP;			
		}
		
		public View getPreView() {
			return mVideoPreview;
		}
		
		public String getVideoFile() {
			return mSaveFileName;
		}
				
		private boolean prepare() {
			// Step 1: Unlock and set camera to MediaRecorder
			mCamera.unlock();
			mMediaRecorder.setCamera(mCamera);

			// Step 2: Set sources
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

			// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
			mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

			// Step 4: Set output file
			File videoFile = getOutputMediaFile(mSaveFilePath, "", MEDIA_TYPE_VIDEO);
			mMediaRecorder.setOutputFile(videoFile.toString());
			mSaveFileName = videoFile.toString();

			// Step 5: Set the preview output
			mMediaRecorder.setPreviewDisplay(mVideoPreview.getHolder().getSurface());

			mMediaRecorder.setOrientationHint(90);
			// Step 6: Prepare configured MediaRecorder
			try {
				mMediaRecorder.prepare();
			} catch (IllegalStateException e) {
				Log.d(TAG, "IllegalStateException preparing MediaRecorder: "
						+ e.getMessage());

				mCamera.lock();
				return false;
			} catch (IOException e) {
				Log.d(TAG,
						"IOException preparing MediaRecorder: "
								+ e.getMessage());
				mCamera.lock();

				return false;
			}
			
			return true;
		}
		
	}
	
	static public class AudioRecorder {
		private MediaRecorder mMediaRecorder;
		private String mSaveFilePath;
		private String mSaveFileName;
		private int mStatus;
		
		public AudioRecorder(String savePath) {
			mSaveFilePath = savePath;
			mStatus = STATUS_INVALID;
		}
		
		public boolean create() {
			Log.d(TAG, "create audiorecorder, status:" + String.valueOf(mStatus));
			if (mStatus >= STATUS_CREATE) {
				return true;
			}

			mStatus = STATUS_CREATE;
			return true;
		}
		
		public void destroy() {
			Log.d(TAG, "destroy audiorecorder, status:" + String.valueOf(mStatus));
			
			if (mStatus == STATUS_INVALID) {
				return;
			}
			
			mStatus = STATUS_INVALID;			
		}
		
		public void resume() {
			Log.d(TAG, "resume audiorecorder, status:" + String.valueOf(mStatus));			
		}

		public void pause() {
			Log.d(TAG, "pause audiorecorder, status:" + String.valueOf(mStatus));

			if (mStatus == STATUS_RUN) {
				stop();
			}
		}
		
		public void start() {
			Log.d(TAG, "start audiorecorder, status:" + String.valueOf(mStatus));

			if (mStatus != STATUS_CREATE && mStatus != STATUS_STOP) {
				return;
			}
				
			mMediaRecorder = new MediaRecorder();
			if (!prepare()) {
				mMediaRecorder = null;
				return ;
			}
									
			mMediaRecorder.start();
			
			mStatus = STATUS_RUN;			
		}
		
		public void stop() {
			Log.d(TAG, "stop audiorecorder, status:" + String.valueOf(mStatus));

			if (mStatus != STATUS_RUN) {
				return;
			}
			
			mMediaRecorder.stop();
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
			
			mStatus = STATUS_STOP;			
		}
		
		public String getAudioFile() {
			return mSaveFileName;
		}
				
		private boolean prepare() {
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			// Step 4: Set output file
			File videoFile = getOutputMediaFile(mSaveFilePath, "", MEDIA_TYPE_AUDIO);
			mMediaRecorder.setOutputFile(videoFile.toString());
			mSaveFileName = videoFile.toString();

			mMediaRecorder.setOnErrorListener(new OnErrorListener() {
				public void onError(MediaRecorder mr, int what, int extra) {
					stop();
				}
			});
			
			try {
				mMediaRecorder.prepare();
			} catch (IllegalStateException e) {
				Log.d(TAG, "IllegalStateException preparing MediaRecorder: "
						+ e.getMessage());

				return false;
			} catch (IOException e) {
				Log.d(TAG,
						"IOException preparing MediaRecorder: "
								+ e.getMessage());
				return false;
			}
			
			return true;
		}
	}	
}
