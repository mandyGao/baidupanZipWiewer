package hotstu.github.bdzviewer.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ZipfileinfoTable {
    public static final String TABLE_NAME = "fileinfo";
    
    public static final String COLUMN_ID = "_id";
    
    public static final String COLUMN_PATH = "path";
            
    public static final String COLUMN_TITLE = "title";
    
    public static final String COLUMN_KEY = "key";
    
    public static final String COLUMN_TOTAL = "total";
    
    public static final String COLUMN_LIST = "list";
    
    
    private static final String DATABASE_CREATE = "create table " 
            + TABLE_NAME
            + "(" 
            + COLUMN_ID + " integer primary key autoincrement, " 
            + COLUMN_PATH + " text not null, " 
            + COLUMN_TITLE + " text not null, " 
            + COLUMN_KEY + " text not null," 
            + COLUMN_TOTAL + " integer not null," 
            + COLUMN_LIST + " text not null" 
            + ")";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
      }

      public static void onUpgrade(SQLiteDatabase database, int oldVersion,
          int newVersion) {
        Log.w(ZipfileinfoTable.class.getName(), "Upgrading database from version "
            + oldVersion + " to " + newVersion
            + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
      }

}
