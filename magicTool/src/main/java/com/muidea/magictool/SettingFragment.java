package com.muidea.magictool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingFragment extends Fragment {
	protected Button mBackButton = null;
    protected Button mExitButton = null;
	protected EditText mServerUrlEditText = null;

	protected CallBack mCallBack = null;
	
	public SettingFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

		mBackButton = (Button)rootView.findViewById(R.id.setting_bt_back);
		mBackButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mCallBack != null) {
					mCallBack.onReturnClick();
				}
			}
		});

        mExitButton = (Button)rootView.findViewById(R.id.setting_bt_exit);
        mExitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.onExitClick();
            }
        });

		mServerUrlEditText =(EditText)rootView.findViewById(R.id.id_ServerAddrEditText);
		mServerUrlEditText.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
                mServerUrlEditText.setFocusableInTouchMode(true);
                mServerUrlEditText.setFocusable(true);
				return true;
			}
		});

        mServerUrlEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mServerUrlEditText.setFocusable(false);
                    mServerUrlEditText.setFocusableInTouchMode(false);

                    SystemConfig.instance().setServerAddr(mServerUrlEditText.getText().toString());

                    return true;
                }

                return false;
            }
        });

        String serverAddr = SystemConfig.instance().getServerAddr();
        mServerUrlEditText.setText(serverAddr);


		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (activity instanceof CallBack) {
			mCallBack = (CallBack)activity;
		}		
	}

	public void onResume() {
		super.onResume();		
	}

	public void onPause() {
		super.onPause();
	}
	
	static public interface CallBack {
		public void onReturnClick();
        public void onExitClick();
	}
}
