package com.sdtv.player;

import android.view.View;

/**
 * @author dingyx
 * @description: 视频播放控制器接口
 * @date: 2021/3/30
 */
public interface IMediaController {

    void hide();

    boolean isShowing();

    void setAnchorView(View view);

    void setEnabled(boolean enabled);

    void setMediaPlayer(AndroidMediaController.MediaPlayerControl player);

    void show(int timeout);

    void  show();

    void showOnce(View view);
}
