/*
 * This is an example test project created in Eclipse to test NotePad which is a sample 
 * project located in AndroidSDK/samples/android-11/NotePad
 * Just click on File --> New --> Project --> Android Project --> Create Project from existing source and
 * select NotePad.
 * 
 * Then you can run these test cases either on the emulator or on device. You right click
 * the test project and select Run As --> Run As Android JUnit Test
 * 
 * @author Renas Reda, renas.reda@jayway.com
 * 
 */

package com.og.filemanager.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

import com.og.filemanager.FileHolderListAdapter;
import com.og.filemanager.FileManagerActivity;
import com.og.filemanager.files.FileHolder;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.og.filemanager.PreferenceActivity;

import org.openintents.util.VersionUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.openContextualActionModeOverflowMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class TestFileManagerActivity extends BaseTestFileManager {

	private static String filenameIsInRightDirectory;
	private Random random = new Random();

	@Rule
    public ActivityTestRule<FileManagerActivity> rule = new ActivityTestRule<>(FileManagerActivity.class);


	@BeforeClass
	public static void setUp() throws Exception {

		Context context = InstrumentationRegistry.getTargetContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit()
				.putBoolean("eula_accepted", true)
				.putInt("org.openintents.distribution.version_number_check", VersionUtils.getVersionCode(context))
				.commit();
		sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath()+'/';

		// need to do this before creating activity
		cleanDirectory(new File(sdcardPath + TEST_DIRECTORY));
		createDirectory(sdcardPath + TEST_DIRECTORY);

		createFile(sdcardPath + TEST_DIRECTORY + "/oi-rem-test.txt", "");
		filenameIsInRightDirectory = "oi-test-is-in-right-directory";
		createFile(sdcardPath + TEST_DIRECTORY + "/" + filenameIsInRightDirectory, "");

		createDirectory(sdcardPath + "oi-filemanager-tests/oi-move-target");
		createFile(sdcardPath + "oi-filemanager-tests/oi-file-1.txt", "");
		createFile(sdcardPath + "oi-filemanager-tests/oi-file-2.txt", "");
		createFile(sdcardPath + "oi-filemanager-tests/oi-file-3.txt", "");
		createFile(sdcardPath + "oi-filemanager-tests/oi-file-4.txt", "");
		createFile(sdcardPath + "oi-filemanager-tests/oi-file-5.txt", "");
		createFile(sdcardPath + "oi-filemanager-tests/.oi-hidden.txt", "");
	}

	@Before
	public void setUpTest() {
		Espresso.registerIdlingResources(new DirectoryScannerIdlingResource(rule.getActivity()));
	}

    @After
	public void tearDown() throws Exception {
		//cleanDirectory(new File(sdcardPath + TEST_DIRECTORY));
	}

	@Test
	public void testNavigation() throws IOException {
//		if(solo.searchText("Accept")) {
//			solo.clickOnButton("Accept");
//			if(solo.searchButton("Continue"))
//				solo.clickOnButton("Continue");
//		}
		createDirectory(sdcardPath + TEST_DIRECTORY);
		createFile(sdcardPath + "oi-filemanager-tests/oi-test.txt", "");
		createDirectory(sdcardPath + "oi-filemanager-tests/oi-test-dir");
		createFile(sdcardPath + "oi-filemanager-tests/oi-test-dir/oi-fff.txt", "");

        clickOnTestDirectory();
		onView(withText(TEST_DIRECTORY)).check(matches(isDisplayed()));
        clickOnFile("oi-test-dir");
		onView(withText("oi-fff.txt")).check(matches(isDisplayed()));

        pressBack();
        pressBack();

        clickOnTestDirectory();
		onData(hasName("oi-test.txt")).check(matches(isDisplayed()));

		clickOnFile("oi-test-dir");
        pressBack();
		onData(hasName("oi-test.txt")).check(matches(isDisplayed()));
        pressBack();
	}

	@Test
	public void testModification() throws IOException {
        clickOnTestDirectory();
        longClickOnFile("oi-rem-test.txt");

        onView(ViewMatchers.withContentDescription(com.og.filemanager.R.string.menu_delete)).perform(click());
        onView(withText(android.R.string.ok)).perform(click());

        openActionBarOverflowOrOptionsMenu(rule.getActivity());

        onView(ViewMatchers.withText(com.og.filemanager.R.string.menu_create_folder)).perform(click());
        onView(ViewMatchers.withId(com.og.filemanager.R.id.foldername)).perform(replaceText("oi-created-folder"));
        onView(withText(android.R.string.ok)).perform(click());

        checkFile("oi-created-folder", matches(isDisplayed()));
        pressBack();
		
		File createdFolder = new File(sdcardPath + "oi-filemanager-tests/oi-created-folder");
		assertThat(createdFolder.exists(), is(true));
        assertThat(createdFolder.isDirectory(), is(true));
        assertThat(new File(sdcardPath + "oi-filemanager-tests/oi-rem-test.txt").exists(), is(false));
	}

	@Test
	public void testBookmarks() throws IOException {
		String fn = "oi-bookmark-" + random.nextInt(1000);
		createDirectory(sdcardPath + TEST_DIRECTORY);
		createDirectory(sdcardPath + "oi-filemanager-tests/" + fn);
		createFile(sdcardPath + "oi-filemanager-tests/" + fn + "/oi-inside-book.txt", "");
		
		// create bookmark
        clickOnTestDirectory();
		longClickOnFile(fn);

        openContextualActionModeOverflowMenu();
        onView(ViewMatchers.withText(com.og.filemanager.R.string.menu_bookmark)).perform(click());
        clickOnFile(fn);

		checkFile("oi-inside-book.txt", matches(isDisplayed()));


		// remove it
        openActionBarOverflowOrOptionsMenu(rule.getActivity());
        onView(ViewMatchers.withText(com.og.filemanager.R.string.menu_bookmarks)).perform(click());
		longClickOnBookmark(fn);
        onView(ViewMatchers.withContentDescription(com.og.filemanager.R.string.menu_delete)).perform(click());

        pressBack();

        // make sure that it is deleted
        openActionBarOverflowOrOptionsMenu(rule.getActivity());
        onView(ViewMatchers.withText(com.og.filemanager.R.string.menu_bookmarks)).perform(click());

		checkIsNotContainedInList(hasBookmarkName(fn));
		pressBack();
        pressBack();
	}

	private void checkIsNotContainedInList(Matcher<Object> matches) {
		onView(withId(android.R.id.list))
				.check(matches(not(withAdaptedData(matches))));
	}

	@Test
    public void testActions() throws IOException {

        clickOnTestDirectory();
		// copy
		longClickOnFile("oi-file-1.txt");
		openContextualActionModeOverflowMenu();
		onView(ViewMatchers.withText(com.og.filemanager.R.string.menu_copy)).perform(click());

		navigateToTargetAndPasteAndCheck("oi-move-target", "oi-file-1.txt", null);
		checkFile("oi-file-1.txt", matches(isDisplayed()));

		// move
		longClickOnFile("oi-file-2.txt");
		onView(ViewMatchers.withContentDescription(com.og.filemanager.R.string.menu_move)).perform(click());
		navigateToTargetAndPasteAndCheck("oi-move-target", "oi-file-2.txt", null);
		checkIsNotContainedInList(hasName("oi-file-2.txt"));

		// multi select
		if(android.os.Build.VERSION.SDK_INT < 11){
			onView(ViewMatchers.withText(com.og.filemanager.R.id.menu_multiselect)).perform(click());
			clickOnFile("oi-file-3.txt");
			clickOnFile("oi-file-4.txt");
			onView(ViewMatchers.withId(com.og.filemanager.R.id.menu_copy)); // TODO verify solo.clickOnImageButton(1);
			pressBack();
			
			navigateToTargetAndPasteAndCheck("oi-move-target", "oi-file-3.txt", "oi-file-4.txt");
		}

		// rename
		longClickOnFile("oi-file-5.txt");
		openContextualActionModeOverflowMenu();
		onView(ViewMatchers.withText(com.og.filemanager.R.string.menu_rename)).perform(click());
		onView(ViewMatchers.withId(com.og.filemanager.R.id.foldername)).perform(replaceText("oi-renamed-file.txt"));
		onView(withText(android.R.string.ok)).perform(click());
		checkFile("oi-renamed-file.txt", matches(isDisplayed()));

		pressBack();
	}
	
	private void navigateToTargetAndPasteAndCheck(String dirname, String name1, String name2) throws IOException {
		createDirectory(sdcardPath + "oi-filemanager-tests/");
		clickOnFile(dirname);

		openActionBarOverflowOrOptionsMenu(rule.getActivity());
		onView(ViewMatchers.withText(com.og.filemanager.R.string.menu_paste)).perform(click());

		checkFile(name1, matches(isDisplayed()));

		if(name2 != null) {
			checkFile(name2, matches(isDisplayed()));
		}

		pressBack();
	}

	@Test
	public void testDetails() throws IOException {
		createDirectory(sdcardPath + TEST_DIRECTORY);
		createFile(sdcardPath + "oi-filemanager-tests/oi-detail.txt", "abcdefg");

		clickOnTestDirectory();


		longClickOnFile("oi-detail.txt");
		openContextualActionModeOverflowMenu();
		onView(ViewMatchers.withText(com.og.filemanager.R.string.menu_details)).perform(click());

		onView(ViewMatchers.withText(com.og.filemanager.R.string.details_type_file)).check(matches(isDisplayed()));

		onView(ViewMatchers.withId(com.og.filemanager.R.id.details_size_value)).check(matches(withText(Formatter.formatFileSize(rule.getActivity(), 7))));

		
		// not sure:
		//Calendar today = new GregorianCalendar();
		//String todayString = today.get(Calendar.DAY_OF_MONTH) + "/" + today.get(Calendar.MONTH) + "/" + today.get(Calendar.YEAR);
		//assertTrue(solo.searchText(todayString));

		pressBack();
		pressBack();
	}

	@Test
	public void testHiddenFiles() throws IOException {
		clickOnTestDirectory();

		PreferenceActivity.setDisplayHiddenFiles(rule.getActivity(), true);
		checkFile(".oi-hidden.txt", matches(isDisplayed()));
		
		openActionBarOverflowOrOptionsMenu(rule.getActivity());
		onView(ViewMatchers.withText(com.og.filemanager.R.string.settings)).perform(click());
		
		onView(allOf(ViewMatchers.withText(com.og.filemanager.R.string.preference_displayhiddenfiles_title), withResourceName("android:id/title"))).perform(click());
		
		pressBack();
		checkIsNotContainedInList(hasName(".oi-hidden.txt"));

		pressBack();
	}

	@Test
	public void testOrder() throws IOException, InterruptedException {
		createDirectory(sdcardPath + TEST_DIRECTORY);
		createFile(sdcardPath + "oi-filemanager-tests/oi-b.txt", "bbb");
		Thread.sleep(10); // make sure that next file is younger
		createFile(sdcardPath + "oi-filemanager-tests/oi-a.txt", "aaaaaa");
		Thread.sleep(10);
		createFile(sdcardPath + "oi-filemanager-tests/oi-c.txt", "");
		clickOnTestDirectory();

		String[] sortOrders = rule.getActivity().getResources().getStringArray(com.og.filemanager.R.array.preference_sortby_names);
		
		setAscending(true);
		setSortOrder(sortOrders[0]);
		isSortedInThisOrder("oi-a.txt", "oi-b.txt", "oi-c.txt");
		
		setSortOrder(sortOrders[1]);
		isSortedInThisOrder("oi-c.txt", "oi-b.txt", "oi-a.txt");
		
		setSortOrder(sortOrders[2]);
		isSortedInThisOrder("oi-b.txt", "oi-a.txt", "oi-c.txt");
		
		setAscending(false);
		setSortOrder(sortOrders[0]);
		isSortedInThisOrder("oi-c.txt", "oi-b.txt", "oi-a.txt");
	}
	
	private void setSortOrder(String name) {
		openActionBarOverflowOrOptionsMenu(rule.getActivity());
		onView(ViewMatchers.withText(com.og.filemanager.R.string.settings)).perform(click());
		onView(ViewMatchers.withText(com.og.filemanager.R.string.preference_sortby)).perform(click());
		onView(withText(name)).perform(click());
		pressBack();
	}
	
	private void setAscending(boolean enabled) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(rule.getActivity());
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("ascending", enabled);
		editor.commit();
	}


	private static Matcher<View> isSortedInThisOrder(final String a, final String b, final String c) {
		return new TypeSafeMatcher<View>() {

			@Override
			protected boolean matchesSafely(View item) {
				ListView fileList = (ListView) item;
				FileHolderListAdapter adapter = (FileHolderListAdapter) fileList.getAdapter();
				int positionOfA = find(adapter, a);
				int positionOfB = find(adapter, b);
				int positionOfC = find(adapter, c);
				return positionOfA < positionOfB && positionOfB < positionOfC;
			}

			private int find(FileHolderListAdapter adapter, String fileName) {
				int size = adapter.getCount();
				for (int i=0; i < size; i++) {
					if (((FileHolder) adapter.getItem(i)).getName().equals(fileName)) {
						return i;
					}
				}
				return -1;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("has items sorted in this order: " + a + " " + b + " " + c);
			}
		};
	}

	@Test
	public void testBrowseToOnPressEnter() throws IOException {
		
		/*
		 *  We start at the SD card. 
		 */
		onView(withText(Environment.getExternalStorageDirectory().getParentFile().getName())).perform(longClick());
		onView(ViewMatchers.withId(com.og.filemanager.R.id.path_bar_path_edit_text)).perform(click()); // Let the editText have focus to be able to send the enter key.
		onView(ViewMatchers.withId(com.og.filemanager.R.id.path_bar_path_edit_text)).perform(replaceText(sdcardPath + TEST_DIRECTORY));
		onView(ViewMatchers.withId(com.og.filemanager.R.id.path_bar_path_edit_text)).perform(pressKey(KeyEvent.KEYCODE_ENTER));


		checkFile(filenameIsInRightDirectory, matches(isDisplayed()));
		
		pressBack();
		pressBack();
	}
	
