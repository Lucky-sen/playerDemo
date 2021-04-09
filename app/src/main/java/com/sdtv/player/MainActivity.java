package com.sdtv.player;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.view.View;

import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        IjkVideoView ijkVideoView = findViewById(R.id.video_view);
        ijkVideoView.setHudView(findViewById(R.id.hud_view));
     //   ijkVideoView.setVideoPath("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
        ijkVideoView.setVideoPath("rtmp://58.200.131.2:1935/livetv/cctv1");
        ijkVideoView.start();

    }
}