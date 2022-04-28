package com.ainirobot.robotos.maputils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.util.Objects;

public class MapInfo implements Comparable<MapInfo> {
    private String mapName;
    private Bitmap bitmap;
    private String coverUrl;
    /**
     * 0 已完成 1 未完成 2 使用中
     */
    private int state;
    private boolean estimateState = false;
    private String timeStamp;
    private Integer version;
    private boolean manualUpload = false;
    private boolean placeUpload = false;
    private boolean hasVision = false;
    private boolean hasTarget = false;

    public MapInfo(String mapName, String timeStamp, int version, String coverUrl) {
        this.mapName = mapName;
        this.timeStamp = timeStamp;
        this.version = version;
        this.coverUrl = coverUrl;
    }

    public MapInfo(String mapName, Bitmap bitmap, int state, boolean estimateState,
                   String timeStamp, int version, boolean manualUpload, boolean placeUpload,
                   boolean hasVision, boolean hasTarget) {
        this.mapName = mapName;
        this.bitmap = bitmap;
        this.state = state;
        this.estimateState = estimateState;
        this.timeStamp = timeStamp;
        this.version = version;
        this.manualUpload = manualUpload;
        this.placeUpload = placeUpload;
        this.hasVision = hasVision;
        this.hasTarget = hasTarget;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public boolean isManualUpload() {
        return manualUpload;
    }

    public boolean isPlaceUpload() {
        return placeUpload;
    }

    public void setManualUpload(boolean manualUpload) {
        this.manualUpload = manualUpload;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isEstimateState() {
        return estimateState;
    }

    public void setEstimateState(boolean estimateState) {
        this.estimateState = estimateState;
    }

    public String getTimeStamp() {
        return timeStamp;
    }


    @Override
    public int compareTo(@NonNull MapInfo info) {
        return info.getVersion().compareTo(this.getVersion());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MapInfo)) {
            return false;
        }

        return Objects.equals(((MapInfo) obj).mapName, this.mapName);
    }

    @Override
    public int hashCode() {
        int hashCode = mapName.hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        return "mapName = " + this.mapName + ", timeStamp = "
                + this.getTimeStamp() + ", version = " + this.getVersion();
    }

    public String getCoverUrl() {
        return coverUrl;
    }
}
