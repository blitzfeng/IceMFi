package com.og.filemanager.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.og.filemanager.FileManagerActivity;

import java.io.IOException;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

@RunWith(AndroidJUnit4.class)
public class TestFileManagerActivityWithIntents extends BaseTestFileManager {

    @Rule
    public ActivityTestRule<FileManagerActivity> rule = new ActivityTestRule<FileManagerActivity>(FileManagerActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Uri uri = Uri.parse("file://" + sdcardPath + "oi-filemanager-tests/oi-dir-to-open");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setClassName("org.openintents.filemanager",
                    FileManagerActivity.class.getCanonicalName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    };


    @BeforeClass
    public static void setup() throws IOException {
        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath()+'/';

        createDirectory(sdcardPath + TEST_DIRECTORY);
        createDirectory(sdcardPath + "oi-filemanager-tests/oi-dir-to-open");
        createDirectory(sdcardPath + "oi-filemanager-tests/oi-dir-to-open/oi-intent");
    }

    @Test
    public void testIntentUrl() throws IOException {
        checkFile("oi-intent", matches(isDisplayed()));
    }


}
