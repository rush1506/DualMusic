package model;

import android.net.Uri;

import com.tata.dualmusic.R;


/**
 * Created by vutha_000 on 7/25/2016.
 */
public class MainListDefinition {

    private static Uri SongsPath = Uri.parse("android.resource://com.tata.dualmusic/" + R.drawable.songs);
    private static Uri FoldersPath = Uri.parse("android.resource://com.tata.dualmusic/" + R.drawable.folders);
    private static Uri ArtistsPath = Uri.parse("android.resource://com.tata.dualmusic/" + R.drawable.artists);
    private static Uri AlbumsPath = Uri.parse("android.resource://com.tata.dualmusic/" + R.drawable.albums);
    private static Uri PlaylistsPath = Uri.parse("android.resource://com.tata.dualmusic/" + R.drawable.playlists);
    private static Uri DualMusicPath = Uri.parse("android.resource://com.tata.dualmusic/" + R.drawable.dual_music);
    private static final String ThisTitles[] = {"Songs", "Dual Music", "Folders", "Artist", "Album", "Playlists"};
    private static String ThisImagePathString[] = {SongsPath.toString(), DualMusicPath.toString(), FoldersPath.toString(),
            ArtistsPath.toString(), AlbumsPath.toString(), PlaylistsPath.toString()};
    private static Uri ThisImagePathUri[] = {SongsPath, DualMusicPath, FoldersPath, ArtistsPath, AlbumsPath, PlaylistsPath};

    public static int count = 6;

    public static String[] getAllTitles() {
        return ThisTitles;
    }

    public static void setAllTitle(String[] titles) {
        titles = titles;
    }

    public static String[] getAllImagePaths() {
        return ThisImagePathString;
    }

    public static void setAllImagePaths(String[] imagePath) {
        ThisImagePathString = imagePath;
    }

    public static String getTitle(int index) {
        return ThisTitles[index];
    }

    public static void setTitle(int index, String title) {
        ThisTitles[index] = title;
    }

    public static String getImagePath(int index) {
        return ThisImagePathString[index];
    }

    public static void setImagePath(int index, String ImagePath) {
        ThisImagePathString[index] = ImagePath;
    }

    public static Uri getSongsPath() {
        return SongsPath;
    }

    public static void setSongsPath(Uri songsPath) {
        SongsPath = songsPath;
    }

    public static Uri getFoldersPath() {
        return FoldersPath;
    }

    public static void setFoldersPath(Uri foldersPath) {
        FoldersPath = foldersPath;
    }

    public static Uri getArtistsPath() {
        return ArtistsPath;
    }

    public static void setArtistsPath(Uri artistsPath) {
        ArtistsPath = artistsPath;
    }

    public static Uri getDualMusicPath() {
        return DualMusicPath;
    }

    public static void setDualMusicPath(Uri dualMusicPath) {
        DualMusicPath = dualMusicPath;
    }

    public static Uri getAlbumsPath() {
        return AlbumsPath;
    }

    public static void setAlbumsPath(Uri albumsPath) {
        AlbumsPath = albumsPath;
    }

    public static Uri getPlaylistsPath() {
        return PlaylistsPath;
    }

    public static void setPlaylistsPath(Uri playlistsPath) {
        PlaylistsPath = playlistsPath;
    }

    public static Uri[] getAllImagePathUri() {
        return ThisImagePathUri;
    }

    public static Uri getImagePathUri(int pos) {
        return ThisImagePathUri[pos];
    }

    public static void setAllImagePathUri(Uri[] ur) {
        ThisImagePathUri = ur;
    }

    public static void setImagePathUri(int pos, Uri ur) {
        ThisImagePathUri[pos] = ur;
    }

}
