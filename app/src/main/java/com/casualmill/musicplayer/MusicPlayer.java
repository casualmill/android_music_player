package com.casualmill.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.casualmill.musicplayer.Services.MusicService;
import com.casualmill.musicplayer.models.Track;

import java.util.ArrayList;

/**
 * Created by Ali on 08-14-2017.
 */

public class MusicPlayer {

    private static MusicService musicService;
    private static Intent musicIntent;
    private static boolean serviceBound = false;
    private static Context context;
    private static ArrayList<Track> currentPlayList;

    public static void init(Context ctx) {
        context = ctx;
        if (musicIntent == null) {
            musicIntent = new Intent(context, MusicService.class);
            context.bindService(musicIntent, musicConnection, Context.BIND_AUTO_CREATE);
            context.startService(musicIntent);
        }
    }

    public static void setServiceTrackList(ArrayList<Track> tracks) {
        currentPlayList = tracks;

        if (tracks == null)
            return;

        ArrayList<Long> ids = new ArrayList<>();
        for (Track t : tracks) {
            ids.add(t.id);
        }
        if (serviceBound) {
            musicService.track_ids = ids;
            musicService.trackPosition = 0;
        }
    }

    public static void playTrackAtIndex(int index){
        musicService.trackPosition = index;
        musicService.playTrack();
    }

    public static boolean playPause() {
        if (musicService.player.isPlaying())
            musicService.player.pause();
        else
            musicService.player.start();
        return musicService.player.isPlaying();
    }

    public static void playNext() {
        musicService.playNext();
    }

    public static void playPrevious() {
        musicService.playPrevious();
    }

    public static void seekToPosition(int pos) {
        if (musicService.player.isPlaying()) {
            int position = (int) (musicService.player.getDuration() * (pos / 100f));
            musicService.player.seekTo(position);
        }
    }

    // Returns progress in [0, 100]
    public static int getProgress() {
        if (serviceBound && musicService.player.isPlaying()) {
            MediaPlayer player = musicService.player;
            return player.getCurrentPosition() * 100 / player.getDuration();
        } else {
            return 0;
        }
    }

    private static ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;

            musicService = binder.getService();
            serviceBound = true;
            setServiceTrackList(currentPlayList);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };
}
