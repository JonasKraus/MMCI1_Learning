package com.example.jonas.mmci1_learning;

        import android.content.Context;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_RATINGS = "ratings";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_RATING = "rating";

    public static final String TABLE_STATE = "state";
    public static final String STATE_ID = "_id";
    public static final String COLUMN_SORTING = "sorting";

    private static final String DATABASE_NAME = "mmci1_learning.db";
    private static final int DATABASE_VERSION = 5;

    // Database creation sql statement
    private static final String RATINGS_CREATE = "create table "
            + TABLE_RATINGS + "(" + COLUMN_ID
            + " integer primary key, " + COLUMN_RATING
            + " integer not null);";

    // Database creation sql statement
    private static final String STATE_CREATE = "create table "
            + TABLE_STATE + "(" + STATE_ID
            + " integer primary key, " + COLUMN_SORTING
            + " varchar not null);";


    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(RATINGS_CREATE);
        //database.execSQL(STATE_CREATE);
        Log.d("created tables", "tabels");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATINGS);
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATE);
        onCreate(db);
    }

}