/*
 * Copyright (C) 2017 OrionStar Technology Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ainirobot.robotos.maputils;

import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.actionbean.PlaceBean;
import com.ainirobot.robotos.view.MapView;


import java.util.List;

public class GlobalData {
    private static final String TAG = GlobalData.class.getSimpleName();

    private String mCurrentMapName;
    private String mCreatingMapLanguage;
    private int mCreatingMapFinishState;
    private MapView mMapView;
    private RoverMap mMap;
    private List<MapInfo> mMapInfoList;
    private List<PlaceBean> mPlaceBeanList;
    private List<PoseBean> mPoseBeanList;
    private OnPoseBeanListChangeListener mOnPoseBeanListChangeListener;
    private Pose2d mNewestLocation;
    private String mLastEditName;
    private int mLastEditTime;
    private int mCurrentWork;
    private boolean mIsHardJump = false;
    private int mCreateRetry;
    /**
     * 显示TargetInfoFragment时，需要回退到对应界面，需知道Maplist界面归属
     */
    private int mMapListSrc;
    private Definition.MapType mMapType = Definition.MapType.TYPE_UNKNOWN;
    private boolean mIsFirstConfigEnd = false;
    private boolean mSupportVoiceSetPlace = false;

    private static GlobalData mInstance;

    public static GlobalData getInstance() {
        if (null == mInstance) {
            mInstance = new GlobalData();
        }
        return mInstance;
    }

    private GlobalData() {
    }

    public MapView getMapView() {
        return mMapView;
    }

    public RoverMap getMap() {
        return mMap;
    }

    public String getCurrentMapName() {
        return mCurrentMapName;
    }

    public void setEditMapData(MapView mapView, RoverMap map) {
        mMapView = mapView;
        mMap = map;
    }

    public void setMapView(MapView mapView) {
        mMapView = mapView;
    }

    public void setRoverMap(RoverMap map) {
        mMap = map;
    }

    public void setCurrentMapName(String mapName) {
        mCurrentMapName = mapName;
    }

    public List<MapInfo> getMapInfoList() {
        return mMapInfoList;
    }

    public void setMapInfoList(List<MapInfo> mapInfoList) {
        mMapInfoList = mapInfoList;
    }

    public List<PlaceBean> getPlaceBeanList() {
        return mPlaceBeanList;
    }

    public void setPlaceBeanList(List<PlaceBean> placeBeanList) {
        mPlaceBeanList = placeBeanList;
    }

    public void setNewestLocation(Pose2d newestLocation) {
        mNewestLocation = newestLocation;
    }

    public void setPoseBeanList(List<PoseBean> poseBeanList, boolean notify) {
        this.mPoseBeanList = poseBeanList;
        if (mOnPoseBeanListChangeListener != null && notify) {
            mOnPoseBeanListChangeListener.onChanged();
        }
    }


    public interface OnPoseBeanListChangeListener {
        void onChanged();
    }
}
