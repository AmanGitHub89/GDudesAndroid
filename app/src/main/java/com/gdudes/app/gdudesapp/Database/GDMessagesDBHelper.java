package com.gdudes.app.gdudesapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Pair;

import com.gdudes.app.gdudesapp.GDTypes.APIMajor.zMessageIDAndDT;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.zMessageStatusUpdate;
import com.gdudes.app.gdudesapp.GDTypes.GDMessage;
import com.gdudes.app.gdudesapp.Helpers.GDDateTimeHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;

import java.util.ArrayList;

public class GDMessagesDBHelper {
    private MessagesDBHelper mHelper;
    private SQLiteDatabase mDatabase;
    //Time in hours after which all messages are deleted from server.
    // Messages sent/recieved before this time need not be queried for status updates.
    public static final int MessageServerCommThresholdHours = 73;

    public GDMessagesDBHelper(Context context) {
        mHelper = new MessagesDBHelper(context);
    }


    ///////////////////////////////////////////////////////////
    public ArrayList<GDMessage> GetMessagesForConversation(String LoggedInUserID, String ConversationWithUserID) {
        String[] columns = {MessagesDBHelper.COLUMN_MESSAGEID, MessagesDBHelper.COLUMN_SENDERID, MessagesDBHelper.COLUMN_SENDERNAME,
                MessagesDBHelper.COLUMN_RECIEVERID, MessagesDBHelper.COLUMN_MESSAGETEXT, MessagesDBHelper.COLUMN_ATTACHEDPICIDS,
                MessagesDBHelper.COLUMN_LOCATION, MessagesDBHelper.COLUMN_ATTACHEDFILEPATH,
                MessagesDBHelper.COLUMN_MESSAGESTATUS, MessagesDBHelper.COLUMN_SENTDATETIME,
                MessagesDBHelper.COLUMN_READDATETIME, MessagesDBHelper.COLUMN_ISSENTTIMEUPDATEDFROMSERVER};
        ArrayList<GDMessage> CachedConversations = new ArrayList<>();
        String[] selectionArgs = {LoggedInUserID, ConversationWithUserID, "X"};
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.query(MessagesDBHelper.TABLE_MESSAGES,
                    columns, MessagesDBHelper.COLUMN_LOGGEDINUSERID + "=? COLLATE NOCASE AND "
                            + MessagesDBHelper.COLUMN_CONVWITHUSERID + "=? COLLATE NOCASE AND "
                            + MessagesDBHelper.COLUMN_MESSAGESTATUS + "<>?",
                    selectionArgs, null, null, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                GDMessage gdMessage = GetMessageFromResultSet(resultSet);
                if (gdMessage.SenderID.equalsIgnoreCase(ConversationWithUserID)) {
                    gdMessage.Direction = 0;
                } else {
                    gdMessage.Direction = 1;
                }
                CachedConversations.add(gdMessage);
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    gdMessage = GetMessageFromResultSet(resultSet);
                    if (gdMessage.SenderID.equalsIgnoreCase(ConversationWithUserID)) {
                        gdMessage.Direction = 0;
                    } else {
                        gdMessage.Direction = 1;
                    }
                    CachedConversations.add(gdMessage);
                }
            }
        } catch (Exception ex) {
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

    public ArrayList<GDMessage> GetPendingMessages(String LoggedInUserID) {
        String[] columns = {MessagesDBHelper.COLUMN_LOGGEDINUSERID, MessagesDBHelper.COLUMN_MESSAGEID, MessagesDBHelper.COLUMN_SENDERID,
                MessagesDBHelper.COLUMN_SENDERNAME, MessagesDBHelper.COLUMN_RECIEVERID, MessagesDBHelper.COLUMN_MESSAGETEXT,
                MessagesDBHelper.COLUMN_ATTACHEDPICIDS, MessagesDBHelper.COLUMN_LOCATION, MessagesDBHelper.COLUMN_ATTACHEDFILEPATH,
                MessagesDBHelper.COLUMN_MESSAGESTATUS, MessagesDBHelper.COLUMN_SENTDATETIME,
                MessagesDBHelper.COLUMN_READDATETIME, MessagesDBHelper.COLUMN_ISSENTTIMEUPDATEDFROMSERVER};
        ArrayList<GDMessage> CachedConversations = new ArrayList<>();
        String[] selectionArgs = {LoggedInUserID, LoggedInUserID, "P", ""};
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.query(MessagesDBHelper.TABLE_MESSAGES,
                    columns,
                    MessagesDBHelper.COLUMN_LOGGEDINUSERID + "=? COLLATE NOCASE AND "
                            + MessagesDBHelper.COLUMN_SENDERID + "=? COLLATE NOCASE AND "
                            + MessagesDBHelper.COLUMN_MESSAGESTATUS + "=? AND "
                            + MessagesDBHelper.COLUMN_ATTACHEDFILEPATH + "=?",
                    selectionArgs, null, null, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                GDMessage gdMessage = GetMessageFromResultSet(resultSet);
                CachedConversations.add(gdMessage);
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    gdMessage = GetMessageFromResultSet(resultSet);
                    CachedConversations.add(gdMessage);
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

    public ArrayList<GDMessage> GetFirstNUnreadInboundMessages(String LoggedInUserID, int Count) {
        ArrayList<GDMessage> CachedConversations = new ArrayList<>();
//        String[] selectionArgs = {LoggedInUserID, LoggedInUserID, "R", "X"};
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            String sql = "SELECT * FROM " + MessagesDBHelper.TABLE_MESSAGES + " WHERE " +
                    MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_RECIEVERID + "='" + LoggedInUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_MESSAGESTATUS + " NOT IN ('R','r','X') ORDER BY " +
                    MessagesDBHelper.COLUMN_SENTDATETIME + " DESC LIMIT " + Integer.toString(Count) + ";";
            resultSet = mDatabase.rawQuery(sql, null);

            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                GDMessage gdMessage = GetMessageFromResultSet(resultSet);
                CachedConversations.add(gdMessage);
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    gdMessage = GetMessageFromResultSet(resultSet);
                    CachedConversations.add(gdMessage);
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

    public ArrayList<Pair<String, String>> GetMessageIDsAndAttachFilePathsForDirectPicDownload(String LoggedInUserID) {
        String[] columns = {MessagesDBHelper.COLUMN_MESSAGEID, MessagesDBHelper.COLUMN_ATTACHEDFILEPATH};
        ArrayList<Pair<String, String>> MessageIDAndPathsList = new ArrayList<>();
        String[] selectionArgs = {LoggedInUserID, "X", ""};
        Cursor resultSet = null;
        try {
            String sTimeNow = GDDateTimeHelper.GetCurrentDateTimeAsString(true);
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.query(MessagesDBHelper.TABLE_MESSAGES,
                    columns,
                    MessagesDBHelper.COLUMN_LOGGEDINUSERID + "=? COLLATE NOCASE AND "
                            + MessagesDBHelper.COLUMN_MESSAGESTATUS + "<>? AND "
                            + MessagesDBHelper.COLUMN_ATTACHEDFILEPATH + "<>? AND "
                            + "((julianday('" + sTimeNow + "') - julianday(" + MessagesDBHelper.COLUMN_SENTDATETIME + ")) * 24) < "
                            + Integer.toString(MessageServerCommThresholdHours),
                    selectionArgs, null, null, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();

                int messageIDIndex = resultSet.getColumnIndex(MessagesDBHelper.COLUMN_MESSAGEID);
                int attachedPathIndex = resultSet.getColumnIndex(MessagesDBHelper.COLUMN_ATTACHEDFILEPATH);
                MessageIDAndPathsList.add(new Pair<>(resultSet.getString(messageIDIndex), resultSet.getString(attachedPathIndex)));
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    MessageIDAndPathsList.add(new Pair<>(resultSet.getString(messageIDIndex), resultSet.getString(attachedPathIndex)));
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
        return MessageIDAndPathsList;
    }

    public ArrayList<GDMessage> GetUnreadSentMessages(String LoggedInUserID) {
        String[] columns = {MessagesDBHelper.COLUMN_LOGGEDINUSERID, MessagesDBHelper.COLUMN_MESSAGEID, MessagesDBHelper.COLUMN_SENDERID,
                MessagesDBHelper.COLUMN_SENDERNAME, MessagesDBHelper.COLUMN_RECIEVERID, MessagesDBHelper.COLUMN_MESSAGETEXT,
                MessagesDBHelper.COLUMN_ATTACHEDPICIDS, MessagesDBHelper.COLUMN_LOCATION, MessagesDBHelper.COLUMN_ATTACHEDFILEPATH,
                MessagesDBHelper.COLUMN_MESSAGESTATUS, MessagesDBHelper.COLUMN_SENTDATETIME,
                MessagesDBHelper.COLUMN_READDATETIME, MessagesDBHelper.COLUMN_ISSENTTIMEUPDATEDFROMSERVER};
        ArrayList<GDMessage> CachedConversations = new ArrayList<>();
        String[] selectionArgs = {LoggedInUserID, LoggedInUserID, "R", "X"};
        Cursor resultSet = null;
        try {
            String sTimeNow = GDDateTimeHelper.GetCurrentDateTimeAsString(true);
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.query(MessagesDBHelper.TABLE_MESSAGES,
                    columns,
                    MessagesDBHelper.COLUMN_LOGGEDINUSERID + "=? COLLATE NOCASE AND "
                            + MessagesDBHelper.COLUMN_SENDERID + "=? COLLATE NOCASE AND "
                            + MessagesDBHelper.COLUMN_MESSAGESTATUS + "<>? COLLATE NOCASE AND "
                            + MessagesDBHelper.COLUMN_MESSAGESTATUS + "<>? " +
                            "AND ((julianday('" + sTimeNow + "') - julianday(" + MessagesDBHelper.COLUMN_SENTDATETIME + ")) * 24) < "
                            + Integer.toString(MessageServerCommThresholdHours),
                    selectionArgs, null, null, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                GDMessage gdMessage = GetMessageFromResultSet(resultSet);
                CachedConversations.add(gdMessage);
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    gdMessage = GetMessageFromResultSet(resultSet);
                    CachedConversations.add(gdMessage);
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

    public ArrayList<GDMessage> GetMessagesByMessageIDList(String LoggedInUserID, ArrayList<String> MessageIDList) {
        String[] columns = {MessagesDBHelper.COLUMN_LOGGEDINUSERID, MessagesDBHelper.COLUMN_MESSAGEID, MessagesDBHelper.COLUMN_SENDERID,
                MessagesDBHelper.COLUMN_SENDERNAME, MessagesDBHelper.COLUMN_RECIEVERID, MessagesDBHelper.COLUMN_MESSAGETEXT,
                MessagesDBHelper.COLUMN_ATTACHEDPICIDS, MessagesDBHelper.COLUMN_LOCATION, MessagesDBHelper.COLUMN_ATTACHEDFILEPATH,
                MessagesDBHelper.COLUMN_MESSAGESTATUS, MessagesDBHelper.COLUMN_SENTDATETIME,
                MessagesDBHelper.COLUMN_READDATETIME, MessagesDBHelper.COLUMN_ISSENTTIMEUPDATEDFROMSERVER};
        ArrayList<GDMessage> CachedConversations = new ArrayList<>();
        Cursor resultSet = null;
        try {
            String selection = MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE  AND " +
                    MessagesDBHelper.COLUMN_MESSAGEID + " IN ('" + TextUtils.join("','", StringHelper.ArrayToUpperCase(MessageIDList)) + "');";
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.query(MessagesDBHelper.TABLE_MESSAGES,
                    columns, selection, null, null, null, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                GDMessage gdMessage = GetMessageFromResultSet(resultSet);
                if (gdMessage.SenderID.equalsIgnoreCase(LoggedInUserID)) {
                    gdMessage.Direction = 1;
                } else {
                    gdMessage.Direction = 0;
                }
                CachedConversations.add(gdMessage);
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    gdMessage = GetMessageFromResultSet(resultSet);
                    if (gdMessage.SenderID.equalsIgnoreCase(LoggedInUserID)) {
                        gdMessage.Direction = 1;
                    } else {
                        gdMessage.Direction = 0;
                    }
                    CachedConversations.add(gdMessage);
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

    private GDMessage GetMessageFromResultSet(Cursor resultSet) {
        GDMessage gdMessage = null;
        String MessageID = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_MESSAGEID));
        String SenderID = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_SENDERID));
        String SenderName = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_SENDERNAME));
        String RecieverID = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_RECIEVERID));
        String MessageText = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_MESSAGETEXT));
        String AttachedPicIDs = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_ATTACHEDPICIDS));
        String Location = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_LOCATION));
        String AttachedFilePath = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_ATTACHEDFILEPATH));
        String MessageStatus = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_MESSAGESTATUS));
        String SentDateTime = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_SENTDATETIME));
        String ReadDateTime = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_READDATETIME));
        Boolean IsSentTimeUpdatedFromServer = Boolean.parseBoolean(resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_ISSENTTIMEUPDATEDFROMSERVER)));

        gdMessage = new GDMessage(MessageID, SenderID, SenderName, RecieverID, MessageText, AttachedPicIDs,
                Location, AttachedFilePath, "", MessageStatus, SentDateTime,
                ReadDateTime, IsSentTimeUpdatedFromServer);
        return gdMessage;
    }

    public ArrayList<String> GetMessageIDListAfterTime(String LoggedInUserID, String FromDateTime) {
        String[] columns = {MessagesDBHelper.COLUMN_MESSAGEID};
        ArrayList<String> CachedMessageIDs = new ArrayList<>();
        Cursor resultSet = null;
        try {
            String selection = MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE  AND " +
                    MessagesDBHelper.COLUMN_SENTDATETIME + " > '" + FromDateTime + "';";
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.query(MessagesDBHelper.TABLE_MESSAGES,
                    columns, selection, null, null, null, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                CachedMessageIDs.add(resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_MESSAGEID)));
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    CachedMessageIDs.add(resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_MESSAGEID)));
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
        return CachedMessageIDs;
    }

    public ArrayList<String> GetMessageIDListAfterTimeForConversation(String LoggedInUserID,
                                                                      String FromDateTime, String convWithUserID) {
        String[] columns = {MessagesDBHelper.COLUMN_MESSAGEID};
        ArrayList<String> CachedMessageIDs = new ArrayList<>();
        Cursor resultSet = null;
        try {
            String selection = MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE  AND " +
                    MessagesDBHelper.COLUMN_CONVWITHUSERID + "='" + convWithUserID + "' COLLATE NOCASE  AND " +
                    MessagesDBHelper.COLUMN_SENTDATETIME + " > '" + FromDateTime + "';";
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.query(MessagesDBHelper.TABLE_MESSAGES,
                    columns, selection, null, null, null, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                CachedMessageIDs.add(resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_MESSAGEID)));
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    CachedMessageIDs.add(resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_MESSAGEID)));
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
        return CachedMessageIDs;
    }

    public GDMessage GetLastMessageForConversation(String LoggedInUserID, String ConversationWithUserID) {
        Cursor resultSet = null;
        try {
            String RawQuery = "SELECT * FROM " + MessagesDBHelper.TABLE_MESSAGES + " WHERE "
                    + MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_CONVWITHUSERID + " = '" + ConversationWithUserID + "' COLLATE NOCASE ORDER BY "
                    + MessagesDBHelper.COLUMN_SENTDATETIME + " DESC LIMIT 1;";
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.rawQuery(RawQuery, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                return GetMessageFromResultSet(resultSet);
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return null;
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (Exception ee) {
                }
            }
            CloseDB();
        }
    }

    public String GetLastMessageDTForConversation(String LoggedInUserID, String ConversationWithUserID) {
        Cursor resultSet = null;
        try {
            String RawQuery = "SELECT " + MessagesDBHelper.COLUMN_SENTDATETIME + " FROM " + MessagesDBHelper.TABLE_MESSAGES + " WHERE "
                    + MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_CONVWITHUSERID + " = '" + ConversationWithUserID + "' COLLATE NOCASE ORDER BY "
                    + MessagesDBHelper.COLUMN_SENTDATETIME + " DESC LIMIT 1;";
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.rawQuery(RawQuery, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                return resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_SENTDATETIME));
            }
            return "";
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (Exception ee) {
                }
            }
            CloseDB();
        }
    }


    ///////////////////////////////////////////////////////////
    public Boolean AddMessageToCache(String LoggedInUserID, String MessageID, String ConvWithUserID, String SenderID,
                                     String SenderName, String RecieverID, String MessageText, String AttachedPicIDs,
                                     String Location, String AttachedFilePath, String AttachedFileSrc, String MessageStatus,
                                     String SentDateTime, String ReadDateTime, Boolean IsSentTimeUpdatedFromServer) {
        try {
            mDatabase = mHelper.getWritableDatabase();
            String sql = "INSERT or replace INTO " + MessagesDBHelper.TABLE_MESSAGES + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.bindString(1, LoggedInUserID.toUpperCase());
            statement.bindString(2, MessageID.toUpperCase());
            statement.bindString(3, ConvWithUserID.toUpperCase());
            statement.bindString(4, SenderID.toUpperCase());
            statement.bindString(5, SenderName);
            statement.bindString(6, RecieverID.toUpperCase());
            statement.bindString(7, MessageText);
            statement.bindString(8, AttachedPicIDs == null ? "" : AttachedPicIDs);
            statement.bindString(9, Location == null ? "" : Location);
            statement.bindString(10, AttachedFilePath == null ? "" : AttachedFilePath);
            statement.bindString(11, AttachedFileSrc == null ? "" : AttachedFileSrc);
            statement.bindString(12, "F");
            statement.bindString(13, "F");
            statement.bindString(14, "F");
            statement.bindString(15, "F");
            statement.bindString(16, MessageStatus);
            statement.bindString(17, SentDateTime == null ? "" : SentDateTime);
            statement.bindString(18, ReadDateTime == null ? "" : ReadDateTime);
            statement.bindString(19, IsSentTimeUpdatedFromServer.toString().toLowerCase());
            statement.execute();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return false;
        } finally {
            CloseDB();
        }
    }

    public Boolean MessageExistsInCache(String MessageID) {
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            String[] columns = {MessagesDBHelper.COLUMN_MESSAGEID};
            String[] selectionArgs = {MessageID};
            resultSet = mDatabase.query(MessagesDBHelper.TABLE_MESSAGES, columns,
                    MessagesDBHelper.COLUMN_MESSAGEID + "=? COLLATE NOCASE", selectionArgs, null, null, null);
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

    ///////////////////////////////////////////////////////////
    public void ClearMessageListFromCache(String LoggedInUserID, String ConversationWithUserID) {
        try {
            mDatabase = mHelper.getWritableDatabase();
            String sql = "DELETE FROM " + MessagesDBHelper.TABLE_MESSAGES + " WHERE " +
                    MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE  AND " +
                    MessagesDBHelper.COLUMN_CONVWITHUSERID + "='" + ConversationWithUserID + "' COLLATE NOCASE ;";
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    public void DeleteMessagesByMessageIDList(ArrayList<String> MessagesToDelete) {
        try {
            mDatabase = mHelper.getWritableDatabase();
            String sql = "DELETE FROM " + MessagesDBHelper.TABLE_MESSAGES + " WHERE " +
                    MessagesDBHelper.COLUMN_MESSAGEID + " IN ('" + TextUtils.join("','", StringHelper.ArrayToUpperCase(MessagesToDelete)) + "');";
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    ///////////////////////////////////////////////////////////
    public void UpdateMessagesListStatusForSent(String LoggedInUserID, ArrayList<zMessageIDAndDT> MessagesIDAndTimeList) {
        try {
            ArrayList<zMessageIDAndDT> MessagesIDAndTimeListToUpdate = new ArrayList<>();
            String CurrentMessageStatus = "";
            for (int i = 0; i < MessagesIDAndTimeList.size(); i++) {
                CurrentMessageStatus = GetMessageStatus(MessagesIDAndTimeList.get(i).MessageID);
                if (CurrentMessageStatus.equals("P")) {
                    MessagesIDAndTimeListToUpdate.add(MessagesIDAndTimeList.get(i));
                }
            }

            mDatabase = mHelper.getWritableDatabase();
            ContentValues cv;
            String WhereArgs[] = new String[1];
            for (int i = 0; i < MessagesIDAndTimeListToUpdate.size(); i++) {
                cv = new ContentValues();
                cv.put(MessagesDBHelper.COLUMN_MESSAGESTATUS, "S");
                cv.put(MessagesDBHelper.COLUMN_SENTDATETIME, MessagesIDAndTimeListToUpdate.get(i).DT);
                cv.put(MessagesDBHelper.COLUMN_ISSENTTIMEUPDATEDFROMSERVER, "true");
                WhereArgs[0] = MessagesIDAndTimeListToUpdate.get(i).MessageID;
                mDatabase.update(MessagesDBHelper.TABLE_MESSAGES, cv, MessagesDBHelper.COLUMN_MESSAGEID + " = ? COLLATE NOCASE ", WhereArgs);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    public void UpdateMessageListStatusForError(ArrayList<GDMessage> MessageList) {
        try {
            ArrayList<String> MessagesIDUpdate = new ArrayList<>();
            String CurrentMessageStatus = "";
            for (int i = 0; i < MessageList.size(); i++) {
                CurrentMessageStatus = GetMessageStatus(MessageList.get(i).MessageID);
                if (CurrentMessageStatus.equals("P")) {
                    MessagesIDUpdate.add(MessageList.get(i).MessageID);
                }
            }

            mDatabase = mHelper.getWritableDatabase();
            ContentValues cv;
            String WhereArgs[] = new String[1];
            for (int i = 0; i < MessagesIDUpdate.size(); i++) {
                cv = new ContentValues();
                cv.put(MessagesDBHelper.COLUMN_MESSAGESTATUS, "E");
                WhereArgs[0] = MessagesIDUpdate.get(i);
                mDatabase.update(MessagesDBHelper.TABLE_MESSAGES, cv, MessagesDBHelper.COLUMN_MESSAGEID + " = ? COLLATE NOCASE", WhereArgs);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    public void UpdateMessageListStatusForErrorWithMessageIDList(ArrayList<String> MessageIDList) {
        try {
            ArrayList<String> MessagesIDUpdate = new ArrayList<>();
            String CurrentMessageStatus = "";
            for (int i = 0; i < MessageIDList.size(); i++) {
                CurrentMessageStatus = GetMessageStatus(MessageIDList.get(i));
                if (CurrentMessageStatus.equals("P")) {
                    MessagesIDUpdate.add(MessageIDList.get(i));
                }
            }

            mDatabase = mHelper.getWritableDatabase();
            ContentValues cv;
            String WhereArgs[] = new String[1];
            for (int i = 0; i < MessagesIDUpdate.size(); i++) {
                cv = new ContentValues();
                cv.put(MessagesDBHelper.COLUMN_MESSAGESTATUS, "E");
                WhereArgs[0] = MessagesIDUpdate.get(i);
                mDatabase.update(MessagesDBHelper.TABLE_MESSAGES, cv, MessagesDBHelper.COLUMN_MESSAGEID + " = ? COLLATE NOCASE", WhereArgs);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    public Boolean UpdateMessagesListStatusForDeleted(ArrayList<GDMessage> MessagesList) {
        try {
            mDatabase = mHelper.getWritableDatabase();
            ArrayList<String> MessageIDs = new ArrayList<>();
            for (GDMessage message : MessagesList) {
                MessageIDs.add(message.MessageID);
            }
            String sql = "UPDATE " + MessagesDBHelper.TABLE_MESSAGES + " SET " +
                    MessagesDBHelper.COLUMN_MESSAGESTATUS + " = 'X' WHERE " +
                    MessagesDBHelper.COLUMN_MESSAGEID + " IN ('" + TextUtils.join("','", StringHelper.ArrayToUpperCase(MessageIDs)) + "');";
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.execute();

//            ContentValues cv;
//            String WhereArgs[] = new String[1];
//            for (int i = 0; i < MessagesList.size(); i++) {
//                cv = new ContentValues();
//                cv.put(MessagesDBHelper.COLUMN_MESSAGESTATUS, "X");
//                WhereArgs[0] = MessagesList.get(i).first.MessageID;
//                mDatabase.update(MessagesDBHelper.TABLE_MESSAGES, cv, MessagesDBHelper.COLUMN_MESSAGEID + " = ?", WhereArgs);
//            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return false;
        } finally {
            CloseDB();
        }
    }

    public int UpdateMessagesListStatus(String LoggedInUserID, ArrayList<zMessageStatusUpdate> MessagesStatusList) {
        ArrayList<zMessageStatusUpdate> MessagesStatusListToUpdate = new ArrayList<>();
        try {
            String CurrentMessageStatus = "";
            for (int i = 0; i < MessagesStatusList.size(); i++) {
                CurrentMessageStatus = GetMessageStatus(MessagesStatusList.get(i).MessageID);
                if (!CurrentMessageStatus.equals("R")) {
                    if (CurrentMessageStatus.equals("D") && MessagesStatusList.get(i).MessageStatus.equals("R")) {
                        MessagesStatusListToUpdate.add(MessagesStatusList.get(i));
                    } else if (CurrentMessageStatus.equals("S") && (MessagesStatusList.get(i).MessageStatus.equals("R") ||
                            MessagesStatusList.get(i).MessageStatus.equals("D"))) {
                        MessagesStatusListToUpdate.add(MessagesStatusList.get(i));
                    } else if (CurrentMessageStatus.equals("P") && !MessagesStatusList.get(i).MessageStatus.equals("P")) {
                        MessagesStatusListToUpdate.add(MessagesStatusList.get(i));
                    }
                }
            }
            if (MessagesStatusListToUpdate.size() == 0) {
                return 0;
            }
            mDatabase = mHelper.getWritableDatabase();
            ContentValues cv;
            String WhereArgs[] = new String[1];
            for (int i = 0; i < MessagesStatusListToUpdate.size(); i++) {
                cv = new ContentValues();
                cv.put(MessagesDBHelper.COLUMN_MESSAGESTATUS, MessagesStatusListToUpdate.get(i).MessageStatus);
                if (MessagesStatusListToUpdate.get(i).SentDateTime != null && !MessagesStatusListToUpdate.get(i).SentDateTime.equals("")) {
                    cv.put(MessagesDBHelper.COLUMN_SENTDATETIME, MessagesStatusListToUpdate.get(i).SentDateTime);
                    cv.put(MessagesDBHelper.COLUMN_ISSENTTIMEUPDATEDFROMSERVER, "true");
                } else {
                    cv.put(MessagesDBHelper.COLUMN_SENTDATETIME, "");
                    cv.put(MessagesDBHelper.COLUMN_ISSENTTIMEUPDATEDFROMSERVER, "false");
                }

                if (MessagesStatusListToUpdate.get(i).ReadDateTime != null && !MessagesStatusListToUpdate.get(i).ReadDateTime.equals("")) {
                    cv.put(MessagesDBHelper.COLUMN_READDATETIME, MessagesStatusListToUpdate.get(i).ReadDateTime);
                } else {
                    cv.put(MessagesDBHelper.COLUMN_READDATETIME, "");
                }
                WhereArgs[0] = MessagesStatusListToUpdate.get(i).MessageID;
                mDatabase.update(MessagesDBHelper.TABLE_MESSAGES, cv, MessagesDBHelper.COLUMN_MESSAGEID + " = ? COLLATE NOCASE ", WhereArgs);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
        return MessagesStatusListToUpdate.size();
    }

    public void UpdatePendingPicMessagesToError(String LoggedInUserID) {
        try {
            String sql = "UPDATE " + MessagesDBHelper.TABLE_MESSAGES +
                    " SET " + MessagesDBHelper.COLUMN_MESSAGESTATUS + " = 'E'" +
                    " WHERE " + MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE"
                    + " AND " + MessagesDBHelper.COLUMN_SENDERID + "='" + LoggedInUserID + "' COLLATE NOCASE"
                    + " AND " + MessagesDBHelper.COLUMN_MESSAGESTATUS + "='P'"
                    + " AND " + MessagesDBHelper.COLUMN_ATTACHEDFILEPATH + "<>'';";
            mDatabase = mHelper.getWritableDatabase();
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.execute();
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    public void UpdateInboundMessagesListStatus(String LoggedInUserID, ArrayList<zMessageIDAndDT> MessageIDAndDTList, String MessageStatus) {
        try {
            if (!MessageStatus.equals("R") && !MessageStatus.equals("D") && !MessageStatus.equals("r")) {
                return;
            }
            ArrayList<zMessageIDAndDT> MessageIDAndDTListToUpdate = new ArrayList<>();
            String CurrentMessageStatus = "";
            if (MessageStatus.equals("D")) {
                for (int i = 0; i < MessageIDAndDTList.size(); i++) {
                    CurrentMessageStatus = GetMessageStatus(MessageIDAndDTList.get(i).MessageID);
                    if (!CurrentMessageStatus.equals("R") && !CurrentMessageStatus.equals("D") && !MessageStatus.equals("r")) {
                        MessageIDAndDTListToUpdate.add(MessageIDAndDTList.get(i));
                    }
                }
            } else {
                MessageIDAndDTListToUpdate.addAll(MessageIDAndDTList);
            }
            if (MessageIDAndDTListToUpdate.size() == 0) {
                return;
            }
            mDatabase = mHelper.getWritableDatabase();
            ContentValues cv = null;
            String WhereArgs[] = new String[2];
            WhereArgs[0] = LoggedInUserID;
            for (int i = 0; i < MessageIDAndDTListToUpdate.size(); i++) {
                cv = new ContentValues();
                cv.put(MessagesDBHelper.COLUMN_MESSAGESTATUS, MessageStatus);
                if (MessageStatus.equals("R")) {
                    cv.put(MessagesDBHelper.COLUMN_READDATETIME, MessageIDAndDTListToUpdate.get(i).DT);
                }
                WhereArgs[1] = MessageIDAndDTListToUpdate.get(i).MessageID;
                mDatabase.update(MessagesDBHelper.TABLE_MESSAGES, cv, MessagesDBHelper.COLUMN_LOGGEDINUSERID + "=? COLLATE NOCASE AND "
                        + MessagesDBHelper.COLUMN_MESSAGEID + " = ? COLLATE NOCASE ", WhereArgs);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        } finally {
            CloseDB();
        }
    }

    public Boolean MarkAllInboundMessagesLocallyRead(String LoggedInUserID) {
        try {
            String sql = "UPDATE " + MessagesDBHelper.TABLE_MESSAGES +
                    " SET " + MessagesDBHelper.COLUMN_MESSAGESTATUS + " = 'r', " + MessagesDBHelper.COLUMN_READDATETIME + " = ''" +
                    " WHERE " + MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE"
                    + " AND " + MessagesDBHelper.COLUMN_RECIEVERID + "='" + LoggedInUserID + "' COLLATE NOCASE;";
            mDatabase = mHelper.getWritableDatabase();
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            statement.execute();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return false;
        } finally {
            CloseDB();
        }
    }

    public Boolean UpdateCompressedDirectPicSource(String messageID, String compressedDirectPicSRc) {
        try {
            mDatabase = mHelper.getWritableDatabase();
            String sql = "UPDATE " + MessagesDBHelper.TABLE_MESSAGES + " SET " +
                    MessagesDBHelper.COLUMN_ATTACHEDFILESRC + " = '" + compressedDirectPicSRc + "' WHERE " +
                    MessagesDBHelper.COLUMN_MESSAGEID + " = '" + messageID + "' COLLATE NOCASE;";
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            int rowsUpdated = statement.executeUpdateDelete();
            return rowsUpdated > 0;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            return false;
        } finally {
            CloseDB();
        }
    }


    private String GetMessageStatus(String MessageID) {
        String MessageStatus = "";
        String[] columns = {MessagesDBHelper.COLUMN_MESSAGESTATUS};
        String[] selectionArgs = {MessageID};
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            resultSet = mDatabase.query(MessagesDBHelper.TABLE_MESSAGES,
                    columns,
                    MessagesDBHelper.COLUMN_MESSAGEID + "=? COLLATE NOCASE ",
                    selectionArgs, null, null, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                MessageStatus = resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_MESSAGESTATUS));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            MessageStatus = "";
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (Exception ee) {
                }
            }
            CloseDB();
        }
        return MessageStatus;
    }

    public int GetConversationUnreadInboundMessageCount(String LoggedInUserID, String ConvWithUserID) {
        int UnreadCount = 0;
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            String sql = "SELECT COUNT(1) FROM " + MessagesDBHelper.TABLE_MESSAGES + " WHERE " +
                    MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_CONVWITHUSERID + "='" + ConvWithUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_SENDERID + "='" + ConvWithUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_MESSAGESTATUS + " NOT IN ('R','r','X');";
            resultSet = mDatabase.rawQuery(sql, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                UnreadCount = resultSet.getInt(0);
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
        return UnreadCount;
    }

    public int GetAllUnreadInboundMessageCount(String LoggedInUserID) {
        int UnreadCount = 0;
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            String sql = "SELECT COUNT(1) FROM " + MessagesDBHelper.TABLE_MESSAGES + " WHERE " +
                    MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_RECIEVERID + "='" + LoggedInUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_MESSAGESTATUS + " NOT IN ('R','r','X');";
            resultSet = mDatabase.rawQuery(sql, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                UnreadCount = resultSet.getInt(0);
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
        return UnreadCount;
    }

    public ArrayList<String> GetAllOfflineReadMessageIDList(String LoggedInUserID) {
        ArrayList<String> OfflineReadMessageIDList = new ArrayList<>();
        Cursor resultSet = null;
        try {
            String sTimeNow = GDDateTimeHelper.GetCurrentDateTimeAsString(true);
            mDatabase = mHelper.getReadableDatabase();
            String sql = "SELECT " + MessagesDBHelper.COLUMN_MESSAGEID + " FROM " + MessagesDBHelper.TABLE_MESSAGES + " WHERE " +
                    MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_MESSAGESTATUS + " = 'r' " +
                    "AND ((julianday('" + sTimeNow + "') - julianday(" + MessagesDBHelper.COLUMN_SENTDATETIME + ")) * 24) < "
                    + Integer.toString(MessageServerCommThresholdHours) + ";";
            resultSet = mDatabase.rawQuery(sql, null);
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                OfflineReadMessageIDList.add(resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_MESSAGEID)));
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    OfflineReadMessageIDList.add(resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_MESSAGEID)));
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
        return OfflineReadMessageIDList;
    }

    public int GetChatCountWithUnreadMessages(String LoggedInUserID) {
        ArrayList<GDMessage> CachedConversations = new ArrayList<>();
        String[] selectionArgs = {LoggedInUserID, LoggedInUserID, "R", "X"};
        Cursor resultSet = null;
        try {
            mDatabase = mHelper.getReadableDatabase();
            String sql = "SELECT " + MessagesDBHelper.COLUMN_SENDERID + " FROM " + MessagesDBHelper.TABLE_MESSAGES + " WHERE " +
                    MessagesDBHelper.COLUMN_LOGGEDINUSERID + "='" + LoggedInUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_RECIEVERID + "='" + LoggedInUserID + "' COLLATE NOCASE AND " +
                    MessagesDBHelper.COLUMN_MESSAGESTATUS + " NOT IN ('R','r','X');";
            resultSet = mDatabase.rawQuery(sql, null);

            ArrayList<String> SenderIDs = new ArrayList<>();
            if (resultSet != null && resultSet.getCount() > 0) {
                resultSet.moveToFirst();
                SenderIDs.add(resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_SENDERID)).toUpperCase());
                for (int i = 1; i < resultSet.getCount(); i++) {
                    resultSet.moveToNext();
                    SenderIDs.add(resultSet.getString(resultSet.getColumnIndex(MessagesDBHelper.COLUMN_SENDERID)).toUpperCase());
                }
            }
            return StringHelper.RemoveDuplicateEntries(SenderIDs).size();
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
        return 0;
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


    private static class MessagesDBHelper extends SQLiteOpenHelper {
        private static final int DB_VERSION = 1;
        private static final String DB_NAME = "GDMessages_db";

        public static final String TABLE_MESSAGES = "CachedMessages";

        public static final String COLUMN_LOGGEDINUSERID = "LoggedInUserID";
        public static final String COLUMN_MESSAGEID = "MessageID";
        public static final String COLUMN_CONVWITHUSERID = "UserID";
        public static final String COLUMN_SENDERID = "SenderID";
        public static final String COLUMN_SENDERNAME = "SenderName";
        public static final String COLUMN_RECIEVERID = "RecieverID";
        public static final String COLUMN_MESSAGETEXT = "MessageText";
        public static final String COLUMN_ATTACHEDPICIDS = "AttachedPicIDs";
        public static final String COLUMN_LOCATION = "Location";
        public static final String COLUMN_ATTACHEDFILEPATH = "AttachedFilePath";
        public static final String COLUMN_ATTACHEDFILESRC = "AttachedFileSrc";
        public static final String COLUMN_SENDERSAVED = "SenderSaved";
        public static final String COLUMN_RECIEVERSAVED = "RecieverSaved";
        public static final String COLUMN_SENDERDELETED = "SenderDeleted";
        public static final String COLUMN_RECIEVERDELETED = "RecieverDeleted";
        public static final String COLUMN_MESSAGESTATUS = "MessageStatus";
        public static final String COLUMN_SENTDATETIME = "SentDateTime";
        public static final String COLUMN_READDATETIME = "ReadDateTime";
        public static final String COLUMN_ISSENTTIMEUPDATEDFROMSERVER = "IsSentTimeUpdatedFromServer";

        private static final String CREATE_TABLE_CACHEDMESSAGES = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COLUMN_LOGGEDINUSERID + " TEXT," +
                COLUMN_MESSAGEID + " TEXT PRIMARY KEY," +
                COLUMN_CONVWITHUSERID + " TEXT," +
                COLUMN_SENDERID + " TEXT," +
                COLUMN_SENDERNAME + " TEXT," +
                COLUMN_RECIEVERID + " TEXT," +
                COLUMN_MESSAGETEXT + " TEXT," +
                COLUMN_ATTACHEDPICIDS + " TEXT," +
                COLUMN_LOCATION + " TEXT," +
                COLUMN_ATTACHEDFILEPATH + " TEXT," +
                COLUMN_ATTACHEDFILESRC + " TEXT," +
                COLUMN_SENDERSAVED + " TEXT," +
                COLUMN_RECIEVERSAVED + " TEXT," +
                COLUMN_SENDERDELETED + " TEXT," +
                COLUMN_RECIEVERDELETED + " TEXT," +
                COLUMN_MESSAGESTATUS + " TEXT," +
                COLUMN_SENTDATETIME + " TEXT," +
                COLUMN_READDATETIME + " TEXT," +
                COLUMN_ISSENTTIMEUPDATEDFROMSERVER + " TEXT);";

        public MessagesDBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE_CACHEDMESSAGES);
            } catch (SQLiteException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
            } catch (SQLiteException exception) {
                exception.printStackTrace();
            }
        }
    }
}
