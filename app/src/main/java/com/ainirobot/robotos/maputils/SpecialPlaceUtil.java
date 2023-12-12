package com.ainirobot.robotos.maputils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ainirobot.base.ApplicationWrapper;
import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.ProductInfo;
import com.ainirobot.coreservice.client.RobotSettings;
import com.ainirobot.coreservice.client.actionbean.PlaceBean;
import com.ainirobot.robotos.R;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SpecialPlaceUtil {
    private static final String TAG = Constant.PREFIX + "SpecialPlaceUtil";

    private static Context sContext;

    private SpecialPlaceUtil() {
        this.sContext = ApplicationWrapper.getApplicationContext();
        initSpecialPlaceLangName();
    }

    public static SpecialPlaceUtil getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        static SpecialPlaceUtil INSTANCE = new SpecialPlaceUtil();
    }

    public static boolean isNavigatorPoint(Constant.NavigatorPoint navigatorPoint,
                                           PlaceBean placeBean) {
        String placeName = placeBean.getPlaceName(Locale.SIMPLIFIED_CHINESE.toString());
        return ProductUtils.getPointUnchangeableText(navigatorPoint).equalsIgnoreCase(placeName);
    }

    public static boolean isNavigatorPoint(Constant.NavigatorPoint navigatorPoint,
                                           String placeName) {
        return checkStringByAllLanguage(ProductUtils.getNavigatorPointStringId(navigatorPoint), placeName);
    }

    public static boolean isChargingPoint(PlaceBean placeBean) {
        String placeName = placeBean.getPlaceName(Locale.SIMPLIFIED_CHINESE.toString());
        return Definition.START_BACK_CHARGE_POSE.equalsIgnoreCase(placeName);
    }

    public static boolean isChargingPoint(String placeName) {
        return checkStringByAllLanguage(R.string.charging_point, placeName);
    }

    public static boolean isChargingPile(String placeName) {
        return checkStringByAllLanguage(R.string.charging_pole, placeName);
    }

//    public static boolean isLocatePoint(PlaceBean placeBean) {
//        String placeName = placeBean.getPlaceName(Locale.SIMPLIFIED_CHINESE.toString());
//        return Definition.LOCATE_POSITION_POSE.equalsIgnoreCase(placeName);
//    }

    public static boolean isLocatePole(String placeName) {
        return checkStringByAllLanguage(R.string.positioning_spot, placeName);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static String getStringByLanguage(int resourceId, String language) {
        Context context = ApplicationWrapper.getApplicationContext();
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        String[] languageInfo = language.split(Definition.UNDERLINE);
        Locale locale = new Locale(languageInfo[0], languageInfo[1]);
        conf.setLocale(locale);
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources().getString(resourceId);
    }


    public static boolean checkStringByAllLanguage(int resourceId, String placeName) {
        if (TextUtils.isEmpty(placeName)) {
            return false;
        }
        if(sContext == null){
            sContext = ApplicationWrapper.getApplicationContext();
        }
        List<String> listLanguage = Arrays.asList(sContext.getResources().
                getStringArray(R.array.special_place_lang));
        for (String code : listLanguage) {
            String name = getStringByLanguage(resourceId, code);
            if (placeName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 特殊点位多语言名称列表，特殊点位配置的语言类型全集，便于本地增加新语言支持后的地图兼容
     */
    private static HashMap<String, List<String>> sSpecialPlace = new HashMap<>();

    private static class ModelPrefix {
        private static final String MINI_TOB = "MiniTob_SpecialPlace_";
        private static final String SAIPH = "Saiph_SpecialPlace_";
        private static final String DEFAULT = "SpecialPlace_";
    }

    public HashMap<String, List<String>> getLangSpecialPlace() {
        return sSpecialPlace;
    }

    @SuppressLint("LongLogTag")
    private void initSpecialPlaceLangName() {
        if(sContext == null){
            sContext = ApplicationWrapper.getApplicationContext();
        }
        List<String> listLanguage = Arrays.asList(sContext.getResources().
                getStringArray(R.array.special_place_lang));
        String prefix = getStringArrayNamePrefix();
        Log.d(TAG, "initSpecialPlaceLangName: listLanguage=" + listLanguage.toString()
                + " prefix=" + prefix);
        for (String language : listLanguage) {
            try {
                int arrayId = getArrayId(prefix + language);
                List<String> names = Arrays.asList(sContext.getResources().getStringArray(arrayId));
                if (names == null && names.size() <= 0) {
                    Log.e(TAG, "initSpecialPlaceLangName: Language " + language + " config null!");
                    continue;
                }
                Log.d(TAG, "initSpecialPlaceLangName: language=" + language + " names=" + names);
                sSpecialPlace.put(language, names);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "initSpecialPlaceLangName: Resource not found exception: language=" + language);
            }
        }
    }

    private int getArrayId(String name) {
        try {
            Field field = R.array.class.getField(name);
            return field.getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @SuppressLint("LongLogTag")
    private String getStringArrayNamePrefix() {
        String model = RobotSettings.getProductModel();
        Log.d(TAG, "getStringArrayNamePrefix: model=" + model);
        if (ProductInfo.ProductModel.CM_MINI_TOB.model.equals(model)) {
            return ModelPrefix.MINI_TOB;
        } else if (ProductInfo.isDeliveryProduct()) {
            return ModelPrefix.SAIPH;
        } else {
            return ModelPrefix.DEFAULT;
        }
    }

}
