package com.og.filemanager.lists;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.og.filemanager.FileHolderListAdapter;
import com.og.filemanager.FileManagerApplication;
import com.og.filemanager.compatibility.ActionbarRefreshHelper;
import com.og.filemanager.files.DirectoryContents;
import com.og.filemanager.files.DirectoryScanner;
import com.og.filemanager.files.FileHolder;
import com.og.filemanager.util.CopyHelper;
import com.og.filemanager.util.MimeTypes;
import com.og.intents.FileManagerIntents;

import java.io.File;
import java.util.ArrayList;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v4.content.ContextCompat.checkSelfPermission;


/**
 * A {@link ListFragment} that displays the contents of a directory.
 * <p>
 * Clicks do nothing.
 * </p>
 * <p>
 * Refreshes OnSharedPreferenceChange
 * </p>
 *
 * @author George Venios
 */
public abstract class FileListFragment extends ListFragment {
    private static final String INSTANCE_STATE_PATH = "path";
    private static final String INSTANCE_STATE_FILES = "files";
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    File mPreviousDirectory = null;

    // Not an anonymous inner class because of:
    // http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently
    private OnSharedPreferenceChangeListener preferenceListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            // We only care for list-altering preferences. This could be
            // dangerous though,
            // as later contributors might not see this, and have their settings
            // not work in realtime.
            // Therefore this is commented out, since it's not likely the
            // refresh is THAT heavy.
            // *****************
            // if (PreferenceActivity.PREFS_DISPLAYHIDDENFILES.equals(key)
            // || PreferenceActivity.PREFS_SORTBY.equals(key)
            // || PreferenceActivity.PREFS_ASCENDING.equals(key))

