package com.blitz.ice.xad;

import android.content.ComponentName;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.blitz.ice.xad.db.DBDao;
import com.blitz.ice.xad.db.DeviceBean;
import com.blitz.ice.xad.utils.DataUtil;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView ;
    List<DeviceBean> list = new ArrayList<>();
    DBDao dbDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.iv);
        String ss = DataUtil.getRandom(15,0);
        System.out.println("ss:"+ss);

System.out.println("n:"+n()+"--"+m());
        System.out.println("a:"+a(n()+m()));
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        List<ScanResult> scanResultList = wifiManager.getScanResults();
        for(ScanResult result:scanResultList)
            System.out.println("result:"+result.BSSID+"---"+result.SSID);
        Log.d("wifiInfo", wifiInfo.toString());
        Log.d("SSID",wifiInfo.getSSID());
        Log.d("BSSID",wifiInfo.getBSSID());
        Log.d("mac",wifiInfo.getMacAddress());
/*
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        Log.d("networktype",telephonyManager.getNetworkType()+"");
     //   Log.d("phonenum",telephonyManager.getLine1Number());
        Log.d("sim operator",telephonyManager.getSimOperator());
        Log.d("sim operator",telephonyManager.getSimOperatorName());
        Log.d("phone type",telephonyManager.getPhoneType()+"");
        Log.d("imsi",telephonyManager.getSubscriberId());
        Log.d("simnum",telephonyManager.getSimSerialNumber());*/

        System.out.println("imei:"+DataUtil.generateIMEI());

        dbDao = new DBDao(this);

        Log.d("ssid","--"+DataUtil.generateSSID());


    }
    public static String n()
    {
        return new String(new BigInteger("59a860345b335056065972a615609b8438025b113423b8219", 12).toByteArray());
    }
    public static String m()
    {
        return new String(new BigInteger("67777115a917b2ab86865125a32b21a756a8a388920109b97040681243b0333667b97aa", 12).toByteArray());
    }

    public static String a(String paramString)
    {
        Object localObject2 = null;
        Object localObject1 = localObject2;
        if (paramString != null) {}
        try
        {
            int i = paramString.length();
            localObject1 = localObject2;
            if (i > 0) {}
            return null;
        }
        catch (Throwable throwable)
        {
            try
            {
                localObject1 = MessageDigest.getInstance("MD5");
                byte[] bytes = paramString.getBytes();
                ((MessageDigest)localObject1).update(bytes, 0, bytes.length);
                localObject1 = String.format("%032x", new Object[] { new BigInteger(1, ((MessageDigest)localObject1).digest()) });
                return (String)localObject1;
            }
            catch (Throwable throwable1) {}
            paramString = paramString;
            return "";
        }
    }

    public void addData(View v){
        List<String> imeiList = DataUtil.getImei(this);
        List<String> imsiList = DataUtil.getImsi(this);
        int imeiSize = imeiList.size();
        int imsiSize = imsiList.size();
        for(int i=0;i<500;i++){

            DeviceBean bean = new DeviceBean();
            String operatorName = DataUtil.generateSimOperateorName();
            bean.setSimOperatorName(operatorName);
            bean.setSimOperator(DataUtil.getSimOperator(operatorName));
            /*if(i<imeiSize)
                bean.setImei(imeiList.get(i));
            if(i<imsiSize)
                bean.setImsi(imsiList.get(i));*/
            bean.setImsi(DataUtil.generateIMSI());
            bean.setImei(DataUtil.generateIMEI());
            //    bean.setPhoneNum();
            bean.setNetworkType(DataUtil.generateNetworkType()+"");
            String brand = DataUtil.getBrand();
            String product = DataUtil.getProduct(brand);
            bean.setBrand(brand);
            bean.setBoard(product);
            bean.setDevice(product);
            bean.setHardware(product);
            bean.setManufacturer(brand);
            bean.setModel(brand);
            bean.setBssid(DataUtil.generateBSSID());
            bean.setSsid(DataUtil.generateSSID());
            bean.setMac(DataUtil.generateMac());
            bean.setProduct(product);
            bean.setPhoneType(DataUtil.generatePhoneType()+"");
            bean.setSerial(DataUtil.generateSerial());
            bean.setAndroidId(DataUtil.generateAndroidId());
            bean.setFingerPrint(DataUtil.generateFingerPrint(brand,product));
            bean.setSimSerialNumber(DataUtil.generateSimSerialNum());
            String sdk = DataUtil.generateSDK();
            bean.setSdk(sdk);
            bean.setRelease(DataUtil.getRelease(sdk));
            dbDao.insertDeviceInfo(bean);

        }
    }

    public void jump(View v){
        Intent intent = new Intent();//context.getPackageManager().getLaunchIntentForPackage("com.og.filemanager");
        ComponentName componentName = new ComponentName("com.og.filemanager","com.og.filemanager.FileManagerActivity");
        //            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(componentName);
        startActivity(intent);
    }

   /* private static boolean a(Context paramContext, InputStream paramInputStream, File paramFile) throws IOException {
        if (paramInputStream == null) {
            return false;
        }
        GZIPInputStream localGZIPInputStream = null;
        ByteArrayOutputStream localByteArrayOutputStream = null;
        byte[] arrayOfByte1 = null;
        try
        {
            localGZIPInputStream = new GZIPInputStream(paramInputStream);
            localByteArrayOutputStream = new ByteArrayOutputStream();
            byte[] arrayOfByte2 = new byte['Ѐ'];
            int i;
            while ((i = localGZIPInputStream.read(arrayOfByte2, 0, 1024)) != -1) {
                localByteArrayOutputStream.write(arrayOfByte2, 0, i);
            }
            localByteArrayOutputStream.flush();
            arrayOfByte1 = localByteArrayOutputStream.toByteArray();
        }
        catch (Throwable localThrowable1) {}finally
        {
            if(localByteArrayOutputStream!=null)
                localByteArrayOutputStream.close();
            if(localGZIPInputStream!=null)
                localGZIPInputStream.close();
            if(paramInputStream!=null)
                paramInputStream.close();
        }
        if (arrayOfByte1 == null) {
            return false;
        }
        String str = new String(Arrays.copyOfRange(arrayOfByte1, arrayOfByte1.length - 128, arrayOfByte1.length));
        d.a(paramContext, str);
        FileOutputStream localFileOutputStream = null;
        try
        {
            int j = arrayOfByte1.length - 128 - d.a(str) % (arrayOfByte1.length - 128);
            localFileOutputStream = new FileOutputStream(paramFile);
            localFileOutputStream.write(arrayOfByte1, j, arrayOfByte1.length - 128 - j);
            localFileOutputStream.write(arrayOfByte1, 0, j);
            localFileOutputStream.flush();
            boolean bool = true;
            return bool;
        }
        catch (Throwable localThrowable2) {}
        return false;
    }*/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(dbDao!=null)
            dbDao.close();
    }
}
