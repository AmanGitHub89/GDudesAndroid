package com.gdudes.app.gdudesapp.Helpers;

import com.gdudes.app.gdudesapp.GDTypes.CompleteUserProfile;

import java.util.ArrayList;
import java.util.Date;

public class CompleteProfileRepository {
    private static ArrayList<CompleteUserProfile> CachedProfiles = new ArrayList<>();

    public static void AddToCache(CompleteUserProfile profile) {
        try {
            RemoveFromCache(profile.UserID);
            profile.ProfileCachedDateTime = GDDateTimeHelper.GetCurrentDateTimeAsString(false);
            CachedProfiles.add(profile);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public static CompleteUserProfile GetProfile(String UserID) {
        try {
            int index = CachedProfiles.indexOf(new CompleteUserProfile(UserID));
            if (index == -1) {
                return null;
            }
            CompleteUserProfile FoundProfile = CachedProfiles.get(index);
            Date cachedDT = GDDateTimeHelper.GetDateFromString(FoundProfile.ProfileCachedDateTime);
            long timeElapsed = GDDateTimeHelper.GetTimeElapsedInSeconds(cachedDT);
            if (timeElapsed == -1 || timeElapsed >= 300) {
                return null;
            }
            return FoundProfile;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            return null;
        }
    }

    public static void RemoveFromCache(String UserID) {
        try {
            CompleteUserProfile profile = new CompleteUserProfile(UserID);
            if (CachedProfiles.contains(profile)) {
                int index = CachedProfiles.indexOf(profile);
                CachedProfiles.remove(index);
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }
}
