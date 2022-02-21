package com.gdudes.app.gdudesapp.GDTypes;

import android.os.Parcel;
import android.os.Parcelable;

public class GeneralMessageActivityContent implements Parcelable {
    public String HeaderText;
    public String DetailedText;
    public String ButtonText;

    public GeneralMessageActivityContent(String vHeaderText, String vDetailedText, String vButtonText) {
        HeaderText = vHeaderText;
        DetailedText = vDetailedText;
        ButtonText = vButtonText;
    }

    protected GeneralMessageActivityContent(Parcel in) {
        HeaderText = in.readString();
        DetailedText = in.readString();
        ButtonText = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(HeaderText);
        dest.writeString(DetailedText);
        dest.writeString(ButtonText);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GeneralMessageActivityContent> CREATOR = new Parcelable.Creator<GeneralMessageActivityContent>() {
        @Override
        public GeneralMessageActivityContent createFromParcel(Parcel in) {
            return new GeneralMessageActivityContent(in);
        }

        @Override
        public GeneralMessageActivityContent[] newArray(int size) {
            return new GeneralMessageActivityContent[size];
        }
    };
}
