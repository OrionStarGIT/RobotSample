package com.ainirobot.robotos.maputils;


import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class GsonUtil {
    private static final Gson gson = new Gson();


    public static String toJson(@NonNull Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(@NonNull String json, @NonNull Class<T> aClass) {
        return gson.fromJson(json, aClass);
    }

    public static <T> T fromJson(@NonNull JsonElement element, @NonNull Class<T> aClass) {
        return gson.fromJson(element, aClass);
    }
}
