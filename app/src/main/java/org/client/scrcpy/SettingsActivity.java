package org.client.scrcpy;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.client.scrcpy.utils.PreUtils;

public class SettingsActivity extends Activity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.context = this;

        Button saveButton = findViewById(R.id.btn_save_settings);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        Button resetButton = findViewById(R.id.btn_reset_settings);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSettings();
                loadSettings();
                Toast.makeText(context, "已重置", Toast.LENGTH_SHORT).show();
            }
        });

        TextView tvCommandHelp = findViewById(R.id.tv_command_help);
        tvCommandHelp.setText(getCommandHelpText());

        loadSettings();
    }

    private void saveSettings() {
        EditText customArgsEdit = findViewById(R.id.edit_custom_args);
        PreUtils.put(context, Constant.PREF_CUSTOM_ARGS, customArgsEdit.getText().toString());
    }

    private void loadSettings() {
        EditText customArgsEdit = findViewById(R.id.edit_custom_args);
        customArgsEdit.setText(PreUtils.get(context, Constant.PREF_CUSTOM_ARGS, ""));
    }

    private void resetSettings() {
        PreUtils.clearAll(context);
    }

    private String getCommandHelpText() {
        return "================ 通用选项 ================\n" +
                "--help              显示帮助信息\n" +
                "--version           显示版本信息\n\n" +


                "================ 捕获选项 ================\n" +
                "--crop=W:H:X:Y     裁剪屏幕（例如 1224:1440:0:780）\n" +
                "--max-size=W       限制视频尺寸（例如 1024）\n" +
                "--video-bit-rate=B 视频比特率（例如 8000000 或 8M）\n" +
                "--video-codec=c    视频编码器（h264/h265/av1）\n" +
                "--video-encoder=E  选择视频编码器\n" +
                "--no-video         禁用视频传输\n" +
                "--no-audio         禁用音频传输\n" +
                "--audio-bit-rate=B 音频比特率（例如 128000 或 128K）\n" +
                "--audio-codec=c    音频编码器（opus/aac/flac/raw）\n" +
                "--audio-encoder=E  选择音频编码器\n" +
                "--no-playback      不播放，只录制\n" +
                "--record=file      录制到文件（支持 mp4/mkv）\n" +
                "--record-format=f  指定录制格式（mkv/mp4）\n" +
                "--no-display       不显示屏幕\n" +
                "--display=id       选择显示设备（例如 1）\n" +
                "--no-clipboard-auto-sync 禁用自动同步剪贴板\n" +
                "--no-clipboard-to-device  禁用将剪贴板发送到设备\n" +
                "--no-clipboard-from-device 禁用从设备获取剪贴板\n" +
                "--no-cleanup       不清理scrcpy的临时文件\n" +
                "--tunnel-host=ip   隧道主机（用于 adb tunnel）\n" +
                "--tunnel-port=p    隧道端口（用于 adb tunnel）\n\n" +


                "================ 窗口选项 ================\n" +
                "--window-title=t   设置窗口标题\n" +
                "--window-x=x       窗口X位置\n" +
                "--window-y=y       窗口Y位置\n" +
                "--window-width=w   窗口宽度\n" +
                "--window-height=h  窗口高度\n" +
                "--window-borderless 无边框窗口\n" +
                "--window-fullscreen 全屏模式\n" +
                "--always-on-top    窗口始终置顶\n" +
                "--turn-screen-off  启动时关闭设备屏幕\n" +
                "--stay-awake       保持设备屏幕常亮\n" +
                "--rotation=n       设置初始旋转（0/1/2/3）\n" +
                "--lock-video-orientation=n 锁定视频方向（0/1/2/3/unlocked）\n" +
                "--show-touches     显示触摸点\n" +
                "--disable-screensaver 禁用屏保\n" +
                "--no-key-repeat    按键按下时不重复\n" +
                "--keyboard-inject=mode  键盘注入模式（none/raw/events）\n" +
                "--mouse-inject=mode     鼠标注入模式（none/raw/events）\n" +
                "--no-keyboard      禁用键盘控制\n" +
                "--no-mouse         禁用鼠标控制\n" +
                "--forward-all-clicks  转发所有点击事件\n" +
                "--forward-all-mouse  转发所有鼠标事件\n" +
                "--forward-all-keyboard  转发所有键盘事件\n" +
                "--shortcut-mod=mod  快捷键修饰键（lctrl/lalt/lsuper/rctrl/ralt/rsuper）\n" +
                "--shortcuts-only   仅处理快捷键\n" +
                "--power-off-on-close  关闭时关闭设备电源\n" +
                "--tcpip            通过TCP/IP连接\n" +
                "--tcpip-serial=ip[:port]  指定通过TCP/IP连接的设备\n" +
                "--select-tcpip     选择通过TCP/IP连接的设备\n" +
                "--port=n           设置端口（默认: 27183）\n" +
                "--select-usb       选择通过USB连接的设备\n" +
                "--otg              OTG模式（仅在电脑上显示键盘鼠标）\n" +
                "--no-downsize-on-error  视频连接失败时不尝试降低尺寸\n" +
                "--no-power-on      启动时不打开设备屏幕\n" +
                "--camera-id=id     选择相机ID\n" +
                "--camera-fps=f     相机帧率\n" +
                "--camera-size=WxH  相机尺寸\n" +
                "--camera-facing=direction  相机方向（front/back）\n" +
                "--camera-ar=ratio  相机宽高比（16:9/4:3/传感器值）\n" +
                "--list-cameras     列出相机并退出\n" +
                "--list-camera-sizes  列出相机支持的尺寸并退出\n" +
                "--list-encoders    列出可用编码器并退出\n" +
                "--list-displays    列出可用显示设备并退出\n" +
                "--v4l2-sink=/dev/videoN  输出到V4L2回环设备\n" +
                "--v4l2-buffer=b   V4L2缓冲区大小（毫秒）\n" +
                "--audio-buffer=b   音频缓冲区大小（毫秒）\n" +
                "--time-limit=t     超时时间（秒）\n" +
                "--no-mipmaps       禁用mipmap\n" +
                "--prefer-text      优先使用文本（性能较低）\n" +
                "--raw-key-events   发送原始键事件（仅键盘）\n";
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
    }
}