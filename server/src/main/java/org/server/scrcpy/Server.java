package org.server.scrcpy;

import org.server.scrcpy.util.Workarounds;
import org.server.scrcpy.wrappers.ServiceManager;

import java.io.IOException;

public final class Server {

    private static String ip = null;

    private Server() {
        // not instantiable
    }

    private static void scrcpy(Options options) throws IOException {
        Workarounds.apply();  // init content

        final Device device = new Device(options);
        
        // 处理 --turn-screen-off 参数
        if (options.isTurnScreenOff()) {
            Ln.i("Turning screen off...");
            ServiceManager.getPowerManager().goToSleep();
        }
        
        try (DroidConnection connection = DroidConnection.open(ip)) {
            ScreenEncoder screenEncoder = new ScreenEncoder(options.getBitRate());

            // asynchronous
            startEventController(device, connection, screenEncoder);

            try {
                // synchronous
                screenEncoder.streamScreen(device, connection.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                // this is expected on close
                Ln.d("Screen streaming stopped");

            }
        } finally {
            // 处理 --turn-screen-off-on-close 或 --stay-awake 的相反操作
            if (options.isTurnScreenOffOnClose()) {
                Ln.i("Turning screen off on close...");
                ServiceManager.getPowerManager().goToSleep();
            }
            if (options.isTurnScreenOff() && !options.isStayAwake()) {
                // 如果开启屏幕关闭但不保持唤醒，结束时可以选择恢复
                Ln.i("Streaming ended");
            }
        }
    }

    private static void startEventController(final Device device, final DroidConnection connection, ScreenEncoder screenEncoder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new EventController(device, connection, screenEncoder).control();
                } catch (IOException e) {
                    // this is expected on close
                    Ln.d("Event controller stopped");
                }
            }
        }).start();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static Options createOptions(String... args) {
        Options options = new Options();

        if (args.length < 1) {
            return options;
        }
        ip = String.valueOf(args[0]);


        if (args.length < 2) {
            return options;
        }
        int maxSize = Integer.parseInt(args[1]) & ~7; // multiple of 8
        options.setMaxSize(maxSize);

        if (args.length < 3) {
            return options;
        }
        int bitRate = Integer.parseInt(args[2]);
        options.setBitRate(bitRate);

        if (args.length < 4) {
            return options;
        }
        
        // 检查第4个参数是否是布尔值（tunnelForward）还是自定义参数
        String arg3 = args[3];
        if (arg3.equals("true") || arg3.equals("false")) {
            // 传统格式：第4个参数是 tunnelForward
            boolean tunnelForward = Boolean.parseBoolean(arg3);
            options.setTunnelForward(tunnelForward);
            
            // 从第5个参数开始解析自定义参数
            Ln.i("Starting to parse custom arguments from index 4, total args: " + args.length);
            for (int i = 4; i < args.length; i++) {
                parseArgument(options, args[i], i);
            }
        } else {
            // 新格式：没有 tunnelForward，第4个参数开始就是自定义参数
            Ln.i("No tunnelForward flag, starting to parse custom arguments from index 3, total args: " + args.length);
            for (int i = 3; i < args.length; i++) {
                parseArgument(options, args[i], i);
            }
        }
        
        Ln.i("Options created: noDisplay=" + options.isNoDisplay() + ", turnScreenOff=" + options.isTurnScreenOff());
        return options;
    }
    
    private static void parseArgument(Options options, String arg, int index) {
        Ln.i("Parsing argument[" + index + "]: " + arg);
        
        if (arg.equals("--no-display")) {
            options.setNoDisplay(true);
        } else if (arg.equals("--turn-screen-off")) {
            options.setTurnScreenOff(true);
        } else if (arg.equals("--turn-screen-off-on-close")) {
            options.setTurnScreenOffOnClose(true);
        } else if (arg.equals("--show-touches")) {
            options.setShowTouches(true);
        } else if (arg.equals("--stay-awake")) {
            options.setStayAwake(true);
        } else if (arg.equals("--no-video")) {
            options.setNoVideo(true);
        } else if (arg.equals("--no-audio")) {
            options.setNoAudio(true);
        } else if (arg.startsWith("--video-codec=")) {
            options.setVideoCodec(arg.substring("--video-codec=".length()));
        } else if (arg.startsWith("--video-fps=")) {
            try {
                options.setVideoFps(Integer.parseInt(arg.substring("--video-fps=".length())));
            } catch (NumberFormatException e) {
                Ln.e("Invalid video-fps: " + arg);
            }
        } else if (arg.startsWith("--audio-codec=")) {
            options.setAudioCodec(arg.substring("--audio-codec=".length()));
        } else if (arg.startsWith("--audio-bit-rate=")) {
            try {
                options.setAudioBitrate(Integer.parseInt(arg.substring("--audio-bit-rate=".length())));
            } catch (NumberFormatException e) {
                Ln.e("Invalid audio-bit-rate: " + arg);
            }
        } else if (arg.startsWith("--display=")) {
            try {
                options.setDisplayId(Integer.parseInt(arg.substring("--display=".length())));
            } catch (NumberFormatException e) {
                Ln.e("Invalid display: " + arg);
            }
        } else if (arg.startsWith("--rotation=")) {
            try {
                options.setRotation(Integer.parseInt(arg.substring("--rotation=".length())));
            } catch (NumberFormatException e) {
                Ln.e("Invalid rotation: " + arg);
            }
        } else if (arg.startsWith("--record=")) {
            options.setRecordPath(arg.substring("--record=".length()));
        }
    }

    public static void main(String... args) throws Exception {
        Ln.i("=== Server started ===");
        Ln.i("Total arguments: " + args.length);
        for (int i = 0; i < args.length; i++) {
            Ln.i("  args[" + i + "] = '" + args[i] + "'");
        }
        
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Ln.e("Exception on thread " + t, e);
            }
        });

        try {
            Process cmd = Runtime.getRuntime().exec("rm /data/local/tmp/scrcpy-server.jar");
            cmd.waitFor();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        Options options = createOptions(args);
        scrcpy(options);
    }
}