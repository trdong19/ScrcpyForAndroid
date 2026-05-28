package org.client.scrcpy;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogActivity extends Activity {
    private TextView logTextView;
    private ScrollView scrollView;
    private Process logcatProcess;
    private boolean isReading = false;
    private Handler handler;
    private StringBuilder logBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        logTextView = findViewById(R.id.tv_log);
        scrollView = findViewById(R.id.scrollView_log);
        handler = new Handler(Looper.getMainLooper());

        Button clearButton = findViewById(R.id.btn_clear_log);
        clearButton.setOnClickListener(v -> {
            logTextView.setText("");
            logBuilder = new StringBuilder();
            Toast.makeText(this, "日志已清空", Toast.LENGTH_SHORT).show();
        });

        Button downloadButton = findViewById(R.id.btn_download_log);
        downloadButton.setOnClickListener(v -> downloadLog());

        Button closeButton = findViewById(R.id.btn_close_log);
        closeButton.setOnClickListener(v -> finish());

        startLogReading();
    }

    private void downloadLog() {
        try {
            // 创建文件名
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "scrcpy_log_" + sdf.format(new Date()) + ".txt";
            
            // 保存到 Downloads 目录
            File downloadDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS);
            File logFile = new File(downloadDir, fileName);
            
            // 写入日志
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
            writer.write(logBuilder.toString());
            writer.close();
            
            Toast.makeText(this, "日志已保存到: " + logFile.getAbsolutePath(), 
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存日志失败: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
        }
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
        logBuilder.append(line).append("\n");
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