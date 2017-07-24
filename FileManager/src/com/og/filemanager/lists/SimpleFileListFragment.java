package com.og.filemanager.lists;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.og.filemanager.compatibility.FileMultiChoiceModeHelper;
import com.og.filemanager.dialogs.CreateDirectoryDialog;
import com.og.filemanager.files.FileHolder;
import com.og.filemanager.util.CopyHelper;
import com.og.filemanager.util.FileUtils;
import com.og.filemanager.util.MenuUtils;
import com.og.filemanager.view.PathBar;
import com.og.filemanager.FileManagerApplication;
import com.og.filemanager.PreferenceActivity;
import com.og.filemanager.compatibility.ActionbarRefreshHelper;
import com.og.intents.FileManagerIntents;

import net.youmi.android.nm.sp.SpotListener;
import net.youmi.android.nm.sp.SpotManager;

import java.io.File;
import java.io.IOException;

/**
 * A file list fragment that supports context menu and CAB selection.
 *
 * @author George Venios
 */
public class SimpleFileListFragment extends FileListFragment {
    private static final String INSTANCE_STATE_PATHBAR_MODE = "pathbar_mode";

    protected static final int REQUEST_CODE_MULTISELECT = 2;

    private PathBar mPathBar;
    private boolean mActionsEnabled = true;

    private int mSingleSelectionMenu = com.og.filemanager.R.menu.context;
    private int mMultiSelectionMenu = com.og.filemanager.R.menu.multiselect;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(com.og.filemanager.R.layout.filelist_browse, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Pathbar init.
        mPathBar = (PathBar) view.findViewById(com.og.filemanager.R.id.pathbar);
        // Handle mPath differently if we restore state or just initially create the view.
        if (savedInstanceState == null)
            mPathBar.setInitialDirectory(getPath());
        else
            mPathBar.cd(getPath());
        mPathBar.setOnDirectoryChangedListener(new PathBar.OnDirectoryChangedListener() {

            @Override
            public void directoryChanged(File newCurrentDir) {
                Context activity = getActivity();
                if (activity == null) {
                    return;
                }
                open(new FileHolder(newCurrentDir, activity));
            }
        });
        if (savedInstanceState != null && savedInstanceState.getBoolean(INSTANCE_STATE_PATHBAR_MODE))
            mPathBar.switchToManualInput();
        // Removed else clause as the other mode is the default. It seems faster this way on Nexus S.

        initContextualActions();

        SpotManager.getInstance(getActivity()).showSpot(getActivity(),
                new SpotListener() {
                    @Override
                    public void onShowSuccess() {
                        System.out.println("onShowSuccess");
                    }

                    @Override
                    public void onShowFailed(int i) {
                        System.out.println("onShowFailed");
                    }

                    @Override
                    public void onSpotClosed() {
                        System.out.println("onSpotClosed");
                    }

                    @Override
                    public void onSpotClicked(boolean b) {
                        System.out.println("onSpotClosed");
                    }
                });
    }

    /**
     * Override this to handle initialization of list item long clicks.
     */
    void initContextualActions() {
        if (mActionsEnabled) {
            if (VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                registerForContextMenu(getListView());
            } else {
                FileMultiChoiceModeHelper multiChoiceModeHelper = new FileMultiChoiceModeHelper(mSingleSelectionMenu, mMultiSelectionMenu);
                multiChoiceModeHelper.setListView(getListView());
                multiChoiceModeHelper.setPathBar(mPathBar);
                multiChoiceModeHelper.setContext(this);
                getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            }
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenuInfo menuInfo) {
        MenuInflater inflater = new MenuInflater(getActivity());

        // Obtain context menu info
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            e.printStackTrace();
            return;
        }

        MenuUtils.fillContextMenu((FileHolder) mAdapter.getItem(info.position), menu, mSingleSelectionMenu, inflater, getActivity());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        FileHolder fh = (FileHolder) mAdapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
        return MenuUtils.handleSingleSelectionAction(this, item, fh, getActivity());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FileHolder item = (FileHolder) mAdapter.getItem(position);

        openInformingPathBar(item);
    }

    /**
     * Use this to open files and folders using this fragment. Appropriately handles pathbar updates.
     *
     * @param item The dir/file to open.
     */
    public void openInformingPathBar(FileHolder item) {
        if (mPathBar == null)
            open(item);
        else
            mPathBar.cd(item.getFile());
    }

    /**
     * Point this Fragment to show the contents of the passed file.
     *
     * @param f If same as current, does nothing.
     */
    private void open(FileHolder f) {
        if (!f.getFile().exists())
            return;

        if (f.getFile().isDirectory()) {
            openDir(f);
        } else if (f.getFile().isFile()) {
            openFile(f);
        }
    }

    private void openFile(FileHolder fileholder) {
        FileUtils.openFile(fileholder, getActivity());
    }

    /**
     * Attempts to open a directory for browsing.
     * Override this to handle folder click behavior.
     *
     * @param fileholder The holder of the directory to open.
     */
    protected void openDir(FileHolder fileholder) {
        // Avoid unnecessary attempts to load.
        if (fileholder.getFile().getAbsolutePath().equals(getPath()))
            return;

        setPath(fileholder.getFile());
        refresh();
    }

