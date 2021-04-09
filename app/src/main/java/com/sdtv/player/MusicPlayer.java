package com.sdtv.player;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.example.widget.media.FileMediaDataSource;
import tv.danmaku.ijk.media.example.widget.media.IRenderView;
import tv.danmaku.ijk.media.example.widget.media.MediaPlayerCompat;
import tv.danmaku.ijk.media.example.widget.media.SurfaceRenderView;
import tv.danmaku.ijk.media.example.widget.media.TextureRenderView;
import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.TextureMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * 作者：admin016
 * 日期时间: 2021/4/2 15:18
 * 内容描述:
 */
public class MusicPlayer extends FrameLayout implements AndroidMediaController.MediaPlayerControl{


        private Context mAppContext;
        private Settings mSettings;

        // 视频播放时的各种状态
        private static final int STATE_ERROR = -1;
        private static final int STATE_IDLE = 0;
        private static final int STATE_PREPARING = 1;
        private static final int STATE_PREPARED = 2;
        private static final int STATE_PLAYING = 3;
        private static final int STATE_PAUSED = 4;
        private static final int STATE_PLAYBACK_COMPLETED = 5;

        // 播放设置
        private boolean mEnableBackgroundPlay = false;

        private Uri mUri;
        private Map<String, String> mHeaders;
        private String mManifestString;     // 播放器设置相关

        // 播放器
        private IMediaPlayer mMediaPlayer = null;
        private IMediaController mMediaController;

        private int mCurrentState = STATE_IDLE;
        private int mTargetState = STATE_IDLE;

        private int mCurrentBufferPercentage;   // 缓冲进度百分比

        private int mSeekWhenPrepared;  // recording the seek position while preparing

        private long mPrepareStartTime = 0;
        private long mPrepareEndTime = 0;

        private long mSeekStartTime = 0;
        private long mSeekEndTime = 0;

        private boolean mCanPause = true;
        private boolean mCanSeekBack = true;
        private boolean mCanSeekForward = true;

        // 状态回调、提供外部注册
        private IMediaPlayer.OnCompletionListener mOnCompletionListener;
        private IMediaPlayer.OnPreparedListener mOnPreparedListener;
        private IMediaPlayer.OnErrorListener mOnErrorListener;
        private IMediaPlayer.OnInfoListener mOnInfoListener;

        // 所有播放视频需要的东西
        private IRenderView.ISurfaceHolder mSurfaceHolder = null;

        // 渲染相关
        private IRenderView mRenderView;
        private List<Integer> mAllRenders = new ArrayList<>();
        private int mCurrentRenderIndex = 0;
        private int mCurrentRender = RENDER_NONE;
        public static final int RENDER_NONE = 0;
        public static final int RENDER_SURFACE_VIEW = 1;
        public static final int RENDER_TEXTURE_VIEW = 2;

        // 画面大小尺寸相关
        private int mVideoWidth;
        private int mVideoHeight;
        private int mSurfaceWidth;
        private int mSurfaceHeight;

        private int mVideoRotationDegree;

        private int mVideoSarNum;
        private int mVideoSarDen;

        private static final int[] s_allAspectRatio = {
                IRenderView.AR_ASPECT_FIT_PARENT,
                IRenderView.AR_ASPECT_FILL_PARENT,
                IRenderView.AR_ASPECT_WRAP_CONTENT,
                IRenderView.AR_16_9_FIT_PARENT,
                IRenderView.AR_4_3_FIT_PARENT};
        private int mCurrentAspectRatioIndex = 0;
        private int mCurrentAspectRatio = s_allAspectRatio[0];

        private TextView subtitleDisplay;


        public MusicPlayer(@NonNull Context context) {
            super(context);
            initVideoView(context);
        }

