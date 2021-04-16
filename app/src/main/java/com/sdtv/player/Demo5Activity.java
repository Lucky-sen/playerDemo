package com.sdtv.player;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;

/**
 * 作者：admin016
 * 日期时间: 2021/4/9 10:04
 * 内容描述:
 */
public class Demo5Activity extends AppCompatActivity implements View.OnClickListener{

    private Button btnPlay,btnPause,btnLast,btnNext;
    private SeekBar seekBar;
    private String PLAYING_TAG;
    private MyServiceConnection myServiceConnection;
    private String path = "http://ws.stream.fm.qq.com/vfm.tc.qq.com/R196000HR7L811pkZW.m4a?fromtag=36&vkey=527619E89046B341B2AD17554160F9B40137645FFAC5E44440A933CE2962999714FAA1598B9CD4D50D9ADA9FF7308899D31799CBD2C98507&guid=10000";
    public MusicService.MyBinder myBinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_five);
        initView();
        EventBus.getDefault().register(this);
        PLAYING_TAG = getPackageName();
        Intent intent = new Intent(this,MusicService.class);
        intent.putExtra("path", path);
        myServiceConnection = new MyServiceConnection();
        bindService(intent, myServiceConnection, BIND_AUTO_CREATE);
    }

    private void initView(){
        btnPlay = findViewById(R.id.btn_play);
        btnPause = findViewById(R.id.btn_pause);
        btnLast = findViewById(R.id.btn_last);
        btnNext = findViewById(R.id.btn_next);
        seekBar = findViewById(R.id.seekBar);

        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnLast.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_play:

                break;
            case R.id.btn_pause:

                break;
            case R.id.btn_last:

                break;
            case R.id.btn_next:

                break;
            default:
                break;
        }
    }
}
