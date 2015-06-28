package com.amb12u.addressbook;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MyContactsProvider extends ContentProvider {
	private final String TAG = "MyContactsProvider";
	private SQLiteDatabase contactsDB;

	//To manage URI user requests
	private static final int MULTIPLE_CONTACTS = 1;
	private static final int SINGLE_CONTACT = 2;
	private static final int ID_PATH_SEGMENT = 1;

	//URI matcher for two possible requests :
	// 1: retrieving a single contact details
	// 2: retrieving multiple contacts details
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(MyContactsConnector.AUTHORITY, "contacts", MULTIPLE_CONTACTS);
		uriMatcher.addURI(MyContactsConnector.AUTHORITY, "contacts/#", SINGLE_CONTACT);
	}

	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate");
		Context context = getContext();
		DatabaseOpenHelper dbHelper = new DatabaseOpenHelper(context, MyContactsConnector.DATABASE_NAME, null, MyContactsConnector.DATABASE_VERSION);
		contactsDB = dbHelper.getWritableDatabase();
		return ( contactsDB == null) ? false : true;
	}

	@Override
	public String getType(Uri uri) {
		Log.d(TAG, "getType");
		switch (uriMatcher.match(uri)) {
		case SINGLE_CONTACT:
			//return one authority
			return "vnd.android.cursor.item/contact/#";
		case MULTIPLE_CONTACTS:
			//return multiple authority
			return "vnd.android.cursor.dir/contacts";
		default: 
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "query");
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(MyContactsConnector.TABLE_NAME);

		//check query type: Multiple Contacts OR Single Contact
		switch (uriMatcher.match(uri)) {
		case MULTIPLE_CONTACTS: //TODO
			break;
		case SINGLE_CONTACT:
			queryBuilder.appendWhere(MyContactsConnector.CONTACT_ID + "=" + uri.getPathSegments().get(ID_PATH_SEGMENT));
			break;
		default: throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		//set up for order of output rows  
		//if no specific column is provided, sort in ascending order of Contact IDs
		//otherwise, sort in ascending order of provided column
		String orderBy = (TextUtils.isEmpty(sortOrder))? MyContactsConnector.CONTACT_ID : sortOrder;

		//get the results with the new query settings
		Cursor cursor = queryBuilder.query(contactsDB, projection, selection, selectionArgs, null, null, orderBy);

		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert");
		long rowID = contactsDB.insert(MyContactsConnector.TABLE_NAME, null, values);
		if (rowID > 0) {
			Uri newUri = ContentUris.withAppendedId(MyContactsConnector.CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return newUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		Log.d(TAG, "update");
		int rowsAffected = 0;

		//check query type: Multiple Contacts OR Single Contact
		switch (uriMatcher.match(uri)) {
		case MULTIPLE_CONTACTS:
			rowsAffected = contactsDB.update(MyContactsConnector.TABLE_NAME, values, selection, selectionArgs);
			break;
		case SINGLE_CONTACT:
			String segment = uri.getPathSegments().get(ID_PATH_SEGMENT);
			String whereClause = MyContactsConnector.CONTACT_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
			rowsAffected = contactsDB.update(MyContactsConnector.TABLE_NAME, values, whereClause, selectionArgs);
			break;
		default: 
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "delete");
		int rowsAffected = 0;

		//check query type: Multiple Contacts OR Single Contact
		switch (uriMatcher.match(uri)) {
		case MULTIPLE_CONTACTS:
			rowsAffected = contactsDB.delete(MyContactsConnector.TABLE_NAME, selection, selectionArgs);
			break;
		case SINGLE_CONTACT:
			String segment = uri.getPathSegments().get(1);
			String whereClause = MyContactsConnector.CONTACT_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
			rowsAffected = contactsDB.delete(MyContactsConnector.TABLE_NAME, whereClause, selectionArgs);
			break;
		default: throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}


	private class DatabaseOpenHelper extends SQLiteOpenHelper{
		private final String TAG = "DatabaseOpenHelper";
		public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(this.TAG, "onCreate");
			String createTable = 
					"create table "+ MyContactsConnector.TABLE_NAME + "(" +
							MyContactsConnector.CONTACT_ID + " integer primary key autoincrement, " +
							MyContactsConnector.CONTACT_NAME + " TEXT, " +
							MyContactsConnector.CONTACT_PHONE + " TEXT, " +
							MyContactsConnector.CONTACT_EMAIL + " TEXT, " +
							MyContactsConnector.CONTACT_IMAGE + " TEXT); ";
			db.execSQL(createTable);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(this.TAG, "onUpgrade");
			db.execSQL("DROP TABLE IF EXISTS " + MyContactsConnector.TABLE_NAME);
			onCreate(db);
		}
	}

}
