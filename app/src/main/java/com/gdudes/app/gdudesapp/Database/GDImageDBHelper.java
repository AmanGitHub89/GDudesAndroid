package com.gdudes.app.gdudesapp.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;

import java.util.Date;

public class GDImageDBHelper {
    private ImageDBHelper mHelper;
    private SQLiteDatabase mDatabase;

    public GDImageDBHelper(Context context) {
        mHelper = new ImageDBHelper(context);
    }

    public String GetImageStringByPicID(String PicID, Boolean GetCompleteSrc) {
        String PicSrc = "";
        if (PicID == null || PicID.equals("")) {
            return PicSrc;
        }
        PicID = PicID.toUpperCase();
        String[] columns = {ImageDBHelper.COLUMN_PICSRC};
        String[] selectionArgs = {PicID};
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.query((GetCompleteSrc ? ImageDBHelper.TABLE_COMPLETESRCIMAGES : ImageDBHelper.TABLE_THUMBNAILIMAGES),
                    columns, ImageDBHelper.COLUMN_PICID + "=?", selectionArgs, null, null, null);
            if (resultSet != null && resultSet.moveToFirst()) {
                PicSrc = resultSet.getString(resultSet.getColumnIndex(ImageDBHelper.COLUMN_PICSRC));
            }
        } catch (Exception ex) {
            PicSrc = "";
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (Exception ee) {
                }
            }
            CloseDB();
        }
        if (PicSrc != null && !PicSrc.equals("")) {
            try {
                //Try to update the last used datetime for the picture
                Date TimeNow = new Date();
                mDatabase = mHelper.getWritableDatabase();
                String sql = "UPDATE " + (GetCompleteSrc ? ImageDBHelper.TABLE_COMPLETESRCIMAGES : ImageDBHelper.TABLE_THUMBNAILIMAGES) +
                        " SET " + ImageDBHelper.COLUMN_LASTUSED + " = '" + GDDateTimeHelper.GetStringFromDate(TimeNow) + "' WHERE " +
                        ImageDBHelper.COLUMN_PICID + " = '" + PicID + "';";
                SQLiteStatement statement = mDatabase.compileStatement(sql);
                statement.execute();
            } catch (Exception ex) {
                ex.printStackTrace();
                GDLogHelper.LogException(ex);
            } finally {
                CloseDB();
            }
        }
        return PicSrc;
    }

    public void AddImageToCache(String PicID, String UserID, String PicSrc, Boolean IsCompleteSrc) {
        if (ImageExistsInCache(PicID, IsCompleteSrc)) {
            return;
        }
        try {
            mDatabase = mHelper.getWritableDatabase();
            Date TimeNow = new Date();
            String sql = "INSERT or replace INTO " + (IsCompleteSrc ? ImageDBHelper.TABLE_COMPLETESRCIMAGES : ImageDBHelper.TABLE_THUMBNAILIMAGES) + " VALUES (?,?,?,?);";
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.bindString(1, PicID.toUpperCase());
            statement.bindString(2, UserID.toUpperCase());
            statement.bindString(3, PicSrc);
            statement.bindString(4, GDDateTimeHelper.GetStringFromDate(TimeNow));
            statement.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    private Boolean ImageExistsInCache(String PicID, Boolean GetCompleteSrc) {
        if (PicID == null || PicID.equals("")) {
            return false;
        }
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getWritableDatabase();
            PicID = PicID.toUpperCase();
            String[] columns = {ImageDBHelper.COLUMN_PICSRC};
            String[] selectionArgs = {PicID};
            resultSet = mDatabase.query((GetCompleteSrc ? ImageDBHelper.TABLE_COMPLETESRCIMAGES : ImageDBHelper.TABLE_THUMBNAILIMAGES),
                    columns, ImageDBHelper.COLUMN_PICID + "=?", selectionArgs, null, null, null);
            String PicSrc = "";
            if (resultSet != null && resultSet.moveToFirst()) {
                return true;
            }
        } catch (Exception ex) {
            return true;
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (Exception ee) {
                }
            }
            CloseDB();
        }
        return false;
    }

    public void ClearUnusedImages() {
        String sTimeNow = GDDateTimeHelper.GetCurrentDateTimeAsString(false);
        try {
            mDatabase = mHelper.getWritableDatabase();
            //Where last used was more than 10 days ago
            String sql = "DELETE FROM " + ImageDBHelper.TABLE_COMPLETESRCIMAGES + " WHERE " +
                    "(julianday('" + sTimeNow + "') - julianday(" + ImageDBHelper.COLUMN_LASTUSED + ")) > 10;";
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
        try {
            mDatabase = mHelper.getWritableDatabase();
            //Where last used was more than 20 days ago
            String sql = "DELETE FROM " + ImageDBHelper.TABLE_THUMBNAILIMAGES + " WHERE " +
                    "(julianday('" + sTimeNow + "') - julianday(" + ImageDBHelper.COLUMN_LASTUSED + ")) > 20;";
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    private void CloseDB() {
        if (mDatabase != null && mDatabase.isOpen()) {
            try {
                mDatabase.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                GDLogHelper.LogException(ex);
            }
        }
    }

    private static class ImageDBHelper extends SQLiteOpenHelper {
        private static final int DB_VERSION = 1;
        private static final String DB_NAME = "GDImages_db";

        public static final String TABLE_THUMBNAILIMAGES = "ThumbnailImages";
        public static final String TABLE_COMPLETESRCIMAGES = "CompleteSrcImages";

        public static final String COLUMN_PICID = "PicID";
        public static final String COLUMN_USERID = "UserID";
        public static final String COLUMN_PICSRC = "PicSrc";
        public static final String COLUMN_LASTUSED = "LastUsed";
        private static final String CREATE_TABLE_CACHEDTHUMBNAILIMAGES = "CREATE TABLE " + TABLE_THUMBNAILIMAGES + " (" +
                COLUMN_PICID + " TEXT PRIMARY KEY," +
                COLUMN_USERID + " TEXT," +
                COLUMN_PICSRC + " TEXT," +
                COLUMN_LASTUSED + " TEXT);";
        private static final String CREATE_TABLE_CACHEDCOMPLETESRCIMAGES = "CREATE TABLE " + TABLE_COMPLETESRCIMAGES + " (" +
                COLUMN_PICID + " TEXT PRIMARY KEY," +
                COLUMN_USERID + " TEXT," +
                COLUMN_PICSRC + " TEXT," +
                COLUMN_LASTUSED + " TEXT);";

        public ImageDBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE_CACHEDTHUMBNAILIMAGES);
                db.execSQL(CREATE_TABLE_CACHEDCOMPLETESRCIMAGES);
            } catch (SQLiteException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
//                db.execSQL(" DROP TABLE " + TABLE_THUMBNAILIMAGES + " IF EXISTS;");
//                db.execSQL(" DROP TABLE " + TABLE_COMPLETESRCIMAGES + " IF EXISTS;");
//                onCreate(db);
            } catch (SQLiteException exception) {
                exception.printStackTrace();
            }
        }
    }
}
