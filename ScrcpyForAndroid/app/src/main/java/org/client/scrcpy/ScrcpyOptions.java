package org.client.scrcpy;

public class ScrcpyOptions {
    public String videoCodec;
    public int videoFps;
    public String audioCodec;
    public int audioBitrate;
    public int displayId;
    public int rotation;
    public boolean showTouches;
    public boolean stayAwake;
    public boolean noVideo;
    public boolean noAudio;
    public String recordPath;
    public boolean turnScreenOff;
    public boolean turnScreenOffOnClose;
    public String customArgs;

    public ScrcpyOptions() {
        this.videoCodec = "";
        this.videoFps = 0;
        this.audioCodec = "";
        this.audioBitrate = 0;
        this.displayId = -1;
        this.rotation = -1;
        this.showTouches = false;
        this.stayAwake = false;
        this.noVideo = false;
        this.noAudio = false;
        this.recordPath = "";
        this.turnScreenOff = false;
        this.turnScreenOffOnClose = false;
        this.customArgs = "";
    }
}