            // Prevent NullPointerException caused from this getting called
            // after we have finish()ed the activity.
            if (getActivity() != null)
                refresh();
        }
    };

    protected FileHolderListAdapter mAdapter;
    protected DirectoryScanner mScanner;
    protected ArrayList<FileHolder> mFiles = new ArrayList<>();
    private String mPath;
    private String mFilename;

    private ViewFlipper mFlipper;
    private File mCurrentDirectory;
    private View mClipboardInfo;
    private TextView mClipboardContent;
    private TextView mClipboardAction;
    private IdlingResource.ResourceCallback resourceCallback;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(INSTANCE_STATE_PATH, mPath);
        outState.putParcelableArrayList(INSTANCE_STATE_FILES, mFiles);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(com.og.filemanager.R.layout.filelist, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Set auto refresh on preference change.
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(preferenceListener);

        // Set list properties
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    startUpdatingFileIcons();
                } else
                    stopUpdatingFileIcons();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });
        getListView().requestFocus();
        getListView().requestFocusFromTouch();

        // Init flipper
        mFlipper = (ViewFlipper) view.findViewById(com.og.filemanager.R.id.flipper);
        mClipboardInfo = view.findViewById(com.og.filemanager.R.id.clipboard_info);
        mClipboardContent = (TextView) view.findViewById(com.og.filemanager.R.id.clipboard_content);
        mClipboardAction = (TextView) view.findViewById(com.og.filemanager.R.id.clipboard_action);
        mClipboardAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FileManagerApplication) getActivity().getApplication()).getCopyHelper().clear();
                updateClipboardInfo();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    ActionbarRefreshHelper.activity_invalidateOptionsMenu(getActivity());
            }
        });

        // Get arguments
        if (savedInstanceState == null) {
            mPath = getArguments().getString(FileManagerIntents.EXTRA_DIR_PATH);
            mFilename = getArguments().getString(
                    FileManagerIntents.EXTRA_FILENAME);
        } else {
            mPath = savedInstanceState.getString(INSTANCE_STATE_PATH);
            mFiles = savedInstanceState
                    .getParcelableArrayList(INSTANCE_STATE_FILES);
        }
        pathCheckAndFix();
        renewScanner();
        mAdapter = new FileHolderListAdapter(mFiles, getActivity());

        setListAdapter(mAdapter);
        if (hasPermissions()) {
            mScanner.start();
        } else {
            requestPermissions();
        }
    }

    private void startUpdatingFileIcons() {
        mAdapter.startProcessingThumbnailLoaderQueue();
    }

    private void stopUpdatingFileIcons() {
        mAdapter.stopProcessingThumbnailLoaderQueue();
    }

    public boolean isLoading() {
        return mFlipper.getDisplayedChild() == 0;
    }

    @Override
    public void onDestroy() {
        mScanner.cancel();
        super.onDestroy();
    }

    /**
     * Reloads {@link #mPath}'s contents.
     */
    public void refresh() {
        if (hasPermissions()) {
            // Cancel and GC previous scanner so that it doesn't load on top of the
            // new list.
            // Race condition seen if a long list is requested, and a short list is
            // requested before the long one loads.
            mScanner.cancel();
            mScanner = null;

            // Indicate loading and start scanning.
            setLoading(true);
            renewScanner().start();
        } else {
            requestPermissions();
        }
    }


    private boolean hasPermissions() {
        return checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        setLoading(true);
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
    }

    /**
     * Switch to permission request mode.
     */
    private void showPermissionDenied() {
        setLoading(false);
        Toast.makeText(getActivity(), com.og.filemanager.R.string.details_permissions, Toast.LENGTH_SHORT).show();
    }

    /**
     * Make the UI indicate loading.
     */
    private void setLoading(boolean show) {
        mFlipper.setDisplayedChild(show ? 0 : 1);
        onLoadingChanged(show);
    }

    public void setResourceCallback(IdlingResource.ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

    protected void selectInList(File selectFile) {
        String filename = selectFile.getName();

        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            FileHolder it = (FileHolder) mAdapter.getItem(i);
            if (it.getName().equals(filename)) {
                getListView().setSelection(i);
                break;
            }
        }
    }

    /**
     * Recreates the {@link #mScanner} using the previously set arguments and
     * {@link #mPath}.
     *
     * @return {@link #mScanner} for convenience.
     */
    protected DirectoryScanner renewScanner() {
        String filetypeFilter = getArguments().getString(
                FileManagerIntents.EXTRA_FILTER_FILETYPE);
        String mimetypeFilter = getArguments().getString(
                FileManagerIntents.EXTRA_FILTER_MIMETYPE);
        boolean writeableOnly = getArguments().getBoolean(
                FileManagerIntents.EXTRA_WRITEABLE_ONLY);
        boolean directoriesOnly = getArguments().getBoolean(
                FileManagerIntents.EXTRA_DIRECTORIES_ONLY);

        mScanner = new DirectoryScanner(new File(mPath), getActivity(),
                new FileListMessageHandler(),
                MimeTypes.getInstance(),
                filetypeFilter == null ? "" : filetypeFilter,
                mimetypeFilter == null ? "" : mimetypeFilter, writeableOnly,
                directoriesOnly);
        return mScanner;
    }

    private class FileListMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case DirectoryScanner.MESSAGE_SHOW_DIRECTORY_CONTENTS:
                    if (getActivity() == null) {
                        return;
                    }

                    DirectoryContents c = (DirectoryContents) msg.obj;
                    mFiles.clear();
                    mFiles.addAll(c.listSdCard);
                    mFiles.addAll(c.listDir);
                    mFiles.addAll(c.listFile);

                    mAdapter.notifyDataSetChanged();


                    if (mPreviousDirectory != null) {
                        selectInList(mPreviousDirectory);
                    } else {
                        // Reset list position.
                        if (!mFiles.isEmpty())
                            getListView().setSelection(0);
                    }
                    setLoading(false);
                    updateClipboardInfo();
                    if (resourceCallback != null) {
                        resourceCallback.onTransitionToIdle();
                    }
                    break;
                case DirectoryScanner.MESSAGE_SET_PROGRESS:
                    // ignore
                    break;
            }
        }
    }

    public void updateClipboardInfo() {
        CopyHelper copyHelper = ((FileManagerApplication) getActivity().getApplication()).getCopyHelper();
        if (copyHelper.canPaste()) {
            mClipboardInfo.setVisibility(View.VISIBLE);
            int count = copyHelper.getItemsCount();
            if (CopyHelper.Operation.COPY.equals(copyHelper.getOperationType())) {
                mClipboardContent.setText(getResources().getQuantityString(com.og.filemanager.R.plurals.clipboard_info_items_to_copy, count, count));
                mClipboardAction.setText(getString(com.og.filemanager.R.string.clipboard_dismiss));
            } else if (CopyHelper.Operation.CUT.equals(copyHelper.getOperationType())) {
                mClipboardContent.setText(getResources().getQuantityString(com.og.filemanager.R.plurals.clipboard_info_items_to_move, count, count));
                mClipboardAction.setText(getString(com.og.filemanager.R.string.clipboard_undo));
            }
        } else {
            mClipboardInfo.setVisibility(View.GONE);
        }
    }

    /**
     * Used to inform subclasses about loading state changing. Can be used to
     * make the ui indicate the loading state of the fragment.
     *
     * @param loading If the list started or stopped loading.
     */
    protected void onLoadingChanged(boolean loading) {
    }

    /**
     * @return The currently displayed directory's absolute path.
     */
    public final String getPath() {
        return mPath;
    }

    /**
     * This will be ignored if path doesn't pass check as valid.
     *
     * @param dir The path to set.
     */
    public final void setPath(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            mPreviousDirectory = mCurrentDirectory;
            mCurrentDirectory = dir;
            mPath = dir.getAbsolutePath();
        }
    }

    private void pathCheckAndFix() {
        File file = new File(mPath);
        // Sanity check that the path (coming from extras_dir_path) is indeed a
        // directory
        if (!file.isDirectory() && file.getParentFile() != null) {
            // remember the filename for picking.
            mFilename = file.getName();
            mPath = file.getParentFile().getAbsolutePath();
        }
    }

    public String getFilename() {
        return mFilename;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_STORAGE_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    refresh();
                } else {
                    showPermissionDenied();
                }
                break;
        }
    }
}
