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
import com.casualmill.musicplayer.models.Album;

import java.util.ArrayList;

/**
 * Created by faztp on 08-Aug-17.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.Holder>{

    private ArrayList<Album> albums;

    public AlbumAdapter(ArrayList<Album> _albums) {
        this.albums = _albums;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        Holder hl = new Holder(v);
        return hl;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Album tr = albums.get(position);

        holder.albumName.setText(tr.title);
        holder.artist.setText(tr.artistName);
        MusicData.setAlbumArt(holder.itemView.getContext(), tr.id, holder.albumArt);
    }

    @Override
    public int getItemCount() { return albums == null ? 0 : albums.size();  }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView albumName, artist;
        protected ImageView albumArt;

        public Holder(View itemView) {
            super(itemView);
            this.albumName = itemView.findViewById(R.id.item_album_albumName);
            this.artist = itemView.findViewById(R.id.item_album_artist);
            this.albumArt = itemView.findViewById(R.id.item_album_albumArt);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
