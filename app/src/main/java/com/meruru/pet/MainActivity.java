package com.meruru.pet;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_REQUEST_CODE = 1001;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        Button startBtn = findViewById(R.id.btn_start);
        Button stopBtn = findViewById(R.id.btn_stop);

        startBtn.setOnClickListener(v -> checkAndStart());
        stopBtn.setOnClickListener(v -> stopPet());

        updateStatus();
    }

    private void checkAndStart() {
        if (!Settings.canDrawOverlays(this)) {
            statusText.setText("需要悬浮窗权限，正在跳转设置...");
            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
            );
            startActivityForResult(intent, OVERLAY_REQUEST_CODE);
        } else {
            startPet();
        }
    }

    private void startPet() {
        Intent intent = new Intent(this, FloatingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        statusText.setText("梅露露已召唤到桌面！");
        Toast.makeText(this, "梅露露出现了！", Toast.LENGTH_SHORT).show();
    }

    private void stopPet() {
        stopService(new Intent(this, FloatingService.class));
        statusText.setText("梅露露回去休息了...");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                startPet();
            } else {
                statusText.setText("没有悬浮窗权限，无法显示");
            }
        }
    }

    private void updateStatus() {
        if (Settings.canDrawOverlays(this)) {
            statusText.setText("点击「召唤梅露露」");
        } else {
            statusText.setText("需要先授予悬浮窗权限");
        }
    }
}

