package org.client.scrcpy;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.client.scrcpy.utils.AdbHelper;
import org.client.scrcpy.utils.ThreadUtils;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class SendCommands {

    public final static int WAIT_TIME = 5000;

    public enum CmdStatus {
        SUCCESS,
        RUNNING,
        ERROR
    }

    public SendCommands() {

    }

    public CmdStatus executeAdbCommands(Context context, final String ip, int port, int forwardport, String localip, int bitrate, int size, ScrcpyOptions options) {
        AtomicReference<CmdStatus> status = new AtomicReference<>(CmdStatus.RUNNING);
        
        java.util.List<String> commandList = new java.util.ArrayList<>();
        commandList.add("-s");
        commandList.add(ip + ":" + port);
        commandList.add("shell");
        commandList.add("CLASSPATH=/data/local/tmp/scrcpy-server.jar");
        commandList.add("app_process");
        commandList.add("/");
        commandList.add("org.server.scrcpy.Server");
        commandList.add("/" + localip);
        commandList.add(Long.toString(size));
        commandList.add(Long.toString(bitrate));

        if (options != null) {
            if (options.videoCodec != null && !options.videoCodec.isEmpty()) {
                commandList.add("--video-codec=" + options.videoCodec);
            }
            if (options.videoFps > 0) {
                commandList.add("--video-fps=" + options.videoFps);
            }
            if (options.audioCodec != null && !options.audioCodec.isEmpty()) {
                commandList.add("--audio-codec=" + options.audioCodec);
            }
            if (options.audioBitrate > 0) {
                commandList.add("--audio-bit-rate=" + options.audioBitrate);
            }
            if (options.displayId >= 0) {
                commandList.add("--display=" + options.displayId);
            }
            if (options.rotation >= 0) {
                commandList.add("--rotation=" + options.rotation);
            }
            if (options.showTouches) {
                commandList.add("--show-touches");
            }
            if (options.stayAwake) {
                commandList.add("--stay-awake");
            }
            if (options.turnScreenOff) {
                commandList.add("--turn-screen-off");
            }
            if (options.turnScreenOffOnClose) {
                commandList.add("--turn-screen-off-on-close");
            }
            if (options.noVideo) {
                commandList.add("--no-video");
            }
            if (options.noAudio) {
                commandList.add("--no-audio");
            }
            if (options.recordPath != null && !options.recordPath.isEmpty()) {
                commandList.add("--record=" + options.recordPath);
            }
            if (options.customArgs != null && !options.customArgs.isEmpty()) {
                Log.i("Scrcpy", "Processing customArgs: " + options.customArgs);
                String[] customArgsArray = options.customArgs.split("\\s+");
                for (String arg : customArgsArray) {
                    if (!arg.isEmpty()) {
                        commandList.add(arg);
                        Log.i("Scrcpy", "Added custom arg: " + arg);
                    }
                }
            }
        }

        String[] commands = commandList.toArray(new String[0]);
        
        ThreadUtils.execute(() -> {
            try {
                boolean serverIsRunning = AdbHelper.checkAdbServer();
                Log.i("Scrcpy", "serverIsRunning: " + serverIsRunning);
                if (!serverIsRunning || !AdbHelper.isRunning()){
                    AdbHelper.restartAdb();
                    AdbHelper.waitForRunning(5);
                }
                CmdStatus curStatus = startPortForward(context, ip, port, forwardport);
                status.set(curStatus);
                if (curStatus == CmdStatus.SUCCESS) {
                    newAdbServerStart(commands);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        int count = 0;
        while (status.get() == CmdStatus.RUNNING && count < (WAIT_TIME / 100)) {
            Log.e("ADB", "Connecting...");
            try {
                Thread.sleep(100);
                count++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (count >= 50) {
            status.set(CmdStatus.ERROR);
            return status.get();
        }
        if (status.get() == CmdStatus.SUCCESS) {
            count = 0;
            //  检测程序是否已经启动，如果启动了，该文件会被删除
            while (status.get() == CmdStatus.SUCCESS && count < 10) {
                String adbTextCmd = AdbHelper.adbCmd(App.mContext,
                        "-s", ip + ":" + port, "shell", "ls", "-alh", "/data/local/tmp/scrcpy-server.jar");
                if (TextUtils.isEmpty(adbTextCmd)) {
                    break;
                } else {
                    try {
                        Thread.sleep(100);
                        count++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return status.get();
    }

    private CmdStatus startPortForward(Context context, String ip, int port, int serverport) {
        Log.i("Scrcpy", "try connect to ip: " + ip);
        AdbHelper.adbCmd(App.mContext, "connect", ip + ":" + port);
        // 复制server端到可执行目录
        String pushRet = AdbHelper.adbCmd(App.mContext, "-s", ip + ":" + port, "push", new File(
                context.getExternalFilesDir("scrcpy"), "scrcpy-server.jar"
        ).getAbsolutePath(), "/data/local/tmp/scrcpy-server.jar");

        Log.i("Scrcpy", "pushRet: " + pushRet);

        String adbTextCmd = AdbHelper.adbCmd(App.mContext, "-s", ip + ":" + port, "shell", "ls", "-alh", "/data/local/tmp/scrcpy-server.jar");
        if (TextUtils.isEmpty(adbTextCmd)) {
            return CmdStatus.ERROR;
        }
        // 开启本地端口 forward 转发
        Log.i("Scrcpy", "开启本地端口转发");
        AdbHelper.adbCmd(App.mContext, "-s", ip + ":" + port, "forward", "tcp:" + serverport, "tcp:" + 7007);
        return CmdStatus.SUCCESS;
    }

    private void newAdbServerStart(String[] command) {
        // 执行启动命令
        AdbHelper.adbCmd(App.mContext, command);
    }

}