    protected void setLongClickMenus(int singleSelectionResource, int multiSelectionResource) {
        mSingleSelectionMenu = singleSelectionResource;
        mMultiSelectionMenu = multiSelectionResource;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(com.og.filemanager.R.menu.simple_file_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // We only know about ".nomedia" once scanning is finished.
        boolean showMediaScanMenuItem = PreferenceActivity.getMediaScanFromPreference(getActivity());
        if (!mScanner.isRunning() && showMediaScanMenuItem) {
            menu.findItem(com.og.filemanager.R.id.menu_media_scan_include).setVisible(mScanner.getNoMedia());
            menu.findItem(com.og.filemanager.R.id.menu_media_scan_exclude).setVisible(!mScanner.getNoMedia());
        } else {
            menu.findItem(com.og.filemanager.R.id.menu_media_scan_include).setVisible(false);
            menu.findItem(com.og.filemanager.R.id.menu_media_scan_exclude).setVisible(false);
        }

        if (((FileManagerApplication) getActivity().getApplication()).getCopyHelper().canPaste()) {
            menu.findItem(com.og.filemanager.R.id.menu_paste).setVisible(true);
        } else {
            menu.findItem(com.og.filemanager.R.id.menu_paste).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case com.og.filemanager.R.id.menu_create_folder:
                CreateDirectoryDialog dialog = new CreateDirectoryDialog();
                dialog.setTargetFragment(this, 0);
                Bundle args = new Bundle();
                args.putString(FileManagerIntents.EXTRA_DIR_PATH, getPath());
                dialog.setArguments(args);
                dialog.show(getActivity().getSupportFragmentManager(), CreateDirectoryDialog.class.getName());
                return true;

            case com.og.filemanager.R.id.menu_media_scan_include:
                includeInMediaScan();
                return true;

            case com.og.filemanager.R.id.menu_media_scan_exclude:
                excludeFromMediaScan();
                return true;

            case com.og.filemanager.R.id.menu_paste:
                if (((FileManagerApplication) getActivity().getApplication()).getCopyHelper().canPaste())
                    ((FileManagerApplication) getActivity().getApplication()).getCopyHelper().paste(new File(getPath()), new CopyHelper.OnOperationFinishedListener() {
                        @Override
                        public void operationFinished(boolean success) {
                            refresh();

                            // Refresh options menu
                            if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
                                ActionbarRefreshHelper.activity_invalidateOptionsMenu(getActivity());
                        }
                    });
                else
                    Toast.makeText(getActivity(), com.og.filemanager.R.string.nothing_to_paste, Toast.LENGTH_LONG).show();
                return true;

            case com.og.filemanager.R.id.menu_multiselect:
                Intent intent = new Intent(FileManagerIntents.ACTION_MULTI_SELECT);
                intent.putExtra(FileManagerIntents.EXTRA_DIR_PATH, getPath());
                startActivityForResult(intent, REQUEST_CODE_MULTISELECT);
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Automatically refresh to display possible changes done through the multiselect fragment.
        if (requestCode == REQUEST_CODE_MULTISELECT)
            refresh();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void includeInMediaScan() {
        // Delete the .nomedia file.
        File file = FileUtils.getFile(mPathBar.getCurrentDirectory(),
                FileUtils.NOMEDIA_FILE_NAME);
        if (file.delete()) {
            Toast.makeText(getActivity(),
                    getString(com.og.filemanager.R.string.media_scan_included), Toast.LENGTH_LONG)
                    .show();
        } else {
            // That didn't work.
            Toast.makeText(getActivity(), getString(com.og.filemanager.R.string.error_generic),
                    Toast.LENGTH_LONG).show();
        }
        refresh();
    }

    private void excludeFromMediaScan() {
        // Create the .nomedia file.
        File file = FileUtils.getFile(mPathBar.getCurrentDirectory(),
                FileUtils.NOMEDIA_FILE_NAME);
        try {
            if (file.createNewFile()) {
                Toast.makeText(getActivity(),
                        getString(com.og.filemanager.R.string.media_scan_excluded),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(),
                        getString(com.og.filemanager.R.string.error_media_scan), Toast.LENGTH_LONG)
                        .show();
            }
        } catch (IOException e) {
            // That didn't work.
            Toast.makeText(getActivity(),
                    getString(com.og.filemanager.R.string.error_generic) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        refresh();
    }

    public void browseToHome() {
        mPathBar.cd(mPathBar.getInitialDirectory());
    }

    public boolean pressBack() {
        return mPathBar.pressBack();
    }

    /**
     * Set whether to show menu and selection actions. Must be set before OnViewCreated is called.
     *
     * @param enabled
     */
    public void setActionsEnabled(boolean enabled) {
        mActionsEnabled = enabled;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(INSTANCE_STATE_PATHBAR_MODE, mPathBar.getMode() == PathBar.Mode.MANUAL_INPUT);
    }
}