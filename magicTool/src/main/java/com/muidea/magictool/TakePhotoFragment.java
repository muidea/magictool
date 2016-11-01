package com.muidea.magictool;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.muidea.magictool.media_util.MediaUtil;

public class TakePhotoFragment extends Fragment implements MediaUtil.PhotoRecorderCallBack {

	protected Button mBackButton = null;
    protected ImageButton mTakePhotoButton = null;
    protected EditText mBarcodeEditText = null;
	protected CallBack mCallBack = null;

	private MediaUtil.PhotoRecorder mPhotoRecorder;
    private FrameLayout mCameraDispalyLayout;
	private String mPhotoSavePath;
	private String mPhotoName;

    private static final int PHOTO_STATUS_IDLE = 0;
    private static final int PHOTO_STATUS_TAKING = 1;
    private int mTakeningStatus = PHOTO_STATUS_IDLE;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == R.id.id_TakeMediaButton) {
                mPhotoName = (String)msg.obj;
                showRecordView(mPhotoName);
            }
        }
    };

	public TakePhotoFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_takephoto, container, false);

		Activity activity = getActivity();

        mPhotoSavePath = SystemConfig.instance().getRecordPath();
        mCameraDispalyLayout = (FrameLayout) rootView.findViewById(R.id.id_CameraPreView);

        try {
            mPhotoRecorder = new MediaUtil.PhotoRecorder(activity.getBaseContext(), mPhotoSavePath, this);
            WindowManager manager = activity.getWindowManager();
            Display display = manager.getDefaultDisplay();
            mPhotoRecorder.create(display, mCameraDispalyLayout);
        } catch (Exception e) {

        }

        mBackButton = (Button)rootView.findViewById(R.id.home_bt_back);
		mBackButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mCallBack != null) {
					mCallBack.onReturnClick();
				}
			}
		});

        mTakePhotoButton = (ImageButton)rootView.findViewById(R.id.id_TakeMediaButton);
        mTakePhotoButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onRecordMediaClick();
            }
        });

        mBarcodeEditText = (EditText)rootView.findViewById(R.id.id_BarcodeEdit);
        mBarcodeEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mTakePhotoButton.performClick();
                    return  true;
                }

                return false;
            }
        });
        mBarcodeEditText.requestFocus();
		return rootView;
	}

    public void onDestroyView() {
        try {
            mPhotoRecorder.destroy();
        } catch (Exception e) {

        }

        super.onDestroyView();
    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof CallBack) {
			mCallBack = (CallBack)activity;
		}
    }

    public void onDetach() {
        super.onDetach();
    }

    public void onResume() {
        try {
            mPhotoRecorder.resume();
            mTakeningStatus = PHOTO_STATUS_IDLE;
        } catch (Exception e) {

        }
		super.onResume();
    }

	public void onPause() {
        super.onPause();
        try {
            mPhotoRecorder.pause();
        } catch (Exception e) {

        }
	}

    public void onRecordMediaClick() {
        if (mTakeningStatus > PHOTO_STATUS_IDLE) {
            return;
        }

        mTakeningStatus = PHOTO_STATUS_TAKING;
        try {
            String barcode = mBarcodeEditText.getText().toString();
            mPhotoRecorder.takePhoto(barcode);
        }catch (Exception e) {

        }
    }

    @Override
    public void takePicture(String fileName) {
        Message message = new Message();
        message.what = R.id.id_TakeMediaButton;
        message.obj = fileName;
        mHandler.sendMessage(message);
    }

    private void showRecordView(String fileName) {
        mTakeningStatus = PHOTO_STATUS_IDLE;

        mBarcodeEditText.setText("");

        String photoFile = fileName.substring(mPhotoSavePath.length() + 1);
        Toast.makeText(getActivity().getApplicationContext(), photoFile,Toast.LENGTH_SHORT).show();
    }


    static public interface CallBack {
		public void onReturnClick();
	}
}
