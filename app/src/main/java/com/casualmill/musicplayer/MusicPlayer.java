package com.casualmill.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.casualmill.musicplayer.events.MusicServiceEvent;
import com.casualmill.musicplayer.models.Track;
import com.casualmill.musicplayer.services.MusicService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by Fahim on 08-14-2017.
 */

public class MusicPlayer {

    public static ArrayList<Track> currentPlayList;
    private static MusicService musicService;
    private static Intent musicIntent;
    private static boolean serviceBound = false;
    private static MediaControllerCompat mController;

    public static void init(Context ctx) {
        if (musicIntent == null) {
            musicIntent = new Intent(ctx, MusicService.class);
            ctx.bindService(musicIntent, musicConnection, Context.BIND_AUTO_CREATE);
            ctx.startService(musicIntent);

        }
    }

    public static void setToken(Context ctx, MediaSessionCompat.Token token) {
        try {
            mController = new MediaControllerCompat(ctx, token);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public static void setServiceTrackList(ArrayList<Track> tracks) {
        currentPlayList = tracks;

        if (tracks == null)
            return;

        if (serviceBound) {
            musicService.tracks = tracks;
            musicService.trackPosition = -1;
        }
    }

    public static void playTrackAtIndex(int index){
        musicService.trackPosition = index;
        musicService.playTrack();
    }

    public static boolean playPause() {
        // even if not initialized, player will call onComplete which will call playNext()
        /*
        if (musicService.mPlayer.isPlaying())
            musicService.mPlayer.pause();
        else
            musicService.mPlayer.start();

        if (musicService.trackPosition >= 0)
            EventBus.getDefault().post(
                    new MusicServiceEvent(
                            musicService.mPlayer.isPlaying() ? MusicServiceEvent.EventType.PLAYING : MusicServiceEvent.EventType.PAUSED,
                            musicService.tracks.get(musicService.trackPosition)
                    )
            );
        return musicService.mPlayer.isPlaying();
        */
        if (mController == null) return false;

        if (mController.getPlaybackState() != null && mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING)
            mController.getTransportControls().pause();
        else
            mController.getTransportControls().play();

        return true;
    }

    public static void playNext() {
        if (mController == null) return;
        mController.getTransportControls().skipToNext();
    }

    public static void playPrevious() {
        if (mController == null) return;
        mController.getTransportControls().skipToPrevious();
    }

    public static void seekToPosition(int pos) {
        if (mController == null) return;
        mController.getTransportControls().seekTo((long) (mController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION) * (pos / 100f)));
    }

    // Returns progress in [0, 100]
    public static int getProgress() {
        if (mController == null || mController.getPlaybackState() == null) return 0;
        if (mController.getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING) return 0;
        return (int)(mController.getPlaybackState().getPosition() * 100 / mController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
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
            musicService = null;
            serviceBound = false;
        }
    };
}
