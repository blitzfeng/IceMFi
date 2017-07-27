package com.og.util;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by blitzfeng on 2017/7/26.
 */

public class Util {
    public static List<IPBean> list = new ArrayList<>();
    public static int location = 0;
    public static ExecutorService threadPool = Executors.newCachedThreadPool();
    public static ScheduledExecutorService timerThread = Executors.newScheduledThreadPool(5);


    public static int getBuild(){
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator + "build");
        if(!file.exists())
            return 21;
        try {
            Reader r = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(r);
            String build = bufferedReader.readLine().trim();
            return Integer.parseInt(build);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 21;

    }
}
