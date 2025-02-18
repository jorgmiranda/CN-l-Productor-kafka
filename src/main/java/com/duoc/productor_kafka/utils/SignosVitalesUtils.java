package com.duoc.productor_kafka.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SignosVitalesUtils {
    public static double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
