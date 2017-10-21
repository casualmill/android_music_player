package com.casualmill.musicplayer.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.casualmill.musicplayer.R;
import com.casualmill.musicplayer.fragments.AlbumsFragment;
import com.casualmill.musicplayer.fragments.PlayFragment;
import com.casualmill.musicplayer.fragments.TracksFragment;

/**
 * Created by faztp on 07-Aug-17.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {

    private final int NUM_PAGES = 3;
    private Context context;

    public MainPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.tracks);
            case 1:
                return context.getString(R.string.albums);
            case 2:
                return context.getString(R.string.now_playing);
            default:
                return null;
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new TracksFragment();
            case 1:
                return new AlbumsFragment();
            case 2:
                return new PlayFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
