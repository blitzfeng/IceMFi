package com.og.filemanager.test;

import android.support.test.espresso.IdlingResource;

import com.og.filemanager.FileManagerActivity;
import com.og.filemanager.IntentFilterActivity;
import com.og.filemanager.lists.PickFileListFragment;
import com.og.filemanager.lists.SimpleFileListFragment;

public class DirectoryScannerIdlingResource implements IdlingResource {
    private final SimpleFileListFragment fragment;
    private ResourceCallback callback;

    public DirectoryScannerIdlingResource(FileManagerActivity activity) {
        fragment = (SimpleFileListFragment) activity.getSupportFragmentManager().findFragmentByTag(FileManagerActivity.FRAGMENT_TAG);
    }

    public DirectoryScannerIdlingResource(IntentFilterActivity activity) {
        fragment = (SimpleFileListFragment) activity.getSupportFragmentManager().findFragmentByTag(PickFileListFragment.class.getName());
    }

    @Override
    public String getName() {
        return DirectoryScannerIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = !fragment.isLoading();
        if (idle && callback != null) {
            callback.onTransitionToIdle();
        }
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.callback = callback;
        fragment.setResourceCallback(callback);
    }
}
