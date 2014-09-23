package com.groupon.go.provider;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

import com.groupon.go.model.Contact;
import com.groupon.go.model.PhoneNumber;

public class ContactProvider {

	private final static String	LOG_TAG	= "ContactProvider";

	/**
	 * Fetch phone contact.
	 * 
	 * @param cr
	 * @return
	 */
	public static Cursor fetchPhoneContacts(ContentResolver cr) {
		Cursor cursor = null;
		try {
			Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
			String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER,
					ContactsContract.CommonDataKinds.Nickname.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone._ID,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
					ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
					ContactsContract.CommonDataKinds.StructuredName.PREFIX, ContactsContract.CommonDataKinds.StructuredName.SUFFIX };
			cursor = cr.query(uri, projection, null, null, ContactsContract.CommonDataKinds.Nickname.DISPLAY_NAME + " asc");
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
		}
		return cursor;
	}

	/**
	 * For Fetch Phone Contacts.
	 * 
	 * @param cr
	 * @return
	 */
	public static ArrayList<Contact> getAllContacts(ContentResolver cr, Context context) {
		Cursor cursor = null;
		ArrayList<Contact> contactList = null;
		try {
			Uri uri = ContactsContract.Contacts.CONTENT_URI;
			cursor = cr.query(uri, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " asc");
			if (cursor == null) {
				return contactList;
			}
			cursor.moveToFirst();
			contactList = new ArrayList<Contact>();
			while (cursor.moveToNext()) {
				int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
				if (hasPhoneNumber == 0) {
					continue;
				}
				int contactId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
				ArrayList<PhoneNumber> phones = ContactProvider.getAllPhoneNos(cr, Integer.toString(contactId));
				if (phones == null || phones.isEmpty()) {
					continue;
				}
				Contact contact = new Contact();
				contact.setPhoneNumbers(phones);
				contact.setDisplayName(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.DISPLAY_NAME)));
				// contact.setContactId(contactId);
				// contact.setRowId(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)));
				contact.setContactImage(queryContactImage(context, contactId));
				contactList.add(contact);
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return contactList;
	}

	/**
	 * Get all phone numbers from the Native contact details.
	 * 
	 * @param cr
	 * @param id
	 * @return
	 */
	public static ArrayList<PhoneNumber> getAllPhoneNos(ContentResolver cr, String id) {
		Cursor cursor = null;
		ArrayList<PhoneNumber> phones = new ArrayList<PhoneNumber>();
		try {
			cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[] { id }, null);
			if (cursor == null) {
				return phones;
			}
			while (cursor.moveToNext()) {
				String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
				PhoneNumber phoneNumber = new PhoneNumber();
				phoneNumber.setMobile_no(phone);
				phones.add(phoneNumber);
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return phones;
	}

	/**
	 * For getting the contact image form Native contact details.
	 * 
	 * @param context
	 * @param contactId
	 * @return {@link Bitmap}
	 */
	public static Bitmap queryContactImage(Context context, int contactId) {
		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);

		// String[] projection = new String[] {
		// ContactsContract.CommonDataKinds.Photo.PHOTO };
		/*
		 * Cursor c = context.getContentResolver().query(
		 * ContactsContract.Data.CONTENT_URI, projection,
		 * ContactsContract.Data._ID + "=?", new String[] {
		 * Integer.toString(contactId) }, null);
		 */
		Cursor cursor = context.getContentResolver().query(photoUri, new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO }, null, null, null);
		if (cursor == null) {
			return null;
		}
		try {
			if (cursor.moveToFirst()) {
				byte[] data = cursor.getBlob(0);
				if (data != null) {
					return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}
}