package logic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import model.Eventt;

public class SQLiteDbEvent extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "mi.db";

    private boolean oc;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS Eventt (id INTEGER PRIMARY KEY AUTOINCREMENT, opis TEXT, lat TEXT NOT NULL, lng TEXT NOT NULL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS Eventt";

    public SQLiteDbEvent(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        onCreate(getWritableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        oc = true;
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void create(Eventt event) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", event.getId());
        values.put("opis", event.getOpis());
        values.put("lat", event.getLat());
        values.put("lng", event.getLng());

        long newRowId;
        newRowId = db.insert(
                "Eventt",
                null,
                values);
        db.close();
    }

    public List<Eventt> retrieve(int i) {
        String selectQuery = "SELECT  * FROM Eventt WHERE id =" + i;
        SQLiteDatabase db = this.getReadableDatabase();
        if (!oc) {
            this.onCreate(db);
        }
        Cursor c = db.rawQuery(selectQuery, null);
        List<Eventt> l = new ArrayList<Eventt>();
        if (c != null && c.moveToFirst()) {
            do {
                l.add(new Eventt(c.getInt(0), c.getString(1),
                        c.getString(2), c.getString(3)));
            } while (c.moveToNext());
        }
        db.close();
        c.close();
        return l;
    }

    public void update(Eventt event) {
        SQLiteDatabase db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", event.getId());
        values.put("opis", event.getOpis());
        values.put("lat", event.getLat());
        values.put("lng", event.getLng());

        String selection = "id LIKE ?";
        String[] selectionArgs = {String.valueOf(event.getId())};

        int count = db.update(
                "Eventt",
                values,
                selection,
                selectionArgs);
        db.close();
    }

    public void delete(Eventt e) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = "id LIKE ?";
        String[] selectionArgs = {String.valueOf(e.getId())};
        db.delete("Eventt", selection, selectionArgs);
        db.close();
    }

    public List<String> getAllLabels() {
        List<String> labels = new ArrayList<String>();

        String selectQuery = "SELECT  * FROM Eventt";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return labels;
    }

    public List<Integer> getAllId() {
        List<Integer> ids = new ArrayList<Integer>();

        String selectQuery = "SELECT  * FROM Eventt";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ids.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return ids;
    }

    public List<Eventt> getAllEve() {
        String selectQuery = "SELECT  * FROM Eventt";
        SQLiteDatabase db = this.getReadableDatabase();
        if (!oc) {
            this.onCreate(db);
        }
        Cursor c = db.rawQuery(selectQuery, null);
        List<Eventt> l = new ArrayList<Eventt>();
        if (c != null && c.moveToFirst()) {
            do {
                l.add(new Eventt(c.getInt(0), c.getString(1),
                        c.getString(2), c.getString(3)));
            } while (c.moveToNext());
        }
        db.close();
        c.close();
        return l;
    }
}