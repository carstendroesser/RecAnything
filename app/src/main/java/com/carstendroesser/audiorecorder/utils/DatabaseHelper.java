package com.carstendroesser.audiorecorder.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.models.Category;
import com.carstendroesser.audiorecorder.models.Media;
import com.carstendroesser.audiorecorder.models.Note;
import com.carstendroesser.audiorecorder.models.Record;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by carstendrosser on 16.10.15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public enum Sorting {
        NAME_ASC(0, "ORDER BY " + RECORD_NAME + " ASC"),
        NAME_DESC(1, "ORDER BY " + RECORD_NAME + " DESC"),
        CREATED_ASC(2, "ORDER BY " + RECORD_TIMESTAMP + " ASC"),
        CREATED_DESC(3, "ORDER BY " + RECORD_TIMESTAMP + " DESC");

        int mId;
        String mQuery;

        Sorting(int pId, String pQuery) {
            mId = pId;
            mQuery = pQuery;
        }

        public static Sorting getSortingById(int pId) {
            switch (pId) {
                case 0:
                    return NAME_ASC;
                case 1:
                    return NAME_DESC;
                case 2:
                    return CREATED_ASC;
                case 3:
                    return CREATED_DESC;
                default:
                    return null;
            }
        }

    }

    public static final String RECORDS_TABLENAME = "records";
    public static final String RECORD_ID = "record_id";
    public static final String RECORD_PATH = "record_path";
    public static final String RECORD_NAME = "record_name";
    public static final String RECORD_CATEGORY = "record_category";
    public static final String RECORD_TIMESTAMP = "record_timestamp";

    public static final String CATEGORIES_TABLENAME = "categories";
    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_NAME = "category_name";
    public static final String CATEGORY_DESCRIPTION = "category_description";
    public static final String CATEGORY_ICON = "category_icon";
    public static final String NOTES_TABLENAME = "notes";
    public static final String NOTE_ID = "note_id";
    public static final String NOTE_RECORD_ID = "note_recordid";
    public static final String NOTE_TEXT = "note_text";
    public static final String MEDIAS_TABLENAME = "medias";
    public static final String MEDIA_ID = "media_id";
    public static final String MEDIA_RECORD_ID = "media_recordid";
    public static final String MEDIA_TYP = "media_typ";
    public static final String MEDIA_PATH = "media_path";
    private static final String RECORDS_CREATE_TABLE
            = "CREATE TABLE "
            + RECORDS_TABLENAME
            + " ("
            + RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + RECORD_PATH + " VARCHAR(100), "
            + RECORD_NAME + " VARCHAR(100), "
            + RECORD_CATEGORY + " INTEGER, "
            + RECORD_TIMESTAMP + " VARCHAR(100));";
    private static final String CATEGORIES_CREATE_TABLE
            = "CREATE TABLE "
            + CATEGORIES_TABLENAME
            + " ("
            + CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CATEGORY_NAME + " VARCHAR(100), "
            + CATEGORY_DESCRIPTION + " VARCHAR(100), "
            + CATEGORY_ICON + " INTEGER);";
    private static final String NOTES_CREATE_TABLE
            = "CREATE TABLE "
            + NOTES_TABLENAME
            + " ("
            + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NOTE_RECORD_ID + " INTEGER, "
            + NOTE_TEXT + " VARCHAR(100));";
    private static final String MEDIAS_CREATE_TABLE
            = "CREATE TABLE "
            + MEDIAS_TABLENAME
            + " ("
            + MEDIA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MEDIA_RECORD_ID + " INTEGER, "
            + MEDIA_TYP + " INTEGER, "
            + MEDIA_PATH + " VARCHAR(100));";

    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;
    private static final int DEFAULT_CATEGORY_ID = 1;
    private static DatabaseHelper mDatabaseHelper;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private ArrayList<Category> mFilteredCategories;

    private DatabaseHelper(Context pContext) {
        super(pContext, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = pContext;
        mFilteredCategories = new ArrayList<>();
    }

    /**
     * Returns the single instance. We only have one to avoid
     * multiple accesses to the database at the same time.
     * This way, accesses get serialized.
     *
     * @param pContext we need that
     * @return
     */
    public static DatabaseHelper getInstance(Context pContext) {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = new DatabaseHelper(pContext);
        }
        return mDatabaseHelper;
    }

    /**
     * Resets all categoryfilters.
     */
    public void clearUnselectedCategories() {
        mFilteredCategories.clear();
    }

    /**
     * Gets all the categoryfilters.
     *
     * @return a list fo categories that shall not be shown
     */
    public ArrayList<Category> getUnselectedCatgoriesList() {
        return mFilteredCategories;
    }

    public long getRecordsCount() {
        checkDatabase();
        long cnt = DatabaseUtils.queryNumEntries(mDatabase, RECORDS_TABLENAME);
        return cnt;
    }

    /**
     * Sets a list of categories that shall not be shown. When retreiving the records,
     * all records with a category contained within this list will be filtered.
     *
     * @param pList the list of categories to filter
     */
    public void setUnselectedCategories(ArrayList<Category> pList) {
        mFilteredCategories.clear();
        mFilteredCategories.addAll(pList);
    }

    @Override
    public void onCreate(SQLiteDatabase pDatabase) {

        //create all tables
        pDatabase.execSQL(RECORDS_CREATE_TABLE);
        pDatabase.execSQL(CATEGORIES_CREATE_TABLE);
        pDatabase.execSQL(NOTES_CREATE_TABLE);
        pDatabase.execSQL(MEDIAS_CREATE_TABLE);

        mDatabase = pDatabase;

        //insert some default categories
        insertCategory(mContext.getString(R.string.default_category_name1),
                mContext.getString(R.string.default_category_description1),
                0);
        insertCategory(mContext.getString(R.string.default_category_name2),
                mContext.getString(R.string.default_category_description2),
                10);
        insertCategory(mContext.getString(R.string.default_category_name3),
                mContext.getString(R.string.default_category_description3),
                7);
    }

    @Override
    public void onUpgrade(SQLiteDatabase pDatabase, int pOldVersion, int pNewVersion) {
    }

    /**
     * Performs a database-query and returns all records, including their categories.
     *
     * @return a list of all records
     */
    public ArrayList<Record> getRecords(Sorting pSorting) {

        checkDatabase();

        //query
        Cursor cursor = mDatabase
                .rawQuery("SELECT * FROM "
                        + RECORDS_TABLENAME
                        + ", "
                        + CATEGORIES_TABLENAME
                        + " WHERE "
                        + RECORD_CATEGORY
                        + " = "
                        + CATEGORY_ID + " " + pSorting.mQuery, null);

        return cursorToRecordsList(cursor);
    }

    /**
     * Converts a cursor to a list of records.
     *
     * @param pCursor the cursor
     * @return a list of records
     */
    private ArrayList<Record> cursorToRecordsList(Cursor pCursor) {
        ArrayList<Record> allRecords = new ArrayList<Record>();
        while (pCursor.moveToNext()) {
            int recordid = pCursor.getInt(0);
            String recordpath = pCursor.getString(1);
            String recordname = pCursor.getString(2);
            int recordcategory = pCursor.getInt(3);
            long timestamp = pCursor.getLong(4);

            int categoryid = pCursor.getInt(5);
            String categoryname = pCursor.getString(6);
            String categorydescription = pCursor.getString(7);
            int categoryicon = pCursor.getInt(8);
            Category category = new Category(categoryid, categoryname, categorydescription, categoryicon);

            Record currentRecord = new Record(recordid, recordpath, recordname, recordcategory, category, timestamp);
            currentRecord.setMediaList(getMediaFor(recordid));
            currentRecord.setNote(getNoteFor(recordid));

            // check if the record is in a category which is filtered
            boolean exclude = false;

            for (Category filteredCategory : mFilteredCategories) {
                if (currentRecord.getCategory().getId() == filteredCategory.getId()) {
                    exclude = true;
                }
            }

            if (!exclude) {
                allRecords.add(currentRecord);
            }

        }

        pCursor.close();

        Collections.reverse(allRecords);
        return allRecords;
    }

    /**
     * Performs a database-query and returns all records, including their categories.
     * Records begin with the given text.
     *
     * @param pText the string to filter records
     * @return a list of records beginning with the text
     */
    public ArrayList<Record> getRecordsBeginningWith(String pText, Sorting pSorting) {

        checkDatabase();

        String[] args = new String[1];
        args[0] = pText + "%";

        //query
        Cursor cursor = mDatabase
                .rawQuery("SELECT * FROM "
                        + RECORDS_TABLENAME
                        + ", "
                        + CATEGORIES_TABLENAME
                        + " WHERE "
                        + RECORD_CATEGORY
                        + " = "
                        + CATEGORY_ID + " AND " + RECORD_NAME + " LIKE ? " + pSorting.mQuery, args);


        return cursorToRecordsList(cursor);
    }

    /**
     * Performs a database-query and returns all categories.
     *
     * @return a list of all categories
     */
    public ArrayList<Category> getCategories() {

        checkDatabase();

        //query
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + CATEGORIES_TABLENAME, null);
        ArrayList<Category> allCategories = new ArrayList<Category>();

        //add the categories
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String description = cursor.getString(2);
            int icon = cursor.getInt(3);
            allCategories.add(new Category(id, name, description, icon));
        }

        cursor.close();

        return allCategories;
    }

    public Note getNoteFor(int pRecordid) {

        checkDatabase();

        //query
        Cursor cursor = mDatabase.rawQuery(
                "SELECT * FROM " + NOTES_TABLENAME + " WHERE " + NOTE_RECORD_ID + " = " + pRecordid,
                null);

        Note note = null;

        if (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            int recordId = cursor.getInt(1);
            String text = cursor.getString(2);
            note = new Note(id, recordId, text);
        } else {
            note = null;
        }

        cursor.close();
        return note;
    }

    private void checkDatabase() {
        if (mDatabase == null) {
            mDatabase = getWritableDatabase();
        }
    }

    /**
     * Fetches all Media for a specific record.
     *
     * @param pRecordid the record to fetch media for
     * @return a list of media that are linked to this record
     */
    public ArrayList<Media> getMediaFor(int pRecordid) {

        checkDatabase();

        //query
        Cursor cursor = mDatabase.rawQuery(
                "SELECT * FROM " + MEDIAS_TABLENAME + " WHERE " + MEDIA_RECORD_ID + " = " + pRecordid,
                null);
        ArrayList<Media> allMedia = new ArrayList<Media>();

        //add all media to the list
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            int recordId = cursor.getInt(1);
            int type = cursor.getInt(2);
            String path = cursor.getString(3);
            allMedia.add(new Media(id, recordId, type, path));
        }

        cursor.close();

        return allMedia;
    }

    /**
     * Get the category for a specific record. Returns a null-object if something went wrong.
     *
     * @param pCategoryId the categoryid to get the category for
     * @return the category with this id
     */
    public Category getCategoryById(int pCategoryId) {

        checkDatabase();

        Category category = null;

        //query
        Cursor cursor = mDatabase
                .rawQuery("SELECT * FROM "
                                + CATEGORIES_TABLENAME
                                + " WHERE "
                                + CATEGORY_ID
                                + " = "
                                + pCategoryId,
                        null);

        //check if cursor is not empty and init category
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String description = cursor.getString(2);
            int icon = cursor.getInt(3);
            category = new Category(id, name, description, icon);
        }

        cursor.close();

        return category;
    }

    /**
     * Fetch a record by a given id. Will return null, if there is no record with this id.
     *
     * @param pId the recordid to fetch the record for
     * @return the record with this id
     */
    public Record getRecordById(int pId) {

        checkDatabase();

        Record record = null;
        //query

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM "
                + RECORDS_TABLENAME
                + ", "
                + CATEGORIES_TABLENAME
                + " WHERE "
                + RECORD_CATEGORY
                + " = "
                + CATEGORY_ID
                + " AND "
                + RECORD_ID
                + " = "
                + pId, null, null);

        //check if cursor is not empty and init record
        if (cursor.moveToFirst()) {
            int recordid = cursor.getInt(0);
            String recordpath = cursor.getString(1);
            String recordname = cursor.getString(2);
            int recordcategory = cursor.getInt(3);
            long timestamp = cursor.getLong(4);

            int categoryid = cursor.getInt(5);
            String categoryname = cursor.getString(6);
            String categorydescription = cursor.getString(7);
            int categoryicon = cursor.getInt(8);
            Category category = new Category(categoryid, categoryname, categorydescription, categoryicon);

            record = new Record(recordid, recordpath, recordname, recordcategory, category, timestamp);
            record.setMediaList(getMediaFor(recordid));
            record.setNote(getNoteFor(recordid));
        }

        cursor.close();

        return record;
    }

    /**
     * Returns records within a category with the given id.
     *
     * @param pId the id of the category to fetch records for
     * @return a list of records, linked to this category
     */
    public List<Record> getRecordsWithCategoryId(int pId, Sorting pSorting) {

        checkDatabase();

        ArrayList<Record> records = new ArrayList<>();

        // query
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM "
                + RECORDS_TABLENAME
                + ", "
                + CATEGORIES_TABLENAME
                + " WHERE "
                + RECORD_CATEGORY
                + " = "
                + CATEGORY_ID
                + " AND "
                + RECORD_CATEGORY
                + " = "
                + pId + " " + pSorting.mQuery, null, null);

        //check if cursor is not empty and init record
        while (cursor.moveToNext()) {
            int recordid = cursor.getInt(0);
            String recordpath = cursor.getString(1);
            String recordname = cursor.getString(2);
            int recordcategory = cursor.getInt(3);
            long recordtimestamp = cursor.getLong(4);

            int categoryid = cursor.getInt(5);
            String categoryname = cursor.getString(6);
            String categorydescription = cursor.getString(7);
            int categoryicon = cursor.getInt(8);
            Category category = new Category(categoryid, categoryname, categorydescription, categoryicon);

            Record currentRecord = new Record(recordid, recordpath, recordname, recordcategory, category, recordtimestamp);
            currentRecord.setMediaList(getMediaFor(recordid));
            currentRecord.setNote(getNoteFor(recordid));
            records.add(currentRecord);
        }

        cursor.close();

        return records;
    }

    /**
     * Adds a new record to the database based on the filepath.
     *
     * @param pFilepath the filepath of the record
     * @return the id of the inserted record
     */
    public long insertRecord(String pFilepath, String pName) {

        checkDatabase();

        //create file with the path to get the filename
        File file = new File(pFilepath);

        //create values to insert
        ContentValues values = new ContentValues();
        values.put(RECORD_PATH, pFilepath);
        values.put(RECORD_NAME, pName);
        values.put(RECORD_TIMESTAMP, file.lastModified());
        values.put(RECORD_CATEGORY, DEFAULT_CATEGORY_ID);

        //insert!
        long id = mDatabase.insert(RECORDS_TABLENAME, null, values);
        return id;
    }

    /**
     * Adds a new Media-object to the database.
     *
     * @param pRecordId the record id this media belongs to
     * @param pMediaTyp the type of this media: Photo or Video
     * @param pPath     the path of this media
     * @return true if succeeded, false otherwise
     */
    public boolean insertMedia(int pRecordId, int pMediaTyp, String pPath) {

        checkDatabase();

        //create values to insert
        ContentValues values = new ContentValues();
        values.put(MEDIA_RECORD_ID, pRecordId);
        values.put(MEDIA_TYP, pMediaTyp);
        values.put(MEDIA_PATH, pPath);

        //insert!
        long row = mDatabase.insert(MEDIAS_TABLENAME, null, values);

        //if row == -1, something went wrong and return false
        return !(row == -1);
    }

    /**
     * Adds a new category to the database.
     *
     * @param pName        the name of this category
     * @param pDescription a description for this category
     * @param pIcon        the iconid for this category
     * @return true if insertion succeeded, false otherwise
     */
    public boolean insertCategory(String pName, String pDescription, int pIcon) {

        checkDatabase();

        //create values to insert
        ContentValues values = new ContentValues();
        values.put(CATEGORY_NAME, pName);
        values.put(CATEGORY_DESCRIPTION, pDescription);
        values.put(CATEGORY_ICON, pIcon);

        //insert!
        long row = mDatabase.insert(CATEGORIES_TABLENAME, null, values);

        //if row == -1, something went wrong and return false
        return !(row == -1);
    }

    /**
     * Adds a note to the database, linked to a specific recording.
     *
     * @param pRecordId the id of the record this note belongs to
     * @param pText     the text this note contains
     * @return true if succeeded, false otherwise
     */
    public boolean insertNote(int pRecordId, String pText) {

        checkDatabase();

        if (getNoteFor(pRecordId) != null) {
            return false;
        }

        //create values to insert
        ContentValues values = new ContentValues();
        values.put(NOTE_RECORD_ID, pRecordId);
        values.put(NOTE_TEXT, pText);

        //insert!
        long row = mDatabase.insert(NOTES_TABLENAME, null, values);

        //if row == -1, something went wrong and return false
        return !(row == -1);
    }

    /**
     * Saves changed record-object to the database.
     *
     * @param pRecord the record which values have been changed
     * @return true, if updating succeeded. Otherwise false
     */
    public boolean updateRecord(Record pRecord) {

        checkDatabase();

        ContentValues values = new ContentValues();
        values.put(RECORD_ID, pRecord.getId());
        values.put(RECORD_PATH, pRecord.getPath());
        values.put(RECORD_NAME, pRecord.getName());
        values.put(RECORD_CATEGORY, pRecord.getCategoryId());
        values.put(RECORD_TIMESTAMP, pRecord.getTimestamp());

        return mDatabase.update(RECORDS_TABLENAME, values, RECORD_ID + " = " + pRecord.getId(), null) > 0;
    }

    /**
     * Saves changed note-object to the database.
     *
     * @param pNote the note which values have been changed
     * @return true if updating succeeded, otherwise false
     */
    public boolean updateNote(Note pNote) {
        checkDatabase();

        ContentValues values = new ContentValues();
        values.put(NOTE_ID, pNote.getId());
        values.put(NOTE_RECORD_ID, pNote.getRecordId());
        values.put(NOTE_TEXT, pNote.getText());

        return mDatabase.update(NOTES_TABLENAME, values, NOTE_ID + " = " + pNote.getId(), null) > 0;
    }

    /**
     * Saves changed category-object to the database.
     *
     * @param pCategory the category which values have been changed
     * @return true if updating succeeded, otherwise false
     */
    public boolean updateCategory(Category pCategory) {

        checkDatabase();

        ContentValues values = new ContentValues();
        values.put(CATEGORY_ID, pCategory.getId());
        values.put(CATEGORY_NAME, pCategory.getName());
        values.put(CATEGORY_DESCRIPTION, pCategory.getDescription());
        values.put(CATEGORY_ICON, pCategory.getIcon());

        return mDatabase.update(CATEGORIES_TABLENAME, values, CATEGORY_ID + " = " + pCategory.getId(), null) > 0;
    }

    /**
     * Deletes a note.
     *
     * @param pNote the note to delete
     * @return true if deleted succesfuly, false otherwise
     */
    public boolean deleteNote(Note pNote) {
        checkDatabase();
        return mDatabase.delete(NOTES_TABLENAME, NOTE_ID + "=" + pNote.getId(), null) > 0;
    }

    /**
     * Deletes a media.
     *
     * @param pMedia the media to delete
     * @return true if deleted succesfuly, false otherwise
     */
    public boolean deleteMedia(Media pMedia) {
        checkDatabase();
        File file = new File(pMedia.getPath());
        boolean fileDeleted = file.delete();
        return mDatabase.delete(MEDIAS_TABLENAME, MEDIA_ID + "=" + pMedia.getId(), null) > 0 && fileDeleted;
    }

    /**
     * Deletes a category.
     *
     * @param pCategory the category to delete
     * @return true if deleted succesfuly, false otherwise
     */
    public boolean deleteCategory(Category pCategory) {
        checkDatabase();
        return mDatabase.delete(CATEGORIES_TABLENAME, CATEGORY_ID + "=" + pCategory.getId(), null) > 0;
    }

    /**
     * Deletes the recordfile by a given record-id.
     *
     * @param pRecordId the record's id to delete the file for
     * @return true if deleted successfully
     */
    public boolean deleteRecordById(int pRecordId) {
        checkDatabase();
        Record record = getRecordById(pRecordId);
        if (record != null) {
            File file = new File(record.getPath());
            boolean fileDeleted = file.delete();
            return mDatabase.delete(RECORDS_TABLENAME, RECORD_ID + "=" + record.getId(), null) > 0 && fileDeleted;
        } else {
            return true;
        }
    }

    /**
     * Deletes all Media for a given record-id.
     *
     * @param pRecordId the record's id to delete all media for
     * @return true if all media has been deleted successfully
     */
    public boolean deleteAllMediaFor(int pRecordId) {
        checkDatabase();
        boolean deletedAll = true;
        ArrayList<Media> medias = getMediaFor(pRecordId);
        for (Media pMedia : medias) {
            deletedAll = deleteMedia(pMedia);
        }

        return deletedAll;
    }

    /**
     * Deletes the note for a given record-id.
     *
     * @param pRecordId the record's id to delete the note for
     * @return true if the note has been deleted successfully
     */
    public boolean deleteNoteFor(int pRecordId) {
        checkDatabase();
        Note note = getNoteFor(pRecordId);
        if (note == null) {
            return true;
        } else {
            return deleteNote(note);
        }
    }

}
