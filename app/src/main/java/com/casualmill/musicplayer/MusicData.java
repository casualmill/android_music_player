package com.casualmill.musicplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.casualmill.musicplayer.models.Album;
import com.casualmill.musicplayer.models.Track;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by faztp on 07-Aug-17.
 */

public class MusicData {

    public static ArrayList<Track> TrackList;
    public static ArrayList<Album> AlbumList;

    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");


    public static void LoadLists(Context ctx) {
        TrackList = getAllTracks(ctx);
        AlbumList = getAllAlbums(ctx);
    }

    public static ArrayList<Track> getAllTracks(Context context) {
        return getAllTracks(context, -1);
    }
    public static ArrayList<Track> getAllTracks(Context context, long album_id) {
        String selectionStatement = "is_music=1 AND title!=''" + (album_id == -1 ? "" : " AND album_id = '"+album_id+"'");
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{ // the columns that is queried. Return data is in this same order
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.TRACK, // track_number
                        MediaStore.Audio.Media.ARTIST_ID,
                        MediaStore.Audio.Media.ALBUM_ID
                },
                selectionStatement,
                null, // Selection args (used for proper escaping) ignored here
                album_id == -1 ? MediaStore.Audio.Media.TITLE : MediaStore.Audio.Media.TRACK); // Sort order

        ArrayList<Track> arrayList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) // confirming if any data returned
        {
            do {
                arrayList.add(new Track(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getLong(6),
                        cursor.getLong(7)
                ));
            } while (cursor.moveToNext());
        }
        if (cursor != null)
            cursor.close();

        return arrayList;
    }

    public static ArrayList<Album> getAllAlbums(Context context) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{ // the columns that is queried. Return data is in this same order
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.Albums.ALBUM,
                        MediaStore.Audio.Albums.ARTIST,
                        MediaStore.Audio.Albums.NUMBER_OF_SONGS,
                        MediaStore.Audio.Albums.FIRST_YEAR, // track_number
                        MediaStore.Audio.Media.ARTIST_ID
                },
                null,
                null, // Selection args (used for proper escaping) ignored here
                MediaStore.Audio.Albums.ALBUM); // Sort order

        ArrayList<Album> arrayList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) // confirming if any data returned
        {
            do {
                arrayList.add(new Album(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getLong(5)
                ));
            } while (cursor.moveToNext());
        }
        if (cursor != null)
            cursor.close();

        return arrayList;
    }


    public static void setAlbumArt(Context context, long album_id, ImageView view) {
        if (album_id == -1) // clear
            GlideApp
                .with(context)
                .load(R.drawable.albumart_default)
                .into(view);
        else
            GlideApp
                .with(context)
                .load(ContentUris.withAppendedId(sArtworkUri, album_id))
                .placeholder(R.drawable.albumart_default)
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .centerCrop()
                .into(view);
    }
}
