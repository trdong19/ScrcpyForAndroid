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
    public boolean noControl;
    public boolean noVideo;
    public boolean noAudio;
    public String recordPath;
    public String keyboardInject;

    public ScrcpyOptions() {
        this.videoCodec = "";
        this.videoFps = 0;
        this.audioCodec = "";
        this.audioBitrate = 0;
        this.displayId = -1;
        this.rotation = -1;
        this.showTouches = false;
        this.stayAwake = false;
        this.noControl = false;
        this.noVideo = false;
        this.noAudio = false;
        this.recordPath = "";
        this.keyboardInject = "";
    }
}