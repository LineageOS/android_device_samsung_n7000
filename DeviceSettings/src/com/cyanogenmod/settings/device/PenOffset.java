/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.cyanogenmod.settings.device;

import java.io.IOException;
import android.content.Context;
import android.util.AttributeSet;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;

public class PenOffset extends ListPreference implements OnPreferenceChangeListener {

    private static final String FILE_PREFIX = "/sys/class/sec/sec_epen/tilt_offset_";

    private final String file;

    public PenOffset(Context context, AttributeSet attrs) {
        super(context, attrs);

        String direction=attrs.getAttributeValue(null,"direction");
        String handText=attrs.getAttributeValue(null,"hand");
        String hand = handText.equals("left")?"0":"1";
        String rotation=attrs.getAttributeValue(null,"rotation");

        file=FILE_PREFIX+direction+"_h"+hand+"_r"+rotation;

        this.setOnPreferenceChangeListener(this);
    }

    /*
     * values copied from wacom_i2c kernel driver
    private static String defaultTiltOffsets[][][] = {
        { { 120, 110, -85, -110, }, {-120,  120,  60, -130, } },
        { {-110, 110, 110, -150, }, {-130, -110, 130,   70, } },
    };
    */

    public static boolean isSupported() {
        return Utils.fileExists(FILE_PREFIX+"x_h0_r0");
    }

    /**
     * Restore tilt offset setting from SharedPreferences. (Write to kernel.)
     * @param context       The context to read the SharedPreferences from
     */
    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        for(int i=0;i<2;i++) {
            for(int j=0;j<2;j++) {
                for(int k=0;k<4;k++) {
                    String suffix = getTiltSuffix(i,j,k);
                    String key = DeviceSettings.KEY_PEN_TILT_OFFSET_PREFIX+suffix;
                    String filename = FILE_PREFIX + suffix;
                    Utils.writeValue(filename, sharedPrefs.getString(key,"0"));
                }
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Utils.writeValue(file, newValue.toString());
        return true;
    }

    private static String getTiltSuffix(int direction,int hand,int rotation)
    {
        String directionString;
        if (direction==0)
            directionString="x";
        else
            directionString="y";
        return directionString+"_h"+hand+"_r"+rotation;
    }
}
