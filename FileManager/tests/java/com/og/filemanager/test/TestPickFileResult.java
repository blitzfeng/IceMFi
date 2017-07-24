package com.og.filemanager.test;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import com.og.filemanager.IntentFilterActivity;
import com.og.intents.FileManagerIntents;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class TestPickFileResult extends BaseTestFileManager {

    @Rule
    public ActivityResultTestRule<IntentFilterActivity> rule = new ActivityResultTestRule<>(IntentFilterActivity.class, false, false);

    @BeforeClass
    public static void setup() throws IOException {
        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
        createDirectory(sdcardPath + TEST_DIRECTORY);
        createFile(sdcardPath + "oi-filemanager-tests/oi-test1.txt", "bbb");
        createFile(sdcardPath + "oi-filemanager-tests/oi-test2.txt", "bbb");
    }

    @Test
    public void testPickFileResult() {
        Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
        intent.setData(Uri.parse(sdcardPath + "oi-filemanager-tests"));
        rule.launchActivity(intent);

        clickOnFile("oi-test1.txt");
        onView(ViewMatchers.withText(com.og.filemanager.R.string.pick_button_default)).perform(click());

        assertThat(rule.getActivityResult(),
                ActivityResultTestRule.hasResultCode(Activity.RESULT_OK));
        assertThat(rule.getActivityResult(),
                ActivityResultTestRule.hasResultData(hasData("file://" + sdcardPath + "oi-filemanager-tests/oi-test1.txt")));
    }

    @Test
    public void testGetContentResult() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setData(Uri.parse(sdcardPath + "oi-filemanager-tests"));
        rule.launchActivity(intent);

        clickOnFile("oi-test2.txt");
        onView(ViewMatchers.withText(com.og.filemanager.R.string.pick_button_default)).perform(click());

        assertThat(rule.getActivityResult(),
                ActivityResultTestRule.hasResultCode(Activity.RESULT_OK));
        assertThat(rule.getActivityResult(),
                ActivityResultTestRule.hasResultData(hasData("content://org.openintents.filemanager" + sdcardPath + "oi-filemanager-tests/oi-test2.txt")));
    }
}
