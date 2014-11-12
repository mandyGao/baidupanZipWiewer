package hotstu.github.bdzviewer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "bdzviewer.db";

    private static DBHelper mdbHelper;

    public static DBHelper getInstance(Context context) {
        if (mdbHelper == null) {
            mdbHelper = new DBHelper(context);
        }
        return mdbHelper;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ZipfileinfoTable.onCreate(db);
        UserinfoTable.onCreate(db);
        // String CREATE_TABLE_USERINFO = "CREATE TABLE IF NOT EXISTS userinfo"
        // + "(_id INTEGER PRIMARY KEY,pass TEXT)";
        // db.execSQL(CREATE_TABLE_USERINFO);
        //
        // db.execSQL("CREATE TABLE IF NOT EXISTS data" +
        // "(_id INTEGER PRIMARY KEY, jsonstr TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ZipfileinfoTable.onUpgrade(db, oldVersion, newVersion);

    }

    // public boolean createPass(String pwd) {
    // SQLiteDatabase db = this.getWritableDatabase();
    // ContentValues values = new ContentValues();
    //
    // values.put("pass",pwd);
    // long result = db.insert(TABLE_USERINFO, null, values);
    // db.close();
    // if (result >= 0)
    // return true;
    // else
    // return false;
    // }
    //
    // public String checkPass(){
    // SQLiteDatabase db = getReadableDatabase();
    // String result = "";
    // Cursor cursor = db.query(TABLE_USERINFO, new String[] {"pass"}, "_id=?",
    // new String[] {"1"}, null, null, null);
    // if (cursor.moveToFirst()){
    // result = cursor.getString(0);
    // }
    // cursor.close();
    // db.close();
    // return result;
    // }
    //
    // public String queryForData() {
    // SQLiteDatabase db = getReadableDatabase();
    // String result = "";
    // Cursor cursor = db.query(TABLE_DATA, new String[] {"jsonstr"}, "_id=?",
    // new String[] {"1"}, null, null, null);
    // if (cursor.moveToFirst()){
    // result = cursor.getString(0);
    // }
    // cursor.close();
    // db.close();
    // return result;
    // }
    //
    // /**
    // * 这里数据库中永远只放一条信息
    // * @param info
    // */
    // public void addOrUpdate(String jsonstr){
    // SQLiteDatabase db = getWritableDatabase();
    // Cursor c = db.rawQuery("SELECT * FROM data", null);
    // int count = c.getCount();
    // c.close();
    // if(count==0){
    // db.execSQL("INSERT INTO data VALUES(?, ?)",new Object[]{1,jsonstr});
    // }
    // else{
    // ContentValues cv = new ContentValues();
    // cv.put("jsonstr", jsonstr);
    // db.update("data", cv, "_id=?", new String[]{"1"});
    // }
    // db.close();
    // }

}
