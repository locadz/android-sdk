/*
 * Copyright 2012. Blue Tang Studio LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.locadz;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import java.net.URI;

/**
 *
 */
public class LocadzUtils {

    public static final String LOGID = "Locadz SDK";

    private static final String INFO_URI = "http://api.locadz.com/rest/v1?appid=%s&appver=%s&client=2&location=%s";

    public static final URI getInfoUri(AdUnitContext context) {

        String locationString = "";

        Location location = context.getLocation();
        if (location != null) {
            locationString = String.format("%.6f,%.6f", location.getLongitude(), location.getLatitude());
        }

        return URI.create(String.format(INFO_URI, context.getAdUnitId(), context.getAppVerion(), locationString));

    }

    /**
     * Retrieve api key from the context.
     * @param context   the context.
     * @return  the api key or null.
     */
    protected static String getAdUnitId(Context context) {
        final String packageName = context.getPackageName();
        final String activityName = context.getClass().getName();
        final PackageManager pm = context.getPackageManager();
        Bundle bundle = null;
        // Attempts to retrieve Activity-specific AdWhirl key first. If not
        // found, retrieve Application-wide AdWhirl key.
        try {
            ActivityInfo activityInfo = pm.getActivityInfo(new ComponentName(
                packageName, activityName), PackageManager.GET_META_DATA);
            bundle = activityInfo.metaData;
            if (bundle != null) {
                return bundle.getString(LocadzConstants.ADUNIT_KEY);
            }
        } catch (PackageManager.NameNotFoundException exception) {
            // Activity cannot be found. Shouldn't be here.
            return null;
        }

        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName,
                PackageManager.GET_META_DATA);
            bundle = appInfo.metaData;
            if (bundle != null) {
                return bundle.getString(LocadzConstants.ADUNIT_KEY);
            }
        } catch (PackageManager.NameNotFoundException exception) {
            // Application cannot be found. Shouldn't be here.
            return null;
        }
        return null;
    }

}