package com.og.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by blitzfeng on 2017/7/26.
 */

public class NetUtil {



    public static List<IPBean> getIp(){
        String urlStr = "http://dev.kuaidaili.com/api/getproxy/?orderid=970103565976644&num=30&area=%E4%B8%AD%E5%9B%BD&area_ex=%E5%8F%B0%E6%B9%BE&b_pcchrome=1&b_pcie=1&b_pcff=1&b_android=1&protocol=1&method=1&an_an=1&an_ha=1&sp1=1&sp2=1&sep=1";
        List<IPBean> list = new ArrayList<>();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(5000);
            System.out.println("repo code:" + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                Reader r = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(r);
                String ipAndPort = null;
                while ((ipAndPort = reader.readLine()) != null) {
                                    System.out.println("ip and port="+ipAndPort);
                    String[] result = ipAndPort.split(":");
                    IPBean bean = new IPBean();
                    bean.setIp(result[0]);
                    bean.setPort(Integer.parseInt(result[1]));
                    list.add(bean);
                }
            }
        } catch (NumberFormatException e){
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return getIp();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static void getIP(Callback callback){
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        String urlStr = "http://dev.kuaidaili.com/api/getproxy/?orderid=970103565976644&num=30&area=%E4%B8%AD%E5%9B%BD&area_ex=%E5%8F%B0%E6%B9%BE&b_pcchrome=1&b_pcie=1&b_pcff=1&b_android=1&protocol=1&method=1&an_an=1&an_ha=1&sp1=1&sp2=1&sep=1";
        final Request request = new Request.Builder().url(urlStr).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static final boolean ping() {

        String result = null;
        try {
            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.d("------ping-----", "result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            Log.d("----result---", "result = " + result);
        }
        return false;
    }


}
