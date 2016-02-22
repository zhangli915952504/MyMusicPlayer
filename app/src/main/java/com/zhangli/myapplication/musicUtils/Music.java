package com.zhangli.myapplication.musicUtils;

import android.os.Parcel;
import android.os.Parcelable;

public class Music implements Parcelable {
    // id title singer data time image
    private int id; // 音乐id
    private String title; // 音乐标题
    private String uri; // 音乐路径
    private int length; // 长度
    private String image; // icon
    private String artist; // 艺术家



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(uri);
        dest.writeInt(length);
        dest.writeString(image);
        dest.writeString(artist);
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel source) {
            int id = source.readInt();
            String title = source.readString();
            String uri = source.readString();
            int length=source.readInt();
            String image=source.readString();
            String artist=source.readString();

            Music music = new Music();
            music.setId(id);
            music.setTitle(title);
            music.setUri(uri);
            music.setLength(length);
            music.setImage(image);
            music.setArtist(artist);

            return music;
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };
}
