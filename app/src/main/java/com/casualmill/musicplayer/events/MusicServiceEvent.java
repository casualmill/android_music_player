package com.casualmill.musicplayer.events;

import com.casualmill.musicplayer.models.Track;

/**
 * Created by faztp on 21-Oct-17.
 */

// for events raised from Services
// only track_id is published.
public class MusicServiceEvent {

    public EventType eventType;
    public Object data;

    public MusicServiceEvent(EventType type, Object data) {
        this.eventType = type;
        this.data = data;
    }

    public enum EventType {
        INIT, // data-> MediaSessionCompat.Token
        PREPARING, PLAYING, RESUMED, PAUSED, COMPLETED, STOPPED // track
    }

}
