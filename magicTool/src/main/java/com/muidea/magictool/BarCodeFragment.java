package com.muidea.magictool;


import android.app.Activity;
import android.app.Fragment;
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

public class BarCodeFragment extends Fragment implements MediaUtil.PhotoRecorderCallBack {

    protected Button mBackButton = null;
    protected ImageButton mTakePhotoButton = null;
    protected EditText mBarcodeEditText = null;
    protected CallBack mCallBack = null;

    private MediaUtil.PhotoRecorder mPhotoRecorder = null;
    private FrameLayout mCameraDispalyLayout;
    private String mPhotoSavePath;
    private String mPhotoName;

    private static final int PHOTO_STATUS_IDLE = 0;
    private static final int PHOTO_STATUS_TAKING = 1;
    private int mTakeingStatus = PHOTO_STATUS_IDLE;

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

    public BarCodeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_takephoto, container, false);

        mPhotoSavePath = SystemConfig.instance().getRecordPath();
        mCameraDispalyLayout = (FrameLayout) rootView.findViewById(R.id.id_CameraPreView);

        //ConstructPhotoView(activity);

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
        mBarcodeEditText = (EditText)rootView.findViewById(R.id.id_BarcodeEdit);
        mBarcodeEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    //mTakePhotoButton.performClick();
                    ConstructPhotoView();
                    return  true;
                }

                return false;
            }
        });
        mBarcodeEditText.requestFocus();
        return rootView;
    }

    private void ConstructPhotoView() {
        try {
            if (mPhotoRecorder != null) {
                return ;
            }

            Activity activity = getActivity();
            mPhotoRecorder = new MediaUtil.PhotoRecorder(activity.getBaseContext(), mPhotoSavePath, this);
            WindowManager manager = activity.getWindowManager();
            Display display = manager.getDefaultDisplay();
            mPhotoRecorder.create(display, mCameraDispalyLayout);

            mPhotoRecorder.resume();

            mTakePhotoButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    onRecordMediaClick();
                }
            });

        } catch (Exception e) {

        }
    }

    private void DestructPhotoView() {
        try {
            if (mPhotoRecorder != null) {
                mTakePhotoButton.setOnClickListener(null);

                mPhotoRecorder.pause();

                mPhotoRecorder.destroy();

                mPhotoRecorder = null;
            }
        } catch (Exception e) {

        }
    }

    public void onDestroyView() {
        DestructPhotoView();

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
            if (mPhotoRecorder != null) {
                //mPhotoRecorder.resume();
                //mTakeingStatus = PHOTO_STATUS_IDLE;
                DestructPhotoView();
            }
        } catch (Exception e) {

        }
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        try {
            if (mPhotoRecorder != null) {
                //mPhotoRecorder.pause();
                DestructPhotoView();
            }
        } catch (Exception e) {

        }
    }

    public void onRecordMediaClick() {
        if (mPhotoRecorder == null) {
            return ;
        }

        if (mTakeingStatus > PHOTO_STATUS_IDLE) {
            return;
        }

        mTakeingStatus = PHOTO_STATUS_TAKING;
        try {
            String barcode = mBarcodeEditText.getText().toString();
            if (!barcode.trim().isEmpty()) {
                mPhotoRecorder.takePhoto(barcode);
            }
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
        mTakeingStatus = PHOTO_STATUS_IDLE;

        mBarcodeEditText.setText("");

        DestructPhotoView();

        String photoFile = fileName.substring(mPhotoSavePath.length() + 1);
        Toast.makeText(getActivity().getApplicationContext(), photoFile,Toast.LENGTH_SHORT).show();
    }


    static public interface CallBack {
        public void onReturnClick();
    }
}
