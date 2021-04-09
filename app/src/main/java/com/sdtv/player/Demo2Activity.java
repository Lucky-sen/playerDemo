package com.sdtv.player;

import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import tv.danmaku.ijk.media.example.widget.media.MyIjkPlayer;

/**
 * 作者：admin016
 * 日期时间: 2021/3/31 14:22
 * 内容描述:
 */
public class Demo2Activity extends AppCompatActivity {

    private MyIjkPlayer videoPlayer;
    private SeekBar seekBarProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_two);
        videoPlayer = findViewById(R.id.video_player);
        seekBarProgress = findViewById(R.id.sb_progress);

        String videoUrl = "http://www.170mv.com/kw/antiserver.kuwo.cn/anti.s?rid=MUSIC_93477122&response=res&format=mp3|aac&type=convert_url&br=128kmp3&agent=iPhone&callback=getlink&jpcallback=getlink.mp3";
//        String videoUrl = "http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4";
        // init player
        //路径
        videoPlayer.setPath(videoUrl);
        videoPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoPlayer!=null){
            videoPlayer.release();
        }
    }

    Handler handler = new Handler();

    private  void startProgressTimer() {
        videoPlayer.postDelayed(progressTask, 300);
    }

    Runnable progressTask = new Runnable() {
        @Override
        public void run() {
            long currentPosition = videoPlayer.getPostionWhenPlaying();
            long duration = videoPlayer.getDuration();
            int progress = (int) ((currentPosition / duration) * 100);
            seekBarProgress.setProgress(progress);
        }
    };


}
