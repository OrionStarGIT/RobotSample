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

public class PoseBean {
    private String name;
    private Pose2d pose;

    public PoseBean() {
    }

    public PoseBean(Pose pose) {
        this.name = pose.getName();
        this.pose = new Pose2d(Float.valueOf(pose.getX()).doubleValue(),
                Float.valueOf(pose.getY()).doubleValue(),
                Float.valueOf(pose.getTheta()).doubleValue());
    }

    public PoseBean(String name, Pose2d pose) {
        this.name = name;
        this.pose = pose;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Pose2d getPose() {
        return pose;
    }

    public void setPose(Pose2d pose) {
        this.pose = pose;
    }

    @Override
    public String toString() {
        return "name = " + name + ", Pose2d = " + pose.toString();
    }
}
