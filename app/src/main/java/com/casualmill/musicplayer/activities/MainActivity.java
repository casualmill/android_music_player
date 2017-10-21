package com.casualmill.musicplayer.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.casualmill.musicplayer.MusicPlayer;
import com.casualmill.musicplayer.R;
import com.casualmill.musicplayer.adapters.MainPagerAdapter;
import com.casualmill.musicplayer.events.MusicServiceEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * MainActivity.java
 * - Starting point of the application
 * - onCreate
 *      checks for permissions
 *          READ EXTERNAL STORAGE
 *      if has_permission:
 *          init()
 *              set activity_main.xml
 *              setup ViewPager, TabLayout, link buttons
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 899;
    public static final boolean DEVELOPER_MODE = true;

    // main_info collector
    private SeekBar seekBar;
    private boolean update_ui = false;
    private Handler handler = new Handler();
    private Runnable mediaInfo_looper = new Runnable() {
        @Override
        public void run() {
            if (update_ui) {
                seekBar.setProgress(MusicPlayer.getProgress());
                handler.postDelayed(this, 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        }

        // Permission has to be given by the user.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        } else
            init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay!
                init();
            } else {
                // I QUIT
                // viewPager, it doesnt matter anyway. read the docs
                Snackbar.make(findViewById(R.id.viewPager), "Permission Denied. Bye Bye",
                        Snackbar.LENGTH_LONG)
                        .show();

                // close the app after 1second
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                finishAffinity();
                            }
                        }, 1000);
            }
        }
    }

    private void init() {
        setContentView(R.layout.activity_main);

        // setup ViewPager
        ViewPager pager = (ViewPager)findViewById(R.id.viewPager);
        MainPagerAdapter pagerAdapter = new MainPagerAdapter(this, getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        //TabLayout
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);

        // Play Button
        Button playButton = (Button)findViewById(R.id.button_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayer.playPause();
            }
        });

        Button prevButton = (Button)findViewById(R.id.button_prev);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayer.playPrevious();
            }
        });

        Button nextButton = (Button)findViewById(R.id.button_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayer.playNext();
            }
        });

        // Seeker
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // default [0,100]
                if (b) // user changed
                    MusicPlayer.seekToPosition(i);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicServiceEvent(MusicServiceEvent event) {
        switch (event.eventType) {
            case COMPLETED:
                update_ui = false;
                break;
            case PLAYING:
                update_ui = true;
                handler.post(mediaInfo_looper);
                break;
            case PAUSED:
                update_ui = false;
                break;
        }
        Log.e("SERVICE", event.eventType + " " + event.track_id);
    }

    @Override
    protected void onStart(){
        super.onStart();
        MusicPlayer.init(getApplicationContext());

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
