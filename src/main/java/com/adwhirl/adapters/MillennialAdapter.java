/*
 Copyright 2009-2010 AdMob, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.adwhirl.adapters;

import android.app.Activity;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import com.locadz.AdvertisingPreference;
import com.locadz.AdvertisingPreference.Gender;
import com.locadz.AdUnitLayout;
import com.locadz.LocadzUtils;
import com.locadz.model.Extra;
import com.locadz.model.Ration;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMAdView.MMAdListener;
import com.millennialmedia.android.MMAdViewSDK;

import java.util.Hashtable;

public class MillennialAdapter extends AdWhirlAdapter implements MMAdListener {

    public MillennialAdapter(AdUnitLayout locadzLayout, Ration ration, Extra extra) {
        super(locadzLayout, ration, extra);
    }


    @Override
    public void handle() {
        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }

        Hashtable<String, String> map = new Hashtable<String, String>();

        final AdvertisingPreference.Gender gender = getAdvertisingPreference().getGender();
        if (gender == Gender.MALE) {
            map.put(MMAdView.KEY_GENDER, "male");
        } else if (gender == Gender.FEMALE) {
            map.put(MMAdView.KEY_GENDER, "female");
        }

        final int age = getAdvertisingPreference().getAge();
        if (age != -1) {
            map.put(MMAdView.KEY_AGE, String.valueOf(age));
        }

        final String postalCode = getAdvertisingPreference().getPostalCode();
        if (!TextUtils.isEmpty(postalCode)) {
            map.put(MMAdView.KEY_ZIP_CODE, postalCode);
        }
        final String keywords = getAdvertisingPreference().getKeywords();
        if (!TextUtils.isEmpty(keywords)) {
            map.put(MMAdView.KEY_KEYWORDS, keywords);
        }

        // MM requests this pair to be specified
        map.put(MMAdView.KEY_VENDOR, "adwhirl");

        // Instantiate an ad view and add it to the view
        MMAdView adView = new MMAdView((Activity) locadzLayout.getContext(),
            ration.getKey(), MMAdView.BANNER_AD_TOP, MMAdView.REFRESH_INTERVAL_OFF, map);
        adView.setId(MMAdViewSDK.DEFAULT_VIEWID);
        adView.setListener(this);
        adView.callForAd();

        Location currentLocation = locadzLayout.getAdUnitContext().getLocation();
        if (getExtra().isLocationOn() == true && currentLocation != null) {
            adView.updateUserLocation(currentLocation);
        }

        adView.setHorizontalScrollBarEnabled(false);
        adView.setVerticalScrollBarEnabled(false);
    }

    public void MMAdReturned(MMAdView adView) {
        Log.d(LocadzUtils.LOG_TAG, "Millennial success");
        adView.setListener(null);

        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }
        locadzLayout.submitPushSubViewRequest(adView);
    }

    public void MMAdFailed(MMAdView adView) {
        Log.d(LocadzUtils.LOG_TAG, "Millennial failure");
        adView.setListener(null);
        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }
        locadzLayout.submitReloadAdRequest();
    }

    public void MMAdClickedToNewBrowser(MMAdView adview) {
        Log.d(LocadzUtils.LOG_TAG, "Millennial Ad clicked, new browser launched");
    }

    public void MMAdClickedToOverlay(MMAdView adview) {
        Log.d(LocadzUtils.LOG_TAG, "Millennial Ad Clicked to overlay");
    }

    public void MMAdOverlayLaunched(MMAdView adview) {
        Log.d(LocadzUtils.LOG_TAG, "Millennial Ad Overlay Launched");
    }

    public void MMAdRequestIsCaching(MMAdView adView) {
        //do nothing
    }

    public void MMAdCachingCompleted(MMAdView adview, boolean success) {
        // Do nothing. This callback is not used for banner ads.
    }
}
