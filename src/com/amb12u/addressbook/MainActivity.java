// Added Features:
// * Edit contact details
// * Clear all contacts from database
// * Copy to clip board when phone number or email are clicked

package com.amb12u.addressbook;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
public class MainActivity extends ListActivity {
	private final String TAG = "MainActivity";

	public static final String CONTACT_ID = "contact_id";
	private ListView contactListView;
	private CursorAdapter contactAdapter;

	OnItemClickListener viewContactListener = new OnItemClickListener() {

		// allow clicking each contact to view his details
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "onItemClick");
			Intent viewContact = new Intent(MainActivity.this, ViewContactActivity.class);
			viewContact.putExtra(CONTACT_ID, id);
			startActivity(viewContact);	
		}
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case R.id.add_contact:
			newContact();
			break;
		case R.id.delete_all_contacts:
			clearContacts();
			break;
		default:
			Log.w(TAG, "problem in selection");
			break;
		}
		return true;
	}

	/**
	 * Creates the intent to add a new contact
	 * and asks for results (confirm | cancel)
	 */
	private void newContact() {
		Log.d(TAG, "newContact");
		Intent newContactIntent = new Intent(MainActivity.this, NewContactActivity.class);
		startActivity(newContactIntent);
	}
	
	/**
	 * Deletes all contacts from database
	 */
	private void clearContacts() {
		
		//create dialog for user to confirm clearing the contacts list
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.delete_contacts_dialog_title);
		builder.setMessage(R.string.delete_contacts_dialog_message);
		builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() { //start deletion
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AsyncTask<Object, Object, Object> deleteTask = new AsyncTask<Object, Object, Object>(){
					@Override
					protected Object doInBackground(Object... arg0) {
						// delete each row
						getContentResolver().delete(MyContactsConnector.CONTENT_URI, null, null);
						return null;
					}
				};
				deleteTask.execute((Object) null);
			}
		});
		builder.setNegativeButton(R.string.cancel, null); //do nothing for 'cancel'
		builder.show();
	}

	/**
	 * Internal class to load contacts from database into the contacts list 
	 * @author Aiman
	 *
	 */
	private class LoadContactsTask extends AsyncTask<Object, Object, Cursor> {

		@Override
		protected Cursor doInBackground(Object... arg0) {
			Log.d(TAG, "doInBackground");
			return getContentResolver().query(MyContactsConnector.CONTENT_URI, null, null, null, MyContactsConnector.CONTACT_NAME);
		}

		protected void onPostExecute(Cursor result) {
			Log.d(TAG, "onPostExecute");
			contactAdapter.changeCursor(result);
		}
	}


	// --------------------- MainActivity LifeCycle Methods ---------------------------

	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		contactListView = getListView();
		contactListView.setOnItemClickListener(viewContactListener);

		String [] from = {"name"}; //source 
		int [] to = {R.id.contactTextView}; // destination

		contactAdapter = new SimpleCursorAdapter(MainActivity.this, R.layout.contact_list_item , null, from, to);
		setListAdapter(contactAdapter);
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
		// load contacts to screen
		new LoadContactsTask().execute((Object[]) null);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		Cursor cursor = contactAdapter.getCursor();
		if(cursor != null) {
			cursor.deactivate();
		}
		contactAdapter.changeCursor(null);
		super.onStop();
	}
}
