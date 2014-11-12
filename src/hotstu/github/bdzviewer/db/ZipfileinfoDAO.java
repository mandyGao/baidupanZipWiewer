package hotstu.github.bdzviewer.db;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import hotstu.github.bdzviewer.model.FileInfo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ZipfileinfoDAO {

    public static long insert(Context context, FileInfo f) {
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        Gson g = new Gson();
        values.put(ZipfileinfoTable.COLUMN_KEY, f.getId());
        values.put(ZipfileinfoTable.COLUMN_TITLE, f.getName());
        values.put(ZipfileinfoTable.COLUMN_TOTAL, f.getTotal());
        values.put(ZipfileinfoTable.COLUMN_PATH, f.getPath());
        values.put(ZipfileinfoTable.COLUMN_LIST, g.toJson(f.getList()));
        long insertId = db.insert(ZipfileinfoTable.TABLE_NAME, null, values);
        db.close();
        return insertId;

    }
    
    public static int delete(Context context, FileInfo f) {
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        int r = db.delete(ZipfileinfoTable.TABLE_NAME, ZipfileinfoTable.COLUMN_KEY+"=?", new String[]{f.getId()});
        db.close();
        return r;
    }

    public static List<FileInfo> getAllFileinfo(Context context) {
        List<FileInfo> fs = new ArrayList<FileInfo>();
        String[] selectionArgs = new String[] {ZipfileinfoTable.COLUMN_KEY, ZipfileinfoTable.COLUMN_TITLE,
                ZipfileinfoTable.COLUMN_PATH, ZipfileinfoTable.COLUMN_TOTAL, ZipfileinfoTable.COLUMN_LIST};
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        Cursor cursor = db.query(ZipfileinfoTable.TABLE_NAME, selectionArgs, null, null,
                null, null, null);

        Gson g = new Gson();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FileInfo f = new FileInfo();
            f.setId(cursor.getString(0));
            f.setName(cursor.getString(1));
            f.setPath(cursor.getString(2));
            f.setTotal(cursor.getInt(3));
            f.setList((ArrayList<String>) g.fromJson(cursor.getString(4), new TypeToken<List<String>>(){}.getType()));
            fs.add(f);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        db.close();
        return fs;
    }

}
