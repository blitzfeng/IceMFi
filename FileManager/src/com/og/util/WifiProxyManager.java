package com.og.util;

/**
 * Created by blitzfeng on 2017/3/20.
 */

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class WifiProxyManager {
    private Context mContext = null;

    public WifiProxyManager(Context context) {
        mContext = context;
    }

    public static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    public static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class) f.getType(), value));
    }

    public static void setProxySettings(String assign, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "proxySettings");
    }
    public static void setProxySettings(String assign, Class sclass)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InstantiationException {
    //    setEnumField(sclass, assign, "ProxySettings");
        Field field = sclass.getField("proxySettings");
        field.set(sclass.newInstance(),Enum.valueOf((Class) field.getType(),assign));
    }

    WifiConfiguration GetCurrentWifiConfiguration(WifiManager manager) {
        if (!manager.isWifiEnabled())
            return null;

        List configurationList = manager.getConfiguredNetworks();
        WifiConfiguration configuration = null;
        int cur = manager.getConnectionInfo().getNetworkId();
        for (int i = 0; i <configurationList.size() ;++i)
        {
            WifiConfiguration wifiConfiguration = (WifiConfiguration) configurationList.get(i);
            if (wifiConfiguration.networkId == cur)
                configuration = wifiConfiguration;
        }

        return configuration;
    }

    public void setWifiProxySettings(String ipaddr,int port) {
        //get the current wifi configuration
        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = GetCurrentWifiConfiguration(manager);
        String bssid = config.BSSID;

        Log.e("input config ", config == null ? "null" : "not null");
        if (null == config)
            return;
        try {
            //get the link properties from the wifi configuration
            Object linkProperties = getField(config, "linkProperties");
            if (null == linkProperties)
                return;

            //get the setHttpProxy method for LinkProperties
            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);
            android.net.LinkProperties lp;
            //get ProxyProperties constructor
            Class[] proxyPropertiesCtorParamTypes = new Class[3];
            proxyPropertiesCtorParamTypes[0] = String.class;
            proxyPropertiesCtorParamTypes[1] = int.class;
            proxyPropertiesCtorParamTypes[2] = String.class;

            Constructor proxyPropertiesCtor = proxyPropertiesClass.getConstructor(proxyPropertiesCtorParamTypes);

            //create the parameters for the constructor
            Object[] proxyPropertiesCtorParams = new Object[3];
            proxyPropertiesCtorParams[0] = ipaddr;//"127.0.0.1";
            proxyPropertiesCtorParams[1] = port;//1080;
            proxyPropertiesCtorParams[2] = null;

            //create a new object using the params
            Object proxySettings = proxyPropertiesCtor.newInstance(proxyPropertiesCtorParams);

            //pass the new object to setHttpProxy
            Object[] params = new Object[1];
            params[0] = proxySettings;
            setHttpProxy.invoke(linkProperties, params);

            setProxySettings("STATIC", config);

            //save the settings
            manager.updateNetwork(config);
            manager.disconnect();
            manager.reconnect();
        } catch (Exception e) {
            Log.e("input manger ", "manager error", e);
        }
    }
    public void setWifiProxySettings(String ipaddr,int port,boolean flag) {
        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = GetCurrentWifiConfiguration(manager);
     //   String bssid = config.BSSID;

        Log.e("input config ", config == null ? "null" : "not null");
        if (null == config)
            return;
        try {
            Class proxyPropertiesClass = Class.forName("android.net.ProxyInfo");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class lpClass = Class.forName("android.net.wifi.WifiConfiguration");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);
            //get ProxyProperties constructor
            Class[] proxyPropertiesCtorParamTypes = new Class[3];
            proxyPropertiesCtorParamTypes[0] = String.class;
            proxyPropertiesCtorParamTypes[1] = int.class;
            proxyPropertiesCtorParamTypes[2] = String.class;

            Constructor proxyPropertiesCtor = proxyPropertiesClass.getConstructor(proxyPropertiesCtorParamTypes);

            //create the parameters for the constructor
            Object[] proxyPropertiesCtorParams = new Object[3];
            proxyPropertiesCtorParams[0] = ipaddr;//"127.0.0.1";
            proxyPropertiesCtorParams[1] = port;//1080;
            proxyPropertiesCtorParams[2] = null;

            //create a new object using the params
            Object proxySettings = proxyPropertiesCtor.newInstance(proxyPropertiesCtorParams);

            //pass the new object to setHttpProxy
            Object[] params = new Object[1];
            params[0] = proxySettings;
            setHttpProxy.invoke(config, params);

            Class ip = Class.forName("android.net.IpConfiguration");

            Field field = ip.getField("proxySettings");
            Method method = lpClass.getDeclaredMethod("setProxySettings",new Class[]{field.getType()});
            method.setAccessible(true);

            Enum en = Enum.valueOf((Class) field.getType(),"STATIC");
            method.invoke(config,en);

        //    setProxySettings("STATIC", ip);

            //save the settings
            manager.updateNetwork(config);
            manager.disconnect();
            manager.reconnect();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void unsetWifiProxySettings(boolean flag){
        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = GetCurrentWifiConfiguration(manager);
        if (null == config)
            return;

        try {
            Class proxyPropertiesClass = Class.forName("android.net.ProxyInfo");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class lpClass = Class.forName("android.net.wifi.WifiConfiguration");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);


            //pass the new object to setHttpProxy
            Object[] params = new Object[1];
            params[0] = null;
            setHttpProxy.invoke(config, params);

            Class ip = Class.forName("android.net.IpConfiguration");

            Field field = ip.getField("proxySettings");
            Method method = lpClass.getDeclaredMethod("setProxySettings",new Class[]{field.getType()});
            method.setAccessible(true);

            Enum en = Enum.valueOf((Class) field.getType(),"NONE");
            method.invoke(config,en);

            //    setProxySettings("STATIC", ip);

            //save the settings
            manager.updateNetwork(config);
            manager.disconnect();
            manager.reconnect();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void unset(){
        if(Util.getBuild()<=19)
            unsetWifiProxySettings();
        else
            unsetWifiProxySettings(true);
    }

    public void unsetWifiProxySettings() {
        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = GetCurrentWifiConfiguration(manager);
        if (null == config)
            return;

        try {
            //get the link properties from the wifi configuration
            Object linkProperties = getField(config, "linkProperties");
            if (null == linkProperties)
                return;

            //get the setHttpProxy method for LinkProperties
            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            //pass null as the proxy
            Object[] params = new Object[1];
            params[0] = null;
            setHttpProxy.invoke(linkProperties, params);

            setProxySettings("NONE", config);

            //save the config
            manager.updateNetwork(config);
            manager.disconnect();
            manager.reconnect();
        } catch (Exception e) {
        }
    }
}