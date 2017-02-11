package model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vutha_000 on 7/25/2016.
 */


/*
*  Class to hold currently playing music information
*
* */
public class Music implements Parcelable {

    private String MUSIC_TITLE;
    private String MUSIC_PATH;
    private String MUSIC_ARTIST;
    private String MUSIC_ALBUM;
    private String MUSIC_DURATION;
    private String MUSIC_DISPLAY_NAME;
    private int MUSIC_ID;

    public Music(Parcel parcel) {
        String[] tmpData = new String[6];
        parcel.readStringArray(tmpData);
        //String tmpData[] = parcel.createStringArray();

        MUSIC_TITLE = tmpData[0];
        MUSIC_PATH = tmpData[1];
        MUSIC_ARTIST = tmpData[2];
        MUSIC_ALBUM = tmpData[3];
        MUSIC_DURATION = tmpData[4];
        MUSIC_DISPLAY_NAME = tmpData[5];

        MUSIC_ID = parcel.readInt();
    }

    public Music() {
        String MUSIC_TITLE = "";
        String MUSIC_PATH = "";
        String MUSIC_ARTIST = "";
        String MUSIC_ALBUM = "";
        String MUSIC_DURATION = "";
        String MUSIC_DISPLAY_NAME = "";
        int MUSIC_ID = -1;
    }

    public int getMUSIC_ID() {
        return MUSIC_ID;
    }

    public void setMUSIC_ID(int MUSIC_ID) {
        this.MUSIC_ID = MUSIC_ID;
    }

    public String getMUSIC_DURATION() {
        return MUSIC_DURATION;
    }

    public void setMUSIC_DURATION(String MUSIC_DURATION) {
        this.MUSIC_DURATION = MUSIC_DURATION;
    }

    public String getMUSIC_DISPLAY_NAME() {
        return MUSIC_DISPLAY_NAME;
    }

    public void setMUSIC_DISPLAY_NAME(String MUSIC_DISPLAY_NAME) {
        this.MUSIC_DISPLAY_NAME = MUSIC_DISPLAY_NAME;
    }

    public String getMUSIC_ARTIST() {
        return MUSIC_ARTIST;
    }

    public void setMUSIC_ARTIST(String MUSIC_ARTIST) {
        this.MUSIC_ARTIST = MUSIC_ARTIST;
    }

    public String getMUSIC_ALBUM() {
        return MUSIC_ALBUM;
    }

    public void setMUSIC_ALBUM(String MUSIC_ALBUM) {
        this.MUSIC_ALBUM = MUSIC_ALBUM;
    }

    public String getMUSIC_TITLE() {
        return MUSIC_TITLE;
    }

    public void setMUSIC_TITLE(String MUSIC_TITLE) {
        this.MUSIC_TITLE = MUSIC_TITLE;
    }

    public String getMUSIC_PATH() {
        return MUSIC_PATH;
    }

    public void setMUSIC_PATH(String MUSIC_PATH) {
        this.MUSIC_PATH = MUSIC_PATH;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeStringArray(new String[]{
                MUSIC_TITLE,
                MUSIC_PATH,
                MUSIC_ARTIST,
                MUSIC_ALBUM,
                MUSIC_DURATION,
                MUSIC_DISPLAY_NAME

        });

        parcel.writeInt(MUSIC_ID);

    }

    public static final Parcelable.Creator<Music> CREATOR
            = new Creator<Music>() {

        @Override
        public Music createFromParcel(Parcel parcel) {
            return new Music(parcel);
        }

        @Override
        public Music[] newArray(int i) {
            return new Music[i];
        }
    };

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (!Music.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final Music music = (Music) obj;

        return this.MUSIC_ID == music.MUSIC_ID;

    }

    @Override
    public int hashCode() {
        return MUSIC_ID;
    }
}
