package com.gdudes.app.gdudesapp.Helpers.ImageHelper;

import android.content.Context;

import com.gdudes.app.gdudesapp.APICaller.APICalls.PicsAPICalls;
import com.gdudes.app.gdudesapp.Database.GDImageDBHelper;
import com.gdudes.app.gdudesapp.GDTypes.APIMajor.UserIDAndGUIDList;
import com.gdudes.app.gdudesapp.GDTypes.GDPic;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.Helpers.GDAsyncHelper.GDAsyncHelper;
import com.gdudes.app.gdudesapp.Helpers.GDAsyncHelper.GDAsyncTaskCallback;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.SessionManager;
import com.gdudes.app.gdudesapp.Helpers.StringHelper;
import com.gdudes.app.gdudesapp.Interfaces.APICallerResultCallback;
import com.gdudes.app.gdudesapp.Interfaces.ImageAPIHelperDelegate;
import com.gdudes.app.gdudesapp.Interfaces.ImageAPIHelperImageNotAvailableLocallyDelegate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageAPIHelper {

    private static ArrayList<GDPic> mPrivatePics = new ArrayList<>();
    private static ArrayList<GDPic> mPublicPics = new ArrayList<>();

    public static void GetUserPictures(final Context context, final Boolean getPublic, final Boolean isManagePics,
                                       final APICallerResultCallback apiCallerResultCallback,
                                       final ImageAPIHelperDelegate imageAPIHelperDelegate) {

        ArrayList<GDPic> cachedPicList = getPublic ? mPublicPics : mPrivatePics;
        if (isManagePics && cachedPicList.size() > 0) {
            Boolean ContainsUnCategorized = false;
            for (int i = 0; i < cachedPicList.size(); i++) {
                if (!cachedPicList.get(i).IsCategorized) {
                    ContainsUnCategorized = true;
                    break;
                }
            }
            if (!ContainsUnCategorized) {
                apiCallerResultCallback.OnComplete(cachedPicList, getPublic);
                GetPicsForGDPicList(context, cachedPicList, imageAPIHelperDelegate);
                return;
            }
        }
        Users loggedInUser = SessionManager.GetLoggedInUser();
        if (loggedInUser == null) {
            return;
        }

        new PicsAPICalls(context).GetUserPictures(getPublic, loggedInUser.UserID, isManagePics, new APICallerResultCallback() {
            @Override
            public void OnComplete(Object result, Object extraData) {
                try {
                    ArrayList<GDPic> picsList = (ArrayList<GDPic>) result;
                    apiCallerResultCallback.OnComplete(picsList, extraData);

                    if (isManagePics) {
                        if (getPublic) {
                            mPublicPics = picsList;
                        } else {
                            mPrivatePics = picsList;
                        }
                    }

                    GetPicsForGDPicList(context, picsList, imageAPIHelperDelegate);
                } catch (Exception e) {
                    GDLogHelper.LogException(e);
                }
            }

            @Override
            public void OnError(String result, Object extraData) {
                apiCallerResultCallback.OnError(result, extraData);
            }

            @Override
            public void OnNoNetwork(Object extraData) {
                apiCallerResultCallback.OnNoNetwork(null);
            }
        });
    }

    public static void GetPicsForGDPicList(final Context context, ArrayList<GDPic> picsList,
                                           final ImageAPIHelperDelegate imageAPIHelperDelegate) {
        ArrayList<String> picIDs = new ArrayList<>();
        for (int i = 0; i < picsList.size(); i++) {
            if (picsList.get(i).image == null) {
                picIDs.add(picsList.get(i).PicID);
            }
        }
        if (picIDs.size() > 0) {
            GetPicsForPicIDList(context, picIDs, false, imageAPIHelperDelegate);
        }
    }

    public static void GetPicsForPicIDList(final Context context, ArrayList<String> picIDs,
                                           Boolean getFromLocalOnly,
                                           final ImageAPIHelperDelegate imageAPIHelperDelegate) {
        int batchSize = 40;
        Boolean getInBatch = (picIDs.size() > (batchSize + 20)) && !getFromLocalOnly;

        ArrayList<String> batchPicIDs = new ArrayList<>();
        if (getInBatch) {
            batchPicIDs.addAll(picIDs.subList(0, batchSize - 1));
            picIDs.removeAll(batchPicIDs);
        } else {
            batchPicIDs = picIDs;
        }
        GetPicsForPicIDListInternal(context, batchPicIDs, getFromLocalOnly, imageAPIHelperDelegate, data -> {
            ArrayList<GDPic> picsList = (ArrayList<GDPic>) data;
            if (picsList.size() > 0) {
                imageAPIHelperDelegate.OnGDPicsAvailable(picsList);
            }
            if (getInBatch) {
                GetPicsForPicIDList(context, picIDs, getFromLocalOnly, imageAPIHelperDelegate);
            }
        });
    }

    private static void GetPicsForPicIDListInternal(final Context context, ArrayList<String> picIDs,
                                                    Boolean getFromLocalOnly,
                                                    final ImageAPIHelperDelegate imageAPIHelperDelegate,
                                                    final GDAsyncTaskCallback gdAsyncTaskCallback) {
        final GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(context);

        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            List<GDPic> cachedPicsList = new ArrayList<>();
            ArrayList<String> nonCachedPicIDs = new ArrayList<>();
            String picSrc;
            for (int i = 0; i < picIDs.size(); i++) {
                picSrc = gdImageDBHelper.GetImageStringByPicID(picIDs.get(i), false);
                if (!picSrc.equals("")) {
                    GDPic pic = new GDPic();
                    pic.PicID = picIDs.get(i);
                    pic.image = ImageHelper.GetBitmapFromString(picSrc);
                    if (pic.image != null) {
                        cachedPicsList.add(pic);
                    }
                } else {
                    nonCachedPicIDs.add(picIDs.get(i));
                }
            }

            if (!getFromLocalOnly) {
                GetNonCachedPics(context, nonCachedPicIDs, imageAPIHelperDelegate);
            }
            gdBackgroundTaskFinished.OnBackgroundTaskFinished(cachedPicsList);
        }, data -> {
            gdAsyncTaskCallback.OnTaskCompleted(data);
        });
    }

    public static void GetSmallPicFromLocal(final Context context, final String picID,
                                           final ImageAPIHelperDelegate imageAPIHelperDelegate,
                                            final ImageAPIHelperImageNotAvailableLocallyDelegate imageAPIHelperImageNotAvailableLocallyDelegate) {
        final GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(context);

        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            String picSrc = gdImageDBHelper.GetImageStringByPicID(picID, false);
            GDPic pic = new GDPic(picID);
            pic.image = ImageHelper.GetBitmapFromString(picSrc);
            if (pic.image != null) {
                gdBackgroundTaskFinished.OnBackgroundTaskFinished(StringHelper.ToArrayList(pic));
            } else {
                gdBackgroundTaskFinished.OnBackgroundTaskFinished(null);
            }
        }, data -> {
            if (data == null) {
                imageAPIHelperImageNotAvailableLocallyDelegate.OnImageNotAvailableLocally(picID);
            } else {
                imageAPIHelperDelegate.OnGDPicsAvailable((ArrayList<GDPic>) data);
            }
        });
    }

    private static void GetNonCachedPics(final Context context, ArrayList<String> picIDList,
                                         final ImageAPIHelperDelegate imageAPIHelperDelegate) {
        Users loggedInUser = SessionManager.GetLoggedInUser();
        if (loggedInUser == null) {
            return;
        }
        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            StringHelper.RemoveDuplicateEntries(picIDList);
            if (picIDList.size() > 0) {
                new PicsAPICalls(context).GetUserPicsByPicIDList(new UserIDAndGUIDList(loggedInUser.UserID, picIDList), (data, ExtraData) -> {
                    gdBackgroundTaskFinished.OnBackgroundTaskFinished(data);
                });
            }
        }, data -> {
            ArrayList<GDPic> picsList = (ArrayList<GDPic>) data;
            imageAPIHelperDelegate.OnGDPicsAvailable(picsList);
        });
    }

    public static void DeletePics(List<GDPic> ToDeleteList, Boolean isPublic) {
        if (isPublic) {
            mPublicPics.removeAll(ToDeleteList);
        } else {
            mPrivatePics.removeAll(ToDeleteList);
        }
    }

    public static void ClearCachedList(Boolean isPublic) {
        if (isPublic) {
            mPublicPics = new ArrayList<>();
        } else {
            mPrivatePics = new ArrayList<>();
        }
    }

    public static void GetFullPicListFromLocalOnly(final Context context, ArrayList<String> picIDList, final Boolean getSmallIfAvailable,
                                                   final ImageAPIHelperDelegate imageAPIHelperDelegate) {
        final GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(context);
        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            ArrayList<GDPic> pics = new ArrayList<>();
            for (int i = 0; i < picIDList.size(); i++) {
                String picID = picIDList.get(i);
                String picSrc = gdImageDBHelper.GetImageStringByPicID(picID, true);
                if (!StringHelper.IsNullOrEmpty(picSrc)) {
                    GDPic pic = new GDPic(picID);
                    pic.image = ImageHelper.GetBitmapFromString(picSrc, true);
                    pic.IsFullPic = true;
                    pics.add(pic);
                } else if (getSmallIfAvailable) {
                    picSrc = gdImageDBHelper.GetImageStringByPicID(picID, false);
                    if (!StringHelper.IsNullOrEmpty(picSrc)) {
                        GDPic pic = new GDPic(picID);
                        pic.image = ImageHelper.GetBitmapFromString(picSrc, false);
                        pic.IsFullPic = false;
                        pics.add(pic);
                    }
                }
                gdBackgroundTaskFinished.OnBackgroundTaskFinished(pics);
            }
        }, data -> {
            ArrayList<GDPic> cachedPicsList = (ArrayList<GDPic>) data;
            if (cachedPicsList.size() > 0) {
                imageAPIHelperDelegate.OnGDPicsAvailable(cachedPicsList);
            }
        });
    }

    public static void GetFullPic(final Context context, final String picID, final Boolean getSmallIfAvailable,
                                  final Boolean getFromLocalOnly,
                                  final ImageAPIHelperImageNotAvailableLocallyDelegate imageAPIHelperImageNotAvailableLocallyDelegate,
                                  final ImageAPIHelperDelegate imageAPIHelperDelegate,
                                  final APICallerResultCallback apiCallerResultCallback) {
        final GDImageDBHelper gdImageDBHelper = new GDImageDBHelper(context);
        GDAsyncHelper.DoTask(gdBackgroundTaskFinished -> {
            String picSrc = gdImageDBHelper.GetImageStringByPicID(picID, true);
            if (!StringHelper.IsNullOrEmpty(picSrc)) {
                GDPic pic = new GDPic(picID);
                pic.image = ImageHelper.GetBitmapFromString(picSrc, true);
                pic.IsFullPic = true;
                gdBackgroundTaskFinished.OnBackgroundTaskFinished(pic);
            } else {
                if (getSmallIfAvailable) {
                    picSrc = gdImageDBHelper.GetImageStringByPicID(picID, false);
                    if (!StringHelper.IsNullOrEmpty(picSrc)) {
                        GDPic pic = new GDPic(picID);
                        pic.image = ImageHelper.GetBitmapFromString(picSrc, false);
                        pic.IsFullPic = false;
                        gdBackgroundTaskFinished.OnBackgroundTaskFinished(pic);
                    }
                }
                if (getFromLocalOnly) {
                    gdBackgroundTaskFinished.OnBackgroundTaskFinished(null);
                    return;
                }
                new PicsAPICalls(context).GetFullUserImage(picID, new APICallerResultCallback() {
                    @Override
                    public void OnComplete(Object result, Object extraData) {
                        apiCallerResultCallback.OnComplete(result, null);
                    }

                    @Override
                    public void OnError(String result, Object extraData) {
                        apiCallerResultCallback.OnError(null, null);
                    }

                    @Override
                    public void OnNoNetwork(Object extraData) {
                        apiCallerResultCallback.OnNoNetwork(null);
                    }
                });
            }
        }, data -> {
            if (data == null) {
                imageAPIHelperImageNotAvailableLocallyDelegate.OnImageNotAvailableLocally(picID);
            } else {
                ArrayList<GDPic> cachedPicsList = new ArrayList<>(Arrays.asList((GDPic) data));
                if (cachedPicsList.size() > 0) {
                    imageAPIHelperDelegate.OnGDPicsAvailable(cachedPicsList);
                }
            }
        });
    }
}
