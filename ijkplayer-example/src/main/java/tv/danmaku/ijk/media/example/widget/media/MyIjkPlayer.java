package tv.danmaku.ijk.media.example.widget.media;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 作者：admin016
 * 日期时间: 2021/3/31 15:05
 * 内容描述:
 */
public class MyIjkPlayer extends FrameLayout {
    private Context context;
    private IMediaPlayer mMediaPlayer = null;//视频控制类
//    private VideoPlayerListener mVideoPlayerListener;//自定义监听器
//    private SurfaceView mSurfaceView;//播放视图
    private String mPath = "";//视频文件地址

    public MyIjkPlayer(@NonNull Context context) {
        super(context);
        initVideoView(context);
    }

    public MyIjkPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);
    }

    public MyIjkPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyIjkPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initVideoView(Context context) {
        this.context = context;
        setFocusable(true);
    }

    public void setPath(String path) {
//        if (TextUtils.equals("", mPath)) {
//            mPath = path;
//            initSurfaceView();
//        } else {
            mPath = path;
            loadVideo();
//        }
    }

    public long getPostionWhenPlaying(){
        return mMediaPlayer.getCurrentPosition();
    }

    public long getDuration(){
        return mMediaPlayer.getDuration();
    }

//    private void initSurfaceView() {
//        mSurfaceView = new SurfaceView(context);
//        mSurfaceView.getHolder().addCallback(new LmnSurfaceCallback());
//        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
//        mSurfaceView.setLayoutParams(layoutParams);
//        this.addView(mSurfaceView);
//    }

    //surfaceView的监听器
//    private class LmnSurfaceCallback implements SurfaceHolder.Callback {
//        @Override
//        public void surfaceCreated(SurfaceHolder holder) {
//        }
//
//        @Override
//        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            loadVideo();
//        }
//
//        @Override
//        public void surfaceDestroyed(SurfaceHolder holder) {
//        }
//    }

    //加载视频
    private void loadVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }

        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        mMediaPlayer = ijkMediaPlayer;

//        if (mVideoPlayerListener != null) {
//            mMediaPlayer.setOnPreparedListener(mVideoPlayerListener);
//            mMediaPlayer.setOnErrorListener(mVideoPlayerListener);
//        }
        try {
            mMediaPlayer.setDataSource(mPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        mMediaPlayer.setDisplay(mSurfaceView.getHolder());
        mMediaPlayer.prepareAsync();
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