// Current implementation directly opens the file and therefore can't be tested.
//	public void testIntentUri() throws IOException {
//		createDirectory(sdcardPath + "oi-filemanager-tests");
//		createFile(sdcardPath + "oi-filemanager-tests/oi-to-open.txt", "bbb");		
//		
//		Intent intent = new Intent(Intent.ACTION_VIEW);
//		intent.setData(Uri.parse("file://" + sdcardPath + "oi-filemanager-tests/oi-to-open.txt"));
//		intent.setClass(activity, FileManagerActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		
//		activity = getInstrumentation().startActivitySync(intent);
//		
//		assertTrue(solo.searchText("oi-to-open.txt"));
//		pressBack();
//		pressBack();	
//	}
	
//	Removed as Filter action is obsolete and removed.
//	public void testFilters() throws IOException {
//		createDirectory(sdcardPath + "oi-filemanager-tests");
//		createFile(sdcardPath + "oi-filemanager-tests/oi-not-filter.txt", "");
//		createFile(sdcardPath + "oi-filemanager-tests/oi-filtered.py", "");
//		createDirectory(sdcardPath + "oi-filemanager-tests/oi-f-dir");
//		solo.clickOnText("oi-filemanager-tests");
//		
//		solo.clickOnMenuItem(getAppString(R.string.menu_filter));
//		solo.enterText(0, ".py");
//		solo.clickOnButton(getAppString(android.R.string.ok));
//		
//		assertTrue(solo.searchText("oi-filtered.py"));
//		assertTrue(solo.searchText("oi-f-dir"));
//		assertFalse(solo.searchText("oi-not-filter.txt"));
//		
//		pressBack();
//		pressBack();
//	}
	
	// Other possible tests:
	// 		testSend
	// 		testMore
	// 		testKeyboardFilter
}