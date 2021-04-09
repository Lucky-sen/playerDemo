package com.sdtv.player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import tv.danmaku.ijk.media.player.IMediaPlayer;


/**
 * @author dingyx
 * @description: 后台播放 Service
 * @date: 2021/3/30
 */
public class MediaPlayerService extends Service {

    private static IMediaPlayer sMediaPlayer;

    public static void intentToStart(Context context) {
        context.startService(new Intent(context,MediaPlayerService.class));
    }

    public static void intentToStop(Context context) {
        context.stopService(new Intent(context, MediaPlayerService.class));
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void setMediaPlayer(IMediaPlayer mp) {
        if (sMediaPlayer != null && sMediaPlayer != mp) {
            if (sMediaPlayer.isPlaying()) {
                sMediaPlayer.stop();
            }
            sMediaPlayer.release();
            sMediaPlayer = null;
        }
        sMediaPlayer = mp;
    }

    public static IMediaPlayer getsMediaPlayer() {
        return sMediaPlayer;
    }


}
