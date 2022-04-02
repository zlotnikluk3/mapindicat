package logic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.util.ArrayList;
import java.util.List;

import model.Flag;

public class SQLiteDbFlag extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "mi.db";
    private boolean oc;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS flagtab (id INTEGER PRIMARY KEY, map INTEGER NOT NULL, dial INTEGER NOT NULL)";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS flagtab";

    public SQLiteDbFlag(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        onCreate(getWritableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        oc = true;
        db.execSQL(SQL_CREATE_ENTRIES);
        ;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void create(Flag f) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", f.getId());
        values.put("map", f.getMap());
        values.put("dial", f.getDial());

        long newRowId;
        newRowId = db.insert(
                "flagtab",
                null,
                values);
        db.close();
    }

    public void update(Flag f) {
        SQLiteDatabase db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", f.getId());
        values.put("map", f.getMap());
        values.put("dial", f.getDial());

        String selection = "id LIKE ?";
        String[] selectionArgs = {String.valueOf(f.getId())};

        int count = db.update(
                "flagtab",
                values,
                selection,
                selectionArgs);
        db.close();
    }


    public List<String> getAllLabels() {
        List<String> labels = new ArrayList<String>();

        String selectQuery = "SELECT  * FROM flagtab";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return labels;
    }

    public List<Flag> getAllFlag() {
        String selectQuery = "SELECT  * FROM flagtab";
        SQLiteDatabase db = this.getReadableDatabase();
        if (!oc) {
            this.onCreate(db);
        }
        Cursor c = db.rawQuery(selectQuery, null);
        List<Flag> l = new ArrayList<Flag>();
        if (c != null && c.moveToFirst()) {
            do {
                l.add(new Flag(c.getInt(0), c.getInt(1), c.getInt(2)));
            } while (c.moveToNext());
        }
        db.close();
        c.close();
        return l;
    }

}
