package com.og.filemanager.dialogs;

import java.io.File;

import com.og.filemanager.files.FileHolder;
import com.og.filemanager.lists.FileListFragment;
import com.og.filemanager.util.MediaScannerUtils;
import com.og.filemanager.util.UIUtils;
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
import android.widget.Toast;

public class RenameDialog extends DialogFragment {
	private FileHolder mFileHolder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mFileHolder = getArguments().getParcelable(FileManagerIntents.EXTRA_DIALOG_FILE_HOLDER);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		LinearLayout view = (LinearLayout) inflater.inflate(com.og.filemanager.R.layout.dialog_text_input, null);
		final EditText v = (EditText) view.findViewById(com.og.filemanager.R.id.foldername);
		v.setText(mFileHolder.getName());

		v.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView text, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO)
					renameTo(text.getText().toString());
				dismiss();
				return true;
			}
		});
		
		return new AlertDialog.Builder(getActivity())
				.setInverseBackgroundForced(UIUtils.shouldDialogInverseBackground(getActivity()))
				.setTitle(com.og.filemanager.R.string.menu_rename)
				.setIcon(mFileHolder.getIcon())
				.setView(view)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								renameTo(v.getText().toString());

							}
						}).create();
	}
	
	private void renameTo(String to){
		boolean res = false;
		
		if(to.length() > 0){
			File from = mFileHolder.getFile();
			
			File dest = new File(mFileHolder.getFile().getParent() + File.separator + to);
			if(!dest.exists()){
				res = mFileHolder.getFile().renameTo(dest);
				((FileListFragment) getTargetFragment()).refresh();

				// Inform media scanner
				MediaScannerUtils.informFileDeleted(getActivity().getApplicationContext(), from);
				MediaScannerUtils.informFileAdded(getActivity().getApplicationContext(), dest);
			}
		}
		
		Toast.makeText(getActivity(), res ? com.og.filemanager.R.string.rename_success : com.og.filemanager.R.string.rename_failure, Toast.LENGTH_SHORT).show();
	}
}