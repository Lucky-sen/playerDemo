package com.sdtv.player;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 作者：admin016
 * 日期时间: 2021/4/2 15:28
 * 内容描述:
 */
public class Demo4Activity extends AppCompatActivity implements View.OnClickListener {

    //正常
    public static final int CURRENT_STATE_NORMAL = 0;
    //准备中
    public static final int CURRENT_STATE_PREPAREING = 1;
    //播放中
    public static final int CURRENT_STATE_PLAYING = 2;
    //开始缓冲
    public static final int CURRENT_STATE_PLAYING_BUFFERING_START = 3;
    //暂停
    public static final int CURRENT_STATE_PAUSE = 5;
    //自动播放结束
    public static final int CURRENT_STATE_AUTO_COMPLETE = 6;
    //错误状态
    public static final int CURRENT_STATE_ERROR = 7;

    //避免切换时频繁setup
    public static final int CHANGE_DELAY_TIME = 2000;

    //当前的播放状态
    protected int mCurrentState = -1;


    private String pathThree = "http://ws.stream.fm.qq.com/vfm.tc.qq.com/R196003KgKXQ4MCmXG.m4a?fromtag=36&vkey=0A3F40799ED5C015BB6910C8669D7CE6D19CD7A6B61844C1157B55FEF7DA3B9274A9AA33D2E23B2B5272FD617C0D0E231D60B0A45FDD78FA&guid=10000";

    private String pathTwo = "http://www.170mv.com/kw/antiserver.kuwo.cn/anti.s?rid=MUSIC_93477122&response=res&format=mp3|aac&type=convert_url&br=128kmp3&agent=iPhone&callback=getlink&jpcallback=getlink.mp3";

    private String path = "http://ws.stream.fm.qq.com/vfm.tc.qq.com/R196000HR7L811pkZW.m4a?fromtag=36&vkey=527619E89046B341B2AD17554160F9B40137645FFAC5E44440A933CE2962999714FAA1598B9CD4D50D9ADA9FF7308899D31799CBD2C98507&guid=10000";

    private List<String> urlList = new ArrayList<>();

    private Button play, pause, stop, volume_plus, volume_decrease, last, next;
    private TextView musicName, musicLength, musicCur;
    private SeekBar seekBar;

    private IjkMediaPlayer mediaPlayer;

    private AudioManager audioManager;

    private Timer timer;

    int maxVolume, currentVolume;

    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。

    private int urlPosition; // 当前播放音乐的位置

    private int currentPosition; //当前音乐进度的位置

    SimpleDateFormat format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_four);

        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

        format = new SimpleDateFormat("mm:ss");

        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        stop = findViewById(R.id.stop);
        last = findViewById(R.id.last);
        next = findViewById(R.id.next);
        volume_plus = findViewById(R.id.volume_plus);
        volume_decrease = findViewById(R.id.volume_decrease);

        musicName = (TextView) findViewById(R.id.music_name);
        musicLength = (TextView) findViewById(R.id.music_length);
        musicCur = (TextView) findViewById(R.id.music_cur);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new MySeekBar());
        seekBar.setKeyProgressIncrement(1);

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        last.setOnClickListener(this);
        next.setOnClickListener(this);
        volume_plus.setOnClickListener(this);
        volume_decrease.setOnClickListener(this);
        initMediaPlayer();
        initData();
    }

    private void initData() {
        urlList.add(path);
        urlList.add(pathTwo);
        urlList.add(pathThree);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:
                play(urlList.get(urlPosition));
                break;
            case R.id.pause:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();//暂停播放
                }
                break;
            case R.id.stop:
                Toast.makeText(this, "停止播放", Toast.LENGTH_SHORT).show();
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.reset();//停止播放
//                    initMediaPlayer();
                }
                break;
            //音量加
            case R.id.volume_plus:
                maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                Toast.makeText(this, "音量增加,最大音量是：" + maxVolume + "，当前音量" + currentVolume,
                        Toast.LENGTH_SHORT).show();
                break;
            //音量减
            case R.id.volume_decrease:
                maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                Toast.makeText(this, "音量减小,最大音量是：" + maxVolume + "，当前音量" + currentVolume,
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.last:
                urlPosition = urlPosition - 1;
                if(urlPosition < 0){
                    urlPosition = urlList.size() - 1;
                }
                play(urlList.get(urlPosition));
                break;
            case R.id.next:
                urlPosition = urlPosition + 1;
                if(urlPosition > urlList.size() - 1){
                    urlPosition = 0;
                }
                play(urlList.get(urlPosition));
                break;
            default:
                break;
        }
    }

    /**
     * 播放方法
     *
     * @param path
     */
    private void play(String path) {
        try {
            if(urlList.get(urlPosition).equals(mediaPlayer.getDataSource())){
                mediaPlayer.start();
                return;
            }else{
                mediaPlayer.reset();
            }
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer iMediaPlayer) {
                    iMediaPlayer.start();
                    iMediaPlayer.seekTo(currentPosition);
                    seekBar.setMax((int) iMediaPlayer.getDuration());
                    musicLength.setText(format.format(mediaPlayer.getDuration()) + "");
                    musicCur.setText(format.format(mediaPlayer.getCurrentPosition()) + "");
                }
            });
            //监听播放时回调函数
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isSeekBarChanging) {
//                        seekBar.setProgress((int) mediaPlayer.getCurrentPosition());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setProgress((int) mediaPlayer.getCurrentPosition());
//                                seekBar.setSecondaryProgress((int) mediaPlayer.getAudioCachedDuration());
                                musicCur.setText(format.format(mediaPlayer.getCurrentPosition()) + "");
                            }
                        });
                    }
                }
            }, 0, 50);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSeekBarChanging = true;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    //初始化音乐播放
    void initMediaPlayer() {
        //进入Idle
        if (mediaPlayer == null) {
            mediaPlayer = new IjkMediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    /*进度条处理*/
    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        /*滚动时,应当暂停后台定时器*/
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }

        /*滑动结束后，重新设置值*/
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            mediaPlayer.seekTo(seekBar.getProgress());
        }
    }
}
