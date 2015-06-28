package com.amb12u.addressbook;


import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NewContactActivity extends Activity {
	private final String TAG = "NewContactActivity";

	private final int IMAGE_INTENT_REQUEST_CODE = 000;
	public static final int IMAGE_MAX_WIDTH = 135;
	public static final int IMAGE_MAX_HEIGHT = 135;

	private long contactId;
	private EditText nameEditText;
	private EditText phoneEditText;
	private EditText emailEditText;
	private ImageView imageView;



	/**
	 * Prompt user to select a specific image from SD card to assign to a contact
	 * @param v
	 */
	public void onClickSelectImage(View v) {
		Log.d(TAG, "onClickSelectImage");
		Intent contactImgaeIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(contactImgaeIntent, IMAGE_INTENT_REQUEST_CODE);
	}

	/**
	 * Result from Gallery activity
	 * Sets the contact photo to a photo selected from gallery
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK){
			Uri targetUri = data.getData();
			Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
				imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT, false));
				imageView.setTag(targetUri.toString());
				Log.d(TAG,targetUri.toString());
			} catch (FileNotFoundException e) {
				loadingPhotoError();
			} catch (NullPointerException e) {
				loadingPhotoError();
			}
		}
	}

	/**
	 * Handler for Cancel Button
	 * Cancels the creation of a new contact, or cancels updating an existing contact details
	 * @param v
	 */
	public void onClickCancel(View v) {
		Log.d(TAG, "onClickCancel");
		setResult(RESULT_CANCELED);
		finish();
	}

	/**
	 * Handler for Confirm button
	 * Confirms the creation of a new contact, or updating an existing contact details
	 * @param v
	 */
	public void onClickConfirm(View v)
	{
		Log.d(TAG, "onClickConfirm");
		if (nameEditText.getText().toString().length() != 0) {	// validation of contact details (contact name field is not empty)
			AsyncTask<Object, Object, Object> saveContactTask = new AsyncTask<Object, Object, Object>(){

				@Override
				protected Object doInBackground(Object... params) {
					if (getIntent().getExtras() == null) {		// adding a new contact 
						addNewContact();
					} else {									// modifying an existing contact 
						updateExistingContact();
					}
					return null;
				}

				@Override
				protected void onPostExecute(Object result) {
					finish();									// close edit contact activity
				}
			};
			saveContactTask.execute((Object)null);
		} else {
			emptyNameFieldError();									//empty contact name (fail of validation)
		}
	}

	/**
	 * Stores a new contact details (name, phone, email, photo) in the database
	 */
	private void addNewContact() {
		Log.d(TAG, "addNewContact");
		
		ContentValues contact = new ContentValues();
		contact.put(MyContactsConnector.CONTACT_NAME, nameEditText.getText().toString());
		contact.put(MyContactsConnector.CONTACT_PHONE, phoneEditText.getText().toString());
		contact.put(MyContactsConnector.CONTACT_EMAIL, emailEditText.getText().toString());
		contact.put(MyContactsConnector.CONTACT_IMAGE, imageView.getTag().toString());
		
		getContentResolver().insert(MyContactsConnector.CONTENT_URI, contact);	
	}

	/**
	 * Updates an existing contact details (name, phone, email, photo) in the database
	 */
	private void updateExistingContact() {
		Log.d(TAG, "updateExistingContact");
		
		ContentValues contact = new ContentValues();
		contact.put(MyContactsConnector.CONTACT_NAME, nameEditText.getText().toString());
		contact.put(MyContactsConnector.CONTACT_PHONE, phoneEditText.getText().toString());
		contact.put(MyContactsConnector.CONTACT_EMAIL, emailEditText.getText().toString());
		contact.put(MyContactsConnector.CONTACT_IMAGE, imageView.getTag().toString());
		
		getContentResolver().update(MyContactsConnector.CONTENT_URI, contact, MyContactsConnector.CONTACT_ID + "=" + contactId, null);	
	}

	/**
	 * Displays a toast
	 * Invoked by <strong>onClickConfirm</strong> to indicate an empty contact name
	 */
	private void emptyNameFieldError() {
		Log.d(TAG, "emptyNameFieldError");
		Toast.makeText(getApplicationContext(), "Missing Name Field", Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Displays a toast
	 * Invoked by <strong>onActivityResult</strong> to indicate an issue loading image
	 */
	private void loadingPhotoError() {
		Log.d(TAG, "loadingPhotoError");
		Toast.makeText(getApplicationContext(), "Problem loading photo", Toast.LENGTH_SHORT).show();
	}

	//----------------- NewContactActivity LifeCycle Methods -------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_contact);

		//access XML views
		nameEditText = (EditText) findViewById(R.id.name_editText);
		phoneEditText = (EditText) findViewById(R.id.phone_editText);
		emailEditText = (EditText) findViewById(R.id.email_editText);
		imageView = (ImageView) findViewById(R.id.new_contact_image);

		Bundle bundle = getIntent().getExtras();
		// determine if this activity has been invoked to modify a contact details
		if (bundle != null) {
			setTitle("Edit Contact");
			//fills fields with previous contact details
			contactId = bundle.getLong("contact_id");
			nameEditText.setText(bundle.getString("contact_name"));
			phoneEditText.setText(bundle.getString("contact_phone"));
			emailEditText.setText(bundle.getString("contact_email"));
			Uri targetUri = Uri.parse(bundle.getString("contact_image"));
			
			Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
				imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, NewContactActivity.IMAGE_MAX_WIDTH, NewContactActivity.IMAGE_MAX_HEIGHT, false));
			} catch (FileNotFoundException e) {
				imageView.setImageResource(R.drawable.default_contact_image);
			}
			imageView.setTag(targetUri.toString());
		} 
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


}