        public MusicPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            initVideoView(context);
        }

        public MusicPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initVideoView(context);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public MusicPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            initVideoView(context);
        }


        private void initVideoView(Context context) {
            mAppContext = context.getApplicationContext();
            mSettings = new Settings(mAppContext);

            initBackground();
            initRenders();

            mVideoWidth = 0;
            mVideoHeight = 0;

            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();

            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;

            subtitleDisplay = new TextView(context);
            subtitleDisplay.setTextSize(24);
            subtitleDisplay.setGravity(Gravity.CENTER);
            addView(subtitleDisplay, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));

        }

        /**
         * 初始化视频播放渲染View
         */
        private void initRenders() {
            mAllRenders.clear();

            if (mSettings.getEnableSurfaceView())
                mAllRenders.add(RENDER_SURFACE_VIEW);

            if (mSettings.getEnableTextureView())
                mAllRenders.add(RENDER_TEXTURE_VIEW);

            if (mSettings.getEnableNoView())
                mAllRenders.add(RENDER_NONE);

            if (mAllRenders.isEmpty())
                mAllRenders.add(RENDER_SURFACE_VIEW);

            mCurrentRender = mAllRenders.get(mCurrentRenderIndex);
            setRender(mCurrentRender);

        }


        /**
         * 设置视频播不同渲染类型
         *
         * @param render render
         */
        public void setRender(int render) {

            switch (render) {
                case RENDER_NONE:
                    setRenderView(null);
                    break;
                case RENDER_TEXTURE_VIEW: {
                    TextureRenderView renderView = new TextureRenderView(getContext());
                    if (mMediaPlayer != null) {
                        renderView.getSurfaceHolder().bindToMediaPlayer(mMediaPlayer);
                        renderView.setVideoSize(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
                        renderView.setVideoSampleAspectRatio(mMediaPlayer.getVideoSarNum(), mMediaPlayer.getVideoSarDen());
                        renderView.setAspectRatio(mCurrentAspectRatio);
                    }
                    setRenderView(renderView);
                    break;
                }
                case RENDER_SURFACE_VIEW: {
                    SurfaceRenderView renderView = new SurfaceRenderView(getContext());
                    setRenderView(renderView);
                    break;
                }
                default:
                    Log.e(Constant.TAG, "invalid render");
                    break;
            }

        }

        /**
         * 设置渲染 View
         *
         * @param renderView renderView
         */
        public void setRenderView(IRenderView renderView) {
            if (mRenderView != null) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setDisplay(null);
                }
                View renderUIView = mRenderView.getView();
                mRenderView.removeRenderCallback(mSHCallback);
                mRenderView = null;
                removeView(renderUIView);
            }
            if (renderView == null) {
                return;
            }
            mRenderView = renderView;
            renderView.setAspectRatio(mCurrentAspectRatio);
            if (mVideoWidth > 0 && mVideoHeight > 0)
                renderView.setVideoSize(mVideoWidth, mVideoHeight);
            if (mVideoSarNum > 0 && mVideoSarDen > 0)
                renderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);

            View rendUIView = mRenderView.getView();
            rendUIView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            addView(rendUIView);

            mRenderView.addRenderCallback(mSHCallback);
            mRenderView.setVideoRotation(mVideoRotationDegree);
        }


        /**
         * 视频播放渲染View 创建、销毁回调
         */
        IRenderView.IRenderCallback mSHCallback = new IRenderView.IRenderCallback() {
            @Override
            public void onSurfaceChanged(@NonNull IRenderView.ISurfaceHolder holder, int format, int w, int h) {
                if (holder.getRenderView() != mRenderView) {
                    Log.e(Constant.TAG, "onSurfaceChanged: unmatched render callback\n");
                    return;
                }

                mSurfaceWidth = w;
                mSurfaceHeight = h;
                boolean isValidState = (mTargetState == STATE_PLAYING);
                boolean hasValidSize = !mRenderView.shouldWaitForResize() || (mVideoWidth == w && mVideoHeight == h);
                if (mMediaPlayer != null && isValidState && hasValidSize) {
                    if (mSeekWhenPrepared != 0) {
                        seekTo(mSeekWhenPrepared);
                    }
                    start();
                }
            }

            @Override
            public void onSurfaceCreated(@NonNull IRenderView.ISurfaceHolder holder, int width, int height) {
                if (holder.getRenderView() != mRenderView) {
                    Log.e(Constant.TAG, "onSurfaceCreated: unmatched render callback\n");
                    return;
                }

                mSurfaceHolder = holder;
                if (mMediaPlayer != null)
                    bindSurfaceHolder(mMediaPlayer, holder);
                else
                    openVideo();
            }

            @Override
            public void onSurfaceDestroyed(@NonNull IRenderView.ISurfaceHolder holder) {
                if (holder.getRenderView() != mRenderView) {
                    Log.e(Constant.TAG, "onSurfaceDestroyed: unmatched render callback\n");
                    return;
                }

                // after we return from this we can't use the surface any more
                mSurfaceHolder = null;
                // REMOVED: if (mMediaController != null) mMediaController.hide();
                // REMOVED: release(true);
                releaseWithoutStop();
            }
        };

        public void releaseWithoutStop() {
            if (mMediaPlayer != null)
                mMediaPlayer.setDisplay(null);
        }


        /**
         * RenderView 绑定 MediaPlayer
         *
         * @param mp     mediaPlayer
         * @param holder ISurfaceHolder
         */
        private void bindSurfaceHolder(IMediaPlayer mp, IRenderView.ISurfaceHolder holder) {
            if (mp == null)
                return;
            if (holder == null) {
                mp.setDisplay(null);
                return;
            }
            holder.bindToMediaPlayer(mp);
        }


        private void openVideo() {
            if (mUri == null || mSurfaceHolder == null) {
                Log.e(Constant.TAG, "uri or mSurfaceHolder null");
            }
            // we shouldn't clear the target state, because somebody might have
            // called start() previously
            release(false);

            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            try {
                mMediaPlayer = createPlayer(mSettings.getPlayer());

                mMediaPlayer.setOnPreparedListener(mPreparedListener);
                mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
                mMediaPlayer.setOnCompletionListener(mCompletionListener);
                mMediaPlayer.setOnErrorListener(mErrorListener);
                mMediaPlayer.setOnInfoListener(mInfoListener);
                mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
                mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
                mMediaPlayer.setOnTimedTextListener(mOnTimedTextListener);
                mCurrentBufferPercentage = 0;
                String scheme = mUri.getScheme();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mSettings.getUsingMediaDataSource() && (TextUtils.isEmpty(scheme) || scheme.equalsIgnoreCase("file"))) {
                    IMediaDataSource dataSource = new FileMediaDataSource(new File(mUri.toString()));
                    mMediaPlayer.setDataSource(dataSource);
                } else {
                    mMediaPlayer.setDataSource(mAppContext, mUri, mHeaders);
                }
                bindSurfaceHolder(mMediaPlayer, mSurfaceHolder);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setScreenOnWhilePlaying(true);
                mPrepareStartTime = System.currentTimeMillis();
                mMediaPlayer.prepareAsync();

                // we don't set the target state here either, but preserve the
                // target state that was there before.
                mCurrentState = STATE_PREPARING;
                attachMediaController();
            } catch (IOException ex) {
                Log.e(Constant.TAG, "Unable to open content: " + mUri, ex);
                mCurrentState = STATE_ERROR;
                mTargetState = STATE_ERROR;
                mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            } catch (IllegalArgumentException ex) {
                Log.e(Constant.TAG, "Unable to open content: " + mUri, ex);
                mCurrentState = STATE_ERROR;
                mTargetState = STATE_ERROR;
                mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            }

        }

        public void setMediaController(IMediaController controller) {
            if (mMediaController != null) {
                mMediaController.hide();
            }
            mMediaController = controller;
            attachMediaController();
        }


        private void attachMediaController() {
            if (mMediaPlayer != null && mMediaController != null) {
                mMediaController.setMediaPlayer(this);
                View anchorView = this.getParent() instanceof View ?
                        (View) this.getParent() : this;
                mMediaController.setAnchorView(anchorView);
                mMediaController.setEnabled(isInPlaybackState());
            }
        }


        public IMediaPlayer createPlayer(int playerType) {
            IMediaPlayer mediaPlayer = null;

            switch (playerType) {
                case Settings.PV_PLAYER_IjkExoMediaPlayer: {
                    // IjkExoMediaPlayer 因原ijkplayer编译出的库不支持，补充添加在exoplayer文件夹
//                    mediaPlayer = new IjkExoMediaPlayer(mAppContext);
                    break;
                }
                case Settings.PV_PLAYER_AndroidMediaPlayer: {
                    mediaPlayer = new AndroidMediaPlayer();
                    break;
                }
                case Settings.PV_PLAYER_IjkMediaPlayer:
                default: {
                    IjkMediaPlayer ijkMediaPlayer = null;
                    if (mUri != null) {
                        ijkMediaPlayer = new IjkMediaPlayer();
                        // IjkMediaPlayer 播放相关设置
                        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
                        if (mManifestString != null) {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "is-manifest", 1);
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "manifest_string", mManifestString);
                        }
                        if (mSettings.getUsingMediaCodec()) {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                            if (mSettings.getUsingMediaCodecAutoRotate()) {
                                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                            } else {
                                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);
                            }
                            if (mSettings.getMediaCodecHandleResolutionChange()) {
                                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
                            } else {
                                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);
                            }
                        } else {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
                        }

                        if (mSettings.getUsingOpenSLES()) {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
                        } else {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
                        }

                        String pixelFormat = mSettings.getPixelFormat();
                        if (TextUtils.isEmpty(pixelFormat)) {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
                        } else {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", pixelFormat);
                        }
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);

                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);

                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);

                    }

                    mediaPlayer = ijkMediaPlayer;

                    break;
                }

            }

            if (mSettings.getEnableDetachedSurfaceTextureView()) {
                mediaPlayer = new TextureMediaPlayer(mediaPlayer);
            }

            return mediaPlayer;
        }


        /**
         * 释放MediaPlayer
         *
         * @param clearTargetState clearTargetState
         */
        public void release(boolean clearTargetState) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
                mCurrentState = STATE_IDLE;
                if (clearTargetState) {
                    mTargetState = STATE_IDLE;
                }
                AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
                am.abandonAudioFocus(null);
            }
        }


        /**
         * 播放器 OnPrepared 回调
         */
        private final IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                mCurrentState = STATE_PREPARED;

                if (mOnPreparedListener != null) {
                    mOnPreparedListener.onPrepared(mMediaPlayer);
                }

                if (mMediaController != null) {
                    mMediaController.setEnabled(true);
                }

                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();

                // mSeekWhenPrepared may be changed after seekTo() call
                int seekToPosition = mSeekWhenPrepared;
                if (seekToPosition != 0) {
                    seekTo(seekToPosition);
                }

                if (mVideoWidth != 0 && mVideoHeight != 0) {
                    if (mRenderView != null) {
                        mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                        mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                        if (!mRenderView.shouldWaitForResize() || mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                            // We didn't actually change the size (it was already at the size
                            // we need), so we won't get a "surface changed" callback, so
                            // start the video here instead of in the callback.
                            if (mTargetState == STATE_PLAYING) {
                                start();
                                if (mMediaController != null) {
                                    mMediaController.show();
                                }
                            } else if (!isPlaying() &&
                                    (seekToPosition != 0 || getCurrentPosition() > 0)) {
                                if (mMediaController != null) {
                                    // 暂停时 显示Controller
                                    // Show the media controls when we're paused into a video and make 'em stick.
                                    mMediaController.show(0);
                                }
                            }
                        }
                    }
                } else {
                    // 不确定视频大小、直接播放
                    if (mTargetState == STATE_PLAYING) {
                        start();
                    }
                }
            }
        };


        /**
         * MediaPlayer onVideoSizeChanged 回调
         */
        private final IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();
                mVideoSarNum = mp.getVideoSarNum();
                mVideoSarDen = mp.getVideoSarDen();
                if (mVideoWidth != 0 && mVideoHeight != 0) {
                    if (mRenderView != null) {
                        mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                        mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                    }
                    requestLayout();
                }
            }
        };

        /**
         * onCompletion
         */
        private final IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                mCurrentState = STATE_PLAYBACK_COMPLETED;
                mTargetState = STATE_PLAYBACK_COMPLETED;
                if (mMediaController != null) {
                    mMediaController.hide();
                }
                if (mOnCompletionListener != null) {
                    mOnCompletionListener.onCompletion(mMediaPlayer);
                }
            }
        };

        /**
         * 视频播放出错回调
         */
        private final IMediaPlayer.OnErrorListener mErrorListener = new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                Log.d(Constant.TAG, "Error: " + framework_err + "," + impl_err);
                mCurrentState = STATE_ERROR;
                mTargetState = STATE_ERROR;
                if (mMediaController != null) {
                    mMediaController.hide();
                }
                // 此处可在上层处理错误

                if (mOnErrorListener != null) {
                    if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                        return true;
                    }
                }
                Toast.makeText(mAppContext, "出错啦,换个视频看看呢", Toast.LENGTH_SHORT).show();
                return true;
            }
        };

        private final IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {
                if (mOnInfoListener != null) {
                    mOnInfoListener.onInfo(mp, arg1, arg2);
                }
                // arg1 arg2 值定义在 IMediaPlayer 中
                Log.e(Constant.TAG, "arg1:" + arg1 + "      arg2：" + arg2);
                switch (arg1) {
                    case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                        Log.d(Constant.TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        Log.d(Constant.TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        Log.d(Constant.TAG, "MEDIA_INFO_BUFFERING_START:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        Log.d(Constant.TAG, "MEDIA_INFO_BUFFERING_END:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                        Log.d(Constant.TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                        Log.d(Constant.TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        Log.d(Constant.TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                        Log.d(Constant.TAG, "MEDIA_INFO_METADATA_UPDATE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                        Log.d(Constant.TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                        Log.d(Constant.TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                        mVideoRotationDegree = arg2;
                        Log.d(Constant.TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + arg2);
                        if (mRenderView != null)
                            mRenderView.setVideoRotation(arg2);
                        break;
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                        Log.d(Constant.TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                        break;
                }
                return true;
            }
        };

        /**
         * 缓冲回调
         */
        private final IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
                mCurrentBufferPercentage = percent;
            }
        };

        /**
         * 进度拖动回调
         */
        private final IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = iMediaPlayer -> mSeekEndTime = System.currentTimeMillis();

        //
        private final IMediaPlayer.OnTimedTextListener mOnTimedTextListener = (mp, text) -> {
            if (text != null) {
                subtitleDisplay.setText(text.getText());
            }
        };

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (isInPlaybackState() && mMediaController != null)
                toggleMediaControlsVisibility();
            return false;
        }

        @Override
        public boolean onTrackballEvent(MotionEvent event) {
            if (isInPlaybackState() && mMediaController != null)
                toggleMediaControlsVisibility();
            return false;
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                    keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                    keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                    keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                    keyCode != KeyEvent.KEYCODE_MENU &&
                    keyCode != KeyEvent.KEYCODE_CALL &&
                    keyCode != KeyEvent.KEYCODE_ENDCALL;
            if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
                if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                        keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                    if (mMediaPlayer.isPlaying()) {
                        pause();
                        mMediaController.show();
                    } else {
                        start();
                        mMediaController.hide();
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                    if (!mMediaPlayer.isPlaying()) {
                        start();
                        mMediaController.hide();
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                        || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                    if (mMediaPlayer.isPlaying()) {
                        pause();
                        mMediaController.show();
                    }
                    return true;
                } else {
                    toggleMediaControlsVisibility();
                }
            }
            return super.onKeyDown(keyCode, event);
        }

        private void toggleMediaControlsVisibility() {
            if (mMediaController.isShowing())
                mMediaController.hide();
            else
                mMediaController.show();
        }

        @Override
        public void start() {
            if (isInPlaybackState()) {
                mMediaPlayer.start();
                mCurrentState = STATE_PLAYING;
            }
            mTargetState = STATE_PLAYING;
        }

        @Override
        public void pause() {
            if (isInPlaybackState()) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mCurrentState = STATE_PAUSED;
                }
            }
            mTargetState = STATE_PAUSED;
        }

        @Override
        public int getDuration() {
            if (isInPlaybackState())
                return (int) mMediaPlayer.getDuration();
            return -1;
        }

        @Override
        public int getCurrentPosition() {
            if (isInPlaybackState())
                return (int) mMediaPlayer.getCurrentPosition();
            return 0;
        }

        @Override
        public void seekTo(int msec) {
            if (isInPlaybackState()) {
                mSeekStartTime = System.currentTimeMillis();
                mMediaPlayer.seekTo(msec);
                mSeekWhenPrepared = 0;
            } else {
                mSeekWhenPrepared = msec;
            }
        }

        @Override
        public boolean isPlaying() {
            return isInPlaybackState() && mMediaPlayer.isPlaying();

        }

        @Override
        public int getBufferPercentage() {
            if (mMediaPlayer != null) {
                return mCurrentBufferPercentage;
            }
            return 0;
        }

        @Override
        public boolean canPause() {
            return mCanPause;
        }

        @Override
        public boolean canSeekBackward() {
            return mCanSeekBack;
        }

        @Override
        public boolean canSeekForward() {
            return mCanSeekForward;
        }

        @Override
        public int getAudioSessionId() {
            return 0;
        }

        private boolean isInPlaybackState() {
            return (mMediaPlayer != null &&
                    mCurrentState != STATE_ERROR &&
                    mCurrentState != STATE_IDLE &&
                    mCurrentState != STATE_PREPARING);
        }

        public int toggleAspectRatio() {
            mCurrentAspectRatioIndex++;
            mCurrentAspectRatioIndex %= s_allAspectRatio.length;

            mCurrentAspectRatio = s_allAspectRatio[mCurrentAspectRatioIndex];
            if (mRenderView != null)
                mRenderView.setAspectRatio(mCurrentAspectRatio);
            return mCurrentAspectRatio;
        }

        public int toggleRender() {
            mCurrentRenderIndex++;
            mCurrentRenderIndex %= mAllRenders.size();

            mCurrentRender = mAllRenders.get(mCurrentRenderIndex);
            setRender(mCurrentRender);
            return mCurrentRender;
        }

        public int togglePlayer() {
            if (mMediaPlayer != null)
                mMediaPlayer.release();

            if (mRenderView != null)
                mRenderView.getView().invalidate();
            openVideo();
            return mSettings.getPlayer();
        }

        /**
         * 设置视频地址
         */
        public void setVideoPath(String path) {
            if (path.contains("adaptationSet")) {
                mManifestString = path;
                setVideoUri(Uri.EMPTY);
            } else {
                setVideoUri(Uri.parse(path));
            }
        }

        /**
         * 设置视频Uri
         *
         * @param uri uri
         */
        public void setVideoUri(Uri uri) {
            setVideoUri(uri, null);
        }

        /**
         * Sets video URI using specific headers.
         *
         * @param uri     the URI of the video.
         * @param headers the headers for the URI request.
         */
        public void setVideoUri(Uri uri, Map<String, String> headers) {
            mUri = uri;
            mHeaders = headers;
            mSeekWhenPrepared = 0;
            openVideo();
            requestLayout();
            invalidate();
        }

        public void stopPlayback() {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                mCurrentState = STATE_IDLE;
                mTargetState = STATE_IDLE;
                AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
                am.abandonAudioFocus(null);
            }
        }


        /**
         * 初始化后台播放相关
         */
        private void initBackground() {
            mEnableBackgroundPlay = mSettings.getEnableBackgroundPlay();
            if (mEnableBackgroundPlay) {
                MediaPlayerService.intentToStart(getContext());
                mMediaPlayer = MediaPlayerService.getsMediaPlayer();
            }
        }

        public boolean isBackgroundPlayEnabled() {
            return mEnableBackgroundPlay;
        }

        public void selectTrack(int stream) {
            MediaPlayerCompat.selectTrack(mMediaPlayer, stream);
        }

        public void deselectTrack(int stream) {
            MediaPlayerCompat.deselectTrack(mMediaPlayer, stream);
        }

        public int getSelectedTrack(int trackType) {
            return MediaPlayerCompat.getSelectedTrack(mMediaPlayer, trackType);
        }

        public void enterBackground() {
            MediaPlayerService.setMediaPlayer(mMediaPlayer);
        }

        public void stopBackgroundPlay() {
            MediaPlayerService.setMediaPlayer(null);
        }

        // 提供外部接收播放状态回调处理
        public void setOnPreparedListener(IMediaPlayer.OnPreparedListener listener) {
            mOnPreparedListener = listener;
        }

        public void setOnCompletionListener(IMediaPlayer.OnCompletionListener listener) {
            mOnCompletionListener = listener;
        }

        public void setOnErrorListener(IMediaPlayer.OnErrorListener listener) {
            mOnErrorListener = listener;
        }

        public void setOnInfoListener(IMediaPlayer.OnInfoListener listener) {
            mOnInfoListener = listener;
        }

}
