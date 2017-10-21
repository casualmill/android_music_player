package com.casualmill.musicplayer.services;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.casualmill.musicplayer.events.MusicServiceEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by Ali on 08-14-2017.
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener {

    public MediaPlayer player;
    public ArrayList<Long> track_ids;
    public int trackPosition = 0;

    private final IBinder serviceBinder = new MusicBinder();

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
        player.release();
    }

    @Override
    public void onCreate(){
        super.onCreate();

        this.InitializeMusicPlayer();
    }

    public void InitializeMusicPlayer()
    {
        //Initializing MusicPlayer and its properties
        player = new MediaPlayer();

        // WAKELOCK permission is required to use this method
        // Keeps the phone awake partially to allow the playback
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        //Setting which type of audio should be played
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void playTrack(){
        player.reset();

        //get trackId to play
        long trackId = track_ids.get(trackPosition);
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,trackId);

        EventBus.getDefault().post(new MusicServiceEvent(MusicServiceEvent.EventType.PREPARING, trackId));
        try{
            player.setDataSource(getApplicationContext(),trackUri);
        }
        catch (Exception ex){
            Log.e("Music Player","Error occured while setting data source",ex);
        }
        player.prepareAsync();

    }

    public void playNext() {
        if (track_ids == null || track_ids.size() == 0)
            return;
        else if (trackPosition == track_ids.size())
            trackPosition = 0;
        else
            trackPosition++;
        playTrack();
    }

    public void playPrevious() {
        if (track_ids == null || track_ids.size() == 0)
            return;
        else if (trackPosition == 0)
            trackPosition = track_ids.size() - 1;
        else
            trackPosition--;
        playTrack();
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
        EventBus.getDefault().post(new MusicServiceEvent(MusicServiceEvent.EventType.COMPLETED, track_ids.get(trackPosition)));
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        EventBus.getDefault().post(new MusicServiceEvent(MusicServiceEvent.EventType.PLAYING, track_ids.get(trackPosition)));
    }

    public class MusicBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }
}
