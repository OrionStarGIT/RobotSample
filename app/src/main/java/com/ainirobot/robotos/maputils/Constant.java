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

public class Constant {
    public static final String PREFIX = "MapTool_";

    public static class MAPCOLOR {
        public final static int PASS = 0xFF4E75C0;
        public final static int BLOCK = 0xFF1D3C7F;
        public final static int UNDETECT = 0xFF182A52;
        public final static int OBSTACLE = 0xFF1D3C7E;
    }

    public enum NavigatorPoint {
        POINT1, POINT2
    }

    public static class CoreDef {
        public static final String POSE_LISTEN = Definition.STATUS_POSE;
    }
}