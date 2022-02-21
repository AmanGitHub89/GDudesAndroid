package com.gdudes.app.gdudesapp.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.gdudes.app.gdudesapp.GDTypes.Conversations;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;

import java.util.ArrayList;

public class GDConversationsDBHelper {
    private ConversationsDBHelper mHelper;
    private SQLiteDatabase mDatabase;
    private Context mContext;

    public GDConversationsDBHelper(Context context) {
        mHelper = new ConversationsDBHelper(context);
        mContext = context;
    }

    public ArrayList<Conversations> GetAllConversationsFromCache(String LoggedInUserID) {
        String[] columns = {ConversationsDBHelper.COLUMN_LOGGEDINUSERID, ConversationsDBHelper.COLUMN_CONVWITHUSERID,
                ConversationsDBHelper.COLUMN_USERNAME, ConversationsDBHelper.COLUMN_PICID, ConversationsDBHelper.COLUMN_PROFILEPIC,
                ConversationsDBHelper.COLUMN_LASTMESSAGEDT, ConversationsDBHelper.COLUMN_SENDERTYPE, ConversationsDBHelper.COLUMN_UNREADCOUNT,
                ConversationsDBHelper.COLUMN_LASTMESSAGELOCALDT};
        ArrayList<Conversations> CachedConversations = new ArrayList<>();
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            String[] selectionArgs = {LoggedInUserID, "T"};
            resultSet = mDatabase.query(ConversationsDBHelper.TABLE_CONVERSATIONS,
                    columns, ConversationsDBHelper.COLUMN_LOGGEDINUSERID + "=? COLLATE NOCASE AND "
                            + ConversationsDBHelper.COLUMN_MARKEDDELETE + "<>?",
                    selectionArgs, null, null, null);

            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                Conversations conversation = GetConversationFromResultSet(resultSet);
                CachedConversations.add(conversation);
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    conversation = GetConversationFromResultSet(resultSet);
                    CachedConversations.add(conversation);
                }
            }
        } catch (Exception ex) {
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
        return CachedConversations;
    }

    public ArrayList<Conversations> GetConversationsForConverseWithIDList(String LoggedInUserID, ArrayList<String> UserIDList) {
        String[] columns = {ConversationsDBHelper.COLUMN_LOGGEDINUSERID, ConversationsDBHelper.COLUMN_CONVWITHUSERID,
                ConversationsDBHelper.COLUMN_USERNAME, ConversationsDBHelper.COLUMN_PICID, ConversationsDBHelper.COLUMN_PROFILEPIC,
                ConversationsDBHelper.COLUMN_LASTMESSAGEDT, ConversationsDBHelper.COLUMN_SENDERTYPE, ConversationsDBHelper.COLUMN_UNREADCOUNT,
                ConversationsDBHelper.COLUMN_LASTMESSAGELOCALDT};
        ArrayList<Conversations> CachedConversations = new ArrayList<>();
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();

            String selection = ConversationsDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE  AND " +
                    ConversationsDBHelper.COLUMN_MARKEDDELETE + "<>'T' AND " +
                    ConversationsDBHelper.COLUMN_CONVWITHUSERID + " IN ('" + TextUtils.join("','", StringHelper.ArrayToUpperCase(UserIDList)) + "');";

            resultSet = mDatabase.query(ConversationsDBHelper.TABLE_CONVERSATIONS,
                    columns, selection, null, null, null, null);


            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                Conversations conversation = GetConversationFromResultSet(resultSet);
                CachedConversations.add(conversation);
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    conversation = GetConversationFromResultSet(resultSet);
                    CachedConversations.add(conversation);
                }
            }
        } catch (Exception ex) {
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
        return CachedConversations;
    }

    public Conversations GetConversationFromCache(String LoggedInUserID, String ConvWithUserID) {
        String[] columns = {ConversationsDBHelper.COLUMN_LOGGEDINUSERID, ConversationsDBHelper.COLUMN_CONVWITHUSERID,
                ConversationsDBHelper.COLUMN_USERNAME, ConversationsDBHelper.COLUMN_PICID, ConversationsDBHelper.COLUMN_PROFILEPIC,
                ConversationsDBHelper.COLUMN_LASTMESSAGEDT, ConversationsDBHelper.COLUMN_SENDERTYPE, ConversationsDBHelper.COLUMN_UNREADCOUNT,
                ConversationsDBHelper.COLUMN_LASTMESSAGELOCALDT};
        String[] selectionArgs = {LoggedInUserID, ConvWithUserID, "T"};
        Conversations conversations = null;
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.query(ConversationsDBHelper.TABLE_CONVERSATIONS,
                    columns, ConversationsDBHelper.COLUMN_LOGGEDINUSERID + "=? COLLATE NOCASE AND "
                            + ConversationsDBHelper.COLUMN_CONVWITHUSERID + "=? COLLATE NOCASE AND "
                            + ConversationsDBHelper.COLUMN_MARKEDDELETE + "<>?",
                    selectionArgs, null, null, null);
            if (resultSet != null && resultSet.moveToFirst()) {
                conversations = GetConversationFromResultSet(resultSet);
            }
        } catch (Exception ex) {
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
        return conversations;
    }

    private Conversations GetConversationFromResultSet(Cursor resultSet) {
        Conversations conversations = null;
        String ConvWithUserID = resultSet.getString(resultSet.getColumnIndex(ConversationsDBHelper.COLUMN_CONVWITHUSERID));
        String UserName = resultSet.getString(resultSet.getColumnIndex(ConversationsDBHelper.COLUMN_USERNAME));
        String PicID = resultSet.getString(resultSet.getColumnIndex(ConversationsDBHelper.COLUMN_PICID));
        String ProfilePicMini = resultSet.getString(resultSet.getColumnIndex(ConversationsDBHelper.COLUMN_PROFILEPIC));
        String LastMessageDT = resultSet.getString(resultSet.getColumnIndex(ConversationsDBHelper.COLUMN_LASTMESSAGEDT));
        String SenderType = resultSet.getString(resultSet.getColumnIndex(ConversationsDBHelper.COLUMN_SENDERTYPE));
        String UnreadCount = resultSet.getString(resultSet.getColumnIndex(ConversationsDBHelper.COLUMN_UNREADCOUNT));
        String LastMessageLocalDT = resultSet.getString(resultSet.getColumnIndex(ConversationsDBHelper.COLUMN_LASTMESSAGELOCALDT));
        conversations = new Conversations(ConvWithUserID, UserName, PicID, ProfilePicMini, LastMessageDT, SenderType,
                UnreadCount, false, null, LastMessageLocalDT);
        return conversations;
    }


    public void AddConversationsListToCache(ArrayList<Conversations> ConversationsList, String LoggedInUserID) {
        for (int i = 0; i < ConversationsList.size(); i++) {
            AddConversationToCache(LoggedInUserID, ConversationsList.get(i).UserID, StringEncoderHelper.encodeURIComponent(ConversationsList.get(i).UserName),
                    ConversationsList.get(i).PicID, ConversationsList.get(i).ProfilePicMini, ConversationsList.get(i).LastMessageDT,
                    ConversationsList.get(i).SenderType, ConversationsList.get(i).UnreadCount, ConversationsList.get(i).LastMessageLocalDT);
        }
    }

    public void AddConversationToCache(String LoggedInUserID, String ConvWithUserID, String UserName, String PicID,
                                       String ProfilePicMini, String LastMessageDT, String SenderType,
                                       String UnreadCount, String LastMessageLocalDT) {
//        if (ConversationExistsInCache(UserID)) {
//            return;
//        }


        if (PicID == null) {
            PicID = "";
        }
//        if (ProfilePicMini == null) {
//            ProfilePicMini = "";
//        }
//        try {
//            if (ProfilePicMini.trim().equals("") && !PicID.trim().equals("")) {
//                GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(mContext);
//                ProfilePicMini = gdImageDBHelper.GetImageStringByPicID(PicID, false);
//                if (ProfilePicMini == null) {
//                    ProfilePicMini = "";
//                }
//            }
//        } catch (Exception ex) {
//            GDLogHelper.LogException(ex);
//        }
        ProfilePicMini = "";
        try {
            mDatabase = mHelper.getWritableDatabase();
            String sql = "INSERT or replace INTO " + ConversationsDBHelper.TABLE_CONVERSATIONS + " VALUES (?,?,?,?,?,?,?,?,?,?);";
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.bindString(1, LoggedInUserID.toUpperCase());
            statement.bindString(2, ConvWithUserID.toUpperCase());
            statement.bindString(3, UserName);
            statement.bindString(4, PicID.toUpperCase());
            statement.bindString(5, ProfilePicMini);
            statement.bindString(6, LastMessageDT);
            statement.bindString(7, SenderType);
            statement.bindString(8, UnreadCount);
            statement.bindString(9, LastMessageLocalDT);
            statement.bindString(10, "F");
            statement.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    public void UpdateLastDTForAllConversationsFromMessages(String LoggedInUserID) {
        ArrayList<Conversations> ConversationsList = GetAllConversationsFromCache(LoggedInUserID);
        if (ConversationsList == null || ConversationsList.size() == 0) {
            return;
        }
        try {
            mDatabase = mHelper.getWritableDatabase();
            GDMessagesDBHelper gdMessagesDBHelper = new GDMessagesDBHelper(mContext);
            String LastMessageDTForConversation = "";
            for (int i = 0; i < ConversationsList.size(); i++) {
                LastMessageDTForConversation = gdMessagesDBHelper.GetLastMessageDTForConversation(LoggedInUserID, ConversationsList.get(i).UserID);
                if (LastMessageDTForConversation != null && !LastMessageDTForConversation.equals("")) {
                    String sql = "UPDATE " + ConversationsDBHelper.TABLE_CONVERSATIONS + " SET " +
                            ConversationsDBHelper.COLUMN_LASTMESSAGELOCALDT + " = '" + LastMessageDTForConversation + "' COLLATE NOCASE WHERE " +
                            ConversationsDBHelper.COLUMN_CONVWITHUSERID + " = ('" + ConversationsList.get(i).UserID + "' COLLATE NOCASE);";
                    SQLiteStatement statement = mDatabase.compileStatement(sql);
                    statement.execute();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    public void UpdateLastDTForConversations(String LoggedInUserID, String ConvWithUserID, String LastDT, Boolean GetLastDTFromMessages) {
        if (ConvWithUserID == null || ConvWithUserID.trim().equals("")) {
            return;
        }
        try {
            if (GetLastDTFromMessages) {
                GDMessagesDBHelper gdMessagesDBHelper = new GDMessagesDBHelper(mContext);
                LastDT = gdMessagesDBHelper.GetLastMessageDTForConversation(LoggedInUserID, ConvWithUserID);
            }
            if (LastDT == null || LastDT.trim().equals("")) {
                return;
            }
            mDatabase = mHelper.getWritableDatabase();
            String sql = "UPDATE " + ConversationsDBHelper.TABLE_CONVERSATIONS + " SET " +
                    ConversationsDBHelper.COLUMN_LASTMESSAGELOCALDT + " = '" + LastDT + "' COLLATE NOCASE WHERE " +
                    ConversationsDBHelper.COLUMN_CONVWITHUSERID + " = ('" + ConvWithUserID + "' COLLATE NOCASE);";
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    public void ClearDataForUser(String LoggedInUserID) {
        try {
            mDatabase = mHelper.getWritableDatabase();
            String sql = "DELETE FROM " + ConversationsDBHelper.TABLE_CONVERSATIONS + " WHERE " +
                    ConversationsDBHelper.COLUMN_LOGGEDINUSERID + "=" + LoggedInUserID + " COLLATE NOCASE ;";
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    public Boolean ConversationExistsInCache(String LoggedInUserID, String ConvWithUserID) {
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            String[] columns = {ConversationsDBHelper.COLUMN_LOGGEDINUSERID, ConversationsDBHelper.COLUMN_CONVWITHUSERID};
            String[] selectionArgs = {LoggedInUserID, ConvWithUserID, "T"};
            resultSet = mDatabase.query(ConversationsDBHelper.TABLE_CONVERSATIONS, columns,
                    ConversationsDBHelper.COLUMN_LOGGEDINUSERID + "=? COLLATE NOCASE AND "
                            + ConversationsDBHelper.COLUMN_CONVWITHUSERID + "=? COLLATE NOCASE AND "
                            + ConversationsDBHelper.COLUMN_MARKEDDELETE + "<>?",
                    selectionArgs, null, null, null);
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

    public Boolean DeleteConversation(String LoggedInUserID, String ConvWithUserID, Boolean OnlyMarkDelete) {
        try {
            mDatabase = mHelper.getWritableDatabase();
            if (OnlyMarkDelete) {
                String sql = "UPDATE " + ConversationsDBHelper.TABLE_CONVERSATIONS + " SET "
                        + ConversationsDBHelper.COLUMN_MARKEDDELETE + " = 'T' WHERE " +
                        ConversationsDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE  AND " +
                        ConversationsDBHelper.COLUMN_CONVWITHUSERID + "='" + ConvWithUserID + "' COLLATE NOCASE ;";
                SQLiteStatement statement = mDatabase.compileStatement(sql);
                statement.execute();
                try {
                    GDMessagesDBHelper gdMessagesDBHelper = new GDMessagesDBHelper(mContext);
                    gdMessagesDBHelper.ClearMessageListFromCache(LoggedInUserID, ConvWithUserID);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    GDLogHelper.LogException(ex);
                }
            } else {
                String sql = "DELETE FROM " + ConversationsDBHelper.TABLE_CONVERSATIONS + " WHERE " +
                        ConversationsDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' AND " +
                        ConversationsDBHelper.COLUMN_CONVWITHUSERID + "='" + ConvWithUserID + "' AND " +
                        ConversationsDBHelper.COLUMN_MARKEDDELETE + "='T';";
                SQLiteStatement statement = mDatabase.compileStatement(sql);
                statement.execute();
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return false;
        } finally {
            CloseDB();
        }
    }

    public ArrayList<String> GetDeletionMarkedConversations(String LoggedInUserID) {
        String[] columns = {ConversationsDBHelper.COLUMN_LOGGEDINUSERID, ConversationsDBHelper.COLUMN_CONVWITHUSERID};
        ArrayList<String> DeletionMarkedConversations = new ArrayList<>();
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            String[] selectionArgs = {LoggedInUserID, "T"};
            resultSet = mDatabase.query(ConversationsDBHelper.TABLE_CONVERSATIONS,
                    columns, ConversationsDBHelper.COLUMN_LOGGEDINUSERID + "=? COLLATE NOCASE AND "
                            + ConversationsDBHelper.COLUMN_MARKEDDELETE + "=?",
                    selectionArgs, null, null, null);

            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();

                DeletionMarkedConversations.add(resultSet.getString(resultSet.getColumnIndex(ConversationsDBHelper.COLUMN_CONVWITHUSERID)));
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    DeletionMarkedConversations.add(resultSet.getString(resultSet.getColumnIndex(ConversationsDBHelper.COLUMN_CONVWITHUSERID)));
                }
            }
        } catch (Exception ex) {
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
        return DeletionMarkedConversations;
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


    private static class ConversationsDBHelper extends SQLiteOpenHelper {
        private static final int DB_VERSION = 3;
        private static final String DB_NAME = "GDConversations_db";

        public static final String TABLE_CONVERSATIONS = "CachedConversations";

        public static final String COLUMN_LOGGEDINUSERID = "LoggedInUserID";
        public static final String COLUMN_CONVWITHUSERID = "ConvWithUserID";
        public static final String COLUMN_USERNAME = "UserName";
        public static final String COLUMN_PICID = "PicID";
        public static final String COLUMN_PROFILEPIC = "ProfilePic";
        public static final String COLUMN_LASTMESSAGEDT = "LastMessageDT";
        public static final String COLUMN_SENDERTYPE = "SenderType";
        public static final String COLUMN_UNREADCOUNT = "UnreadCount";
        public static final String COLUMN_LASTMESSAGELOCALDT = "LastMessageLocalDT";
        public static final String COLUMN_MARKEDDELETE = "MarkedDelete";

        private static final String ColumnsSql = COLUMN_LOGGEDINUSERID + " TEXT," +
                COLUMN_CONVWITHUSERID + " TEXT," +
                COLUMN_USERNAME + " TEXT," +
                COLUMN_PICID + " TEXT," +
                COLUMN_PROFILEPIC + " TEXT," +
                COLUMN_LASTMESSAGEDT + " TEXT," +
                COLUMN_SENDERTYPE + " TEXT," +
                COLUMN_UNREADCOUNT + " TEXT," +
                COLUMN_LASTMESSAGELOCALDT + " TEXT," +
                COLUMN_MARKEDDELETE + " TEXT";
        private static final String CREATE_TABLE_CACHEDCONVERSATIONS = "CREATE TABLE " + TABLE_CONVERSATIONS + " (" +
                ColumnsSql + "," +
                "PRIMARY KEY (" + COLUMN_LOGGEDINUSERID + ", " + COLUMN_CONVWITHUSERID + "));";

        public ConversationsDBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE_CACHEDCONVERSATIONS);
            } catch (SQLiteException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                if (oldVersion == 2 && newVersion == 3) {
                    //Table recreation needed as 'IsAdmin' column was deleted smometime back and older users get exception on inserting.
                    String recreateTableCommand = "CREATE TEMPORARY TABLE conversations_backup(" + ColumnsSql + ");";
                    recreateTableCommand += "INSERT INTO conversations_backup SELECT " + COLUMN_LOGGEDINUSERID + "," + COLUMN_CONVWITHUSERID +
                            "," + COLUMN_USERNAME + "," + COLUMN_PICID + "," + COLUMN_PROFILEPIC + "," + COLUMN_LASTMESSAGEDT + "," + COLUMN_SENDERTYPE
                            + "," + COLUMN_UNREADCOUNT + "," + COLUMN_LASTMESSAGELOCALDT + "," + COLUMN_MARKEDDELETE + " FROM TABLE_CONVERSATIONS;";
                    recreateTableCommand += "DROP TABLE " + TABLE_CONVERSATIONS + ";";
                    recreateTableCommand += CREATE_TABLE_CACHEDCONVERSATIONS;
                    recreateTableCommand += "INSERT INTO" + TABLE_CONVERSATIONS + "SELECT " + COLUMN_LOGGEDINUSERID + "," + COLUMN_CONVWITHUSERID +
                            "," + COLUMN_USERNAME + "," + COLUMN_PICID + "," + COLUMN_PROFILEPIC + "," + COLUMN_LASTMESSAGEDT + "," + COLUMN_SENDERTYPE
                            + "," + COLUMN_UNREADCOUNT + "," + COLUMN_LASTMESSAGELOCALDT + "," + COLUMN_MARKEDDELETE + " FROM conversations_backup;";
                    recreateTableCommand += "DROP TABLE conversations_backup;";

                    db.beginTransaction();
                    db.execSQL(recreateTableCommand);
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
            } catch (SQLiteException ex) {
                GDLogHelper.LogException(ex);
            }
        }
    }
}
