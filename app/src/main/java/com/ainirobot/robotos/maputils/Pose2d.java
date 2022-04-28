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

import com.ainirobot.coreservice.client.actionbean.Pose;
import com.google.gson.annotations.SerializedName;

public class Pose2d {
    @SerializedName("px")
    public double x;
    @SerializedName("py")
    public double y;
    @SerializedName("theta")
    public double t;
    @SerializedName("status")
    public int status;

    public Pose2d(double x, double y, double t) {
        this.x = x;
        this.y = y;
        this.t = t;
    }

    public Pose2d(double x, double y, double t, int status) {
        this.x = x;
        this.y = y;
        this.t = t;
        this.status = status;
    }

    public Pose2d(Pose pose) {
        this.x = Float.valueOf(pose.getX()).doubleValue();
        this.y = Float.valueOf(pose.getY()).doubleValue();
        this.t = Float.valueOf(pose.getTheta()).doubleValue();
    }

    @Override
    public String toString() {
        return "Pose2d{" +
                "x=" + x +
                ", y=" + y +
                ", t=" + t +
                ", status = " + status +
                '}';
    }
}
