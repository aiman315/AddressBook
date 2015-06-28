package com.amb12u.addressbook;

import android.net.Uri;

/**
 * Connector Class, holds constants that are used throughout the application by database
 * @author Aiman
 *
 */
public class MyContactsConnector {

	public static final String AUTHORITY = "com.amb12u.adressbook";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/contacts");

	//Database attributes
	public static final String DATABASE_NAME = "AddressBook";
	public static final int DATABASE_VERSION = 1;
	public static final String TABLE_NAME = "contacts_table";

	//Table attributes
	public static final String CONTACT_ID = "_id";
	public static final String CONTACT_NAME = "name";
	public static final String CONTACT_PHONE = "phone";
	public static final String CONTACT_EMAIL = "email";
	public static final String CONTACT_IMAGE = "image";

	//Table columns indexes
	public static final int NAME_COLUMN = 1;
	public static final int PHONE_COLUMN = 2;
	public static final int EMAIL_COLUMN = 3;
	public static final int IMAGE_COLUMN = 4;
}
