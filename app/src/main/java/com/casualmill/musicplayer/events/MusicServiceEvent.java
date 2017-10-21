package com.casualmill.musicplayer.events;

/**
 * Created by faztp on 21-Oct-17.
 */

// for events raised from Services
// only track_id is published.
public class MusicServiceEvent {

    public EventType eventType;
    public long track_id;

    public MusicServiceEvent(EventType type, long track_id) {
        this.eventType = type;
        this.track_id = track_id;
    }

    public enum EventType { PREPARING, PLAYING, PAUSED, COMPLETED }
}
