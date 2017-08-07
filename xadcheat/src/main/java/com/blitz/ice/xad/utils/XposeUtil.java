package com.blitz.ice.xad.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by blitzfeng on 2017/7/19.
 */

public class XposeUtil {

    public static String pkg1 = "de.robv.android.xpose.installer";
    public static String pkg2 = "com.blitz.ice.xadcheat";

    public static List<PackageInfo> generateRandomPackage(Context context) {
        List<PackageInfo> list = new ArrayList<>();
        AssetManager manager = context.getAssets();
        try {
            InputStream is = manager.open("package");
            Reader re = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(re);
            String pack = null;
            while ((pack = reader.readLine()) != null) {
                PackageInfo info = new PackageInfo();
                info.packageName = pack;
                list.add(info);
            }
            //随机移除一部分包名
            Random random = new Random();
            if(list.size()>35) {
                int[] num = {27,29,30,31,32,33,34,35};
                int length = num[random.nextInt(8)];
                for (int i = 0; i < length; i++) {
                    int location = random.nextInt(list.size());
                    list.remove(location);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

}
