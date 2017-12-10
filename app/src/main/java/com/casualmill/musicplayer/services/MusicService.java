package com.casualmill.musicplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.casualmill.musicplayer.MusicData;
import com.casualmill.musicplayer.R;
import com.casualmill.musicplayer.activities.MainActivity;
import com.casualmill.musicplayer.events.MusicServiceEvent;
import com.casualmill.musicplayer.models.Track;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by Ali on 08-14-2017.
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener {


    private MediaPlayer mPlayer;
    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;

    public ArrayList<Track> tracks;
    public int trackPosition = -1;
    public static final int NOTIFICATION_ID = 95;

    private final IBinder serviceBinder = new MusicBinder();

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSession.release();
        mPlayer.stop();
        mPlayer.release();
    }

    @Override
    public void onCreate(){
        super.onCreate();

        this.initService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mSession == null)
            this.initService();

        return super.onStartCommand(intent, flags, startId);
    }

    private void initService()
    {
        //Initializing MusicPlayer and its properties
        mPlayer = new MediaPlayer();

        // WAKELOCK permission is required to use this method
        // Keeps the phone awake partially to allow the playback
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        //Setting which type of audio should be played
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);

        mSession = new MediaSessionCompat(this, "MediaSession");
        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Log.e("SERVICE", "onPlay");
                if (!mPlayer.isPlaying() && mPlayer.getCurrentPosition() > 1) // resume
                    mPlayer.start();
                else
                    playTrack();

                mSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, mPlayer.getCurrentPosition(), 1).build());
            }

            @Override
            public void onSkipToQueueItem(long id) {
                super.onSkipToQueueItem(id);
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.e("SERVICE", "onPause");
                mPlayer.pause();

                mSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PAUSED, mPlayer.getCurrentPosition(), 1).build());
            }

            @Override
            public void onPrepare() {
                super.onPrepare();
                Log.e("SERVICE", "onPrepare");

                mSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_BUFFERING, mPlayer.getCurrentPosition(), 1).build());
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Log.e("SERVICE", "onSkipToNext");

                mSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, mPlayer.getCurrentPosition(), 1).build());
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Log.e("SERVICE", "onSkipToPrev");

                mSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS, mPlayer.getCurrentPosition(), 1).build());
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                Log.e("SERVICE", "onSeekTo");

                mSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, mPlayer.getCurrentPosition(), 1).build());
            }

            @Override
            public void onStop() {
                super.onStop();
                Log.e("SERVICE", "onStop");

                mSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_STOPPED, mPlayer.getCurrentPosition(), 1).build());
            }
        });
        mSession.setActive(true);
        EventBus.getDefault().post(new MusicServiceEvent(MusicServiceEvent.EventType.INIT, mSession.getSessionToken()));
    }

    public void playTrack(){
        mPlayer.reset();

        //get trackId to play
        Track track = tracks.get(trackPosition);
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.id);

        mSession.setMetadata(MusicData.getTrackMetaData(this, track));

        EventBus.getDefault().post(new MusicServiceEvent(MusicServiceEvent.EventType.PREPARING, track));
        try{
            mPlayer.setDataSource(getApplicationContext(),trackUri);
        }
        catch (Exception ex){
            Log.e("Music Player","Error occurred while setting data source",ex);
        }
        mPlayer.prepareAsync();

    }

    public void playNext() {
        if (tracks == null || tracks.size() == 0)
            return;
        else if (trackPosition == tracks.size())
            trackPosition = 0;
        else
            trackPosition++;
        playTrack();
    }

    public void playPrevious() {
        if (tracks == null || tracks.size() == 0)
            return;
        else if (trackPosition == 0)
            trackPosition = tracks.size() - 1;
        else
            trackPosition--;
        playTrack();
    }

    private void notificationManager(MusicServiceEvent.EventType type) {
        Notification n = createNotification();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        switch (type) {
            case PAUSED:
            case PLAYING:
                //manager.notify(NOTIFICATION_ID, n);
                break;
            case COMPLETED:
            case STOPPED:
                manager.cancel(NOTIFICATION_ID);
                break;
        }
        Notification.Builder builder = new Notification.Builder(this);
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // prev

        // play/pause
        String label;
        int icon;
        PendingIntent playPauseIntent;
        if (mPlayer.isPlaying()) {
            label = getString(R.string.pause);
            icon = R.drawable.pause;
            //intent = mPauseIntent;
        } else {
            label = getString(R.string.play);
            icon = R.drawable.play;
            //intent = mPlayIntent;
        }
        //builder.addAction(icon, label, intent);

        // next

        // ContentIntent
        Intent contentIntent = new Intent(this, MainActivity.class);
        PendingIntent pContentIntent = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentTitle("Music Player");
        builder.setContentText("Track 1");
        builder.setContentIntent(pContentIntent);
        //builder.setStyle(new NotificationCompat.MediaStyle()); need compat for this

        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playNext();
        EventBus.getDefault().post(new MusicServiceEvent(MusicServiceEvent.EventType.COMPLETED, tracks.get(trackPosition)));
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        EventBus.getDefault().post(new MusicServiceEvent(MusicServiceEvent.EventType.PLAYING, tracks.get(trackPosition)));
    }

    public class MusicBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }
}
