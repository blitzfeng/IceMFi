/* 
 * Copyright (C) 2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.og.filemanager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.VisibleForTesting;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.og.filemanager.bookmarks.BookmarkListActivity;
import com.og.filemanager.db.DBDao;
import com.og.filemanager.files.FileHolder;
import com.og.filemanager.util.FileUtils;
import com.og.filemanager.compatibility.HomeIconHelper;
import com.og.filemanager.lists.SimpleFileListFragment;
import com.og.filemanager.util.UIUtils;
import com.og.intents.FileManagerIntents;
import com.og.util.IPBean;
import com.og.util.MLog;
import com.og.util.MenuIntentOptionsWithIcons;
import com.og.util.NetUtil;
import com.og.util.Util;
import com.og.util.WifiProxyManager;

import net.youmi.android.AdManager;
import net.youmi.android.nm.sp.SpotListener;
import net.youmi.android.nm.sp.SpotManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FileManagerActivity extends DistributionLibraryFragmentActivity implements SpotListener, Callback {
	@VisibleForTesting
	public static final String FRAGMENT_TAG = "ListFragment";
    
    protected static final int REQUEST_CODE_BOOKMARKS = 1;
	private ShowAdReceiver receiver;
	
	private SimpleFileListFragment mFragment;
	private TimerTask task;
	private Timer timer;
	/**
	 * 0 表示正常  1表示暂停   2表示终止
	 */
	private int isStop = 0;
	private int count = 0;
	private boolean isTaskRunning = false;
	private int location = 0;
	private WifiProxyManager wifiProxyManager;
	private DBDao dbDao;
	private IPBean bean;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 0:
					Toast.makeText(FileManagerActivity.this,"重新请求ip，次数："+requestCount,Toast.LENGTH_SHORT).show();
					break;
				case 1:

					FileManagerActivity.this.startActivity(new Intent(FileManagerActivity.this,TestActivity.class));
					break;
                case 2:
                	/*MLog.e("file","准备切换代理");
                    setProxy();
					isStop = 1;*/
                    break;
				case 3:
			//		AdManager.getInstance(FileManagerActivity.this).init("1204a74cefdec234", "c04e0d119fa30fdb", true);
					if(isStop == 1) {
						isStop = 0;
						MLog.d("file","设置isStop = 0");
					}else if(!SpotManager.getInstance(FileManagerActivity.this).isSpotShowing()) {
			//			SpotManager.getInstance(FileManagerActivity.this).showSpot(FileManagerActivity.this, FileManagerActivity.this);
						MLog.d("file","network");
					}
					break;
                case 4:
                    String ip = (String) msg.obj;
                    Toast.makeText(FileManagerActivity.this,"设置当前ip:"+ip+"--当前ip location="+location,Toast.LENGTH_SHORT).show();
                    break;
				case 5:
					checkLocation();
					break;
				case 6:
					Toast.makeText(FileManagerActivity.this,"一分钟后重新请求ip",Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};
	
	@Override
	protected void onNewIntent(Intent intent) {
		if(intent.getData() != null)
			mFragment.openInformingPathBar(new FileHolder(FileUtils.getFile(intent.getData()), this));
	}
	/**
	 * Either open the file and finish, or navigate to the designated directory. This gives FileManagerActivity the flexibility to actually handle file scheme data of any type.
	 * @return The folder to navigate to, if applicable. Null otherwise.
	 */
	private File resolveIntentData(){
		File data = FileUtils.getFile(getIntent().getData());
		if(data == null)
			return null;
		
		if(data.isFile() && ! getIntent().getBooleanExtra(FileManagerIntents.EXTRA_FROM_OI_FILEMANAGER, false)){
			FileUtils.openFile(new FileHolder(data, this), this);

			finish();
			return null;
		}
		else
			return FileUtils.getFile(getIntent().getData());
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		UIUtils.setThemeFor(this);

		super.onCreate(icicle);
		setContentView(R.layout.activity_test);

		receiver = new ShowAdReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("package removed");
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(receiver,filter);

		dbDao = new DBDao(this);

		location = dbDao.queryLocation();
		if(location == 0){//无数据
			NetUtil.getIP(this);
		}else {
			IPBean bean = dbDao.queryIP(location);
			if(bean == null)
				NetUtil.getIP(this);
			else
				setProxy(bean);
		}
		
		// Enable home button.
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			HomeIconHelper.activity_actionbar_setHomeButtonEnabled(this);
		
		// Search when the user types.
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		// If not called by name, open on the requested location.
		File data = resolveIntentData();

		// Add fragment only if it hasn't already been added.
		mFragment = (SimpleFileListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
		if(mFragment == null){
			mFragment = new SimpleFileListFragment();
			Bundle args = new Bundle();
			if(data == null)
				args.putString(FileManagerIntents.EXTRA_DIR_PATH, Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/");
			else
				args.putString(FileManagerIntents.EXTRA_DIR_PATH, data.toString());
			mFragment.setArguments(args);
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, mFragment, FRAGMENT_TAG).commit();
		}
		else {
			// If we didn't rotate and data wasn't null.
			if(icicle == null && data!=null)
				mFragment.openInformingPathBar(new FileHolder(new File(data.toString()), this));
		}

		SpotManager.getInstance(this).setImageType(SpotManager.IMAGE_TYPE_VERTICAL);
		SpotManager.getInstance(this).setAnimationType(SpotManager.ANIMATION_TYPE_NONE);

		timer = new Timer();
	}



	int requestCount = 0;
	private void setProxy(IPBean bean) {

		MLog.d("file","setProxy");

		MLog.e("file","设置当前ip:"+bean.getIp()+"--当前ip location="+location);

		Message msg = Message.obtain();
		msg.what = 4;
		msg.obj = bean.getIp();
		handler.sendMessage(msg);

		if(wifiProxyManager == null)
			wifiProxyManager = new WifiProxyManager(this);
		if(Util.getBuild()<=19)
			wifiProxyManager.setWifiProxySettings(bean.getIp(),bean.getPort());
		else
			wifiProxyManager.setWifiProxySettings(bean.getIp(),bean.getPort(),true);


		dbDao.updateLocation(++location);

	}

	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = new MenuInflater(this);
 		inflater.inflate(R.menu.main, menu);
 		
 	//	mDistribution.onCreateOptionsMenu(menu);
 		return true;
 	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Generate any additional actions that can be performed on the
		// overall list. This allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		// menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
		// new ComponentName(this, NoteEditor.class), null, intent, 0, null);

		// Workaround to add icons:
		MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this,
				menu);
		menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, FileManagerActivity.class), null,
				intent, 0, null);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.menu_search:
	//		onSearchRequested();展示
			if(!SpotManager.getInstance(this).isSpotShowing()) {
				SpotManager.getInstance(this).showSpot(FileManagerActivity.this, this);
				MLog.d("file","展示");
			}
			return true;
		
		case R.id.menu_settings:
			/*Intent intent = new Intent(this, PreferenceActivity.class);
			startActivity(intent);*/
			//下一条
			handler.sendEmptyMessage(5);
			return true;
		
		case R.id.menu_bookmarks:
	//		startActivityForResult(new Intent(FileManagerActivity.this, BookmarkListActivity.class), REQUEST_CODE_BOOKMARKS);
			/*List<IPBean> list = dbDao.query();
			if(list==null||list.size()==0||location>=list.size()){
				Toast.makeText(FileManagerActivity.this,"ip list为空或者遍历到list末尾",Toast.LENGTH_SHORT).show();
				return false;
			}
			timer = new Timer();
			isStop = 0;
			task = new TimerTask() {
				@Override
				public void run() {
					if(isStop == 1 || isStop == 2)
						return;
					*//*if(SpotManager.getInstance(FileManagerActivity.this).isSpotShowing())
						handler.sendEmptyMessage(0);*//*
					handler.sendEmptyMessage(1);
					count++;
					MLog.e("file","count="+count);
					if(count>=6) {
						count = 0;
						handler.sendEmptyMessage(2);

					}

				}
			};
			timer.schedule(task,15*1000,10*1000);
			Toast.makeText(this,"启动任务",Toast.LENGTH_SHORT).show();*/
			return true;
		case R.id.menu_endtask:
			/*timer.cancel();
			if(task!=null)
				task.cancel();
			isStop = 2;

			Toast.makeText(this,"结束任务",Toast.LENGTH_SHORT).show();*/
			break;

		case android.R.id.home:
			mFragment.browseToHome();
			return true;
		}
		return super.onOptionsItemSelected(item);

	}


	// The following methods should properly handle back button presses on every API Level.
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (VERSION.SDK_INT > VERSION_CODES.DONUT) {
			if (keyCode == KeyEvent.KEYCODE_BACK && mFragment.pressBack())
				return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (VERSION.SDK_INT <= VERSION_CODES.DONUT) {
			if (keyCode == KeyEvent.KEYCODE_BACK && mFragment.pressBack())
				return true;
		}

		return super.onKeyDown(keyCode, event);
	}

    /**
     * This is called after the file manager finished.
     */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
        case REQUEST_CODE_BOOKMARKS:
            if (resultCode == RESULT_OK && data != null) {
            	mFragment.openInformingPathBar(new FileHolder(new File(data.getStringExtra(BookmarkListActivity.KEY_RESULT_PATH)), this));
            }
            break;
        default:
        	super.onActivityResult(requestCode, resultCode, data);
        }
		
	}
	
	/**
	 * We override this, so that we get informed about the opening of the search dialog and start scanning silently.
	 */
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putString(FileManagerIntents.EXTRA_SEARCH_INIT_PATH, mFragment.getPath());
		startSearch(null, false, appData, false);
		
		return true;
	}


	@Override
	protected void onPause() {
		super.onPause();
		// 插屏广告
		SpotManager.getInstance(this).onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 插屏广告
		SpotManager.getInstance(this).onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 插屏广告
		SpotManager.getInstance(this).onDestroy();
		SpotManager.getInstance(this).onAppExit();
		if(wifiProxyManager!=null){
			wifiProxyManager.unset();
		}

		if(receiver!=null)
			unregisterReceiver(receiver);
	}

	@Override
	public void onShowSuccess() {

	}
	long currMis = 0;

	@Override
	public void onShowFailed(int i) {
		MLog.e("file","onShowFailed:"+i);
		if(i!=1){
			if((System.currentTimeMillis()-currMis)<20000) {

				return;
			}else
				currMis = System.currentTimeMillis();
		}
		switch (i){
			case 0:
				Toast.makeText(FileManagerActivity.this,"网络出现问题，20秒后重连，请等待",Toast.LENGTH_SHORT).show();
				reconnect();
				break;
			case 1:
				Toast.makeText(FileManagerActivity.this,"没有广告，重新请求",Toast.LENGTH_SHORT).show();
			//	setProxy();
				reconnect();
				break;
			case 2:
				Toast.makeText(FileManagerActivity.this,"资源加载未完成，20秒后重连",Toast.LENGTH_SHORT).show();
				reShowSpot();
				break;
			case 3:
				Toast.makeText(FileManagerActivity.this,"广告太频繁,20秒后重连，请等待",Toast.LENGTH_SHORT).show();
				reShowSpot();
				break;
		}
	}

	@Override
	public void onSpotClosed() {

	}

	@Override
	public void onSpotClicked(boolean b) {

	}

	private void reconnect(){
		AdManager.getInstance(FileManagerActivity.this).init("1204a74cefdec234", "c04e0d119fa30fdb", true);

		reShowSpot();
	}

	private synchronized void reShowSpot(){
		synchronized (handler){
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if(!SpotManager.getInstance(FileManagerActivity.this).isSpotShowing()) {
						SpotManager.getInstance(FileManagerActivity.this).showSpot(FileManagerActivity.this, FileManagerActivity.this);
						MLog.d("file","reShowSpot");
					}
				}
			},20*1000);
		}

	}

	@Override
	public void onFailure(Call call, IOException e) {
		MLog.e("file","onFailure");
		e.printStackTrace();
	//	handler.sendEmptyMessage(5);
		if(wifiProxyManager == null)
			wifiProxyManager = new WifiProxyManager(this);
		wifiProxyManager.unset();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				NetUtil.getIP(FileManagerActivity.this);
			}
		},60*1000);
	}

	@Override
	public void onResponse(Call call, Response response) throws IOException {
	//	System.out.println("response="+response.body().string());
		List<IPBean> ipBeanList = new ArrayList<>();
		Reader r = response.body().charStream();
		BufferedReader reader = new BufferedReader(r);
		String ipAndPort = null;
		try {
			while ((ipAndPort = reader.readLine()) != null) {
                                System.out.println("ip and port="+ipAndPort);
                String[] result = ipAndPort.split(":");
                IPBean bean = new IPBean();
                bean.setIp(result[0]);
                bean.setPort(Integer.parseInt(result[1]));
                ipBeanList.add(bean);
            }

			//数据库重置
			dbDao.insertIPInfo(ipBeanList);
			location = dbDao.queryLocation();
			if(location == 0)
				dbDao.insertLocation(1);
			else
				dbDao.updateLocation(1);
			setProxy(ipBeanList.get(0));
		} catch (Exception e) {
			e.printStackTrace();
	//		NetUtil.getIP(this);
		}

	}



	class ShowAdReceiver extends BroadcastReceiver{
		long currMis = 0;
		@Override
		public void onReceive(Context context, Intent intent) {
			if("package removed".equals(intent.getAction())){
				System.out.println("--------------------------------------------");
				if(!SpotManager.getInstance(context).isSpotShowing()) {
					SpotManager.getInstance(context).showSpot(context, FileManagerActivity.this);
					MLog.d("file","package ");
				}
			}else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (info.getState().equals(NetworkInfo.State.CONNECTED)&&(System.currentTimeMillis()-currMis)>3000) {
					currMis = System.currentTimeMillis();
					MLog.d("file","-----wifi connect"+"---"+location);
			//		checkLocation();
					/*boolean b = NetUtil.ping();
					if(!b){
						Toast.makeText(FileManagerActivity.this,"检测代理可以性:不可用  切换下个ip",Toast.LENGTH_SHORT).show();
						Util.location ++;
						if(!checkLocation())
							return ;
					}else {
						Toast.makeText(FileManagerActivity.this,"检测代理可以性:可用",Toast.LENGTH_SHORT).show();


					}*/
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							handler.sendEmptyMessage(3);
						}
					},4000);

				}
			}
		}
	}

	private void checkLocation() {
		int lo = dbDao.queryLocation();
		IPBean bean = dbDao.queryIP(lo);
		if(bean == null){
			MLog.e("file","列表为空或此列表ip已使用完毕");

			Toast.makeText(this, "列表为空或此列表ip已使用完毕",Toast.LENGTH_SHORT).show();
			dbDao.deleteIP();
			NetUtil.getIP(this);
			return;
		}
		setProxy(bean);
	}
}
