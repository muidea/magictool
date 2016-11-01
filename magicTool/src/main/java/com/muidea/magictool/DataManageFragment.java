package com.muidea.magictool;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataManageFragment extends Fragment {
	protected Button mBackButton = null;
	protected Button mUploadButton = null;

	protected CallBack mCallBack = null;

	private ListView mRecordListView;

    private List<String> mRecordFileList = new ArrayList<String>();

    private ArrayAdapter<String> mRecordFileListAdapter = null;

    protected boolean mContinueUpload = false;

	public DataManageFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_datamanage, container, false);

        Activity activity = getActivity();
        mRecordListView = (ListView)rootView.findViewById(R.id.id_recordListView);
        mRecordFileListAdapter = new ArrayAdapter<String>(activity.getBaseContext(), R.layout.listview);
        mRecordListView.setAdapter(mRecordFileListAdapter);
        mRecordFileList.clear();;

		mBackButton = (Button)rootView.findViewById(R.id.return_back_bt);
		mBackButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mCallBack != null) {
					mCallBack.onReturnClick();
				}
			}
		});

		mUploadButton = (Button)rootView.findViewById(R.id.upload_file_bt);
		mUploadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

                if (mRecordFileList.isEmpty()) {
                    return;
                }

				if (mCallBack != null) {
                    final String recordPath = SystemConfig.instance().getRecordPath();
                    mContinueUpload = true;

                    final List<String> uploadList = new ArrayList<String>();
                    uploadList.add(mRecordFileList.get(0));
                    mRecordFileList.remove(0);
                    UploadFileNotify notify = new UploadFileNotify() {
                        @Override
                        public void uploadFile(String fileName, boolean result) {
                            mRecordFileListAdapter.remove(fileName);
                            String msg = String.format("上传%s%s", fileName, result ? "成功":"失败");
                            Toast.makeText(getActivity().getApplicationContext(), msg,Toast.LENGTH_SHORT).show();

                            if (mContinueUpload && !mRecordFileList.isEmpty()) {
                                mUploadButton.performClick();
                            }
                        }
                    };
					mCallBack.onUploadFileClick(recordPath, uploadList, notify);
				}
			}
		});

        for (String file : getData()) {
            mRecordFileListAdapter.add(file);
        }

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof CallBack) {
			mCallBack = (CallBack)activity;
		}		
	}

    public void onDetach() {
        mContinueUpload = false;
        super.onDetach();
    }

	public void onResume() {
		super.onResume();		
	}

	public void onPause() {
		super.onPause();
	}

    public List<String> getData()
    {
        String recordPath = SystemConfig.instance().getRecordPath();
        File[] files = new File(recordPath).listFiles();
        if (files != null) {
            for (File file : files ) {
                mRecordFileList.add(file.getName());
            }
        }
        return mRecordFileList;
    }

    public interface UploadFileNotify {
        public void uploadFile(String fileName, boolean result);
    }

	static public interface CallBack {
		public void onReturnClick();
        public void onUploadFileClick(String filePath, List<String> fileList, UploadFileNotify notifier);
	}
}
