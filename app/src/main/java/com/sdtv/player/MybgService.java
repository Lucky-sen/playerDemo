package com.sdtv.player;

/**
 * 作者：admin016
 * 日期时间: 2021/4/12 17:19
 * 内容描述:
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class MybgService extends Service  {

    private final String TAG = MybgService.class.getSimpleName();
    public static String ACTION_PRE_SONG = "pre_song";
    public static String ACTION_PLAY_AND_PAUSE = "play_and_pause";
    public static String ACTION_NEXT_SONG = "next_song";

    MediaBroadCastReciver broadCastReciver;

    NotificationManager notificationManager;

    private IjkMediaPlayer mediaPlayer;

    private String playPath;

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * 进程优先级别：前台进程，可视进程，服务进程，后台进程，空进程  （前台进程是最稳定，系统内存不足是先回收 空进程）
         *
         * 为什么要把服务Service提升为前台进程，在内存不足时，前台进程不会那么容易被系统回收
         *
         * 把 服务进程 提升到 前台进程 会自动绑定通知
         */

        // 需要用到通知，用户点击通知栏，就计划APP-->Activity

        // 这是以前到写法，已经过时
        /*Notification notification = new
                Notification(R.mipmap.ic_launcher, "我的音乐播放器", System.currentTimeMillis());*/
        /**
         * Oreo不用Priority了，用importance
         * IMPORTANCE_NONE 关闭通知
         * IMPORTANCE_MIN 开启通知，不会弹出，但没有提示音，状态栏中无显示
         * IMPORTANCE_LOW 开启通知，不会弹出，不发出提示音，状态栏中显示
         * IMPORTANCE_DEFAULT 开启通知，不会弹出，发出提示音，状态栏中显示
         * IMPORTANCE_HIGH 开启通知，会弹出，发出提示音，状态栏中显示
         */
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        String CHANNEL_ID = "My_Test_Id";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "testChannel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        RemoteViews contentViews = getContentNormalView();
        builder.setContent(contentViews)
                .setSmallIcon(R.mipmap.dog)
                .setWhen(System.currentTimeMillis())
//                .setAutoCancel(false)  //打开程序后图标消失
                .setDefaults(Notification.DEFAULT_ALL) //设置默认的提示音，振动方式，灯光
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_accept));
        //         设置事件信息，点击通知可以跳转到指定Activity
        Intent intent = new Intent(this, Demo6Activity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        //播放上一首
        Intent previousButtonIntent = new Intent();
        previousButtonIntent.setAction(ACTION_PRE_SONG);
        PendingIntent pendPreviousButtonIntent = PendingIntent.getBroadcast(this, 0, previousButtonIntent, 0);
        contentViews.setOnClickPendingIntent(R.id.btn_last, pendPreviousButtonIntent);
        builder.setContentIntent(pendingIntent);
        //播放/暂停添加点击监听
        Intent playPauseButtonIntent = new Intent();
        playPauseButtonIntent.setAction(ACTION_PLAY_AND_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(this, 0, playPauseButtonIntent, 0);
        contentViews.setOnClickPendingIntent(R.id.btn_pause, playPausePendingIntent);
        //下一首图标添加监听
        Intent nextButtonIntent = new Intent(ACTION_NEXT_SONG);
        PendingIntent pendNextButtonIntent = PendingIntent.getBroadcast(this, 0, nextButtonIntent, 0);
        contentViews.setOnClickPendingIntent(R.id.btn_next, pendNextButtonIntent);

        Notification notification = builder.build();
        startForeground(1011, notification);


//        IntentFilter intentFilter=new IntentFilter();
//        intentFilter.addAction(ACTION_NEXT_SONG);
//        intentFilter.addAction(ACTION_PLAY_AND_PAUSE);
//        intentFilter.addAction(ACTION_PRE_SONG);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadCastReciver);
    }

    private RemoteViews getContentNormalView() {
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.view_normal_notify);
        return mRemoteViews;
    }


    public class Mybind extends Binder {
        //获取歌曲长度
        public int getMusicDuration() {
            int rtn = 0;
            if (mediaPlayer != null) {
                rtn = (int) mediaPlayer.getDuration();
            }

            return rtn;
        }
        //获取当前播放进度
        public int getMusicCurrentPosition() {
            int rtn = 0;
            if (mediaPlayer != null) {
                rtn = (int) mediaPlayer.getCurrentPosition();

            }

            return rtn;
        }

        public void seekTo(int position) {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(position);
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "绑定成功");
        return new Mybind();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "解绑成功");

        // 为什么解绑服务了，音乐还在播放，应该MediaPlay内部是一个服务

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release(); // 释放硬件播放资源
        }

        stopForeground(true);

//        if(notificationManager!=null){
//            notificationManager.cancel(1011);
//        }
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //获取意图传递的信息
        String action = intent.getStringExtra("action");
        String path = intent.getStringExtra("path");
        switch (action)
        {
            case "play":
                if (mediaPlayer == null)
                {
                    mediaPlayer = new IjkMediaPlayer();
                }
                try{
                    Intent intent1 = new Intent(this,LockScreenService.class);
                    startService(intent1);
                    if(playPath!=null && playPath.equals(path)){
                        mediaPlayer.start();
                    }else {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(path);
                        playPath = path;
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(IMediaPlayer iMediaPlayer) {
                                iMediaPlayer.start();
                            }
                        });
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

                break;
            case "stop":
                if (mediaPlayer !=null)
                {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                break;
            case "pause":
                if(mediaPlayer !=null){
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                        Intent intent1 = new Intent(this,LockScreenService.class);
                        stopService(intent1);
                    }else{
                        mediaPlayer.start();
                    }
                }
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }
}


