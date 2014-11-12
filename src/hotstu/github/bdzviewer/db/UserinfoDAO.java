package hotstu.github.bdzviewer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserinfoDAO {
    
    public static boolean createPass(Context context, String pwd) {
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("pass", pwd);
        long result = db.insert(UserinfoTable.TABLE_NAME, null, values);
        db.close();
        if (result >= 0)
            return true;
        else
            return false;
    }

    public static String checkPass(Context context) {
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
        String result = "";
        Cursor cursor = db.query(UserinfoTable.TABLE_NAME, new String[] { "pass" },
                "_id=?", new String[] { "1" }, null, null, null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return result;
    }

}
