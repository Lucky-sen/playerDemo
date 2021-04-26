package com.sdtv.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.sdtv.player.MybgService.ACTION_NEXT_SONG;
import static com.sdtv.player.MybgService.ACTION_PLAY_AND_PAUSE;
import static com.sdtv.player.MybgService.ACTION_PRE_SONG;

/**
 * 作者：sen.dong
 * 日期时间: 2021/4/25 14:40
 * 内容描述: 音乐播放服务
 */
public class MusicPlayerService extends Service {

    private final String TAG = MusicPlayerService.class.getSimpleName();

    public static String ACTION_PRE_SONG = "pre_song";
    public static String ACTION_PLAY_AND_PAUSE = "play_and_pause";
    public static String ACTION_NEXT_SONG = "next_song";

    NotificationManager notificationManager;

    private IjkMediaPlayer mediaPlayer;

    private String playPath;

    //通知栏当前标记播放状态
    private boolean isPlaying = true;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotification();
    }

    /**
     * 关键通知栏以及响应事件
     * 进程优先级别：前台进程，可视进程，服务进程，后台进程，空进程  （前台进程是最稳定，系统内存不足是先回收 空进程）
     * 为什么要把服务Service提升为前台进程，在内存不足时，前台进程不会那么容易被系统回收
     * 把 服务进程 提升到 前台进程 会自动绑定通知
     * Oreo不用Priority了，用importance
     * IMPORTANCE_NONE 关闭通知
     * IMPORTANCE_MIN 开启通知，不会弹出，但没有提示音，状态栏中无显示
     * IMPORTANCE_LOW 开启通知，不会弹出，不发出提示音，状态栏中显示
     * IMPORTANCE_DEFAULT 开启通知，不会弹出，发出提示音，状态栏中显示
     * IMPORTANCE_HIGH 开启通知，会弹出，发出提示音，状态栏中显示
     */
    private void createNotification() {
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
        builder.setContentIntent(pendingIntent);
        //播放上一首
        Intent previousButtonIntent = new Intent();
        previousButtonIntent.setAction(ACTION_PRE_SONG);
        PendingIntent pendPreviousButtonIntent = PendingIntent.getBroadcast(this, 0, previousButtonIntent, 0);
        contentViews.setOnClickPendingIntent(R.id.btn_last, pendPreviousButtonIntent);
        //播放/暂停添加点击监听
        Intent playPauseButtonIntent = new Intent();
        playPauseButtonIntent.setAction(ACTION_PLAY_AND_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(this, 0, playPauseButtonIntent, 0);
        contentViews.setOnClickPendingIntent(R.id.btn_pause, playPausePendingIntent);
        if(isPlaying){
            contentViews.setTextViewText(R.id.btn_pause, "暂停");
        }else {
            contentViews.setTextViewText(R.id.btn_pause, "播放");
        }
        //下一首图标添加监听
        Intent nextButtonIntent = new Intent(ACTION_NEXT_SONG);
        PendingIntent pendNextButtonIntent = PendingIntent.getBroadcast(this, 0, nextButtonIntent, 0);
        contentViews.setOnClickPendingIntent(R.id.btn_next, pendNextButtonIntent);
        Notification notification = builder.build();
        startForeground(1011, notification);
    }

    private RemoteViews getContentNormalView() {
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.view_normal_notify);
        return mRemoteViews;
    }

    private void play(String path){
        playPath = path;
        if(mediaPlayer == null){
            mediaPlayer = new IjkMediaPlayer();
        }
        try{
            mediaPlayer.reset();
            mediaPlayer.setDataSource(playPath);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer iMediaPlayer) {
                    iMediaPlayer.start();
                    isPlaying = true;
                    startLockScreenService();
                }
            });
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * 歌曲在播放时，开启锁屏服务
     */
    private void startLockScreenService(){
        Intent intent = new Intent(this,LockScreenService.class);
        startService(intent);
    }

    /**
     * 歌曲暂停或者停止，关闭锁屏服务
     */
    private void stopLockScreenService(){
        Intent intent = new Intent(this,LockScreenService.class);
        stopService(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = "";
        String path = "";
        if(intent!=null){
            action = intent.getStringExtra("action");
            path = intent.getStringExtra("path");
        }
        switch (action){
            case "play":
                play(path);
                break;
            case "pause":
                if(mediaPlayer!=null){
                    mediaPlayer.pause();
                    isPlaying = false;
                    stopLockScreenService();
                    createNotification();
                }
                break;
            case "continue":
                startLockScreenService();
                mediaPlayer.start();
                isPlaying = true;
                createNotification();
                break;
            case "stop":
                if(mediaPlayer!=null){
                    stopLockScreenService();
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    isPlaying = false;
                }
                break;
            default:
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release(); // 释放硬件播放资源
        }
        //停止前台服务
        stopForeground(true);
        stopLockScreenService();
        return super.onUnbind(intent);
    }



    public class MyBinder extends Binder{
        //当前音乐是否在播放
        public boolean isMediaPlaying(){
            boolean isPlaying = false;
            if(mediaPlayer!=null){
                isPlaying = mediaPlayer.isPlaying();
            }
            return isPlaying;
        }

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
}
