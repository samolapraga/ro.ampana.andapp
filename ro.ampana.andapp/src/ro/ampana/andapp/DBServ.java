/**
 * Application name : Recipes App
 * Author			: Taufan Erfiyanto
 * Date				: March 2012
 */
package ro.ampana.andapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import ro.ampana.andapp.ServXmlParser.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class DBServ {//extends SQLiteOpenHelper {
	private static final String TAG = "ServiceDatabase";
	// The columns we'll include in the service table
	public static final String COL_WPID = "WPID";
	public static final String COL_NAME = "NAME";
	public static final String COL_ADDR = "ADDRESS";
	public static final String COL_LAT = "LAT";
	public static final String COL_LNG = "LNG";
	public static final String COL_DESC = "DESCRIPTION";
	public static final String COL_HRS = "HOURS";
	public static final String COL_PHONE = "PHONE";
	public static final String COL_ZONE = "ZONE";
	public static final String COL_IMG = "IMAGE";
	public static final String COL_URL = "URL";
	private static final String DATABASE_NAME = "SERVICE";
	private static final String FTS_VIRTUAL_TABLE = "SERV";
	private static final int DATABASE_VERSION = 1;

	/*
	 * Defines a handle to the database helper object. The MainDatabaseHelper
	 * class is defined in a following snippet.
	 */

	public final DatabaseOpenHelper mDatabaseOpenHelper;

	public DBServ(Context context) {
		//super(context, DATABASE_NAME, null, DATABASE_VERSION);
//		mHelperContext = context;
		mDatabaseOpenHelper = new DatabaseOpenHelper(context);
	}

	public static class DatabaseOpenHelper extends SQLiteOpenHelper {
	private final Context mHelperContext;
	private SQLiteDatabase mDatabase;
	private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE "
			+ FTS_VIRTUAL_TABLE + " USING fts3 (" + COL_WPID + ", " + COL_NAME
			+ ", " + COL_ADDR + ", " + COL_LAT + ", " + COL_LNG + ", "
			+ COL_DESC + ", " + COL_HRS + ", " + COL_PHONE + ", " + COL_ZONE
			+ ", " + COL_IMG + ", " + COL_URL + ")";

	 DatabaseOpenHelper(Context context) {
		 super(context, DATABASE_NAME, null, DATABASE_VERSION);
	 	mHelperContext = context;
	 }

	@Override
	public void onCreate(SQLiteDatabase db) {
		mDatabase = db;
		mDatabase.execSQL(FTS_TABLE_CREATE);
		loadService();
	}
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
		onCreate(db);
	}

	private void loadService() {
		new Thread(new Runnable() {
			public void run() {
				try {
					loadWords();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
	}

	private void loadWords() throws IOException {
		final Resources resources = mHelperContext.getResources();
		InputStream inputStream = resources.openRawResource(R.raw.php);

		// Instantiate the parser
		ServXmlParser ServXmlParser = new ServXmlParser();
		List<Entry> entries = null;

		try {
			entries = ServXmlParser.parse(inputStream);
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		
		// ServXmlParser returns a List (called "entries") of Entry objects.
		// Each Entry object represents a single post in the XML feed.
		// Each entry is displayed in the UI as a link that optionally includes
		// a text summary.
		for (Entry entry : entries) {
			long id = addServ(entry.wpid, entry.name, entry.desc);
			if (id < 0) {
				Log.e(TAG, "unable to add serv: " + entry.wpid + "-"
						+ entry.name);
			}
		}

	}

	public long addServ(Long wpid, String name, String desc) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(COL_WPID, wpid);
		initialValues.put(COL_NAME, name);
		initialValues.put(COL_DESC, desc);
		return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
	}



	
	public void openDataBase() throws SQLException {
		String myPath = DATABASE_NAME;
		try{
		mDatabase = SQLiteDatabase.openOrCreateDatabase(myPath, null);
		} catch (SQLException e) {
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}
		
	}


	/** this code is used to get all data from database */
	public ArrayList<ArrayList<Object>> getAllData(String ServKeyword) {
		ArrayList<ArrayList<Object>> dataArrays = new ArrayList<ArrayList<Object>>();

		Cursor cursor = null;

		if (ServKeyword.equals("")) {
			try {
				cursor = mDatabase.query(FTS_VIRTUAL_TABLE, new String[] {
						COL_WPID, COL_NAME, COL_DESC }, null, null, null, null,
						null);
				cursor.moveToFirst();

				if (!cursor.isAfterLast()) {
					do {
						ArrayList<Object> dataList = new ArrayList<Object>();

						dataList.add(cursor.getLong(0));
						dataList.add(cursor.getString(1));
						dataList.add(cursor.getString(2));

						dataArrays.add(dataList);
					}

					while (cursor.moveToNext());
				}
				cursor.close();
			} catch (SQLException e) {
				Log.e("DB Error", e.toString());
				e.printStackTrace();
			}
		} else {
			try {
				cursor = mDatabase.query(FTS_VIRTUAL_TABLE, new String[] {
						COL_WPID, COL_NAME, COL_DESC }, COL_NAME + " LIKE '%"
						+ ServKeyword + "%'", null, null, null, null);
				cursor.moveToFirst();

				if (!cursor.isAfterLast()) {
					do {
						ArrayList<Object> dataList = new ArrayList<Object>();

						dataList.add(cursor.getLong(0));
						dataList.add(cursor.getString(1));
						dataList.add(cursor.getString(2));

						dataArrays.add(dataList);
					}

					while (cursor.moveToNext());
				}
				cursor.close();
			} catch (SQLException e) {
				Log.e("DB Error", e.toString());
				e.printStackTrace();
			}
		}
		return dataArrays;
	}

	/** this code is used to get data from database base on id value */
	public ArrayList<Object> getDetail(long id) {

		ArrayList<Object> rowArray = new ArrayList<Object>();
		Cursor cursor;

		try {
			cursor = mDatabase.query(FTS_VIRTUAL_TABLE, new String[] {
					COL_NAME, COL_ADDR, COL_LAT, COL_LNG, COL_DESC, COL_HRS,
					COL_PHONE, COL_ZONE, COL_IMG, COL_URL }, COL_WPID + "="
					+ id, null, null, null, null, null);

			cursor.moveToFirst();

			if (!cursor.isAfterLast()) {
				do {
					rowArray.add(cursor.getString(0));
					rowArray.add(cursor.getString(1));
					rowArray.add(cursor.getString(2));
					rowArray.add(cursor.getString(3));
					rowArray.add(cursor.getString(4));
					rowArray.add(cursor.getString(5));
					rowArray.add(cursor.getString(6));
					rowArray.add(cursor.getString(7));
					rowArray.add(cursor.getString(8));
					rowArray.add(cursor.getString(9));
				} while (cursor.moveToNext());
			}

			cursor.close();
		} catch (SQLException e) {
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}

		return rowArray;
	}

	public void createDataBase() {

		boolean dbExist = checkDataBase();
		SQLiteDatabase db_Write = null;

		if (dbExist) {
			// do nothing - database already exist
			deleteDataBase();
			try {
				copyDataBase();
			} catch (IOException e) {
				// throw new Error("Error copying database");
			}
		} else {
			db_Write = this.getWritableDatabase();
			// db_Write.execSQL(FTS_TABLE_CREATE);
			try {
				loadWords();
			} catch (IOException e) {
				// throw new Error("Error copying database");
			}
			db_Write.close();


		}

	}

	private void deleteDataBase() {
		File dbFile = new File(DATABASE_NAME);

		dbFile.delete();
	}

	private boolean checkDataBase() {

		File dbFile = new File(DATABASE_NAME);

		return dbFile.exists();

	}

	private void copyDataBase() throws IOException {

		
		 /* Gets a writeable database. This will trigger its creation if it
		 * doesn't already exist.*/
		 

		mDatabase.execSQL(FTS_TABLE_CREATE);
		loadService();

	}

}
}