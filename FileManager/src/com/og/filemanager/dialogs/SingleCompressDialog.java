package com.og.filemanager.dialogs;

import java.io.File;

import com.og.filemanager.files.FileHolder;
import com.og.filemanager.lists.FileListFragment;
import com.og.filemanager.util.CompressManager;
import com.og.filemanager.util.MediaScannerUtils;
import com.og.intents.FileManagerIntents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SingleCompressDialog extends DialogFragment implements OverwriteFileDialog.Overwritable {
	private FileHolder mFileHolder;
	private CompressManager mCompressManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFileHolder = getArguments().getParcelable(FileManagerIntents.EXTRA_DIALOG_FILE_HOLDER);
		
		mCompressManager = new CompressManager(getActivity());
		mCompressManager.setOnCompressFinishedListener(new CompressManager.OnCompressFinishedListener() {
			
			@Override
			public void compressFinished() {
				((FileListFragment) SingleCompressDialog.this.getTargetFragment()).refresh();

				MediaScannerUtils.informFileAdded(getTargetFragment().getActivity().getApplicationContext(), tbcreated);
			}
		});
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		LinearLayout view = (LinearLayout) inflater.inflate(com.og.filemanager.R.layout.dialog_text_input, null);
		final EditText v = (EditText) view.findViewById(com.og.filemanager.R.id.foldername);
		v.setHint(com.og.filemanager.R.string.compressed_file_name);
		
		v.setOnEditorActionListener(new TextView.OnEditorActionListener(){
			public boolean onEditorAction(TextView text, int actionId, KeyEvent event) {
				   if (actionId == EditorInfo.IME_ACTION_GO)
					   compress(v.getText().toString());
				   dismiss();
				   return true;
				}
		});
		
		return new AlertDialog.Builder(getActivity())
				.setTitle(com.og.filemanager.R.string.menu_compress)
				.setView(view)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						compress(v.getText().toString());
					}
				}).setNegativeButton(android.R.string.cancel, null).create();
	}
	
	private void compress(final String zipname){
		tbcreated = new File(mFileHolder.getFile().getParent() + File.separator + zipname + ".zip");
		if (tbcreated.exists()) {
			this.zipname = zipname;
			OverwriteFileDialog dialog = new OverwriteFileDialog();
			dialog.setTargetFragment(this, 0);
			dialog.show(getFragmentManager(), "OverwriteFileDialog");
		} else {
			mCompressManager.compress(mFileHolder, tbcreated.getName());
		}
	}

	private File tbcreated;
	private String zipname;
	
	@Override
	public void overwrite() {
		tbcreated.delete();
		compress(zipname);
	}
}