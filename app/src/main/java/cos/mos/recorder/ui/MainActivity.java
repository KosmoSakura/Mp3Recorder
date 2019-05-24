package cos.mos.recorder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;

import cos.mos.recorder.R;
import cos.mos.recorder.record.RecordingService;

public class MainActivity extends AppCompatActivity {
    private TextView mRecordingPrompt;
    private ImageButton tRecord;
    private Chronometer mChronometer;
    private boolean mStartRecording = true;
    private boolean mPauseRecording = true;
    private long timeWhenPaused = 0;
    private int mRecordPromptCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tRecord = findViewById(R.id.main_switch);
        mChronometer = findViewById(R.id.chronometer);
        mRecordingPrompt = findViewById(R.id.recording_status_text);
    }

    public void mainClick(View view) {
        switch (view.getId()) {
            case R.id.main_list:
                startActivity(new Intent(this, PlayActivity.class));
                break;
            case R.id.main_switch:
                tRecord.setSelected(!tRecord.isSelected());
                if (tRecord.isSelected()) {
                    onRecord(mStartRecording);
                    mStartRecording = !mStartRecording;
                } else {
                    onPauseRecord(mPauseRecording);
                    mPauseRecording = !mPauseRecording;
                }
                break;
        }
    }

    private void onRecord(boolean start) {
        Intent intent = new Intent(this, RecordingService.class);
        if (start) {
            // 开始
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                folder.mkdir();
            }

            //开始时计
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if (mRecordPromptCount == 0) {
                        mRecordingPrompt.setText("R .");
                    } else if (mRecordPromptCount == 1) {
                        mRecordingPrompt.setText("R . .");
                    } else if (mRecordPromptCount == 2) {
                        mRecordingPrompt.setText("R . . .");
                        mRecordPromptCount = -1;
                    }
                    mRecordPromptCount++;
                }
            });

            //start RecordingService
            startService(intent);
            //keep screen on while recording
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mRecordingPrompt.setText("R .");
            mRecordPromptCount++;
        } else {
            //停止
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;
            mRecordingPrompt.setText("这是个开关");
            stopService(intent);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * @param pause 暂停
     */
    private void onPauseRecord(boolean pause) {
        if (pause) {
            //暂停
            mRecordingPrompt.setText("Resume");
            timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
        } else {
            //恢复
            mRecordingPrompt.setText("Pause");
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            mChronometer.start();
        }
    }
}