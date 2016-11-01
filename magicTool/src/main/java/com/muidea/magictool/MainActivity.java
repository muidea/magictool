package com.muidea.magictool;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.muidea.magictool.media_util.MediaUtil;

import java.io.File;
import java.util.List;

public class MainActivity extends BindServiceActivity implements BarCodeFragment.CallBack, DataManageFragment.CallBack, SettingFragment.CallBack {
	private SlidingMenu mSlidingMenu = null;

	private BarCodeFragment mTakePhotoFragment = new BarCodeFragment();;
	private DataManageFragment mDataManageFragment = new DataManageFragment();
	private SettingFragment mSettingFragment = new SettingFragment();

    private DataManageFragment.UploadFileNotify mUploadFileNotify = null;

	@Override
	protected void connectService() {

	}

	@Override
	protected void disconnectService() {

	}

	@Override
	protected boolean checkServiceStatus() {
		return false;
	}

	@Override
	protected void readyService() {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// configure the SlidingMenu
       mSlidingMenu = new SlidingMenu(this);  
       mSlidingMenu.setMode(SlidingMenu.LEFT);  //菜单从哪边出来
      
       mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN); // 设置触摸屏幕的模式 ，设置滑动的屏幕局限，该设置为全屏区域都可以滑动 
       mSlidingMenu.setShadowWidthRes(R.dimen.navigation_drawer_width);  //中间阴影宽度
       
       mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);  // 设置滑动菜单视图余下的宽度，SlidingMenu划出时主页面显示的剩余宽度  
       //menu.setBehindWidth(300);//设置SlidingMenu菜单的宽度
       
       mSlidingMenu.setFadeDegree(0.35f); ////SlidingMenu滑动时的渐变程度 
       mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT); //使SlidingMenu附加在Activity上  
       mSlidingMenu.setMenu(R.layout.fragment_navigation);   //设置menu的布局文件    //为侧滑菜单设置布局  

       
       mSlidingMenu.toggle();//动态断定主动封闭或开启SlidingMenu
       mSlidingMenu.showMenu();//显示SlidingMenu
       mSlidingMenu.showContent();//显示内容

		try {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.container, mTakePhotoFragment).commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

        RelativeLayout takePhotolayout = (RelativeLayout)findViewById(R.id.Fragment_vew_left_takephoto_layout);
		takePhotolayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectTakePhoto();
			}
		});

        RelativeLayout manageDatalayout = (RelativeLayout)findViewById(R.id.Fragment_vew_left_datamanager_layout);
		manageDatalayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectManagData();
			}
		});

        RelativeLayout settinglayout = (RelativeLayout)findViewById(R.id.Fragment_vew_left_setting_layout);
		settinglayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectSetting();
			}
		});

        File systemFile = new File(MediaUtil.getExternalStorageDir(), "magictool");
		if (!systemFile.exists()) {
			systemFile.mkdirs();
		}
		SystemConfig.instance().load(systemFile);
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mSlidingMenu.isMenuShowing()) {
				mSlidingMenu.toggle();
				return true;
			}

			return false;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@SuppressWarnings("deprecation")
	private void showExitGameAlert() {
		// 创建退出对话框
		AlertDialog isExit = new AlertDialog.Builder(this).create();
		// 设置对话框标题
		isExit.setTitle(R.string.alert_title);
		// 设置对话框消息
		isExit.setMessage(getString(R.string.alert_info));
		// 添加选择按钮并注册监听
		isExit.setButton(getString(R.string.alert_ok), listener);
		isExit.setButton2(getString(R.string.alert_cancel), listener);
		// 显示对话框
		isExit.show();
	}
	
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which)
		{
			switch (which)
			{
			case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
				finish();
				break;
			case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
				break;
			default:
				break;
			}
		}
	};	
	
	private void selectTakePhoto() {
		try {
            mUploadFileNotify = null;

			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.container, mTakePhotoFragment).commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mSlidingMenu.toggle();
	}
	
	private void selectManagData() {
		try {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.container, mDataManageFragment).commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mSlidingMenu.toggle();
	}
	
	private void selectSetting() {
		try {
            mUploadFileNotify = null;

			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.container, mSettingFragment).commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		mSlidingMenu.toggle();
	}


	@Override
	public void onReturnClick() {
		mSlidingMenu.toggle();
	}

	@Override
	public void onExitClick() {
		showExitGameAlert();
	}

	@Override
	public void onUploadFileClick(final String filePath, final List<String> fileList, final DataManageFragment.UploadFileNotify notifier){
        final SystemConfig.ServerInfo serverInfo = SystemConfig.instance().getUploadFileServer();

        mUploadFileNotify = notifier;

        mCoreService.postTask(new CoreService.ServiceTask() {
            @Override
            public boolean doTask() {
                for (final String file : fileList) {
                    File uploadFile = new File(filePath, file);

                    final boolean ret = mCoreService.uploadFile(serverInfo, uploadFile);
                    if (ret) {
                        uploadFile.delete();
                    }
					/*
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    final boolean ret = true;
                    */
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mUploadFileNotify != null) {
                                mUploadFileNotify.uploadFile(file, ret);
                            }
                        }
                    });
                }
                return true;
            }
        });
	}
}
