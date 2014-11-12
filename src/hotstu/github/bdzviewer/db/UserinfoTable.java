package hotstu.github.bdzviewer.db;

import android.database.sqlite.SQLiteDatabase;

public class UserinfoTable {
    public static final String TABLE_NAME = "userinfo";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_PWD = "pass";

    public static void onCreate(SQLiteDatabase database) {
        String CREATE_TABLE_USERINFO = "CREATE TABLE IF NOT EXISTS userinfo"
                + "(_id INTEGER PRIMARY KEY,pass TEXT)";
        database.execSQL(CREATE_TABLE_USERINFO);
    }
}
