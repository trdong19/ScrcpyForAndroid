package org.client.scrcpy;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        
        TextView helpText = findViewById(R.id.help_text);
        helpText.setText(getHelpContent());
    }

    private String getHelpContent() {
        return "=== Scrcpy 命令参数参考 ===\n\n" +
               "【视频设置】\n" +
               "--video-codec=<codec>\n" +
               "  设置视频编码器，可选值: h264, h265, av1\n\n" +
               "--video-fps=<fps>\n" +
               "  设置视频帧率，例如: 30, 60\n\n" +
               "--max-size=<size>\n" +
               "  限制视频尺寸，例如: 1024\n\n" +
               "--video-bit-rate=<bitrate>\n" +
               "  设置视频比特率（单位：bps），例如: 4000000\n\n" +
               "--no-video\n" +
               "  禁用视频传输\n\n" +
               "\n【音频设置】\n" +
               "--audio-codec=<codec>\n" +
               "  设置音频编码器，可选值: opus, aac\n\n" +
               "--audio-bit-rate=<bitrate>\n" +
               "  设置音频比特率（单位：bps），例如: 128000\n\n" +
               "--no-audio\n" +
               "  禁用音频传输\n\n" +
               "\n【设备设置】\n" +
               "--display=<id>\n" +
               "  指定显示设备ID，0为主屏幕\n\n" +
               "--rotation=<degrees>\n" +
               "  设置屏幕旋转角度，可选值: 0, 90, 180, 270\n\n" +
               "--lock-video-orientation=<degrees>\n" +
               "  锁定视频方向，可选值: 0, 90, 180, 270\n\n" +
               "--show-touches\n" +
               "  在远程设备上显示触摸轨迹\n\n" +
               "--stay-awake\n" +
               "  保持远程设备屏幕常亮\n\n" +
               "--turn-screen-off\n" +
               "  控制时关闭远程设备屏幕\n\n" +
               "--turn-screen-off-on-close\n" +
               "  控制结束时关闭远程设备屏幕\n\n" +
               "\n【控制设置】\n" +
               "--no-control\n" +
               "  禁用控制功能（仅查看模式）\n\n" +
               "--keyboard-inject=<mode>\n" +
               "  键盘注入模式，可选值: none, raw, events\n\n" +
               "\n【录制设置】\n" +
               "--record=<file>\n" +
               "  录制视频到文件，支持 mkv 和 mp4 格式\n\n" +
               "--no-playback\n" +
               "  只录制，不播放\n\n" +
               "--record-format=<format>\n" +
               "  指定录制格式，可选值: mkv, mp4\n\n" +
               "\n【连接设置】\n" +
               "--port=<port>\n" +
               "  设置监听端口\n\n" +
               "--tcpip\n" +
               "  使用TCP/IP连接\n\n" +
               "--serial=<serial>\n" +
               "  指定设备序列号\n\n" +
               "\n【高级设置】\n" +
               "--buffer-size=<size>\n" +
               "  设置视频缓冲区大小（单位：字节）\n\n" +
               "--max-latency=<ms>\n" +
               "  设置最大延迟（单位：毫秒）\n\n" +
               "--sync\n" +
               "  同步音频和视频\n\n" +
               "--clipboard-autosync\n" +
               "  自动同步剪贴板\n\n" +
               "--power-off-on-close\n" +
               "  关闭连接时关闭远程设备电源\n\n" +
               "\n=== 使用示例 ===\n" +
               "限制分辨率为1024并锁定方向:\n" +
               "  --max-size=1024 --lock-video-orientation=0\n\n" +
               "禁用音频并保持常亮:\n" +
               "  --no-audio --stay-awake\n\n" +
               "录制视频到指定文件:\n" +
               "  --record=/sdcard/scrcpy/record.mp4";
    }
}
