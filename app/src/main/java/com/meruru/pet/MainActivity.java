package com.meruru.pet;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_REQUEST_CODE = 1001;
    private TextView statusText;
    private LinearLayout mainMenu;
    private WebView fullWebView;
    private boolean isShowingFull = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        mainMenu = findViewById(R.id.main_menu);
        fullWebView = findViewById(R.id.full_webview);

        Button startBtn = findViewById(R.id.btn_start);
        Button stopBtn = findViewById(R.id.btn_stop);
        Button fullBtn = findViewById(R.id.btn_full);

        startBtn.setOnClickListener(v -> checkAndStart());
        stopBtn.setOnClickListener(v -> stopPet());
        fullBtn.setOnClickListener(v -> showFullVersion());

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

    private void showFullVersion() {
        mainMenu.setVisibility(View.GONE);
        fullWebView.setVisibility(View.VISIBLE);
        isShowingFull = true;

        WebSettings ws = fullWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        fullWebView.setWebViewClient(new WebViewClient());
        fullWebView.loadUrl("file:///android_asset/full.html");
    }

    @Override
    public void onBackPressed() {
        if (isShowingFull) {
            fullWebView.setVisibility(View.GONE);
            mainMenu.setVisibility(View.VISIBLE);
            isShowingFull = false;
        } else {
            super.onBackPressed();
        }
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
            statusText.setText("点击下方按钮召唤梅露露");
        } else {
            statusText.setText("需要先授予悬浮窗权限");
        }
    }
}
