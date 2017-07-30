package com.og.filemanager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.og.filemanager.db.DBDao;
import com.og.util.IPBean;
import com.og.util.MLog;
import com.og.util.NetUtil;
import com.og.util.Util;
import com.og.util.WifiProxyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ShowActivity extends Activity implements Callback{


    private Timer timer;
    private TimerTask task;
    private int isStop = 0;
    private int count = 0;
    private int location = 0;
    private List<IPBean> list = new ArrayList<>();
    private WifiProxyManager wifiProxyManager;
    private NetworkReceiver receiver;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    ShowActivity.this.startActivity(new Intent(ShowActivity.this,TestActivity.class));
                    break;
                case 1:
                    MLog.e("file","准备切换代理");
                    checkLocation();
                    isStop = 1;
                    break;
                case 2:
                    Toast.makeText(ShowActivity.this,"ip获取成功，可以进行展示",Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(ShowActivity.this,"一分钟后重新请求ip",Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    String ip = (String) msg.obj;
                    Toast.makeText(ShowActivity.this,"设置当前ip:"+ip+"--当前ip location="+location,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        receiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(receiver,filter);

        NetUtil.getIP(this);

    }

    public void startShow(View v){

        timer = new Timer();
        isStop = 0;
        task = new TimerTask() {
            @Override
            public void run() {
                if(isStop == 1 || isStop == 2)
                    return;

                handler.sendEmptyMessage(0);
                count++;
                MLog.e("file","count="+count);
                if(count>=6) {
                    count = 0;
                    handler.sendEmptyMessage(1);

                }
            }
        };
        timer.schedule(task,15*1000,10*1000);
        Toast.makeText(this,"启动任务",Toast.LENGTH_SHORT).show();

    }

    public void stopShow(View v){
        timer.cancel();
        if(task!=null)
            task.cancel();
        isStop = 2;

        Toast.makeText(this,"结束任务",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
        MLog.e("file","onFailure");

        if(wifiProxyManager == null)
            wifiProxyManager = new WifiProxyManager(this);
        wifiProxyManager.unset();
        handler.sendEmptyMessage(3) ;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                NetUtil.getIP(ShowActivity.this);
            }
        },60*1000);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        List<IPBean> ipBeanList = new ArrayList<>();
        Reader r = response.body().charStream();
        BufferedReader reader = new BufferedReader(r);
        String ipAndPort = null;
        try {
            while ((ipAndPort = reader.readLine()) != null) {
                //                System.out.println("ip and port="+ipAndPort);
                String[] result = ipAndPort.split(":");
                IPBean bean = new IPBean();
                bean.setIp(result[0]);
                bean.setPort(Integer.parseInt(result[1]));
                ipBeanList.add(bean);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        list = ipBeanList;
        location = 0;
        handler.sendEmptyMessage(2);
        setProxy();
    }

    private void checkLocation(){

        if(location >= list.size()){
            MLog.e("file","列表为空或此列表ip已使用完毕");

            Toast.makeText(this, "列表为空或此列表ip已使用完毕",Toast.LENGTH_SHORT).show();
            NetUtil.getIP(this);
            return;
        }
        setProxy();

    }

    private void setProxy() {

        MLog.d("file", "setProxy");
        IPBean bean = list.get(location);
        MLog.e("file", "设置当前ip:" + bean.getIp() + "--当前ip location=" + location);

        Message msg = Message.obtain();
        msg.what = 4;
        msg.obj = bean.getIp();
        handler.sendMessage(msg);

        if (wifiProxyManager == null)
            wifiProxyManager = new WifiProxyManager(this);
        if (Util.getBuild() <= 19)
            wifiProxyManager.setWifiProxySettings(bean.getIp(), bean.getPort());
        else
            wifiProxyManager.setWifiProxySettings(bean.getIp(), bean.getPort(), true);
        ++location;
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver!=null)
            unregisterReceiver(receiver);
    }

    class NetworkReceiver extends BroadcastReceiver{

        long currMis = 0;
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.CONNECTED) && (System.currentTimeMillis() - currMis) > 3000) {
                    currMis = System.currentTimeMillis();
                    MLog.d("file", "-----wifi connect" + "---" + location);

                    isStop = 0;

                }
            }
        }
    }
}
