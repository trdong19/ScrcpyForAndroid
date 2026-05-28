package org.server.scrcpy;

public class Options {
    private int maxSize;
    private int bitRate;
    private boolean tunnelForward;
    private boolean noDisplay = false;
    private boolean turnScreenOff = false;
    private boolean turnScreenOffOnClose = false;
    private boolean showTouches = false;
    private boolean stayAwake = false;
    private boolean noVideo = false;
    private boolean noAudio = false;
    private String videoCodec;
    private int videoFps;
    private String audioCodec;
    private int audioBitrate;
    private int displayId = -1;
    private int rotation = -1;
    private String recordPath;

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public boolean isTunnelForward() {
        return tunnelForward;
    }

    public void setTunnelForward(boolean tunnelForward) {
        this.tunnelForward = tunnelForward;
    }

    public boolean isNoDisplay() {
        return noDisplay;
    }

    public void setNoDisplay(boolean noDisplay) {
        this.noDisplay = noDisplay;
    }

    public boolean isTurnScreenOff() {
        return turnScreenOff;
    }

    public void setTurnScreenOff(boolean turnScreenOff) {
        this.turnScreenOff = turnScreenOff;
    }

    public boolean isTurnScreenOffOnClose() {
        return turnScreenOffOnClose;
    }

    public void setTurnScreenOffOnClose(boolean turnScreenOffOnClose) {
        this.turnScreenOffOnClose = turnScreenOffOnClose;
    }

    public boolean isShowTouches() {
        return showTouches;
    }

    public void setShowTouches(boolean showTouches) {
        this.showTouches = showTouches;
    }

    public boolean isStayAwake() {
        return stayAwake;
    }

    public void setStayAwake(boolean stayAwake) {
        this.stayAwake = stayAwake;
    }

    public boolean isNoVideo() {
        return noVideo;
    }

    public void setNoVideo(boolean noVideo) {
        this.noVideo = noVideo;
    }

    public boolean isNoAudio() {
        return noAudio;
    }

    public void setNoAudio(boolean noAudio) {
        this.noAudio = noAudio;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public int getVideoFps() {
        return videoFps;
    }

    public void setVideoFps(int videoFps) {
        this.videoFps = videoFps;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public int getAudioBitrate() {
        return audioBitrate;
    }

    public void setAudioBitrate(int audioBitrate) {
        this.audioBitrate = audioBitrate;
    }

    public int getDisplayId() {
        return displayId;
    }

    public void setDisplayId(int displayId) {
        this.displayId = displayId;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public String getRecordPath() {
        return recordPath;
    }

    public void setRecordPath(String recordPath) {
        this.recordPath = recordPath;
    }
}