package com.sdtv.player;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.sdtv.player.MybgService.ACTION_NEXT_SONG;
import static com.sdtv.player.MybgService.ACTION_PLAY_AND_PAUSE;
import static com.sdtv.player.MybgService.ACTION_PRE_SONG;

/**
 * 作者：admin016
 * 日期时间: 2021/4/12 17:10
 * 内容描述:
 */
public class Demo6Activity extends AppCompatActivity implements View.OnClickListener, MediaBroadCastReciver.CastMediaPlayListener {

    private Button btnPlay,btnPause,btnStop,btnLast,btnNext;
    private TextView tvCurrent,tvMax;
    private SeekBar seekBar;
    private MybgService.Mybind mybind;
    private String pathThree = "http://ws.stream.fm.qq.com/vfm.tc.qq.com/R196003KgKXQ4MCmXG.m4a?fromtag=36&vkey=0A3F40799ED5C015BB6910C8669D7CE6D19CD7A6B61844C1157B55FEF7DA3B9274A9AA33D2E23B2B5272FD617C0D0E231D60B0A45FDD78FA&guid=10000";
    private String pathTwo = "http://www.170mv.com/kw/antiserver.kuwo.cn/anti.s?rid=MUSIC_93477122&response=res&format=mp3|aac&type=convert_url&br=128kmp3&agent=iPhone&callback=getlink&jpcallback=getlink.mp3";
    private String path = "http://ws.stream.fm.qq.com/vfm.tc.qq.com/R196000HR7L811pkZW.m4a?fromtag=36&vkey=527619E89046B341B2AD17554160F9B40137645FFAC5E44440A933CE2962999714FAA1598B9CD4D50D9ADA9FF7308899D31799CBD2C98507&guid=10000";
    private List<String> pathList = new ArrayList<>();
    private Intent intent;
    MyServiceConnection connection;
    private SimpleDateFormat format;
    //防止拖动进度条时，计时器更改进度条与手动拖动冲突
    private boolean isSeekBarChanging = false;
    private MediaBroadCastReciver broadCastReciver;
    //当前所播放的音频
    private int playPosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo6);
        initView();
        initData();
        intent = new Intent(this,MybgService.class);
        format = new SimpleDateFormat("mm:ss");
        broadCastReciver = new MediaBroadCastReciver();
        broadCastReciver.setCastMediaPlayListener(this);
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ACTION_NEXT_SONG);
        intentFilter.addAction(ACTION_PLAY_AND_PAUSE);
        intentFilter.addAction(ACTION_PRE_SONG);
        registerReceiver(broadCastReciver,intentFilter);
    }

    private void initView(){
        btnPlay = findViewById(R.id.btn_play);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);
        seekBar = findViewById(R.id.seek_bar);
        tvCurrent = findViewById(R.id.tv_current);
        tvMax = findViewById(R.id.tv_max);
        btnLast = findViewById(R.id.btn_last);
        btnNext = findViewById(R.id.btn_next);

        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnLast.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarChanging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarChanging = false;
                mybind.seekTo(seekBar.getProgress());
            }
        });
    }

    /**
     * 获取音频播放列表
     */
    private void initData(){
        pathList.add(path);
        pathList.add(pathTwo);
        pathList.add(pathThree);
    }

    @Override
    public void playNext() {
        playNextMedia();
    }

    @Override
    public void playAndPause() {
        playPause();
    }

    @Override
    public void playLast() {
        playLastMedia();
    }

    class MyServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mybind = (MybgService.Mybind) iBinder;
            tvCurrent.setText(format.format(mybind.getMusicCurrentPosition()));

            //连接之后启动子线程设置当前进度
            new Thread() {
                public void run()
                {
                    //改变当前进度条的值
                    //设置当前进度
                    while (true) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setMax((int) mybind.getMusicDuration());
                                tvMax.setText(format.format(mybind.getMusicDuration()));
                                if(!isSeekBarChanging){
                                    seekBar.setProgress(mybind.getMusicCurrentPosition());
                                }
                                tvCurrent.setText(format.format(mybind.getMusicCurrentPosition()));
                            }
                        });
                        try {
                            Thread.sleep(200);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_play:
                intent.putExtra("action","play");
                intent.putExtra("path", pathList.get(playPosition));
                startService(intent);//开启服务
                if(connection == null){
                    connection = new MyServiceConnection();
                    bindService(intent,connection,BIND_AUTO_CREATE);//建立和service连接
                }

                break;
            case R.id.btn_pause:
                intent.putExtra("action","pause");
                startService(intent);
                break;
            case R.id.btn_stop:
                intent.putExtra("action", "stop");
                startService(intent);
                break;
            case R.id.btn_last:

                break;
            case R.id.btn_next:

                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑服务：注意bindService后 必须要解绑服务，否则会报 连接资源异常
        if (null != connection) {
            unbindService(connection);
        }
    }

    private void playNextMedia(){
        playPosition = playPosition + 1;
        if(playPosition > pathList.size() - 1){
            playPosition = 0;
        }
        String path = pathList.get(playPosition);
        intent.putExtra("action", "next");
        intent.putExtra("path",path);
        startService(intent);
    }

    private void playLastMedia(){
        playPosition = playPosition - 1;
        if(playPosition < 0){
            playPosition = pathList.size() - 1;
        }
        String path = pathList.get(playPosition);
        intent.putExtra("action", "last");
        intent.putExtra("path",path);
        startService(intent);
    }

    private void playPause(){
        intent.putExtra("action", "pause");
        startService(intent);
    }

    private void play(String path){
        intent.putExtra("action","play");
        intent.putExtra("path", path);
        startService(intent);//开启服务
    }
}
