package cos.mos.recorder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cos.mos.recorder.R;
import cos.mos.recorder.decode.MP3Recorder;

public class MainActivity extends AppCompatActivity {
    private ImageButton tRecord;
    private Chronometer chronometer;
    private MP3Recorder mp3Recorder = new MP3Recorder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tRecord = findViewById(R.id.main_switch);
        chronometer = findViewById(R.id.chronometer);
    }

    public void mainClick(View view) {
        switch (view.getId()) {
            case R.id.main_list:
                startActivity(new Intent(this, PlayActivity.class));
                break;
            case R.id.main_switch:
                tRecord.setSelected(!tRecord.isSelected());
                if (tRecord.isSelected()) {
                    start();
                } else {
                    stop();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }


    private void start() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mp3Recorder/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            mp3Recorder.start(new File(dir, dateFormat.format(new Date()) + ".mp3"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void stop() {
        mp3Recorder.stop();
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toast.makeText(this, "播放列表点下面", Toast.LENGTH_SHORT).show();
    }
}