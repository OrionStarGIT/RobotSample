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


import android.graphics.Bitmap;

public class RoverMap {
    public Bitmap bitmap;
    public byte[] extra;
    public double x;
    public double y;
    public double res;
    public int height;
    public int width;
    public String pgmMd5;

    @Override
    public String toString() {
        return "x = " + x
                + ", y = " + y
                + ", res = " + res
                + "height = " + height
                + "width = " + width;
    }
}
