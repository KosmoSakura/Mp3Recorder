package cos.mos.recorder.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private String[] pps = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(() -> checkPermissions(pps), 250);
    }

    private void checkPermissions(String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            toNext();
            return;
        }
        boolean st = true;
        for (String str : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, str)) {
                st = false;
                break;
            }
        }
        if (st) {
            toNext();
        } else {
            ActivityCompat.requestPermissions(this, permissions, 10086);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        checkPermissions(pps);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void toNext() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}