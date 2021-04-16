package com.sdtv.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import static com.sdtv.player.MybgService.ACTION_NEXT_SONG;
import static com.sdtv.player.MybgService.ACTION_PLAY_AND_PAUSE;
import static com.sdtv.player.MybgService.ACTION_PRE_SONG;

/**
 * 作者：admin016
 * 日期时间: 2021/4/14 16:58
 * 内容描述:
 */
public class MediaBroadCastReciver  extends BroadcastReceiver {

    private CastMediaPlayListener castMediaPlayListener;

    public void setCastMediaPlayListener(CastMediaPlayListener castMediaPlayListener) {
        this.castMediaPlayListener = castMediaPlayListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null && intent.getAction()!=null){
            if(ACTION_PRE_SONG.equals(intent.getAction())){
                if(castMediaPlayListener!=null){
                    castMediaPlayListener.playLast();
                }
                Log.d("test", "播放上一首");
            }else if(ACTION_PLAY_AND_PAUSE.equals(intent.getAction())){
                if(castMediaPlayListener!=null){
                    castMediaPlayListener.playAndPause();
                }
                Log.d("test", "播放/暂停");
            }else if(ACTION_NEXT_SONG.equals(intent.getAction())){
                if(castMediaPlayListener!=null){
                    castMediaPlayListener.playNext();
                }
                Log.d("test", "播放下一首");
            }
        }
    }

    public interface CastMediaPlayListener{
        void playNext();
        void playAndPause();
        void playLast();
    }
}
