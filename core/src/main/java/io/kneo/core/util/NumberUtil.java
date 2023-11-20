package io.kneo.core.util;


import io.kneo.core.server.EnvConst;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

public class NumberUtil {

    public static String formatFloat(float originalCost) {
        return NumberFormat.getInstance(new Locale(EnvConst.DEFAULT_COUNTRY_OF_NUMBER_FORMAT)).format(originalCost);
    }

    public static int getRandomNumber(int low, int high) {
        if (low == high) {
            return low;
        } else {
            Random r = new Random();
            return r.nextInt(high - low) + low;
        }
    }

    public static double getRandomNumberAsDouble(int low, int high) {
        if (low == high) {
            return low;
        } else {
            Random r = new Random();
            double val = ((double) low) + (((double) high) - ((double) low)) * r.nextDouble();
            return roundDouble(val);
        }
    }

    public static float getRandomNumberAsFloat(int low, int high) {
        float leftLimit = low;
        float rightLimit = high;
        return leftLimit + new Random().nextFloat() * (rightLimit - leftLimit);
    }

    public static int extractNumbers(String val) {
        try {
            return Integer.parseInt(val.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public static int stringToInt(String d, int defaultValue) {
        d = d.replaceAll("\\s+", "").replaceAll(",", "").replaceAll("/\\D/g", "").replaceAll("\\p{IsCyrillic}", "");
        try {
            return Integer.parseInt(d);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static double roundDouble(double val) {
        val = val * 100;
        val = Math.round(val);
        val = val / 100;
        return val;
    }

}