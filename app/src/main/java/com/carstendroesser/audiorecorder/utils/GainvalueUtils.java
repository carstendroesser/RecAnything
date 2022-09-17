package com.carstendroesser.audiorecorder.utils;

import android.content.Context;

/**
 * Created by carstendrosser on 03.12.17.
 */

public class GainvalueUtils {

    public static int getSelectedGainValueIndex(Context pContext) {
        float gainvalue = Settings.getGainValue(pContext);

        if (gainvalue == 1.0f) {
            return 0;
        } else if (gainvalue == 1.25f) {
            return 1;
        } else if (gainvalue == 1.58f) {
            return 2;
        } else if (gainvalue == 1.99f) {
            return 3;
        } else if (gainvalue == 2.51f) {
            return 4;
        } else if (gainvalue == 3.16f) {
            return 5;
        } else if (gainvalue == 3.98f) {
            return 6;
        } else if (gainvalue == 5.01f) {
            return 7;
        } else if (gainvalue == 6.3f) {
            return 8;
        }

        return 0;
    }

    public static float dbToGainValue(Context pContext, int pDb) {
        switch (pDb) {
            case 0:
                return 1.0f;
            case 2:
                return 1.25f;
            case 4:
                return 1.58f;
            case 6:
                return 1.99f;
            case 8:
                return 2.51f;
            case 10:
                return 3.16f;
            case 12:
                return 3.98f;
            case 14:
                return 5.01f;
            case 16:
                return 6.3f;
        }

        return 1;
    }

}
