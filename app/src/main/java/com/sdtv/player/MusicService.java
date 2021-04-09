package com.sdtv.player;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 作者：admin016
 * 日期时间: 2021/4/9 10:10
 * 内容描述:
 */
public class MusicService extends Service {

    private IjkMediaPlayer mediaPlayer;
    private String path; //播放地址
    private boolean isThreadStop; //是否停止线程

    @Override
    public void onCreate() {
        super.onCreate();
        //创建播放器
        mediaPlayer = new IjkMediaPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        isThreadStop = false;
        path = intent.getStringExtra("path");
        try{

        }catch (Exception e){
            e.printStackTrace();
            Log.d("musicService", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isThreadStop = true;
        if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.release();
        }
        return super.onUnbind(intent);
    }

    class MyBinder extends Binder implements MediaListener{

        @Override
        public void play() {
            mediaPlayer.start();
            updateSeekBar();
        }

        @Override
        public void pause() {
            if(mediaPlayer!=null && mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }
        }

        @Override
        public void moveon() {
            mediaPlayer.start();
        }

        @Override
        public void rePlay() {
            mediaPlayer.start();
        }

        @Override
        public void seekToPosition(int position) {
            mediaPlayer.seekTo(position);
        }

        @Override
        public long getCurrentPosition() {
            return 0;
        }

        private void updateSeekBar(){
            new Thread(){
                @Override
                public void run() {
                  while (!isThreadStop){
                      try {
                          long currentPosition = mediaPlayer.getCurrentPosition();

                        /*
                            发送数据给activity
                         */
//                       //方法1，通过handler。但是这样有好多静态变量
//                        Message message = Message.obtain();
//                        message.what=3;
//                        message.arg1=currentPosition;
//                        MainActivity.handler.sendMessage(message);
                          //方法2，通过广播
                          //方法3，使用EventBus实现
                          EventBus.getDefault().post(new UpdateUI(currentPosition,3));
                          Thread.sleep(1000);
                      } catch (InterruptedException e) {
                          e.printStackTrace();
                      }
                  }
                }
            }.start();
        }
    }
}
