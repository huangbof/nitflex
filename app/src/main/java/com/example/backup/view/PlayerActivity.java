package com.example.backup.view;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.backup.R;
import com.example.backup.controller.MovieController;
import com.example.backup.service.SessionManager;
import com.example.backup.util.VideoRemuxer;

import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";

    private VideoView videoView;
    private View layoutLoading;
    private View layoutControlsOverlay;
    private View topBar;
    private View layoutBottomBar;
    private ImageButton btnBack;
    private TextView tvTitle;
    private TextView tvPlayerSub;

    // Custom Controls
    private View btnRewind10;
    private ImageButton btnPlayPause;
    private View btnForward10;
    private SeekBar playerSeekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalDuration;
    private ImageButton btnToggleOrientation;

    private int movieId;
    private String videoUrl;
    private String movieTitle;
    private SessionManager session;

    private boolean isControlsVisible = true;
    private boolean isUserTracking = false;
    private int totalDuration = 0;
    private int currentPosition = 0;
    private boolean isEndgameMovie = false;
    private boolean isMovieCompleted = false;

    // Quản lý âm lượng và Audio Focus chống mất tiếng
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private MediaPlayer currentMediaPlayer;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Handler hideControlsHandler = new Handler(Looper.getMainLooper());

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            progressHandler.postDelayed(this, 400);
        }
    };

    private final Runnable autoHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideControls();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Giữ màn hình luôn sáng trong suốt thời gian xem phim
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Cho phép video mở rộng tối đa sát viền màn hình (kể cả vùng camera nốt ruồi / tai thỏ)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_player);

        // Khóa luồng điều khiển âm lượng vật lý là STREAM_MUSIC và đảm bảo có tiếng
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        checkAndEnsureAudio();
        requestAudioFocus();

        session = new SessionManager(this);
        movieId    = getIntent().getIntExtra("movie_id", -1);
        videoUrl   = getIntent().getStringExtra("video_url");
        movieTitle = getIntent().getStringExtra("movie_title");

        isEndgameMovie = (movieTitle != null && (movieTitle.toLowerCase().contains("endgame") 
                || movieTitle.toLowerCase().contains("hồi kết")))
                || (movieId == 299534);

        initViews();
        enableImmersiveCinemaMode();
        handleWindowInsets();
        setupControlListeners();

        if (movieTitle != null && !movieTitle.isEmpty()) {
            tvTitle.setText(movieTitle);
        }

        // Khôi phục mốc thời gian đang xem dở (start_position truyền từ Intent hoặc Local Cache / MySQL)
        int passedPosition = getIntent().getIntExtra("start_position", 0);
        if (passedPosition > 0) {
            currentPosition = passedPosition;
        } else if (movieId > 0) {
            int localSaved = session.getLocalWatchProgress(movieId) * 1000;
            if (localSaved > 0) {
                currentPosition = localSaved;
            }
        }

        if (movieId > 0 && currentPosition <= 0 && session.isLoggedIn()) {
            MovieController.getWatchProgress(session.getUserId(), movieId, progressSec -> {
                if (progressSec > 0) {
                    currentPosition = progressSec * 1000;
                    if (videoView != null && videoView.isPlaying()) {
                        videoView.seekTo(currentPosition);
                        Toast.makeText(PlayerActivity.this, "Đang tiếp tục phát từ " + formatTime(currentPosition), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        setupVideoPlayback();
    }

    private void initViews() {
        videoView             = findViewById(R.id.videoView);
        layoutLoading         = findViewById(R.id.layoutPlayerLoading);
        layoutControlsOverlay = findViewById(R.id.layoutControlsOverlay);
        topBar                = findViewById(R.id.playerTopBar);
        layoutBottomBar       = findViewById(R.id.layoutBottomBar);
        btnBack               = findViewById(R.id.btnPlayerBack);
        tvTitle               = findViewById(R.id.tvPlayerTitle);
        tvPlayerSub           = findViewById(R.id.tvPlayerSub);

        btnRewind10           = findViewById(R.id.btnRewind10);
        btnPlayPause          = findViewById(R.id.btnPlayPause);
        btnForward10          = findViewById(R.id.btnForward10);
        playerSeekBar         = findViewById(R.id.playerSeekBar);
        tvCurrentTime         = findViewById(R.id.tvCurrentTime);
        tvTotalDuration       = findViewById(R.id.tvTotalDuration);
        btnToggleOrientation  = findViewById(R.id.btnToggleOrientation);
    }

    // Chế độ xem phim chuẩn điện ảnh: ẩn status bar và gesture navigation bar
    private void enableImmersiveCinemaMode() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            controller.hide(WindowInsetsCompat.Type.systemBars());
        }
    }

    // Tối ưu căn chỉnh viền (WindowInsets + DisplayCutout) cho cả màn hình dọc và màn hình xoay ngang
    private void handleWindowInsets() {
        View root = findViewById(R.id.playerRoot);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );

            boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

            int dp16 = dpToPx(16);
            int dp24 = dpToPx(24);
            int dp36 = dpToPx(36);

            if (isLandscape) {
                // Khi để màn hình ngang: đẩy an toàn sang hai bên để camera nốt ruồi không đè vào nút Back và tiêu đề
                int safeLeft  = Math.max(insets.left, dp36);
                int safeRight = Math.max(insets.right, dp36);
                int safeTop   = Math.max(insets.top, dp16);
                int safeBot   = Math.max(insets.bottom, dp16);

                topBar.setPadding(safeLeft, safeTop, safeRight, dp16);
                layoutBottomBar.setPadding(safeLeft, dp16, safeRight, safeBot);
            } else {
                // Khi để màn hình dọc
                int safeTop = insets.top + dp16;
                int safeBot = insets.bottom + dp16;

                topBar.setPadding(dp24, safeTop, dp24, dp16);
                layoutBottomBar.setPadding(dp24, dp16, dp24, safeBot);
            }

            return windowInsets;
        });
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()
        );
    }

    private void setupControlListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Bấm vào vùng màn hình để ẩn/hiện bảng điều khiển
        View.OnClickListener toggleListener = v -> {
            if (isControlsVisible) {
                hideControls();
            } else {
                showControlsWithTimeout();
            }
        };

        layoutControlsOverlay.setOnClickListener(toggleListener);
        findViewById(R.id.playerRoot).setOnClickListener(toggleListener);

        // Nút Play / Pause
        btnPlayPause.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                hideControlsHandler.removeCallbacks(autoHideRunnable);
            } else {
                videoView.start();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                showControlsWithTimeout();
            }
        });

        // Tua lùi 10 giây
        btnRewind10.setOnClickListener(v -> {
            int current = videoView.getCurrentPosition();
            int target = Math.max(0, current - 10000);
            videoView.seekTo(target);
            playerSeekBar.setProgress(target);
            tvCurrentTime.setText(formatTime(target));
            showControlsWithTimeout();
        });

        // Tua tiến 10 giây
        btnForward10.setOnClickListener(v -> {
            int current = videoView.getCurrentPosition();
            int max = totalDuration > 0 ? totalDuration : videoView.getDuration();
            int target = Math.min(max > 0 ? max : current + 10000, current + 10000);
            videoView.seekTo(target);
            playerSeekBar.setProgress(target);
            tvCurrentTime.setText(formatTime(target));
            showControlsWithTimeout();
        });

        // Nút xoay nhanh hướng màn hình ngang / dọc
        btnToggleOrientation.setOnClickListener(v -> {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            showControlsWithTimeout();
        });

        // Thanh kéo thời lượng (SeekBar)
        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserTracking = true;
                hideControlsHandler.removeCallbacks(autoHideRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserTracking = false;
                videoView.seekTo(seekBar.getProgress());
                showControlsWithTimeout();
            }
        });
    }

    private void showControlsWithTimeout() {
        layoutControlsOverlay.setVisibility(View.VISIBLE);
        isControlsVisible = true;
        hideControlsHandler.removeCallbacks(autoHideRunnable);
        hideControlsHandler.postDelayed(autoHideRunnable, 4000);
    }

    private void hideControls() {
        if (!isUserTracking && videoView.isPlaying()) {
            layoutControlsOverlay.setVisibility(View.GONE);
            isControlsVisible = false;
            enableImmersiveCinemaMode(); // Duy trì ẩn thanh hệ thống khi xem phim
        }
    }

    private void updateProgress() {
        if (videoView == null) return;

        int pos = videoView.getCurrentPosition();
        int dur = videoView.getDuration();

        if (dur > 0 && dur != totalDuration) {
            totalDuration = dur;
            playerSeekBar.setMax(totalDuration);
            tvTotalDuration.setText(formatTime(totalDuration));
        } else if (totalDuration > 0 && playerSeekBar.getMax() != totalDuration) {
            playerSeekBar.setMax(totalDuration);
            tvTotalDuration.setText(formatTime(totalDuration));
        }

        if (!isUserTracking) {
            playerSeekBar.setProgress(pos);
            tvCurrentTime.setText(formatTime(pos));
        }

        if (videoView.isPlaying()) {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private String formatTime(int millis) {
        if (millis < 0) millis = 0;
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        int hours   = (millis / (1000 * 60 * 60));
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    private void setupVideoPlayback() {
        layoutLoading.setVisibility(View.VISIBLE);

        Uri resolvedUri = null;
        int rawResId = 0;
        String subtitleText = "Đang phát phim • Nit Flex Player";

        // 1. Phân giải videoUrl từ cơ sở dữ liệu nếu có
        if (videoUrl != null && !videoUrl.trim().isEmpty()) {
            String cleanUrl = videoUrl.trim();
            File localFile = new File(cleanUrl);
            if (localFile.exists() && localFile.isFile()) {
                resolvedUri = Uri.fromFile(localFile);
                subtitleText = "Video nội bộ máy: " + localFile.getName();
            } else if (cleanUrl.startsWith("file://")) {
                resolvedUri = Uri.parse(cleanUrl);
                subtitleText = "Video từ bộ nhớ thiết bị • Nit Flex Player";
            } else if (cleanUrl.startsWith("android.resource://") || cleanUrl.startsWith("content://")) {
                resolvedUri = Uri.parse(cleanUrl);
                subtitleText = "Video từ bộ nhớ thiết bị • Nit Flex Player";
            } else if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                // Tự động phân giải tên file trong res/raw (hỗ trợ cả raw/endgame, endgame.mp4 hoặc chỉ gõ endgame)
                String resName = cleanUrl.replace("raw/", "").replace(".mp4", "").trim().toLowerCase();
                int resId = getResources().getIdentifier(resName, "raw", getPackageName());
                if (resId != 0) {
                    rawResId = resId;
                    resolvedUri = Uri.parse("android.resource://" + getPackageName() + "/" + resId);
                    subtitleText = "Bản phim MP4 nội bộ: " + resName;
                }
            } else if (cleanUrl.startsWith("http")) {
                resolvedUri = Uri.parse(cleanUrl);
                subtitleText = "Đang phát luồng MP4 HD • Nit Flex Player";
            }
        }

        // 2. Nếu là Avengers: Endgame
        if (resolvedUri == null && isEndgameMovie) {
            rawResId = R.raw.endgame;
            resolvedUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.endgame);
            subtitleText = "Đang phát bản phim Avengers: Endgame • Nit Flex 1080p";
        }

        // 3. Fallback mặc định về file MP4 nội bộ
        if (resolvedUri == null) {
            rawResId = R.raw.sample_movie;
            resolvedUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample_movie);
            subtitleText = "Đang phát trực tiếp file MP4 nội bộ • Nit Flex HD";
        }

        if (tvPlayerSub != null) {
            tvPlayerSub.setText(subtitleText);
        }

        final Uri finalRawUri = resolvedUri;
        final int finalRawResId = rawResId;

        // Tự động kiểm tra xem video có bị phân mảnh fMP4 (Endgame, Infinity War, Avatar...) hay không
        boolean isFmp4 = VideoRemuxer.isFragmentedMp4(this, finalRawUri, finalRawResId);

        if (isFmp4) {
            VideoRemuxer.prepareVideoUri(this, finalRawUri, finalRawResId, (playableUri, durationMs) -> {
                if (isDestroyed() || isFinishing()) return;

                if (durationMs > 0) {
                    totalDuration = durationMs;
                    playerSeekBar.setMax(totalDuration);
                    tvTotalDuration.setText(formatTime(totalDuration));
                }

                startPlayerWithUri(playableUri);
            });
        } else {
            // Video MP4 tiêu chuẩn phát trực tiếp
            int detectedDur = VideoRemuxer.detectMediaDurationMs(this, finalRawUri, finalRawResId);
            if (detectedDur > 0) {
                totalDuration = detectedDur;
                playerSeekBar.setMax(totalDuration);
                tvTotalDuration.setText(formatTime(totalDuration));
            }
            startPlayerWithUri(finalRawUri);
        }
    }

    private void startPlayerWithUri(Uri videoUri) {
        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                currentMediaPlayer = mp;
                layoutLoading.setVisibility(View.GONE);
                mp.setLooping(false);

                // Khóa luồng âm thanh Media và đặt âm lượng tối đa 1.0f
                try {
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mp.setVolume(1.0f, 1.0f);
                } catch (Exception e) {
                    Log.w(TAG, "Không thể gán âm lượng MediaPlayer: " + e.getMessage());
                }

                int dur = mp.getDuration();
                if (dur > 0) {
                    totalDuration = dur;
                } else if (totalDuration <= 0) {
                    totalDuration = VideoRemuxer.detectMediaDurationMs(PlayerActivity.this, videoUri, 0);
                }

                if (totalDuration > 0) {
                    playerSeekBar.setMax(totalDuration);
                    tvTotalDuration.setText(formatTime(totalDuration));
                }

                if (currentPosition > 0) {
                    if (totalDuration > 5000 && currentPosition >= totalDuration - 5000) {
                        currentPosition = 0; // Đã xem hết phim thì phát lại từ đầu
                    }
                    videoView.seekTo(currentPosition);
                }
                videoView.start();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);

                // Tự động tăng lượt xem phim trong CSDL MySQL
                if (movieId > 0) {
                    MovieController.incrementMovieViewCount(movieId);
                }

                progressHandler.post(updateProgressRunnable);
                showControlsWithTimeout();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.w(TAG, "MediaPlayer warning/error: what=" + what + ", extra=" + extra);
                layoutLoading.setVisibility(View.GONE);
                return true;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Kiểm tra xem có thực sự phát đến cuối phim không (tránh trường hợp MediaPlayer báo hoàn tất sớm)
                int current = videoView.getCurrentPosition();
                if (totalDuration > 5000 && current < totalDuration - 3000) {
                    // Chưa đến cuối phim mà bị ngắt: tiếp tục phát
                    videoView.seekTo(current);
                    videoView.start();
                    return;
                }
                isMovieCompleted = true;
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                showControlsWithTimeout();
                // Đã xem hết phim: đặt tiến độ bằng tổng thời lượng (durSec) để tự động ẩn khỏi Tiếp Tục Xem
                int durSec = totalDuration > 0 ? (totalDuration / 1000) : 7200;
                session.saveLocalWatchProgress(movieId, durSec, durSec);
                if (session.isLoggedIn() && movieId > 0) {
                    MovieController.saveWatchProgress(session.getUserId(), movieId, durSec, durSec);
                }
                Toast.makeText(PlayerActivity.this, "Bạn đã xem xong phim!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        enableImmersiveCinemaMode();
        // Áp dụng lại safe insets khi xoay màn hình
        ViewCompat.requestApplyInsets(findViewById(R.id.playerRoot));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableImmersiveCinemaMode();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressHandler.removeCallbacks(updateProgressRunnable);
        hideControlsHandler.removeCallbacks(autoHideRunnable);
        if (videoView != null && videoView.isPlaying()) {
            currentPosition = videoView.getCurrentPosition();
            videoView.pause();
        }
        saveCurrentProgressToHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableImmersiveCinemaMode();
        requestAudioFocus();
        if (currentMediaPlayer != null) {
            try {
                currentMediaPlayer.setVolume(1.0f, 1.0f);
            } catch (Exception ignored) {}
        }
        if (videoView != null && currentPosition > 0) {
            videoView.seekTo(currentPosition);
            videoView.start();
            progressHandler.post(updateProgressRunnable);
            showControlsWithTimeout();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressHandler.removeCallbacks(updateProgressRunnable);
        hideControlsHandler.removeCallbacks(autoHideRunnable);
        saveCurrentProgressToHistory();
        abandonAudioFocus();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    // Tự động kiểm tra và mở âm lượng Media nếu máy đang bị tắt tiếng (Mute / 0)
    private void checkAndEnsureAudio() {
        try {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                int currentVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                if (currentVol == 0 && maxVol > 0) {
                    int defaultVol = Math.max(1, (int) (maxVol * 0.6));
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVol, AudioManager.FLAG_SHOW_UI);
                    Toast.makeText(this, "Đã tự động bật âm lượng phát phim", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Không thể kiểm tra âm lượng hệ thống: " + e.getMessage());
        }
    }

    // Yêu cầu Audio Focus để hệ thống Android ưu tiên phát âm thanh phim, không bị các luồng khác can thiệp
    private void requestAudioFocus() {
        try {
            if (audioManager == null) {
                audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            }
            if (audioManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                            .build();
                    audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(focusChange -> {
                                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                                    if (videoView != null && videoView.isPlaying()) {
                                        videoView.pause();
                                        btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                                    }
                                }
                            })
                            .build();
                    audioManager.requestAudioFocus(audioFocusRequest);
                } else {
                    audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Lỗi yêu cầu Audio Focus: " + e.getMessage());
        }
    }

    // Giải phóng Audio Focus khi thoát xem phim
    private void abandonAudioFocus() {
        try {
            if (audioManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
                } else {
                    audioManager.abandonAudioFocus(null);
                }
            }
        } catch (Exception ignored) {}
    }

    private void saveCurrentProgressToHistory() {
        if (movieId <= 0 || isMovieCompleted) return;
        int currentMs = (videoView != null && videoView.getCurrentPosition() > 0)
                ? videoView.getCurrentPosition() : currentPosition;
        int posSec = currentMs > 0 ? (currentMs / 1000) : 0;
        int durSec = totalDuration > 0 ? (totalDuration / 1000) : 7200;

        session.saveLocalWatchProgress(movieId, posSec, durSec);
        if (session.isLoggedIn()) {
            MovieController.saveWatchProgress(session.getUserId(), movieId, posSec, durSec);
        }
    }
}
