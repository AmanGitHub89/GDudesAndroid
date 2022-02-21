package com.gdudes.app.gdudesapp.Helpers;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;

import com.gdudes.app.gdudesapp.APICaller.APICallInfo;
import com.gdudes.app.gdudesapp.BuildConfig;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.ImageHelper.ImageHelper;
import com.gdudes.app.gdudesapp.Interfaces.GDTimerTaskRun;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class GDLogHelper {
    private static String mFileDirectory = "";
    private static String mLogFileName = "";
    private static GDTimer mTimerWriteLog = null;
    private static ArrayList<PendingLogs> PendingLogList = new ArrayList<>();
    private static Boolean IsLogBackupRunning = false;
    private static Boolean IsWriteLogToFileRunning = false;

    public enum LogLevel {
        INFO,
        DEBUG,
        ERROR,
        EXCEPTION,
        CRITICAL
    }

    public enum MethodEE {
        ENTER,
        EXIT
    }

    public static void LogMethodEnterExit(String ClassName, String MethodName, MethodEE methodEE) {
        StartTimer();
        PendingLogList.add(new PendingLogs(LogLevel.DEBUG, ClassName + "->" + MethodName + "() :\t" + (methodEE == MethodEE.ENTER ? "Entering method" : "Exiting method")));
    }

    public static void Log(String ClassName, String MethodName, String LogText) {
        StartTimer();
        PendingLogList.add(new PendingLogs(LogLevel.DEBUG, ClassName + "->" + MethodName + "() :\t" + LogText));
    }

    public static void Log(String ClassName, String MethodName, String LogText, LogLevel logLevel) {
        StartTimer();
        PendingLogList.add(new PendingLogs(logLevel, ClassName + "->" + MethodName + "() :\t" + LogText + "\n\n"));
    }

    public static void LogException(Exception exception) {
        if (exception instanceof JsonSyntaxException) {
            return;
        }
        StringWriter stringWriter = null;
        try {
            StartTimer();
            stringWriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stringWriter));
            PendingLogList.add(new PendingLogs(LogLevel.EXCEPTION, stringWriter.toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (stringWriter != null) {
                    stringWriter.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void StartTimer() {
        try {
            if (mTimerWriteLog == null) {
                //Run every 3 seconds
                mTimerWriteLog = new GDTimer(3000, 3000, new Handler(), new GDTimerTaskRun() {
                    @Override
                    public void OnTimerElapsed() {
                        WriteLogToFile();
                    }
                });
                mTimerWriteLog.Start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void WriteLogToFile() {
        if (IsLogBackupRunning || IsWriteLogToFileRunning || PendingLogList.size() == 0) {
            return;
        }
        IsWriteLogToFileRunning = true;
        if (mLogFileName.equals("")) {
            GetFileName();
        }
        File logFile = null;
        BufferedWriter bufferedWriter = null;
        try {
            logFile = new File(mLogFileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            String CurrentDateTime = GDDateTimeHelper.GetCurrentDateTimeAsString(false);
            bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
            for (int i = 0; i < PendingLogList.size(); i++) {
                bufferedWriter.append(PendingLogList.get(i).logLevel.toString() + ":" + CurrentDateTime + "\t"
                        + PendingLogList.get(i).LogText + "\n");
            }
            PendingLogList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IsWriteLogToFileRunning = false;
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void GetFileName() {
        try {
            mLogFileName = CreateDirectoryForLog() + File.separator + "GDudes_Log";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String CreateDirectoryForLog() {
        String Directory = "";
        File wallpaperDirectory;
        try {
            Directory = Environment.getExternalStorageDirectory() + File.separator + ".GDudes Log";
            mFileDirectory = Directory;
            wallpaperDirectory = new File(Directory);
            if (!wallpaperDirectory.exists()) {
                // have the object build the directory structure, if needed.
                wallpaperDirectory.mkdirs();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Directory = "";
        }
        return Directory;
    }

    public static Boolean CreateLogBackup() {
        IsLogBackupRunning = true;
        Boolean BackupCreated = false;
        if (mLogFileName.equals("")) {
            GetFileName();
        }
        try {
            File logFile = new File(mLogFileName);
            if (logFile.exists()) {
                String LogBackupFileName = mLogFileName + "_" + GDGenericHelper.GetNewGUID();
                if (ImageHelper.CopyImage(mLogFileName, LogBackupFileName)) {
                    BackupCreated = true;
                    File file = new File(mLogFileName);
                    file.delete();
                }
            }
        } catch (Exception ex) {
            LogException(ex);
        } finally {
            IsLogBackupRunning = false;
        }
        return BackupCreated;
    }

    public static String GetBackupLogData(Boolean forceGet) {
        StringBuilder CompleteLogData = new StringBuilder();
        if (mLogFileName.equals("")) {
            GetFileName();
        }
        try {
            File f = new File(mFileDirectory);
            File file[] = f.listFiles();

            //If all files total are more than 4MB return and delete all files
            int file_size = 0;
            for (int i = 0; i < file.length; i++) {
                file_size = file_size + (int) (file[i].length() / 1024);
            }

            String version = "";
            try {
                version = Integer.toString(BuildConfig.VERSION_CODE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (file_size > 4000) {
                for (int i = 0; i < file.length; i++) {
                    file[i].delete();
                }

                CompleteLogData.append("****************************** Log Start v(" + version + ") *****************************************************"
                        + GDDateTimeHelper.GetCurrentDateTimeAsString(false) + "\n");
                CompleteLogData.append("Deleting log more than 4 MB");
                CompleteLogData.append("****************************** Log End *****************************************************"
                        + GDDateTimeHelper.GetCurrentDateTimeAsString(false));
                //delete all backup files and return
                return CompleteLogData.toString();
            }
            //If all files total are less than 1 MB, return
            if (file_size < 1000 && !forceGet) {
                return "";
            }

            CompleteLogData.append("****************************** Log Start v(" + version + ") *****************************************************"
                    + GDDateTimeHelper.GetCurrentDateTimeAsString(false) + "\n");
            for (int i = 0; i < file.length; i++) {
                if (!file[i].getName().equalsIgnoreCase("GDudes_Log")) {
                    CompleteLogData.append(GetLogAsString(mFileDirectory + File.separator + file[i].getName()));
                }
            }
            CompleteLogData.append("****************************** Log End *****************************************************"
                    + GDDateTimeHelper.GetCurrentDateTimeAsString(false));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CompleteLogData.toString();
    }

    public static String GetLogAsString(String filePath) {
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();
        FileInputStream fileInputStream = null;
        try {
            File fl = new File(filePath);
            fileInputStream = new FileInputStream(fl);
            reader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            stringBuilder.append("*************************************************************************\n");
            stringBuilder.append("*************************** End Of File *********************************\n");
            stringBuilder.append("*************************************************************************\n\n\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (Exception ex) {

            }
        }
        return stringBuilder.toString();
    }

    public static void DeleteAllBackupLogs() {
        if (mLogFileName.equals("")) {
            GetFileName();
        }
        try {
            File directory = new File(mFileDirectory);
            File files[] = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (!files[i].getName().equalsIgnoreCase("GDudes_Log")) {
                    File file = new File(mFileDirectory + File.separator + files[i].getName());
                    file.delete();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void UploadErrorLogsToServer(Context context, Boolean forceGet) {
        try {
            Users user = SessionManager.GetLoggedInUser();
            if (user == null || StringHelper.IsNullOrEmpty(user.UserID)) {
                if (forceGet) {
                    user = new Users(GDGenericHelper.GetNewGUID());
                } else {
                    return;
                }
            }
            WriteLogToFile();

            //Log.d("GDLog", "UploadErrorLogsToServer");
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            //upload at 1 AM or 3 AM or 5 AM or 7 AM or 9 AM of User's local time
            if (!forceGet && hour != 1 && hour != 3 && hour != 5 && hour != 7 && hour != 9) {
                return;
            }
            if (!GDLogHelper.CreateLogBackup()) {
                return;
            }
            String LogData = GDLogHelper.GetBackupLogData(forceGet);
            if (LogData == null || LogData.trim().equals("")) {
                return;
            }
            APICallInfo apiCallInfo = new APICallInfo("Home", "UploadErrorLog", null, "POST",
                    new ErrorLog(user.UserID, LogData, Integer.toString(BuildConfig.VERSION_CODE)), null, false, null, APICallInfo.APITimeouts.LONG);
            apiCallInfo.CalledFromService = true;
            GDGenericHelper.executeAsyncPOSTAPITask(context, apiCallInfo, (result, ExtraData) -> {
                try {
                    if (result != null && result.equals("1")) {
                        GDLogHelper.DeleteAllBackupLogs();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    GDLogHelper.LogException(e);
                }
            }, () -> {
            });
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }


    static class PendingLogs {
        LogLevel logLevel;
        String LogText;

        public PendingLogs(LogLevel vlogLevel, String vLogText) {
            logLevel = vlogLevel;
            LogText = vLogText;
        }
    }

    static class ErrorLog {
        public String UserID;
        public String LogData;
        public String AppVer;

        public ErrorLog(String vUserID, String vLogData, String vAppVer) {
            UserID = vUserID;
            LogData = vLogData;
            AppVer = vAppVer;
        }
    }
}
