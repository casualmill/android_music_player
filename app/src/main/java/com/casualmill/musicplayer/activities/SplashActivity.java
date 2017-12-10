package com.casualmill.musicplayer.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.casualmill.musicplayer.MusicData;
import com.casualmill.musicplayer.R;

public class SplashActivity extends AppCompatActivity {

    private static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 899;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        } else
            loadMainActivity();

    }

    private void loadMainActivity() {
        final ProgressBar pb = findViewById(R.id.splash_progressBar);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler(Looper.getMainLooper());
                MusicData.LoadLists(SplashActivity.this);
                try {
                    for (int i = 0; i < 100; i++) {
                        final int j = i;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                pb.setProgress(j);
                                if (j == 99) pb.setVisibility(View.INVISIBLE);
                            }
                        });

                        Thread.sleep(10);
                    }
                    Thread.sleep(200);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent mainActivityIntent = new Intent(SplashActivity.this, MainActivity.class);
                            //mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainActivityIntent);
                            finish();
                        }
                    });
                } catch (Exception e) {}
            }
        });

        t.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay!
                loadMainActivity();
            } else {
                // I QUIT
                // viewPager, it doesnt matter anyway. read the docs
                Snackbar.make(findViewById(R.id.splash_imageView), "Permission Denied. Bye Bye",
                        Snackbar.LENGTH_LONG)
                        .show();

                // close the app after 1second
                new Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                finishAffinity();
                            }
                        }, 1000);
            }
        }
    }
}
