package com.example.myimagegallery;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;


// Thông tin model của một ảnh từ MediaStore.Image
public class ImageItem implements Parcelable {
    private final Uri imageUrl;
    private final String title;

    public ImageItem(Uri imageUrl, String title) {
        this.imageUrl = imageUrl;
        this.title = title;
    }

    protected ImageItem(Parcel in) {
        imageUrl = in.readParcelable(Uri.class.getClassLoader());
        title = in.readString();
    }

    public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel in) {
            return new ImageItem(in);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };

    public Uri getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(imageUrl, flags);
        dest.writeString(title);
    }
}
