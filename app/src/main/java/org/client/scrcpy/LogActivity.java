package org.client.scrcpy;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogActivity extends Activity {
    private TextView logTextView;
    private ScrollView scrollView;
    private Process logcatProcess;
    private boolean isReading = false;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        logTextView = findViewById(R.id.tv_log);
        scrollView = findViewById(R.id.scrollView_log);
        handler = new Handler(Looper.getMainLooper());

        Button clearButton = findViewById(R.id.btn_clear_log);
        clearButton.setOnClickListener(v -> logTextView.setText(""));

        Button closeButton = findViewById(R.id.btn_close_log);
        closeButton.setOnClickListener(v -> finish());

        startLogReading();
    }

    private void startLogReading() {
        isReading = true;
        new Thread(() -> {
            try {
                logcatProcess = Runtime.getRuntime().exec("logcat -v time");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(logcatProcess.getInputStream()));

                String line;
                while (isReading && (line = reader.readLine()) != null) {
                    final String logLine = line;
                    handler.post(() -> appendLog(logLine));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void appendLog(String line) {
        logTextView.append(line + "\n");
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isReading = false;
        if (logcatProcess != null) {
            logcatProcess.destroy();
        }
    }
}