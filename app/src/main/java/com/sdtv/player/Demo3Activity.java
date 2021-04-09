package com.sdtv.player;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 作者：admin016
 * 日期时间: 2021/4/1 15:38
 * 内容描述:
 */
public class Demo3Activity extends AppCompatActivity{

    private String path = "http://www.170mv.com/kw/antiserver.kuwo.cn/anti.s?rid=MUSIC_93477122&response=res&format=mp3|aac&type=convert_url&br=128kmp3&agent=iPhone&callback=getlink&jpcallback=getlink.mp3";

    private IjkMediaPlayer mMediaPlayer;

    private SeekBar sbProgress;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_three);
        sbProgress = findViewById(R.id.seekbar_progress);
        doPlay(path);

    }

    private void doPlay(String path) {
        try {
            mMediaPlayer = new IjkMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
            //MediaPlayer调用seek()方法时触发该监听器
            mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer mp) {

                    //发送播放中广播
//                    AudioBroadcastReceiver.sendPlayReceiver(mContext, audioInfo);
//                    mWorkerHandler.removeMessages(MESSAGE_WHAT_LOADPLAYPROGRESSDATA);
//                    mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOADPLAYPROGRESSDATA);

                    mMediaPlayer.start();
                }
            });

            //Media Player的播放完成事件绑定事件监听器
            mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {

//                    if (audioInfo.getType() == AudioInfo.TYPE_NET && mMediaPlayer.getCurrentPosition() < (audioInfo.getDuration() - 2 * 1000)) {
//                        releasePlayer();
//                        //网络歌曲未播放全部，需要重新调用播放歌曲
//                        handleSong(audioInfo);
//                    } else {
//                        releasePlayer();
//                        //播放完成，执行下一首操作
//                        AudioPlayerManager.newInstance(mContext).next();
//                    }

                }
            });

            mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
//                    if (audioInfo.getPlayProgress() != 0) {
//                        mMediaPlayer.seekTo(audioInfo.getPlayProgress());
//                    } else {

                        //发送播放中广播
//                        AudioBroadcastReceiver.sendPlayReceiver(mContext, audioInfo);
//                        mWorkerHandler.removeMessages(MESSAGE_WHAT_LOADPLAYPROGRESSDATA);
//                        mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOADPLAYPROGRESSDATA);

                        mMediaPlayer.start();

//                    }
                }
            });
            //为MediaPlayer的播放错误事件绑定事件监听器。
            mMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {

//                    handleError();

                    return false;
                }
            });
        } catch (Exception exception){
            exception.printStackTrace();
            Log.d("dsss", exception.getMessage());
        }
    }

    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if(handler!=null && mMediaPlayer.isPlaying()){

            }
        }
    };

    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer!=null){
            mMediaPlayer.release();
        }
    }
}
