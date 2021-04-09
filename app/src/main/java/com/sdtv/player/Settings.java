package com.sdtv.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * @author dingyx
 * @description: Player Settings
 * @date: 2021/3/30
 */
public class Settings {

    private final SharedPreferences mSharedPreferences;

    //  默认采用 IjkMediaPlayer 播放器进行播放
    public static final int PV_PLAYER__Auto = 0;
    public static final int PV_PLAYER_AndroidMediaPlayer = 1;
    public static final int PV_PLAYER_IjkMediaPlayer = 2;
    public static final int PV_PLAYER_IjkExoMediaPlayer = 3;

    public Settings(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    /**
     * 是否允许后台播放
     *
     * @return
     */
    public boolean getEnableBackgroundPlay() {
        return mSharedPreferences.getBoolean(Constant.PREF_KEY_ENABLE_BACKGROUND_PLAY, false);
    }

    public int getPlayer() {
        return mSharedPreferences.getInt(Constant.PREF_KEY_PLAYER, PV_PLAYER__Auto);
    }

    public boolean getUsingMediaCodec() {
        return mSharedPreferences.getBoolean(Constant.PREF_KEY_USING_MEDIA_CODEC, false);
    }

    public boolean getUsingMediaCodecAutoRotate() {
        return mSharedPreferences.getBoolean(Constant.PREF_USING_MEDIA_CODEC_AUTO_ROTATE, false);
    }


    public boolean getMediaCodecHandleResolutionChange() {
        return mSharedPreferences.getBoolean(Constant.PREF_MEDIA_CODEC_HANDLE_RESOLUTION_CHANGE, false);
    }

    public boolean getUsingOpenSLES() {
        return mSharedPreferences.getBoolean(Constant.PREF_USING_OPENSL_ES, false);
    }

    public String getPixelFormat() {
        return mSharedPreferences.getString(Constant.PREF_PIXEL_FORMAT, "");
    }

    public boolean getEnableNoView() {
        return mSharedPreferences.getBoolean(Constant.PREF_ENABLE_NO_VIEW, false);
    }

    public boolean getEnableSurfaceView() {
        return mSharedPreferences.getBoolean(Constant.PREF_ENABLE_SURFACE_VIEW, false);
    }

    public boolean getEnableTextureView() {
        return mSharedPreferences.getBoolean(Constant.PREF_ENABLE_TEXTURE_VIEW, false);
    }

    public boolean getEnableDetachedSurfaceTextureView() {
        return mSharedPreferences.getBoolean(Constant.PREF_ENABLE_DETACHED_SURFACE_TEXTURE, false);
    }

    public boolean getUsingMediaDataSource() {
        return mSharedPreferences.getBoolean(Constant.PREF_USING_MEDIA_DATA_SOURCE, false);
    }

    public String getLastDirectory() {
        return mSharedPreferences.getString(Constant.PREF_LAST_DIRECTORY, "/");
    }

    public void setLastDirectory(String path) {
        mSharedPreferences.edit().putString(Constant.PREF_LAST_DIRECTORY, path).apply();
    }

}
