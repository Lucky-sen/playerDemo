package com.sdtv.player;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
import static com.sdtv.player.MusicPlayerService.ACTION_NEXT_SONG;
import static com.sdtv.player.MusicPlayerService.ACTION_PLAY_AND_PAUSE;
import static com.sdtv.player.MusicPlayerService.ACTION_PRE_SONG;

/**
 * 作者：sen.dong
 * 日期时间: 2021/4/25 10:30
 * 内容描述:
 */
public class MusicPlayerActivity extends AppCompatActivity implements View.OnClickListener, MediaBroadCastReciver.CastMediaPlayListener {

    private Button btnPlay, btnPause, btnStop, btnLast, btnNext;
    private SeekBar seekBar;
    private TextView tvCurrent,tvMax;
    private String pathThree = "http://ws.stream.fm.qq.com/vfm.tc.qq.com/R196003KgKXQ4MCmXG.m4a?fromtag=36&vkey=0A3F40799ED5C015BB6910C8669D7CE6D19CD7A6B61844C1157B55FEF7DA3B9274A9AA33D2E23B2B5272FD617C0D0E231D60B0A45FDD78FA&guid=10000";
    private String pathTwo = "http://www.170mv.com/kw/antiserver.kuwo.cn/anti.s?rid=MUSIC_93477122&response=res&format=mp3|aac&type=convert_url&br=128kmp3&agent=iPhone&callback=getlink&jpcallback=getlink.mp3";
    private String path = "http://ws.stream.fm.qq.com/vfm.tc.qq.com/R196000HR7L811pkZW.m4a?fromtag=36&vkey=527619E89046B341B2AD17554160F9B40137645FFAC5E44440A933CE2962999714FAA1598B9CD4D50D9ADA9FF7308899D31799CBD2C98507&guid=10000";
    private List<String> pathList = new ArrayList<>();
    //当前播放列表播放歌曲
    private int playPosition = 0;
    //启动service Intent
    private Intent intent;
    //activity 与 service交互
    private MusicPlayerService.MyBinder myBinder;
    //格式化
    private SimpleDateFormat format;
    //避免播放进度与手动拖动冲突
    private boolean isSeekBarChanging;
    //服务链接
    private MyServiceConnection connection;
    //音频播放广播
    private MediaBroadCastReciver broadCastReciver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo6);
        initView();
        initData();
    }

    private void initView() {
        btnPlay = findViewById(R.id.btn_play);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);
        btnLast = findViewById(R.id.btn_last);
        btnNext = findViewById(R.id.btn_next);
        tvCurrent = findViewById(R.id.tv_current);
        tvMax = findViewById(R.id.tv_max);
        seekBar = findViewById(R.id.seek_bar);

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
                if(myBinder!=null){
                    myBinder.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    private void initData(){
        intent = new Intent(this,MusicPlayerService.class);
        format = new SimpleDateFormat("mm:ss");
        broadCastReciver = new MediaBroadCastReciver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ACTION_NEXT_SONG);
        intentFilter.addAction(ACTION_PLAY_AND_PAUSE);
        intentFilter.addAction(ACTION_PRE_SONG);
        registerReceiver(broadCastReciver,intentFilter);
        broadCastReciver.setCastMediaPlayListener(this);
        pathList.add(path);
        pathList.add(pathTwo);
        pathList.add(pathThree);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                 intent.putExtra("action", "play");
                 intent.putExtra("path", pathList.get(playPosition));
                 startService(intent);
                 if(connection == null){
                     connection = new MyServiceConnection();
                     bindService(intent, connection, BIND_AUTO_CREATE);
                 }
                break;
            case R.id.btn_pause:
                if("暂停".equals(btnPause.getText().toString())){
                    intent.putExtra("action", "pause");
                    btnPause.setText("继续");
                }else if("继续".equals(btnPause.getText().toString())){
                    intent.putExtra("action", "continue");
                    btnPause.setText("暂停");
                }
                intent.putExtra("path", pathList.get(playPosition));
                startService(intent);
                break;
            case R.id.btn_stop:
                intent.putExtra("action", "stop");
                startService(intent);
                break;
            case R.id.btn_last:
                playLast();
                break;
            case R.id.btn_next:
                playNext();
                break;
        }
    }

    @Override
    public void playNext() {
        playPosition = playPosition + 1;
        if(playPosition > pathList.size() - 1){
            playPosition = 0;
        }
        String playPath = pathList.get(playPosition);
        intent.putExtra("action", "play");
        intent.putExtra("path", playPath);
        startService(intent);
    }

    @Override
    public void playAndPause() {
        if("暂停".equals(btnPause.getText().toString())){
            intent.putExtra("action", "pause");
            btnPause.setText("继续");
        }else if("继续".equals(btnPause.getText().toString())){
            intent.putExtra("action", "continue");
            btnPause.setText("暂停");
        }
        intent.putExtra("path", pathList.get(playPosition));
        startService(intent);
    }

    @Override
    public void playLast() {
        playPosition = playPosition - 1;
        if(playPosition < 0){
            playPosition = pathList.size() - 1;
        }
        String playPath = pathList.get(playPosition);
        intent.putExtra("action", "play");
        intent.putExtra("path", playPath);
        startService(intent);
    }

    /**
     *  服务绑定链接处理
     */
    class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myBinder = (MusicPlayerService.MyBinder) iBinder;
            tvCurrent.setText(format.format(myBinder.getMusicCurrentPosition()));
            new Thread(){
                public void run() {
                    while (true){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setMax((int) myBinder.getMusicDuration());
                                tvMax.setText(format.format(myBinder.getMusicDuration()));
                                if(!isSeekBarChanging){
                                    seekBar.setProgress(myBinder.getMusicCurrentPosition());
                                }
                                tvCurrent.setText(format.format(myBinder.getMusicCurrentPosition()));
                            }
                        });
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
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
    protected void onDestroy() {
        super.onDestroy();
        // 解绑服务：注意bindService后 必须要解绑服务，否则会报 连接资源异常
        if (null != connection) {
            unbindService(connection);
        }
        stopService(intent);
        if(broadCastReciver!=null){
            unregisterReceiver(broadCastReciver);
        }
    }
}
