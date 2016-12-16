package com.example.videoplayer.part;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.videoplayer.R;

import java.io.IOException;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

/**
 * Created by XpBoom on 2016/12/16.
 */

public class SimpleVideoPlayer extends FrameLayout {
    /**
     * 视频播放URL
     */
    private String videoPath;
    private MediaPlayer mediaPlayer = new MediaPlayer(getContext());
    /**
     * 是否准备好
     */
    private boolean isPrepared;
    /**
     * 是否正在播放
     */
    private boolean isPlaying;
    //视图相关
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    /**
     * 预览图
     */
    private ImageView isPreview;
    /**
     * 播放，暂停
     */
    private ImageButton btnToggle;
    /**
     * 进度条
     */
    private ProgressBar progressBar;
    private static final int PROGRESS_MAX = 1000;

    public SimpleVideoPlayer(Context context) {
        super(context);
    }

    public SimpleVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUI();
    }

    private void initUI() {
        //Vitamio初始化
        Vitamio.isInitialized(getContext());
        //填充布局
        //填充布局
        LayoutInflater.from(getContext()).inflate(R.layout.view_simple_video_player, this, true);
        //初始化SurfaceView
        initSurfaceView();
        //初始化视频播放控制视图
        initControllerViews();
    }

    /**
     * 初始化视频播放控制视图
     */
    private void initControllerViews() {
        isPreview = (ImageView) findViewById(R.id.ivPreview);
        btnToggle = (ImageButton) findViewById(R.id.btnToggle);
        btnToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断是否正在播放
                if (mediaPlayer.isPlaying()) {
                    //暂停播放
                    pauseMediaPlayer();
                } else if (isPrepared) {
                    startMediaPlayer();
                } else {
                    Toast.makeText(getContext(), "Can't play now!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //设置进度条
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(PROGRESS_MAX);
        //全屏播放按钮
        btnToggle = (ImageButton) findViewById(R.id.btnToggle);
        btnToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    private void startMediaPlayer() {
        isPreview.setVisibility(INVISIBLE);
        btnToggle.setImageResource(R.dr);
    }

    //暂停MedioPlayer
    private void pauseMediaPlayer() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        isPlaying = false;
        btnToggle.setImageResource(R.drawable.ic_play_arrow);
        //更新进度条
        handler.removeMessages(0);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isPlaying) {
                //每0.2秒更新一次播放进度
                int progress = (int) (mediaPlayer.getCurrentPosition() * PROGRESS_MAX / mediaPlayer.getDuration());
                progressBar.setProgress(progress);
                handler.sendEmptyMessageDelayed(0,200);
            }
        }
    };

    /**
     * 初始化SurfaceView
     */
    private void initSurfaceView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
    }

    //设置数据源
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    //提供OnResume方法(在activity的onResume调用)
    public void onResume() {
        //：初始化MediaPlayer，
        initMediaPlayer();
        //准备MediaPlayer
        prepareMediaPlater();
    }

    /**
     * 准备MediaPlayer
     */
    private void prepareMediaPlater() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(videoPath);
            //设置循环播放
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            isPreview.setVisibility(VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化MediaPlayer
     */
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer(getContext());
        mediaPlayer.setDisplay(surfaceHolder);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                startMediaPlayer();
            }
        });
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_FILE_OPEN_OK) {
                    mediaPlayer.audioInitedOk(mediaPlayer.audioTrackInit());
                    return true;
                }
                return false;
            }
        });
        //视频大小改变的监听
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                int layoutwidth = surfaceView.getWidth();
                int layoutHeight = layoutwidth * height / width;
                //更新surfaceview的size
                ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
                params.width = layoutwidth;
                params.height = layoutHeight;
                surfaceView.setLayoutParams(params);
            }
        });
    }

    //提供OnPause方法(在activity的onPause调用)，
    public void onPause() {
        //暂停mediaplayer
        pauseMediaPlayer();
        //：释放mediaplayer
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        mediaPlayer.release();

    }
}
