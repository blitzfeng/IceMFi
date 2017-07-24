package com.og.filemanager;

import com.og.filemanager.util.CopyHelper;
import com.og.filemanager.util.MimeTypes;

import android.app.Application;

import net.youmi.android.AdManager;

public class FileManagerApplication extends Application{
	private CopyHelper mCopyHelper;
	
	@Override
	public void onCreate() {
		super.onCreate();

		mCopyHelper = new CopyHelper(this);
		MimeTypes.initInstance(this);
	}
	
	public CopyHelper getCopyHelper(){
		return mCopyHelper;
	}
}