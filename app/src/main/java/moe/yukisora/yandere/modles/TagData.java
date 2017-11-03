package moe.yukisora.yandere.modles;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

public class TagData implements SearchSuggestion {
    public static final Creator<TagData> CREATOR = new Creator<TagData>() {
        @Override
        public TagData createFromParcel(Parcel in) {
            return new TagData(in);
        }

        @Override
        public TagData[] newArray(int size) {
            return new TagData[size];
        }
    };
    public int color;
    public String tag;

    public TagData(int color, String tag) {
        this.color = color;
        this.tag = tag;
    }

    public TagData(Parcel source) {
        this.color = source.readInt();
        this.tag = source.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(color);
        parcel.writeString(tag);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getBody() {
        return tag;
    }
}