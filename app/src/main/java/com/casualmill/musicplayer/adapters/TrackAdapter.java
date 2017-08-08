package com.casualmill.musicplayer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.casualmill.musicplayer.MusicData;
import com.casualmill.musicplayer.R;
import com.casualmill.musicplayer.activities.MainActivity;
import com.casualmill.musicplayer.models.Track;

import java.util.ArrayList;

/**
 * Created by faztp on 08-Aug-17.
 */

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.Holder>{

    private ArrayList<Track> tracks;

    public TrackAdapter(ArrayList<Track> _tracks) {
        this.tracks = _tracks;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false);
        Holder hl = new Holder(v);
        return hl;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Track tr = tracks.get(position);

        holder.title.setText(tr.title);
        holder.artist.setText(tr.artistName);
        holder.albumArt.setImageBitmap(MusicData.getAlbumCoverArt(MainActivity.mContext, tr.albumId));
    }

    @Override
    public int getItemCount() { return tracks == null ? 0 : tracks.size();  }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, artist;
        protected ImageView albumArt;

        public Holder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.item_track_title);
            this.artist = itemView.findViewById(R.id.item_track_artist);
            this.albumArt = itemView.findViewById(R.id.item_track_albumArt);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
