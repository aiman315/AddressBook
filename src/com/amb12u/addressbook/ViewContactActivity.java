package com.amb12u.addressbook;


import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewContactActivity extends Activity {
	private final String TAG = "ViewContactActivity";
	
	private long contactId;
	private TextView nameTextView;
	private TextView phoneTextView;
	private TextView emailTextView;
	private ImageView imageView;

	private OnClickListener copyContent = new OnClickListener() {
		@Override
		public void onClick(View v) {
			onClickCopyViewContent(v);
		}
	};

	/**
	 * Starts NewContactActivity with the purpose to edit a contact
	 * passes existing details to fill fields in the started activity, so they are edited
	 * Invoked by clicking on <strong>Edit Contact</strong> in action bar
	 */
	private void editContact() {
		Intent editContactIntent = new Intent(this, NewContactActivity.class);
		editContactIntent.putExtra("contact_id", contactId);
		editContactIntent.putExtra("contact_name", nameTextView.getText());
		editContactIntent.putExtra("contact_phone", phoneTextView.getText());
		editContactIntent.putExtra("contact_email", emailTextView.getText());
		editContactIntent.putExtra("contact_image", imageView.getTag().toString());
		startActivity(editContactIntent);
	}

	/**
	 * Creates a dialog for user to choose to delete contact from database
	 * Invoked by clicking on <strong>Delete Contact</strong> in action bar
	 */
	private void deleteContact() {
		AlertDialog.Builder builder = new AlertDialog.Builder(ViewContactActivity.this);
		builder.setTitle(R.string.delete_dialog_title);
		builder.setMessage(R.string.delete_dialog_message);
		builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				// a task to delete contact without affecting worker thread
				AsyncTask<Long, Object, Object> deleteTask = new AsyncTask<Long, Object, Object>(){					
					@Override
					protected Object doInBackground(Long... params) {
						getContentResolver().delete(MyContactsConnector.CONTENT_URI, MyContactsConnector.CONTACT_ID + "=" + contactId, null);
						return null;
					}
					@Override
					protected void onPostExecute(Object result) {
						finish();		// close activity upon delete
					}
				};
				deleteTask.execute(new Long[]{contactId});
			}
		});
		
		builder.setNegativeButton(R.string.cancel, null);
		builder.show();
	}

	//inner class to load the selected contact details without interrupting the worker thread
	private class LoadContactTask extends AsyncTask<Long, Object, Cursor> {
		
		//query for data from database
		@Override
		protected Cursor doInBackground(Long... params) {
			String whereClause = MyContactsConnector.CONTACT_ID + "=" + params[0];
			return getContentResolver().query(MyContactsConnector.CONTENT_URI, null, whereClause, null, null);
		}

		// fill activity views with data from database
		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			result.moveToFirst();

			nameTextView.setText(result.getString(MyContactsConnector.NAME_COLUMN));
			phoneTextView.setText(result.getString(MyContactsConnector.PHONE_COLUMN));
			emailTextView.setText(result.getString(MyContactsConnector.EMAIL_COLUMN));
			Uri targetUri = Uri.parse(result.getString(MyContactsConnector.IMAGE_COLUMN));
			
			Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
				imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, NewContactActivity.IMAGE_MAX_WIDTH, NewContactActivity.IMAGE_MAX_HEIGHT, false));
			} catch (FileNotFoundException e) {
				//display error message 
				loadingPhotoError(); //FIXME: contacts with default image will still display error message
				//reset to default image
				imageView.setImageResource(R.drawable.default_contact_image);
			}
			imageView.setTag(targetUri.toString());
			
			setTitle(result.getString(MyContactsConnector.NAME_COLUMN)); //set activity title to contact name
			result.close();
		}
	}	
	
	/**
	 * Displays a toast
	 * Invoked by <strong>onActivityResult</strong> to indicate an issue loading image
	 */
	private void loadingPhotoError() {
		Log.d(TAG, "loadingPhotoError");
		Toast.makeText(getApplicationContext(), "Problem loading photo", Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Copies clicked view text to clip board
	 * Displays a toast to indicate the content of clicked view has been copied
	 * @param clicked view
	 */
	public void onClickCopyViewContent(View v) {
		Log.d(TAG, "onClickCopyViewContent");
		TextView textView = (TextView) v;
	    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
	    ClipData clip = ClipData.newPlainText(null, textView.getText().toString());
	    clipboard.setPrimaryClip(clip);
	    Toast.makeText(getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
	}
	
	//------------------------ ViewContactActivity LifeCycle methods -------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_contact);
		
		nameTextView = (TextView) findViewById(R.id.name_textView);
		phoneTextView = (TextView) findViewById(R.id.phone_textView);
		phoneTextView.setOnClickListener(copyContent);
		emailTextView = (TextView) findViewById(R.id.email_textView);
		emailTextView.setOnClickListener(copyContent);
		imageView = (ImageView) findViewById(R.id.contact_image_imageView);
		
		Bundle bundle = getIntent().getExtras();
		contactId = bundle.getLong("contact_id");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_contact, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		new LoadContactTask().execute(contactId);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.edit_contact:
			Log.d(TAG, "edit contact");
			editContact();
			break;
		case R.id.delete_contact:
			Log.d(TAG, "delete contact");
			deleteContact();
			break;
		default:
			Log.d(TAG, "problem in selection");
			break;
		}
		return true;
	}
}
