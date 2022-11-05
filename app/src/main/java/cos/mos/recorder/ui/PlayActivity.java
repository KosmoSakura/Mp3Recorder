package cos.mos.recorder.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cos.mos.recorder.R;

public class PlayActivity extends AppCompatActivity {
    private Handler handler = new Handler();
    private MediaPlayer mMediaPlayer;
    private boolean isPlaying = false;//当前是否正在播放音频
    private TextView tName;
    private SeekBar seekBar;
    private View playRoot;
    private ImageView iPlay;
    private Mp3Adapteer adapteer;
    private ArrayList<String> list;
    private String dirAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        playRoot = findViewById(R.id.play_root);
        tName = findViewById(R.id.play_name);
        iPlay = findViewById(R.id.play_play);
        seekBar = findViewById(R.id.play_seekbar);
        ListView lv = findViewById(R.id.play_list);

        list = new ArrayList<>();
        adapteer = new Mp3Adapteer(list, this);
        lv.setAdapter(adapteer);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            if (playRoot.isShown() || position < 0 || position >= list.size()) {
                return;
            }
            show(list.get(position));
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMediaPlayer != null && fromUser) {
                    mMediaPlayer.seekTo(progress);
                    handler.removeCallbacks(mRunnable);
                    updateSeekBar();
                } else if (mMediaPlayer == null && fromUser) {
                    prepareMediaPlayerFromPoint(progress);
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    handler.removeCallbacks(mRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    handler.removeCallbacks(mRunnable);
                    mMediaPlayer.seekTo(seekBar.getProgress());
                    updateSeekBar();
                }
            }
        });
        iPlay.setOnClickListener(v -> {
            onPlay(isPlaying);
            isPlaying = !isPlaying;
        });
        playRoot.setOnClickListener(v -> stopPlaying());
        playRoot.setVisibility(View.GONE);
    }

    private void show(String dir) {
        dirAudio = dir;
        File file = new File(dir);
        if (dirAudio != null && file.exists()) {
            tName.setText(file.getName());
            playRoot.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mp3Recorder/");
        if (file.exists()) {
            for (File dir : file.listFiles()) {
                if (dir != null && dir.getName().contains("mp3")) {
                    list.add(dir.getAbsolutePath());
                }
            }
            adapteer.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlaying();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlaying();
    }

    //启动/停止
    private void onPlay(boolean isPlaying) {
        if (!isPlaying) {
            //目前MediaPlayer不播放音频
            if (mMediaPlayer == null) {
                startPlaying(); //从头开始
            } else {
                resumePlaying(); //恢复当前暂停的媒体播放器
            }
        } else {
            // 暂停
            pausePlaying();
        }
    }


    private void startPlaying() {
        if (dirAudio == null) {
            return;
        }
        iPlay.setImageResource(R.drawable.ic_pause);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(dirAudio);
            mMediaPlayer.prepare();
            seekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.setOnPreparedListener(mp -> mMediaPlayer.start());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnCompletionListener(mp -> stopPlaying());
        updateSeekBar();
        //保持屏幕打开
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void prepareMediaPlayerFromPoint(int progress) {
        if (dirAudio == null) {
            return;
        }
        //设置mediaPlayer从音频文件的progress开始
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(dirAudio);
            mMediaPlayer.prepare();
            seekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.seekTo(progress);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        //保持屏幕打开
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void pausePlaying() {
        if (mMediaPlayer != null) {
            iPlay.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(mRunnable);
            mMediaPlayer.pause();
        }
    }

    private void resumePlaying() {
        if (mMediaPlayer != null) {
            iPlay.setImageResource(R.drawable.ic_pause);
            handler.removeCallbacks(mRunnable);
            mMediaPlayer.start();
            updateSeekBar();
        }
    }

    private void stopPlaying() {
        playRoot.setVisibility(View.GONE);
        dirAudio = null;
        iPlay.setImageResource(R.drawable.ic_play);
        handler.removeCallbacks(mRunnable);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        isPlaying = !isPlaying;
        seekBar.setProgress(seekBar.getMax());
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null) {
                int mCurrentPosition = mMediaPlayer.getCurrentPosition();
                seekBar.setProgress(mCurrentPosition);
                updateSeekBar();
            }
        }
    };

    private void updateSeekBar() {
        handler.postDelayed(mRunnable, 1000);
    }
}