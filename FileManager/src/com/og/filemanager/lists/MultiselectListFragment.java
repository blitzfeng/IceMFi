package com.og.filemanager.lists;

import java.util.ArrayList;

import com.og.filemanager.files.FileHolder;
import com.og.filemanager.util.MenuUtils;
import com.og.filemanager.view.LegacyActionContainer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Dedicated file list fragment, used for multiple selection on platforms older than Honeycomb.
 * OnDestroy sets RESULT_OK on the parent activity so that callers refresh their lists if appropriate.
 * @author George Venios
 */
public class MultiselectListFragment extends FileListFragment {
	private LegacyActionContainer mLegacyActionContainer;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		return inflater.inflate(com.og.filemanager.R.layout.filelist_legacy_multiselect, null);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		super.onViewCreated(view, savedInstanceState);
		
		mAdapter.setItemLayout(com.og.filemanager.R.layout.item_filelist_multiselect);
		
		// Init members
		mLegacyActionContainer =  (LegacyActionContainer) view.findViewById(com.og.filemanager.R.id.action_container);
		mLegacyActionContainer.setMenuResource(com.og.filemanager.R.menu.multiselect);
		mLegacyActionContainer.setOnActionSelectedListener(new LegacyActionContainer.OnActionSelectedListener() {
			@Override
			public void actionSelected(MenuItem item) {
				if(getListView().getCheckItemIds().length == 0){
					Toast.makeText(getActivity(), com.og.filemanager.R.string.no_selection, Toast.LENGTH_SHORT).show();
					return;
				}
				
				ArrayList<FileHolder> fItems = new ArrayList<>();
				
				for(long i : getListView().getCheckItemIds()){
					fItems.add((FileHolder) mAdapter.getItem((int) i));
				}
				
				MenuUtils.handleMultipleSelectionAction(MultiselectListFragment.this, item, fItems, getActivity());
			}
		});
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(com.og.filemanager.R.menu.options_multiselect, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ListView list = getListView();
		
		switch(item.getItemId()){
		case com.og.filemanager.R.id.check_all:
			for(int i = 0; i < mAdapter.getCount(); i++){
				list.setItemChecked(i, true);
			}
			return true;
		case com.og.filemanager.R.id.uncheck_all:
			for(int i = 0; i < mAdapter.getCount(); i++){
				list.setItemChecked(i, false);
			}
			return true;
		default:
			return false;
		}
	}
}