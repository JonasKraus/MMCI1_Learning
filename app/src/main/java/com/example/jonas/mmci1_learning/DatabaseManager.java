package com.example.jonas.mmci1_learning;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonas on 14.02.2015.
 */
public class DatabaseManager {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_RATING };

    public DatabaseManager(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public String createRating(int id, int rating) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_RATING, rating);
        database.execSQL("INSERT OR replace INTO RATINGS (_id, rating) values("+id+", "+rating+")");
        /*
        if (getRating(id) == -1) {
            long insertId = database.insert(MySQLiteHelper.TABLE_RATINGS, "" + id, values);
            Log.d("Insert", insertId+"");
            return "INSERT";
        } else {
            long updateId = database.update(MySQLiteHelper.TABLE_RATINGS, values, id+"", null);
            Log.d("update", updateId+"");
            return "UPDATE";
        }
        */
        return "insert or update "+id+" "+rating+ "getRate() " +getRating(id);
    }
    public String createState(int id, String sorting) {
        database.execSQL("DROP TABLE IF EXISTS "+MySQLiteHelper.TABLE_STATE+";");
        database.execSQL("INSERT INTO "+MySQLiteHelper.TABLE_STATE+" (_id, sorting) values("+id+", "+sorting+")");
        return "updated STATE "+id + " "+sorting;
    }
    public Pair<Integer, String> getState() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STATE,
                null, null, null,
                null, null, null);
        if (cursor.moveToFirst()) {
            int state_id = cursor.getInt(0);
            String sorting = cursor.getString(1);
            cursor.close();
            Log.d("select", state_id + " sorting:" + sorting);
            Pair<Integer, String> pair = new Pair<>(state_id, sorting);
            return pair;
        }
        return null;
    }

    public int getRating(int id) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_RATINGS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null,
                null, null, null);
        int rating = cursorToRating(cursor);
        cursor.close();
        Log.d("select",rating+" from id:"+id);
        return rating;
    }

    public List<Integer> getIdsWithRatingASC() {
        List<Integer>ids = new ArrayList<Integer>();
        // String[] idCol = new String[]{"_id"};
        /*
        Cursor cursor = database.query(MySQLiteHelper.TABLE_RATINGS,
                allColumns, null, null, null, null, "rating ASC");
                */
        Cursor c = database.rawQuery("Select _id from ratings order by rating ASC", null);

        if (c.moveToFirst()) {
            do {
                ids.add((Integer)c.getInt(0));
            } while (c.moveToNext());
        }
        c.close();
        Log.d("ids", ids.toArray().toString());
        return ids;
    }

    public int getOverallKnowledge(int size) {
        List<Integer>ratings = new ArrayList<Integer>();
        Cursor c = database.rawQuery("Select rating from ratings", null);

        if (c.moveToFirst()) {
            do {
                ratings.add((Integer) c.getInt(0));
            } while (c.moveToNext());
        }
        c.close();
        int res =0;
        for (int r : ratings) {
            res += r;
        }
        res = res/size;
        return res;
    }

    private int cursorToRating(Cursor cursor) {
        if (cursor.moveToFirst()) {
            int rating = (cursor.getInt(1));
            return rating;
        }
        return -1;
    }
    private int cursorToId(Cursor cursor) {
        if (cursor.moveToFirst()) {
            int id = (cursor.getInt(0));
            return id;
        }
        return -1;
    }
}
